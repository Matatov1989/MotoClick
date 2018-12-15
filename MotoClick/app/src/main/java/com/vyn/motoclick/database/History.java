package com.vyn.motoclick.database;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yurka on 31.10.2017.
 */

public class History {

    public String uidUser;
    public String nameUser;
    public String tokenUser;
    public int cntMsg;

    public History(){}

    public History(String uidUser, String nameUser, String tokenUser, int cntMsg){
        this.uidUser = uidUser;
        this.nameUser = nameUser;
        this.tokenUser = tokenUser;
        this.cntMsg = cntMsg;
    }

    public String getUidUser(){
        return this.uidUser;
    }

    public String getNameUser(){
        return this.nameUser;
    }

    public String getTokenUser(){
        return this.tokenUser;
    }

    public int getCntMsg(){return this.cntMsg;}


    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uidUser", uidUser);
        result.put("nameUser", nameUser);
        result.put("tokenUser", tokenUser);
        result.put("cntMsg", cntMsg);

        return result;
    }
}
