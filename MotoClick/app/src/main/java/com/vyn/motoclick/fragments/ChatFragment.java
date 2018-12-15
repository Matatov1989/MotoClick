package com.vyn.motoclick.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vyn.motoclick.R;
import com.vyn.motoclick.adapters.ChatRecyclerAdapter;
import com.vyn.motoclick.chat.ChatContract;
import com.vyn.motoclick.chat.ChatPresenter;
import com.vyn.motoclick.database.Chat;
import com.vyn.motoclick.database.History;
import com.vyn.motoclick.events.PushNotificationEvent;
import com.vyn.motoclick.utils.Constants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.vyn.motoclick.utils.Constants.ARG_RECEIVER;
import static com.vyn.motoclick.utils.Constants.ARG_RECEIVER_UID;

/**
 * Created by Yurka on 15.06.2017.
 */

public class ChatFragment extends Fragment implements ChatContract.View, TextView.OnEditorActionListener {
    final String LOG_TAG = "myLogs";
    private RecyclerView mRecyclerViewChat;
    private EditText mETxtMessage;

    private ProgressDialog mProgressDialog;

    private ChatRecyclerAdapter mChatRecyclerAdapter;

    private ChatPresenter mChatPresenter;
    private boolean flagSend = true;
    String msg;

    ImageButton btnSendMsg;

    public static ChatFragment newInstance(String receiver, String receiverUid, String receiverToken, String senderToken) {
        Bundle args = new Bundle();
        args.putString(ARG_RECEIVER, receiver);
        args.putString(ARG_RECEIVER_UID, receiverUid);
        args.putString(Constants.ARG_RECEIVER_TOKEN, receiverToken);
        args.putString(Constants.ARG_SENDER_TOKEN, senderToken);



        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_chat, container, false);
        bindViews(fragmentView);

        btnSendMsg = (ImageButton) fragmentView.findViewById(R.id.btnSendMsg);

