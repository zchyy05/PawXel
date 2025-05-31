package com.example.pawxel;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pawxel.utils.MusicManager;

public class BaseActivity extends AppCompatActivity {
    private static boolean isTransitioning = false;
    protected boolean allowBackgroundMusic() {
        return false; // default: no music
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isTransitioning = false;

        if (allowBackgroundMusic() && shouldPlayMusic()) {
            getWindow().getDecorView().postDelayed(() -> {
                if (!isFinishing() && !isDestroyed() && !isTransitioning) {
                    MusicManager.start(this);
                }
            },400);
        }
        hideSystemUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Set transition flag and pause music immediately
        isTransitioning = true;
        MusicManager.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Always pause music when activity stops
        MusicManager.pause();
    }

    // Method to be called before starting new activities
    protected void prepareForTransition() {
        isTransitioning = true;
        MusicManager.pause();
    }

    protected boolean shouldPlayMusic() {
        return !isMuted();
    }

    protected boolean isMuted() {
        SharedPreferences prefs = getSharedPreferences("PawxelPrefs", MODE_PRIVATE);
        return prefs.getBoolean("isMuted", false);
    }

    protected void setMuted(boolean muted) {
        SharedPreferences prefs = getSharedPreferences("PawxelPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isMuted", muted);
        editor.apply();
    }

    protected void hideSystemUI() {
        View decorView = getWindow().getDecorView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            if (getWindow().getInsetsController() != null) {
                getWindow().getInsetsController().hide(
                        WindowInsets.Type.statusBars() |
                                WindowInsets.Type.navigationBars() |
                                WindowInsets.Type.systemBars()
                );
                getWindow().getInsetsController().setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            );

            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_FULLSCREEN
            );
        }
    }
}