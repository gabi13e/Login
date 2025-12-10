package com.example.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class AddTeacherActivity extends AppCompatActivity {

    private static final String TAG = "AddTeacherActivity";

    private EditText firstNameInput, middleNameInput, lastNameInput, emailInput, schoolIdInput;
    private Spinner departmentSpinner;
    private Button addTeacherButton, cancelButton;

    private String selectedDepartment = "";

    private DatabaseReference teachersRef;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_teacher);

        // Initialize Views
        firstNameInput = findViewById(R.id.firstname_input);
        middleNameInput = findViewById(R.id.middlename_input);
        lastNameInput = findViewById(R.id.lastname_input);
        emailInput = findViewById(R.id.email_input);
        schoolIdInput = findViewById(R.id.schoolid_input);
        departmentSpinner = findViewById(R.id.department_spinner);
        addTeacherButton = findViewById(R.id.add_teacher_button);
        cancelButton = findViewById(R.id.cancel_button);

        // Firebase Database Reference
        teachersRef = FirebaseDatabase.getInstance().getReference("teachers");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Setup Spinner Listener
        setupSpinnerListener();

        // Cancel Button
        cancelButton.setOnClickListener(v -> finish());

        // Add Teacher Button
        addTeacherButton.setOnClickListener(v -> {
            Log.d(TAG, "Add Teacher button clicked!");
            addTeacherToDatabase();
        });
    }

    private void setupSpinnerListener() {
        // Department Spinner
        departmentSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedDepartment = parent.getItemAtPosition(position).toString();
                Log.d(TAG, "Department selected: " + selectedDepartment);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedDepartment = "";
            }
        });
    }

    private void addTeacherToDatabase() {
        Log.d(TAG, "========== ADD TEACHER STARTED ==========");

        String firstName = firstNameInput.getText().toString().trim();
        String middleName = middleNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String schoolId = schoolIdInput.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(email)
                || TextUtils.isEmpty(schoolId)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_LONG).show();
            return;
        }

        if (selectedDepartment.equals("Select Department") || TextUtils.isEmpty(selectedDepartment)) {
            Toast.makeText(this, "Please select a department", Toast.LENGTH_LONG).show();
            return;
        }

        // Email validation
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "All validations passed! Checking for duplicate School ID...");
        addTeacherButton.setEnabled(false);
        Toast.makeText(this, "Checking School ID...", Toast.LENGTH_SHORT).show();

        // Check if School ID already exists in users collection
        Query userQuery = usersRef.orderByChild("schoolId").equalTo(schoolId);
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // School ID already exists in users collection
                    Log.w(TAG, "School ID already registered in users!");
                    addTeacherButton.setEnabled(true);
                    Toast.makeText(AddTeacherActivity.this,
                            "This School ID is already registered.",
                            Toast.LENGTH_LONG).show();
                } else {
                    // Check if School ID exists in teachers collection
                    checkTeacherDuplicate(schoolId, firstName, middleName, lastName, email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error checking School ID: " + error.getMessage());
                addTeacherButton.setEnabled(true);
                Toast.makeText(AddTeacherActivity.this,
                        "Error checking School ID. Please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkTeacherDuplicate(String schoolId, String firstName, String middleName,
                                       String lastName, String email) {
        Query teacherQuery = teachersRef.orderByChild("schoolId").equalTo(schoolId);
        teacherQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // School ID already exists in teachers collection
                    Log.w(TAG, "Teacher with this School ID already exists!");
                    addTeacherButton.setEnabled(true);
                    Toast.makeText(AddTeacherActivity.this,
                            "A teacher with this School ID already exists.",
                            Toast.LENGTH_LONG).show();
                } else {
                    // School ID is unique, create teacher
                    createTeacherInDatabase(firstName, middleName, lastName, email, schoolId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error checking teacher duplicate: " + error.getMessage());
                addTeacherButton.setEnabled(true);
                Toast.makeText(AddTeacherActivity.this,
                        "Error checking teacher records. Please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createTeacherInDatabase(String firstName, String middleName, String lastName,
                                         String email, String schoolId) {
        Log.d(TAG, "Creating teacher in database...");
        Toast.makeText(this, "Adding teacher...", Toast.LENGTH_SHORT).show();

        // Generate a unique ID for the teacher
        String teacherId = teachersRef.push().getKey();

        // Create Teacher object
        Teacher newTeacher = new Teacher(
                teacherId,
                firstName,
                middleName,
                lastName,
                email,
                schoolId,
                selectedDepartment,
                "Teacher", // Fixed role
                System.currentTimeMillis() // Timestamp
        );

        // Save to Firebase Database
        teachersRef.child(teacherId).setValue(newTeacher)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Teacher added successfully!");

                        // Also add to users collection
                        addToUsersCollection(newTeacher);

                        Toast.makeText(AddTeacherActivity.this,
                                "Teacher added successfully!",
                                Toast.LENGTH_LONG).show();

                        // Clear form
                        clearForm();
                    } else {
                        Log.e(TAG, "Failed to add teacher: " + task.getException());
                        Toast.makeText(AddTeacherActivity.this,
                                "Failed to add teacher: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                    addTeacherButton.setEnabled(true);
                });
    }

    private void addToUsersCollection(Teacher teacher) {
        // Create a user entry for the teacher
        User teacherUser = new User(
                teacher.firstName,
                teacher.middleName,
                teacher.lastName,
                teacher.email,
                teacher.schoolId,
                teacher.role,
                teacher.department,
                "" // No year level for teachers
        );

        String userId = usersRef.push().getKey();
        usersRef.child(userId).setValue(teacherUser)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Teacher also added to users collection");
                    } else {
                        Log.e(TAG, "Failed to add teacher to users collection");
                    }
                });
    }

    private void clearForm() {
        firstNameInput.setText("");
        middleNameInput.setText("");
        lastNameInput.setText("");
        emailInput.setText("");
        schoolIdInput.setText("");
        departmentSpinner.setSelection(0);
        firstNameInput.requestFocus();
    }

    // Teacher Model Class
    public static class Teacher {
        public String teacherId;
        public String firstName;
        public String middleName;
        public String lastName;
        public String email;
        public String schoolId;
        public String department;
        public String role;
        public long createdAt;

        public Teacher() {
            // Default constructor required for Firebase
        }

        public Teacher(String teacherId, String firstName, String middleName, String lastName,
                       String email, String schoolId, String department, String role, long createdAt) {
            this.teacherId = teacherId;
            this.firstName = firstName;
            this.middleName = middleName;
            this.lastName = lastName;
            this.email = email;
            this.schoolId = schoolId;
            this.department = department;
            this.role = role;
            this.createdAt = createdAt;
        }
    }

    // Reuse your existing User class
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