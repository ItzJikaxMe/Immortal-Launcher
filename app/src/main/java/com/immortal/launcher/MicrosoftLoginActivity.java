package com.immortal.launcher;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import git.artdeell.mojo.authenticator.BackgroundLogin;
import git.artdeell.mojo.authenticator.accounts.Account;
import git.artdeell.mojo.authenticator.accounts.Accounts;
import git.artdeell.mojo.authenticator.impl.MicrosoftBackgroundLogin;
import git.artdeell.mojo.authenticator.listener.LoginListener;
import git.artdeell.mojo.prefs.LauncherPreferences;

/** Immortal-branded Microsoft OAuth flow backed by the engine's Minecraft account authenticator. */
public final class MicrosoftLoginActivity extends Activity {
    private static final String AUTH_URL =
            "https://login.live.com/oauth20_authorize.srf"
            + "?client_id=00000000402b5328"
            + "&response_type=code"
            + "&scope=service%3A%3Auser.auth.xboxlive.com%3A%3AMBI_SSL"
            + "&redirect_url=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf";
    private static final String REDIRECT = "https://login.live.com/oauth20_desktop.srf";

    private TextView status;
    private ProgressBar progress;
    private boolean exchangingCode;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        LauncherPreferences.DEFAULT_PREF = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        buildUi();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(12, 12, 16));
        root.setPadding(dp(16), dp(12), dp(16), dp(12));

        TextView title = new TextView(this);
        title.setText("IMMORTAL  •  MICROSOFT ACCOUNT");
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        title.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(48)));

        status = new TextView(this);
        status.setText("Sign in with the Microsoft account that owns Minecraft: Java Edition.");
        status.setTextColor(Color.LTGRAY);
        status.setTextSize(13);
        root.addView(status, new LinearLayout.LayoutParams(-1, dp(48)));

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setVisibility(android.view.View.GONE);
        root.addView(progress, new LinearLayout.LayoutParams(-1, dp(5)));

        WebView web = new WebView(this);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.setBackgroundColor(Color.WHITE);
        root.addView(web, new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(root);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(url);
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(request.getUrl().toString());
            }
            @Override public void onPageFinished(WebView view, String url) {
                if (!exchangingCode) status.setText("Complete the Microsoft sign-in in the secure page above.");
            }
        });
        web.loadUrl(AUTH_URL);
    }

    private boolean handleUrl(String address) {
        if (address == null || !address.startsWith(REDIRECT)) return false;
        if (exchangingCode) return true;
        Uri uri = Uri.parse(address);
        String error = uri.getQueryParameter("error");
        String code = uri.getQueryParameter("code");
        if (code == null || code.isEmpty()) {
            if ("access_denied".equals(error)) {
                status.setText("Sign-in was cancelled. You can go back and try again.");
            } else {
                status.setText("Microsoft sign-in failed: " + (error == null ? "No authorization code returned." : error));
            }
            return true;
        }
        exchangingCode = true;
        status.setText("Verifying your Microsoft and Xbox account, then checking Minecraft ownership…");
        progress.setVisibility(android.view.View.VISIBLE);
        progress.setIndeterminate(true);

        BackgroundLogin login = MicrosoftBackgroundLogin.CREATOR.create();
        login.createAccount(new LoginListener() {
            @Override public void onLoginDone(Account account) {
                try {
                    account.isMicrosoft = true;
                    account.save();
                    Accounts.setCurrent(account);
                    getSharedPreferences("immortal_settings", MODE_PRIVATE).edit()
                            .putBoolean("microsoft_signed_in", true)
                            .putString("microsoft_player", account.username)
                            .apply();
                    runOnUiThread(() -> {
                        status.setText("Signed in as " + account.username + ".");
                        Toast.makeText(MicrosoftLoginActivity.this, "Microsoft account connected.", Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                } catch (Throwable error) {
                    showFailure(error);
                }
            }
            @Override public void onLoginError(Throwable error) { showFailure(error); }
            @Override public void onLoginProgress(int step) {
                runOnUiThread(() -> status.setText("Microsoft authentication step " + step + "…"));
            }
            @Override public void setMaxLoginProgress(int max) {
                runOnUiThread(() -> {
                    progress.setIndeterminate(false);
                    progress.setMax(Math.max(1, max));
                });
            }
        }, code);
        return true;
    }

    private void showFailure(Throwable error) {
        runOnUiThread(() -> {
            exchangingCode = false;
            progress.setVisibility(android.view.View.GONE);
            String message = error == null || error.getMessage() == null
                    ? "Microsoft authentication failed." : error.getMessage();
            status.setText("Sign-in failed: " + message);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        });
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
