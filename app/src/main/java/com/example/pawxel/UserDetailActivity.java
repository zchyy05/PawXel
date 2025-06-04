package com.example.pawxel;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pawxel.database.AppDatabase;
import com.example.pawxel.database.User;
import com.example.pawxel.database.UserDao;

import java.util.Objects;

public class UserDetailActivity extends BaseActivity {

    private EditText usernameField, passwordField, petNameField, petTypeField, petColorField;
    private EditText healthField, hungerField, thirstField, energyField, playField;
    private EditText fedCountField, pettingCountField, showeredCountField, highScoreField, catchHighScoreField;
    private Button saveButton;

    private boolean isNewUser;
    private String username;
    private UserDao userDao;
    private User user;
    private boolean isEditMode;
    private ImageView petAvatarPreview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        Objects.requireNonNull(getSupportActionBar()).hide();



        userDao = AppDatabase.getInstance(this).userDao();

        petAvatarPreview = findViewById(R.id.petAvatarPreview);
        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        petNameField = findViewById(R.id.petNameField);
        petTypeField = findViewById(R.id.petTypeField);
        petColorField = findViewById(R.id.petColorField);

        healthField = findViewById(R.id.healthField);
        hungerField = findViewById(R.id.hungerField);
        thirstField = findViewById(R.id.thirstField);
        energyField = findViewById(R.id.energyField);
        playField = findViewById(R.id.playField);

        fedCountField = findViewById(R.id.fedCountField);
        pettingCountField = findViewById(R.id.pettingCountField);
        showeredCountField = findViewById(R.id.showeredCountField);
        highScoreField = findViewById(R.id.highScoreField);
        catchHighScoreField = findViewById(R.id.catchHighScoreField);
        Button cancelButton = findViewById(R.id.cancelButton);
        saveButton = findViewById(R.id.saveUserButton);

        isNewUser = getIntent().getBooleanExtra("isNewUser", false);
        isEditMode = getIntent().getBooleanExtra("isEditMode", false);
        username = getIntent().getStringExtra("username");


