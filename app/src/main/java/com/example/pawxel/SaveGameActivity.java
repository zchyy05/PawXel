package com.example.pawxel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;

public class SaveGameActivity extends BaseActivity {

    private TextView savedUsernameText;
    private TextView savedPetNameText;
    private ImageView savedPetImage;
    private Button resumeButton, newGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_game);
        Objects.requireNonNull(getSupportActionBar()).hide();

        savedUsernameText = findViewById(R.id.savedUsernameText);
        savedPetNameText = findViewById(R.id.savedPetNameText);
        savedPetImage = findViewById(R.id.savedPetImage);
        resumeButton = findViewById(R.id.resumeButton);
        newGameButton = findViewById(R.id.newGameButton);

        SharedPreferences prefs = getSharedPreferences("PawxelPrefs", MODE_PRIVATE);
        String username = prefs.getString("loggedInUser", "Player");


        String pet = prefs.getString("pet_" + username, null);
        String color = prefs.getString("petColor_" + username, null);
        String petName = prefs.getString("petName_" + username, "Pet");

        savedUsernameText.setText("Welcome back, " + username + "!");
        savedPetNameText.setText("Your pet: " + petName);

        if (pet != null && color != null) {
            int imageResId = getPetDrawable(pet, color);
            if (imageResId != 0) {
                savedPetImage.setImageResource(imageResId);
            }
        }

        resumeButton.setOnClickListener(v -> {
            prepareForTransition();
            startActivity(new Intent(this, Room.class));
            finish();
        });

        newGameButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("pet_" + username);
            editor.remove("petColor_" + username);
            editor.remove("petName_" + username);
            editor.apply();

            prepareForTransition();
            Intent intent = new Intent(this, PetSelectionActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
        });
    }

    private int getPetDrawable(String petType, String color) {
        switch (petType) {
            case "dog":
                switch (color) {
                    case "gray": return R.drawable.graydog1;
                    case "brown": return R.drawable.browndog1;
                    case "white": return R.drawable.whitedog2;
                    case "black": return R.drawable.blackdog1;
                    case "golden": return R.drawable.yellowdog1;
                    case "cream": return R.drawable.creamdog1;
                }
                break;
            case "cat":
                switch (color) {
                    case "black": return R.drawable.blackcat1;
                    case "brown": return R.drawable.browncat1;
                    case "cream": return R.drawable.creamcat1;
                    case "gray": return R.drawable.graycat1;
                    case "white": return R.drawable.whitecat1;
                    case "orange": return R.drawable.yellowcat1;
                }
                break;
        }
        return 0;
    }

    @Override
    protected boolean shouldPlayMusic() {
        return !isMuted();
    }
}
