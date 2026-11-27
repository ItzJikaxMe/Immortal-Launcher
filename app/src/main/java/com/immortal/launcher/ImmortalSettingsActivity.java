package com.immortal.launcher;

import android.os.Bundle;
import android.view.View;

import git.artdeell.mojo.LauncherActivity;
import git.artdeell.mojo.R;

/**
 * Hosts Mojo's original settings fragments inside Immortal.
 * The preference XML, preference classes, runtime manager, renderer and driver
 * dialogs remain the upstream Mojo implementations rather than re-created copies.
 */
public final class ImmortalSettingsActivity extends LauncherActivity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        View settingsButton = findViewById(R.id.setting_button);
        if (settingsButton != null) {
            settingsButton.post(settingsButton::performClick);
        }
    }

    @Override
    public void onBackPressed() {
        // Mojo's settings sub-pages use the fragment back stack. Close this
        // host when the root settings page is reached instead of showing Mojo's
        // separate home screen underneath Immortal.
        if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            finish();
            return;
        }
        super.onBackPressed();
    }
}
