package com.example.pawxel.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
@Entity

public class User {
    @PrimaryKey
    @NonNull
    public String username;

    public String password;


    public String petType;
    public String petColor;
    public String petName;
    public int health = 50;
    public int hunger = 50;
    public int thirst = 75;
    public int energy = 80;
    public int play = 60;

    public int fedCount = 0;
    public int pettingCount = 0;
    public int showeredCount = 0;

    public int highScore;
    public int catchHighScore = 0;
}