        btnSendMsg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!mETxtMessage.getText().toString().isEmpty()) {
                    //  flagSend = false;
                    msg = mETxtMessage.getText().toString();
                    mETxtMessage.setText("");
                    sendMessage(msg);
                }
            }
        });

        return fragmentView;
    }

    private void bindViews(View view) {
        //resetCntMsgFirebase(getArguments().getString(Constants.ARG_RECEIVER_UID));
        mRecyclerViewChat = (RecyclerView) view.findViewById(R.id.recycler_view_chat);
        mETxtMessage = (EditText) view.findViewById(R.id.edit_text_message);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
     //   resetCntMsgFirebase(Constants.ARG_RECEIVER_UID);
        init();

    }

    private void init() {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle(getString(R.string.loading));
        mProgressDialog.setMessage(getString(R.string.please_wait));
        mProgressDialog.setIndeterminate(true);

        mETxtMessage.setOnEditorActionListener(this);

        mChatPresenter = new ChatPresenter(this);
        mChatPresenter.getMessage(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                getArguments().getString(ARG_RECEIVER_UID));
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            sendMessage("");
            flagSend = true;
            return true;
        }
        return false;
    }

    //удалить прочитанное сообшение сброс счетчика смс+++
    private void resetCntMsgFirebase(final String uidSender) {
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

    private void sendMessage(String message) {
        String sender = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String receiver = getArguments().getString(ARG_RECEIVER);
        String receiverUid = getArguments().getString(ARG_RECEIVER_UID);
        //    String message = mETxtMessage.getText().toString();

        String receiverFirebaseToken = getArguments().getString(Constants.ARG_RECEIVER_TOKEN);
        //     String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        Chat chat = new Chat(sender, senderUid, receiver, receiverUid, message, System.currentTimeMillis());
        mChatPresenter.sendMessage(getActivity().getApplicationContext(), chat, receiverFirebaseToken);

    //    sendToUser();
        sendToUser();
        sendToMe(senderUid);


    }

    private void sendToMe(final String senderUid){
        FirebaseDatabase.getInstance().getReference().child("users")
                                                     .child(senderUid)      //отпрпавитель
                                                     .child("history")
                                                     .child(getArguments().getString(ARG_RECEIVER_UID)) //получатель
                                                     .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.d(LOG_TAG, "sendToMe " + dataSnapshot);
                if (dataSnapshot.getValue() != null) {

                //    Log.d(LOG_TAG, "sendToMe have" + dataSnapshot);
              //      Log.d(LOG_TAG, "TEST send val " + dataSnapshot.getValue());
              //      Log.d(LOG_TAG, "TEST send if cnt " + dataSnapshot.getValue(History.class).getCntMsg());
                    Log.d(LOG_TAG, "sendToMe token " + dataSnapshot.getValue(History.class).getTokenUser());

                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                    String historyKey = mDatabase
                            .child("users")
                            .child(senderUid)      //отпрпавитель
                            .child("history")
                            .child(getArguments().getString(ARG_RECEIVER_UID)) //получатель
                            .getKey();

                 //   int cntMsg = dataSnapshot.getValue(History.class).getCntMsg() + 1;

                    History history = new History(dataSnapshot.getValue(History.class).getUidUser(), dataSnapshot.getValue(History.class).getNameUser(), dataSnapshot.getValue(History.class).getTokenUser(), 0);
                    Map<String, Object> postValues = history.toMap();

                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("users/" + senderUid + "/history/" + historyKey, postValues);

                    mDatabase.updateChildren(childUpdates);
                }
                else{
                    Log.d(LOG_TAG, "sendToMe new " + dataSnapshot);

                    Log.d(LOG_TAG, "sendToMe new token " + getArguments().getString(Constants.ARG_RECEIVER_TOKEN));
                    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                    History history = new History(getArguments().getString(ARG_RECEIVER_UID), getArguments().getString(ARG_RECEIVER), getArguments().getString(Constants.ARG_RECEIVER_TOKEN), 0);
                    database.child("users")
                            .child(senderUid)
                            .child("history")
                            .child(getArguments().getString(ARG_RECEIVER_UID))
                            .setValue(history)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //        Toast.makeText(MapsActivity.this, toastFriend, Toast.LENGTH_SHORT).show();
                                    } else {
                                    }
                                }
                            });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, "2 TEST onCancelled " + databaseError);
            }
        });
    }

    //send count to data for user
    private void sendToUser() {

        Log.d(LOG_TAG, "sendToUser");

        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(getArguments().getString(ARG_RECEIVER_UID))      //полечатель
                .child("history")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())        //отправитель
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {

                    Log.d(LOG_TAG, "sendToUser update");
               //     Log.d(LOG_TAG, "sendToUser val " + dataSnapshot.getValue());
                    Log.d(LOG_TAG, "sendToUser cnt " + dataSnapshot.getValue(History.class).getCntMsg());
                    Log.d(LOG_TAG, "sendToUser name " + dataSnapshot.getValue(History.class).getNameUser());
                    Log.d(LOG_TAG, "sendToUser token " + dataSnapshot.getValue(History.class).getTokenUser());

                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                    String historyKey = mDatabase
                            .child("users")
                            .child(getArguments().getString(ARG_RECEIVER_UID))      //получатель
                            .child("history")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())    //отправитель
                            .getKey();

                    int cntMsg = dataSnapshot.getValue(History.class).getCntMsg() + 1;

                    History history = new History(dataSnapshot.getValue(History.class).getUidUser(), dataSnapshot.getValue(History.class).getNameUser(), dataSnapshot.getValue(History.class).getTokenUser(), cntMsg);
                    Map<String, Object> postValues = history.toMap();

                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("users/" + getArguments().getString(ARG_RECEIVER_UID) + "/history/" + historyKey, postValues);

                    mDatabase.updateChildren(childUpdates);
                }
                else{
                    Log.d(LOG_TAG, "sendToUser new");
               //     Log.d(LOG_TAG, "sendToUser new token "+getArguments().getString(Constants.ARG_RECEIVER_TOKEN));
                    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                    History history = new History(FirebaseAuth.getInstance().getCurrentUser().getUid(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), getArguments().getString(Constants.ARG_SENDER_TOKEN), 1);
                    database.child("users")
                            .child(getArguments().getString(ARG_RECEIVER_UID))
                            .child("history")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(history)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //        Toast.makeText(MapsActivity.this, toastFriend, Toast.LENGTH_SHORT).show();
                                    } else {
                                    }
                                }
                            });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, "2 TEST onCancelled " + databaseError);
            }
        });

    }

    @Override
    public void onSendMessageSuccess() {
        mETxtMessage.setText("");
    }

    @Override
    public void onSendMessageFailure(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGetMessagesSuccess(Chat chat) {
        if (mChatRecyclerAdapter == null) {
            mChatRecyclerAdapter = new ChatRecyclerAdapter(new ArrayList<Chat>());
            mRecyclerViewChat.setAdapter(mChatRecyclerAdapter);
        }
        mChatRecyclerAdapter.add(chat);
        mRecyclerViewChat.smoothScrollToPosition(mChatRecyclerAdapter.getItemCount() - 1);
    }

    @Override
    public void onGetMessagesFailure(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onPushNotificationEvent(PushNotificationEvent pushNotificationEvent) {
        if (mChatRecyclerAdapter == null || mChatRecyclerAdapter.getItemCount() == 0) {
            mChatPresenter.getMessage(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                    pushNotificationEvent.getUid());
        }
    }
}