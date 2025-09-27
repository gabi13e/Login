package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput;
    private CheckBox rememberCheckbox;
    private RelativeLayout loginButtonContainer;
    private TextView loginTab, signinTab, forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        setupClickListeners();
        checkForRegistrationData();
    }

    private void initializeViews() {
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        rememberCheckbox = findViewById(R.id.remember_checkbox);

        loginButtonContainer = findViewById(R.id.login_button_container);
        loginTab = findViewById(R.id.login_tab);
        signinTab = findViewById(R.id.signin_tab);
        forgotPassword = findViewById(R.id.forgot_password);
    }

    private void setupClickListeners() {
        loginButtonContainer.setOnClickListener(v -> {
            if (validateForm()) {
                performLogin();
            }
        });

        signinTab.setOnClickListener(v -> navigateToSignUp());
        forgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Forgot password coming soon!", Toast.LENGTH_SHORT).show()
        );
    }

    private void checkForRegistrationData() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("registered_username")) {
            String username = intent.getStringExtra("registered_username");
            if (username != null) {
                usernameInput.setText(username);
                Toast.makeText(this, "Account created successfully! Please login.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        String username = usernameInput.getText().toString().trim();
        if (username.isEmpty()) {
            usernameInput.setError("Username is required");
            isValid = false;
        }

        String password = passwordInput.getText().toString();
        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            isValid = false;
        }

        return isValid;
    }

    private void performLogin() {
        loginButtonContainer.setEnabled(false);
        Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show();

        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        new Thread(() -> {
            try {
                Thread.sleep(1000);

                runOnUiThread(() -> {
                    if (authenticateUser(username, password)) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();


                    } else {
                        Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                        loginButtonContainer.setEnabled(true);
                        passwordInput.setText("");
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                    loginButtonContainer.setEnabled(true);
                });
            }
        }).start();
    }

    private boolean authenticateUser(String username, String password) {
        // ✅ Read saved credentials
        String storedUsername = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("registered_username", null);
        String storedPassword = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("registered_password", null);

        return storedUsername != null && storedPassword != null &&
                storedUsername.equals(username) && storedPassword.equals(password);
    }

    private void navigateToSignUp() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }
}
