package com.example.pawxel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.pawxel.database.AppDatabase;
import com.example.pawxel.database.User;

import java.util.Objects;

public class Welcome extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        Objects.requireNonNull(getSupportActionBar()).hide();

        TextView welcome = findViewById(R.id.welcomeText);

        SharedPreferences prefs = getSharedPreferences("PawxelPrefs", MODE_PRIVATE);
        String username = prefs.getString("loggedInUser", null);

        if (username != null) {
            User user = AppDatabase.getInstance(this).userDao().getUserByUsername(username);
            welcome.setText("Welcome back, " + user.petName + "! Let's start the game.");
        } else {
            welcome.setText("Game Started!");
        }

        Button howToPlayButton = findViewById(R.id.howToPlayButton);
        howToPlayButton.setOnClickListener(v -> {
            Intent intent = new Intent(Welcome.this, HowToPlay.class);
            startActivity(intent);
        });
    }
}
