package com.example.Whack_a_Pikachu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

import maes.tech.intentanim.CustomIntent;

import static android.view.View.VISIBLE;

public class GameActivity extends AppCompatActivity {

    public static final String TAG = "GameActivity";
    // Game States
    private static final int GAME_RUNNING = 0;
    private static final int GAME_LOST = 1;
    private static final int GAME_WON = 2;
    private static int gameStatus = -1;
    // Message Types of the Handler
    private static final int REMOVE = 0;
    // Handler Related
    private Handler gameHandler;
    private LinkedList<WhackButton> possibleButtonPicks;
    private Runnable timerRunnable;
    private Runnable addPikaRunnable;
    private Runnable addHaunterRunnable;
    // Game Data ?
    private boolean isHardMode;
    private WhackButton[] whackButtons;
    private final int progressBarAnimationMultiplier = 1000;
    ProgressBar scoreBar;
    int colsNum = 3;
    int rowsNum = 3;
    // Player Data
    private String player_name = "";
    private int player_score_clicked_objects = 0;
    private int current_score = 0;
    private int player_health = 3;
    // Lose \ Win Popup Related
    private View popupView = null;
    private PopupWindow popupWindow = null;
    // GPS Location
    private LocationHelper locationHelper;
    private static final int locationRequestCode = 1000;

