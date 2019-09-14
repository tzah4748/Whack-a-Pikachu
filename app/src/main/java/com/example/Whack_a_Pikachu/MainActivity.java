package com.example.Whack_a_Pikachu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import maes.tech.intentanim.CustomIntent;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText playerNameEdit;
    private Snackbar feedbackSnackbar;
    private CheckBox isHardMode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Gif of Pikachu
        Glide.with(this).load(R.drawable.main_pikachu).dontTransform().into((ImageView) findViewById(R.id.mainPikachu));
        // Outline the title
        TextView textViewShadow = findViewById(R.id.textViewShadowId);
        textViewShadow.getPaint().setStrokeWidth(5);
        textViewShadow.getPaint().setStyle(Paint.Style.STROKE);
        playerNameEdit = findViewById(R.id.player_name);
        feedbackSnackbar = Snackbar.make(playerNameEdit, "Please enter a player name", Snackbar.LENGTH_SHORT);
        isHardMode = findViewById(R.id.is_hard_mode);
        findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGame();
            }
        });
        findViewById(R.id.highscores).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHighscores();
            }
        });
    }

    public void startGame() {
        String playerName = playerNameEdit.getText().toString();
        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
        intent.putExtra("player_name", playerName);
        intent.putExtra("is_hard_mode", isHardMode.isChecked());
        if(playerName.length() > 0) {
            startActivity(intent);
            CustomIntent.customType(this, "left-to-right");
        } else {
            feedbackSnackbar.show();
        }
    }

    public void showHighscores() {
        Intent intent = new Intent(getApplicationContext(), HighscoresActivity.class);
        startActivity(intent);
        CustomIntent.customType(this, "bottom-to-up");
    }
}
