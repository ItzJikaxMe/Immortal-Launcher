package com.immortal.launcher;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(14, 13, 19);
    private static final int SURFACE = Color.rgb(28, 26, 35);
    private static final int SURFACE_2 = Color.rgb(37, 34, 45);
    private static final int TEXT = Color.rgb(239, 237, 245);
    private static final int MUTED = Color.rgb(160, 156, 174);
    private static final int ACCENT = Color.rgb(151, 111, 235);
    private LinearLayout page;
    private String selectedTab = "Play";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);
        setContentView(buildScreen());
    }

    private View buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(16), dp(10), dp(16), dp(8));

        root.addView(buildHeader(), new LinearLayout.LayoutParams(-1, dp(62)));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(-1, 0, 1f);
        scrollParams.topMargin = dp(8);
        root.addView(scroll, scrollParams);

        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(page);

        if ("Play".equals(selectedTab)) {
            addHomeContent(page);
        } else {
            addSectionContent(page, selectedTab);
        }

        root.addView(buildBottomNav(), new LinearLayout.LayoutParams(-1, dp(66)));
        return root;
    }

    private View buildHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView logo = label("I", 35, ACCENT, true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(round(SURFACE_2, 15, Color.rgb(65, 57, 82)));
        header.addView(logo, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.VERTICAL);
        brand.setPadding(dp(10), 0, 0, 0);
        TextView name = label("IMMORTAL", 19, TEXT, true);
        TextView version = label("0.1.0", 12, MUTED, false);
        brand.addView(name);
        brand.addView(version);
        header.addView(brand, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView settings = label("⚙", 23, MUTED, true);
        settings.setGravity(Gravity.CENTER);
        settings.setBackground(round(SURFACE, 50, Color.rgb(51, 47, 61)));
        header.addView(settings, new LinearLayout.LayoutParams(dp(44), dp(44)));
        settings.setOnClickListener(v -> openTab("Settings"));
        return header;
    }

    private void addHomeContent(LinearLayout host) {
        LinearLayout account = card();
        account.setOrientation(LinearLayout.HORIZONTAL);
        account.setGravity(Gravity.CENTER_VERTICAL);
        TextView avatar = label("✦", 22, ACCENT, true);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(round(SURFACE_2, 50, Color.TRANSPARENT));
        account.addView(avatar, new LinearLayout.LayoutParams(dp(44), dp(44)));
        LinearLayout accountText = new LinearLayout(this);
        accountText.setOrientation(LinearLayout.VERTICAL);
        accountText.setPadding(dp(12), 0, 0, 0);
        accountText.addView(label("Player", 16, TEXT, true));
        accountText.addView(label("Offline account", 12, MUTED, false));
        account.addView(accountText, new LinearLayout.LayoutParams(0, -2, 1f));
        account.addView(label("⌄", 22, MUTED, true));
        host.addView(account, params(-1, dp(72), 0, 0, 0, 12));

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.BOTTOM);
        hero.setPadding(dp(20), dp(20), dp(20), dp(20));
        GradientDrawable heroBg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(49, 45, 60), Color.rgb(24, 22, 30), Color.rgb(16, 15, 21)});
        heroBg.setCornerRadius(dp(22));
        heroBg.setStroke(dp(1), Color.rgb(58, 54, 68));
        hero.setBackground(heroBg);
        TextView ready = label("Ready to play", 25, TEXT, true);
        hero.addView(ready);
        TextView subtitle = label("Minecraft Java  •  Choose your version", 13, MUTED, false);
        LinearLayout.LayoutParams subParams = params(-1, -2, 0, 8, 0, 0);
        hero.addView(subtitle, subParams);
        host.addView(hero, params(-1, dp(176), 0, 0, 0, 14));

        TextView play = label("▶    PLAY MINECRAFT", 17, Color.WHITE, true);
        play.setGravity(Gravity.CENTER);
        play.setBackground(round(ACCENT, 18, Color.TRANSPARENT));
        host.addView(play, params(-1, dp(58), 0, 0, 0, 12));
        play.setOnClickListener(v -> showMessage("Game launch will be connected next."));

        LinearLayout version = card();
        version.setOrientation(LinearLayout.HORIZONTAL);
        version.setGravity(Gravity.CENTER_VERTICAL);
        version.addView(label("Minecraft version", 14, MUTED, false),
                new LinearLayout.LayoutParams(0, -2, 1f));
        version.addView(label("Select version  ⌄", 14, TEXT, true));
        host.addView(version, params(-1, dp(56), 0, 0, 0, 18));

        TextView section = label("LIBRARY", 12, MUTED, true);
        host.addView(section, params(-1, -2, dp(3), 0, 0, 9));
        String[][] items = {
                {"▦", "Mods"}, {"▰", "Resource Packs"},
                {"☼", "Shaders"}, {"⬡", "Modpacks"},
                {"♟", "Skins"}, {"▤", "Game Files"}
        };
        for (int i = 0; i < items.length; i += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.addView(categoryCard(items[i][0], items[i][1]), new LinearLayout.LayoutParams(0, dp(88), 1f));
            LinearLayout.LayoutParams second = new LinearLayout.LayoutParams(0, dp(88), 1f);
            second.leftMargin = dp(10);
            row.addView(categoryCard(items[i + 1][0], items[i + 1][1]), second);
            host.addView(row, params(-1, dp(88), 0, 0, 0, 10));
        }
        LinearLayout memory = card();
        memory.setOrientation(LinearLayout.HORIZONTAL);
        memory.setGravity(Gravity.CENTER_VERTICAL);
        memory.addView(label("▥   Memory", 14, MUTED, true),
                new LinearLayout.LayoutParams(0, -2, 1f));
        memory.addView(label("2.0 GB / 6.0 GB", 14, TEXT, true));
        host.addView(memory, params(-1, dp(54), 0, 8, 0, 12));
    }

    private View categoryCard(String icon, String title) {
        LinearLayout item = card();
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL);
        TextView symbol = label(icon, 23, ACCENT, true);
        symbol.setGravity(Gravity.CENTER);
        item.addView(symbol, new LinearLayout.LayoutParams(dp(38), dp(42)));
        TextView name = label(title, 14, TEXT, true);
        name.setPadding(dp(8), 0, 0, 0);
        item.addView(name);
        item.setOnClickListener(v -> openTab(title));
        return item;
    }

    private void addSectionContent(LinearLayout host, String tab) {
        host.addView(label(tab, 27, TEXT, true), params(-1, -2, dp(12), dp(14), 0, 6));
        host.addView(label("Manage your Minecraft Java " + tab.toLowerCase() + ".", 14, MUTED, false),
                params(-1, -2, 0, 0, 0, 18));
        LinearLayout empty = card();
        empty.setGravity(Gravity.CENTER);
        empty.addView(label("Your " + tab + " will appear here", 15, MUTED, false));
        host.addView(empty, params(-1, dp(150), 0, 0, 0, 12));
    }

    private View buildBottomNav() {
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setBackground(round(SURFACE, 20, Color.rgb(49, 45, 59)));
        String[][] tabs = {{"▶", "Play"}, {"▦", "Mods"}, {"▰", "Packs"}, {"☼", "Shaders"}, {"⚙", "Settings"}};
        for (String[] tab : tabs) {
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            TextView icon = label(tab[0], 19, tab[1].equals(selectedTab) ? ACCENT : MUTED, true);
            TextView name = label(tab[1], 10, tab[1].equals(selectedTab) ? TEXT : MUTED, true);
            item.addView(icon);
            item.addView(name);
            nav.addView(item, new LinearLayout.LayoutParams(0, -1, 1f));
            item.setOnClickListener(v -> openTab(tab[1]));
        }
        return nav;
    }

    private void openTab(String tab) {
        if ("Packs".equals(tab)) tab = "Resource Packs";
        selectedTab = tab;
        setContentView(buildScreen());
    }

    private LinearLayout card() {
        LinearLayout box = new LinearLayout(this);
        box.setPadding(dp(14), dp(10), dp(14), dp(10));
        box.setBackground(round(SURFACE, 18, Color.rgb(51, 47, 61)));
        return box;
    }

    private TextView label(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private GradientDrawable round(int color, int radius, int stroke) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(radius));
        if (stroke != Color.TRANSPARENT) shape.setStroke(dp(1), stroke);
        return shape;
    }

    private LinearLayout.LayoutParams params(int width, int height, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(width, height);
        p.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return p;
    }

    private void showMessage(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
