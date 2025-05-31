package com.example.pawxel;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.Objects;

public class Welcome extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        Objects.requireNonNull(getSupportActionBar()).hide();

        TextView welcome = findViewById(R.id.welcomeText);
        welcome.setText("Game Started!");


        Button howToPlayButton = findViewById(R.id.howToPlayButton);
        howToPlayButton.setOnClickListener(v -> {
            Intent intent = new Intent(Welcome.this, HowToPlay.class);
            startActivity(intent);
        });
    }
}