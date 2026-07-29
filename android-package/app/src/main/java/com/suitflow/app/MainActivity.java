package com.suitflow.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class MainActivity extends Activity {
    private static final String LATEST_SUITFLOW_URL =
            "https://suitflow-tryon-test.mukeshneupane13.chatgpt.site/";
    private static final String PASSWORD_SHA256 =
            "7c0a326bee24f1ca7d9b8a25f878cf14210f2a35c50f638ff9961323e8bc43d3";
    private static final String ACTIVATION_PREFS = "suitflow_activation";
    private static final String ACTIVATED_KEY = "activated";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (isActivated()) showLatestApp();
        else showActivationGate();
    }

    private boolean isActivated() {
        return getSharedPreferences(ACTIVATION_PREFS, MODE_PRIVATE)
                .getBoolean(ACTIVATED_KEY, false);
    }

    private void rememberActivation() {
        SharedPreferences preferences = getSharedPreferences(ACTIVATION_PREFS, MODE_PRIVATE);
        preferences.edit().putBoolean(ACTIVATED_KEY, true).apply();
    }

    private void showActivationGate() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(28), dp(28), dp(28), dp(28));
        root.setBackgroundColor(Color.rgb(16, 20, 22));

        TextView title = new TextView(this);
        title.setText("SuitFlow");
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText("Enter activation password once");
        subtitle.setTextColor(Color.rgb(200, 204, 205));
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subtitleParams.setMargins(0, dp(10), 0, dp(22));

        EditText password = new EditText(this);
        password.setHint("Password");
        password.setSingleLine(true);
        password.setTextColor(Color.WHITE);
        password.setHintTextColor(Color.rgb(145, 150, 151));
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        Button unlock = new Button(this);
        unlock.setText("Activate SuitFlow");
        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54));
        buttonParams.setMargins(0, dp(18), 0, 0);

        View.OnClickListener submit = view -> {
            if (hash(password.getText().toString()).equals(PASSWORD_SHA256)) {
                rememberActivation();
                showLatestApp();
            } else {
                password.setText("");
                password.requestFocus();
                Toast.makeText(this, "Incorrect activation password", Toast.LENGTH_SHORT).show();
            }
        };
        unlock.setOnClickListener(submit);
        password.setOnEditorActionListener((v, actionId, event) -> {
            submit.onClick(v);
            return true;
        });

        root.addView(title);
        root.addView(subtitle, subtitleParams);
        root.addView(password, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(unlock, buttonParams);
        setContentView(root);
    }

    private void showLatestApp() {
        WebView web = new WebView(this);
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> request.grant(request.getResources()));
            }
        });
        web.loadUrl(LATEST_SUITFLOW_URL);
        setContentView(web);
    }

    private String hash(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder output = new StringBuilder();
            for (byte item : bytes) output.append(String.format("%02x", item));
            return output.toString();
        } catch (Exception error) {
            return "";
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
