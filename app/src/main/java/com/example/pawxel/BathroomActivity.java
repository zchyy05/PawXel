package com.example.pawxel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pawxel.database.AppDatabase;
import com.example.pawxel.database.User;
import com.example.pawxel.database.UserDao;

import java.util.Objects;

public class BathroomActivity extends BaseActivity {

    private ImageView shower, petImage;
    private TextView healthText;
    private int health;
    private MediaPlayer showerSoundPlayer;

    private float dX, dY;
    private Handler healthHandler = new Handler();
    private Runnable healthRunnable;
    private boolean showerTouching = false;
    private boolean hasCountedShower = false;

    private String username;
    private User user;
    private UserDao userDao;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bathroom);
        Objects.requireNonNull(getSupportActionBar()).hide();

        username = getSharedPreferences("PawxelPrefs", MODE_PRIVATE)
                .getString("loggedInUser", null);

        userDao = AppDatabase.getInstance(this).userDao();
        user = userDao.getUserByUsername(username);

        shower = findViewById(R.id.shower);
        petImage = findViewById(R.id.petImage);
        healthText = findViewById(R.id.healthText);

        // Load health from Room (passed via intent OR user.health)
        health = getIntent().getIntExtra("health", user.health);
        updateHealthDisplay();

        setPetAppearance(user.petType, user.petColor);

        enableDrag(petImage);
        enableDrag(shower);
        startHealthCheckLoop();

        findViewById(R.id.backToRoomButton).setOnClickListener(v -> {
            user.health = health;
            userDao.insert(user);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("updatedHealth", health);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void setPetAppearance(String pet, String color) {
        int petDrawable = R.drawable.whitedog1;

        if ("dog".equals(pet)) {
            switch (color) {
                case "gray": petDrawable = R.drawable.graydog1; break;
                case "brown": petDrawable = R.drawable.browndog1; break;
                case "white": petDrawable = R.drawable.whitedog2; break;
                case "black": petDrawable = R.drawable.blackdog1; break;
                case "golden": petDrawable = R.drawable.yellowdog1; break;
                case "cream": petDrawable = R.drawable.creamdog1; break;
            }
        } else {
            switch (color) {
                case "black": petDrawable = R.drawable.blackcat1; break;
                case "brown": petDrawable = R.drawable.browncat1; break;
                case "cream": petDrawable = R.drawable.creamcat1; break;
                case "gray": petDrawable = R.drawable.graycat1; break;
                case "white": petDrawable = R.drawable.whitecat1; break;
                case "orange": petDrawable = R.drawable.yellowcat1; break;
            }
        }

        petImage.setImageResource(petDrawable);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void enableDrag(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    v.setX(event.getRawX() + dX);
                    v.setY(event.getRawY() + dY);

                    boolean currentlyTouching = isOverlapping(shower, petImage);

                    if (currentlyTouching && !showerTouching) {
                        if (!isMuted()) {
                            if (showerSoundPlayer == null) {
                                showerSoundPlayer = MediaPlayer.create(this, R.raw.shower_sound);
                                showerSoundPlayer.setLooping(true);
                                showerSoundPlayer.start();
                            } else if (!showerSoundPlayer.isPlaying()) {
                                showerSoundPlayer.start();
                            }
                        }
                    } else if (!currentlyTouching && showerTouching) {
                        if (showerSoundPlayer != null && showerSoundPlayer.isPlaying()) {
                            showerSoundPlayer.pause();
                            showerSoundPlayer.seekTo(0);
                        }
                    }

                    showerTouching = currentlyTouching;
                    return true;
            }
            return false;
        });
    }

    private boolean isOverlapping(View v1, View v2) {
        int[] loc1 = new int[2];
        int[] loc2 = new int[2];
        v1.getLocationOnScreen(loc1);
        v2.getLocationOnScreen(loc2);

        return Math.abs(loc1[0] - loc2[0]) < 250 && Math.abs(loc1[1] - loc2[1]) < 250;
    }

    private void startHealthCheckLoop() {
        healthRunnable = new Runnable() {
            @Override
            public void run() {
                if (showerTouching) {
                    health = Math.min(100, health + 3);
                    updateHealthDisplay();
                    user.health = health;

                    if (!hasCountedShower) {
                        user.showeredCount++;
                        hasCountedShower = true;
                    }

                    userDao.insert(user);
                } else {
                    hasCountedShower = false;
                }

                healthHandler.postDelayed(this, 1000);
            }
        };

        healthHandler.postDelayed(healthRunnable, 1000);
    }

    private void updateHealthDisplay() {
        healthText.setText("❤️ Health: " + health);
    }

    @Override
    protected void onDestroy() {
        healthHandler.removeCallbacks(healthRunnable);

        if (showerSoundPlayer != null) {
            showerSoundPlayer.stop();
            showerSoundPlayer.release();
            showerSoundPlayer = null;
        }

        super.onDestroy();
    }

    @Override
    protected boolean allowBackgroundMusic() {
        return true;
    }

    @Override
    protected boolean shouldPlayMusic() {
        return !isMuted();
    }
}
