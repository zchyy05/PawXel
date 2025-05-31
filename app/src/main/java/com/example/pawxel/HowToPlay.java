package com.example.pawxel;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import java.util.Objects;

public class HowToPlay extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.how_to_play);
        Objects.requireNonNull(getSupportActionBar()).hide();

        Button backToStoryButton = findViewById(R.id.backToStoryButton);
        Button goToRoomButton = findViewById(R.id.goToRoomButton);

        backToStoryButton.setOnClickListener(v -> {
            // Go back to the Welcome screen
            Intent intent = new Intent(HowToPlay.this, Welcome.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Optional: Clears any activities on top
            startActivity(intent);
            finish();
        });

        goToRoomButton.setOnClickListener(v -> {
            // Navigate to the Room screen (create this next)
            Intent intent = new Intent(HowToPlay.this, Room.class);
            startActivity(intent);
        });
    }
}
