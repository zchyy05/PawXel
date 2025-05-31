package com.example.pawxel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pawxel.utils.MusicManager;

import java.util.Objects;

public class PetSelectionActivity extends BaseActivity {

    private ImageView dogPet, catPet;
    private Button nextButton;
    private String selectedPet = null;
    private String username;
    private TextView displayName;
    private MediaPlayer petSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pet_selection_activity);
        Objects.requireNonNull(getSupportActionBar()).hide();

        dogPet = findViewById(R.id.petDog);
        catPet = findViewById(R.id.petCat);
        nextButton = findViewById(R.id.nextButton);
        displayName = findViewById(R.id.displayName);

        username = getIntent().getStringExtra("username");
        if (username == null) {
            Toast.makeText(this, "No user found. Please log in again.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }

        displayName.setText("Choose your pet, " + username);
        nextButton.setEnabled(false);

        dogPet.setOnClickListener(v -> {
            playPetSound("dog");
            selectPet("dog", dogPet);
        });

        catPet.setOnClickListener(v -> {
            playPetSound("cat");
            selectPet("cat", catPet);
        });

        nextButton.setOnClickListener(v -> {
            if (selectedPet == null) {
                Toast.makeText(this, "Please select a pet before continuing.", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("PawxelPrefs", MODE_PRIVATE);
            prefs.edit()
                    .putString("pet_" + username, selectedPet)
                    .putString("pet", selectedPet)
                    .apply();


            prepareForTransition();

            Intent intent = new Intent(this, selectedPet.equals("dog") ? DogColorActivity.class : CatColorActivity.class);
            startActivity(intent);
            finish();
        });
    }


    private void selectPet(String petName, ImageView petImage) {
        dogPet.setBackgroundResource(0);
        catPet.setBackgroundResource(0);

        selectedPet = petName;
        petImage.setBackgroundResource(R.drawable.image_frame);

        Animation jump = AnimationUtils.loadAnimation(this, R.anim.jump);
        petImage.startAnimation(jump);

        nextButton.setEnabled(true);
    }

    private void playPetSound(String petType) {
        if (petSound != null) {
            petSound.release();
        }

        if ("dog".equals(petType)) {
            petSound = MediaPlayer.create(this, R.raw.bark);
        } else if ("cat".equals(petType)) {
            petSound = MediaPlayer.create(this, R.raw.cat);
        }

        if (petSound != null && !isMuted()) {
            petSound.start();
        }
    }

    @Override
    protected void onDestroy() {
        if (petSound != null) {
            petSound.release();
            petSound = null;
        }
        super.onDestroy();
    }
}