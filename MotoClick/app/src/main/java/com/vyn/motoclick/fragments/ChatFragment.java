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
import com.google.firebase.Timestamp;
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
        args.putString(Constants.ARG_RECEIVER, receiver);
        args.putString(Constants.ARG_RECEIVER_UID, receiverUid);
        args.putString(Constants.ARG_RECEIVER_TOKEN, receiverToken);
        args.putString(Constants.ARG_SENDER_TOKEN, senderToken);

        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ChatFragment newInstance(Chat chat) {
        Bundle args = new Bundle();
        args.putString(Constants.ARG_SENDER, chat.getSenderName());
        args.putString(Constants.ARG_SENDER_UID, chat.getSenderUid());
        args.putString(Constants.ARG_SENDER_TOKEN, chat.getReceiverToken());
        args.putString(Constants.ARG_RECEIVER, chat.getReceiverName());
        args.putString(Constants.ARG_RECEIVER_UID, chat.getReceiverUid());
        args.putString(Constants.ARG_RECEIVER_TOKEN, chat.getReceiverToken());

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

    private void sendMessage(String message) {
        String sender = getArguments().getString(Constants.ARG_SENDER);
        String senderUid = getArguments().getString(Constants.ARG_SENDER_UID);
        String senderToken = getArguments().getString(Constants.ARG_SENDER_TOKEN);
        String receiver = getArguments().getString(Constants.ARG_RECEIVER);
        String receiverUid = getArguments().getString(Constants.ARG_RECEIVER_UID);
        String receiverToken = getArguments().getString(Constants.ARG_RECEIVER_TOKEN);


        Chat chat = new Chat(sender, senderUid, senderToken, receiver, receiverUid, receiverToken, message, Timestamp.now());


        mChatPresenter.sendMessage(getActivity().getApplicationContext(), chat, receiverToken);

//    sendToUser();
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