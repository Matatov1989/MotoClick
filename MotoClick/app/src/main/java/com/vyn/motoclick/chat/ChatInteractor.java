package com.vyn.motoclick.chat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vyn.motoclick.database.Chat;
import com.vyn.motoclick.database.ContactData;
import com.vyn.motoclick.fcm.FcmNotificationBuilder;
import com.vyn.motoclick.utils.Constants;
import com.vyn.motoclick.utils.SharedPrefUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yurka on 15.06.2017.
 */

public class ChatInteractor implements ChatContract.Interactor {
    final String LOG_TAG = "myLogs";

    private FirebaseFirestore db;

    private ChatContract.OnSendMessageListener mOnSendMessageListener;
    private ChatContract.OnGetMessagesListener mOnGetMessagesListener;

    public ChatInteractor(ChatContract.OnSendMessageListener onSendMessageListener) {
        this.mOnSendMessageListener = onSendMessageListener;
    }

    public ChatInteractor(ChatContract.OnGetMessagesListener onGetMessagesListener) {
        this.mOnGetMessagesListener = onGetMessagesListener;
    }

    public ChatInteractor(ChatContract.OnSendMessageListener onSendMessageListener,
                          ChatContract.OnGetMessagesListener onGetMessagesListener) {
        this.mOnSendMessageListener = onSendMessageListener;
        this.mOnGetMessagesListener = onGetMessagesListener;
    }

    @Override
    public void sendMessageToFirebaseUser(final Context context, final Chat chat, final String receiverFirebaseToken) {
        final String room_type_1 = chat.senderUid + "_" + chat.receiverUid;
        final String room_type_2 = chat.receiverUid + "_" + chat.senderUid;

        Map<String, Object> mapChat = new HashMap<>();
        mapChat.put(Constants.ARG_SENDER, chat.getSenderName());
        mapChat.put(Constants.ARG_SENDER_UID, chat.getSenderUid());
        mapChat.put(Constants.ARG_RECEIVER, chat.getReceiverName());
        mapChat.put(Constants.ARG_RECEIVER_UID, chat.getReceiverUid());
        mapChat.put(Constants.ARG_MESSAGE, chat.getMessage());
        mapChat.put(Constants.ARG_TIME, chat.getTimestamp());


        db = FirebaseFirestore.getInstance();

        //chat
        db.collection(Constants.ARG_CHAT_ROOMS)
                .document(chat.getSenderUid()+chat.getReceiverUid())
                .collection(Constants.ARG_TIMES)
                .document(String.valueOf(chat.getTimestamp().getSeconds()))
                .set(mapChat);

        //is read (false/true)
        Map<String, Object> mapRead = new HashMap<>();
        mapRead.put(Constants.ARG_IS_READ, true);

        db.collection(Constants.ARG_CHAT_ROOMS)
                .document(chat.getSenderUid()+chat.getReceiverUid())
                .collection(Constants.ARG_READ)
                .document(chat.getReceiverUid())
                .set(mapRead);


        //   Map<String, Object> mapContacts = new HashMap<>();
        //  DocumentReference citiesRef = db.collection(Constants.ARG_USERS).document(chat.getReceiverUid()).getPath();
        //   String g = db.collection(Constants.ARG_USERS).document(chat.getReceiverUid()).getPath();

        //    mapRead.put(Constants.ARG_ARRAY_CONTACTS, Arrays.asList(FieldValue.arrayUnion(g) ,g));

        //  Log.d(LOG_TAG, "uuuuuuuu  "+ g);


        //add to list contacts
        final Map<String, Object> mapAddContactReceiver = new HashMap<>();
        mapAddContactReceiver.put(Constants.ARG_LIST_CONTACTS, FieldValue.arrayUnion(chat.getReceiverUid()));
        db.collection(Constants.ARG_USERS).document(chat.getSenderUid()).update(mapAddContactReceiver);

        final Map<String, Object> mapAddContactSender = new HashMap<>();
        mapAddContactSender.put(Constants.ARG_LIST_CONTACTS, FieldValue.arrayUnion(chat.getSenderUid()));
        db.collection(Constants.ARG_USERS).document(chat.getReceiverUid()).update(mapAddContactSender);


        /*update*/
        //   DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(Constants.ARG_USERS).child(userData.getUserId());
        //   Map<String, Object> userValues = new HashMap<String, Object>();

       /*
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child(Constants.ARG_CHAT_ROOMS).getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(room_type_1)) {
                    databaseReference.child(Constants.ARG_CHAT_ROOMS).child(room_type_1).child(String.valueOf(chat.timestamp)).setValue(chat);
                    sendContactToList(room_type_1, chat.senderUid, chat.receiverUid, true);
                } else if (dataSnapshot.hasChild(room_type_2)) {
                    databaseReference.child(Constants.ARG_CHAT_ROOMS).child(room_type_2).child(String.valueOf(chat.timestamp)).setValue(chat);
                    sendContactToList(room_type_2, chat.receiverUid, chat.senderUid, true);
                } else {
                    databaseReference.child(Constants.ARG_CHAT_ROOMS).child(room_type_1).child(String.valueOf(chat.timestamp)).setValue(chat);
                    getMessageFromFirebaseUser(chat.senderUid, chat.receiverUid);
                    sendContactToList(room_type_2, chat.senderUid, chat.receiverUid, false);
                }
                // send push notification to the receiver

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mOnSendMessageListener.onSendMessageFailure("Unable to send message: " + databaseError.getMessage());
            }
        });
*/


        sendPushNotificationToReceiver(
                chat.getSenderName(),
                chat.getMessage(),
                chat.getSenderUid(),
                chat.getSenderToken(),
                receiverFirebaseToken);
        mOnSendMessageListener.onSendMessageSuccess();
    }

