package com.immortal.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import androidx.preference.PreferenceManager;
import git.artdeell.mojo.prefs.LauncherPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

/** Converts Immortal touch-layout coordinates to the engine v9 controlmap format. */
public final class ControlLayoutBridge {
    private static final String PREFS = "immortal_controls";
    private static final String KEY = "layout_v1";
    private static final int DEFAULT_BG = 0x4D000000;
    private ControlLayoutBridge() {}

    public static File sync(Context context) throws Exception {
        SharedPreferences own = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = own.getString(KEY, "");
        if (raw == null || raw.trim().isEmpty()) return defaultLayoutFile(context);
        JSONArray sourceControls = new JSONArray(raw);
        JSONArray buttons = new JSONArray();
        JSONArray joysticks = new JSONArray();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float density = Math.max(0.5f, metrics.density);
        SharedPreferences settings = context.getSharedPreferences("immortal_settings", Context.MODE_PRIVATE);
        float opacity = Math.max(0f, Math.min(1f, settings.getInt("button_transparency", 100) / 100f));

        for (int i = 0; i < sourceControls.length(); i++) {
            JSONObject source = sourceControls.getJSONObject(i);
            String label = source.optString("label", "BUTTON").trim();
            String kind = source.optString("kind", "button");
            float x = clamp(source.optDouble("x", 0.5), 0f, 1f);
            float y = clamp(source.optDouble("y", 0.5), 0f, 1f);
            float w = clamp(source.optDouble("w", 0.1), 0.045f, 0.32f);
            float h = clamp(source.optDouble("h", 0.12), 0.045f, 0.32f);
            JSONObject target = new JSONObject();
            target.put("name", label.isEmpty() ? "BUTTON" : label);
            JSONArray keycodes = new JSONArray();
            int code = keyCode(label);
            if (code != 0) keycodes.put(code);
            while (keycodes.length() < 4) keycodes.put(0);
            target.put("keycodes", keycodes);
            target.put("dynamicX", "${screen_width} * " + decimal(x));
            target.put("dynamicY", "${screen_height} * " + decimal(y));
            target.put("width", Math.max(18, Math.round(metrics.widthPixels * w / density)));
            target.put("height", Math.max(18, Math.round(metrics.heightPixels * h / density)));
            target.put("isToggle", "SNEAK".equalsIgnoreCase(label));
            target.put("isSwipeable", false);
            target.put("passThruEnabled", false);
            target.put("opacity", opacity);
            target.put("bgColor", DEFAULT_BG);
            target.put("strokeColor", -1);
            target.put("strokeWidth", 0);
            target.put("cornerRadius", 18);
            target.put("displayInGame", true);
            target.put("displayInMenu", false);
            target.put("bitmapTag", JSONObject.NULL);
            if ("joystick".equalsIgnoreCase(kind)) {
                target.put("forwardLock", false);
                target.put("absolute", false);
                joysticks.put(target);
            } else {
                buttons.put(target);
            }
        }

        JSONObject root = new JSONObject();
        root.put("version", 9);
        root.put("scaledAt", 100);
        root.put("mControlDataList", buttons);
        root.put("mDrawerDataList", new JSONArray());
        root.put("mJoystickDataList", joysticks);

        File file = defaultLayoutFile(context);
        File temporary = new File(file.getParentFile(), file.getName() + ".part");
        try (FileOutputStream output = new FileOutputStream(temporary)) {
            output.write(root.toString(2).getBytes(StandardCharsets.UTF_8));
            output.getFD().sync();
        }
        if (file.exists() && !file.delete()) throw new java.io.IOException("Cannot replace engine control layout.");
        if (!temporary.renameTo(file)) throw new java.io.IOException("Cannot activate engine control layout.");
        if (LauncherPreferences.DEFAULT_PREF == null) {
            LauncherPreferences.DEFAULT_PREF = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        }
        LauncherPreferences.DEFAULT_PREF.edit().putString("defaultCtrl", file.getAbsolutePath()).apply();
        LauncherPreferences.PREF_DEFAULTCTRL_PATH = file.getAbsolutePath();
        return file;
    }

    private static File defaultLayoutFile(Context context) throws java.io.IOException {
        File root = context.getExternalFilesDir(null);
        if (root == null) root = context.getFilesDir();
        File folder = new File(root, "controlmap");
        if (!folder.exists() && !folder.mkdirs()) throw new java.io.IOException("Cannot create Minecraft controlmap folder.");
        return new File(folder, "default.json");
    }

    private static int keyCode(String label) {
        switch (label.toUpperCase(java.util.Locale.ROOT)) {
            // ControlData v9 stores Android KeyEvent keycodes, not GLFW keycodes.
            case "JUMP": return KeyEvent.KEYCODE_SPACE;
            case "SNEAK": return KeyEvent.KEYCODE_SHIFT_LEFT;
            case "ATTACK": return -3; // ControlData.SPECIALBTN_MOUSEPRI
            case "USE": return -4;    // ControlData.SPECIALBTN_MOUSESEC
            case "INVENTORY": return KeyEvent.KEYCODE_E;
            case "CHAT": return KeyEvent.KEYCODE_T;
            case "PAUSE": return KeyEvent.KEYCODE_ESCAPE;
            case "SPRINT": return KeyEvent.KEYCODE_CTRL_LEFT;
            case "DROP": return KeyEvent.KEYCODE_Q;
            case "SWAP HAND": return KeyEvent.KEYCODE_F;
            default: return 0;
        }
    }
    private static float clamp(double value, float min, float max) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return min;
        return Math.max(min, Math.min(max, (float) value));
    }
    private static String decimal(float value) {
        return String.format(java.util.Locale.ROOT, "%.5f", value);
    }
}