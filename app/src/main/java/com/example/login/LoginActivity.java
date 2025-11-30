package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText schoolIdInput, passwordInput;
    private CheckBox rememberCheckbox;
    private RelativeLayout loginButtonContainer;
    // --- CHANGE 1: Removed signupLink from the variable declarations ---
    private TextView loginTab, signupTab, forgotPassword;
    private ImageView passwordToggle;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        setupClickListeners();
        checkForRegistrationData();
    }

    private void initializeViews() {
        schoolIdInput = findViewById(R.id.school_id_input);
        passwordInput = findViewById(R.id.password_input);
        rememberCheckbox = findViewById(R.id.remember_checkbox);
        loginButtonContainer = findViewById(R.id.login_button_container);
        loginTab = findViewById(R.id.login_tab);
        signupTab = findViewById(R.id.signup_tab);
        forgotPassword = findViewById(R.id.forgot_password);
        passwordToggle = findViewById(R.id.password_toggle);
        // --- CHANGE 2: The line initializing signupLink is no longer needed ---
    }

    private void setupClickListeners() {
        loginButtonContainer.setOnClickListener(v -> {
            Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
            v.startAnimation(bounce);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (validateForm()) {
                    performLogin();
                }
            }, 250);
        });

        signupTab.setOnClickListener(v -> {
            Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
            v.startAnimation(bounce);
            new Handler(Looper.getMainLooper()).postDelayed(this::navigateToSignUp, 250);
        });

        // --- CHANGE 3: Removed the click listener for the non-existent signupLink ---

        forgotPassword.setOnClickListener(v -> {
            Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
            v.startAnimation(bounce);
            new Handler(Looper.getMainLooper()).postDelayed(() ->
                            Toast.makeText(this, "Forgot password coming soon!", Toast.LENGTH_SHORT).show()
                    , 250);
        });

        passwordToggle.setOnClickListener(v -> togglePasswordVisibility());
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out_rotate);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isPasswordVisible) {
                    passwordToggle.setImageResource(R.drawable.ic_visibility);
                    passwordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    passwordToggle.setImageResource(R.drawable.ic_visibility_off);
                    passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                Animation fadeIn = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade_in_rotate);
                passwordToggle.startAnimation(fadeIn);
                passwordInput.setSelection(passwordInput.getText().length());
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        passwordToggle.startAnimation(fadeOut);
    }

    // ... (The rest of your methods for login logic remain the same) ...

    private void checkForRegistrationData() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("registered_school_id")) {
            String schoolId = intent.getStringExtra("registered_school_id");
            if (schoolId != null) {
                schoolIdInput.setText(schoolId);
                Toast.makeText(this, "Account created successfully! Please login.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean validateForm() {
        boolean isValid = true;
        String schoolId = schoolIdInput.getText().toString().trim();
        if (schoolId.isEmpty()) {
            schoolIdInput.setError("School ID is required");
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

        String schoolId = schoolIdInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                runOnUiThread(() -> {
                    if (authenticateUser(schoolId, password)) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        // TODO: Navigate to the main activity/dashboard after successful login
                    } else {
                        Toast.makeText(this, "Invalid School ID or password", Toast.LENGTH_SHORT).show();
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

    private boolean authenticateUser(String schoolId, String password) {
        String storedSchoolId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("registered_school_id", null);
        String storedPassword = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("registered_password", null);

        return storedSchoolId != null && storedPassword != null &&
                storedSchoolId.equals(schoolId) && storedPassword.equals(password);
    }

    private void navigateToSignUp() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }
}
