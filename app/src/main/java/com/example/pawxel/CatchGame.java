package com.example.pawxel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.pawxel.database.AppDatabase;
import com.example.pawxel.database.User;
import com.example.pawxel.database.UserDao;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Update;

import java.util.Random;

public class CatchGame extends AppCompatActivity {

    private FrameLayout gameLayout;
    private ImageView petView;
    private TextView scoreText;

    private int screenWidth;
    private int score = 0;
    private int missed = 0;

    private String petType;
    private String petColor;
    private TextView highScoreText;
    private UserDao userDao;
    private User user;

    private int[] foodImages = {
            R.drawable.egg,
            R.drawable.milk,
            R.drawable.fish,
            R.drawable.chicken,
            R.drawable.beef
    };

    private int[] foodPoints = {1, 2, 3, 4, 5};

    private Handler fallHandler = new Handler();
    private Random random = new Random();
    private Button tryAgainButton;
    private MediaPlayer backgroundMusic;
    private TextView finalScoreText;
    private int fallDelay = 1200;
    private Button startGameButton;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catch_game);
        getSupportActionBar().hide();

        String username = getSharedPreferences("PawxelPrefs", MODE_PRIVATE).getString("loggedInUser", null);
        userDao = AppDatabase.getInstance(this).userDao();
        user = userDao.getUserByUsername(username);
        finalScoreText = findViewById(R.id.finalScoreText);

        highScoreText = findViewById(R.id.highScoreText);
        highScoreText.setText("High Score: " + user.catchHighScore);




        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();
        });

        gameLayout = findViewById(R.id.gameFrame);
        scoreText = findViewById(R.id.scoreText);

        petType = getIntent().getStringExtra("petType");
        petColor = getIntent().getStringExtra("petColor");

        setupPet();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        tryAgainButton = findViewById(R.id.tryAgainButton);
        tryAgainButton.setOnClickListener(v -> {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        });


        backgroundMusic = MediaPlayer.create(this, R.raw.background);
        backgroundMusic.setLooping(true);
        backgroundMusic.start();

        gameLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float x = event.getX();
                petView.setX(x - petView.getWidth() / 2f);
            }
            return true;
        });

        startGameButton = findViewById(R.id.startGameButton);
        startGameButton.setOnClickListener(v -> {
            startGameButton.setVisibility(View.GONE);
            fallHandler.post(fallRunnable);
        });
    }

    private void setupPet() {
        petView = new ImageView(this);
        petView.setLayoutParams(new FrameLayout.LayoutParams(150, 150));
        petView.setY(getResources().getDisplayMetrics().heightPixels - 250);
        petView.setImageResource(getPetDrawable());
        gameLayout.addView(petView);
    }

    private int getPetDrawable() {
        if ("dog".equals(petType)) {
            switch (petColor) {
                case "gray": return R.drawable.graydog1;
                case "brown": return R.drawable.browndog1;
                case "white": return R.drawable.whitedog2;
                case "black": return R.drawable.blackdog1;
                case "golden": return R.drawable.yellowdog1;
                case "cream": return R.drawable.creamdog1;
            }
        } else {
            switch (petColor) {
                case "black": return R.drawable.blackcat1;
                case "brown": return R.drawable.browncat1;
                case "cream": return R.drawable.creamcat1;
                case "gray": return R.drawable.graycat1;
                case "white": return R.drawable.whitecat1;
                case "orange": return R.drawable.yellowcat1;
            }
        }
        return R.drawable.blackcat1;
    }

    private final Runnable fallRunnable = new Runnable() {
        @Override
        public void run() {
            spawnFood();

            if (missed >= 5) {
                fallHandler.removeCallbacks(this);
                finalScoreText.setText("Final Score: " + score);
                finalScoreText.setVisibility(View.VISIBLE);
                if (score > user.catchHighScore) {
                    user.catchHighScore = score;
                    AppDatabase.getInstance(CatchGame.this).userDao().insert(user);
                    highScoreText.setText("High Score: " + user.catchHighScore);
                    Toast.makeText(CatchGame.this, "🎉 New Catch Game High Score: " + score, Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(CatchGame.this, "Game Over!", Toast.LENGTH_SHORT).show();
                }

                tryAgainButton.setVisibility(View.VISIBLE);
                gameLayout.setOnTouchListener(null);
                return;
            }

            fallDelay = Math.max(400, fallDelay - 10);
            fallHandler.postDelayed(this, fallDelay);
        }
    };


    private void spawnFood() {
        int index = random.nextInt(foodImages.length);
        ImageView food = new ImageView(this);
        food.setImageResource(foodImages[index]);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100, 100);
        food.setLayoutParams(params);
        food.setX(random.nextInt(screenWidth - 100));
        food.setY(0);
        gameLayout.addView(food);

        Handler moveHandler = new Handler();
        moveHandler.post(new Runnable() {
            float y = 0;

            @Override
            public void run() {
                y += 15;
                food.setY(y);
                if (y > gameLayout.getHeight() - 200) {
                    float foodX = food.getX();
                    float petX = petView.getX();
                    if (Math.abs(foodX - petX) < 100) {
                        score += foodPoints[index];
                        scoreText.setText("Points: " + score);
                        gameLayout.removeView(food);
                    } else {
                        missed++;
                        gameLayout.removeView(food);
                    }
                } else {
                    moveHandler.postDelayed(this, 30);
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.release();
            backgroundMusic = null;
        }
    }

}
