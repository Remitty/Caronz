package com.remitty.caronz.helper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.remitty.caronz.R;

public class WebViewActivity extends AppCompatActivity {
    private WebView webView;
    private boolean loading = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        initComponents();

        Bundle bundle = getIntent().getExtras();
        String uri = bundle.getString("uri");
        Log.d("webview url", uri);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        webView.loadUrl(uri);

    }

    private void initComponents() {

        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new MyBrowser());
    }

    private class MyBrowser extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if(!loading) {
                String url = request.getUrl().toString();
                Intent intent = new Intent();
                intent.putExtra("response", url);
                setResult(RESULT_OK, intent);
                finish();

                return true;
            }
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d("url finished", url);
            loading = false;
        }
    }
}
