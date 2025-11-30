package com.example.login;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class SignUpActivity extends AppCompatActivity {

    private EditText fullNameInput, emailInput, schoolIdInput, passwordInput, confirmPasswordInput;
    private CheckBox termsCheckbox;
    private RelativeLayout signupButtonContainer;
    // --- CHANGE 1: Removed loginLink from variable declarations ---
    private TextView loginTab, signupTab, termsLink;
    private ImageView passwordToggle, confirmPasswordToggle;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initializeViews();
        setupClickListeners();
        // The setupPasswordToggles method is no longer needed if using animations that set the initial state
    }

    private void initializeViews() {
        fullNameInput = findViewById(R.id.fullname_input);
        emailInput = findViewById(R.id.email_input);
        schoolIdInput = findViewById(R.id.schoolid_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);

        termsCheckbox = findViewById(R.id.terms_checkbox);
        signupButtonContainer = findViewById(R.id.signup_button_container);

        loginTab = findViewById(R.id.login_tab);
        signupTab = findViewById(R.id.signup_tab);
        termsLink = findViewById(R.id.terms_link);

        // --- CHANGE 2: No longer need to initialize loginLink ---

        passwordToggle = findViewById(R.id.password_toggle);
        confirmPasswordToggle = findViewById(R.id.confirm_password_toggle);
    }

    private void setupClickListeners() {
        // Sign up button with animation
        signupButtonContainer.setOnClickListener(v -> {
            Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
            v.startAnimation(bounce);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (validateForm()) {
                    performSignUp();
                }
            }, 250);
        });

        // Login tab with animation
        loginTab.setOnClickListener(v -> {
            Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
            v.startAnimation(bounce);
            new Handler(Looper.getMainLooper()).postDelayed(this::navigateToLogin, 250);
        });

        // --- CHANGE 3: Removed the click listener for the non-existent loginLink ---

        // Terms link with animation
        termsLink.setOnClickListener(v -> {
            Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
            v.startAnimation(bounce);
            new Handler(Looper.getMainLooper()).postDelayed(this::showTermsAndConditions, 250);
        });

        passwordToggle.setOnClickListener(v -> togglePasswordVisibility());
        confirmPasswordToggle.setOnClickListener(v -> toggleConfirmPasswordVisibility());
    }

    // Using animated toggles
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        animatePasswordToggle(passwordToggle, passwordInput, isPasswordVisible);
    }

    private void toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        animatePasswordToggle(confirmPasswordToggle, confirmPasswordInput, isConfirmPasswordVisible);
    }

    private void animatePasswordToggle(final ImageView toggleIcon, final EditText editText, final boolean isVisible) {
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out_rotate);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isVisible) {
                    toggleIcon.setImageResource(R.drawable.ic_visibility);
                    editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    toggleIcon.setImageResource(R.drawable.ic_visibility_off);
                    editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                Animation fadeIn = AnimationUtils.loadAnimation(SignUpActivity.this, R.anim.fade_in_rotate);
                toggleIcon.startAnimation(fadeIn);
                editText.setSelection(editText.getText().length());
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        toggleIcon.startAnimation(fadeOut);
    }

    // ... (Your validateForm, performSignUp, and other methods remain the same)

    private boolean validateForm() {
        if (fullNameInput.getText().toString().trim().isEmpty()) {
            fullNameInput.setError("Full name is required");
            return false;
        }
        if (emailInput.getText().toString().trim().isEmpty()) {
            emailInput.setError("Email is required");
            return false;
        }
        if (schoolIdInput.getText().toString().trim().isEmpty()) {
            schoolIdInput.setError("School ID is required");
            return false;
        }
        if (passwordInput.getText().toString().isEmpty()) {
            passwordInput.setError("Password is required");
            return false;
        }
        if (confirmPasswordInput.getText().toString().isEmpty()) {
            confirmPasswordInput.setError("Please confirm your password");
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

        String schoolId = schoolIdInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        new Thread(() -> {
            try {
                Thread.sleep(1500); // Simulate network delay

                runOnUiThread(() -> {
                    SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
                    editor.putString("registered_school_id", schoolId);
                    editor.putString("registered_password", password);
                    editor.apply();

                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.putExtra("registered_school_id", schoolId);
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
        // Assuming you have a TermsAndConditionsActivity
        Intent intent = new Intent(this, TermsAndConditionsActivity.class);
        startActivity(intent);
    }
}
