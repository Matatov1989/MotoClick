package com.vyn.motoclick.activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vyn.motoclick.R;
import com.vyn.motoclick.adapters.ContactsRecyclerAdapter;
import com.vyn.motoclick.database.ContactData;
import com.vyn.motoclick.database.UserData;
import com.vyn.motoclick.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class ContactsActivity extends AppCompatActivity {

    static final String LOG_TAG = "myLogs";

    RecyclerView recyclerContact;
    ContactsRecyclerAdapter adapter;

    UserData userData;
//   private ArrayList<ContactData> contactDataArrayList = new ArrayList<>();

    public ArrayList<ContactData> arrayListContact = new ArrayList<ContactData>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_contacts);

        userData = (UserData) getIntent().getParcelableExtra(UserData.class.getCanonicalName());

        recyclerContact = (RecyclerView) findViewById(R.id.recyclerContact);
        recyclerContact.setClickable(true);

        getContacts();

        //     Log.d(LOG_TAG, "ContactActivity sort size " + Constants.ARG_USER_CONTACT_DATA.size());
        //     Log.d(LOG_TAG, "ContactActivity sort size " + Constants.ARG_USER_CONTACT_DATA.get(0).getUid());

    }


    private void getContacts() {
        Log.d(LOG_TAG, "ContactActivity " );
        arrayListContact.clear();
        FirebaseDatabase.getInstance().getReference()
                .child(Constants.ARG_USERS)
                .child(userData.getUserId())
                .child(Constants.ARG_CONTACTS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren().iterator();

                        while (dataSnapshots.hasNext()) {
                            DataSnapshot dataSnapshotChild = dataSnapshots.next();
                            ContactData contactData = dataSnapshotChild.getValue(ContactData.class);
                            Log.d(LOG_TAG, "ContactActivity " + contactData.getContactId());
                            arrayListContact.add(contactData);
                        }

                        FirebaseDatabase.getInstance().getReference()
                                .child(Constants.ARG_USERS)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren().iterator();

                                        while (dataSnapshots.hasNext()) {
                                            DataSnapshot dataSnapshotChild = dataSnapshots.next();
                                            ContactData contactData = dataSnapshotChild.getValue(ContactData.class);
                                            Log.d(LOG_TAG, "ContactActivity " + contactData.getContactId());
                                            arrayListContact.add(contactData);
                                        }






                                        Collections.sort(arrayListContact, new ContactData());
                                        for (int i = 0; i < arrayListContact.size(); i++)
                                            Log.d(LOG_TAG, "ContactActivity sort " + arrayListContact);


                                        adapter = new ContactsRecyclerAdapter(ContactsActivity.this, arrayListContact);

                                        recyclerContact.setAdapter(adapter);
                                        recyclerContact.setLayoutManager(new LinearLayoutManager(ContactsActivity.this));
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }

                                });



                        Collections.sort(arrayListContact, new ContactData());
                        for (int i = 0; i < arrayListContact.size(); i++)
                            Log.d(LOG_TAG, "ContactActivity sort " + arrayListContact);


                        adapter = new ContactsRecyclerAdapter(ContactsActivity.this, arrayListContact);

                        recyclerContact.setAdapter(adapter);
                        recyclerContact.setLayoutManager(new LinearLayoutManager(ContactsActivity.this));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }
}