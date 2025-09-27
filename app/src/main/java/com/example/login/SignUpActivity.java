package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    private EditText fullNameInput, emailInput, usernameInput, passwordInput, confirmPasswordInput;
    private CheckBox termsCheckbox;
    private RelativeLayout signupButtonContainer;
    private TextView loginTab, signupTab, termsLink, loginLink;
    private ImageView passwordToggle, confirmPasswordToggle;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initializeViews();
        setupClickListeners();
        setupPasswordToggles();  // Add this line!
    }

    private void initializeViews() {
        fullNameInput = findViewById(R.id.username_input);
        emailInput = findViewById(R.id.email_input);
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);

        termsCheckbox = findViewById(R.id.terms_checkbox);
        signupButtonContainer = findViewById(R.id.signup_button_container);

        loginTab = findViewById(R.id.login_tab);
        signupTab = findViewById(R.id.signup_tab);
        termsLink = findViewById(R.id.terms_link);
        loginLink = findViewById(R.id.login_link);

        passwordToggle = findViewById(R.id.password_toggle);
        confirmPasswordToggle = findViewById(R.id.confirm_password_toggle);
    }

    private void setupClickListeners() {
        signupButtonContainer.setOnClickListener(v -> {
            if (validateForm()) {
                performSignUp();
            }
        });

        loginTab.setOnClickListener(v -> navigateToLogin());
        loginLink.setOnClickListener(v -> navigateToLogin());

        // Add Terms & Conditions click listener
        termsLink.setOnClickListener(v -> showTermsAndConditions());

        // Add password toggle click listeners
        passwordToggle.setOnClickListener(v -> togglePasswordVisibility());
        confirmPasswordToggle.setOnClickListener(v -> toggleConfirmPasswordVisibility());
    }

    // Add this method to set initial state of password toggles
    private void setupPasswordToggles() {
        passwordToggle.setImageResource(R.drawable.ic_visibility_off);
        confirmPasswordToggle.setImageResource(R.drawable.ic_visibility_off);
    }

    // Add password toggle methods
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password
            passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordToggle.setImageResource(R.drawable.ic_visibility_off);
            isPasswordVisible = false;
        } else {
            // Show password
            passwordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            passwordToggle.setImageResource(R.drawable.ic_visibility);
            isPasswordVisible = true;
        }
        // Move cursor to end of text
        passwordInput.setSelection(passwordInput.getText().length());
    }

    private void toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            // Hide password
            confirmPasswordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
            confirmPasswordToggle.setImageResource(R.drawable.ic_visibility_off);
            isConfirmPasswordVisible = false;
        } else {
            // Show password
            confirmPasswordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            confirmPasswordToggle.setImageResource(R.drawable.ic_visibility);
            isConfirmPasswordVisible = true;
        }
        // Move cursor to end of text
        confirmPasswordInput.setSelection(confirmPasswordInput.getText().length());
    }

    private boolean validateForm() {
        if (fullNameInput.getText().toString().trim().isEmpty()) {
            fullNameInput.setError("Full name is required");
            return false;
        }
        if (emailInput.getText().toString().trim().isEmpty()) {
            emailInput.setError("Email is required");
            return false;
        }
        if (usernameInput.getText().toString().trim().isEmpty()) {
            usernameInput.setError("Username is required");
            return false;
        }
        if (passwordInput.getText().toString().isEmpty()) {
            passwordInput.setError("Password is required");
            return false;
        }
        if (!passwordInput.getText().toString().equals(confirmPasswordInput.getText().toString())) {
            confirmPasswordInput.setError("Passwords do not match");
            return false;
        }
        if (!termsCheckbox.isChecked()) {
            Toast.makeText(this, "Please accept Terms & Conditions", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void performSignUp() {
        signupButtonContainer.setEnabled(false);
        Toast.makeText(this, "Creating account...", Toast.LENGTH_SHORT).show();

        String fullName = fullNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        // Simple validation
        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            signupButtonContainer.setEnabled(true);
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            signupButtonContainer.setEnabled(true);
            return;
        }

        new Thread(() -> {
            try {
                Thread.sleep(1500); // Simulate network delay

                runOnUiThread(() -> {
                    // Save username + password into SharedPreferences
                    getSharedPreferences("user_prefs", MODE_PRIVATE)
                            .edit()
                            .putString("registered_username", username)
                            .putString("registered_password", password)
                            .apply();

                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();

                    // Send username back to LoginActivity so it shows in the field
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.putExtra("registered_username", username);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Sign up failed. Please try again.", Toast.LENGTH_SHORT).show();
                    signupButtonContainer.setEnabled(true);
                });
            }
        }).start();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showTermsAndConditions() {
        Intent intent = new Intent(this, TermsAndConditionsActivity.class);
        startActivity(intent);
    }
}