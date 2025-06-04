package com.example.pawxel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pawxel.database.AppDatabase;
import com.example.pawxel.database.User;
import com.example.pawxel.database.UserDao;
import com.example.pawxel.utils.MusicManager;

import java.util.Objects;

public class MainActivity extends BaseActivity {
    private boolean isSignUpMode = false;
    private EditText usernameInput, passwordInput;
    private TextView titleText, accountStatus;
    private Button playButton, toggleButton;
    private String username;
    private boolean shouldNavigateImmediately = false;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        db = AppDatabase.getInstance(this);

        SharedPreferences prefs = getSharedPreferences("PawxelPrefs", MODE_PRIVATE);
        String loggedInUser = prefs.getString("loggedInUser", null);
        if (loggedInUser != null) {
            username = loggedInUser;
            shouldNavigateImmediately = true;
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
        UserDao userDao = db.userDao();

        if (isSignUpMode) {
            User existingUser = userDao.getUserByUsername(inputUser);
            if (existingUser != null) {
                Toast.makeText(this, "Username already taken", Toast.LENGTH_SHORT).show();
                return;
            }
            if (inputUser.equals("admin")) {
                Toast.makeText(this, "Username 'admin' is reserved", Toast.LENGTH_SHORT).show();
                return;
            }


            User newUser = new User();
            newUser.username = inputUser;
            newUser.password = inputPass;
            userDao.insert(newUser);
            Log.d("SignupDebug", "Inserted user: " + newUser.username);

            User check = userDao.getUserByUsername(inputUser);
            if (check != null) {
                Log.d("SignupDebug", "Verified user in DB: " + check.username);
            } else {
                Log.e("SignupDebug", "Insert failed!");
            }

            Toast.makeText(this, "Signup successful! Please log in.", Toast.LENGTH_SHORT).show();
            usernameInput.setText("");
            passwordInput.setText("");
            toggleMode();
        } else {
            if (inputUser.equals("admin") && inputPass.equals("admin123")) {
                // Hardcoded admin login
                startActivity(new Intent(MainActivity.this, AdminDashboardActivity.class));
                finish();
                return;
            }

            User user = userDao.getUserByUsername(inputUser);
            if (user != null && user.password.equals(inputPass)) {
                SharedPreferences.Editor editor = getSharedPreferences("PawxelPrefs", MODE_PRIVATE).edit();
                editor.putString("loggedInUser", inputUser);
                editor.apply();

                MusicManager.pause();
                openGameChoice();
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openGameChoice() {
        UserDao userDao = db.userDao();
        User user = userDao.getUserByUsername(username);

        if (user != null && user.petType != null) {
            startActivity(new Intent(MainActivity.this, SaveGameActivity.class));
        } else {
            Intent intent = new Intent(MainActivity.this, PetSelectionActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        }

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