        if (!isNewUser) {
            if (username == null || username.isEmpty()) {
                Toast.makeText(this, "Username not provided", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            user = userDao.getUserByUsername(username);

            if (user == null) {
                Toast.makeText(this, "User not found in database", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            populateFields(user);
            usernameField.setEnabled(false);

            if (!isEditMode) {
                setEditable(false);
            } else {
                setEditable(true);
            }
        }
        TextView formTitle = findViewById(R.id.formTitle);

        if (isNewUser) {
            setEditable(true);
            formTitle.setText("Create New User");
        } else if (isEditMode) {
            formTitle.setText("Edit User");
        } else {
            formTitle.setText("User Details");
        }
        if (isNewUser) {
            setEditable(true);
            formTitle.setText("Create New User");
            saveButton.setVisibility(Button.VISIBLE);
            cancelButton.setText("Cancel");
        } else if (isEditMode) {
            setEditable(true);
            formTitle.setText("Edit User");
            saveButton.setVisibility(Button.VISIBLE);
            cancelButton.setText("Cancel");
        } else {
            setEditable(false);
            formTitle.setText("User Details");
            saveButton.setVisibility(Button.GONE);
            cancelButton.setText("Back");
        }


        saveButton.setOnClickListener(v -> saveUser());
        petTypeField.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                updatePetAvatar(petTypeField.getText().toString().trim(), petColorField.getText().toString().trim());
            }
        });

        petColorField.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                updatePetAvatar(petTypeField.getText().toString().trim(), petColorField.getText().toString().trim());
            }
        });


        cancelButton.setOnClickListener(v -> {
            finish();
        });

    }


    private void populateFields(User u) {
        usernameField.setText(u.username);
        passwordField.setText(u.password);
        petNameField.setText(u.petName);
        petTypeField.setText(u.petType);
        petColorField.setText(u.petColor);
        updatePetAvatar(u.petType, u.petColor);
        healthField.setText(String.valueOf(u.health));
        hungerField.setText(String.valueOf(u.hunger));
        thirstField.setText(String.valueOf(u.thirst));
        energyField.setText(String.valueOf(u.energy));
        playField.setText(String.valueOf(u.play));

        fedCountField.setText(String.valueOf(u.fedCount));
        pettingCountField.setText(String.valueOf(u.pettingCount));
        showeredCountField.setText(String.valueOf(u.showeredCount));
        highScoreField.setText(String.valueOf(u.highScore));
        catchHighScoreField.setText(String.valueOf(u.catchHighScore));

    }

    private void saveUser() {
        String uname = usernameField.getText().toString().trim();
        String pass = passwordField.getText().toString().trim();

        if (TextUtils.isEmpty(uname) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Username and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (user == null) user = new User();

        user.username = uname;
        user.password = pass;
        user.petName = petNameField.getText().toString().trim();
        user.petType = petTypeField.getText().toString().trim();
        user.petColor = petColorField.getText().toString().trim();

        user.health = parseIntSafe(healthField.getText().toString(), 50);
        user.hunger = parseIntSafe(hungerField.getText().toString(), 50);
        user.thirst = parseIntSafe(thirstField.getText().toString(), 75);
        user.energy = parseIntSafe(energyField.getText().toString(), 80);
        user.play = parseIntSafe(playField.getText().toString(), 60);

        user.fedCount = parseIntSafe(fedCountField.getText().toString(), 0);
        user.pettingCount = parseIntSafe(pettingCountField.getText().toString(), 0);
        user.showeredCount = parseIntSafe(showeredCountField.getText().toString(), 0);
        user.highScore = parseIntSafe(highScoreField.getText().toString(), 0);
        user.catchHighScore = parseIntSafe(catchHighScoreField.getText().toString(), 0);

        userDao.insert(user);
        Toast.makeText(this, "User saved", Toast.LENGTH_SHORT).show();
        finish();
    }
    private void updatePetAvatar(String petType, String petColor) {
        if (petAvatarPreview == null) return;

        if (petType == null || petColor == null) {
            petAvatarPreview.setImageResource(android.R.drawable.ic_menu_help);
            return;
        }

        switch (petType.toLowerCase()) {
            case "dog":
                switch (petColor.toLowerCase()) {
                    case "gray":
                        petAvatarPreview.setImageResource(R.drawable.graydog1);
                        break;
                    case "brown":
                        petAvatarPreview.setImageResource(R.drawable.browndog1);
                        break;
                    case "white":
                        petAvatarPreview.setImageResource(R.drawable.whitedog2);
                        break;
                    case "black":
                        petAvatarPreview.setImageResource(R.drawable.blackdog1);
                        break;
                    case "golden":
                        petAvatarPreview.setImageResource(R.drawable.yellowdog1);
                        break;
                    case "cream":
                        petAvatarPreview.setImageResource(R.drawable.creamdog1);
                        break;
                    default:
                        petAvatarPreview.setImageResource(R.drawable.whitedog2);
                }
                break;
            case "cat":
                switch (petColor.toLowerCase()) {
                    case "white":
                        petAvatarPreview.setImageResource(R.drawable.whitecat1);
                        break;
                    case "black":
                        petAvatarPreview.setImageResource(R.drawable.blackcat1);
                        break;
                    case "orange":
                        petAvatarPreview.setImageResource(R.drawable.yellowcat1);
                        break;
                    case "cream":
                        petAvatarPreview.setImageResource(R.drawable.creamcat1);
                        break;
                    case "gray":
                        petAvatarPreview.setImageResource(R.drawable.graycat1);
                        break;
                    case "brown":
                        petAvatarPreview.setImageResource(R.drawable.browncat1);
                        break;
                    default:
                        petAvatarPreview.setImageResource(R.drawable.whitecat1);
                }
                break;
            default:
                petAvatarPreview.setImageResource(android.R.drawable.ic_menu_help);
                break;
        }
    }


    private int parseIntSafe(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private void setEditable(boolean editable) {
        passwordField.setEnabled(editable);
        petNameField.setEnabled(editable);
        petTypeField.setEnabled(editable);
        petColorField.setEnabled(editable);
        healthField.setEnabled(editable);
        hungerField.setEnabled(editable);
        thirstField.setEnabled(editable);
        energyField.setEnabled(editable);
        playField.setEnabled(editable);
        fedCountField.setEnabled(editable);
        pettingCountField.setEnabled(editable);
        showeredCountField.setEnabled(editable);
        highScoreField.setEnabled(editable);
        catchHighScoreField.setEnabled(editable);
        saveButton.setEnabled(editable);
    }


    @Override
    protected boolean allowBackgroundMusic() {
        return false;
    }
}
