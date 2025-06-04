package com.example.pawxel;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pawxel.database.AppDatabase;
import com.example.pawxel.database.User;
import com.example.pawxel.database.UserDao;
import com.example.pawxel.utils.MusicManager;

import java.util.Objects;

public class Room extends BaseActivity {

    private float dX, dY;
    private int lastAction;

    private int health, hunger, thirst, energy, play;
    private MediaPlayer mediaPlayer;
    private MediaPlayer radioPlayer;
    private MediaPlayer airconPlayer;

    private boolean radioPlaying = false;
    private boolean isAirconOn = false;

    private Handler statHandler = new Handler();
    private Runnable statRunnable;

    private ImageView petImage, bed, radio;
    private TextView healthText, hungerText, thirstText, energyText, playText;

    private UserDao userDao;
    private User user;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        Objects.requireNonNull(getSupportActionBar()).hide();

        String username = getSharedPreferences("PawxelPrefs", MODE_PRIVATE)
                .getString("loggedInUser", null);

        userDao = AppDatabase.getInstance(this).userDao();
        user = userDao.getUserByUsername(username);

        health = user.health;
        hunger = user.hunger;
        thirst = user.thirst;
        energy = user.energy;
        play = user.play;

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

        setPetAppearance(user.petType, user.petColor);

        ImageView chicken = findViewById(R.id.chicken);

        chicken.setOnClickListener(v -> {
            Intent intent = new Intent(Room.this, CatchGame.class);
            intent.putExtra("petType", user.petType);
            intent.putExtra("petColor", user.petColor);
            startActivity(intent);

        });

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

