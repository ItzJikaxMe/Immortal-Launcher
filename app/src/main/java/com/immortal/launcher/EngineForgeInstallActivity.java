package com.immortal.launcher;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import git.artdeell.mojo.LauncherActivity;
import git.artdeell.mojo.tasks.AsyncAssetManager;
import git.artdeell.mojo.instances.Instance;
import git.artdeell.mojo.instances.InstanceInstaller;
import git.artdeell.mojo.instances.Instances;
import git.artdeell.mojo.modding.forge.ForgelikeUtils;

/** Immortal-branded host for the upstream Forge/NeoForge installer pipeline. */
public final class EngineForgeInstallActivity extends LauncherActivity {
    public static final String EXTRA_GAME_VERSION = "immortal.forge.game";
    public static final String EXTRA_LOADER = "immortal.forge.loader";
    public static final String EXTRA_INSTALLED_VERSION = "immortal.forge.installed_version";

    private TextView status;
    private String gameVersion;
    private String loader;
    private boolean installerStarted;
    private boolean resultSent;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        gameVersion = getIntent().getStringExtra(EXTRA_GAME_VERSION);
        loader = getIntent().getStringExtra(EXTRA_LOADER);
        showStatus("Preparing " + loader + " installer…");
        if (gameVersion == null || gameVersion.isEmpty()
                || (!"Forge".equals(loader) && !"NeoForge".equals(loader))) {
            fail("Invalid Forge/NeoForge installation request.");
            return;
        }
        loadCompatibleBuilds();
    }

    private void loadCompatibleBuilds() {
        new Thread(() -> {
            try {
                ForgelikeUtils utils = "Forge".equals(loader)
                        ? ForgelikeUtils.FORGE_UTILS : ForgelikeUtils.NEOFORGE_UTILS;
                List<String> all = utils.downloadVersions();
                ArrayList<String> compatible = new ArrayList<>();
                ArrayList<String> labels = new ArrayList<>();
                if (all != null) {
                    for (String full : all) {
                        boolean matches;
                        if ("Forge".equals(loader)) {
                            matches = full.startsWith(gameVersion + "-");
                        } else {
                            matches = gameVersion.equals(utils.processVersionString(full));
                        }
                        if (matches) {
                            compatible.add(full);
                            labels.add(full);
                        }
                    }
                }
                runOnUiThread(() -> {
                    if (compatible.isEmpty()) {
                        fail("No " + loader + " builds were found for Minecraft " + gameVersion + ".");
                        return;
                    }
                    status.setText("Choose a compatible " + loader + " build for " + gameVersion + ".");
                    new android.app.AlertDialog.Builder(this)
                            .setTitle(loader + " • Minecraft " + gameVersion)
                            .setItems(labels.toArray(new String[0]), (dialog, which) ->
                                    installSelected(utils, compatible.get(which)))
                            .setNegativeButton("Cancel", (dialog, which) -> finish())
                            .setOnCancelListener(dialog -> finish())
                            .show();
                });
            } catch (Exception error) {
                runOnUiThread(() -> fail("Could not load " + loader + " builds: " + error.getMessage()));
            }
        }).start();
    }

    private void installSelected(ForgelikeUtils utils, String fullVersion) {
        showStatus("Installing Minecraft " + gameVersion + " and preparing " + loader + " " + fullVersion + "…");
        new Thread(() -> {
            try {
                new MinecraftInstaller(this, message -> runOnUiThread(() -> status.setText(message)))
                        .install(gameVersion);
                AsyncAssetManager.unpackComponents(getApplicationContext());
                status.post(() -> status.setText("Preparing official " + loader + " installer…"));

                InstanceInstaller installer = utils.createInstaller(fullVersion);
                if (installer == null) throw new java.io.IOException("The official installer could not be prepared.");
                Instance instance = Instances.createInstance(item -> {
                    item.name = "Immortal " + loader + " " + fullVersion;
                    item.icon = utils.getIconName();
                    item.sharedData = true;
                    item.installer = installer;
                    item.renderer = "opengles2";
                }, fullVersion);
                Instances.setSelectedInstance(instance);

                runOnUiThread(() -> {
                    installerStarted = true;
                    status.setText("Opening the official " + loader + " installer. Follow its installation window.");
                    installer.start();
                });
            } catch (Exception error) {
                runOnUiThread(() -> fail("Could not start " + loader + " installer: " + error.getMessage()));
            }
        }).start();
    }

    @Override protected void onResume() {
        super.onResume();
        if (!installerStarted || resultSent) return;
        try {
            Instance selected = Instances.loadSelectedInstance();
            if (selected != null && selected.installer == null
                    && selected.versionId != null && !selected.versionId.isEmpty()) {
                resultSent = true;
                Intent result = new Intent();
                result.putExtra(EXTRA_INSTALLED_VERSION, selected.versionId);
                setResult(RESULT_OK, result);
                Toast.makeText(this, loader + " installed: " + selected.versionId, Toast.LENGTH_LONG).show();
                finish();
            } else if (selected != null && selected.installer != null) {
                status.setText("The installer window closed before completing. You can go back and retry.");
            }
        } catch (Exception error) {
            status.setText("Could not verify installed profile: " + error.getMessage());
        }
    }

    private void showStatus(String message) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(24), dp(24), dp(24), dp(24));
        root.setBackgroundColor(Color.rgb(12, 12, 16));
        TextView brand = new TextView(this);
        brand.setText("IMMORTAL");
        brand.setTextColor(Color.WHITE);
        brand.setTextSize(22);
        brand.setGravity(Gravity.CENTER);
        root.addView(brand, new LinearLayout.LayoutParams(-1, dp(46)));
        status = new TextView(this);
        status.setText(message);
        status.setTextColor(Color.LTGRAY);
        status.setTextSize(13);
        status.setGravity(Gravity.CENTER);
        root.addView(status, new LinearLayout.LayoutParams(-1, dp(90)));
        setContentView(root);
    }

    private void fail(String message) {
        if (status != null) status.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
