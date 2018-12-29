package com.vyn.motoclick.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.vyn.motoclick.R;
import com.vyn.motoclick.adapters.ContactsRecyclerAdapter;
import com.vyn.motoclick.database.UserData;
import com.vyn.motoclick.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;


public class ContactsActivity extends AppCompatActivity {

    static final String LOG_TAG = "myLogs";

    FirebaseFirestore db;

    RecyclerView recyclerContact;
    ContactsRecyclerAdapter adapter;

    UserData userData;

    public ArrayList<UserData> arrayListContact = new ArrayList<UserData>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_contacts);

        db = FirebaseFirestore.getInstance();

        userData = (UserData) getIntent().getParcelableExtra(UserData.class.getCanonicalName());

        recyclerContact = (RecyclerView) findViewById(R.id.recyclerContact);
        recyclerContact.setClickable(true);

        getContacts();

    }

    private void getContacts() {
        Log.d(LOG_TAG, "ContactActivity getContacts " + userData.getUserListContacts().size());

        db.collection(Constants.ARG_USERS)
                .whereArrayContains(Constants.ARG_LIST_CONTACTS, userData.getUserId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(LOG_TAG, document.getId() + " ====> " + document.getData());
                                arrayListContact.add(document.toObject(UserData.class));
                            }

                            Collections.sort(arrayListContact, new UserData());
                            for (int i = 0; i < arrayListContact.size(); i++)
                                Log.d(LOG_TAG, "ContactActivity getContacts sort " + arrayListContact);

                            adapter = new ContactsRecyclerAdapter(ContactsActivity.this, arrayListContact, userData);

                            recyclerContact.setAdapter(adapter);
                            recyclerContact.setLayoutManager(new LinearLayoutManager(ContactsActivity.this));

                        } else {
                            Log.d(LOG_TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(ContactsActivity.this, MapsActivity.class);
        intent.putExtra(UserData.class.getCanonicalName(), userData);
        startActivity(intent);
    }
}
