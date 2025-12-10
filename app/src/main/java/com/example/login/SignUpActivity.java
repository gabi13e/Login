package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    private EditText firstnameInput, middlenameInput, lastnameInput, emailInput, schoolIdInput, passwordInput, confirmPasswordInput;
    private Spinner roleSpinner, departmentSpinner, yearlevelSpinner;
    private ImageView passwordToggle, confirmPasswordToggle;
    private CheckBox termsCheckbox;
    private RelativeLayout signupButtonContainer;
    private TextView termsLink, loginTab;

    private String selectedRole = "";
    private String selectedDepartment = "";
    private String selectedYearLevel = "";

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Views
        firstnameInput = findViewById(R.id.firstname_input);
        middlenameInput = findViewById(R.id.middlename_input);
        lastnameInput = findViewById(R.id.lastname_input);
        emailInput = findViewById(R.id.email_input);
        schoolIdInput = findViewById(R.id.schoolid_input);
        roleSpinner = findViewById(R.id.role_spinner);
        departmentSpinner = findViewById(R.id.department_spinner);
        yearlevelSpinner = findViewById(R.id.yearlevel_spinner);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        passwordToggle = findViewById(R.id.password_toggle);
        confirmPasswordToggle = findViewById(R.id.confirm_password_toggle);
        termsCheckbox = findViewById(R.id.terms_checkbox);
        signupButtonContainer = findViewById(R.id.signup_button_container);
        termsLink = findViewById(R.id.terms_link);
        loginTab = findViewById(R.id.login_tab);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Spinner Listeners
        setupSpinnerListeners();

        // Password toggles
        passwordToggle.setOnClickListener(v -> togglePassword(passwordInput, passwordToggle));
        confirmPasswordToggle.setOnClickListener(v -> togglePassword(confirmPasswordInput, confirmPasswordToggle));

        // Terms & Conditions link
        termsLink.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, TermsAndConditionsActivity.class));
        });

        // Login tab click
        loginTab.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });

        // Sign Up button
        signupButtonContainer.setOnClickListener(v -> {
            Log.d(TAG, "Sign Up button clicked!");
            signUpUser();
        });

        ImageView backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

    }

    private void setupSpinnerListeners() {
        // Role Spinner
        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRole = parent.getItemAtPosition(position).toString();
                Log.d(TAG, "Role selected: " + selectedRole);

                // Disable Year Level spinner if Teacher is selected
                if (selectedRole.equalsIgnoreCase("Teacher")) {
                    yearlevelSpinner.setSelection(0);
                    yearlevelSpinner.setEnabled(false);
                    selectedYearLevel = "";
                } else {
                    yearlevelSpinner.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRole = "";
                yearlevelSpinner.setEnabled(true);
            }
        });


        // Department Spinner
        departmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDepartment = parent.getItemAtPosition(position).toString();
                Log.d(TAG, "Department selected: " + selectedDepartment);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDepartment = "";
            }
        });

        // Year Level Spinner
        yearlevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYearLevel = parent.getItemAtPosition(position).toString();
                Log.d(TAG, "Year Level selected: " + selectedYearLevel);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedYearLevel = "";
            }
        });
    }

    private void togglePassword(EditText editText, ImageView toggleIcon) {
        int selection = editText.getSelectionEnd();
        if (editText.getInputType() == (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)) {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_visibility_off);
        } else {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_visibility);
        }
        editText.setSelection(selection);
    }

    private void signUpUser() {
        Log.d(TAG, "========== SIGN UP STARTED ==========");

        String firstname = firstnameInput.getText().toString().trim();
        String middlename = middlenameInput.getText().toString().trim(); // OPTIONAL NOW
        String lastname = lastnameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String schoolId = schoolIdInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        Log.d(TAG, "Validating inputs...");

        // Validation - MIDDLE NAME IS NOW OPTIONAL
        if (TextUtils.isEmpty(firstname) || TextUtils.isEmpty(lastname) || TextUtils.isEmpty(email)
                || TextUtils.isEmpty(schoolId) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_LONG).show();
            return;
        }

        if (selectedRole.equals("Select Role") || TextUtils.isEmpty(selectedRole)) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_LONG).show();
            return;
        }

        if (selectedDepartment.equals("Select Department") || TextUtils.isEmpty(selectedDepartment)) {
            Toast.makeText(this, "Please select a department", Toast.LENGTH_LONG).show();
            return;
        }

        // Only validate Year Level if role is NOT Teacher
        if (!selectedRole.equalsIgnoreCase("Teacher")) {
            if (selectedYearLevel.equals("Select Year Level") || TextUtils.isEmpty(selectedYearLevel)) {
                Toast.makeText(this, "Please select a year level", Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            selectedYearLevel = "";
        }


        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_LONG).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_LONG).show();
            return;
        }

        if (!termsCheckbox.isChecked()) {
            Toast.makeText(this, "You must agree to the Terms & Conditions", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "All validations passed! Checking for duplicate School ID...");
        signupButtonContainer.setEnabled(false);
        Toast.makeText(this, "Checking School ID...", Toast.LENGTH_SHORT).show();

        // ============ CHECK FOR DUPLICATE SCHOOL ID ============
        Query query = usersRef.orderByChild("schoolId").equalTo(schoolId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.w(TAG, "School ID already registered!");
                    signupButtonContainer.setEnabled(true);
                    Toast.makeText(SignUpActivity.this,
                            "This School ID is already registered. Please use a different ID or contact support.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "School ID is unique, proceeding with signup...");
                    createFirebaseUser(firstname, middlename, lastname, email, schoolId, password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error checking School ID: " + error.getMessage());
                signupButtonContainer.setEnabled(true);
                Toast.makeText(SignUpActivity.this,
                        "Error checking School ID. Please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createFirebaseUser(String firstname, String middlename, String lastname,
                                    String email, String schoolId, String password) {
        Log.d(TAG, "Creating Firebase user...");
        Toast.makeText(this, "Creating account...", Toast.LENGTH_SHORT).show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase Auth SUCCESS!");
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            String userId = user.getUid();
                            Log.d(TAG, "User UID: " + userId);

                            User newUser = new User(firstname, middlename, lastname, email, schoolId,
                                    selectedRole, selectedDepartment, selectedYearLevel);

                            Log.d(TAG, "Saving user data to database...");
                            usersRef.child(userId).setValue(newUser);

                            Log.d(TAG, "Scheduling navigation to Login...");
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                Log.d(TAG, "Navigating to LoginActivity NOW!");
                                Toast.makeText(SignUpActivity.this, "Sign Up Successful!", Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

                                Log.d(TAG, "Navigation completed!");
                            }, 1500);

                        } else {
                            Log.e(TAG, "User is NULL after successful auth!");
                            signupButtonContainer.setEnabled(true);
                            Toast.makeText(SignUpActivity.this, "Error: User is null", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e(TAG, "Firebase Auth FAILED!");
                        signupButtonContainer.setEnabled(true);
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Log.e(TAG, "Error: " + errorMessage);

                        if (errorMessage != null && errorMessage.contains("email address is already in use")) {
                            Toast.makeText(SignUpActivity.this,
                                    "This email is already registered. Please login or use a different email.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Sign Up failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // User model class for Firebase
    public static class User {
        public String firstname;
        public String middlename;
        public String lastname;
        public String fullName;
        public String email;
        public String schoolId;
        public String role;
        public String department;
        public String yearLevel;

        public User() {}

        public User(String firstname, String middlename, String lastname, String email,
                    String schoolId, String role, String department, String yearLevel) {
            this.firstname = firstname;
            this.middlename = middlename;
            this.lastname = lastname;

            // Build fullName - handle optional middle name properly
            if (middlename != null && !middlename.isEmpty()) {
                this.fullName = firstname + " " + middlename + " " + lastname;
            } else {
                this.fullName = firstname + " " + lastname;
            }

            this.email = email;
            this.schoolId = schoolId;
            this.role = role;
            this.department = department;
            this.yearLevel = yearLevel;
        }
    }
}