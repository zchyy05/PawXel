package com.example.pawxel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bathroom);
        Objects.requireNonNull(getSupportActionBar()).hide();

        SharedPreferences prefs = getSharedPreferences("PawxelPrefs", MODE_PRIVATE);
        username = prefs.getString("loggedInUser", null);

        shower = findViewById(R.id.shower);
        petImage = findViewById(R.id.petImage);
        healthText = findViewById(R.id.healthText);

        // Load health (if passed via intent or fallback to stored)
        health = getIntent().getIntExtra("health", prefs.getInt("health_" + username, 100));
        updateHealthDisplay();

        // Load pet image
        String pet = prefs.getString("pet_" + username, "dog");
        String petColor = prefs.getString("petColor_" + username, "white");
        int petDrawable = R.drawable.whitedog1;

        if ("dog".equals(pet)) {
            switch (petColor) {
                case "gray": petDrawable = R.drawable.graydog1; break;
                case "brown": petDrawable = R.drawable.browndog1; break;
                case "white": petDrawable = R.drawable.whitedog2; break;
                case "black": petDrawable = R.drawable.blackdog1; break;
                case "golden": petDrawable = R.drawable.yellowdog1; break;
                case "cream": petDrawable = R.drawable.creamdog1; break;
            }
        } else {
            switch (petColor) {
                case "black": petDrawable = R.drawable.blackcat1; break;
                case "brown": petDrawable = R.drawable.browncat1; break;
                case "cream": petDrawable = R.drawable.creamcat1; break;
                case "gray": petDrawable = R.drawable.graycat1; break;
                case "white": petDrawable = R.drawable.whitecat1; break;
                case "orange": petDrawable = R.drawable.yellowcat1; break;
            }
        }

        petImage.setImageResource(petDrawable);

        enableDrag(petImage);
        enableDrag(shower);
        startHealthCheckLoop();

        findViewById(R.id.backToRoomButton).setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updatedHealth", health);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
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
                                showerSoundPlayer = MediaPlayer.create(BathroomActivity.this, R.raw.shower_sound);
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

        int v1x = loc1[0], v1y = loc1[1];
        int v2x = loc2[0], v2y = loc2[1];

        int horizontalMargin = 250;
        int verticalMargin = 250;

        return Math.abs(v1x - v2x) < horizontalMargin && Math.abs(v1y - v2y) < verticalMargin;
    }

    private void startHealthCheckLoop() {
        healthRunnable = new Runnable() {
            @Override
            public void run() {
                if (showerTouching) {
                    health = Math.min(100, health + 3);
                    updateHealthDisplay();

                    SharedPreferences prefs = getSharedPreferences("PawxelPrefs", MODE_PRIVATE);
                    prefs.edit().putInt("health_" + username, health).apply();

                    // ✅ Count only once per session
                    if (!hasCountedShower) {
                        int showeredCount = prefs.getInt("showeredCount_" + username, 0);
                        prefs.edit().putInt("showeredCount_" + username, showeredCount + 1).apply();
                        hasCountedShower = true;
                    }
                } else {
                    // Reset flag when user stops showering
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
