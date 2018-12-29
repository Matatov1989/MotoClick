package com.vyn.motoclick.database;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class UserData implements Parcelable, Comparator<UserData> {
    public String userId;
    public String userName;
    public String userUriPhoto;
    public int userTypeVehicle;
    public String userFirebaseToken;
    public GeoPoint userGeoPoint;
    public Timestamp userTimeStamp;
    public List<String> userListContacts;
    public List<String> userListChats;

    public UserData() {
    }

    public UserData(String userId, String userName, String userUriPhoto, int userTypeVehicle, String userFirebaseToken, GeoPoint userGeoPoint, Timestamp userTimeStamp) {
        this.userId = userId;
        this.userName = userName;
        this.userUriPhoto = userUriPhoto;
        this.userTypeVehicle = userTypeVehicle;
        this.userFirebaseToken = userFirebaseToken;
        this.userGeoPoint = userGeoPoint;
        this.userTimeStamp = userTimeStamp;
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

    public int getUserTypeVehicle() {
        return userTypeVehicle;
    }

    public void setUserTypeVehicle(int userTypeVehicle) {
        this.userTypeVehicle = userTypeVehicle;
    }

    public String getUserFirebaseToken() {
        return userFirebaseToken;
    }

    public void setUserFirebaseToken(String userFirebaseToken) {
        this.userFirebaseToken = userFirebaseToken;
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

    public List<String> getUserListContacts() {
        return userListContacts;
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

        dest.writeInt(this.userTypeVehicle);
        dest.writeString(this.userFirebaseToken);
        dest.writeList(this.userListContacts);

        dest.writeDouble(userGeoPoint.getLatitude());
        dest.writeDouble(userGeoPoint.getLongitude());

        dest.writeString(userTimeStamp.toDate().toString());

    }

    protected UserData(Parcel in) {
        this.userId = in.readString();
        this.userName = in.readString();
        this.userUriPhoto = in.readString();

        this.userTypeVehicle = in.readInt();
        this.userFirebaseToken = in.readString();
        userListContacts = new ArrayList<String>();
        in.readList(userListContacts, null);

        this.userGeoPoint = new GeoPoint(in.readDouble(), in.readDouble());

        this.userTimeStamp = new Timestamp(new Date(in.readString()));
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