package com.example.login.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.login.LoginActivity;
import com.example.login.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TeacherProfileFragment extends Fragment {

    private static final String TAG = "TeacherProfile";
    private static final String DATABASE_URL = "https://saintsgateportal-default-rtdb.firebaseio.com";

    // Views
    private TextView teacherNameText;
    private TextView teacherIdText;
    private TextView teacherEmailText;
    private TextView teacherDepartmentText;
    private TextView teacherSpecializationText;
    private TextView activeCoursesText;
    private TextView totalStudentsText;
    private Button logoutButton;
    private LinearLayout changePasswordLayout;
    private LinearLayout notificationSettingsLayout;

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference database;
    private String currentTeacherId;

    // Listeners for cleanup
    private ValueEventListener coursesListener;
    private ValueEventListener studentsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_profile, container, false);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(DATABASE_URL);
        database = firebaseDatabase.getReference();

        // Initialize views
        initializeViews(view);

        // Get current teacher
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            currentTeacherId = currentUser.getUid();
            loadTeacherProfile();
            loadTeachingStatistics();
        }

        // Setup listeners
        setupClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        teacherNameText = view.findViewById(R.id.teacher_name_text);
        teacherIdText = view.findViewById(R.id.teacher_id_text);
        teacherEmailText = view.findViewById(R.id.teacher_email_text);
        teacherDepartmentText = view.findViewById(R.id.teacher_department_text);
        teacherSpecializationText = view.findViewById(R.id.teacher_specialization_text);
        activeCoursesText = view.findViewById(R.id.active_courses_text);
        totalStudentsText = view.findViewById(R.id.total_students_text);
        logoutButton = view.findViewById(R.id.logout_button);
        changePasswordLayout = view.findViewById(R.id.change_password_layout);
        notificationSettingsLayout = view.findViewById(R.id.notification_settings_layout);
    }

    private void setupClickListeners() {
        logoutButton.setOnClickListener(v -> logout());

        changePasswordLayout.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Change Password feature coming soon", Toast.LENGTH_SHORT).show();
        });

        notificationSettingsLayout.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Notification Settings feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadTeacherProfile() {
        database.child("users").child(currentTeacherId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Get name
                            String firstName = snapshot.child("firstname").getValue(String.class);
                            String middleName = snapshot.child("middlename").getValue(String.class);
                            String lastName = snapshot.child("lastname").getValue(String.class);
                            String fullName = buildFullName(firstName, middleName, lastName);
                            teacherNameText.setText(fullName);

                            // Get email
                            String email = snapshot.child("email").getValue(String.class);
                            if (email != null) {
                                teacherEmailText.setText(email);
                            }

                            // Get teacher ID
                            String teacherId = snapshot.child("teacherId").getValue(String.class);
                            if (teacherId == null || teacherId.isEmpty()) {
                                teacherId = "T-" + currentTeacherId.substring(0, Math.min(8, currentTeacherId.length())).toUpperCase();
                            }
                            teacherIdText.setText(teacherId);

                            // Get department
                            String department = snapshot.child("department").getValue(String.class);
                            if (department != null && !department.isEmpty()) {
                                teacherDepartmentText.setText(department);
                            } else {
                                teacherDepartmentText.setText("Not specified");
                            }

                            // Get specialization
                            String specialization = snapshot.child("specialization").getValue(String.class);
                            if (specialization != null && !specialization.isEmpty()) {
                                teacherSpecializationText.setText(specialization);
                            } else {
                                teacherSpecializationText.setText("Not specified");
                            }

                            Log.d(TAG, "Teacher profile loaded: " + fullName);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading profile: " + error.getMessage());
                        Toast.makeText(requireContext(), "Error loading profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadTeachingStatistics() {
        // Load courses count with real-time listener
        coursesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int coursesCount = 0;

                if (snapshot.exists()) {
                    for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                        coursesCount++;
                    }
                }

                activeCoursesText.setText(String.valueOf(coursesCount));
                Log.d(TAG, "Courses updated: " + coursesCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading courses: " + error.getMessage());
            }
        };
        database.child("courses").addValueEventListener(coursesListener);

        // Load total students count from users table (real-time)
        studentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int studentCount = 0;

                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String role = userSnapshot.child("role").getValue(String.class);
                        if ("Student".equalsIgnoreCase(role)) {
                            studentCount++;
                        }
                    }
                }

                totalStudentsText.setText(String.valueOf(studentCount));
                Log.d(TAG, "Total students updated: " + studentCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading students: " + error.getMessage());
            }
        };
        database.child("users").addValueEventListener(studentsListener);
    }

    private String buildFullName(String firstName, String middleName, String lastName) {
        StringBuilder fullName = new StringBuilder();

        if (firstName != null && !firstName.isEmpty()) {
            fullName.append(capitalize(firstName)).append(" ");
        }
        if (middleName != null && !middleName.isEmpty()) {
            fullName.append(capitalize(middleName)).append(" ");
        }
        if (lastName != null && !lastName.isEmpty()) {
            fullName.append(capitalize(lastName));
        }

        String result = fullName.toString().trim();
        return result.isEmpty() ? "Teacher" : result;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private void logout() {
        // Sign out from Firebase
        auth.signOut();

        // Navigate to login activity
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Remove listeners to prevent memory leaks
        if (coursesListener != null) {
            database.child("courses").removeEventListener(coursesListener);
        }
        if (studentsListener != null) {
            database.child("users").removeEventListener(studentsListener);
        }

        Log.d(TAG, "Listeners removed");
    }
}