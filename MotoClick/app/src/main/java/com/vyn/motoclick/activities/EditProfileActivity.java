package com.vyn.motoclick.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vyn.motoclick.R;
import com.vyn.motoclick.database.UserData;
import com.vyn.motoclick.utils.Constants;

import java.io.FileNotFoundException;
import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {

    static final String LOG_TAG = "myLogs";

    private FirebaseFirestore db;

    UserData userData;
    RequestOptions requestOptions;

    TextInputLayout tilEditName;
    EditText editTextName;
    TextView btnDeleteAccount;
    ImageView imageEditUserPhoto;
    Spinner spinnerTypeVehicle;
    Uri selectedImage;

    public ProgressDialog mProgressDialog;

    private static final int REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_edit_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.edit_tool_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        db = FirebaseFirestore.getInstance();

        tilEditName = (TextInputLayout) findViewById(R.id.tilEditName);
        editTextName = (EditText) tilEditName.findViewById(R.id.editTextName);
        btnDeleteAccount = (TextView) findViewById(R.id.btnDeleteAccount);
        imageEditUserPhoto = (ImageView) findViewById(R.id.imageEditUserPhoto);
        spinnerTypeVehicle = (Spinner) findViewById(R.id.spinnerTypeVehicle);

        userData = (UserData) getIntent().getParcelableExtra(UserData.class.getCanonicalName());

        requestOptions = new RequestOptions()
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher);

        Glide.with(this)
                .setDefaultRequestOptions(requestOptions)
                .load(userData.getUserUriPhoto())
                .into(imageEditUserPhoto);

        editTextName.setText(userData.getUserName());

        spinnerTypeVehicle.setSelection(userData.getUserTypeVehicle());

        imageEditUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, REQUEST);
            }
        });

        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnDeleteAccount.setTextColor(getResources().getColor(R.color.colorWhiteDark));
                dialogDelAccount();
            }
        });

    }

    //dialog delete user account +++
    private void dialogDelAccount() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setCancelable(true);
        adb.setMessage(R.string.dialogDelAccount);
        adb.setPositiveButton(R.string.btnOK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                deleteAccountFromFirebase();

                Glide.with(EditProfileActivity.this)
                        .setDefaultRequestOptions(requestOptions)
                        .load("")
                        .into(imageEditUserPhoto);

                editTextName.setText("");
                //       navSettingsUser.setVisibility(View.INVISIBLE);
                btnDeleteAccount.setTextColor(getResources().getColor(R.color.colorBlack));
            }
        });
        adb.setNeutralButton(R.string.btnCancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                btnDeleteAccount.setTextColor(getResources().getColor(R.color.colorBlack));
            }
        });
        adb.show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap img = null;
        if (requestCode == REQUEST && resultCode == RESULT_OK) {
            selectedImage = data.getData();
            try {
                img = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                //     imageEditUserPhoto.setImageBitmap(img);
/*
                Glide.with(this)
                        .setDefaultRequestOptions(requestOptions)
                        .load(img)
                        .into(imageEditUserPhoto);*/

                uploadPickInFirestorage();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                if (!userData.getUserUriPhoto().contains("googleusercontent"))
                    deletePhoroFromFirestorage(userData.getUserUriPhoto());
            }
        }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
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
                    userData.setUserUriPhoto(downloadUri.toString());
                    db.collection(Constants.ARG_USERS).document(userData.getUserId()).update(Constants.ARG_PHOTO, userData.getUserUriPhoto());

                    Glide.with(EditProfileActivity.this)
                            .setDefaultRequestOptions(requestOptions)
                            .load(userData.getUserUriPhoto())
                            .into(imageEditUserPhoto);

                    hideProgressDialog(getString(R.string.dialogProgressUploadFinish));

                } else {
                    hideProgressDialog(getString(R.string.dialogProgressUploadFinishError));
                }
            }
        });
    }

    //to save in firebase storage, need to delete the previous picture +++
    private void deletePhoroFromFirestorage(String uriPhoto) {
        Log.d(LOG_TAG, "*** deletForoFromFirebase *** " + uriPhoto);

        FirebaseStorage.getInstance().getReferenceFromUrl(uriPhoto).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
            }
        });
    }


    //delete user account from firebase database *** проверить в relase
    private void deleteAccountFromFirebase() {
        //remove a picture
        if (!userData.getUserUriPhoto().contains("googleusercontent"))
            deletePhoroFromFirestorage(userData.getUserUriPhoto());

        //remove a user data
        FirebaseDatabase.getInstance().getReference().child(Constants.ARG_USERS).child(userData.getUserId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, " " + task.isSuccessful());
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.toastDelAccount, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                    Toast.makeText(EditProfileActivity.this, R.string.toastDelAccountError, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //remove an accont
        FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, " " + task.isSuccessful());
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.toastDelAccount, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                    Toast.makeText(EditProfileActivity.this, R.string.toastDelAccountError, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_save:
                updateUserProfileToFirestore();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void updateUserProfileToFirestore(){

        userData.setUserName(editTextName.getText().toString());
        userData.setUserTypeVehicle(spinnerTypeVehicle.getSelectedItemPosition());

        db.collection(Constants.ARG_USERS)
                .document(userData.getUserId())
                .update(Constants.ARG_NAME, userData.getUserName(), Constants.ARG_TYPE_VEHICLE, userData.getUserTypeVehicle());

    }
}
