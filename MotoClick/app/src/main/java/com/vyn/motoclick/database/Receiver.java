package com.vyn.motoclick.database;

/**
 * Created by Yurka on 28.10.2017.
 */

public class Receiver {

    public String receiver;
    public String receiverUid;
    public String receiverToken;

    public Receiver(String receiver, String receiverUid, String receiverToken){
        this.receiver = receiver;
        this.receiverUid = receiverUid;
        this.receiverToken = receiverToken;
    }

    public String getReceiver(){return this.receiver;}

    public String getReceiverUid(){return this.receiverUid;}

    public String getReceiverToken(){return this.receiverToken;}
}
