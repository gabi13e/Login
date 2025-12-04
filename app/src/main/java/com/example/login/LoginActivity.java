package com.example.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference database;

    private EditText schoolIdInput;
    private EditText passwordInput;
    private ImageView passwordToggle;
    private CheckBox rememberCheckbox;
    private TextView forgotPassword;
    private RelativeLayout loginButton;
    private CardView adminLoginCard;
    private TextView signupTab;

    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        initializeViews();

        // Set up listeners
        setupListeners();

        // Check if user is already logged in
        checkRememberMe();
    }

    private void initializeViews() {
        schoolIdInput = findViewById(R.id.school_id_input);
        passwordInput = findViewById(R.id.password_input);
        passwordToggle = findViewById(R.id.password_toggle);
        rememberCheckbox = findViewById(R.id.remember_checkbox);
        forgotPassword = findViewById(R.id.forgot_password);
        loginButton = findViewById(R.id.login_button_container);
        adminLoginCard = findViewById(R.id.admin_login_card);
        signupTab = findViewById(R.id.signup_tab);
    }

    private void setupListeners() {
        // Password visibility toggle
        passwordToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });

        // Login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        // Admin login
        adminLoginCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToAdminLogin();
            }
        });

        // Forgot password
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleForgotPassword();
            }
        });

        // Sign up tab
        signupTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToSignUp();
            }
        });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password
            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordToggle.setImageResource(R.drawable.ic_visibility_off);
            isPasswordVisible = false;
        } else {
            // Show password
            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordToggle.setImageResource(R.drawable.ic_visibility);
            isPasswordVisible = true;
        }
        // Move cursor to end
        passwordInput.setSelection(passwordInput.getText().length());
    }

    private void performLogin() {
        String schoolId = schoolIdInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validation
        if (schoolId.isEmpty()) {
            schoolIdInput.setError("School ID is required");
            schoolIdInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        // Show loading
        loginButton.setEnabled(false);
        Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show();

        // Check user in Firebase Database
        Query query = database.child("users").orderByChild("schoolId").equalTo(schoolId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String userEmail = userSnapshot.child("email").getValue(String.class);
                        String userRole = userSnapshot.child("role").getValue(String.class);

                        if (userEmail != null) {
                            // Sign in with Firebase Auth
                            signInWithEmail(userEmail, password, userRole != null ? userRole : "student");
                        }
                    }
                } else {
                    loginButton.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "School ID not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loginButton.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signInWithEmail(String email, String password, String role) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        loginButton.setEnabled(true);
                        if (task.isSuccessful()) {
                            // Save remember me preference
                            if (rememberCheckbox.isChecked()) {
                                saveRememberMe(true);
                            }

                            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                            // Navigate based on role
                            navigateToHome(role);
                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void navigateToHome(String role) {
        Intent intent;
        if ("admin".equals(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToAdminLogin() {
        Intent intent = new Intent(this, AdminLoginActivity.class);
        startActivity(intent);
    }

    private void navigateToSignUp() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    private void handleForgotPassword() {
        String schoolId = schoolIdInput.getText().toString().trim();

        if (schoolId.isEmpty()) {
            Toast.makeText(this, "Please enter your School ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find email by school ID
        Query query = database.child("users").orderByChild("schoolId").equalTo(schoolId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String email = userSnapshot.child("email").getValue(String.class);
                        if (email != null) {
                            sendPasswordResetEmail(email);
                        }
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "School ID not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendPasswordResetEmail(String email) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this,
                                    "Password reset email sent to " + email,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Failed to send reset email",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveRememberMe(boolean remember) {
        SharedPreferences sharedPref = getSharedPreferences("SaintsGatePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("remember_me", remember);
        editor.apply();
    }

    private void checkRememberMe() {
        SharedPreferences sharedPref = getSharedPreferences("SaintsGatePrefs", MODE_PRIVATE);
        boolean rememberMe = sharedPref.getBoolean("remember_me", false);

        if (rememberMe && auth.getCurrentUser() != null) {
            // User is already logged in
            getUserRoleAndNavigate(auth.getCurrentUser().getUid());
        }
    }

    private void getUserRoleAndNavigate(String uid) {
        database.child("users").child(uid).child("role")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String role = snapshot.getValue(String.class);
                        navigateToHome(role != null ? role : "student");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Default to student if error
                        navigateToHome("student");
                    }
                });
    }
}