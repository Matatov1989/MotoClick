package com.vyn.motoclick.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vyn.motoclick.R;
import com.vyn.motoclick.database.LocationData;
import com.vyn.motoclick.database.UserData;
import com.vyn.motoclick.utils.Constants;
import com.vyn.motoclick.utils.SharedPrefUtil;

public class AuthenticationActivity extends AppCompatActivity {

    static final String LOG_TAG = "myLogs";

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;


    private SignInButton signInButton;
    private TextView btnPrivacyPolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_authentication);

        //    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //    setSupportActionBar(toolbar);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

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
        //   Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            addUserToFirebaseDatabase(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            //     Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addUserToFirebaseDatabase(FirebaseUser firebaseUser) {

        Log.d(LOG_TAG, "addUserToFirebaseDatabase  ");
        LocationData locationUser = new LocationData(0.0, 0.0);

        final UserData userData = new UserData(firebaseUser.getUid(), firebaseUser.getDisplayName(), firebaseUser.getPhotoUrl().toString(), locationUser, new SharedPrefUtil(getBaseContext()).getString(Constants.ARG_TOKEN));

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child(Constants.ARG_USERS).child(firebaseUser.getUid()).setValue(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(LOG_TAG, "addUserToFirebaseDatabase task.isSuccessful() "+task.isSuccessful());
                if (task.isSuccessful()) {

                    Intent intent = new Intent(AuthenticationActivity.this, MapsActivity.class);
                    intent.putExtra(UserData.class.getCanonicalName(), userData);
                    startActivity(intent);
                } else {
                }
            }
        });
    }
}