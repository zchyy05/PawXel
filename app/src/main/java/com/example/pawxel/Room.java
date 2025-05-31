package com.example.pawxel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.media.MediaPlayer;
import android.app.AlertDialog;

import com.example.pawxel.utils.MusicManager;

import java.util.Objects;

public class Room extends BaseActivity {

    private float dX, dY;
    private int lastAction;

    private int health, hunger, thirst, energy, play;
    private MediaPlayer mediaPlayer;
    private MediaPlayer radioPlayer;
    private ImageView bed;
    private TextView healthText, hungerText, thirstText, energyText, playText;
    private Handler statHandler = new Handler();
    private Runnable statRunnable;
    private ImageView petImage;

    private boolean radioPlaying = false;
    private ImageView radio;
    private MediaPlayer airconPlayer;
    private boolean isAirconOn = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        Objects.requireNonNull(getSupportActionBar()).hide();

        SharedPreferences prefs = getSharedPreferences("PawxelPrefs", MODE_PRIVATE);
        String username = prefs.getString("loggedInUser", null);

        // Load saved stats per user
        health = prefs.getInt("health_" + username, 50);
        hunger = prefs.getInt("hunger_" + username, 50);
        thirst = prefs.getInt("thirst_" + username, 75);
        energy = prefs.getInt("energy_" + username, 80);
        play = prefs.getInt("play_" + username, 60);

        petImage = findViewById(R.id.petImage);
        ImageView food = findViewById(R.id.food);
        ImageView water = findViewById(R.id.water);
        ImageView aircon = findViewById(R.id.aircon);
        ImageView bone = findViewById(R.id.bone);
        ImageView door = findViewById(R.id.door);
        Button settings = findViewById(R.id.settings);
        healthText = findViewById(R.id.healthText);
        hungerText = findViewById(R.id.hungerText);
        thirstText = findViewById(R.id.thirstText);
        energyText = findViewById(R.id.energyText);
        playText = findViewById(R.id.playText);
        bed = findViewById(R.id.bed);
        radio = findViewById(R.id.radio);
        TextView achievementsButton = findViewById(R.id.achievementsButton);
        achievementsButton.setOnClickListener(v -> showAchievementsPopup());

        // Load pet data per user
        String pet = prefs.getString("pet_" + username, "dog");
        String petColor = prefs.getString("petColor_" + username, "white");
        int petDrawableRes = R.drawable.whitedog1;

        if ("dog".equals(pet)) {
            switch (petColor) {
                case "gray": petDrawableRes = R.drawable.graydog1; break;
                case "brown": petDrawableRes = R.drawable.browndog1; break;
                case "white": petDrawableRes = R.drawable.whitedog2; break;
                case "black": petDrawableRes = R.drawable.blackdog1; break;
                case "golden": petDrawableRes = R.drawable.yellowdog1; break;
                case "cream": petDrawableRes = R.drawable.creamdog1; break;
            }
        } else {
            switch (petColor) {
                case "black": petDrawableRes = R.drawable.blackcat1; break;
                case "brown": petDrawableRes = R.drawable.browncat1; break;
                case "cream": petDrawableRes = R.drawable.creamcat1; break;
                case "gray": petDrawableRes = R.drawable.graycat1; break;
                case "white": petDrawableRes = R.drawable.whitecat1; break;
                case "orange": petDrawableRes = R.drawable.yellowcat1; break;
            }
        }

        petImage.setImageResource(petDrawableRes);

        // Drag interaction
        petImage.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();
                    lastAction = MotionEvent.ACTION_DOWN;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    v.setX(event.getRawX() + dX);
                    v.setY(event.getRawY() + dY);
                    lastAction = MotionEvent.ACTION_MOVE;
                    return true;
                case MotionEvent.ACTION_UP:
                    float deltaX = Math.abs(event.getRawX() - (v.getX() - dX));
                    float deltaY = Math.abs(event.getRawY() - (v.getY() - dY));
                    int pettingCount = prefs.getInt("pettingCount_" + username, 0);
                    prefs.edit().putInt("pettingCount_" + username, pettingCount + 1).apply();

