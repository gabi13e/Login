package com.example.login;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AddEditUserActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference database;

    private ImageView backButton;
    private TextView titleText;
    private EditText schoolIdInput;
    private EditText firstNameInput;
    private EditText lastNameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText departmentInput;
    private EditText yearLevelInput;
    private Spinner roleSpinner;
    private Button saveButton;

    private TextView passwordLabel;
    private CardView passwordCard;
    private TextView yearLevelLabel;
    private CardView yearLevelCard;

    private String userId;
    private boolean isEditMode = false;
    private String defaultRole = "student";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_user);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        // Check if edit mode
        userId = getIntent().getStringExtra("userId");
        String mode = getIntent().getStringExtra("mode");
        defaultRole = getIntent().getStringExtra("defaultRole");
        if (defaultRole == null) defaultRole = "student";

        isEditMode = "edit".equals(mode) && userId != null;

        // Initialize views
        initializeViews();

        // Setup role spinner
        setupRoleSpinner();

        // Setup listeners
        setupListeners();

        // Load user data if edit mode
        if (isEditMode) {
            titleText.setText("Edit User");
            passwordLabel.setVisibility(View.GONE);
            passwordCard.setVisibility(View.GONE);
            loadUserData();
        } else {
            if ("teacher".equals(defaultRole)) {
                titleText.setText("Add Teacher");
                yearLevelLabel.setVisibility(View.GONE);
                yearLevelCard.setVisibility(View.GONE);
            } else {
                titleText.setText("Add Student");
            }
        }
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        titleText = findViewById(R.id.title_text);
        schoolIdInput = findViewById(R.id.school_id_input);
        firstNameInput = findViewById(R.id.first_name_input);
        lastNameInput = findViewById(R.id.last_name_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        departmentInput = findViewById(R.id.department_input);
        yearLevelInput = findViewById(R.id.year_level_input);
        roleSpinner = findViewById(R.id.role_spinner);
        saveButton = findViewById(R.id.save_button);
        passwordLabel = findViewById(R.id.password_label);
        passwordCard = findViewById(R.id.password_card);
        yearLevelLabel = findViewById(R.id.year_level_label);
        yearLevelCard = findViewById(R.id.year_level_card);
    }

    private void setupRoleSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.role_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        // Set default role
        if ("teacher".equals(defaultRole)) {
            roleSpinner.setSelection(1); // teacher
        } else if ("admin".equals(defaultRole)) {
            roleSpinner.setSelection(2); // admin
        } else {
            roleSpinner.setSelection(0); // student
        }
    }

    private void setupListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditMode) {
                    updateUser();
                } else {
                    addUser();
                }
            }
        });
    }

    private void loadUserData() {
        database.child("users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            schoolIdInput.setText(snapshot.child("schoolId").getValue(String.class));
                            firstNameInput.setText(snapshot.child("firstName").getValue(String.class));
                            lastNameInput.setText(snapshot.child("lastName").getValue(String.class));
                            emailInput.setText(snapshot.child("email").getValue(String.class));
                            departmentInput.setText(snapshot.child("department").getValue(String.class));

                            String yearLevel = snapshot.child("yearLevel").getValue(String.class);
                            yearLevelInput.setText(yearLevel);

                            String role = snapshot.child("role").getValue(String.class);
                            if ("admin".equals(role)) {
                                roleSpinner.setSelection(2);
                            } else if ("teacher".equals(role)) {
                                roleSpinner.setSelection(1);
                                yearLevelLabel.setVisibility(View.GONE);
                                yearLevelCard.setVisibility(View.GONE);
                            } else {
                                roleSpinner.setSelection(0);
                            }

                            // Disable email editing
                            emailInput.setEnabled(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddEditUserActivity.this,
                                "Error loading user data",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addUser() {
        String schoolId = schoolIdInput.getText().toString().trim();
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String department = departmentInput.getText().toString().trim();
        String yearLevel = yearLevelInput.getText().toString().trim();
        String role = roleSpinner.getSelectedItem().toString();

        // Validation
        if (!validateInputs(schoolId, firstName, lastName, email, password, department, yearLevel, role)) {
            return;
        }

        // Show loading
        saveButton.setEnabled(false);
        saveButton.setText("Creating...");

        // Create user with Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String newUserId = auth.getCurrentUser().getUid();
                            saveUserToDatabase(newUserId, schoolId, firstName, lastName,
                                    email, role, department, yearLevel);
                        } else {
                            saveButton.setEnabled(true);
                            saveButton.setText("Save User");
                            String errorMessage = task.getException() != null ?
                                    task.getException().getMessage() : "Failed to create user";
                            Toast.makeText(AddEditUserActivity.this,
                                    errorMessage,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUser() {
        String schoolId = schoolIdInput.getText().toString().trim();
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String department = departmentInput.getText().toString().trim();
        String yearLevel = yearLevelInput.getText().toString().trim();
        String role = roleSpinner.getSelectedItem().toString();

        // Validation (without password)
        if (!validateInputsForEdit(schoolId, firstName, lastName, email, department, yearLevel, role)) {
            return;
        }

        // Show loading
        saveButton.setEnabled(false);
        saveButton.setText("Updating...");

        // Update user data in database
        Map<String, Object> updates = new HashMap<>();
        updates.put("schoolId", schoolId);
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("role", role);
        updates.put("department", department);
        updates.put("yearLevel", yearLevel);

        database.child("users").child(userId).updateChildren(updates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        saveButton.setEnabled(true);
                        saveButton.setText("Save User");

                        if (task.isSuccessful()) {
                            Toast.makeText(AddEditUserActivity.this,
                                    "User updated successfully",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AddEditUserActivity.this,
                                    "Failed to update user",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserToDatabase(String userId, String schoolId, String firstName,
                                    String lastName, String email, String role,
                                    String department, String yearLevel) {
        Map<String, Object> user = new HashMap<>();
        user.put("schoolId", schoolId);
        user.put("email", email);
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("role", role);
        user.put("department", department);
        user.put("yearLevel", yearLevel);
        user.put("createdAt", System.currentTimeMillis());
        user.put("isActive", true);

        database.child("users").child(userId).setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        saveButton.setEnabled(true);
                        saveButton.setText("Save User");

                        if (task.isSuccessful()) {
                            Toast.makeText(AddEditUserActivity.this,
                                    "User added successfully",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AddEditUserActivity.this,
                                    "Failed to save user data",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean validateInputs(String schoolId, String firstName, String lastName,
                                   String email, String password, String department,
                                   String yearLevel, String role) {
        if (schoolId.isEmpty()) {
            schoolIdInput.setError("School ID is required");
            schoolIdInput.requestFocus();
            return false;
        }

        if (firstName.isEmpty()) {
            firstNameInput.setError("First name is required");
            firstNameInput.requestFocus();
            return false;
        }

        if (lastName.isEmpty()) {
            lastNameInput.setError("Last name is required");
            lastNameInput.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email");
            emailInput.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return false;
        }

        if (department.isEmpty()) {
            departmentInput.setError("Department is required");
            departmentInput.requestFocus();
            return false;
        }

        // Year level is optional for teachers
        if ("student".equals(role) && yearLevel.isEmpty()) {
            yearLevelInput.setError("Year level is required for students");
            yearLevelInput.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validateInputsForEdit(String schoolId, String firstName, String lastName,
                                          String email, String department, String yearLevel, String role) {
        if (schoolId.isEmpty()) {
            schoolIdInput.setError("School ID is required");
            schoolIdInput.requestFocus();
            return false;
        }

        if (firstName.isEmpty()) {
            firstNameInput.setError("First name is required");
            firstNameInput.requestFocus();
            return false;
        }

        if (lastName.isEmpty()) {
            lastNameInput.setError("Last name is required");
            lastNameInput.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return false;
        }

        if (department.isEmpty()) {
            departmentInput.setError("Department is required");
            departmentInput.requestFocus();
            return false;
        }

        // Year level is optional for teachers
        if ("student".equals(role) && yearLevel.isEmpty()) {
            yearLevelInput.setError("Year level is required for students");
            yearLevelInput.requestFocus();
            return false;
        }

        return true;
    }
}