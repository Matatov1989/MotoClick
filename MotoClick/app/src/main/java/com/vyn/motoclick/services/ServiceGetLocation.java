package com.vyn.motoclick.services;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vyn.motoclick.utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class ServiceGetLocation extends Service {
    static final String LOG_TAG = "myLogs";
    Location locationDevice;
    private FusedLocationProviderClient mFusedLocationClient;

    String userId;

    public ServiceGetLocation() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        userId = intent.getStringExtra("userIdUpdateLocation");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //       return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // Logic to handle location object
                    locationDevice = location;
                    Log.d(LOG_TAG, "locationDevice "+locationDevice );
                    stopSelf();
                } else {
                    stopSelf();
                }
            }
        });
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(Constants.ARG_USERS).child(userId).child(Constants.ARG_LOCATION);
        Map<String, Object> userValues = new HashMap<String, Object>();
        userValues.put(Constants.ARG_LAT, locationDevice.getLatitude());
        userValues.put(Constants.ARG_LON, locationDevice.getLongitude());
        mDatabase.updateChildren(userValues);
    }
}