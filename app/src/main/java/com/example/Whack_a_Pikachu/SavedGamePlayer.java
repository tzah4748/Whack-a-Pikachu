package com.example.Whack_a_Pikachu;

import com.google.android.gms.maps.model.LatLng;

public class SavedGamePlayer implements Comparable<SavedGamePlayer> {
    private String name;
    private int score;
    private LatLng location;

    SavedGamePlayer(String name, int score, LatLng location) {
        this.name = name;
        this.score = score;
        this.location = location;
    }

    String getName() {
        return name;
    }

    int getScore() {
        return score;
    }

    LatLng getLocation() { return location; }

    @Override
    public int compareTo(SavedGamePlayer o) {
        return getScore() - o.getScore();
    }
}
