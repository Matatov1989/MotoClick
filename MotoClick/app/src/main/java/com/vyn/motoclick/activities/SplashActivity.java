package com.vyn.motoclick.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vyn.motoclick.R;
import com.vyn.motoclick.database.LocationData;
import com.vyn.motoclick.database.UserData;
import com.vyn.motoclick.utils.Constants;


/**
 * Created by Yurka on 15.06.2017.
 */

public class SplashActivity extends AppCompatActivity {

    static final String LOG_TAG = "myLogs";

    private static final int SPLASH_TIME_MS = 2 * 1000; //3 secs
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
                if (FirebaseAuth.getInstance().getCurrentUser() == null)
                    startActivity(new Intent(SplashActivity.this, AuthenticationActivity.class));
                else
                    getMyAccountFromFirebase();

                finish();
            }
        };
        mHandler.postDelayed(mRunnable, SPLASH_TIME_MS);
    }


    //вытаскиваю свои данные и заполняю иими объекты +++
    private void getMyAccountFromFirebase() {
        //    Log.d(LOG_TAG, "getMyAccountFromFirebase");

        FirebaseDatabase.getInstance().getReference()
                .child(Constants.ARG_USERS)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(LOG_TAG, "getMyAccountFromFirebase onDataChange " + dataSnapshot.getValue(UserData.class).getUserName());

                        String userName = dataSnapshot.getValue(UserData.class).getUserName();
                        String userPhoto = dataSnapshot.getValue(UserData.class).getUserUriPhoto();
                        //  myMoto = dataSnapshot.getValue(UserData.class).getUserMoto();

                        String userUid = dataSnapshot.getValue(UserData.class).getUserId();
                        String userToken = dataSnapshot.getValue(UserData.class).getUserFirebaseToken();
/*
                        lat = dataSnapshot.getValue(UserData.class).getUserLocation().getLatitude();
                        lon = dataSnapshot.getValue(UserData.class).getUserLocation().getLongitude();
                        */

                        LocationData locationData = new LocationData(dataSnapshot.getValue(UserData.class).getUserLocation().getLatitude(), dataSnapshot.getValue(UserData.class).getUserLocation().getLongitude());

                        UserData userData = new UserData(userUid, userName, userPhoto, locationData, userToken);
                        Intent intent = new Intent(SplashActivity.this, MapsActivity.class);
                        intent.putExtra(UserData.class.getCanonicalName(), userData);
                        startActivity(intent);

                        //    startActivity(new Intent(SplashActivity.this, AuthenticationActivity.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //          Log.d(LOG_TAG, "getMyAccountFromFirebase onCancelled " + databaseError);
                    }
                });
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