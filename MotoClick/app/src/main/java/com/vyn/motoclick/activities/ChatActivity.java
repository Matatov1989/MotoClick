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
import com.vyn.motoclick.database.Chat;
import com.vyn.motoclick.database.UserData;
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

    public static void startActivity(Context context, Chat chat, UserData userData) {
        Intent intent = new Intent(context, ChatActivity.class);

        intent.putExtra(Constants.ARG_SENDER, chat.getSenderName());
        intent.putExtra(Constants.ARG_SENDER_UID, chat.getSenderUid());
        intent.putExtra(Constants.ARG_SENDER_TOKEN, chat.getSenderToken());

        intent.putExtra(Constants.ARG_RECEIVER, chat.getReceiverName());
        intent.putExtra(Constants.ARG_RECEIVER_UID, chat.getReceiverUid());
        intent.putExtra(Constants.ARG_RECEIVER_TOKEN, chat.getReceiverToken());

        intent.putExtra(UserData.class.getCanonicalName(), userData);

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


    private void bindViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        // set the toolbar
        setSupportActionBar(mToolbar);
        // set toolbar title
        mToolbar.setTitle(getIntent().getExtras().getString(Constants.ARG_RECEIVER));
    }

    private void init() {
        Log.d(LOG_TAG, "ChatActivity name " + getIntent().getExtras().getString(Constants.ARG_RECEIVER));
        Log.d(LOG_TAG, "ChatActivity id " + getIntent().getExtras().getString(Constants.ARG_RECEIVER_UID));
        Log.d(LOG_TAG, "ChatActivity tokenR " + getIntent().getExtras().getString(Constants.ARG_RECEIVER_TOKEN));
        Log.d(LOG_TAG, "ChatActivity tokenS" + getIntent().getExtras().getString(Constants.ARG_SENDER_TOKEN));

        Chat chat = new Chat(getIntent().getExtras().getString(Constants.ARG_SENDER),
                getIntent().getExtras().getString(Constants.ARG_SENDER_UID),
                getIntent().getExtras().getString(Constants.ARG_SENDER_TOKEN),
                getIntent().getExtras().getString(Constants.ARG_RECEIVER),
                getIntent().getExtras().getString(Constants.ARG_RECEIVER_UID),
                getIntent().getExtras().getString(Constants.ARG_RECEIVER_TOKEN));
        // set the register screen fragment
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_content_chat,
                ChatFragment.newInstance(chat),
                ChatFragment.class.getSimpleName());
        fragmentTransaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MapsActivity.setChatActivityOpen(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MapsActivity.setChatActivityOpen(false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ChatActivity.this, MapsActivity.class);
        intent.putExtra(UserData.class.getCanonicalName(), (UserData) getIntent().getParcelableExtra(UserData.class.getCanonicalName()));
        startActivity(intent);
    }
}