package com.vyn.motoclick.database;

import com.google.firebase.Timestamp;

/**
 * Created by Yurka on 15.06.2017.
 */

public class Chat {
    public String senderName;
    public String senderUid;
    public String senderToken;
    public String receiverName;
    public String receiverUid;
    public String receiverToken;
    public String message;
    public Timestamp timestamp;

    public Chat(String senderName, String senderUid, String senderToken, String receiverName, String receiverUid, String receiverToken) {
        this.senderName = senderName;
        this.senderUid = senderUid;
        this.senderToken = senderToken;
        this.receiverName = receiverName;
        this.receiverUid = receiverUid;
        this.receiverToken = receiverToken;
    }

    public Chat(String sender, String senderUid, String senderToken, String receiver, String receiverUid, String receiverToken, String message, Timestamp timestamp) {
        this.senderName = sender;
        this.senderUid = senderUid;
        this.senderToken = senderToken;
        this.receiverName = receiver;
        this.receiverUid = receiverUid;
        this.receiverToken = receiverToken;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public String getSenderToken() {
        return senderToken;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getReceiverUid() {
        return receiverUid;
    }

    public String getReceiverToken() {
        return receiverToken;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}