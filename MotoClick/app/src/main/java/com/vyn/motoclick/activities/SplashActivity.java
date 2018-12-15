package com.vyn.motoclick.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.vyn.motoclick.R;


/**
 * Created by Yurka on 15.06.2017.
 */

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_TIME_MS = 2000;
    private Handler mHandler;
    private Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_splash);
        mHandler = new Handler();

        mRunnable = new Runnable() {
            @Override
            public void run() {

                //the check is a new user
                if (FirebaseAuth.getInstance().getCurrentUser() != null)
                    startActivity(new Intent(SplashActivity.this, MapsActivity.class));
                else
                    startActivity(new Intent(SplashActivity.this, AuthenticationActivity.class));

                finish();
            }
        };
        mHandler.postDelayed(mRunnable, SPLASH_TIME_MS);
    }

    /*@Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.postDelayed(mRunnable, SPLASH_TIME_MS);
    }*/
}