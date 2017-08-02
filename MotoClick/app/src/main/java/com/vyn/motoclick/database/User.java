package com.vyn.motoclick.database;

/**
 * Created by Yurka on 15.06.2017.
 */

public class User {
    public String uid;
    public String email;
    public String firebaseToken;
    public String uriPhoto;
    public String location;
    public String nameUser;
    public String status;
    public String phone;
    public String moto;

    public User(){
    }

    public User(String uid, String email, String firebaseToken, String uriPhoto, String location, String nameUser, String status, String phone, String moto){
        this.uid = uid;
        this.email = email;
        this.firebaseToken = firebaseToken;
        this.uriPhoto = uriPhoto;
        this.location = location;
        this.nameUser = nameUser;
        this.status = status;
        this.phone = phone;
        this.moto = moto;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getFirebaseToken() {
        return firebaseToken;
    }

    public String getUriPhoto() {
        return uriPhoto;
    }

    public String getLocation() {
        return location;
    }

    public String getNameUser() {
        return nameUser;
    }

    public String getStatus() {
        return status;
    }

    public String getPhone() {
        return phone;
    }

    public String getMoto() {
        return moto;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirebaseToken(String firebaseToken) {
        this.firebaseToken = firebaseToken;
    }

    public void setUriPhoto(String uriPhoto) {
        this.uriPhoto = uriPhoto;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setNameUser(String nameUser) {
        this.nameUser = nameUser;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setMoto(String moto) {
        this.moto = moto;
    }
}