package com.immortal.launcher;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.graphics.Color;
import android.content.pm.ActivityInfo;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import git.artdeell.mojo.LauncherActivity;
import git.artdeell.mojo.JVersionList;
import git.artdeell.mojo.NewJREUtil;
import git.artdeell.mojo.authenticator.AuthType;
import git.artdeell.mojo.authenticator.accounts.Account;
import git.artdeell.mojo.authenticator.accounts.Accounts;
import git.artdeell.mojo.extra.ExtraConstants;
import git.artdeell.mojo.extra.ExtraCore;
import git.artdeell.mojo.instances.Instance;
import git.artdeell.mojo.instances.Instances;
import git.artdeell.mojo.prefs.LauncherPreferences;
import git.artdeell.mojo.tasks.AsyncVersionList;
import git.artdeell.mojo.tasks.AsyncAssetManager;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Invisible engine host. Immortal owns the public UI; this activity reuses the
 * pinned Java game engine's account, instance, download, native extraction and
 * game-process pipeline without showing the engine's launcher UI.
 */
public final class EngineLaunchActivity extends LauncherActivity {
    public static final String EXTRA_VERSION_ID = "immortal.version_id";

    private TextView statusView;
    private boolean resumed;
    private boolean versionListReady;
    private boolean launchDispatched;
    private String requestedVersion;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setStatusBarColor(Color.rgb(12, 12, 16));
        getWindow().setNavigationBarColor(Color.rgb(12, 12, 16));
        showStatus("Preparing Minecraft Java engine…");

        requestedVersion = getIntent().getStringExtra(EXTRA_VERSION_ID);
        if (requestedVersion == null || requestedVersion.trim().isEmpty()) {
            fail("No Minecraft version was selected.");
            return;
        }

        try {
            LauncherPaths.migrateLegacyMinecraftDirectory(this);
            LauncherPaths.minecraftDirectory(this).mkdirs();
            LauncherPaths.sharedDirectory(this).mkdirs();

            applyImmortalPreferences();
            prepareLocalAccount();
            prepareInstance(requestedVersion);

            // These are the engine's packaged default controls, profile and runtime components.
            AsyncAssetManager.unpackSingleFiles(getApplicationContext());
            AsyncAssetManager.unpackComponents(getApplicationContext());

            showStatus("Checking Minecraft metadata and Java runtime…");
            new AsyncVersionList().getVersionList(versions -> runOnUiThread(() -> {
                if (versions == null) {
                    fail("Minecraft version metadata could not be loaded. Check the connection and try again.");
                    return;
                }
                ExtraCore.setValue(ExtraConstants.RELEASE_TABLE, versions);
                versionListReady = true;
                dispatchWhenResumed();
            }));
        } catch (Throwable error) {
            fail("Could not prepare the game session: " + error.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumed = true;
        dispatchWhenResumed();
    }

    @Override
    protected void onPause() {
        resumed = false;
        super.onPause();
    }

    private void dispatchWhenResumed() {
        if (!resumed || !versionListReady || launchDispatched) return;
        launchDispatched = true;
        showStatus("Downloading missing libraries and Android native files…");
        ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true);
    }

    private void applyImmortalPreferences() {
        if (LauncherPreferences.DEFAULT_PREF == null) {
            LauncherPreferences.DEFAULT_PREF = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        }

        SharedPreferences source = getSharedPreferences("immortal_settings", MODE_PRIVATE);
        SharedPreferences.Editor target = LauncherPreferences.DEFAULT_PREF.edit();

        target.putInt("allocation", source.getInt("ram_mb", 1536));
        target.putString("javaArgs", source.getString("jvm_args", ""));
        target.putString("renderer", mapRenderer(source.getString("renderer", "auto")));
        target.putInt("resolutionRatio", source.getInt("resolution_scale", 100));
        target.putBoolean("ignoreNotch", source.getBoolean("ignore_notch", false));
        target.putBoolean("sustainedPerformance", source.getBoolean("sustained_performance", false));
        target.putBoolean("alternate_surface", source.getBoolean("alternate_surface", true));
        target.putBoolean("force_vsync", source.getBoolean("vsync", false));
        target.putBoolean("use_angle", source.getBoolean("use_angle", false));
        target.putBoolean("java_sandbox", source.getBoolean("java_sandbox", true));

        target.putBoolean("disableGestures", source.getBoolean("disable_gestures", false));
        target.putBoolean("disableDoubleTap", source.getBoolean("disable_double_tap", false));
        target.putInt("timeLongPressTrigger", source.getInt("long_press_ms", 300));
        target.putInt("buttonscale", source.getInt("button_scale", 100));
        target.putInt("buttonTransparency", source.getInt("button_transparency", 100));
        target.putBoolean("buttonAllCaps", source.getBoolean("button_all_caps", true));
        target.putInt("mousescale", source.getInt("mouse_scale", 100));
        target.putInt("mousespeed", source.getInt("mouse_speed", 100));
        target.putBoolean("mouse_start", source.getBoolean("mouse_start", false));
        target.putBoolean("enableGyro", source.getBoolean("gyro_enabled", false));
        target.putInt("gyroSensitivity", source.getInt("gyro_sensitivity", 100));
        target.putInt("gyroSampleRate", source.getInt("gyro_rate", 20));
        target.putBoolean("gyroSmoothing", source.getBoolean("gyro_smoothing", true));
        target.putBoolean("gyroInvertX", source.getBoolean("gyro_invert_x", false));
        target.putBoolean("gyroInvertY", source.getBoolean("gyro_invert_y", false));
        target.putInt("gamepad_deadzone_scale", Math.max(50, 100 - source.getInt("gamepad_deadzone", 15)));
        target.putBoolean("keyboardAutoPanning", source.getBoolean("keyboard_autopan", true));

        target.putBoolean("checkGameFiles", source.getBoolean("verify_game_files", true));
        target.putBoolean("fastStartupCheck", source.getBoolean("fast_startup_check", true));
        target.putBoolean("verifyManifest", source.getBoolean("verify_manifest", true));
        target.putBoolean("showMemoryWarning", source.getBoolean("show_memory_warning", true));
        target.putString("downloadSource", source.getString("download_source", "default"));
        target.putBoolean("disableServerBlocklist", source.getBoolean("disable_server_blocklist", false));
        target.putBoolean("bigCoreAffinity", source.getBoolean("big_core_affinity", false));
        target.putBoolean("freedrenoSysmem", source.getBoolean("freedreno_sysmem", false));
        target.putBoolean("alsoftForceOpenSL", source.getBoolean("opensl_audio", false));
        target.putBoolean("ubwcWorkaround", source.getBoolean("ubwc_workaround", false));
        target.putBoolean("dump_shaders", source.getBoolean("dump_shaders", false));
        target.putBoolean("force_english", source.getBoolean("force_english", false));
        target.apply();

        LauncherPreferences.loadPreferences(this);
    }

