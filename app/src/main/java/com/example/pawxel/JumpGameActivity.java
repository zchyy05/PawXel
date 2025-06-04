package com.example.pawxel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.media.MediaPlayer;
import android.widget.Toast;

import com.example.pawxel.database.AppDatabase;
import com.example.pawxel.database.User;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class JumpGameActivity extends BaseActivity {

    private ImageView dog, bg;
    private ArrayList<ImageView> obstacles = new ArrayList<>();
    private Button tryAgainButton, backToRoomButton;
    private TextView scoreText, highScoreText;
    private Handler handler = new Handler();
    private Runnable gameLoop;
    private float gravity = -4f;
    private float velocityY = 0;
    private boolean isJumping = false, isHolding = false, isGameOver = false;

    private int score = 0, speed = 15;
    private MediaPlayer gameMusic;
    private Random random = new Random();

    private String username;
    private User currentUser;
    private Button startGameButton;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jump_game);
        Objects.requireNonNull(getSupportActionBar()).hide();

        dog = findViewById(R.id.dog);
        bg = findViewById(R.id.bg);
        scoreText = findViewById(R.id.scoreText);
        highScoreText = findViewById(R.id.highScoreText);
        tryAgainButton = findViewById(R.id.tryAgainButton);
        backToRoomButton = findViewById(R.id.backToRoomButton);
        startGameButton = findViewById(R.id.startGameButton);
        startGameButton.setOnClickListener(v -> {
            startGameButton.setVisibility(View.GONE);
            bg.post(() -> {
                float floorY = bg.getHeight() - dog.getHeight() - 190;
                dog.setY(floorY);
                startGameLoop();
            });
        });

        username = getSharedPreferences("PawxelPrefs", MODE_PRIVATE).getString("loggedInUser", null);

        if (username == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUser = AppDatabase.getInstance(this).userDao().getUserByUsername(username);
        if (currentUser == null) {
            Toast.makeText(this, "User not found in database", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        highScoreText.setText("High Score: " + currentUser.highScore);;

        for (int i = 1; i <= 6; i++) {
            int resId = getResources().getIdentifier("obstacle" + i, "id", getPackageName());
            obstacles.add(findViewById(resId));
        }

        dog.setImageResource(getPetDrawable(currentUser.petType, currentUser.petColor));

        tryAgainButton.setOnClickListener(v -> recreate());
        backToRoomButton.setOnClickListener(v -> {
            stopGame();
            Intent intent = new Intent(JumpGameActivity.this, Room.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        bg.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && !isJumping && !isGameOver) {
                isJumping = true;
                velocityY = 48;
                isHolding = true;
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                isHolding = false;
            }
            return true;
        });

        startGameMusic();


    }

    private void startGameLoop() {
        int lastX = bg.getWidth() + 800;
        for (ImageView obs : obstacles) {
            obs.setX(lastX);
            lastX += 500 + random.nextInt(600);
        }

        gameLoop = new Runnable() {
            @Override
            public void run() {
                if (isGameOver) return;

                if (isJumping) {
                    float gravityEffect = isHolding ? gravity / 2 : gravity;
                    velocityY += gravityEffect;
                    float newY = dog.getY() - velocityY;
                    float floorY = bg.getHeight() - dog.getHeight() - 190;

                    if (newY >= floorY) {
                        newY = floorY;
                        isJumping = false;
                        velocityY = 0;
                    }
                    dog.setY(newY);
                }

                float previousX = -9999;
                for (ImageView obs : obstacles) {
                    obs.setX(obs.getX() - speed);

                    if (obs.getX() < -obs.getWidth()) {
                        float newX = bg.getWidth() + 600 + random.nextInt(800);
                        if (Math.abs(newX - previousX) < 250) newX += 250;
                        obs.setX(newX);
                        previousX = newX;

                        score++;
                        scoreText.setText("Score: " + score);
                    }

                    if (dog.getX() + dog.getWidth() - 20 > obs.getX() + 20 &&
                            dog.getX() + 20 < obs.getX() + obs.getWidth() - 20 &&
                            dog.getY() + dog.getHeight() > obs.getY() + 20) {

                        isGameOver = true;
                        scoreText.setText("Game Over! Score: " + score);

                        if (score > currentUser.highScore) {
                            currentUser.highScore = score;
                            AppDatabase.getInstance(JumpGameActivity.this).userDao().insert(currentUser);
                            highScoreText.setText("High Score: " + currentUser.highScore);
                            Toast.makeText(JumpGameActivity.this, "🎉 New High Score!", Toast.LENGTH_SHORT).show();
                        }


                        tryAgainButton.setVisibility(View.VISIBLE);
                        backToRoomButton.setVisibility(View.VISIBLE);
                    }
                }

                handler.postDelayed(this, 30);
            }
        };

        handler.post(gameLoop);
    }

    private void startGameMusic() {
        if (gameMusic == null) {
            gameMusic = MediaPlayer.create(this, R.raw.jump);
            gameMusic.setLooping(true);
            gameMusic.start();
        }
    }

    private void stopGameMusic() {
        if (gameMusic != null) {
            gameMusic.stop();
            gameMusic.release();
            gameMusic = null;
        }
    }

    private void stopGame() {
        isGameOver = true;
        handler.removeCallbacksAndMessages(null);
        stopGameMusic();
    }

    private int getPetDrawable(String pet, String color) {
        if ("dog".equals(pet)) {
            switch (color) {
                case "gray": return R.drawable.graydog1;
                case "brown": return R.drawable.browndog1;
                case "white": return R.drawable.whitedog2;
                case "black": return R.drawable.blackdog1;
                case "golden": return R.drawable.yellowdog1;
                case "cream": return R.drawable.creamdog1;
                default: return R.drawable.whitedog1;
            }
        } else {
            switch (color) {
                case "black": return R.drawable.blackcat1;
                case "brown": return R.drawable.browncat1;
                case "cream": return R.drawable.creamcat1;
                case "gray": return R.drawable.graycat1;
                case "white": return R.drawable.whitecat1;
                case "orange": return R.drawable.yellowcat1;
                default: return R.drawable.whitecat1;
            }
        }
    }

    @Override protected void onDestroy() { stopGame(); super.onDestroy(); }
    @Override protected void onPause() { super.onPause(); stopGameMusic(); handler.removeCallbacks(gameLoop); }
    @Override protected void onResume() { super.onResume(); if (!isGameOver) { startGameMusic(); handler.post(gameLoop); } }
    @Override protected boolean allowBackgroundMusic() { return false; }
}
