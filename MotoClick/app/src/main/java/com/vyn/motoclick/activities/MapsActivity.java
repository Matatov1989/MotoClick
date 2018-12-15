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
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import com.vyn.motoclick.database.Friend;
import com.vyn.motoclick.database.History;
import com.vyn.motoclick.database.Receiver;
import com.vyn.motoclick.database.UserData;
import com.vyn.motoclick.utils.Constants;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.vyn.motoclick.R.id.map;
import static java.lang.System.exit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {
    static final String LOG_TAG = "myLogs";

    String myName;
    String myPhoto;
    String myMoto;

    String myUid;
    String myToken;
    double lat, lon;
    boolean webTrue;

    static public ArrayList<User> usersArrList = new ArrayList<>();
    static public ArrayList<History> historyArrList = new ArrayList<>();
    public ArrayList<Friend> friendArrList;
    public ArrayList<Receiver> receiverArrList;

    TextView deleteAccount;

    String statusUser;
    String btnAddOrDel;

    boolean flagSelectFoto = false;

    Dialog dialog, dialog1;

    Friend friend;
    History history;
    Receiver receiver;

    ImageView dialogImageUser;
    TextView dialogNameUser;
    TextView dialogMotoUser;

    ImageView imageUser;
    TextView settingsUser;
    TextView textUserName;
    TextView textCntMsg;

    ImageView imageProf;

    Uri selectedImage;

    private static final int REQUEST = 1;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    public ProgressDialog mProgressDialog;


    private static boolean sIsChatActivityOpen = false;

    private boolean firsrCreate = false;

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
        checkPermissions();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //          setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(MapsActivity.this);

        MenuItem menuItem = navigationView.getMenu().findItem(R.id.nav_messages);
        textCntMsg = (TextView) MenuItemCompat.getActionView(menuItem);

        FloatingActionButton fabMyLocal = (FloatingActionButton) findViewById(R.id.fabMyLocal);
        fabMyLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getLocation();
                getMyAccountFromFirebase();
                //        getAllUsersFromFirebase();
            }
        });

        View headerView = navigationView.getHeaderView(0);
        imageUser = (ImageView) headerView.findViewById(R.id.imageUser);
        settingsUser = (TextView) headerView.findViewById(R.id.settingsUser);
        textUserName = (TextView) headerView.findViewById(R.id.textUserName);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {

        } else {
            friend = new Friend();
            history = new History();
            getLocation();
            getMyAccountFromFirebase();
            getCountReseivMsgFromFirebase(false);
            //       getAllUsersFromFirebase();
        }

        settingsUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsUser.setTextColor(getResources().getColor(R.color.colorWhiteDark));
                dialogSettingsUser();
            }
        });
    }

    //вытаскиваю свои данные и заполняю иими объекты +++
    private void getMyAccountFromFirebase() {
        Log.d(LOG_TAG, "getMyAccountFromFirebase");

        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                   //     Log.d(LOG_TAG, "getMyAccountFromFirebase onDataChange " + dataSnapshot.getValue(UserData.class).getNameUser());
                   //     Log.d(LOG_TAG, "getMyAccountFromFirebase onDataChange " + dataSnapshot.getValue(UserData.class).getStatus());

                        myName = dataSnapshot.getValue(UserData.class).getUserName();
                        myPhoto = dataSnapshot.getValue(UserData.class).getUserUriPhoto();
                        myMoto = dataSnapshot.getValue(UserData.class).getUserMoto();

                        myUid = dataSnapshot.getValue(UserData.class).getUserId();
                        myToken = dataSnapshot.getValue(UserData.class).getUserFirebaseToken();

                        lat = dataSnapshot.getValue(UserData.class).getUserLocation().getLatitude();
                        lon = dataSnapshot.getValue(UserData.class).getUserLocation().getLongitude();

                        Picasso.with(MapsActivity.this)
                                .load(myPhoto)
                                .error(R.mipmap.ic_launcher)
                                .into(imageUser);

                        textUserName.setText(myName);

                        //запускаем карту
                        SupportMapFragment mapFragment =
                                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                        mapFragment.getMapAsync(MapsActivity.this);

                        getAllUsersFromFirebase();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(LOG_TAG, "getMyAccountFromFirebase onCancelled " + databaseError);
                    }
                });
    }


    private void updateUserToDatabase() {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(myUid);

        Map<String, Object> userValues = new HashMap<String, Object>();

        userValues.put("nameUser", myName);
        userValues.put("moto", myMoto);
        userValues.put("uriPhoto", myPhoto);
        //       userValues.put("webTrue", webTrue);

        mDatabase.updateChildren(userValues);

        getMyAccountFromFirebase();

        Toast toast = Toast.makeText(getApplicationContext(),
                "update",
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

    }

    //загрузка картинки в хранилище firebase +++
    private void uploadPickInFirebase() {
        showProgressDialog(getText(R.string.dialogProgressPick).toString());

        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(selectedImage.getLastPathSegment());
        storageReference.putFile(selectedImage);
        storageReference.putFile(selectedImage).addOnCompleteListener(MapsActivity.this,
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            myPhoto = task.getResult().getDownloadUrl().toString();
                            updateUserToDatabase();
                            /*
                            Picasso.with(MapsActivity.this)
                                    .load(task.getResult().getDownloadUrl())
                                    .error(R.mipmap.ic_launcher)
                                    .into(imageUser);*/

                            hideProgressDialog(getText(R.string.toastPickFinish).toString());
                        } else {
                            task.getException();
                        }
                    }
                });
    }

    //удаление аккаунта из firebase *** проверить в relase
    private void deleteAccountFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();

        //моя точка
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            if (!textUserName.getText().toString().isEmpty()) {
                if (lat != 0 && lon != 0) {

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
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                @Override
                public boolean onMarkerClick(Marker arg0) {
                    Log.d(LOG_TAG, "snipped " + arg0.getSnippet());
                    Log.d(LOG_TAG, "you " + FirebaseAuth.getInstance().getCurrentUser().getUid());
                    if (!arg0.getSnippet().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                        //      int position = Integer.parseInt(arg0.getSnippet());
                        for (int i = 0; i < usersArrList.size(); i++) {
                            if (arg0.getSnippet().toString().equals(usersArrList.get(i).getUid()))
                                getUserFromFirebase(arg0.getSnippet());
                            ///           dialogInfoUser(usersArrList.get(i).getNameUser(), Integer.parseInt(usersArrList.get(i).getStatus()), usersArrList.get(i).getUriPhoto(), usersArrList.get(i).getMoto(), usersArrList.get(i).getPhone(), i, usersArrList.get(i).getUid());
                        }
                    }
                    return true;
                }
            });
        } else //пустота...если никого нет
        {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(31.7962994, 35.1053184))        //точка иерусалима
                    .zoom(6)                                            //зум
                    .bearing(0)                                    //поворот карт
                    .tilt(20)                                       //угол наклона
                    .build();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume  ");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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

    private void getLocation() {
        Log.d(LOG_TAG, "getLocation");
        locationGPS = new LocationGPS(MapsActivity.this);
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
        Log.d(LOG_TAG, "getLocation ret = " + location);
    }

    //диалог для изменения личных данных и аву или удаления аккаунта +++
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
                    myName = editName.getText().toString();
                    myMoto = editMoto.getText().toString();

                    if (flagSelectFoto) {
                        flagSelectFoto = false;
                        uploadPickInFirebase();
                    } else
                        updateUserToDatabase();

                    settingsUser.setTextColor(getResources().getColor(R.color.colorWhite));
                    dialog1.dismiss();
                } else //без имени никак
                {
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

        //chang foto вход в галерею с выбором картинки
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

    //диалог удаления аккаунта +++
    private void dialogDelAccount() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setCancelable(true);
        adb.setMessage(R.string.dialogDelAccount);
        adb.setPositiveButton(R.string.btnOK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                dialog1.dismiss();

                deleteAccountFromFirebase();
                myName = "";
                myMoto = "";
                myPhoto = "";

                Picasso.with(MapsActivity.this)
                        .load("1")
                        .error(R.mipmap.ic_launcher)
                        .into(imageUser);

                textUserName.setText("");
                settingsUser.setVisibility(View.INVISIBLE);
                deleteAccount.setTextColor(getResources().getColor(R.color.colorBlack));
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
        dialog = adb.show();
    }

    //диалог информация пользователей (маршрут, связаться, добавить в друзья) +++
    private void dialogInfoUser(final UserData user) {
        LayoutInflater adbInflater = LayoutInflater.from(MapsActivity.this);
        View v = adbInflater.inflate(R.layout.dialog_info_user, null);

        dialogImageUser = (ImageView) v.findViewById(R.id.imageUser);
        dialogNameUser = (TextView) v.findViewById(R.id.textNameUser);
        dialogMotoUser = (TextView) v.findViewById(R.id.textMotoUser);

        Picasso.with(this)
                .load(user.getUriPhoto())
                .error(R.mipmap.ic_launcher)
                .into(dialogImageUser);

        dialogNameUser.setText(user.getNameUser());
        dialogMotoUser.setText(dialogMotoUser.getText().toString() + " " + user.getMoto());

        AlertDialog.Builder adb = new AlertDialog.Builder(MapsActivity.this);
        adb.setCancelable(true);
        adb.setView(v);
        adb.setIcon(android.R.drawable.ic_input_add);
        adb.setPositiveButton(R.string.btnContact, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ChatActivity.startActivity(MapsActivity.this,
                        user.getNameUser(),
                        user.getUid(),
                        user.getFirebaseToken(),
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
                String lat = String.valueOf(user.getLocation().getLatitude());
                String lon = String.valueOf(user.getLocation().getLongitude());

                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lon + "&mode=d&avoid=h");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);

                dialog.dismiss();
            }
        });
        dialog = adb.show();
    }


    //запуск диалог прогресса +++
    private void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(message);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    //остановка диалог прогресса +++
    private void hideProgressDialog(String message) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            Toast.makeText(MapsActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }

    //посоветовать друзьям +++
    private void adviseToFriends() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.vyn.motoclick&hl");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
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
        switch (item.getItemId()) {
            case R.id.nav_list_friends:
                getFriendsFromFirebase();
                break;
            case R.id.nav_messages:
                getCountReseivMsgFromFirebase(true);

                //   dialogListMesage();
                //         getMessagesFromFirebase();
                break;
            case R.id.nav_instructions:
                dialogInstructions();
                break;
            case R.id.nav_advise_friend:
                //advise to friends
                adviseToFriends();
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