package com.example.login.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.util.Locale;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final String DATABASE_URL = "https://saintsgateportal-default-rtdb.firebaseio.com";

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference database;
    private ValueEventListener userListener;
    private ValueEventListener enrolledCoursesListener;

    // Views
    private TextView profileNameText;
    private TextView profileEmailText;
    private TextView profileSchoolIdText;
    private TextView profileRoleText;
    private TextView profileDepartmentText;
    private TextView profileYearLevelText;
    private Button logoutButton;

    // Info cards
    private TextView gpaValueText;
    private TextView attendanceValueText;
    private TextView coursesValueText;
    private TextView unitsValueText;

    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        Log.d(TAG, "ProfileFragment created");

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(DATABASE_URL);
        database = firebaseDatabase.getReference();

        // Get current user
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            Log.d(TAG, "Current user ID: " + currentUserId);
        } else {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return view;
        }

        // Initialize views
        initializeViews(view);

        // Load user data
        loadUserProfile();

        // Setup logout button
        setupLogoutButton();

        return view;
    }

    private void initializeViews(View view) {
        profileNameText = view.findViewById(R.id.profile_name_text);
        profileEmailText = view.findViewById(R.id.profile_email_text);
        profileSchoolIdText = view.findViewById(R.id.profile_school_id_text);
        profileRoleText = view.findViewById(R.id.profile_role_text);
        profileDepartmentText = view.findViewById(R.id.profile_department_text);
        profileYearLevelText = view.findViewById(R.id.profile_year_level_text);
        logoutButton = view.findViewById(R.id.logout_button);

        gpaValueText = view.findViewById(R.id.gpa_value_text);
        attendanceValueText = view.findViewById(R.id.attendance_value_text);
        coursesValueText = view.findViewById(R.id.courses_value_text);
        unitsValueText = view.findViewById(R.id.units_value_text);
    }

    private void setupLogoutButton() {
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> showLogoutConfirmationDialog());
        }
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Logout", (dialog, which) -> {
                    Log.d(TAG, "User confirmed logout");
                    logout();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Log.d(TAG, "User cancelled logout");
                    dialog.dismiss();
                })
                .setCancelable(true)
                .show();
    }

    private void loadUserProfile() {
        if (currentUserId == null) {
            Log.e(TAG, "User ID is null");
            navigateToLogin();
            return;
        }

        Log.d(TAG, "Loading profile for user: " + currentUserId);

        // Load basic user info
        userListener = database.child("users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Log.d(TAG, "User data found in database");

                            // Read name parts
                            String firstName = snapshot.child("firstname").getValue(String.class);
                            String middleName = snapshot.child("middlename").getValue(String.class);
                            String lastName = snapshot.child("lastname").getValue(String.class);

                            // Concatenate full name and capitalize
                            String fullName = buildFullName(firstName, middleName, lastName);

                            // Other profile info
                            String email = snapshot.child("email").getValue(String.class);
                            String schoolId = snapshot.child("schoolId").getValue(String.class);
                            String role = snapshot.child("role").getValue(String.class);
                            String department = snapshot.child("department").getValue(String.class);
                            String yearLevel = snapshot.child("yearLevel").getValue(String.class);

                            Log.d(TAG, "Full Name: " + fullName);
                            Log.d(TAG, "Role: " + role);
                            Log.d(TAG, "Department: " + department);
                            Log.d(TAG, "Year Level: " + yearLevel);

                            // Display basic profile info
                            displayBasicProfile(fullName, email, schoolId, role, department, yearLevel);

                            // Load academic stats
                            loadAcademicStats();

                        } else {
                            Log.w(TAG, "User data not found in database");
                            displayDefaultProfile();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Database error: " + error.getMessage());
                        Toast.makeText(getContext(), "Failed to load profile: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        displayDefaultProfile();
                    }
                });
    }

    private String buildFullName(String firstName, String middleName, String lastName) {
        String fullName = "";
        if (firstName != null && !firstName.isEmpty()) fullName += capitalize(firstName) + " ";
        if (middleName != null && !middleName.isEmpty()) fullName += capitalize(middleName) + " ";
        if (lastName != null && !lastName.isEmpty()) fullName += capitalize(lastName);
        fullName = fullName.trim();
        if (fullName.isEmpty()) fullName = "Student";
        return fullName;
    }

    private void loadAcademicStats() {
        Log.d(TAG, "Loading academic stats from enrolledCourses");

        enrolledCoursesListener = database.child("users").child(currentUserId).child("enrolledCourses")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int totalCourses = 0;
                        int totalUnits = 0;
                        double totalGradeSum = 0;
                        int validGradesCount = 0;

                        if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                            Log.d(TAG, "Found " + snapshot.getChildrenCount() + " enrolled courses");

                            for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                                totalCourses++;

                                // Assume each course is 3 units (you can add a units field to courses if needed)
                                totalUnits += 3;

                                // Get grades
                                Double prelimGrade = courseSnapshot.child("prelimGrade").getValue(Double.class);
                                Double midtermGrade = courseSnapshot.child("midtermGrade").getValue(Double.class);
                                Double finalsGrade = courseSnapshot.child("finalsGrade").getValue(Double.class);

                                // Calculate average for this course
                                if (prelimGrade != null && midtermGrade != null && finalsGrade != null) {
                                    double avg = (prelimGrade + midtermGrade + finalsGrade) / 3.0;

                                    // Only count if there's an actual grade (> 0)
                                    if (avg > 0) {
                                        totalGradeSum += avg;
                                        validGradesCount++;
                                    }
                                }
                            }
                        } else {
                            Log.d(TAG, "No enrolled courses found");
                        }

                        // Calculate GPA (convert from 100-point to 4.0 scale)
                        String gpaText = "N/A";
                        if (validGradesCount > 0) {
                            double averageGrade = totalGradeSum / validGradesCount;
                            double gpa = averageGrade / 25.0; // Simple conversion: 100/4 = 25
                            gpaText = String.format(Locale.getDefault(), "%.2f", gpa);
                            Log.d(TAG, "Calculated GPA: " + gpaText + " from " + validGradesCount + " courses");
                        }

                        // Update UI
                        displayAcademicStats(gpaText, totalCourses, totalUnits);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading academic stats: " + error.getMessage());
                        displayAcademicStats("N/A", 0, 0);
                    }
                });
    }

    private void displayBasicProfile(String name, String email, String schoolId, String role,
                                     String department, String yearLevel) {
        profileNameText.setText(name != null ? name : "Student");
        profileEmailText.setText(email != null ? email : "N/A");
        profileSchoolIdText.setText(schoolId != null ? schoolId : "N/A");
        profileRoleText.setText(role != null ? role : "Student");
        profileDepartmentText.setText(department != null ? department : "N/A");

        // Don't show "N/A" for year level if user is a Teacher
        if (role != null && role.equalsIgnoreCase("Teacher")) {
            profileYearLevelText.setText("N/A");
        } else {
            profileYearLevelText.setText(yearLevel != null && !yearLevel.isEmpty() ? yearLevel : "N/A");
        }

        Log.d(TAG, "Basic profile displayed successfully");
    }

    private void displayAcademicStats(String gpa, int courses, int units) {
        gpaValueText.setText(gpa);
        coursesValueText.setText(String.valueOf(courses));
        unitsValueText.setText(String.valueOf(units));

        // Attendance is not tracked yet, so keep as N/A
        attendanceValueText.setText("N/A");

        Log.d(TAG, "Academic stats displayed: GPA=" + gpa + ", Courses=" + courses + ", Units=" + units);
    }

    private void displayDefaultProfile() {
        profileNameText.setText("Student");
        profileEmailText.setText("N/A");
        profileSchoolIdText.setText("N/A");
        profileRoleText.setText("Student");
        profileDepartmentText.setText("N/A");
        profileYearLevelText.setText("N/A");

        gpaValueText.setText("N/A");
        attendanceValueText.setText("N/A");
        coursesValueText.setText("0");
        unitsValueText.setText("0");
    }

    private void logout() {
        Log.d(TAG, "Logging out user");

        if (getContext() != null) {
            // Clear remember me preference
            SharedPreferences sharedPref = getContext().getSharedPreferences("SaintsGatePrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("remember_me", false);
            editor.apply();
        }

        // Sign out from Firebase
        auth.signOut();

        // Show logout message
        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to login
        navigateToLogin();
    }

    private void navigateToLogin() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove Firebase listeners to prevent memory leaks
        if (database != null && currentUserId != null) {
            if (userListener != null) {
                database.child("users").child(currentUserId).removeEventListener(userListener);
            }
            if (enrolledCoursesListener != null) {
                database.child("users").child(currentUserId).child("enrolledCourses")
                        .removeEventListener(enrolledCoursesListener);
            }
        }
        Log.d(TAG, "ProfileFragment destroyed, listeners removed");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "ProfileFragment resumed");
        // Data will automatically refresh via ValueEventListeners
    }
}