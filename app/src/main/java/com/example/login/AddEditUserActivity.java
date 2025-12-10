package com.example.login;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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
    private EditText schoolIdInput, firstNameInput, middleNameInput, lastNameInput;
    private EditText emailInput, passwordInput, departmentInput, roleInput;
    private EditText yearInput, yearLevelInput;
    private Button saveButton;
    private TextView passwordLabel, yearLabel, yearLevelLabel;
    private CardView passwordCard, yearCard, yearLevelCard;

    private String userId;
    private boolean isEditMode = false;
    private String defaultRole = "student";
    private String selectedRole = "student";
    private String selectedDepartment = "";
    private String selectedYear = "";
    private String selectedYearLevel = "";

    private String[] departments = {"Computer Science", "Information Technology", "Engineering", "Business Administration", "Education"};
    private String[] years = {"1st Year", "2nd Year", "3rd Year", "4th Year", "5th Year"};
    private String[] roles = {"student", "teacher", "admin"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        userId = getIntent().getStringExtra("userId");
        String mode = getIntent().getStringExtra("mode");
        defaultRole = getIntent().getStringExtra("defaultRole");
        if (defaultRole == null) defaultRole = "student";
        selectedRole = defaultRole;
        isEditMode = "edit".equals(mode) && userId != null;

        initializeViews();
        setupListeners();

        if (isEditMode) {
            titleText.setText("Edit User");
            passwordLabel.setVisibility(View.GONE);
            passwordCard.setVisibility(View.GONE);
            loadUserData();
        } else {
            if ("teacher".equals(defaultRole)) {
                titleText.setText("Add Teacher");
                yearLabel.setVisibility(View.GONE);
                yearCard.setVisibility(View.GONE);
                yearLevelLabel.setVisibility(View.GONE);
                yearLevelCard.setVisibility(View.GONE);
            } else {
                titleText.setText("Add Student");
            }
            roleInput.setText(capitalizeFirstLetter(defaultRole));
        }
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        titleText = findViewById(R.id.title_text);
        schoolIdInput = findViewById(R.id.school_id_input);
        firstNameInput = findViewById(R.id.first_name_input);
        middleNameInput = findViewById(R.id.middle_name_input);
        lastNameInput = findViewById(R.id.last_name_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        departmentInput = findViewById(R.id.department_input);
        roleInput = findViewById(R.id.role_input);
        yearInput = findViewById(R.id.year_input);
        yearLevelInput = findViewById(R.id.year_level_input);
        saveButton = findViewById(R.id.save_button);
        passwordLabel = findViewById(R.id.password_label);
        passwordCard = findViewById(R.id.password_card);
        yearLabel = findViewById(R.id.year_label);
        yearCard = findViewById(R.id.year_card);
        yearLevelLabel = findViewById(R.id.year_level_label);
        yearLevelCard = findViewById(R.id.year_level_card);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        departmentInput.setOnClickListener(v -> showPicker("department", departments));
        roleInput.setOnClickListener(v -> showPicker("role", new String[]{"Student", "Teacher", "Admin"}));
        yearInput.setOnClickListener(v -> showPicker("year", years));
        yearLevelInput.setOnClickListener(v -> showPicker("yearLevel", years));

        saveButton.setOnClickListener(v -> {
            if (isEditMode) updateUser();
            else addUser();
        });
    }

    private void showPicker(String type, String[] options) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select " + capitalizeFirstLetter(type));
        builder.setItems(options, (dialog, which) -> {
            switch (type) {
                case "department":
                    selectedDepartment = options[which];
                    departmentInput.setText(selectedDepartment);
                    break;
                case "role":
                    selectedRole = roles[which];
                    roleInput.setText(options[which]);
                    if ("student".equals(selectedRole)) {
                        yearLabel.setVisibility(View.VISIBLE);
                        yearCard.setVisibility(View.VISIBLE);
                        yearLevelLabel.setVisibility(View.VISIBLE);
                        yearLevelCard.setVisibility(View.VISIBLE);
                    } else {
                        yearLabel.setVisibility(View.GONE);
                        yearCard.setVisibility(View.GONE);
                        yearInput.setText("");
                        selectedYear = "";
                    }
                    break;
                case "year":
                    selectedYear = options[which];
                    yearInput.setText(selectedYear);
                    break;
                case "yearLevel":
                    selectedYearLevel = options[which];
                    yearLevelInput.setText(selectedYearLevel);
                    break;
            }
        });
        builder.show();
    }

    private void loadUserData() {
        database.child("users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            schoolIdInput.setText(snapshot.child("schoolId").getValue(String.class));
                            firstNameInput.setText(snapshot.child("firstName").getValue(String.class));
                            middleNameInput.setText(snapshot.child("middleName").getValue(String.class));
                            lastNameInput.setText(snapshot.child("lastName").getValue(String.class));
                            emailInput.setText(snapshot.child("email").getValue(String.class));

                            selectedDepartment = snapshot.child("department").getValue(String.class);
                            departmentInput.setText(selectedDepartment);

                            selectedYearLevel = snapshot.child("yearLevel").getValue(String.class);
                            yearLevelInput.setText(selectedYearLevel);

                            selectedRole = snapshot.child("role").getValue(String.class);
                            roleInput.setText(capitalizeFirstLetter(selectedRole));

                            if ("student".equals(selectedRole)) {
                                yearLabel.setVisibility(View.VISIBLE);
                                yearCard.setVisibility(View.VISIBLE);
                                yearLevelLabel.setVisibility(View.VISIBLE);
                                yearLevelCard.setVisibility(View.VISIBLE);
                            } else {
                                yearLabel.setVisibility(View.GONE);
                                yearCard.setVisibility(View.GONE);
                            }

                            emailInput.setEnabled(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddEditUserActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addUser() {
        String schoolId = schoolIdInput.getText().toString().trim();
        String firstName = firstNameInput.getText().toString().trim();
        String middleName = middleNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (!validateInputs(schoolId, firstName, lastName, email, password, selectedDepartment, selectedYearLevel, selectedRole)) return;

        saveButton.setEnabled(false);
        saveButton.setText("Creating...");

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String newUserId = task.getResult().getUser().getUid();
                        saveUserToDatabase(newUserId, schoolId, firstName, middleName, lastName, email, selectedRole, selectedDepartment, selectedYearLevel);
                    } else {
                        saveButton.setEnabled(true);
                        saveButton.setText("Save User");
                        Toast.makeText(AddEditUserActivity.this, task.getException() != null ? task.getException().getMessage() : "Failed to create user", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUser() {
        String schoolId = schoolIdInput.getText().toString().trim();
        String firstName = firstNameInput.getText().toString().trim();
        String middleName = middleNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();

        if (!validateInputsForEdit(schoolId, firstName, lastName, selectedDepartment, selectedYearLevel, selectedRole)) return;

        saveButton.setEnabled(false);
        saveButton.setText("Updating...");

        Map<String, Object> updates = new HashMap<>();
        updates.put("schoolId", schoolId);
        updates.put("firstName", firstName);
        updates.put("middleName", middleName);
        updates.put("lastName", lastName);
        updates.put("role", selectedRole);
        updates.put("department", selectedDepartment);
        updates.put("yearLevel", selectedYearLevel);

        database.child("users").child(userId).updateChildren(updates)
                .addOnCompleteListener(task -> {
                    saveButton.setEnabled(true);
                    saveButton.setText("Save User");
                    if (task.isSuccessful()) {
                        Toast.makeText(AddEditUserActivity.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddEditUserActivity.this, "Failed to update user", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToDatabase(String userId, String schoolId, String firstName, String middleName, String lastName,
                                    String email, String role, String department, String yearLevel) {
        Map<String, Object> user = new HashMap<>();
        user.put("schoolId", schoolId);
        user.put("firstName", firstName);
        user.put("middleName", middleName);
        user.put("lastName", lastName);
        user.put("email", email);
        user.put("role", role);
        user.put("department", department);
        user.put("yearLevel", yearLevel);
        user.put("createdAt", System.currentTimeMillis());
        user.put("isActive", true);

        database.child("users").child(userId).setValue(user)
                .addOnCompleteListener(task -> {
                    saveButton.setEnabled(true);
                    saveButton.setText("Save User");
                    if (task.isSuccessful()) {
                        Toast.makeText(AddEditUserActivity.this, "User added successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddEditUserActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateInputs(String schoolId, String firstName, String lastName, String email, String password,
                                   String department, String yearLevel, String role) {
        if (schoolId.isEmpty()) { schoolIdInput.setError("Required"); schoolIdInput.requestFocus(); return false; }
        if (firstName.isEmpty()) { firstNameInput.setError("Required"); firstNameInput.requestFocus(); return false; }
        if (lastName.isEmpty()) { lastNameInput.setError("Required"); lastNameInput.requestFocus(); return false; }
        if (email.isEmpty()) { emailInput.setError("Required"); emailInput.requestFocus(); return false; }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { emailInput.setError("Invalid"); emailInput.requestFocus(); return false; }
        if (password.isEmpty()) { passwordInput.setError("Required"); passwordInput.requestFocus(); return false; }
        if (password.length() < 6) { passwordInput.setError("Min 6 chars"); passwordInput.requestFocus(); return false; }
        if (department.isEmpty()) { Toast.makeText(this, "Select department", Toast.LENGTH_SHORT).show(); return false; }
        if ("student".equals(role) && yearLevel.isEmpty()) { Toast.makeText(this, "Select year level", Toast.LENGTH_SHORT).show(); return false; }
        return true;
    }

    private boolean validateInputsForEdit(String schoolId, String firstName, String lastName,
                                          String department, String yearLevel, String role) {
        if (schoolId.isEmpty()) { schoolIdInput.setError("Required"); schoolIdInput.requestFocus(); return false; }
        if (firstName.isEmpty()) { firstNameInput.setError("Required"); firstNameInput.requestFocus(); return false; }
        if (lastName.isEmpty()) { lastNameInput.setError("Required"); lastNameInput.requestFocus(); return false; }
        if (department.isEmpty()) { Toast.makeText(this, "Select department", Toast.LENGTH_SHORT).show(); return false; }
        if ("student".equals(role) && yearLevel.isEmpty()) { Toast.makeText(this, "Select year level", Toast.LENGTH_SHORT).show(); return false; }
        return true;
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}
