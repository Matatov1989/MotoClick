package com.vyn.motoclick.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.vyn.motoclick.R;
import com.vyn.motoclick.adapters.StatusListAdapter;
import com.vyn.motoclick.database.User;
import com.vyn.motoclick.location.LocationGPS;
import com.vyn.motoclick.utils.Constants;
import com.vyn.motoclick.utils.SharedPrefUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.vyn.motoclick.R.id.map;
import static java.lang.System.exit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {
    final String LOG_TAG = "myLogs";

    String myName;
    String myPhoto;
    String myMoto;
    String myPhone;
    String myStatus;
    String local;

    TextView deleteAccount;

    String statusUser;

    boolean flagSelectFoto = false;

    Dialog dialog, dialog1;
    LocationGPS locationGPS;

    String[] arrStatus;
    static public ArrayList<User> users;

    ImageView dialogImageUser;
    TextView dialogNameUser;
    TextView dialogStatusUser;
    TextView dialogMotoUser;
    TextView dialogPhoneUser;

    ImageView imageUser;
    TextView settingsUser;
    TextView textUserName;
    TextView textUserStatus;

    ImageView imageProf;

    ListView listStatus;
    Uri selectedImage;

    private SignInButton signInButton;
    private TextView btnPrivacyPolicy;

    private static final int RC_SIGN_IN = 9001;
    private static final int REQUEST = 1;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    public ProgressDialog mProgressDialog;

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
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFragment.getMapAsync(this);

        checkPermissions();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //          setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(MapsActivity.this);

        FloatingActionButton fabMyLocal = (FloatingActionButton) findViewById(R.id.fabMyLocal);
        fabMyLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAllUsersFromFirebase();
            }
        });

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

        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            dialogAuthorization();
        else {
            FirebaseAuth.getInstance().getCurrentUser().getUid();
            local = getLocation();
            getOneUserFromFirebase(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());
            users = new ArrayList<>();
            getAllUsersFromFirebase();
        }

        settingsUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsUser.setTextColor(getResources().getColor(R.color.colorWhiteDark));
                dialogSettingsUser();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAllUsersFromFirebase();
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

        Bitmap img = null;
        if (requestCode == REQUEST && resultCode == RESULT_OK) {
            selectedImage = data.getData();
            try {
                img = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                imageProf.setImageBitmap(img);
                flagSelectFoto = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void dialogSettingsUser() {
        LayoutInflater adbInflater = LayoutInflater.from(MapsActivity.this);
        View v = adbInflater.inflate(R.layout.dialog_settings_user, null);

        imageProf = (ImageView) v.findViewById(R.id.imageUser);
        deleteAccount = (TextView) v.findViewById(R.id.btnDeleteAccount);

        TextInputLayout tilName = (TextInputLayout) v.findViewById(R.id.textInputLayoutName);
        final EditText editName = (EditText) tilName.findViewById(R.id.editTextName);

        TextInputLayout tilPhone = (TextInputLayout) v.findViewById(R.id.textInputLayoutPhone);
        final EditText editPhone = (EditText) tilPhone.findViewById(R.id.editTextPhone);

        TextInputLayout tilMoto = (TextInputLayout) v.findViewById(R.id.textInputLayoutMoto);
        final EditText editMoto = (EditText) tilMoto.findViewById(R.id.editTextMoto);

        Picasso.with(this)
                .load(myPhoto)
                .error(R.mipmap.ic_launcher)
                .into(imageProf);

        editName.setText(myName);
        editPhone.setText(myPhone);
        editMoto.setText(myMoto);

        editName.setSelection(editName.getText().toString().length());
        editPhone.setSelection(editPhone.getText().toString().length());
        editMoto.setSelection(editMoto.getText().toString().length());

        final AlertDialog.Builder adb = new AlertDialog.Builder(MapsActivity.this);
        adb.setCancelable(true);
        adb.setView(v);
        adb.setPositiveButton(R.string.btnSave, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (editName.length() != 0) {
                    if (flagSelectFoto == true) {
                        Log.d(LOG_TAG, "5 ");
                        myName = editName.getText().toString();
                        myPhone = editPhone.getText().toString();
                        myMoto = editMoto.getText().toString();
                        uploadPickInFirebase(FirebaseAuth.getInstance().getCurrentUser());
                        flagSelectFoto = false;
                    } else {
                        updateUserToDatabase(MapsActivity.this, FirebaseAuth.getInstance().getCurrentUser(), editName.getText().toString(), myPhoto, myStatus, editPhone.getText().toString(), editMoto.getText().toString());
                        getOneUserFromFirebase(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());
                        textUserName.setText(myName);
                    }
                    settingsUser.setTextColor(getResources().getColor(R.color.colorWhite));
                    dialog1.dismiss();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            R.string.toastEnterName,
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    dialogSettingsUser();
                }
            }
        });
        adb.setNegativeButton(R.string.btnCancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                settingsUser.setTextColor(getResources().getColor(R.color.colorWhite));
                dialog1.dismiss();
            }
        });

        dialog1 = adb.show();

        //chang foto
        imageProf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, REQUEST);
            }
        });

        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount.setTextColor(getResources().getColor(R.color.colorWhiteDark));
                dialogDelAccount();
            }
        });
    }

    private void dialogDelAccount() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setCancelable(true);
        adb.setMessage(R.string.dialogDelAccount);
        adb.setPositiveButton(R.string.btnOK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                dialog1.dismiss();
                deleteAccount();
                myName = "";
                myPhone = "";
                myMoto = "";
                myPhoto = myPhone + "1";
                myStatus = "";

                Picasso.with(MapsActivity.this)
                        .load(myPhoto)
                        .error(R.mipmap.ic_launcher)
                        .into(imageUser);

                textUserName.setText("");
                textUserStatus.setText(R.string.textStatus);
                settingsUser.setVisibility(View.INVISIBLE);

            }
        });
        adb.setNeutralButton(R.string.btnCancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deleteAccount.setTextColor(getResources().getColor(R.color.colorBlack));
                dialog.dismiss();
            }
        });
        dialog = adb.show();
    }

    private void deleteAccount() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            R.string.toastDelAccount,
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(MapsActivity.this, R.string.toastDelAccountError, Toast.LENGTH_SHORT).show();
                    deleteAccount.setTextColor(getResources().getColor(R.color.colorBlack));
                }
            }
        });
    }

    private void uploadPickInFirebase(FirebaseUser firebaseUser) {
        showProgressDialog(getText(R.string.dialogProgressPick).toString());
        StorageReference storageReference =
                FirebaseStorage.getInstance()
                        .getReference(firebaseUser.getUid())
                        //          .child(selectedImage)
                        .child(selectedImage.getLastPathSegment());
        storageReference.putFile(selectedImage);
        storageReference.putFile(selectedImage).addOnCompleteListener(MapsActivity.this,
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            myPhoto = task.getResult().getDownloadUrl().toString();
                            //            updateUserFotoToDatabase(MapsActivity.this, FirebaseAuth.getInstance().getCurrentUser(),uriPhotoUser);
                            //           updateUserToDatabase(MapsActivity.this, FirebaseAuth.getInstance().getCurrentUser(), nameDisplayUser, uriPhotoUser, status);

                            updateUserToDatabase(MapsActivity.this, FirebaseAuth.getInstance().getCurrentUser(), myName, myPhoto, myStatus, myPhone, myMoto);
                            getOneUserFromFirebase(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());

                            textUserName.setText(myName);

                            Picasso.with(MapsActivity.this)
                                    .load(task.getResult().getDownloadUrl())
                                    .error(R.mipmap.ic_launcher)
                                    .into(imageUser);

                            hideProgressDialog(getText(R.string.toastPickFinish).toString());
                        } else {
                            task.getException();
                        }
                    }
                });
    }

    public void updateUserToDatabase(final Context context, FirebaseUser firebaseUser, String name, String photo, String status, String phone, String moto) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        User user = new User(firebaseUser.getUid(),
                firebaseUser.getEmail(),
                new SharedPrefUtil(context).getString(Constants.ARG_FIREBASE_TOKEN),
                photo,
                local,
                name,
                status,
                phone,
                moto);
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

    private String getLocation() {
        local = "0.0+0.0+";
        locationGPS = new LocationGPS(MapsActivity.this);
        // check if GPS enabled
        if (locationGPS.canGetLocation())
            local = locationGPS.getLatitude() + "+" + locationGPS.getLongitude() + "+";
        else
            locationGPS.showSettingsAlert();
        locationGPS.stopUsingGPS();
        return local;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            if (users.size() != 0) {
                for (int i = 0; i < users.size(); i++) {

                    String[] arrLocal = users.get(i).getLocation().split("\\+");
                    Double lat = Double.valueOf(arrLocal[0]);
                    Double lon = Double.valueOf(arrLocal[1]);
                    if (!users.get(i).getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        if (lat != 0.0 && lon != 0.0) {
                            statusUser = users.get(i).getStatus();
                            switch (Integer.parseInt(statusUser)) {
                                case 0:
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).snippet("" + i).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_status_home)));
                                    break;
                                case 1:
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).snippet("" + i).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_status_want)));
                                    break;
                                case 2:
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).snippet("" + i).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_status_way)));
                                    break;
                                case 3:
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).snippet("" + i).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_status_sos)));
                                    break;
                                case 4:
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).snippet("" + i).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_status_friends)));
                                    break;
                            }
                        }
                    } else {

                        //    Picasso.with(this)
                        //             .load(uriPhotoUser)
                        //           .error(R.mipmap.ic_launcher)
                        //           .into(imageUser);

                        //    textUserName.setText(nameDisplayUser);

                        myStatus = users.get(i).getStatus();

                        //            Log.d(LOG_TAG, "onMapReady " + users.get(i).getStatus());

                        arrLocal = local.split("\\+");
                        lat = Double.valueOf(arrLocal[0]);
                        lon = Double.valueOf(arrLocal[1]);

                        if (lat == 0.0 && lon == 0.0) {
                            Toast.makeText(MapsActivity.this, "Check settings location", Toast.LENGTH_SHORT).show();
                        } else {
                            switch (Integer.parseInt(myStatus)) {
                                case 0:
                                    textUserStatus.setText(getText(R.string.textStatus) + " " + getText(R.string.statusHome));
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).snippet(getText(R.string.markerSnippet).toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_status_home)));
                                    break;
                                case 1:
                                    textUserStatus.setText(getText(R.string.textStatus) + " " + getText(R.string.statusWant));
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).snippet(getText(R.string.markerSnippet).toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_status_want)));
                                    break;
                                case 2:
                                    textUserStatus.setText(getText(R.string.textStatus) + " " + getText(R.string.statusWay));
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).snippet(getText(R.string.markerSnippet).toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_status_way)));
                                    break;
                                case 3:
                                    textUserStatus.setText(getText(R.string.textStatus) + " " + getText(R.string.statusSOS));
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).snippet(getText(R.string.markerSnippet).toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_status_sos)));
                                    break;
                                case 4:
                                    textUserStatus.setText(getText(R.string.textStatus) + " " + getText(R.string.statusFriends));
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).snippet(getText(R.string.markerSnippet).toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_status_friends)));
                                    break;

                            }
                            //вид на карту
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(new LatLng(lat, lon))        //точка иерусалима
                                    .zoom(15)                                            //зум
                                    .bearing(0)                                    //поворот карт
                                    .tilt(20)                                       //угол наклона
                                    .build();
                            //И передаем полученный объект в метод newCameraPosition, получая CameraUpdate, который в свою очередь передаем в метод animateCamera
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                            mMap.animateCamera(cameraUpdate);
                        }
                    }
                }
            } else {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(31.7962994, 35.1053184))        //точка иерусалима
                        .zoom(6)                                            //зум
                        .bearing(0)                                    //поворот карт
                        .tilt(20)                                       //угол наклона
                        .build();

                //И передаем полученный объект в метод newCameraPosition, получая CameraUpdate, который в свою очередь передаем в метод animateCamera
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                mMap.animateCamera(cameraUpdate);
            }

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                @Override
                public boolean onMarkerClick(Marker arg0) {
                    if (!arg0.getSnippet().toString().equals(getText(R.string.markerSnippet))) {
                        int position = Integer.parseInt(arg0.getSnippet());
                        dialogInfoUser(users.get(position).getNameUser(), Integer.parseInt(users.get(position).getStatus()), users.get(position).getUriPhoto(), users.get(position).getMoto(), users.get(position).getPhone(), position);
                    }
                    return true;
                }

            });
        } else {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(31.7962994, 35.1053184))        //точка иерусалима
                    .zoom(6)                                            //зум
                    .bearing(0)                                    //поворот карт
                    .tilt(20)                                       //угол наклона
                    .build();
            //И передаем полученный объект в метод newCameraPosition, получая CameraUpdate, который в свою очередь передаем в метод animateCamera
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            mMap.animateCamera(cameraUpdate);
        }
    }

    public void getOneUserFromFirebase(final String uidUser) {
        Log.d(LOG_TAG, "getOneUserFromFirebase " + uidUser);
        FirebaseDatabase.getInstance().getReference().child(Constants.ARG_USERS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren().iterator();
                users = new ArrayList<>();
                while (dataSnapshots.hasNext()) {
                    DataSnapshot dataSnapshotChild = dataSnapshots.next();
                    User user = dataSnapshotChild.getValue(User.class);
                    Log.d(LOG_TAG, "getOneUserFromFirebase  uidUser       " + uidUser);
                    Log.d(LOG_TAG, "getOneUserFromFirebase  user.getUid() " + user.getUid());
                    if (uidUser.equals(user.getUid())) {

                        myName = user.getNameUser();
                        myPhoto = user.getUriPhoto();
                        myStatus = user.getStatus();
                        myMoto = user.getMoto();
                        myPhone = user.getPhone();


                        Log.d(LOG_TAG, "getOneUserFromFirebase 1 " + myPhoto);


                        Picasso.with(MapsActivity.this)
                                .load(myPhoto)
                                .error(R.mipmap.ic_launcher)
                                .into(imageUser);

                        Log.d(LOG_TAG, "getOneUserFromFirebase 2 " + myPhoto);
                        textUserName.setText(myName);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, "getOneUserFromFirebase  onCancelled " + databaseError.toString());
            }
        });
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
/*
                    Log.d(LOG_TAG, "getAllUsersFromFirebase " + user.getNameUser());
                    Log.d(LOG_TAG, "getAllUsersFromFirebase *" + user.getMoto()+"*");
                    Log.d(LOG_TAG, "getAllUsersFromFirebase " + user.getUriPhoto());
                    Log.d(LOG_TAG, "getAllUsersFromFirebase " + user.getStatus());*/
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
                btnPrivacyPolicy.setTextColor(getResources().getColor(R.color.colorWhiteDark));
                startActivity(new Intent(MapsActivity.this, PrivacyPolicyActivity.class).putExtra("flagExit", "maps"));
            }
        });
    }

    private void authorize() {
        Intent authorizeIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(authorizeIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        showProgressDialog(getText(R.string.dialogProgressAuth).toString());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            local = getLocation();
                            addUserToDatabase(MapsActivity.this, task.getResult().getUser(), local, "0");
                            getOneUserFromFirebase(task.getResult().getUser().getUid());
                            dialog.dismiss();
                            getAllUsersFromFirebase();
                            hideProgressDialog(getText(R.string.toastAuthFinish).toString());
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
                status,
                "",
                "");
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

    public void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(message);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog(String message) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            Toast.makeText(MapsActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }

    //диалог статуса
    private void dialogStatus() {

        arrStatus = new String[]{
                getText(R.string.statusHome).toString(),
                getText(R.string.statusWant).toString(),
                getText(R.string.statusWay).toString(),
                getText(R.string.statusSOS).toString(),
                getText(R.string.statusFriends).toString(),
        };

        Integer[] imgid = {
                R.drawable.ic_status_home,
                R.drawable.ic_status_want,
                R.drawable.ic_status_way,
                R.drawable.ic_status_sos,
                R.drawable.ic_status_friends,
        };

        LayoutInflater adbInflater = LayoutInflater.from(MapsActivity.this);
        View v = adbInflater.inflate(R.layout.dialog_list_status, null);

        StatusListAdapter adapter = new StatusListAdapter(MapsActivity.this, arrStatus, imgid);
        listStatus = (ListView) v.findViewById(R.id.listStatus);
        listStatus.setAdapter(adapter);

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setCancelable(false);
        adb.setTitle(R.string.titleStatus);
        adb.setIcon(R.drawable.artboard_green);
        adb.setView(v);

        dialog = adb.show();

        listStatus.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                String Slecteditem = arrStatus[+position];
                Toast.makeText(getApplicationContext(), Slecteditem, Toast.LENGTH_SHORT).show();

                switch (position) {
                    case 0:
                        myStatus = "0";
                        textUserStatus.setText(R.string.statusHome);
                        locationGPS.stopUsingGPS();
                        break;
                    case 1:
                        myStatus = "1";
                        textUserStatus.setText(R.string.statusWant);
                        locationGPS.stopUsingGPS();
                        break;
                    case 2:
                        myStatus = "2";
                        textUserStatus.setText(R.string.statusWay);
                        locationGPS = new LocationGPS(MapsActivity.this, true);
                        break;
                    case 3:
                        myStatus = "3";
                        textUserStatus.setText(R.string.statusSOS);
                        locationGPS.stopUsingGPS();
                        break;
                    case 4:
                        myStatus = "4";
                        textUserStatus.setText(R.string.statusFriends);
                        locationGPS.stopUsingGPS();
                        break;

                }
                updateUserToDatabase(MapsActivity.this, FirebaseAuth.getInstance().getCurrentUser(), myName, myPhoto, myStatus, myPhone, myMoto);
                getAllUsersFromFirebase();
                dialog.dismiss();
            }
        });
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

    private void dialogGroupFacebook() {
        String[] arrGroups = new String[]{
                "Group for Israel",
                "General Group",
        };
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setCancelable(false);

        adb.setItems(arrGroups, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        Uri address = Uri.parse("https://m.facebook.com/IsraelTypicalMoto");
                        Intent openlink = new Intent(Intent.ACTION_VIEW, address);
                        startActivity(openlink);
                        dialog.dismiss();
                        break;
                    case 1:
                        address = Uri.parse("https://m.facebook.com/UniteAllMotorcyclists");
                        openlink = new Intent(Intent.ACTION_VIEW, address);
                        startActivity(openlink);
                        dialog.dismiss();
                        break;
                }
            }
        });

        dialog = adb.show();
    }

    //диалог информации пользователя
    public void dialogInfoUser(String name, int status, String uriPhoto, String moto, String phone, final int posotion) {
        LayoutInflater adbInflater = LayoutInflater.from(MapsActivity.this);
        View v = adbInflater.inflate(R.layout.dialog_info_user, null);

        dialogImageUser = (ImageView) v.findViewById(R.id.imageUser);
        dialogNameUser = (TextView) v.findViewById(R.id.textNameUser);
        //     dialogMailUser = (TextView) v.findViewById(R.id.textMailUser);
        dialogStatusUser = (TextView) v.findViewById(R.id.textStatusUser);
        dialogMotoUser = (TextView) v.findViewById(R.id.textMotoUser);
        dialogPhoneUser = (TextView) v.findViewById(R.id.textPhoneUser);

        Picasso.with(this)
                .load(uriPhoto)
                .error(R.mipmap.ic_launcher)
                .into(dialogImageUser);

        dialogNameUser.setText(name);
        //      dialogMailUser.setText(dialogMailUser.getText().toString() + " " + mail);
        dialogMotoUser.setText(dialogMotoUser.getText().toString() + " " + moto);
        dialogPhoneUser.setText(dialogPhoneUser.getText().toString() + " " + phone);

        switch (status) {
            case 0:
                dialogStatusUser.setText(dialogStatusUser.getText().toString() + " " + getText(R.string.statusHome));
                break;
            case 1:
                dialogStatusUser.setText(dialogStatusUser.getText().toString() + " " + getText(R.string.statusWant));
                break;
            case 2:
                dialogStatusUser.setText(dialogStatusUser.getText().toString() + " " + getText(R.string.statusWay));
                break;
            case 3:
                dialogStatusUser.setText(dialogStatusUser.getText().toString() + " " + getText(R.string.statusSOS));
                break;
            case 4:
                dialogStatusUser.setText(dialogStatusUser.getText().toString() + " " + getText(R.string.statusFriends));
                break;

        }

        AlertDialog.Builder adb = new AlertDialog.Builder(MapsActivity.this);
        adb.setCancelable(true);
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
                String[] arrLcal = local.split("\\+");
                String lat = arrLcal[0];
                String lon = arrLcal[1];

                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lon + "&mode=d&avoid=h");
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
            moveTaskToBack(true);
            exit(0);
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
            if (!textUserName.getText().equals(""))
                dialogStatus();
            else
                Toast.makeText(MapsActivity.this, R.string.toasNotAuth, Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_instructions) {
            dialogInstructions();
        } else if (id == R.id.nav_advise_friend) {
            //advise to friends
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.vyn.motoclick&hl");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } else if (id == R.id.nav_about_program) {
            startActivity(new Intent(MapsActivity.this, AboutProgram.class));
        }
        /*else if (id == R.id.nav_from_developer) {
            //from developer
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/developer?id=Yurka+Sergeant+Matatov"));
            startActivity(intent);
        }*/
        else if (id == R.id.nav_link_fb) {
            //открыть ссылку в браузере
            if ("iw".equals(getResources().getConfiguration().locale.getLanguage())) {
                dialogGroupFacebook();
            } else {
                Uri address = Uri.parse("https://m.facebook.com/UniteAllMotorcyclists");
                Intent openlink = new Intent(Intent.ACTION_VIEW, address);
                startActivity(openlink);
            }
        } else if (id == R.id.nav_feedback) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.vyn.motoclick&hl"));
            startActivity(intent);
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
                    showDialogOK(getText(R.string.dialogPermissionLocation).toString(), new DialogInterface.OnClickListener() {
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
        return sIsChatActivityOpen;
    }

    public static void setChatActivityOpen(boolean isChatActivityOpen) {
        MapsActivity.sIsChatActivityOpen = isChatActivityOpen;
    }
}