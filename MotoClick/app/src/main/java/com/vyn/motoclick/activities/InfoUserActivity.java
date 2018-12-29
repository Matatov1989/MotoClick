package com.vyn.motoclick.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vyn.motoclick.R;
import com.vyn.motoclick.database.Chat;
import com.vyn.motoclick.database.UserData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class InfoUserActivity extends AppCompatActivity implements OnMapReadyCallback {

    static final String LOG_TAG = "myLogs";

    UserData userData;

    TextView textInfoUserName;
    TextView textInfoUserVehicle;
    TextView textInfoUserLastVisit;

    ImageView imageInfoUserPhoto;

    private MapView mapView;
    private GoogleMap gmap;

    private static final String MAP_VIEW_BUNDLE_KEY = "AIzaSyDZTzXCqK-_yurRQKvt_x3n6RWhPN7_qAw";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_info_user);

        DateFormat FORMATTER = SimpleDateFormat.getDateTimeInstance();
        final String[] arrTypeVehicle = getResources().getStringArray(R.array.arrTypeVehicle);

        textInfoUserName = (TextView) findViewById(R.id.textInfoUserName);
        textInfoUserVehicle = (TextView) findViewById(R.id.textInfoUserVehicle);
        textInfoUserLastVisit = (TextView) findViewById(R.id.textInfoUserLastVisit);

        imageInfoUserPhoto = (ImageView) findViewById(R.id.imageInfoUserPhoto);

        userData = (UserData) getIntent().getParcelableExtra(UserData.class.getCanonicalName());

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);


        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher);

        Glide.with(this)
                .setDefaultRequestOptions(requestOptions)
                .load(userData.getUserUriPhoto())
                .into(imageInfoUserPhoto);

        textInfoUserName.setText(userData.getUserName());
        textInfoUserVehicle.setText(getString(R.string.textTypeVehicle, arrTypeVehicle[userData.getUserTypeVehicle()]));
        textInfoUserLastVisit.setText(getString(R.string.textLastVisit, FORMATTER.format(userData.getUserTimeStamp().toDate())));

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;
        gmap.setMinZoomPreference(12);
        LatLng ny = new LatLng(userData.getUserGeoPoint().getLatitude(), userData.getUserGeoPoint().getLongitude());
        gmap.addMarker(new MarkerOptions().position(new LatLng(userData.getUserGeoPoint().getLatitude(), userData.getUserGeoPoint().getLongitude())).title(userData.getUserName()));

        gmap.moveCamera(CameraUpdateFactory.newLatLng(ny));
    }

    public void onClickWay(View view) {
        String lat = String.valueOf(userData.getUserGeoPoint().getLatitude());
        String lon = String.valueOf(userData.getUserGeoPoint().getLongitude());
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lon + "&mode=d&avoid=h");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    public void onClickContact(View view) {
        Chat chat = new Chat(getIntent().getStringExtra("name"),
                getIntent().getStringExtra("uid"),getIntent().getStringExtra("token"),
                userData.getUserName(), userData.getUserId(), userData.getUserFirebaseToken());
        ChatActivity.startActivity(this, chat, userData);
    }
}
