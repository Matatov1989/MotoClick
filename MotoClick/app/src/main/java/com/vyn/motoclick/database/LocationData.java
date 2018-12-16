package com.vyn.motoclick.database;

import android.os.Parcel;
import android.os.Parcelable;

public class LocationData implements Parcelable {
    public double latitude;
    public double longitude;

    public LocationData() {
    }

    public LocationData(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
/*
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("latitude", latitude);
        result.put("longitude", longitude);


        return result;
    }*/

    @Override
    public int describeContents() {
        return 0;
    }



    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
    }

    protected LocationData(Parcel in) {
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
    }

    public static final Parcelable.Creator<LocationData> CREATOR = new Parcelable.Creator<LocationData>() {
        @Override
        public LocationData createFromParcel(Parcel source) {
            return new LocationData(source);
        }

        @Override
        public LocationData[] newArray(int size) {
            return new LocationData[size];
        }
    };
}