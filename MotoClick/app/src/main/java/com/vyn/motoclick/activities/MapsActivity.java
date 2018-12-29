package com.vyn.motoclick.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.getbase.floatingactionbutton.FloatingActionButton;

import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.android.clustering.ClusterManager;
import com.vyn.motoclick.ClusterMarker;
import com.vyn.motoclick.MyClusterManagerRenderer;
import com.vyn.motoclick.R;
import com.vyn.motoclick.database.Chat;
import com.vyn.motoclick.database.UserData;
import com.vyn.motoclick.utils.Constants;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.vyn.motoclick.R.id.map;
import static java.lang.System.exit;
import static java.lang.System.in;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnInfoWindowClickListener {
    static final String LOG_TAG = "myLogs";

    LocationCallback locationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private final static long UPDATE_INTERVAL = 4 * 1000;  // 4 secs
    private final static long FASTEST_INTERVAL = 2 * 1000; // 2 secs

    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseFirestore db;

    double lat, lon;

    static public ArrayList<UserData> usersArrList = new ArrayList<>();

    ImageView imageDialogUser;

    Dialog dialogSettings;

    UserData userData;

    ImageView imageNavUser;
    TextView navSettingsUser;
    TextView textNavUserName;
    TextView textNavUserMoto;
    TextView textCntMsg;
    TextView deleteAccount;

    Uri selectedImage;

    private static final int REQUEST = 1;

    private GoogleMap mMap;
    //    private GoogleMap mGoogleMap;
    public ProgressDialog mProgressDialog;

    private static boolean sIsChatActivityOpen = false;

    private static boolean flagEnableLocation = false;

    private MapView mMapView;

    RequestOptions requestOptions;
    private LatLngBounds mMapBoundary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFragment.getMapAsync(this);

        //      mMapView = (MapView) findViewById(R.id.map);

        db = FirebaseFirestore.getInstance();

        Log.d(LOG_TAG, "onCreate  ");
        requestOptions = new RequestOptions()
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher);


        userData = (UserData) getIntent().getParcelableExtra(UserData.class.getCanonicalName());

        Log.d(LOG_TAG, "onCreate  " + userData.getUserName());
        Log.d(LOG_TAG, "onCreate  " + userData.getUserId());
        Log.d(LOG_TAG, "onCreate  s = " + userData.getUserListContacts().size());


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(MapsActivity.this);

        MenuItem menuItem = navigationView.getMenu().findItem(R.id.nav_messages);
        textCntMsg = (TextView) MenuItemCompat.getActionView(menuItem);

        final FloatingActionButton fabGPS = (FloatingActionButton) findViewById(R.id.fabGPS);
        fabGPS.setTitle(getString(R.string.btnFabOnGPS));
        fabGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flagEnableLocation) {
                    disableLocation();
                    fabGPS.setTitle(getString(R.string.btnFabOnGPS));
                    flagEnableLocation = false;
                } else {
                    enableLocation();
                    fabGPS.setTitle(getString(R.string.btnFabOffGPS));
                    flagEnableLocation = true;
                }
            }
        });

        final FloatingActionButton fabBtnLocation = (FloatingActionButton) findViewById(R.id.fabLocation);
        fabBtnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //      actionB.setTitle("Action B clicked");
                Log.d(LOG_TAG, "getLocation ");
                getLocation();
            }
        });

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        analiz();

        View headerView = navigationView.getHeaderView(0);
        imageNavUser = (ImageView) headerView.findViewById(R.id.imageNavUser);
        textNavUserName = (TextView) headerView.findViewById(R.id.textNavUserName);
        textNavUserMoto = (TextView) headerView.findViewById(R.id.textNavUserMoto);

        Glide.with(this)
                .setDefaultRequestOptions(requestOptions)
                .load(userData.getUserUriPhoto())
                .into(imageNavUser);

        textNavUserName.setText(userData.getUserName());
        //   textNavUserMoto.setText(userData.getUserMoto());

        navSettingsUser = (TextView) headerView.findViewById(R.id.navSettingsUser);
        navSettingsUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navSettingsUser.setTextColor(getResources().getColor(R.color.colorWhiteDark));

                Intent intent = new Intent(MapsActivity.this, EditProfileActivity.class);
                intent.putExtra(UserData.class.getCanonicalName(), userData);
                startActivity(intent);
            }
        });

   //     startActivity(new Intent(this, InfoUserActivity.class));
    }

    //get location updates
    public void enableLocation() {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // ---------------------------------- LocationRequest ------------------------------------
        // Create the location request to start receiving updates
        LocationRequest mLocationRequestHighAccuracy = new LocationRequest();
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
        mLocationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);


        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequestHighAccuracy, locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        Location location = locationResult.getLastLocation();
                        if (location != null) {
                            Log.d(LOG_TAG, "locationDevice " + location);
                      /*      DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(Constants.ARG_USERS).child(userData.getUserId()).child(Constants.ARG_LOCATION);
                            Map<String, Object> userValues = new HashMap<String, Object>();
                            userValues.put(Constants.ARG_LAT, location.getLatitude());
                            userValues.put(Constants.ARG_LON, location.getLongitude());
                            mDatabase.updateChildren(userValues);*/
                        }
                    }
                },
                Looper.myLooper());     // Looper.myLooper tells this to repeat forever until thread is destroyed
    }

    //stop location
    public void disableLocation() {
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(LOG_TAG, "onMapReady");
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);
    }

    private void analiz() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, userData.getUserId());
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, userData.getUserName());
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    //update user location
    private void getLocation() {
        Log.d(LOG_TAG, "getLocation");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //       return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    Log.d(LOG_TAG, "locationDevice " + location);

                    userData.setUserGeoPoint(new GeoPoint(location.getLatitude(), location.getLongitude()));
                    db.collection(Constants.ARG_USERS).document(userData.getUserId()).update(Constants.ARG_LOCATION, userData.getUserGeoPoint());

                    setCameraView();
                } else {
                }
            }
        });
    }

    //диалог инструкции +++
    public void dialogInstructions() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setCancelable(false);
        adb.setTitle(R.string.titleInstruction);
        adb.setMessage(R.string.dialogInstruction);
        adb.setIcon(android.R.drawable.ic_dialog_alert);
        adb.setPositiveButton(R.string.btnOK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        adb.show();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_messages:
                //      startActivity(new Intent(MapsActivity.this, ContactsActivity.class));

                Intent intentContact = new Intent(MapsActivity.this, ContactsActivity.class);
                intentContact.putExtra(UserData.class.getCanonicalName(), userData);
                startActivity(intentContact);

                break;
            case R.id.nav_instructions:
                dialogInstructions();
                break;
            case R.id.nav_advise_friend:
                //advise to friends
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.vyn.motoclick&hl");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;

            case R.id.nav_about_program:
                Intent intentAboutProgram = new Intent(MapsActivity.this, AboutProgram.class);
                intentAboutProgram.putExtra(UserData.class.getCanonicalName(), userData);
                startActivity(intentAboutProgram);
                break;

            case R.id.nav_feedback:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.vyn.motoclick&hl"));
                startActivity(intent);
                break;

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    public static boolean isChatActivityOpen() {
        Log.d(LOG_TAG, "isChatActivityOpen  " + sIsChatActivityOpen);
        return sIsChatActivityOpen;
    }

    public static void setChatActivityOpen(boolean isChatActivityOpen) {
        MapsActivity.sIsChatActivityOpen = isChatActivityOpen;
    }


    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;

    private void startUserLocationsRunnable() {

        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                retrieveUserLocations();
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void stopLocationUpdates() {
        mHandler.removeCallbacks(mRunnable);
    }

    private ArrayList<UserData> userListData = new ArrayList<>();

    private void retrieveUserLocations() {
        Log.d(LOG_TAG, "retrieveUserLocations  ");

        db.collection(Constants.ARG_USERS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int i = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(LOG_TAG, document.getId() + " => " + document.toObject(UserData.class));
                                UserData user = document.toObject(UserData.class);
                                userListData.add(user);

                                LatLng updatedLatLng = new LatLng(user.getUserGeoPoint().getLatitude(), user.getUserGeoPoint().getLongitude());

                                if (mClusterMarkers.size() != 0) {
                                    mClusterMarkers.get(i).setPosition(updatedLatLng);
                                    mClusterManagerRenderer.setUpdateMarker(mClusterMarkers.get(i++));
                                }
                            }
                        } else {
                            Log.d(LOG_TAG, "Error getting documents: ", task.getException());
                        }

                        if (mClusterMarkers.size() == 0)
                            addMapMarkers();
                    }
                });
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ClusterManager<ClusterMarker> mClusterManager;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    private ArrayList<Integer> listIndex = new ArrayList<>();

    private void addMapMarkers() {

        if (mMap != null) {

            if (mClusterManager == null) {
                mClusterManager = new ClusterManager<ClusterMarker>(getApplicationContext(), mMap);
            }
            if (mClusterManagerRenderer == null) {
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        this,
                        mMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }

            Log.d(LOG_TAG, "addMapMarkers userListData ");
            for (UserData userLocation : userListData) {

                try {
                    String snippet = "";
                    if (userLocation.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
                        snippet = "This is you";
                    } else {
                        snippet = "Determine route to " + userLocation.getUserName() + "?";
                    }

                    int avatar = R.drawable.ic_motorcycle; // set the default avatar
                    try {
                        avatar = Integer.parseInt(userLocation.getUserUriPhoto());
                    } catch (NumberFormatException e) {
                    }
                    ClusterMarker newClusterMarker = new ClusterMarker(
                            new LatLng(userLocation.getUserGeoPoint().getLatitude(), userLocation.getUserGeoPoint().getLongitude()),
                            userLocation.getUserName(),
                            snippet,
                            avatar,
                            userLocation
                    );
                    mClusterManager.addItem(newClusterMarker);
                    mClusterMarkers.add(newClusterMarker);

                } catch (NullPointerException e) {
                }
            }
            mClusterManager.cluster();
            setCameraView();
        }
    }

    private void setCameraView() {
        Log.d(LOG_TAG, "setCameraView =" + userData.getUserGeoPoint().getLatitude());
        // Set a boundary to start
        double bottomBoundary = userData.getUserGeoPoint().getLatitude() - .1;
        double leftBoundary = userData.getUserGeoPoint().getLongitude() - .1;
        double topBoundary = userData.getUserGeoPoint().getLatitude() + .1;
        double rightBoundary = userData.getUserGeoPoint().getLongitude() + .1;

        mMapBoundary = new LatLngBounds(new LatLng(bottomBoundary, leftBoundary), new LatLng(topBoundary, rightBoundary));

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
    }

    @Override
    public void onResume() {
        super.onResume();
//             mMapView.onResume();
        Log.d(LOG_TAG, "onResume");
        startUserLocationsRunnable(); // update user locations every 'LOCATION_UPDATE_INTERVAL'
    }

    @Override
    public void onPause() {
        //         mMapView.onPause();
        stopLocationUpdates(); // stop updating user locations
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
//        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        //      mMapView.onStop();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            moveTaskToBack(true);
            exit(0);
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (marker.getSnippet().equals("This is you")) {
            marker.hideInfoWindow();
        } else {
            final int index = Integer.parseInt(marker.getId().replaceAll("[\\D]", ""));

            Intent intent = new Intent(this, InfoUserActivity.class);
            intent.putExtra(UserData.class.getCanonicalName(), mClusterMarkers.get(index).getUser());
            intent.putExtra("name", userData.getUserName());
            intent.putExtra("uid", userData.getUserId());
            intent.putExtra("token", userData.getUserFirebaseToken());
            startActivity(intent);
        }
    }
}