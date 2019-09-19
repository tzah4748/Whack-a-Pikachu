package com.example.Whack_a_Pikachu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;

import maes.tech.intentanim.CustomIntent;

public class HighscoresActivity extends AppCompatActivity {

    ArrayList<SavedGamePlayer> highscorePlayersData;
    ArrayList<GamePlayer> highscorePlayers;
    ArrayList<ParcelableGamePlayer> parcelableGamePlayers;
    LinearLayout highscore_list;
    View.OnClickListener locationBtnOnClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscores);
        highscore_list = findViewById(R.id.highscores_list);
        highscorePlayers = new ArrayList<>();
        parcelableGamePlayers = new ArrayList<>();
        findViewById(R.id.reset_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetData();
            }
        });
        locationBtnOnClickListener = new  View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMapActivity((ImageButton) view);
            }
        };
        loadData();
    }

    @Override
    public void finish() {
        super.finish();
        CustomIntent.customType(this, "up-to-bottom");
    }

    private void resetData() {
        highscore_list.removeAllViews();
        highscorePlayersData = new ArrayList<>();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<SavedGamePlayer>>() {}.getType();
        String json = gson.toJson(highscorePlayersData, type);
        editor.putString("highscore_list", json);
        editor.apply();
        loadData();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("highscore_list", null);
        Type type = new TypeToken<ArrayList<SavedGamePlayer>>() {}.getType();
        highscorePlayersData = gson.fromJson(json, type);
        if (highscorePlayersData == null)
            highscorePlayersData = new ArrayList<>();
        Collections.sort(highscorePlayersData);
        Collections.reverse(highscorePlayersData);
        for(SavedGamePlayer tmp : highscorePlayersData) {
            GamePlayer gp  = new GamePlayer(this, tmp.getName(), tmp.getScore(), tmp.getLocation());
            gp.getLocationBtn().setOnClickListener(locationBtnOnClickListener);
            highscorePlayers.add(gp);
            highscore_list.addView(gp);
            parcelableGamePlayers.add(new ParcelableGamePlayer(gp.getLocationBtn().getId(), tmp.getName(), tmp.getScore(), tmp.getLocation()));

        }
    }

    private void openMapActivity(ImageButton caller_btn) {
        // Search for the caller_btn object
        for(ParcelableGamePlayer tmp : parcelableGamePlayers) {
            if(caller_btn.getId() == tmp.getId()) {
                // When found, check that location isn't null
                if(tmp.getLocation() != null) {
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    intent.putExtra("callerBtnId", caller_btn.getId());
                    intent.putParcelableArrayListExtra("highscorePlayers", parcelableGamePlayers);
                    startActivity(intent);
                    CustomIntent.customType(this, "bottom-to-up");
                } else {
                    // If null, don't open the MapsActivity.
                    Toast.makeText(this, "Location unavailable.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
