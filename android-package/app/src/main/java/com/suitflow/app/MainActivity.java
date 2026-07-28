package com.suitflow.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class MainActivity extends Activity {
    private static final int FILE_CHOOSER_REQUEST = 42;
    private static final String PASSWORD_SHA256 =
            "7c0a326bee24f1ca7d9b8a25f878cf14210f2a35c50f638ff9961323e8bc43d3";
    private static final String ACTIVATION_PREFS = "suitflow_activation";
    private static final String ACTIVATED_KEY = "activated";
    private ValueCallback<Uri[]> fileCallback;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (isActivated()) {
            showApp();
        } else {
            showActivationGate();
        }
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
                showApp();
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

    private void showApp() {
        WebView web = new WebView(this);
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(
                    WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (!"appassets.androidplatform.net".equals(uri.getHost())) return null;
                String path = uri.getPath();
                if (path == null || !path.startsWith("/tryon/")) return null;
                try {
                    InputStream stream = getAssets().open(path.substring(1));
                    String extension = MimeTypeMap.getFileExtensionFromUrl(path);
                    String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                    if (mime == null) mime = "application/octet-stream";
                    return new WebResourceResponse(mime, "UTF-8", stream);
                } catch (IOException ignored) {
                    return null;
                }
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(
                    WebView view,
                    ValueCallback<Uri[]> callback,
                    FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                Intent intent = params.createIntent();
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST);
                    return true;
                } catch (Exception error) {
                    fileCallback = null;
                    Toast.makeText(MainActivity.this, "Camera or gallery is unavailable", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });
        web.loadUrl("https://appassets.androidplatform.net/tryon/index.html");
        setContentView(web);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != FILE_CHOOSER_REQUEST || fileCallback == null) return;
        Uri[] result = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
        fileCallback.onReceiveValue(result);
        fileCallback = null;
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
