package com.vyn.motoclick.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import com.vyn.motoclick.R;

/**
 * Created by Yurka on 21.06.2017.
 */

public class PrivacyPolicyActivity extends AppCompatActivity {

    private WebView mWebView;
    private String flagExit;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_privacy_policy);

        Intent intent = getIntent();
        if (intent.hasExtra("flagExit"))
            flagExit = intent.getStringExtra("flagExit");

        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);                          // turn on JavaScript
        mWebView.loadUrl("https://sites.google.com/view/motoclick-privacypolicy");  // set link
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (flagExit.equals("maps"))
            startActivity(new Intent(PrivacyPolicyActivity.this, MapsActivity.class));
        else if (flagExit.equals("aboutProgram"))
            startActivity(new Intent(PrivacyPolicyActivity.this, AboutProgram.class));
    }
}
