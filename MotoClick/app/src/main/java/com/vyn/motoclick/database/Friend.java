package com.vyn.motoclick.database;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yurka on 07.10.2017.
 */

public class Friend  {

    public String uidFriend;
    public String nameFriend;
    public String tokenFriend;

    public Friend (){}

    public Friend (String uidFriend){
        this.uidFriend = uidFriend;
    }

    public Friend (String uidFriend, String nameFriend, String tokenFriend){
        this.uidFriend = uidFriend;
        this.nameFriend = nameFriend;
        this.tokenFriend = tokenFriend;
    }

    public String getUidFriend(){
        return this.uidFriend;
    }

    public String getNameFriend(){
        return this.nameFriend;
    }

    public String getTokenFriend(){
        return this.tokenFriend;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uidFriend", uidFriend);
        result.put("nameFriend", nameFriend);
        result.put("tokenFriend", tokenFriend);

        return result;
    }

}