package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

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
        signupButtonContainer.setOnClickListener(v -> signUpUser());
    }

    private void setupSpinnerListeners() {
        // Role Spinner
        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRole = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRole = "";
            }
        });

        // Department Spinner
        departmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDepartment = parent.getItemAtPosition(position).toString();
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedYearLevel = "";
            }
        });
    }

    private void togglePassword(EditText editText, ImageView toggleIcon) {
        int selection = editText.getSelectionEnd();
        // Check if password is currently visible or hidden
        if (editText.getInputType() == (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)) {
            // If visible, hide it
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_visibility_off);
        } else {
            // If hidden, show it
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_visibility);
        }
        // Restore cursor position
        editText.setSelection(selection);
    }

    private void signUpUser() {
        String firstname = firstnameInput.getText().toString().trim();
        String middlename = middlenameInput.getText().toString().trim();
        String lastname = lastnameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String schoolId = schoolIdInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(firstname) || TextUtils.isEmpty(lastname) || TextUtils.isEmpty(email)
                || TextUtils.isEmpty(schoolId) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedRole.equals("Select Role") || TextUtils.isEmpty(selectedRole)) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDepartment.equals("Select Department") || TextUtils.isEmpty(selectedDepartment)) {
            Toast.makeText(this, "Please select a department", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedYearLevel.equals("Select Year Level") || TextUtils.isEmpty(selectedYearLevel)) {
            Toast.makeText(this, "Please select a year level", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!termsCheckbox.isChecked()) {
            Toast.makeText(this, "You must agree to the Terms & Conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        signupButtonContainer.setEnabled(false); // Disable button

        // Create user in Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Save extra user info in Realtime Database
                        if (user != null) {
                            String userId = user.getUid();
                            User newUser = new User(firstname, middlename, lastname, email, schoolId,
                                    selectedRole, selectedDepartment, selectedYearLevel);
                            usersRef.child(userId).setValue(newUser)
                                    .addOnCompleteListener(dbTask -> {
                                        signupButtonContainer.setEnabled(true); // Re-enable button
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(SignUpActivity.this, "Sign Up Successful!", Toast.LENGTH_SHORT).show();
                                            // Redirect to LoginActivity
                                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(SignUpActivity.this, "Failed to save user data.", Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        signupButtonContainer.setEnabled(true); // Re-enable button
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(SignUpActivity.this, "Sign Up failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // User model class for Firebase
    public static class User {
        public String firstname;
        public String middlename;
        public String lastname;
        public String email;
        public String schoolId;
        public String role;
        public String department;
        public String yearLevel;

        public User() {
            // Default constructor required for Firebase
        }

        public User(String firstname, String middlename, String lastname, String email,
                    String schoolId, String role, String department, String yearLevel) {
            this.firstname = firstname;
            this.middlename = middlename;
            this.lastname = lastname;
            this.email = email;
            this.schoolId = schoolId;
            this.role = role;
            this.department = department;
            this.yearLevel = yearLevel;
        }
    }
}