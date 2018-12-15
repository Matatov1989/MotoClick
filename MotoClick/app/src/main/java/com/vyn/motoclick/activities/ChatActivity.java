package com.vyn.motoclick.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vyn.motoclick.R;
import com.vyn.motoclick.database.History;
import com.vyn.motoclick.fragments.ChatFragment;
import com.vyn.motoclick.utils.Constants;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Yurka on 15.06.2017.
 */

public class ChatActivity extends AppCompatActivity {
    final String LOG_TAG = "myLogs";
    private Toolbar mToolbar;

    public static void startActivity(Context context, String receiver, String receiverUid, String receiverToken, String senderToken) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(Constants.ARG_RECEIVER, receiver);
        intent.putExtra(Constants.ARG_RECEIVER_UID, receiverUid);
        intent.putExtra(Constants.ARG_RECEIVER_TOKEN, receiverToken);
        intent.putExtra(Constants.ARG_SENDER_TOKEN, senderToken);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_chat);

        // исправить
        NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();

     //   resetCntMsgFirebase(getIntent().getExtras().getString(Constants.ARG_RECEIVER_UID));

        bindViews();
        init();

//        resetCntMsgFirebase(Constants.ARG_RECEIVER_UID);
    }

    //удалить прочитанное сообшение сброс счетчика смс+++
    private void resetCntMsgFirebase(final String uidSender) {
        Log.d(LOG_TAG, "resetCntMsgFirebase " + uidSender);
        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("history")
                .child(uidSender).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.d(LOG_TAG, "resetCntMsgFirebase " + dataSnapshot);
                if (dataSnapshot.getValue() != null) {

            //        Log.d(LOG_TAG, "resetCntMsgFirebase send val " + dataSnapshot.getValue());
            //        Log.d(LOG_TAG, "resetCntMsgFirebase send if cnt " + dataSnapshot.getValue(History.class).getCntMsg());
            //        Log.d(LOG_TAG, "resetCntMsgFirebase send if name " + dataSnapshot.getValue(History.class).getNameUser());

                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                    String historyKey = mDatabase.child("users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("history")
                            .child(uidSender).getKey();

                    History history = new History(dataSnapshot.getValue(History.class).getUidUser(), dataSnapshot.getValue(History.class).getNameUser(), dataSnapshot.getValue(History.class).getTokenUser(), 0);
                    Map<String, Object> postValues = history.toMap();

                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/history/" + historyKey, postValues);

                    mDatabase.updateChildren(childUpdates);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
        //        Log.d(LOG_TAG, "2 TEST onCancelled " + databaseError);
            }
        });
    }

    private void bindViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

    }

    private void init() {
        // set the toolbar
        setSupportActionBar(mToolbar);

        // set toolbar title
        //      mToolbar.setTitle(getIntent().getExtras().getString(Constants.ARG_RECEIVER));
        mToolbar.setTitle(getIntent().getExtras().getString(Constants.ARG_RECEIVER));

        // set the register screen fragment
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_content_chat,
                ChatFragment.newInstance(getIntent().getExtras().getString(Constants.ARG_RECEIVER),
                        getIntent().getExtras().getString(Constants.ARG_RECEIVER_UID),
                        getIntent().getExtras().getString(Constants.ARG_RECEIVER_TOKEN),
                        getIntent().getExtras().getString(Constants.ARG_SENDER_TOKEN)),
                ChatFragment.class.getSimpleName());
        fragmentTransaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetCntMsgFirebase(getIntent().getExtras().getString(Constants.ARG_RECEIVER_UID));
        //      MainActivity1.setChatActivityOpen(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        resetCntMsgFirebase(getIntent().getExtras().getString(Constants.ARG_RECEIVER_UID));
        //     MainActivity1.setChatActivityOpen(false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ChatActivity.this, MapsActivity.class));
    }
}