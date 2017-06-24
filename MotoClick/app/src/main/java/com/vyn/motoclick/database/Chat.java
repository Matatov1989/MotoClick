package com.vyn.motoclick.database;

/**
 * Created by Yurka on 15.06.2017.
 */

public class Chat {
    public String sender;
    public String name;
    public String receiver;
    public String senderUid;
    public String receiverUid;
    public String message;
    public long timestamp;

    public Chat(){
    }

    public Chat(String sender, String name,String receiver, String senderUid, String receiverUid, String message, long timestamp){
        this.sender = sender;
        this.name = name;
        this.receiver = receiver;
        this.senderUid = senderUid;
        this.receiverUid = receiverUid;
        this.message = message;
        this.timestamp = timestamp;
    }
}
