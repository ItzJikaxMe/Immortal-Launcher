package com.immortal.launcher;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private final int blue = Color.rgb(83, 174, 255);
    private final int pale = Color.rgb(218, 237, 255);
    private final int muted = Color.rgb(167, 198, 225);
    private LinearLayout content;
    private String selectedTab = "Play";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        setContentView(buildScreen());
    }

    private View buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12), dp(10), dp(12), dp(10));
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(12, 38, 68), Color.rgb(8, 22, 43), Color.rgb(20, 57, 91)}));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER);
        root.addView(top, new LinearLayout.LayoutParams(-1, 0, 1f));

        LinearLayout character = panel();
        character.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(0, -1, 0.37f);
        leftParams.setMargins(0, 0, dp(10), 0);
        top.addView(character, leftParams);

        TextView characterTag = text("CHARACTER   ♧", 15, pale, true);
        characterTag.setGravity(Gravity.CENTER);
        character.addView(characterTag);
        TextView avatar = text("◉", 78, pale, true);
        avatar.setGravity(Gravity.CENTER);
        character.addView(avatar);
        TextView skinHint = text("Your Minecraft skin", 13, muted, false);
        character.addView(skinHint);
        TextView skinButton = pill("WARDROBE", blue, Color.rgb(7, 35, 66));
        LinearLayout.LayoutParams skinParams = new LinearLayout.LayoutParams(dp(150), dp(38));
        skinParams.topMargin = dp(16);
        character.addView(skinButton, skinParams);

        LinearLayout main = panel();
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(dp(18), dp(14), dp(18), dp(14));
        LinearLayout.LayoutParams mainParams = new LinearLayout.LayoutParams(0, -1, 0.56f);
        mainParams.setMargins(0, 0, dp(10), 0);
        top.addView(main, mainParams);

        TextView welcome = text("Welcome back", 17, muted, true);
        welcome.setGravity(Gravity.END);
        main.addView(welcome);
        TextView player = text("✦  Player", 25, pale, true);
        player.setGravity(Gravity.END);
        main.addView(player);

        TextView versionLabel = text("MINECRAFT VERSION", 15, pale, true);
        versionLabel.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(-1, -2);
        labelParams.topMargin = dp(14);
        main.addView(versionLabel, labelParams);

        LinearLayout editions = new LinearLayout(this);
        editions.setGravity(Gravity.CENTER);
        editions.setOrientation(LinearLayout.HORIZONTAL);
        main.addView(editions, new LinearLayout.LayoutParams(-1, dp(42)));
        editions.addView(pill("Rust Edition", Color.TRANSPARENT, pale),
                new LinearLayout.LayoutParams(0, -1, 1));
        editions.addView(pill("Java Edition  ✓", blue, Color.rgb(5, 37, 76)),
                new LinearLayout.LayoutParams(0, -1, 1));

        TextView version = pill("▼     Select Minecraft version", Color.rgb(17, 54, 88), pale);
        LinearLayout.LayoutParams versionParams = new LinearLayout.LayoutParams(-1, dp(48));
        versionParams.topMargin = dp(10);
        main.addView(version, versionParams);

        View spacer = new View(this);
        main.addView(spacer, new LinearLayout.LayoutParams(1, 0, 1));

        TextView play = pill("▦     PLAY     ▶", blue, Color.WHITE);
        play.setTextSize(19);
        LinearLayout.LayoutParams playParams = new LinearLayout.LayoutParams(-1, dp(50));
        playParams.topMargin = dp(8);
        main.addView(play, playParams);

        LinearLayout rail = panel();
        rail.setOrientation(LinearLayout.VERTICAL);
        rail.setGravity(Gravity.CENTER);
        top.addView(rail, new LinearLayout.LayoutParams(0, -1, 0.075f));
        String[] railItems = {"⌂", "▣", "↓", "♟", "▤"};
        for (String item : railItems) {
            TextView icon = text(item, 25, pale, true);
            icon.setGravity(Gravity.CENTER);
            rail.addView(icon, new LinearLayout.LayoutParams(-1, 0, 1));
        }

        LinearLayout bottom = panel();
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams bottomParams = new LinearLayout.LayoutParams(-1, dp(62));
        bottomParams.topMargin = dp(10);
        root.addView(bottom, bottomParams);
        String[][] tabs = {
                {"⚙", "Settings"}, {"♧", "Mods"}, {"♙", "Wardrobe"},
                {"●", "Accounts"}, {"▶", "Play"}
        };
        for (String[] tab : tabs) {
            TextView item = text(tab[0] + "\n" + tab[1], 13,
                    tab[1].equals(selectedTab) ? Color.rgb(5, 34, 69) : pale, true);
            item.setGravity(Gravity.CENTER);
            if (tab[1].equals(selectedTab)) {
                item.setBackground(round(blue, 30, 0x00000000));
            }
            bottom.addView(item, new LinearLayout.LayoutParams(0, -1, 1));
            item.setOnClickListener(v -> {
                selectedTab = tab[1];
                setContentView(buildScreen());
            });
        }
        return root;
    }

    private LinearLayout panel() {
        LinearLayout view = new LinearLayout(this);
        view.setOrientation(LinearLayout.VERTICAL);
        view.setPadding(dp(10), dp(10), dp(10), dp(10));
        view.setBackground(round(Color.argb(145, 20, 55, 88), 26, Color.argb(170, 190, 222, 255)));
        return view;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private TextView pill(String value, int background, int color) {
        TextView view = text(value, 15, color, true);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(10), 0, dp(10), 0);
        view.setBackground(round(background, 28, Color.argb(90, 180, 220, 255)));
        return view;
    }

    private GradientDrawable round(int color, int radius, int stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        if (stroke != 0) drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