                    user.pettingCount++;
                    if (deltaX < 10 && deltaY < 10) {
                        play = Math.min(100, play + 5);
                        user.play = play;
                        updateStatsDisplay();
                        playPetSound(user.petType);
                    }
                    if (isNear(petImage, bed)) {
                        energy = Math.min(100, energy + 5);
                        user.energy = energy;
                        updateStatsDisplay();
                    }
                    userDao.insert(user);
                    return true;
                default:
                    return false;
            }
        });
        float bottomOffset = -40f;
        food.setOnClickListener(v -> {
            petImage.animate()
                    .x(food.getX() + food.getWidth() / 2f - petImage.getWidth() / 2f)
                    .y(food.getY() + food.getHeight() / 2f - petImage.getHeight() / 2f + bottomOffset)
                    .setDuration(500)
                    .withEndAction(() -> {
                hunger = Math.min(100, hunger + 5);
                health = Math.min(100, health + 5);
                thirst = Math.max(0, thirst - 2);
                user.fedCount++;
                user.hunger = hunger;
                user.health = health;
                user.thirst = thirst;
                updateStatsDisplay();
                userDao.insert(user);
            }).start();
        });

        water.setOnClickListener(v -> {
            petImage.animate()
                    .x(water.getX() + water.getWidth() / 2f - petImage.getWidth() / 2f)
                    .y(water.getY() + water.getHeight() / 2f - petImage.getHeight() / 2f + bottomOffset)
                    .setDuration(500)
                    .withEndAction(() -> {
                thirst = Math.min(100, thirst + 5);
                user.thirst = thirst;
                updateStatsDisplay();
                userDao.insert(user);
            }).start();
        });

        door.setOnClickListener(v -> {
            Intent intent = new Intent(Room.this, BathroomActivity.class);
            intent.putExtra("health", health);
            startActivityForResult(intent, 101);
        });

        bone.setOnClickListener(v -> {
            Intent intent = new Intent(Room.this, JumpGameActivity.class);
            intent.putExtra("pet", user.petType);
            intent.putExtra("petColor", user.petColor);
            startActivity(intent);
        });

        radio.setOnClickListener(v -> {
            if (isMuted()) {
                Toast.makeText(this, "Music is muted", Toast.LENGTH_SHORT).show();
                return;
            }

            if (radioPlaying) {
                stopPlayer(radioPlayer);
                radioPlayer = null;
                radioPlaying = false;

                MusicManager.start(this);
                petImage.clearAnimation();
            } else {
                stopPlayer(radioPlayer);
                radioPlayer = null;

                MusicManager.pause();

                radioPlayer = MediaPlayer.create(this, R.raw.bed);
                if (radioPlayer != null) {
                    radioPlayer.setLooping(true);
                    radioPlayer.setOnErrorListener((mp, what, extra) -> {
                        Toast.makeText(this, "Radio error", Toast.LENGTH_SHORT).show();
                        return true;
                    });
                    try {
                        radioPlayer.start();
                        radioPlaying = true;
                        petImage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.cat_listening));
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error starting radio", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Failed to create radio player", Toast.LENGTH_SHORT).show();
                }
            }
        });


        aircon.setOnClickListener(v -> {
            if (isMuted()) {
                Toast.makeText(this, "Music is muted", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isAirconOn) {
                stopPlayer(airconPlayer);
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
                                    stopPlayer(radioPlayer);
                                    stopPlayer(airconPlayer);
                                } else if (!radioPlaying) {
                                    MusicManager.start(Room.this);
                                }
                                break;
                            case 1:
                                user = userDao.getUserByUsername(username);
                                user.health = health;
                                user.hunger = hunger;
                                user.thirst = thirst;
                                user.energy = energy;
                                user.play = play;
                                userDao.insert(user);
                                Toast.makeText(this, "Game Saved!", Toast.LENGTH_SHORT).show();
                                break;
                            case 2:
                                getSharedPreferences("PawxelPrefs", MODE_PRIVATE).edit().remove("loggedInUser").apply();
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



    private void playPetSound(String petType) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        int soundResId = "dog".equals(petType) ? R.raw.bark : R.raw.cat;
        mediaPlayer = MediaPlayer.create(this, soundResId);
        mediaPlayer.start();
    }


    private void setPetAppearance(String pet, String color) {
        int drawable = R.drawable.whitedog1;
        if ("dog".equals(pet)) {
            switch (color) {
                case "gray": drawable = R.drawable.graydog1; break;
                case "brown": drawable = R.drawable.browndog1; break;
                case "white": drawable = R.drawable.whitedog2; break;
                case "black": drawable = R.drawable.blackdog1; break;
                case "golden": drawable = R.drawable.yellowdog1; break;
                case "cream": drawable = R.drawable.creamdog1; break;
            }
        } else {
            switch (color) {
                case "black": drawable = R.drawable.blackcat1; break;
                case "brown": drawable = R.drawable.browncat1; break;
                case "cream": drawable = R.drawable.creamcat1; break;
                case "gray": drawable = R.drawable.graycat1; break;
                case "white": drawable = R.drawable.whitecat1; break;
                case "orange": drawable = R.drawable.yellowcat1; break;
            }
        }
        petImage.setImageResource(drawable);
    }

    private void showAchievementsPopup() {
        String username = getSharedPreferences("PawxelPrefs", MODE_PRIVATE).getString("loggedInUser", null);
        user = userDao.getUserByUsername(username);

        StringBuilder achievements = new StringBuilder();
        achievements.append("🏆 High Score: ").append(user.highScore).append("\n");
        achievements.append("🎯 Catch Game High Score: ").append(user.catchHighScore).append("\n");
        achievements.append("🍖 Times Fed: ").append(user.fedCount).append("\n");
        achievements.append("🛁 Times Showered: ").append(user.showeredCount).append("\n");
        achievements.append("🐾 Times Petted: ").append(user.pettingCount).append("\n");

        new AlertDialog.Builder(this)
                .setTitle("Your Achievements")
                .setMessage(achievements.toString())
                .setPositiveButton("Close", null)
                .show();
    }


    private void updateStatsDisplay() {
        healthText.setText("❤️ Health: " + health);
        hungerText.setText("🍖 Hunger: " + hunger);
        thirstText.setText("💧 Thirst: " + thirst);
        energyText.setText("⚡ Energy: " + energy);
        playText.setText("🎾 Play: " + play);
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

                user.health = health;
                user.hunger = hunger;
                user.thirst = thirst;
                user.energy = energy;
                user.play = play;
                userDao.insert(user);

                updateStatsDisplay();
                statHandler.postDelayed(this, 10000);
            }
        };
        statHandler.postDelayed(statRunnable, 10000);
    }

    private void stopPlayer(MediaPlayer player) {
        try {
            if (player != null && player.isPlaying()) {
                player.stop();
            }
            if (player != null) {
                player.release();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
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

        stopPlayer(mediaPlayer);
        mediaPlayer = null;

        stopPlayer(radioPlayer);
        radioPlayer = null;

        stopPlayer(airconPlayer);
        airconPlayer = null;

        if (petImage != null) petImage.clearAnimation();

        if (!radioPlaying && !isMuted()) MusicManager.pause();

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopPlayer(radioPlayer);
        radioPlayer = null;

        stopPlayer(airconPlayer);
        airconPlayer = null;
    }

    @Override
    protected boolean allowBackgroundMusic() {
        return true;
    }
    @Override
    protected void onResume() {
        super.onResume();

        String username = getSharedPreferences("PawxelPrefs", MODE_PRIVATE)
                .getString("loggedInUser", null);
        user = userDao.getUserByUsername(username);

        updateStatsDisplay();
    }

    @Override
    protected boolean shouldPlayMusic() {
        return !isMuted() && !radioPlaying;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK) {
            String username = getSharedPreferences("PawxelPrefs", MODE_PRIVATE)
                    .getString("loggedInUser", null);

            // 💡 RELOAD the latest user data from DB
            user = userDao.getUserByUsername(username);

            // Update health from result data
            health = data.getIntExtra("updatedHealth", user.health);
            user.health = health;

            updateStatsDisplay();
        }
    }

}
