package com.vyn.motoclick.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vyn.motoclick.R;
import com.vyn.motoclick.database.UserData;
import com.vyn.motoclick.utils.Constants;


/**
 * Created by Yurka on 15.06.2017.
 */

public class SplashActivity extends AppCompatActivity {

    static final String LOG_TAG = "myLogs";

    private static final int SPLASH_TIME_MS = 2 * 1000; //2 secs
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
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    finish();
                    startActivity(new Intent(SplashActivity.this, AuthenticationActivity.class));
                } else
                    getMyAccountFromFirebase();
            }
        };
        mHandler.postDelayed(mRunnable, SPLASH_TIME_MS);
    }

    //вытаскиваю свои данные и заполняю иими объекты +++
    private void getMyAccountFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection(Constants.ARG_USERS).document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Intent intent = new Intent(SplashActivity.this, MapsActivity.class);
                intent.putExtra(UserData.class.getCanonicalName(), documentSnapshot.toObject(UserData.class));
                startActivity(intent);
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