package com.example.login;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class EditUserActivity extends AppCompatActivity {

    private TextView titleText;
    private EditText schoolIdInput, firstNameInput, middleNameInput, lastNameInput,
            emailInput, passwordInput, departmentInput, roleInput, yearLevelInput;
    private Button saveButton;
    private ImageView backButton;

    private String userId;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user); // Reuse your existing XML

        // Initialize views
        titleText = findViewById(R.id.title_text);
        schoolIdInput = findViewById(R.id.school_id_input);
        firstNameInput = findViewById(R.id.first_name_input);
        middleNameInput = findViewById(R.id.middle_name_input);
        lastNameInput = findViewById(R.id.last_name_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        departmentInput = findViewById(R.id.department_input);
        roleInput = findViewById(R.id.role_input);
        yearLevelInput = findViewById(R.id.year_level_input);
        saveButton = findViewById(R.id.save_button);
        backButton = findViewById(R.id.back_button);

        // Check if we're in edit mode
        if (getIntent().hasExtra("USER_ID")) {
            isEditMode = true;
            userId = getIntent().getStringExtra("USER_ID");
            titleText.setText("Edit User");
            saveButton.setText("Update User");

            // Hide password fields in edit mode
            TextView passwordLabel = findViewById(R.id.password_label);
            View passwordCard = findViewById(R.id.password_card);
            if (passwordLabel != null) passwordLabel.setVisibility(View.GONE);
            if (passwordCard != null) passwordCard.setVisibility(View.GONE);

            // Load user data
            loadUserData();
        }

        // Setup dropdowns
        setupDepartmentDropdown();
        setupRoleDropdown();
        setupYearLevelDropdown();

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Save/Update button
        saveButton.setOnClickListener(v -> {
            if (validateForm()) {
                if (isEditMode) {
                    updateUser();
                } else {
                    addUser();
                }
            }
        });
    }

    private void loadUserData() {
        schoolIdInput.setText(getIntent().getStringExtra("SCHOOL_ID"));
        firstNameInput.setText(getIntent().getStringExtra("FIRST_NAME"));
        middleNameInput.setText(getIntent().getStringExtra("MIDDLE_NAME"));
        lastNameInput.setText(getIntent().getStringExtra("LAST_NAME"));
        emailInput.setText(getIntent().getStringExtra("EMAIL"));
        departmentInput.setText(getIntent().getStringExtra("DEPARTMENT"));
        roleInput.setText(getIntent().getStringExtra("ROLE"));
        yearLevelInput.setText(getIntent().getStringExtra("YEAR_LEVEL"));

        // Handle role-based UI visibility
        String role = getIntent().getStringExtra("ROLE");
        if (role != null) {
            updateUIBasedOnRole(role);
        }
    }

    private void setupDepartmentDropdown() {
        departmentInput.setOnClickListener(v -> showSingleChoiceDialog(
                "Select Department",
                new String[]{"Senior High", "Information Technology", "Engineering", "Business Administration", "Teacher Education",
                        "Physical Education", "Nursing" ,"Criminology",  "Hospitality Management"},

                departmentInput
        ));
    }

    private void setupRoleDropdown() {
        roleInput.setOnClickListener(v -> {
            showSingleChoiceDialog(
                    "Select Role",
                    new String[]{"Student", "Teacher"},
                    roleInput
            );
        });
    }

    private void setupYearLevelDropdown() {
        yearLevelInput.setOnClickListener(v -> showSingleChoiceDialog(
                "Select Year Level",
                new String[]{"1st Year", "2nd Year", "3rd Year", "4th Year", "5th Year"},
                yearLevelInput
        ));
    }

    private void updateUIBasedOnRole(String role) {
        TextView yearLevelLabel = findViewById(R.id.year_level_label);
        View yearLevelCard = findViewById(R.id.year_level_card);

        if ("Student".equalsIgnoreCase(role)) {
            if (yearLevelLabel != null) yearLevelLabel.setVisibility(View.VISIBLE);
            if (yearLevelCard != null) yearLevelCard.setVisibility(View.VISIBLE);
        } else {
            if (yearLevelLabel != null) yearLevelLabel.setVisibility(View.GONE);
            if (yearLevelCard != null) yearLevelCard.setVisibility(View.GONE);
        }
    }

    private void showSingleChoiceDialog(String title, String[] items, EditText targetEditText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        builder.setItems(items, (dialog, which) -> {
            targetEditText.setText(items[which]);

            // If role is changed, update UI
            if (targetEditText.getId() == R.id.role_input) {
                updateUIBasedOnRole(items[which]);
            }
        });

        builder.show();
    }

    private boolean validateForm() {
        // Basic validation
        if (schoolIdInput.getText().toString().trim().isEmpty()) {
            schoolIdInput.setError("School ID is required");
            return false;
        }

        if (firstNameInput.getText().toString().trim().isEmpty()) {
            firstNameInput.setError("First name is required");
            return false;
        }

        if (lastNameInput.getText().toString().trim().isEmpty()) {
            lastNameInput.setError("Last name is required");
            return false;
        }

        if (emailInput.getText().toString().trim().isEmpty()) {
            emailInput.setError("Email is required");
            return false;
        }

        if (departmentInput.getText().toString().trim().isEmpty()) {
            departmentInput.setError("Department is required");
            return false;
        }

        if (roleInput.getText().toString().trim().isEmpty()) {
            roleInput.setError("Role is required");
            return false;
        }

        // For new users, validate password
        if (!isEditMode && passwordInput.getText().toString().trim().length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            return false;
        }

        // Role-specific validation
        if ("Student".equalsIgnoreCase(roleInput.getText().toString().trim())) {
            if (yearLevelInput.getText().toString().trim().isEmpty()) {
                yearLevelInput.setError("Year level is required for students");
                return false;
            }
        }

        return true;
    }

    private void addUser() {
        // Implement add user logic here
        Toast.makeText(this, "Add user functionality", Toast.LENGTH_SHORT).show();
        // Similar to your existing AddUserActivity logic
    }

    private void updateUser() {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("schoolId", schoolIdInput.getText().toString().trim());
        updates.put("firstname", firstNameInput.getText().toString().trim());
        updates.put("middlename", middleNameInput.getText().toString().trim());
        updates.put("lastname", lastNameInput.getText().toString().trim());
        updates.put("email", emailInput.getText().toString().trim());
        updates.put("department", departmentInput.getText().toString().trim());
        updates.put("role", roleInput.getText().toString().trim());

        if ("Student".equalsIgnoreCase(roleInput.getText().toString().trim())) {
            updates.put("yearLevel", yearLevelInput.getText().toString().trim());
        } else {
            // Clear yearLevel for non-students
            updates.put("yearLevel", null);
        }

        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}