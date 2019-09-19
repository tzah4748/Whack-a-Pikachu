package com.example.Whack_a_Pikachu;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class ParcelableGamePlayer implements Parcelable {

    public static final Creator<ParcelableGamePlayer> CREATOR = new Creator<ParcelableGamePlayer>() {
        @Override
        public ParcelableGamePlayer createFromParcel(Parcel in) {
            return new ParcelableGamePlayer(in);
        }

        @Override
        public ParcelableGamePlayer[] newArray(int size) {
            return new ParcelableGamePlayer[size];
        }
    };

    private int id;
    private String name;
    private int score;
    private LatLng location;

    public ParcelableGamePlayer(int id, String name, int score, LatLng location) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.location = location;

    }

    protected ParcelableGamePlayer(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.score = in.readInt();
        this.location = in.readParcelable(LatLng.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.id);
        parcel.writeString(this.name);
        parcel.writeInt(this.score);
        parcel.writeParcelable(this.location, i);
    }

    public int getId() { return id; }

    public String getName() { return name; }

    public int getScore() { return score; }

    public LatLng getLocation() { return location; }
}
