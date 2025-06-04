package com.example.pawxel;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.pawxel.database.AppDatabase;
import com.example.pawxel.database.User;
import com.example.pawxel.database.UserDao;

import java.util.Objects;

public class DogColorActivity extends BaseActivity {

    private ImageView dogPreview;
    private EditText petNameInput;
    private String selectedColor = "white";
    private Animation jumpAnimation;
    private MediaPlayer clickSound;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_color);
        Objects.requireNonNull(getSupportActionBar()).hide();

        username = getSharedPreferences("PawxelPrefs", MODE_PRIVATE)
                .getString("loggedInUser", null);

        jumpAnimation = AnimationUtils.loadAnimation(this, R.anim.jump);
        clickSound = MediaPlayer.create(this, R.raw.button);
        dogPreview = findViewById(R.id.dogPreview);
        petNameInput = findViewById(R.id.petNameInput);

        Button colorGray = findViewById(R.id.colorGray);
        Button colorBrown = findViewById(R.id.colorBrown);
        Button colorWhite = findViewById(R.id.colorWhite);
        Button colorBlack = findViewById(R.id.colorBlack);
        Button colorGolden = findViewById(R.id.colorGolden);
        Button colorCream = findViewById(R.id.colorCream);
        Button nextButton = findViewById(R.id.nextButton);
        Button backButton = findViewById(R.id.backButton);

        colorGray.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#808080")));
        colorBrown.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#8B4513")));
        colorWhite.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
        colorBlack.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2C2C2C")));
        colorGolden.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DAA520")));
        colorCream.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F5DEB3")));

        colorGray.setOnClickListener(v -> updateDogPreview("gray"));
        colorBrown.setOnClickListener(v -> updateDogPreview("brown"));
        colorWhite.setOnClickListener(v -> updateDogPreview("white"));
        colorBlack.setOnClickListener(v -> updateDogPreview("black"));
        colorGolden.setOnClickListener(v -> updateDogPreview("golden"));
        colorCream.setOnClickListener(v -> updateDogPreview("cream"));

        nextButton.setOnClickListener(v -> {
            String name = petNameInput.getText().toString().trim();
            if (name.isEmpty()) {
                petNameInput.setError("Please enter a name for your dog");
                return;
            }

            UserDao userDao = AppDatabase.getInstance(this).userDao();
            User user = userDao.getUserByUsername(username);
            user.petColor = selectedColor;
            user.petName = name;
            userDao.insert(user);

            startActivity(new Intent(DogColorActivity.this, Welcome.class));
            finish();
        });

        backButton.setOnClickListener(v -> {
            Intent backIntent = new Intent(DogColorActivity.this, PetSelectionActivity.class);
            backIntent.putExtra("username", username);
            startActivity(backIntent);
            finish();
        });

    }

    private void updateDogPreview(String colorName) {
        if (clickSound != null) clickSound.start();

        switch (colorName) {
            case "gray": dogPreview.setImageResource(R.drawable.graydog1); break;
            case "brown": dogPreview.setImageResource(R.drawable.browndog1); break;
            case "white": dogPreview.setImageResource(R.drawable.whitedog2); break;
            case "black": dogPreview.setImageResource(R.drawable.blackdog1); break;
            case "golden": dogPreview.setImageResource(R.drawable.yellowdog1); break;
            case "cream": dogPreview.setImageResource(R.drawable.creamdog1); break;
        }

        selectedColor = colorName;
        dogPreview.startAnimation(jumpAnimation);
    }

    @Override
    protected void onDestroy() {
        if (clickSound != null) {
            clickSound.release();
            clickSound = null;
        }
        super.onDestroy();
    }
}
