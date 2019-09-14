package com.example.Whack_a_Pikachu;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private int callerBtnId;
    private ArrayList<ParcelableGamePlayer> highscorePlayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Intent intent = getIntent();
        callerBtnId = intent.getIntExtra("callerBtnId", 0);
        highscorePlayers = intent.getParcelableArrayListExtra("highscorePlayers");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add Marker of all the players in the highscore list
        for(ParcelableGamePlayer tmp : highscorePlayers) {
            mMap.addMarker(new MarkerOptions().position(tmp.getLocation()).title(tmp.getName() + " - Score: " + tmp.getScore()));
            // Add Marker for the calling GamePlayer.
            if(tmp.getId() == callerBtnId) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(tmp.getLocation(), 15.0f));
            }
        }
    }
}