    // nextInt is normally exclusive of the top value,
    // so add 1 to make it inclusive
    // int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Intent intent = getIntent();
        locationHelper = new LocationHelper(this, locationRequestCode);
        locationHelper.fetchLocation();
        player_name = intent.getStringExtra("player_name");
        isHardMode = intent.getBooleanExtra("is_hard_mode", false);
        if (isHardMode) {
            rowsNum = 4;
            colsNum = 4;
        } else {
            rowsNum = 3;
            colsNum = 3;
        }
        scoreBar = findViewById(R.id.score_progress);
        scoreBar.setMax(scoreBar.getMax() * progressBarAnimationMultiplier);
        whackButtons = new WhackButton[rowsNum * colsNum];
        RelativeLayout whack_layout = findViewById(R.id.whack_layout);
        whack_layout.addView(createNewGrid(colsNum, rowsNum));
        possibleButtonPicks = new LinkedList<>(Arrays.asList(whackButtons));
        gameHandler = createMessageGameHandler();
        createRunnables();
        resetGame();
    }

    @Override
    public void finish() {
        super.finish();
        CustomIntent.customType(this, "right-to-left");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeRunnables();
        if (popupWindow != null)
            popupWindow.dismiss();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == locationRequestCode) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationHelper.fetchLocation();
            } else {
                Toast.makeText(this, "Permission was not granted.", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private Handler createMessageGameHandler() {
        return new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                WhackButton whackButton = (WhackButton) msg.obj;
                if (msg.what == 0) {
                    if (gameStatus == GAME_RUNNING) {
                        if (whackButton.state == WhackButton.States.GOOD)
                            if (isHardMode)
                                loseHealth();
                        whackButton.setStateEmpty();
                        possibleButtonPicks.add(whackButton);
                    }
                }
            }
        };
    }

    private void createRunnables() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                /* and here comes the "trick" */
                if (gameStatus == GAME_RUNNING) {
                    updateGameTime();
                    gameHandler.postDelayed(this, 1000);
                }
            }
        };
        addPikaRunnable = new Runnable() {
            @Override
            public void run() {
                if (gameStatus == GAME_RUNNING) {
                    // Check if there are buttons to add...
                    if (possibleButtonPicks.size() > 0) {
                        // Randomly select a button to add
                        int selectedPikaIndex = ThreadLocalRandom.current().nextInt(0, possibleButtonPicks.size());
                        WhackButton selectedPika = possibleButtonPicks.get(selectedPikaIndex);
                        selectedPika.setStateGood();
                        // This button now can't be selected anymore.
                        possibleButtonPicks.remove(selectedPika);
                        // Add this button to the remove queue
                        //nextButtonToRemove.add(selectedPika);
                        int addNextPikaIn = ThreadLocalRandom.current().nextInt(1000, 1500);
                        if (isHardMode)
                            addNextPikaIn = ThreadLocalRandom.current().nextInt(250, 1000);
                        int addNextHaunterIn = ThreadLocalRandom.current().nextInt(1000, 1500);
                        int removeThisPikaIn = ThreadLocalRandom.current().nextInt(2000, 3000);
                        gameHandler.postDelayed(this, addNextPikaIn);
                        gameHandler.postDelayed(addHaunterRunnable, addNextHaunterIn);
                        Message msg = new Message();
                        msg.what = REMOVE;
                        msg.obj = selectedPika;
                        gameHandler.sendMessageDelayed(msg, removeThisPikaIn);
                        //gameHandler.postDelayed(removeWhackRunnable, removeThisPikaIn);
                    } else {
//                        gameHandler.removeCallbacks(addHaunterRunnable);
//                        gameHandler.removeCallbacks(removeWhackRunnable);
                        gameHandler.postDelayed(this, 1000);
                    }
                }
            }
        };
        addHaunterRunnable = new Runnable() {
            @Override
            public void run() {
                if (gameStatus == GAME_RUNNING) {
                    // Check if there are buttons to add...
                    if (possibleButtonPicks.size() > 0) {
                        // Randomly select a button to add
                        int selectedHaunterIndex = ThreadLocalRandom.current().nextInt(0, possibleButtonPicks.size());
                        WhackButton selectedHaunter = possibleButtonPicks.get(selectedHaunterIndex);
                        selectedHaunter.setStateBad();
                        // This button now can't be selected anymore.
                        possibleButtonPicks.remove(selectedHaunter);
                        // Add this button to the remove queue
                        //nextButtonToRemove.add(selectedHaunter);
                        int removeThisHaunterIn = ThreadLocalRandom.current().nextInt(2000, 3000);
                        //gameHandler.postDelayed(removeWhackRunnable, removeThisHaunterIn);
                        Message msg = new Message();
                        msg.what = REMOVE;
                        msg.obj = selectedHaunter;
                        gameHandler.sendMessageDelayed(msg, removeThisHaunterIn);
                    }
                }

            }
        };
    }

    private void addRunnables() {
        gameHandler.postDelayed(timerRunnable, 1000);
        int addNextPikaIn = ThreadLocalRandom.current().nextInt(1000, 2000);
        gameHandler.postDelayed(addPikaRunnable, addNextPikaIn);
    }

    private void removeRunnables() {
        gameHandler.removeCallbacks(timerRunnable);
        gameHandler.removeCallbacks(addPikaRunnable);
        gameHandler.removeCallbacks(addHaunterRunnable);
    }

    private GridLayout createNewGrid(int colsNum, int rowsNum) {
        ViewGroup.LayoutParams wrapContentParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        GridLayout gridLayout = new GridLayout(this);
        gridLayout.setLayoutParams(wrapContentParams);
        gridLayout.setColumnCount(colsNum);
        gridLayout.setRowCount(rowsNum);
        gridLayout.setId(View.generateViewId());

        int smallerFraction = colsNum < rowsNum ? colsNum : rowsNum;
        int biggerFraction = colsNum > rowsNum ? colsNum : rowsNum;
        int screenWidth = screenWidthPixels();
        int screenHeight = screenHeightPixels();
        int theSmallerAxis = screenHeight < screenWidth ? screenHeight : screenWidth; // Equals: Math.min(screenHeight, screenWidth);
        int buttonWidth = theSmallerAxis / smallerFraction;
        int buttonHeight = theSmallerAxis / biggerFraction;

        // Programmatically create the buttons layout
        for (int i = 0; i < rowsNum * colsNum; i++) {
            // Setting ConstraintLayout to include the Whack Button Image and the TextView
            ConstraintLayout pikaSpace = new ConstraintLayout(this);
            pikaSpace.setId(View.generateViewId());
            pikaSpace.setLayoutParams(wrapContentParams);

            // Create the TextView
            TextView givenScoreView = new TextView(this);
            givenScoreView.setId(View.generateViewId());
            givenScoreView.setLayoutParams(wrapContentParams);
            givenScoreView.setTypeface(ResourcesCompat.getFont(this, R.font.pokemon_solid));
            givenScoreView.setTextColor(getResources().getColor(R.color.pokeYellow));
            givenScoreView.setTextSize(40);

            // Create the Whack Button
            WhackButton pikaBtn = new WhackButton(this, buttonWidth, buttonHeight, i, givenScoreView);
            pikaBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    whackOnClick((WhackButton) view);
                }
            });

            pikaSpace.addView(pikaBtn);
            ConstraintLayoutCenterInParent(pikaSpace, pikaBtn);
            pikaSpace.addView(givenScoreView);
            ConstraintLayoutCenterInParent(pikaSpace, givenScoreView);
            whackButtons[i] = pikaBtn;
            gridLayout.addView(pikaSpace);
        }
        return gridLayout;
    }

    private int screenWidthPixels() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    private int screenHeightPixels() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public void updateGameTime() {
        TextView gameTimeView = findViewById(R.id.game_timer);
        String stringGameTime = gameTimeView.getText().toString();
        int intGameTime = Integer.parseInt(stringGameTime);
        if (intGameTime > 0)
            gameTimeView.setText(String.valueOf(intGameTime - 1));
        else
            gameLost();
    }

    public void resetGame() {
        resetTime();
        resetScore();
        resetHealth();
        for (WhackButton btn : whackButtons)
            btn.setStateEmpty();
        possibleButtonPicks = new LinkedList<>(Arrays.asList(whackButtons));
        gameStatus = GAME_RUNNING;
        if (popupWindow != null)
            popupWindow.dismiss();
        popupWindow = null;
        // Just in case they are still running...
        removeRunnables();
        // Add Runnable.
        addRunnables();
    }

    public void gameLost() {
        gameStatus = GAME_LOST;
        removeRunnables();
        showLosePopup(findViewById(R.id.game_timer));
    }

    public void gameWon() {
        gameStatus = GAME_WON;
        removeRunnables();

        showWinPopup(findViewById(R.id.game_timer), checkIsHighScore());
    }

    private boolean checkIsHighScore() {
        // load data
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("highscore_list", null);
        Type type = new TypeToken<ArrayList<SavedGamePlayer>>() {
        }.getType();
        ArrayList<SavedGamePlayer> highscorePlayersData = gson.fromJson(json, type);
        // if no data --> return empty arraylist
        if (highscorePlayersData == null)
            highscorePlayersData = new ArrayList<>();
        // sort the list by score
        Collections.sort(highscorePlayersData);
        Collections.reverse(highscorePlayersData);
        // create a SavedGamePlayer instance
        SavedGamePlayer currentPlayer = new SavedGamePlayer(player_name, player_score_clicked_objects, locationHelper.getCurrentLocation());
        // if my score is higher than the lowest score in the list --> new highscore!
        if (highscorePlayersData.size() < 10) {
            highscorePlayersData.add(currentPlayer);
            saveData(highscorePlayersData);
            return true;
        } else {
            if (player_score_clicked_objects > highscorePlayersData.get(highscorePlayersData.size() - 1).getScore()) {
                highscorePlayersData.remove(highscorePlayersData.size() - 1);
                highscorePlayersData.add(currentPlayer);
                // Sort again after adding.
                Collections.sort(highscorePlayersData);
                Collections.reverse(highscorePlayersData);
                saveData(highscorePlayersData);
                return true;
            }
        }
        return false;
    }

    private void saveData(ArrayList<SavedGamePlayer> highscorePlayersData) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<SavedGamePlayer>>() {
        }.getType();
        String json = gson.toJson(highscorePlayersData, type);
        editor.putString("highscore_list", json);
        editor.apply();
    }

    public void showLosePopup(View view) {
        // Inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.lose_popup_window, null, false);
        // Set the ImageView as GIF
        ImageView pikaGif = popupView.findViewById(R.id.run_pikachu);
        Glide.with(popupView).load(R.drawable.happy_pikachu).into(pikaGif);
        // Set the player name on the losing screen
        TextView loseMsg = popupView.findViewById(R.id.lose_view_upper);
        loseMsg.setText(loseMsg.getText() + " " + player_name);
        // Set Buttons Listener
        popupView.findViewById(R.id.play_again_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetGame();
            }
        });
        popupView.findViewById(R.id.back_to_menu_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        // Fullscreen popup
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        if (popupWindow == null) {
            popupWindow = new PopupWindow(popupView, width, height, true);
            popupWindow.showAtLocation(view, Gravity.FILL, 0, 0);
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    if (gameStatus != GAME_RUNNING)
                        finish();
                }
            });
        }
    }

    public void showWinPopup(View view, boolean isHighScore) {
        // Inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.win_popup_window, null, false);
        // Set the ImageView as GIF
        ImageView pikaGif = popupView.findViewById(R.id.sad_pikachu);
        Glide.with(popupView).load(R.drawable.sad_pikachu).into(pikaGif);
        // Set the player name on the losing screen
        TextView winMsg = popupView.findViewById(R.id.win_view_upper);
        winMsg.setText((winMsg.getText() + " " + player_name + "\n( Score: " + player_score_clicked_objects + " )"));
        if (isHighScore) {
            popupView.findViewById(R.id.new_highscore).setVisibility(VISIBLE);
            Button highScoresBtn = popupView.findViewById(R.id.highscores_btn);
            highScoresBtn.setVisibility(VISIBLE);
            highScoresBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showHighscores();
                }
            });

        }
        // Set Buttons Listener
        popupView.findViewById(R.id.play_again_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetGame();
            }
        });
        popupView.findViewById(R.id.back_to_menu_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        // Fullscreen popup
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        if (popupWindow == null) {
            popupWindow = new PopupWindow(popupView, width, height, true);
            popupWindow.showAtLocation(view, Gravity.FILL, 0, 0);
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    if (gameStatus != GAME_RUNNING)
                        finish();
                }
            });
        }
    }

    public void whackOnClick(WhackButton view) {
        ProgressBar scoreBar = findViewById(R.id.score_progress);
        current_score = scoreBar.getProgress();
        String givenScore = "";
        switch (view.state) {
            case EMPTY:
                loseHealth();
                break;
            case BAD:
                if (isHardMode) {
                    givenScore = "-5";
                    current_score -= (5 * progressBarAnimationMultiplier);
                } else {
                    givenScore = "-3";
                    current_score -= (3 * progressBarAnimationMultiplier);
                }
                break;
            case GOOD:
                if (isHardMode) {
                    int tempScore = ThreadLocalRandom.current().nextInt(1, 3);
                    givenScore = "+" + tempScore;
                    current_score += (tempScore * progressBarAnimationMultiplier);
                } else {
                    int tempScore = ThreadLocalRandom.current().nextInt(2, 5);
                    givenScore = "+" + tempScore;
                    current_score += (tempScore * progressBarAnimationMultiplier);
                }
                player_score_clicked_objects += 1;
                break;
        }
        view.setStateEmpty();
        view.givenScoreView.setText(givenScore);
        YoYo.with(Techniques.FadeOutUp).duration(1000).playOn(view.givenScoreView);
        scoreBar.setInterpolator(new AccelerateDecelerateInterpolator());
        ObjectAnimator.ofInt(scoreBar, "progress", current_score)
                .setDuration(500)
                .start();
        if (current_score >= scoreBar.getMax())
            gameWon();
    }

    public void resetHealth() {
        player_health = 3;
        ImageView[] currentHealth = {findViewById(R.id.hp_1), findViewById(R.id.hp_2), findViewById(R.id.hp_3)};
        for (ImageView i : currentHealth) {
            i.setImageResource(R.drawable.health_ok);
        }
    }

    public void resetScore() {
        player_score_clicked_objects = 0;
        current_score = 0;
        // Reset score
        ProgressBar scoreBar = findViewById(R.id.score_progress);
        scoreBar.setProgress(current_score);
    }

    public void resetTime() {
        TextView gameTimeView = findViewById(R.id.game_timer);
        gameTimeView.setText(R.string.time_left);
    }

    public void loseHealth() {
        ImageView currentHealth = findViewById(R.id.hp_3);
        switch (player_health) {
            case 1:
                currentHealth = findViewById(R.id.hp_1);
                break;
            case 2:
                currentHealth = findViewById(R.id.hp_2);
                break;
            case 3:
                currentHealth = findViewById(R.id.hp_3);
                break;
        }
        currentHealth.setImageResource(R.drawable.health_bad);
        YoYo.with(Techniques.Flash).duration(500).repeat(2).playOn(currentHealth);
        player_health -= 1;
        if (player_health == 0)
            gameLost();
    }

    public void ConstraintLayoutCenterInParent(ConstraintLayout parent, View centerThis) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(parent);
        constraintSet.connect(centerThis.getId(), ConstraintSet.LEFT, parent.getId(), ConstraintSet.LEFT);
        constraintSet.connect(centerThis.getId(), ConstraintSet.TOP, parent.getId(), ConstraintSet.TOP);
        constraintSet.connect(centerThis.getId(), ConstraintSet.RIGHT, parent.getId(), ConstraintSet.RIGHT);
        constraintSet.connect(centerThis.getId(), ConstraintSet.BOTTOM, parent.getId(), ConstraintSet.BOTTOM);
        constraintSet.applyTo(parent);
    }

    public void showHighscores() {
        Intent intent = new Intent(getApplicationContext(), HighscoresActivity.class);
        intent.putExtra("new_high_score", true);
        intent.putExtra("name", player_name);
        intent.putExtra("score", player_score_clicked_objects);
        startActivity(intent);
        CustomIntent.customType(this, "bottom-to-up");
    }
}