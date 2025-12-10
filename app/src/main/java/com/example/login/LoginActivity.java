package com.example.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    private static final String TAG = "LoginActivity";

    private FirebaseAuth auth;
    private DatabaseReference database;

    private EditText schoolIdInput;
    private EditText passwordInput;
    private ImageView passwordToggle;
    private CheckBox rememberCheckbox;
    private TextView forgotPassword;
    private RelativeLayout loginButton;
    private LinearLayout adminLoginContainer;
    private TextView signupTab;

    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_login);
            Log.d(TAG, "LoginActivity created");

            // Initialize Firebase
            auth = FirebaseAuth.getInstance();
            database = FirebaseDatabase.getInstance().getReference();

            // Initialize views
            initializeViews();

            // Set up listeners
            setupListeners();

            // Check if user is already logged in
            checkRememberMe();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            Toast.makeText(this, "Error initializing login: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        try {
            schoolIdInput = findViewById(R.id.school_id_input);
            passwordInput = findViewById(R.id.password_input);
            passwordToggle = findViewById(R.id.password_toggle);
            rememberCheckbox = findViewById(R.id.remember_checkbox);
            forgotPassword = findViewById(R.id.forgot_password);
            loginButton = findViewById(R.id.login_button_container);
            adminLoginContainer = findViewById(R.id.admin_login_container);
            signupTab = findViewById(R.id.signup_tab);

            // Check for null views
            if (schoolIdInput == null) Log.e(TAG, "schoolIdInput is NULL!");
            if (passwordInput == null) Log.e(TAG, "passwordInput is NULL!");
            if (loginButton == null) Log.e(TAG, "loginButton is NULL!");
            if (adminLoginContainer == null) Log.e(TAG, "adminLoginContainer is NULL!");

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: ", e);
        }
    }

    private void setupListeners() {
        try {
            // Password visibility toggle
            if (passwordToggle != null) {
                passwordToggle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        togglePasswordVisibility();
                    }
                });
            }

            // Login button
            if (loginButton != null) {
                loginButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            performLogin();
                        } catch (Exception e) {
                            Log.e(TAG, "Error in performLogin: ", e);
                            Toast.makeText(LoginActivity.this, "Login error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            loginButton.setEnabled(true);
                        }
                    }
                });
            }

            // Admin login container
            if (adminLoginContainer != null) {
                adminLoginContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Admin login clicked!");
                        try {
                            Intent intent = new Intent(LoginActivity.this, AdminLoginActivity.class);
                            startActivity(intent);
                            Log.d(TAG, "Started AdminLoginActivity");
                        } catch (Exception e) {
                            Log.e(TAG, "Error starting AdminLoginActivity: ", e);
                            Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
                Log.e(TAG, "adminLoginContainer is NULL - check XML id!");
            }

            // Forgot password
            if (forgotPassword != null) {
                forgotPassword.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleForgotPassword();
                    }
                });
            }

            // Sign up tab
            if (signupTab != null) {
                signupTab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        navigateToSignUp();
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up listeners: ", e);
        }
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

        Log.d(TAG, "========== LOGIN ATTEMPT ==========");
        Log.d(TAG, "School ID entered: '" + schoolId + "'");
        Log.d(TAG, "Password length: " + password.length());

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

        Log.d(TAG, "Querying database for schoolId: " + schoolId);

        // Check user in Firebase Database
        Query query = database.child("users").orderByChild("schoolId").equalTo(schoolId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    Log.d(TAG, "========== DATABASE QUERY RESULT ==========");
                    Log.d(TAG, "Snapshot exists: " + snapshot.exists());
                    Log.d(TAG, "Snapshot children count: " + snapshot.getChildrenCount());

                    if (snapshot.exists()) {
                        Log.d(TAG, "School ID found in database!");

                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String userId = userSnapshot.getKey();
                            String userEmail = userSnapshot.child("email").getValue(String.class);
                            String userRole = userSnapshot.child("role").getValue(String.class);
                            String storedSchoolId = userSnapshot.child("schoolId").getValue(String.class);

                            Log.d(TAG, "User ID: " + userId);
                            Log.d(TAG, "User email: " + userEmail);
                            Log.d(TAG, "User role: " + userRole);
                            Log.d(TAG, "Stored schoolId: '" + storedSchoolId + "'");

                            if (userEmail != null) {
                                // Sign in with Firebase Auth
                                signInWithEmail(userEmail, password, userRole != null ? userRole : "Student");
                                return; // Exit after first match
                            } else {
                                Log.e(TAG, "Email is NULL for this user!");
                            }
                        }
                    } else {
                        Log.w(TAG, "School ID NOT found in database");
                        Log.d(TAG, "Snapshot value: " + snapshot.getValue());
                        loginButton.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "School ID not found. Please check and try again.", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing database result: ", e);
                    loginButton.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "========== DATABASE ERROR ==========");
                Log.e(TAG, "Error code: " + error.getCode());
                Log.e(TAG, "Error message: " + error.getMessage());
                Log.e(TAG, "Error details: " + error.getDetails());

                loginButton.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void signInWithEmail(String email, String password, String role) {
        Log.d(TAG, "Signing in with email authentication");

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        loginButton.setEnabled(true);
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Login successful!");

                            try {
                                // Save remember me preference
                                if (rememberCheckbox.isChecked()) {
                                    saveRememberMe(true);
                                }

                                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                                // Navigate based on role
                                navigateToHome(role);
                            } catch (Exception e) {
                                Log.e(TAG, "Error after successful login: ", e);
                                Toast.makeText(LoginActivity.this, "Navigation error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.e(TAG, "Login failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                            Toast.makeText(LoginActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void navigateToHome(String role) {
        Log.d(TAG, "Navigating to home with role: " + role);

        try {
            Intent intent;

            if ("admin".equalsIgnoreCase(role) || "Admin".equals(role)) {
                // Navigate to Admin Dashboard
                intent = new Intent(this, MainActivity.class); // Replace with AdminDashboardActivity when ready
                Log.d(TAG, "Admin role detected, navigating to MainActivity");

            } else if ("teacher".equalsIgnoreCase(role) || "Teacher".equals(role)) {
                // Navigate to Teacher Dashboard
                intent = new Intent(this, TeacherMainActivity.class);
                Log.d(TAG, "Teacher role detected, navigating to TeacherMainActivity");

            } else {
                // Navigate to Student Dashboard
                intent = new Intent(this, MainActivity.class);
                Log.d(TAG, "Student role detected, navigating to MainActivity");
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            Log.e(TAG, "Error navigating to home: ", e);
            Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void navigateToSignUp() {
        Log.d(TAG, "Navigating to SignUp");
        try {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to SignUp: ", e);
            Toast.makeText(this, "SignUp not available: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleForgotPassword() {
        String schoolId = schoolIdInput.getText().toString().trim();

        if (schoolId.isEmpty()) {
            Toast.makeText(this, "Please enter your School ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Processing forgot password for School ID: " + schoolId);

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
        Log.d(TAG, "Sending password reset email to: " + email);

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Password reset email sent successfully");
                            Toast.makeText(LoginActivity.this,
                                    "Password reset email sent to " + email,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Log.e(TAG, "Failed to send password reset email");
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
        Log.d(TAG, "Remember me saved: " + remember);
    }

    private void checkRememberMe() {
        SharedPreferences sharedPref = getSharedPreferences("SaintsGatePrefs", MODE_PRIVATE);
        boolean rememberMe = sharedPref.getBoolean("remember_me", false);

        Log.d(TAG, "Checking remember me: " + rememberMe);

        if (rememberMe && auth.getCurrentUser() != null) {
            Log.d(TAG, "User already logged in, auto-navigating");
            // User is already logged in
            getUserRoleAndNavigate(auth.getCurrentUser().getUid());
        }
    }

    private void getUserRoleAndNavigate(String uid) {
        Log.d(TAG, "Getting user role for UID: " + uid);

        database.child("users").child(uid).child("role")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String role = snapshot.getValue(String.class);
                        Log.d(TAG, "Retrieved role: " + role);
                        navigateToHome(role != null ? role : "Student");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error getting user role: " + error.getMessage());
                        // Default to student if error
                        navigateToHome("Student");
                    }
                });
    }
}
