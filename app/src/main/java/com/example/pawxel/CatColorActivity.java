package com.example.pawxel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.Objects;

public class CatColorActivity extends BaseActivity {

    private ImageView catPreview;
    private EditText petNameInput;
    private String selectedColor = "white";
    private Animation jumpAnimation;
    private MediaPlayer clickSound;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cat_color);
        Objects.requireNonNull(getSupportActionBar()).hide();

        SharedPreferences prefs = getSharedPreferences("PawxelPrefs", MODE_PRIVATE);
        username = prefs.getString("loggedInUser", null);

        jumpAnimation = AnimationUtils.loadAnimation(this, R.anim.jump);
        clickSound = MediaPlayer.create(this, R.raw.button);
        catPreview = findViewById(R.id.catPreview);
        petNameInput = findViewById(R.id.petNameInput);

        Button colorBlack = findViewById(R.id.colorBlack);
        Button colorBrown = findViewById(R.id.colorBrown);
        Button colorCream = findViewById(R.id.colorCream);
        Button colorGray = findViewById(R.id.colorGray);
        Button colorWhite = findViewById(R.id.colorWhite);
        Button colorOrange = findViewById(R.id.colorOrange);
        Button nextButton = findViewById(R.id.nextButton);
        Button backButton = findViewById(R.id.backButton);

        colorBlack.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2C2C2C")));
        colorBrown.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#8B4513")));
        colorCream.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F5DEB3")));
        colorGray.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#808080")));
        colorWhite.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
        colorOrange.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF8C00")));

        colorBlack.setOnClickListener(v -> updateCatPreview("black"));
        colorBrown.setOnClickListener(v -> updateCatPreview("brown"));
        colorCream.setOnClickListener(v -> updateCatPreview("cream"));
        colorGray.setOnClickListener(v -> updateCatPreview("gray"));
        colorWhite.setOnClickListener(v -> updateCatPreview("white"));
        colorOrange.setOnClickListener(v -> updateCatPreview("orange"));

        nextButton.setOnClickListener(v -> {
            String name = petNameInput.getText().toString().trim();
            if (name.isEmpty()) {
                petNameInput.setError("Please enter a name for your cat");
                return;
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("petName_" + username, name);
            editor.putString("petColor_" + username, selectedColor);
            editor.apply();

            startActivity(new Intent(CatColorActivity.this, Welcome.class));
            finish();
        });

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(CatColorActivity.this, PetSelectionActivity.class));
            finish();
        });
    }

    private void updateCatPreview(String colorName) {
        if (clickSound != null) clickSound.start();

        switch (colorName) {
            case "black": catPreview.setImageResource(R.drawable.blackcat1); break;
            case "brown": catPreview.setImageResource(R.drawable.browncat1); break;
            case "cream": catPreview.setImageResource(R.drawable.creamcat1); break;
            case "gray": catPreview.setImageResource(R.drawable.graycat1); break;
            case "white": catPreview.setImageResource(R.drawable.whitecat1); break;
            case "orange": catPreview.setImageResource(R.drawable.yellowcat1); break;
        }

        selectedColor = colorName;
        catPreview.startAnimation(jumpAnimation);
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