    private String mapRenderer(String value) {
        if ("gles3".equalsIgnoreCase(value)) return "opengles3_ltw";
        if ("vulkan".equalsIgnoreCase(value)) return "vulkan_zink";
        return "opengles2";
    }

    private void prepareLocalAccount() throws Exception {
        String username = getSharedPreferences("immortal_settings", MODE_PRIVATE)
                .getString("offline_player", "Player").trim();
        if (username.isEmpty()) username = "Player";
        if (username.length() > 16) username = username.substring(0, 16);

        String profileId = UUID.nameUUIDFromBytes(
                ("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8))
                .toString().replace("-", "");

        Account current = Accounts.getCurrent();
        if (current == null) {
            final String playerName = username;
            final String uuid = profileId;
            Account created = Accounts.create(account -> {
                account.username = playerName;
                account.profileId = uuid;
                account.authType = AuthType.LOCAL;
                account.isMicrosoft = false;
                account.accessToken = "0";
                account.refreshToken = "0";
            });
            Accounts.setCurrent(created);
        } else if (current.authType == null || current.authType == AuthType.LOCAL) {
            current.username = username;
            current.profileId = profileId;
            current.authType = AuthType.LOCAL;
            current.isMicrosoft = false;
            current.accessToken = "0";
            current.refreshToken = "0";
            current.save();
            Accounts.setCurrent(current);
        }
    }

    private void prepareInstance(String versionId) throws Exception {
        String instanceName = "Immortal-" + versionId;
        Instance selected = null;
        List<Instance> instances = Instances.loadAllInstances();
        for (Instance item : instances) {
            if (instanceName.equals(item.name) && versionId.equals(item.versionId)) {
                selected = item;
                break;
            }
        }

        if (selected == null) {
            final String renderer = mapRenderer(
                    getSharedPreferences("immortal_settings", MODE_PRIVATE).getString("renderer", "auto"));
            final String args = getSharedPreferences("immortal_settings", MODE_PRIVATE).getString("jvm_args", "");
            selected = Instances.createInstance(instance -> {
                instance.name = instanceName;
                instance.versionId = versionId;
                instance.sharedData = true;
                instance.renderer = renderer;
                instance.jvmArgs = args;
                instance.argsMode = Instance.ARGS_MODE_REPLACE;
            }, "Immortal");
        } else {
            selected.versionId = versionId;
            selected.sharedData = true;
            selected.renderer = mapRenderer(
                    getSharedPreferences("immortal_settings", MODE_PRIVATE).getString("renderer", "auto"));
            selected.jvmArgs = getSharedPreferences("immortal_settings", MODE_PRIVATE).getString("jvm_args", "");
            selected.argsMode = Instance.ARGS_MODE_REPLACE;
            selected.write();
        }
        Instances.setSelectedInstance(selected);
    }

    private void showStatus(String message) {
        LinearLayout root = new LinearLayout(this);
        root.setGravity(Gravity.CENTER);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(36, 36, 36, 36);
        root.setBackgroundColor(Color.rgb(12, 12, 16));
        TextView brand = new TextView(this);
        brand.setText("IMMORTAL");
        brand.setTextColor(Color.WHITE);
        brand.setTextSize(22);
        brand.setGravity(Gravity.CENTER);
        root.addView(brand);
        statusView = new TextView(this);
        statusView.setText(message);
        statusView.setTextColor(Color.LTGRAY);
        statusView.setTextSize(13);
        statusView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = 18;
        root.addView(statusView, params);
        setContentView(root);
    }

    private void fail(String message) {
        if (statusView != null) statusView.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
