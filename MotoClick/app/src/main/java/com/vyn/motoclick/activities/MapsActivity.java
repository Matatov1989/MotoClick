package com.vyn.motoclick.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.getbase.floatingactionbutton.FloatingActionButton;

import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
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
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.vyn.motoclick.R;
import com.vyn.motoclick.database.UserData;
import com.vyn.motoclick.services.ServiceGetLocation;
import com.vyn.motoclick.utils.Constants;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.vyn.motoclick.R.id.map;
import static java.lang.System.exit;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {
    static final String LOG_TAG = "myLogs";

    private FirebaseAnalytics mFirebaseAnalytics;
    /*
        String myName;
        String myPhoto;
        String myMoto;

        String myUid;
        String myToken;*/
    double lat, lon;

    static public ArrayList<UserData> usersArrList = new ArrayList<>();

    String btnAddOrDel;

    boolean flagSelectFoto = false;
    ImageView imageDialogUser;

    Dialog dialogSettings;

    UserData userData;

    ImageView dialogImageUser;
    TextView dialogNameUser;
    TextView dialogMotoUser;

    ImageView imageNavUser;
    TextView navSettingsUser;
    TextView textNavUserName;
    TextView textNavUserMoto;
    TextView textCntMsg;
    TextView deleteAccount;

    Uri selectedImage;

    private static final int REQUEST = 1;

    private GoogleMap mMap;
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

        Log.d(LOG_TAG, "onCreate  ");

        userData = (UserData) getIntent().getParcelableExtra(UserData.class.getCanonicalName());

    //    deleteAccountFromFirebase();

        Log.d(LOG_TAG, "onCreate userData " + userData.getUserName());
        Log.d(LOG_TAG, "onCreate userData " + userData.getUserUriPhoto());
        Log.d(LOG_TAG, "onCreate userData " + userData.getUserLocation().getLatitude());
        Log.d(LOG_TAG, "onCreate userData " + userData.getUserLocation().getLongitude());

        lat = userData.getUserLocation().getLatitude();
        lon = userData.getUserLocation().getLongitude();

        checkPermissions();

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
        fabGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //     actionA.setTitle("Action A clicked");
                Log.d(LOG_TAG, "actionA ");
            }
        });

        final FloatingActionButton fabBtnLocation = (FloatingActionButton) findViewById(R.id.fabLocation);
        fabBtnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //      actionB.setTitle("Action B clicked");
                Log.d(LOG_TAG, "actionB ");

                startService(new Intent(MapsActivity.this, ServiceGetLocation.class).putExtra("userIdUpdateLocation", userData.getUserId()));
            }
        });


        /*
        FloatingActionButton fabMyLocal = (FloatingActionButton) findViewById(R.id.fabMyLocal);
        fabMyLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
           //     getLocation();
                startActivity(new Intent(MapsActivity.this, TestActivity.class));
            }
        });
        */

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        analiz();

        View headerView = navigationView.getHeaderView(0);
        imageNavUser = (ImageView) headerView.findViewById(R.id.imageNavUser);
        textNavUserName = (TextView) headerView.findViewById(R.id.textNavUserName);
        textNavUserMoto = (TextView) headerView.findViewById(R.id.textNavUserMoto);

        Picasso.get()
                .load(userData.getUserUriPhoto())
                .error(R.mipmap.ic_launcher)
                .into(imageNavUser);

        textNavUserName.setText(userData.getUserName());
        textNavUserMoto.setText(userData.getUserMoto());

        navSettingsUser = (TextView) headerView.findViewById(R.id.navSettingsUser);
        navSettingsUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navSettingsUser.setTextColor(getResources().getColor(R.color.colorWhiteDark));
                dialogSettingsUser();
            }
        });
    }

    //dialog change user data, or photo, or delete an account +++
    private void dialogSettingsUser() {
        LayoutInflater adbInflater = LayoutInflater.from(MapsActivity.this);
        View v = adbInflater.inflate(R.layout.dialog_settings_user, null);

        imageDialogUser = (ImageView) v.findViewById(R.id.imageDialogUser);
        deleteAccount = (TextView) v.findViewById(R.id.btnDeleteAccount);

        TextInputLayout tilName = (TextInputLayout) v.findViewById(R.id.textInputLayoutName);
        final EditText editDialogTextName = (EditText) tilName.findViewById(R.id.editDialogTextName);

        TextInputLayout tilMoto = (TextInputLayout) v.findViewById(R.id.textInputLayoutMoto);
        final EditText editDialogTextMoto = (EditText) tilMoto.findViewById(R.id.editDialogTextMoto);

        Picasso.get()
                .load(userData.getUserUriPhoto())
                .error(R.mipmap.ic_launcher)
                .into(imageDialogUser);

        editDialogTextName.setText(userData.getUserName());
        editDialogTextMoto.setText(userData.getUserMoto());

        editDialogTextName.setSelection(editDialogTextName.getText().toString().length());
        editDialogTextMoto.setSelection(editDialogTextMoto.getText().toString().length());

        final AlertDialog.Builder adb = new AlertDialog.Builder(MapsActivity.this);
        adb.setCancelable(true);
        adb.setView(v);
        adb.setPositiveButton(R.string.btnSave, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(Constants.ARG_USERS).child(userData.getUserId());
                Map<String, Object> userValues = new HashMap<String, Object>();

                if (editDialogTextName.length() != 0) {
                    userValues.put(Constants.ARG_NAME, editDialogTextName.getText().toString());
                    userData.setUserName(editDialogTextName.getText().toString());
                    textNavUserName.setText(userData.getUserName());

                    if (editDialogTextMoto.length() != 0) {
                        Log.d(LOG_TAG, "update moto  ");
                        userValues.put(Constants.ARG_MOTO, editDialogTextMoto.getText().toString());
                        userData.setUserMoto(editDialogTextMoto.getText().toString());
                        textNavUserMoto.setText(userData.getUserMoto());
                    }

                    mDatabase.updateChildren(userValues);

                    Toast toast = Toast.makeText(getApplicationContext(), "данные обновлены", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.toastEnterName, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    dialogSettingsUser();
                }
                navSettingsUser.setTextColor(getResources().getColor(R.color.colorWhite));
            }
        });
        adb.setNegativeButton(R.string.btnCancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                navSettingsUser.setTextColor(getResources().getColor(R.color.colorWhite));
                dialog.dismiss();
            }
        });

        dialogSettings = adb.show();

        //chang foto вход в галерею с выбором картинки
        imageDialogUser.setOnClickListener(new View.OnClickListener() {
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

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.d(LOG_TAG, "onMapReady");
        mMap = googleMap;
        mMap.clear();
        //моя точка
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            if (lat != 0 && lon != 0)
                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).snippet("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_motorcycle)));

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

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                @Override
                public boolean onMarkerClick(Marker arg0) {
                    Log.d(LOG_TAG, "snipped " + arg0.getSnippet());
                    Log.d(LOG_TAG, "you " + FirebaseAuth.getInstance().getCurrentUser().getUid());
                    if (!arg0.getSnippet().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                        //      int position = Integer.parseInt(arg0.getSnippet());
                        for (int i = 0; i < usersArrList.size(); i++) {
                            if (arg0.getSnippet().toString().equals(usersArrList.get(i).getUserId())) {

                            }
                            //              getUserFromFirebase(arg0.getSnippet());
                            ///           dialogInfoUser(usersArrList.get(i).getNameUser(), Integer.parseInt(usersArrList.get(i).getStatus()), usersArrList.get(i).getUriPhoto(), usersArrList.get(i).getMoto(), usersArrList.get(i).getPhone(), i, usersArrList.get(i).getUid());
                        }
                    }
                    return true;
                }
            });
        } else //error with firebase
        {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(31.7962994, 35.1053184))       //точка иерусалима
                    .zoom(6)                                                //зум
                    .bearing(0)                                             //поворот карт
                    .tilt(20)                                               //угол наклона
                    .build();
        }
    }

    private void analiz() {
   /*     Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap img = null;
        if (requestCode == REQUEST && resultCode == RESULT_OK) {
            selectedImage = data.getData();
            try {
                img = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                imageDialogUser.setImageBitmap(img);
                uploadPickInFirestorage();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //to save in firebase storage, need to delete the previous picture +++
    private void deletePhoroFromFirestorage(String uriPhoto) {
        Log.d(LOG_TAG, "*** deletForoFromFirebase ***");

        FirebaseStorage.getInstance().getReferenceFromUrl(uriPhoto).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
                Log.d(LOG_TAG, "foto delete ");

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                Log.d(LOG_TAG, "foto delete error");
            }
        });
    }

    //upload a picture to firebase storage and save new link to user data +++
    private void uploadPickInFirestorage() {
        showProgressDialog(getString(R.string.dialogProgressUpload));
        //create link to picture
        Log.d(LOG_TAG, "*** uploadPickInFirebaseTest *** ");
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(selectedImage.getLastPathSegment());

        UploadTask uploadTask = storageReference.putFile(selectedImage);
        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d(LOG_TAG, "foto upload error ");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                Log.d(LOG_TAG, "foto good " + taskSnapshot.getMetadata().getReference().getDownloadUrl());
                Toast toast = Toast.makeText(getApplicationContext(), "фото загружено", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                //           Log.d(LOG_TAG, "foto good  bucket " + taskSnapshot.getMetadata().);


            }
        }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                Log.d(LOG_TAG, "foto upload task ");
                if (!task.isSuccessful()) {
                    Log.d(LOG_TAG, "foto task ! if ");
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {

                    Uri downloadUri = task.getResult();
                    Log.d(LOG_TAG, "foto upload onComplete " + downloadUri);

                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(Constants.ARG_USERS).child(userData.getUserId());
                    Map<String, Object> userValues = new HashMap<String, Object>();
                    userValues.put(Constants.ARG_PHOTO, downloadUri.toString());
                    mDatabase.updateChildren(userValues);

                    if (!userData.getUserUriPhoto().contains("googleusercontent"))
                        deletePhoroFromFirestorage(userData.getUserUriPhoto());

                    userData.setUserUriPhoto(downloadUri.toString());

                    Picasso.get()
                            .load(userData.getUserUriPhoto())
                            .error(R.mipmap.ic_launcher)
                            .into(imageNavUser);

                    Picasso.get()
                            .load(userData.getUserUriPhoto())
                            .error(R.mipmap.ic_launcher)
                            .into(imageDialogUser);

                    hideProgressDialog(getString(R.string.dialogProgressUploadFinish));

                } else {
                    hideProgressDialog(getString(R.string.dialogProgressUploadFinishError));
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume  ");
    }


    private void getLocation() {
        Log.d(LOG_TAG, "getLocation");
    /*    locationGPS = new LocationGPS(MapsActivity.this);
        // check if GPS enabled
        Log.d(LOG_TAG, "getLocation check = " + locationGPS.canGetLocation());
        if (locationGPS.canGetLocation()) {
            Log.d(LOG_TAG, "getLocation check = " + locationGPS.canGetLocation());
            location = new Location(locationGPS.getLatitude(), locationGPS.getLongitude());

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("location");
                Map<String, Object> userValues = new HashMap<String, Object>();
                userValues.put("latitude", location.getLatitude());
                userValues.put("longitude", location.getLongitude());
                mDatabase.updateChildren(userValues);
            }
            //    return location;
        } else {
            Log.d(LOG_TAG, "win getLocation lat = " + locationGPS.getLatitude());
            location = new Location(locationGPS.getLatitude(), locationGPS.getLongitude());
            locationGPS.showSettingsAlert();
            //     return location;
        }
        locationGPS.stopUsingGPS();
        Log.d(LOG_TAG, "getLocation ret = " + location);*/
    }


    //dialog delete user account +++
    private void dialogDelAccount() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setCancelable(true);
        adb.setMessage(R.string.dialogDelAccount);
        adb.setPositiveButton(R.string.btnOK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                dialogSettings.dismiss();
                deleteAccountFromFirebase();
                Picasso.get()
                        .load("1")
                        .error(R.mipmap.ic_launcher)
                        .into(imageNavUser);

                textNavUserName.setText("");
                textNavUserMoto.setText("");
                navSettingsUser.setVisibility(View.INVISIBLE);
                deleteAccount.setTextColor(getResources().getColor(R.color.colorBlack));
            }
        });
        adb.setNeutralButton(R.string.btnCancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                deleteAccount.setTextColor(getResources().getColor(R.color.colorBlack));
            }
        });
        adb.show();
    }

    //delete user account from firebase database *** проверить в relase
    private void deleteAccountFromFirebase() {
        Log.d(LOG_TAG, "** deleteAccountFromFirebase ** ");
     /*   FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, " " + task.isSuccessful());
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.toastDelAccount, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                    Toast.makeText(MapsActivity.this, R.string.toastDelAccountError, Toast.LENGTH_SHORT).show();
                }
            }
        });
*/
/*
        FirebaseDatabase.getInstance().getReference().child(Constants.ARG_USERS).child(userData.getUserId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, " " + task.isSuccessful());
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.toastDelAccount, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                    Toast.makeText(MapsActivity.this, R.string.toastDelAccountError, Toast.LENGTH_SHORT).show();
                }
            }
        });
*/

        Log.d(LOG_TAG, "foto delete "+FirebaseStorage.getInstance().getReference().child(userData.getUserId()));

        FirebaseStorage.getInstance().getReference().child(userData.getUserId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
                Log.d(LOG_TAG, "foto delete ");

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                Log.d(LOG_TAG, "foto delete error "+exception);
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
/*
    //диалог информация пользователей (маршрут, связаться, добавить в друзья) +++
    private void dialogInfoUser(final UserData user) {
        LayoutInflater adbInflater = LayoutInflater.from(MapsActivity.this);
        View v = adbInflater.inflate(R.layout.dialog_info_user, null);

        dialogImageUser = (ImageView) v.findViewById(R.id.imageUser);
        dialogNameUser = (TextView) v.findViewById(R.id.textNameUser);
        dialogMotoUser = (TextView) v.findViewById(R.id.textMotoUser);

        Picasso.get()
                .load(user.getUserUriPhoto())
                .error(R.mipmap.ic_launcher)
                .into(dialogImageUser);

        dialogNameUser.setText(user.getUserName());
        dialogMotoUser.setText(dialogMotoUser.getText().toString() + " " + user.getUserMoto());

        AlertDialog.Builder adb = new AlertDialog.Builder(MapsActivity.this);
        adb.setCancelable(true);
        adb.setView(v);
        adb.setIcon(android.R.drawable.ic_input_add);
        adb.setPositiveButton(R.string.btnContact, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ChatActivity.startActivity(MapsActivity.this,
                        user.getUserName(),
                        user.getUserId(),
                        user.getUserFirebaseToken(),
                        myToken);
                dialog.dismiss();
            }
        });
        adb.setNeutralButton(btnAddOrDel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //     addFriendToFirebase(usersArrList.get(posotion).getUid(), usersArrList.get(posotion).getNameUser(), usersArrList.get(posotion).getFirebaseToken());
               if (btnAddOrDel.equals(getString(R.string.btnAddFriend)))
                    addFriendToFirebase(user.getUid(), user.getNameUser(), user.getFirebaseToken());
                else if (btnAddOrDel.equals(getString(R.string.btnDelFriend)))
                    dialogDeleteFriend(user.getUid(), user.getNameUser());

                dialog.dismiss();
            }
        });
        adb.setNegativeButton(R.string.btnWay, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String lat = String.valueOf(user.getUserLocation().getLatitude());
                String lon = String.valueOf(user.getUserLocation().getLongitude());

                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lon + "&mode=d&avoid=h");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);

                dialog.dismiss();
            }
        });
        adb.show();
    }
    */

    //start progress dialog  +++
    private void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(message);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    //stop progress dialog +++
    private void hideProgressDialog(String message) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            Toast.makeText(MapsActivity.this, message, Toast.LENGTH_SHORT).show();
        }
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.nav_messages:
                //         getCountReseivMsgFromFirebase(true);

                //   dialogListMesage();
                //         getMessagesFromFirebase();
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
                startActivity(new Intent(MapsActivity.this, AboutProgram.class));
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
        Log.d(LOG_TAG, "isChatActivityOpen  " + sIsChatActivityOpen);
        return sIsChatActivityOpen;
    }

    public static void setChatActivityOpen(boolean isChatActivityOpen) {
        MapsActivity.sIsChatActivityOpen = isChatActivityOpen;
    }
}