    private void sendContactToList(String chatId, String sendeId, String receiverId, boolean flagMsg) {
/*
        //    final UserData userData = new UserData(firebaseUser.getUid(), firebaseUser.getDisplayName(), firebaseUser.getPhotoUrl().toString(), locationData, new SharedPrefUtil(getBaseContext()).getString(Constants.ARG_TOKEN));

        ContactData constant = new ContactData(chatId, sendeId, flagMsg);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child(Constants.ARG_USERS)
                .child(receiverId)
                .child(Constants.ARG_CONTACTS)
                .child(chatId)
                .setValue(constant).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(LOG_TAG, "addUserToFirebaseDatabase task.isSuccessful() " + task.isSuccessful());
                if (task.isSuccessful()) {

                } else {

                }
            }
        });
*/
    }

    private void sendPushNotificationToReceiver(String senderName, String message, String senderUid, String senderToken, String receiverToken) {
        FcmNotificationBuilder.initialize()
                .title(senderName)
                .message(message)
                .username(senderName)
                .uid(senderUid)
                .firebaseToken(senderToken)
                .receiverFirebaseToken(receiverToken)
                .send();
    }

    @Override
    public void getMessageFromFirebaseUser(String senderUid, String receiverUid) {
        final String room_type_1 = senderUid + "_" + receiverUid;
        final String room_type_2 = receiverUid + "_" + senderUid;
/*
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child(Constants.ARG_CHAT_ROOMS).getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(room_type_1)) {
                    FirebaseDatabase.getInstance()
                            .getReference()
                            .child(Constants.ARG_CHAT_ROOMS)
                            .child(room_type_1).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Chat chat = dataSnapshot.getValue(Chat.class);
                            mOnGetMessagesListener.onGetMessagesSuccess(chat);
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            mOnGetMessagesListener.onGetMessagesFailure("Unable to get message: " + databaseError.getMessage());
                        }
                    });
                } else if (dataSnapshot.hasChild(room_type_2)) {
                    FirebaseDatabase.getInstance()
                            .getReference()
                            .child(Constants.ARG_CHAT_ROOMS)
                            .child(room_type_2).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Chat chat = dataSnapshot.getValue(Chat.class);
                            mOnGetMessagesListener.onGetMessagesSuccess(chat);
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            mOnGetMessagesListener.onGetMessagesFailure("Unable to get message: " + databaseError.getMessage());
                        }
                    });
                } else {
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mOnGetMessagesListener.onGetMessagesFailure("Unable to get message: " + databaseError.getMessage());
            }
        });
        */
    }
}