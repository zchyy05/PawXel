package com.example.pawxel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pawxel.utils.MusicManager;

import java.util.Objects;

public class MainActivity extends BaseActivity {
    private boolean isSignUpMode = false;
    private EditText usernameInput, passwordInput;
    private TextView titleText, accountStatus;
    private Button playButton, toggleButton;
    private String username;
    private boolean shouldNavigateImmediately = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        // Persistent login check
        SharedPreferences prefs = getSharedPreferences("PawxelPrefs", MODE_PRIVATE);
        String loggedInUser = prefs.getString("loggedInUser", null);
        if (loggedInUser != null) {
            username = loggedInUser;
            shouldNavigateImmediately = true;
            // Don't start music here - let the destination activity handle it
            openGameChoice();
            return;
        }

        titleText = findViewById(R.id.titleText);
        accountStatus = findViewById(R.id.accountStatus);
        playButton = findViewById(R.id.playButton);
        toggleButton = findViewById(R.id.signupButton);
        usernameInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);

        toggleButton.setOnClickListener(v -> toggleMode());
        playButton.setOnClickListener(v -> handleAuth());

        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        findViewById(R.id.pet3).startAnimation(shake);
        findViewById(R.id.pet4).startAnimation(shake);
    }

    @Override
    protected boolean shouldPlayMusic() {
        // Don't play music if we're navigating immediately
        return !shouldNavigateImmediately && !isMuted();
    }

    private void toggleMode() {
        isSignUpMode = !isSignUpMode;
        if (isSignUpMode) {
            titleText.setText("Sign up for Pawxel");
            playButton.setText("Sign Up");
            toggleButton.setText("Back to Login");
            accountStatus.setText("Already have an account?");
        } else {
            titleText.setText("Login to Pawxel");
            playButton.setText("Login");
            toggleButton.setText("Sign up here");
            accountStatus.setText("No account?");
        }
    }

    private void handleAuth() {
        String inputUser = usernameInput.getText().toString().trim();
        String inputPass = passwordInput.getText().toString().trim();

        if (inputUser.isEmpty() || inputPass.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        username = inputUser;
        SharedPreferences prefs = getSharedPreferences("PawxelPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (isSignUpMode) {
            String existingPass = prefs.getString("user_" + inputUser, null);
            if (existingPass != null) {
                Toast.makeText(this, "Username already taken", Toast.LENGTH_SHORT).show();
                return;
            }
            editor.putString("user_" + inputUser, inputPass);
            editor.apply();
            Toast.makeText(this, "Signup successful! Please log in.", Toast.LENGTH_SHORT).show();
            usernameInput.setText("");
            passwordInput.setText("");
            toggleMode();
        } else {
            String savedPass = prefs.getString("user_" + inputUser, null);
            if (savedPass != null && savedPass.equals(inputPass)) {
                editor.putString("loggedInUser", inputUser);

                editor.apply();

                // Stop music before navigating
                MusicManager.pause();
                openGameChoice();
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openGameChoice() {
        SharedPreferences prefs = getSharedPreferences("PawxelPrefs", MODE_PRIVATE);
        Intent intent;

        if (prefs.contains("pet_" + username)) {
            intent = new Intent(MainActivity.this, SaveGameActivity.class);
        } else {
            intent = new Intent(MainActivity.this, PetSelectionActivity.class);
            intent.putExtra("username", username);
        }

        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            MusicManager.stop();
        }
    }
}