package com.vyn.motoclick.database;

/**
 * Created by Yurka on 15.06.2017.
 */

public class Chat {
    public String sender;
    public String senderUid;
    public String receiver;
    public String receiverUid;
    public String message;
    public String receiverToken;
    public long timestamp;

    public Chat() {
    }

    public Chat(String sender, String senderUid, String receiver, String receiverUid, String message, long timestamp) {
        this.sender = sender;
        this.senderUid = senderUid;
        this.receiver = receiver;
        this.receiverUid = receiverUid;
        this.message = message;
        this.timestamp = timestamp;
    }
}