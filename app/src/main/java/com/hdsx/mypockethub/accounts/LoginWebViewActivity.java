package com.hdsx.mypockethub.accounts;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.util.Logs;

import static com.hdsx.mypockethub.accounts.LoginActivity.INTENT_EXTRA_URL;

public class LoginWebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        String userAgent = webView.getSettings().getUserAgentString();
        userAgent.replaceAll("Chrome/\\d{2}.\\d.\\d.\\d", "");
        webView.getSettings().setUserAgentString(userAgent);

        webView.loadUrl(getIntent().getStringExtra(INTENT_EXTRA_URL));
        webView.setWebViewClient(new WebViewClient() {

            MaterialDialog dialog = new MaterialDialog.Builder(LoginWebViewActivity.this)
                    .progress(true, 0)
                    .content(R.string.loading)
                    .build();

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                dialog.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                dialog.dismiss();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Logs.e(url);
                Uri uri = Uri.parse(url);
                return overrideOAuth(uri) && super.shouldOverrideUrlLoading(view, url);
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return overrideOAuth(request.getUrl()) && super.shouldOverrideUrlLoading(view, request);
            }

            private boolean overrideOAuth(Uri uri) {
                if (uri.getScheme().equals(getString(R.string.github_oauth_scheme))) {
                    Intent data = new Intent();
                    data.setData(uri);
                    setResult(RESULT_OK, data);
                    finish();
                    return true;
                }
                return false;
            }

        });
        setContentView(webView);
    }

}
