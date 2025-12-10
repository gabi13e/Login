package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminLoginActivity extends AppCompatActivity {

    private static final String TAG = "AdminLoginActivity";

    private FirebaseAuth auth;
    private DatabaseReference database;

    private EditText adminIdInput, adminPasswordInput;
    private ImageView adminPasswordToggle;
    private RelativeLayout adminLoginButton;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_admin);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        adminIdInput = findViewById(R.id.admin_id_input);
        adminPasswordInput = findViewById(R.id.admin_password_input);
        adminPasswordToggle = findViewById(R.id.admin_password_toggle);
        adminLoginButton = findViewById(R.id.admin_login_button);

        // Password toggle
        adminPasswordToggle.setOnClickListener(v -> togglePasswordVisibility());

        // Login button
        adminLoginButton.setOnClickListener(v -> performAdminLogin());

        // Back to student login
        findViewById(R.id.back_to_student_login).setOnClickListener(v -> finish());

        // Optional: Auto-login for testing (remove in production)
        // adminIdInput.setText("admin@example.com");
        // adminPasswordInput.setText("admin123");
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            adminPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            adminPasswordToggle.setImageResource(R.drawable.ic_visibility_off);
        } else {
            adminPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            adminPasswordToggle.setImageResource(R.drawable.ic_visibility);
        }
        adminPasswordInput.setSelection(adminPasswordInput.getText().length());
        isPasswordVisible = !isPasswordVisible;
    }

    private void performAdminLogin() {
        String adminEmail = adminIdInput.getText().toString().trim();
        String password = adminPasswordInput.getText().toString().trim();

        if (adminEmail.isEmpty()) {
            adminIdInput.setError("Admin email required");
            adminIdInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            adminPasswordInput.setError("Password required");
            adminPasswordInput.requestFocus();
            return;
        }

        // Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(adminEmail).matches()) {
            adminIdInput.setError("Please enter a valid email address");
            adminIdInput.requestFocus();
            return;
        }

        // Show loading or disable button to prevent multiple clicks
        adminLoginButton.setEnabled(false);

        // Sign in with Firebase Authentication
        auth.signInWithEmailAndPassword(adminEmail, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Authentication successful");

                        // Get the current user
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Check if user has Admin role in database
                            checkUserRole(firebaseUser.getUid());
                        } else {
                            adminLoginButton.setEnabled(true);
                            Toast.makeText(AdminLoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        adminLoginButton.setEnabled(true);
                        Log.e(TAG, "Login failed: " + task.getException());
                        Toast.makeText(AdminLoginActivity.this,
                                task.getException() != null ?
                                        task.getException().getMessage() : "Invalid credentials",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRole(String userId) {
        database.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adminLoginButton.setEnabled(true);

                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);

                    // Check for "admin" (lowercase) instead of "Admin"
                    if ("admin".equalsIgnoreCase(role)) { // Using equalsIgnoreCase for safety
                        Log.d(TAG, "User is an Admin. Role: " + role);
                        Toast.makeText(AdminLoginActivity.this, "Admin login successful", Toast.LENGTH_SHORT).show();

                        // Navigate to Admin Dashboard
                        Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.w(TAG, "User is not an Admin. Role: " + role);
                        auth.signOut(); // Sign out non-admin users
                        Toast.makeText(AdminLoginActivity.this,
                                "Access denied. Admin privileges required.",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.w(TAG, "User data not found in database");
                    auth.signOut();
                    Toast.makeText(AdminLoginActivity.this,
                            "User data not found. Please contact administrator.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                adminLoginButton.setEnabled(true);
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(AdminLoginActivity.this,
                        "Failed to verify user role. Please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }}