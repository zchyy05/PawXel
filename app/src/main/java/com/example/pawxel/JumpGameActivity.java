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
import android.media.MediaPlayer;
import android.widget.Toast;

import com.example.pawxel.utils.MusicManager;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class JumpGameActivity extends BaseActivity {

    private ImageView dog, bg;
    private ArrayList<ImageView> obstacles = new ArrayList<>();
    private Button tryAgainButton, backToRoomButton;
    private TextView scoreText;
    private Handler handler = new Handler();
    private Runnable gameLoop;
    private float gravity = -4f; // Slightly reduced for realism
    private float velocityY = 0;
    private boolean isJumping = false;
    private boolean isHolding = false;
    private int score = 0;
    private boolean isGameOver = false;

    private TextView highScoreText;
    private SharedPreferences prefs;
    private static final String HIGH_SCORE_KEY = "high_score";

    private MediaPlayer gameMusic;
    private Random random = new Random();
    private int speed = 15;
    private String username;

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
        prefs = getSharedPreferences("PawxelPrefs", MODE_PRIVATE);
        username = prefs.getString("loggedInUser", null);
        if (username == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int highScore = prefs.getInt(HIGH_SCORE_KEY + "_" + username, 0);  // ✅ Safe to access now
        highScoreText.setText("High Score: " + highScore);
        tryAgainButton = findViewById(R.id.tryAgainButton);
        backToRoomButton = findViewById(R.id.backToRoomButton);

        for (int i = 1; i <= 6; i++) {
            int resId = getResources().getIdentifier("obstacle" + i, "id", getPackageName());
            obstacles.add(findViewById(resId));
        }

        String pet = prefs.getString("pet_" + username, "dog");
        String petColor = prefs.getString("petColor_" + username, "white");

        dog.setImageResource(getPetDrawable(pet, petColor));

        tryAgainButton.setOnClickListener(v -> recreate());
        backToRoomButton.setOnClickListener(v -> {
            stopGame();
            Intent intent = new Intent(JumpGameActivity.this, Room.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        bg.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!isJumping && !isGameOver) {
                        isJumping = true;
                        velocityY = 48; // Lowered for realism
                    }
                    isHolding = true;
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isHolding = false;
                    break;
            }
            return true;
        });

        startGameMusic();

        bg.post(() -> {
            float floorY = bg.getHeight() - dog.getHeight() - 190;
            dog.setY(floorY);
            startGameLoop();
        });
    }

    private void startGameMusic() {
        if (gameMusic == null) {
            gameMusic = MediaPlayer.create(this, R.raw.jump);
            gameMusic.setLooping(true);
            gameMusic.start();
        } else if (!gameMusic.isPlaying()) {
            gameMusic.start();
        }
    }


    private void stopGameMusic() {
        if (gameMusic != null) {
            if (gameMusic.isPlaying()) {
                gameMusic.stop();
            }
            gameMusic.release();
            gameMusic = null;
        }
    }


    private int getPetDrawable(String pet, String petColor) {
        if ("dog".equals(pet)) {
            switch (petColor) {
                case "gray": return R.drawable.graydog1;
                case "brown": return R.drawable.browndog1;
                case "white": return R.drawable.whitedog2;
                case "black": return R.drawable.blackdog1;
                case "golden": return R.drawable.yellowdog1;
                case "cream": return R.drawable.creamdog1;
                default: return R.drawable.whitedog1;
            }
        } else {
            switch (petColor) {
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

    private void startGameLoop() {
        int lastX = bg.getWidth() + 800;
        for (ImageView obs : obstacles) {
            int spacing = 500 + random.nextInt(600);
            obs.setX(lastX);
            lastX += spacing;
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

                float previousObstacleX = -9999;
                for (ImageView obs : obstacles) {
                    obs.setX(obs.getX() - speed);

                    if (obs.getX() < -obs.getWidth()) {
                        int spacing = 600 + random.nextInt(800);
                        float newX = bg.getWidth() + spacing;

                        if (Math.abs(newX - previousObstacleX) < 250) {
                            newX += 250;
                        }

                        obs.setX(newX);
                        previousObstacleX = newX;

                        score++;
                        scoreText.setText("Score: " + score);
                    }

                    // Adjust collision bounds to avoid early game over
                    float padding = 20;
                    if (dog.getX() + dog.getWidth() - padding > obs.getX() + padding &&
                            dog.getX() + padding < obs.getX() + obs.getWidth() - padding &&
                            dog.getY() + dog.getHeight() > obs.getY() + padding) {

                        isGameOver = true;
                        scoreText.setText("Game Over! Score: " + score);
                        int savedHighScore = prefs.getInt(HIGH_SCORE_KEY + "_" + username, 0);


                        if (score > savedHighScore) {
                            prefs.edit().putInt(HIGH_SCORE_KEY + "_" + username, score).apply();

                            highScoreText.setText("High Score: " + score);
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

    private void stopGame() {
        isGameOver = true;
        handler.removeCallbacksAndMessages(null);
        stopGameMusic();
    }

    @Override
    protected void onDestroy() {
        stopGame();
        super.onDestroy();
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopGameMusic();
        if (gameLoop != null) {
            handler.removeCallbacks(gameLoop);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isGameOver) {
            startGameMusic();
            handler.post(gameLoop);
        }
    }
    @Override
    protected boolean allowBackgroundMusic() {
        return false;
    }
}
