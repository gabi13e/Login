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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class AddStudentActivity extends AppCompatActivity {

    private static final String TAG = "AddStudentActivity";

    private EditText firstNameInput, middleNameInput, lastNameInput, emailInput, schoolIdInput;
    private Spinner departmentSpinner, yearLevelSpinner;
    private Button addStudentButton, cancelButton;

    private String selectedDepartment = "";
    private String selectedYearLevel = "";

    private DatabaseReference studentsRef;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        // Initialize Views
        firstNameInput = findViewById(R.id.firstname_input);
        middleNameInput = findViewById(R.id.middlename_input);
        lastNameInput = findViewById(R.id.lastname_input);
        emailInput = findViewById(R.id.email_input);
        schoolIdInput = findViewById(R.id.schoolid_input);
        departmentSpinner = findViewById(R.id.department_spinner);
        yearLevelSpinner = findViewById(R.id.yearlevel_spinner);
        addStudentButton = findViewById(R.id.add_student_button);
        cancelButton = findViewById(R.id.cancel_button);

        // Firebase Database Reference
        studentsRef = FirebaseDatabase.getInstance().getReference("students");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Setup Spinner Listeners
        setupSpinnerListeners();

        // Cancel Button
        cancelButton.setOnClickListener(v -> finish());

        // Add Student Button
        addStudentButton.setOnClickListener(v -> {
            Log.d(TAG, "Add Student button clicked!");
            addStudentToDatabase();
        });
    }

    private void setupSpinnerListeners() {
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

        // Year Level Spinner
        yearLevelSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedYearLevel = parent.getItemAtPosition(position).toString();
                Log.d(TAG, "Year Level selected: " + selectedYearLevel);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedYearLevel = "";
            }
        });
    }

    private void addStudentToDatabase() {
        Log.d(TAG, "========== ADD STUDENT STARTED ==========");

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

        if (selectedYearLevel.equals("Select Year Level") || TextUtils.isEmpty(selectedYearLevel)) {
            Toast.makeText(this, "Please select a year level", Toast.LENGTH_LONG).show();
            return;
        }

        // Email validation
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "All validations passed! Checking for duplicate School ID...");
        addStudentButton.setEnabled(false);
        Toast.makeText(this, "Checking School ID...", Toast.LENGTH_SHORT).show();

        // Check if School ID already exists in users collection
        Query userQuery = usersRef.orderByChild("schoolId").equalTo(schoolId);
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // School ID already exists in users collection
                    Log.w(TAG, "School ID already registered in users!");
                    addStudentButton.setEnabled(true);
                    Toast.makeText(AddStudentActivity.this,
                            "This School ID is already registered.",
                            Toast.LENGTH_LONG).show();
                } else {
                    // Check if School ID exists in students collection
                    checkStudentDuplicate(schoolId, firstName, middleName, lastName, email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error checking School ID: " + error.getMessage());
                addStudentButton.setEnabled(true);
                Toast.makeText(AddStudentActivity.this,
                        "Error checking School ID. Please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkStudentDuplicate(String schoolId, String firstName, String middleName,
                                       String lastName, String email) {
        Query studentQuery = studentsRef.orderByChild("schoolId").equalTo(schoolId);
        studentQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // School ID already exists in students collection
                    Log.w(TAG, "Student with this School ID already exists!");
                    addStudentButton.setEnabled(true);
                    Toast.makeText(AddStudentActivity.this,
                            "A student with this School ID already exists.",
                            Toast.LENGTH_LONG).show();
                } else {
                    // School ID is unique, create student
                    createStudentInDatabase(firstName, middleName, lastName, email, schoolId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error checking student duplicate: " + error.getMessage());
                addStudentButton.setEnabled(true);
                Toast.makeText(AddStudentActivity.this,
                        "Error checking student records. Please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createStudentInDatabase(String firstName, String middleName, String lastName,
                                         String email, String schoolId) {
        Log.d(TAG, "Creating student in database...");
        Toast.makeText(this, "Adding student...", Toast.LENGTH_SHORT).show();

        // Generate a unique ID for the student
        String studentId = studentsRef.push().getKey();

        // Create Student object (similar to your User class structure)
        Student newStudent = new Student(
                studentId,
                firstName,
                middleName,
                lastName,
                email,
                schoolId,
                selectedDepartment,
                selectedYearLevel,
                "Student", // Fixed role
                System.currentTimeMillis() // Timestamp for when student was added
        );

        // Save to Firebase Database
        studentsRef.child(studentId).setValue(newStudent)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Student added successfully!");

                        // Also add to users collection for authentication purposes
                        addToUsersCollection(newStudent);

                        Toast.makeText(AddStudentActivity.this,
                                "Student added successfully!",
                                Toast.LENGTH_LONG).show();

                        // Clear form
                        clearForm();
                    } else {
                        Log.e(TAG, "Failed to add student: " + task.getException());
                        Toast.makeText(AddStudentActivity.this,
                                "Failed to add student: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                    addStudentButton.setEnabled(true);
                });
    }

    private void addToUsersCollection(Student student) {
        // Create a user entry for the student (without password initially)
        // Password can be set separately or a default password can be used
        User studentUser = new User(
                student.firstName,
                student.middleName,
                student.lastName,
                student.email,
                student.schoolId,
                student.role,
                student.department,
                student.yearLevel
        );

        // Generate a unique user ID (you might want to use the same ID or a different one)
        String userId = usersRef.push().getKey();
        usersRef.child(userId).setValue(studentUser)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Student also added to users collection");
                    } else {
                        Log.e(TAG, "Failed to add student to users collection");
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
        yearLevelSpinner.setSelection(0);
        firstNameInput.requestFocus();
    }

    // Student Model Class (similar to your User class)
    public static class Student {
        public String studentId;
        public String firstName;
        public String middleName;
        public String lastName;
        public String email;
        public String schoolId;
        public String department;
        public String yearLevel;
        public String role;
        public long createdAt;

        public Student() {
            // Default constructor required for Firebase
        }

        public Student(String studentId, String firstName, String middleName, String lastName,
                       String email, String schoolId, String department, String yearLevel,
                       String role, long createdAt) {
            this.studentId = studentId;
            this.firstName = firstName;
            this.middleName = middleName;
            this.lastName = lastName;
            this.email = email;
            this.schoolId = schoolId;
            this.department = department;
            this.yearLevel = yearLevel;
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