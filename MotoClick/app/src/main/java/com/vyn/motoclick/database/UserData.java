package com.vyn.motoclick.database;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class UserData implements Parcelable {
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

    public UserData(String userId, String userName, String userUriPhoto, LocationData userLocation, String userFirebaseToken) {
        this.userId = userId;
        this.userName = userName;
        this.userUriPhoto = userUriPhoto;
        this.userLocation = userLocation;
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

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserUriPhoto(String userUriPhoto) {
        this.userUriPhoto = userUriPhoto;
    }

    public void setUserMoto(String userMoto) {
        this.userMoto = userMoto;
    }
/*
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userName", userName);
        result.put("userUriPhoto", userUriPhoto);
        result.put("userLocation", userLocation);
        result.put("userMoto", userMoto);

        return result;
    }
*/
    /*
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(userName);
        dest.writeString(userUriPhoto);
        dest.writeValue(userLocation);
        dest.writeString(userFirebaseToken);
    }

    public static final Parcelable.Creator<UserData> CREATOR = new Parcelable.Creator<UserData>() {
        public UserData createFromParcel(Parcel in) {
            return new UserData(in);
        }

        public UserData[] newArray(int size) {
            return new UserData[size];
        }
    };

    private UserData(Parcel in) {
        userId = in.readString();
        userName = in.readString();
        userUriPhoto = in.readString();
        userLocation = in.readv
        userFirebaseToken = in.readString();
    }*/


    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userId);
        dest.writeString(this.userName);
        dest.writeString(this.userUriPhoto);
        dest.writeParcelable(this.userLocation, flags);
        dest.writeString(this.userMoto);
        dest.writeString(this.userFirebaseToken);
    }

    protected UserData(Parcel in) {
        this.userId = in.readString();
        this.userName = in.readString();
        this.userUriPhoto = in.readString();
        this.userLocation = in.readParcelable(LocationData.class.getClassLoader());
        this.userMoto = in.readString();
        this.userFirebaseToken = in.readString();
    }

    public static final Parcelable.Creator<UserData> CREATOR = new Parcelable.Creator<UserData>() {
        @Override
        public UserData createFromParcel(Parcel source) {
            return new UserData(source);
        }

        @Override
        public UserData[] newArray(int size) {
            return new UserData[size];
        }
    };
}