package com.example.pawxel.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import com.example.pawxel.R;

public class MusicManager {
    private static MediaPlayer mediaPlayer;
    private static boolean skipNextStart = false;
    private static Handler handler = new Handler(Looper.getMainLooper());
    private static Runnable pendingStart;

    public static void skipNextStart() {
        skipNextStart = true;
    }

    public static void start(Context context) {
        // Cancel any pending start operations
        if (pendingStart != null) {
            handler.removeCallbacks(pendingStart);
            pendingStart = null;
        }

        if (skipNextStart) {
            skipNextStart = false;
            return;
        }

        // Ensure we're not already playing
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            return;
        }

        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context.getApplicationContext(), R.raw.main);
                if (mediaPlayer != null) {
                    mediaPlayer.setLooping(true);
                    mediaPlayer.start();
                }
            } else if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // If there's an error, recreate the MediaPlayer
            stop();
            try {
                mediaPlayer = MediaPlayer.create(context.getApplicationContext(), R.raw.main);
                if (mediaPlayer != null) {
                    mediaPlayer.setLooping(true);
                    mediaPlayer.start();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void pause() {
        // Cancel any pending start operations
        if (pendingStart != null) {
            handler.removeCallbacks(pendingStart);
            pendingStart = null;
        }

        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        // Cancel any pending start operations
        if (pendingStart != null) {
            handler.removeCallbacks(pendingStart);
            pendingStart = null;
        }

        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mediaPlayer = null; // Ensure it's null even if release fails
        }
    }

    public static boolean isPlaying() {
        try {
            return mediaPlayer != null && mediaPlayer.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }
}