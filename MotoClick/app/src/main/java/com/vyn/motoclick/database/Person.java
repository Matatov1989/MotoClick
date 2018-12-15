package com.vyn.motoclick.database;

/**
 * Created by Yurka on 25.11.2017.
 */

public class Person {

    public String uidFriend;
    public String nameFriend;
    public String tokenFriend;

    public Person (){}

    public Person (String uidFriend, String nameFriend, String tokenFriend){
        this.uidFriend = uidFriend;
        this.nameFriend = nameFriend;
        this.tokenFriend = tokenFriend;
    }

    public String getUidFriend1(){
        return this.uidFriend;
    }

    public String getNameFriend1(){
        return this.nameFriend;
    }

    public String getTokenFriend1(){
        return this.tokenFriend;
    }
}
