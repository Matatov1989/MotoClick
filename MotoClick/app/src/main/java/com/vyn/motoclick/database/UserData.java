package com.vyn.motoclick.database;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class UserData {
    public String userId;
    public String userName;
    public String userUriPhoto;
    public LocationData userLocation;
    public String userMoto;
    public String userFirebaseToken;

    public UserData() {
    }

    public UserData(String userId, String userName, String userUriPhoto, LocationData userLocation, String userMoto, String userFirebaseToken) {
        this.userId = userId;
        this.userName = userName;
        this.userUriPhoto = userUriPhoto;
        this.userLocation = userLocation;
        this.userMoto = userMoto;
        this.userFirebaseToken = userFirebaseToken;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserUriPhoto() {
        return userUriPhoto;
    }

    public LocationData getUserLocation() {
        return userLocation;
    }

    public String getUserMoto() {
        return userMoto;
    }

    public String getUserFirebaseToken() {
        return userFirebaseToken;
    }


    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("userName", userName);
        result.put("userUriPhoto", userUriPhoto);
        result.put("userLocation", userLocation);
        result.put("userMoto", userMoto);
        result.put("userFirebaseToken", userFirebaseToken);

        return result;
    }
}
