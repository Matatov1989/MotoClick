package com.vyn.motoclick.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vyn.motoclick.R;
import com.vyn.motoclick.database.User;
import com.vyn.motoclick.location.LocationGPS;
import com.vyn.motoclick.utils.Constants;
import com.vyn.motoclick.utils.SharedPrefUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.vyn.motoclick.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {
    final String LOG_TAG = "myLogs";
    private GoogleMap mMap;

    Dialog dialog;
    LocationGPS locationGPS;

    String[] arrStatus;
    static public ArrayList<User> users;

    TextView nameUser;
    TextView mailUser;
    TextView statusUser;

    ImageView imageUser;
    TextView settingsUser;
    TextView textUserName;
    TextView textUserStatus;

    private SignInButton signInButton;
    private TextView btnPrivacyPolicy;

    int positionStatus = 0;

    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    public ProgressDialog mProgressDialog;

    private static final String TAG = "GoogleActivity";

    static String status = "0";
    static String local = "0";

    private static boolean sIsChatActivityOpen = false;

    public static final int MULTIPLE_PERMISSIONS = 1; // code you want.
    String[] permissions = new String[]{
            android.Manifest.permission.ACCESS_FINE_LOCATION,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        checkPermissions();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //      setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(MapsActivity.this);

        View headerView = navigationView.getHeaderView(0);
        imageUser = (ImageView) headerView.findViewById(R.id.imageUser);
        settingsUser = (TextView) headerView.findViewById(R.id.settingsUser);
        textUserName = (TextView) headerView.findViewById(R.id.textUserName);
        textUserStatus = (TextView) headerView.findViewById(R.id.textUserStatus);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            dialogAuthorization();
        } else {
            FirebaseAuth.getInstance().getCurrentUser().getUid();
            local = getLocation();

            Picasso.with(this)
                    .load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
                    .error(R.mipmap.ic_launcher)
                    .into(imageUser);

            textUserName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

            settingsUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    settingsUser.setTextColor(Color.RED);
                }
            });

            users = new ArrayList<>();
            getAllUsersFromFirebase();
        }
    }

    private String getLocation() {
        String local = "0.0";
        locationGPS = new LocationGPS(MapsActivity.this);
        // check if GPS enabled
        if (locationGPS.canGetLocation())
            local = locationGPS.getLatitude() + "-" + locationGPS.getLongitude() + "-";
        else
            locationGPS.showSettingsAlert();

        locationGPS.stopUsingGPS();
        return local;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(LOG_TAG, "onMapReady " );
        mMap = googleMap;

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            if (users.size() != 0) {
                for (int i = 0; i < users.size(); i++) {

                    String[] arrLocal = users.get(i).getLocation().split("-");
                //    Log.d(LOG_TAG, "arrLocal");
                //    Log.d(LOG_TAG, "1 " + arrLocal[0]);
                //    Log.d(LOG_TAG, "local net");
                    Double lat = Double.valueOf(arrLocal[0]);
                    Double lon = Double.valueOf(arrLocal[1]);
                    if (!users.get(i).getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                        status = users.get(i).getStatus();
                        switch (Integer.parseInt(status)) {
                            case 0:
                                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).snippet("" + i).icon(BitmapDescriptorFactory.fromResource(R.drawable.home)));
                                break;
                            case 1:
                                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).snippet("" + i).icon(BitmapDescriptorFactory.fromResource(R.drawable.way)));
                                break;
                            case 2:
                                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).snippet("" + i).icon(BitmapDescriptorFactory.fromResource(R.drawable.sos)));
                                break;
                            case 3:
                                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).snippet("" + i).icon(BitmapDescriptorFactory.fromResource(R.drawable.friends)));
                                break;
                            case 4:
                                textUserStatus.setText(getText(R.string.statusWant));
                                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).snippet("" + i).icon(BitmapDescriptorFactory.fromResource(R.drawable.want)));
                                break;
                        }



                    } else {
                        Picasso.with(this)
                                .load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
                                .error(R.mipmap.ic_launcher)
                                .into(imageUser);

                        textUserName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

                        status = users.get(i).getStatus();



                        Log.d(LOG_TAG, "onMapReady " +users.get(i).getStatus());



                        arrLocal = local.split("-");
                        lat = Double.valueOf(arrLocal[0]);
                        lon = Double.valueOf(arrLocal[1]);

                        switch (Integer.parseInt(status)) {
                            case 0:
                                textUserStatus.setText(getText(R.string.statusHome));
                                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).snippet(getText(R.string.markerSnippet).toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.home)));

                                break;
                            case 1:
                                textUserStatus.setText(getText(R.string.statusWay));     mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).snippet(getText(R.string.markerSnippet).toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.way)));

                                break;
                            case 2:
                                textUserStatus.setText(getText(R.string.statusSOS));
                                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).snippet(getText(R.string.markerSnippet).toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.sos)));

                                break;
                            case 3:
                                textUserStatus.setText(getText(R.string.statusFriends));
                                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).snippet(getText(R.string.markerSnippet).toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.friends)));

                                break;
                            case 4:
                                textUserStatus.setText(getText(R.string.statusWant));
                                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).snippet(getText(R.string.markerSnippet).toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.want)));

                                break;
                        }


                        //вид на карту
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(lat, lon))        //точка иерусалима
                                .zoom(15)                                            //зум
                                .bearing(45)                                    //поворот карт
                                .tilt(20)                                       //угол наклона
                                .build();
                        //И передаем полученный объект в метод newCameraPosition, получая CameraUpdate, который в свою очередь передаем в метод animateCamera
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                        mMap.animateCamera(cameraUpdate);
                    }
                }
            } else {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(31.7962994, 35.1053184))        //точка иерусалима
                        .zoom(6)                                            //зум
                        .bearing(45)                                    //поворот карт
                        .tilt(20)                                       //угол наклона
                        .build();

                //И передаем полученный объект в метод newCameraPosition, получая CameraUpdate, который в свою очередь передаем в метод animateCamera
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                mMap.animateCamera(cameraUpdate);
            }

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                @Override
                public boolean onMarkerClick(Marker arg0) {
                    Log.d(LOG_TAG, "setOnMarkerClickListener " + arg0.getSnippet());
                    if (!arg0.getSnippet().toString().equals(getText(R.string.markerSnippet))) {
                        int position = Integer.parseInt(arg0.getSnippet());
                        dialogInfoUser(users.get(position).getEmail(), users.get(position).getNameUser(), Integer.parseInt(users.get(position).getStatus()), users.get(position).getUriPhoto(), position);
                    }
                    return true;
                }

            });
        } else {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(31.7962994, 35.1053184))        //точка иерусалима
                    .zoom(6)                                            //зум
                    .bearing(45)                                    //поворот карт
                    .tilt(20)                                       //угол наклона
                    .build();
            //И передаем полученный объект в метод newCameraPosition, получая CameraUpdate, который в свою очередь передаем в метод animateCamera
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            mMap.animateCamera(cameraUpdate);
        }
    }

    public void getAllUsersFromFirebase() {
        FirebaseDatabase.getInstance().getReference().child(Constants.ARG_USERS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren().iterator();
                users = new ArrayList<>();
                while (dataSnapshots.hasNext()) {
                    DataSnapshot dataSnapshotChild = dataSnapshots.next();
                    User user = dataSnapshotChild.getValue(User.class);
                    users.add(user);
                }
                SupportMapFragment mapFragment =
                        (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(MapsActivity.this);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void dialogAuthorization() {
        LayoutInflater adbInflater = LayoutInflater.from(MapsActivity.this);
        View v = adbInflater.inflate(R.layout.dialog_authorization, null);

        signInButton = (SignInButton) v.findViewById(R.id.auth_button);
        btnPrivacyPolicy = (TextView) v.findViewById(R.id.btnPrivacyPolicy);

        AlertDialog.Builder adb = new AlertDialog.Builder(MapsActivity.this);
        adb.setCancelable(false);

        adb.setView(v);
        adb.setIcon(R.mipmap.ic_launcher);

        dialog = adb.show();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authorize();
            }
        });

        btnPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPrivacyPolicy.setTextColor(Color.RED);

                startActivity(new Intent(MapsActivity.this, PrivacyPolicyActivity.class));
            }
        });
    }

    private void authorize() {
        Intent authorizeIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(authorizeIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        showProgressDialog();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            local = getLocation();
                            addUserToDatabase(MapsActivity.this, task.getResult().getUser(), local, status);

                            hideProgressDialog();
                            dialog.dismiss();
                            getAllUsersFromFirebase();

                        } else {
                            Toast.makeText(MapsActivity.this, R.string.toastAuthFailed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void addUserToDatabase(final Context context, FirebaseUser firebaseUser, String local, String status) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        User user = new User(firebaseUser.getUid(),
                firebaseUser.getEmail(),
                new SharedPrefUtil(context).getString(Constants.ARG_FIREBASE_TOKEN),
                firebaseUser.getPhotoUrl().toString(),
                local,
                firebaseUser.getDisplayName(),
                status);
        database.child(Constants.ARG_USERS)
                .child(firebaseUser.getUid())
                .setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                        } else {
                        }
                    }
                });
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getText(R.string.dialogProgress));
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            Toast.makeText(MapsActivity.this, R.string.toastAuthFinish, Toast.LENGTH_SHORT).show();
        }
    }

    //диалог статуса
    private void dialogStatus() {
        arrStatus = new String[]{
                getText(R.string.statusHome).toString(),
                getText(R.string.statusWay).toString(),
                getText(R.string.statusSOS).toString(),
                getText(R.string.statusFriends).toString(),
                getText(R.string.statusWant).toString(),
        };
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setCancelable(false);
        adb.setTitle(R.string.titleStatus);
        adb.setIcon(R.drawable.artboard_green);
        adb.setSingleChoiceItems(arrStatus, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        status = "0";
                        textUserStatus.setText(R.string.statusHome);
                        break;
                    case 1:
                        status = "1";
                        textUserStatus.setText(R.string.statusWay);
                        break;
                    case 2:
                        status = "2";
                        textUserStatus.setText(R.string.statusSOS);
                        break;
                    case 3:
                        status = "3";
                        textUserStatus.setText(R.string.statusFriends);
                        break;
                    case 4:
                        status = "4";
                        textUserStatus.setText(R.string.statusFriends);
                        break;
                }
                addUserToDatabase(MapsActivity.this, FirebaseAuth.getInstance().getCurrentUser(), local, status);
                dialog.dismiss();
            }
        });
        dialog = adb.show();
    }

    //диалог инструкции
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
        dialog = adb.show();
    }

    //диалог информации пользователя
    public void dialogInfoUser(String mail, String name, int status, String uriPhoto, final int posotion) {
        LayoutInflater adbInflater = LayoutInflater.from(MapsActivity.this);
        View v = adbInflater.inflate(R.layout.dialog_info_user, null);

        imageUser = (ImageView) v.findViewById(R.id.imageUser);
        nameUser = (TextView) v.findViewById(R.id.textNameUser);
        mailUser = (TextView) v.findViewById(R.id.textMailUser);
        statusUser = (TextView) v.findViewById(R.id.textStatusUser);

        Picasso.with(this)
                .load(uriPhoto)
                .error(R.mipmap.ic_launcher)
                .into(imageUser);

        nameUser.setText(name);
        mailUser.setText(mailUser.getText().toString() + " " + mail);

        Log.d(LOG_TAG, "ssss " + status );
        switch (status) {
            case 0:
                statusUser.setText(statusUser.getText().toString() + " "+getText(R.string.statusHome));
                break;
            case 1:
                statusUser.setText(statusUser.getText().toString() + " "+getText(R.string.statusWay));
                break;
            case 2:
                statusUser.setText(statusUser.getText().toString() + " "+getText(R.string.statusSOS));
                break;
            case 3:
                statusUser.setText(statusUser.getText().toString() + " "+getText(R.string.statusFriends));
                break;
            case 4:
                statusUser.setText(statusUser.getText().toString() + " "+getText(R.string.statusWant));
                break;
        }

        AlertDialog.Builder adb = new AlertDialog.Builder(MapsActivity.this);
        adb.setCancelable(false);
        adb.setView(v);
        adb.setIcon(android.R.drawable.ic_input_add);
        adb.setPositiveButton(R.string.btnContact, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ChatActivity.startActivity(MapsActivity.this,
                        users.get(posotion).getEmail(),
                        users.get(posotion).getUid(),
                        users.get(posotion).getFirebaseToken());
                dialog.dismiss();
            }
        });
        adb.setNeutralButton(R.string.btnCancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        adb.setNegativeButton(R.string.btnWay, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                String local = users.get(posotion).getLocation();
                String []arrLcal = local.split("-");
                String lat = arrLcal[0];
                String lon = arrLcal[1];

                Uri gmmIntentUri = Uri.parse("google.navigation:q="+lat+","+lon+"&mode=d&avoid=h");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);

                dialog.dismiss();
            }
        });

        dialog = adb.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_status) {
            dialogStatus();
        } else if (id == R.id.nav_instructions) {
            dialogInstructions();
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_advise_friend) {
            //advise to friends
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "yes");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } else if (id == R.id.nav_about_program) {
            startActivity(new Intent(MapsActivity.this, PrivacyPolicyActivity.class));
        } else if (id == R.id.nav_from_developer) {
            //from developer
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/developer?id=Yurka+Sergeant+Matatov"));
            startActivity(intent);
        } else if (id == R.id.nav_link_fb) {
            //открыть ссылку в браузере
            Uri address = Uri.parse("https://www.facebook.com/IsraelTypicalMoto/");
            Intent openlink = new Intent(Intent.ACTION_VIEW, address);
            startActivity(openlink);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    public boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED)
                listPermissionsNeeded.add(p);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showDialogOK("yyyyyyyyyyyy", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    checkPermissions();
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    // proceed with logic by disabling the related features or quit the app.
                                    break;
                            }
                        }
                    });
                }
                return;
            }
        }
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(R.string.btnOK, okListener)
                .setNegativeButton(R.string.btnCancel, okListener)
                .create()
                .show();
    }


    public static boolean isChatActivityOpen() {
        //    Log.d(LOG_TAG, "onMessageReceived");
        return sIsChatActivityOpen;
    }

    public static void setChatActivityOpen(boolean isChatActivityOpen) {
        //      Log.d(LOG_TAG, "onMessageReceived");
        MapsActivity.sIsChatActivityOpen = isChatActivityOpen;
    }
}