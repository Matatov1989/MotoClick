package com.vyn.motoclick.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.vyn.motoclick.R;
import com.vyn.motoclick.adapters.ContactsRecyclerAdapter;
import com.vyn.motoclick.database.UserData;

import java.util.ArrayList;
import java.util.Collections;

public class ContactsActivity extends AppCompatActivity {

    static final String LOG_TAG = "myLogs";

    RecyclerView recyclerContact;
    ContactsRecyclerAdapter adapter;

    public ArrayList<UserData> arrayListContact = new ArrayList<UserData>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_contacts);

        recyclerContact = (RecyclerView) findViewById(R.id.recyclerContact);
        recyclerContact.setClickable(true);


        //     for (int i = 0; i < Constants.ARG_USER_CONTACT_DATA.size(); i++)
        //       Log.d(LOG_TAG, "ContactActivity " + Constants.ARG_USER_CONTACT_DATA.get(i).getUserName());

        //     getFriendsFromFirebase();
   //     Log.d(LOG_TAG, "ContactActivity sort size " + Constants.ARG_USER_CONTACT_DATA.size());
   //     Log.d(LOG_TAG, "ContactActivity sort size " + Constants.ARG_USER_CONTACT_DATA.get(0).getUid());

        Collections.sort(arrayListContact, new UserData());
        for (int i = 0; i < arrayListContact.size(); i++)
            Log.d(LOG_TAG, "ContactActivity sort " + arrayListContact);


        adapter = new ContactsRecyclerAdapter(this, arrayListContact);

        recyclerContact.setAdapter(adapter);
        recyclerContact.setLayoutManager(new LinearLayoutManager(this));
    }
}