                    if (deltaX < 10 && deltaY < 10) {
                        play = Math.min(100, play + 5);
                        updateStatsDisplay();
                        playPetSound(pet);
                    }
                    if (isNear(petImage, bed)) {
                        energy = Math.min(100, energy + 5);
                        updateStatsDisplay();
                    }
                    return true;
                default:
                    return false;
            }
        });

        // Room Interactions
        food.setOnClickListener(v -> petImage.animate().x(food.getX()).y(food.getY() - 80).setDuration(500).withEndAction(() -> {
            hunger = Math.min(100, hunger + 5);
            health = Math.min(100, health + 5);
            thirst = Math.max(0, thirst - 2);
            int fedCount = prefs.getInt("fedCount_" + username, 0);
            prefs.edit().putInt("fedCount_" + username, fedCount + 1).apply();
            updateStatsDisplay();
        }).start());

        water.setOnClickListener(v -> petImage.animate().x(water.getX()).y(food.getY() - 80).setDuration(500).withEndAction(() -> {
            thirst = Math.min(100, thirst + 5);
            updateStatsDisplay();
        }).start());

        door.setOnClickListener(v -> {
            prepareForTransition();
            Intent intent = new Intent(Room.this, BathroomActivity.class);
            intent.putExtra("health", health);
            startActivityForResult(intent, 101);
        });

        bone.setOnClickListener(v -> {
            prepareForTransition();
            Intent intent = new Intent(Room.this, JumpGameActivity.class);
            intent.putExtra("pet", pet);
            intent.putExtra("petColor", petColor);
            startActivity(intent);
        });

        radio.setOnClickListener(v -> {
            if (isMuted()) {
                Toast.makeText(this, "Music is muted", Toast.LENGTH_SHORT).show();
                return;
            }
            if (radioPlaying) {
                if (radioPlayer != null) {
                    radioPlayer.stop();
                    radioPlayer.release();
                    radioPlayer = null;
                }
                radioPlaying = false;
                MusicManager.start(this);
                petImage.clearAnimation();
            } else {
                MusicManager.pause();
                radioPlayer = MediaPlayer.create(this, R.raw.bed);
                radioPlayer.setLooping(true);
                radioPlayer.start();
                radioPlaying = true;
                petImage.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.cat_listening));
            }
        });

        aircon.setOnClickListener(v -> {
            if (isMuted()) {
                Toast.makeText(this, "Music is muted", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isAirconOn) {
                if (airconPlayer != null) {
                    airconPlayer.stop();
                    airconPlayer.release();
                    airconPlayer = null;
                }
                isAirconOn = false;
            } else {
                airconPlayer = MediaPlayer.create(this, R.raw.aircon);
                airconPlayer.setLooping(true);
                airconPlayer.start();
                isAirconOn = true;
            }
        });

        settings.setOnClickListener(v -> {
            String[] options = {isMuted() ? "🔊 Unmute" : "🔇 Mute Music", "💾 Save Game", "🚪 Logout"};
            new AlertDialog.Builder(Room.this)
                    .setTitle("Settings")
                    .setItems(options, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                setMuted(!isMuted());
                                if (isMuted()) {
                                    MusicManager.pause();
                                    if (radioPlayer != null) radioPlayer.pause();
                                    if (airconPlayer != null) airconPlayer.pause();
                                } else if (!radioPlaying) {
                                    MusicManager.start(Room.this);
                                }
                                break;
                            case 1:
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putInt("health_" + username, health);
                                editor.putInt("hunger_" + username, hunger);
                                editor.putInt("thirst_" + username, thirst);
                                editor.putInt("energy_" + username, energy);
                                editor.putInt("play_" + username, play);
                                editor.apply();
                                break;
                            case 2:
                                SharedPreferences.Editor logoutEditor = prefs.edit();
                                logoutEditor.remove("loggedInUser");
                                logoutEditor.apply();
                                Intent intent = new Intent(Room.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                                break;
                        }
                    }).show();
        });

        updateStatsDisplay();
        startStatDecay();
    }

    private void updateStatsDisplay() {
        healthText.setText("❤️ Health: " + health);
        hungerText.setText("🍖 Hunger: " + hunger);
        thirstText.setText("💧 Thirst: " + thirst);
        energyText.setText("⚡ Energy: " + energy);
        playText.setText("🎾 Play: " + play);
    }
    private void showAchievementsPopup() {
        SharedPreferences prefs = getSharedPreferences("PawxelPrefs", MODE_PRIVATE);
        String username = prefs.getString("loggedInUser", "Player");

        StringBuilder achievements = new StringBuilder();

        // Fix: use the exact same key from JumpGameActivity
        int highScore = prefs.getInt("high_score_" + username, 0);
        int fedCount = prefs.getInt("fedCount_" + username, 0);
        int showeredCount = prefs.getInt("showeredCount_" + username, 0);
        int pettingCount = prefs.getInt("pettingCount_" + username, 0);

        achievements.append("🏆 High Score: ").append(highScore).append("\n");
        achievements.append("🍖 Times Fed: ").append(fedCount).append("\n");
        achievements.append("🛁 Times Showered: ").append(showeredCount).append("\n");
        achievements.append("🐾 Times Petted: ").append(pettingCount).append("\n");

        new AlertDialog.Builder(Room.this)
                .setTitle("Your Achievements")
                .setMessage(achievements.toString())
                .setPositiveButton("Close", null)
                .show();
    }


    private void startStatDecay() {
        statRunnable = new Runnable() {
            @Override
            public void run() {
                health = Math.max(0, health - 1);
                hunger = Math.max(0, hunger - 1);
                thirst = Math.max(0, thirst - 1);
                energy = Math.max(0, energy - 1);
                play = Math.max(0, play - 1);
                updateStatsDisplay();
                statHandler.postDelayed(this, 10000);
            }
        };
        statHandler.postDelayed(statRunnable, 10000);
    }

    private void playPetSound(String petType) {
        if (!isMuted()) {
            MusicManager.start(this);
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        int soundRes = petType.equals("dog") ? R.raw.bark : R.raw.cat;
        mediaPlayer = MediaPlayer.create(this, soundRes);
        mediaPlayer.start();
    }

    private boolean isNear(View pet, View target) {
        int[] petLocation = new int[2];
        int[] targetLocation = new int[2];
        pet.getLocationOnScreen(petLocation);
        target.getLocationOnScreen(targetLocation);
        return Math.abs(petLocation[0] - targetLocation[0]) < 100 && Math.abs(petLocation[1] - targetLocation[1]) < 100;
    }

    @Override
    protected void onDestroy() {
        statHandler.removeCallbacks(statRunnable);
        if (mediaPlayer != null) mediaPlayer.release();
        if (radioPlayer != null) radioPlayer.release();
        if (airconPlayer != null) airconPlayer.release();
        if (petImage != null) petImage.clearAnimation();
        if (!radioPlaying && !isMuted()) MusicManager.pause();
        super.onDestroy();
    }

    @Override
    protected boolean allowBackgroundMusic() {
        return true;
    }

    @Override
    protected boolean shouldPlayMusic() {
        return !isMuted() && !radioPlaying;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK) {
            health = data.getIntExtra("updatedHealth", health);
            updateStatsDisplay();
        }
    }
}
