package com.vyn.motoclick.database;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Comparator;

public class UserData implements Parcelable, Comparator<UserData> {
    public String userId;
    public String userName;
    public String userUriPhoto;
    public GeoPoint userGeoPoint;
    public Timestamp userTimeStamp;
    public boolean isOnline;
    public String userTypeVehicle;

    public String userFirebaseToken;

    public UserData() {
    }

    public UserData(String userId, String userName, String userUriPhoto, GeoPoint userGeoPoint, Timestamp userTimeStamp, boolean isOnline, String userTypeVehicle, String userFirebaseToken) {
        this.userId = userId;
        this.userName = userName;
        this.userUriPhoto = userUriPhoto;
        this.userGeoPoint = userGeoPoint;
        this.userTimeStamp = userTimeStamp;
        this.isOnline = isOnline;
        this.userTypeVehicle = userTypeVehicle;
        this.userFirebaseToken = userFirebaseToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserUriPhoto() {
        return userUriPhoto;
    }

    public void setUserUriPhoto(String userUriPhoto) {
        this.userUriPhoto = userUriPhoto;
    }

    public GeoPoint getUserGeoPoint() {
        return userGeoPoint;
    }

    public void setUserGeoPoint(GeoPoint userGeoPoint) {
        this.userGeoPoint = userGeoPoint;
    }

    public Timestamp getUserTimeStamp() {
        return userTimeStamp;
    }

    public void setUserTimeStamp(Timestamp userTimeStamp) {
        this.userTimeStamp = userTimeStamp;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getUserTypeVehicle() {
        return userTypeVehicle;
    }

    public void setUserTypeVehicle(String userTypeVehicle) {
        this.userTypeVehicle = userTypeVehicle;
    }

    public String getUserFirebaseToken() {
        return userFirebaseToken;
    }

    public void setUserFirebaseToken(String userFirebaseToken) {
        this.userFirebaseToken = userFirebaseToken;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userId);
        dest.writeString(this.userName);
        dest.writeString(this.userUriPhoto);
        //  dest.writeString(this.userGeoPoint);
        dest.writeString(this.userTypeVehicle);
        dest.writeString(this.userFirebaseToken);
    }

    protected UserData(Parcel in) {
        this.userId = in.readString();
        this.userName = in.readString();
        this.userUriPhoto = in.readString();
//      this.userGeoPoint = in.readString();
        this.userTypeVehicle = in.readString();
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

    @Override
    public int compare(UserData o1, UserData o2) {
        return  o1.getUserName().compareToIgnoreCase(o2.getUserName());
    }
}