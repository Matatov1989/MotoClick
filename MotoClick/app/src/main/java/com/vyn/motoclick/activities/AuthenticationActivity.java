package com.vyn.motoclick.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.vyn.motoclick.R;
import com.vyn.motoclick.database.UserData;
import com.vyn.motoclick.utils.Constants;
import com.vyn.motoclick.utils.SharedPrefUtil;

import java.util.Date;

public class AuthenticationActivity extends AppCompatActivity {

    static final String LOG_TAG = "myLogs";

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private GeoPoint geoPoint;

    private SignInButton signInButton;
    private TextView btnPrivacyPolicy;

    public ProgressDialog mProgressDialog;

    public static final int CODE_LOCATION = 1; // code you want.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_authentication);

        checkPermissionLocation();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButton = (SignInButton) findViewById(R.id.auth_button);
        btnPrivacyPolicy = (TextView) findViewById(R.id.btnPrivacyPolicy);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        btnPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPrivacyPolicy.setTextColor(getResources().getColor(R.color.colorWhiteDark));
                startActivity(new Intent(AuthenticationActivity.this, PrivacyPolicyActivity.class));
            }
        });

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                //   Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        showProgressDialog(getString(R.string.dialogProgressAuth));
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            //       FirebaseUser user = db.getCurrentUser();
                            //      addUserToFirebaseDatabase(user);
                            addToFire(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            //     Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addToFire(FirebaseUser firebaseUser) {
        Log.d(LOG_TAG, "addUserToFirebaseDatabase  ");
        final UserData userData = new UserData(
                firebaseUser.getUid(),
                firebaseUser.getDisplayName(),
                firebaseUser.getPhotoUrl().toString(),
                geoPoint,
                Timestamp.now(),
                firebaseUser.getUid(),
                new SharedPrefUtil(getBaseContext()).getString(Constants.ARG_TOKEN));

        // Add a new document with a generated ID
        db.collection(Constants.ARG_USERS).document(firebaseUser.getUid()).set(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hideProgressDialog(getString(R.string.dialogProgressAuthFinish));

                Intent intent = new Intent(AuthenticationActivity.this, MapsActivity.class);
                intent.putExtra(UserData.class.getCanonicalName(), userData);
                startActivity(intent);
            }
        });
    }

    //update user location
    private void getLastLocation() {
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
                    geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                } else {
                }
            }
        });
    }

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
            Toast.makeText(AuthenticationActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }

    //check permission on read a phone book
    private void checkPermissionLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                dialogPermissionReadBook();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, CODE_LOCATION);
            }
        } else {
            getLastLocation();
        }
    }

    //dialog if user do not set permission on read a phone book
    private void dialogPermissionReadBook() {
        android.app.AlertDialog.Builder adb = new android.app.AlertDialog.Builder(this);
        adb.setCancelable(false);
        adb.setMessage(R.string.dialogPermissionLocation);
        adb.setPositiveButton(R.string.btnOK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(AuthenticationActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, CODE_LOCATION);
                dialog.dismiss();
            }
        });
        adb.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CODE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation();
                } else {
                    checkPermissionLocation();
                }
                return;
            }
        }
    }
}