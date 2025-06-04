package com.example.pawxel;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pawxel.database.AppDatabase;
import com.example.pawxel.database.User;
import com.example.pawxel.database.UserDao;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AdminDashboardActivity extends BaseActivity implements UserAdapter.OnUserClickListener {

    private RecyclerView userRecyclerView;
    private FloatingActionButton addUserButton;
    private UserAdapter userAdapter;
    private List<User> userList;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        Objects.requireNonNull(getSupportActionBar()).hide();

        userRecyclerView = findViewById(R.id.userRecyclerView);
        addUserButton = findViewById(R.id.addUserButton);

        userDao = AppDatabase.getInstance(this).userDao();
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadUsers();

        addUserButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserDetailActivity.class);
            intent.putExtra("isNewUser", true);
            startActivity(intent);
        });
        TextView logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            getSharedPreferences("PawxelPrefs", MODE_PRIVATE)
                    .edit()
                    .remove("loggedInUser")
                    .apply();

            Intent intent = new Intent(this, MainActivity.class); // Or LoginActivity
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        EditText searchEditText = findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


    }

    private void loadUsers() {
        userList = userDao.getAllUsers();
        userAdapter = new UserAdapter(userList, this);
        userRecyclerView.setAdapter(userAdapter);
    }

    @Override
    public void onUserClick(int position) {
        User user = userList.get(position);
        Intent intent = new Intent(this, UserDetailActivity.class);
        intent.putExtra("username", user.username);
        intent.putExtra("isNewUser", false);
        intent.putExtra("isEditMode", false);
        startActivity(intent);
    }

    @Override
    public void onEditClick(int position) {
        User user = userList.get(position);
        Intent intent = new Intent(this, UserDetailActivity.class);
        intent.putExtra("username", user.username);
        intent.putExtra("isNewUser", false);
        intent.putExtra("isEditMode", true);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(int position) {
        User user = userList.get(position);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete user \"" + user.username + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    userDao.delete(user);
                    Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show();
                    loadUsers();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    private void filterUsers(String query) {
        List<User> filteredList = userList.stream()
                .filter(user -> user.username.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        userAdapter.updateList(filteredList);

    }


    @Override
    protected boolean allowBackgroundMusic() {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }
}
