package com.vyn.motoclick.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import com.vyn.motoclick.R;

/**
 * Created by Yurka on 21.06.2017.
 */

public class PrivacyPolicyActivity extends AppCompatActivity {

    private WebView mWebView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_privacy_policy);

        mWebView = (WebView) findViewById(R.id.webView);
        // включаем поддержку JavaScript
        mWebView.getSettings().setJavaScriptEnabled(true);
        // указываем страницу загрузки
        mWebView.loadUrl("https://sites.google.com/view/motoclick-privacypolicy");
    }
/*
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(PrivacyPolicyActivity.this, MapsActivity.class));
    }*/
}
