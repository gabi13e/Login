package com.example.login.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.login.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference database;

    // User info views
    private TextView greetingText;
    private TextView userNameText;
    private TextView gpaText;
    private TextView attendanceText;
    private TextView activeCoursesText;
    private TextView creditsText;
    private TextView pendingAssignmentsText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Log.d(TAG, "HomeFragment created");

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        initializeViews(view);

        // Load user data
        loadUserData();

        return view;
    }

    private void initializeViews(View view) {
        greetingText = view.findViewById(R.id.greeting_text);
        userNameText = view.findViewById(R.id.user_name_text);
        gpaText = view.findViewById(R.id.gpa_text);
        attendanceText = view.findViewById(R.id.attendance_text);
        activeCoursesText = view.findViewById(R.id.active_courses_text);
        creditsText = view.findViewById(R.id.credits_text);
        pendingAssignmentsText = view.findViewById(R.id.pending_assignments_text);

        Log.d(TAG, "Views initialized");
    }

    private void loadUserData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No user logged in");
            return;
        }

        String uid = currentUser.getUid();
        Log.d(TAG, "Loading user data for UID: " + uid);

        database.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "User data found");

                    // Get first name only
                    String firstname = snapshot.child("firstname").getValue(String.class);
                    String firstName = "Student";
                    if (firstname != null && !firstname.isEmpty()) {
                        firstName = firstname.substring(0, 1).toUpperCase() + firstname.substring(1).toLowerCase();
                    }

                    // Set greeting based on time of day
                    setGreetingBasedOnTime();

                    // Set first name in userNameText
                    if (userNameText != null) {
                        userNameText.setText(firstName);
                    }

                    // Load bills (if exists)
                    String bills = snapshot.child("bills").getValue(String.class);
                    if (pendingAssignmentsText != null) {
                        pendingAssignmentsText.setText(bills != null && !bills.isEmpty() ? bills : "No Bills");
                    }

                    // Load academic stats from enrolledCourses
                    loadAcademicStatsFromEnrolledCourses(uid);

                    Log.d(TAG, "Data loaded successfully");
                } else {
                    Log.w(TAG, "User data not found");
                    displayDefaultInfo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading user data: " + error.getMessage());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading data", Toast.LENGTH_SHORT).show();
                }
                displayDefaultInfo();
            }
        });
    }

    private void setGreetingBasedOnTime() {
        if (greetingText == null) return;

        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        String greeting;

        if (hour >= 5 && hour < 12) {
            greeting = "Good Morning";
        } else if (hour >= 12 && hour < 18) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }

        greetingText.setText(greeting);
    }

    private void loadAcademicStatsFromEnrolledCourses(String uid) {
        Log.d(TAG, "Loading academic stats from enrolledCourses");

        database.child("users").child(uid).child("enrolledCourses")
                .addListenerForSingleValueEvent(new ValueEventListener() {
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

                                // Get units (default to 3 if not specified)
                                Integer units = courseSnapshot.child("units").getValue(Integer.class);
                                totalUnits += (units != null ? units : 3);

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
                        String gpaDisplay = "N/A";
                        if (validGradesCount > 0) {
                            double averageGrade = totalGradeSum / validGradesCount;
                            double gpa = averageGrade / 25.0; // Simple conversion: 100/4 = 25
                            gpaDisplay = String.format(Locale.getDefault(), "%.1f", gpa);
                            Log.d(TAG, "Calculated GPA: " + gpaDisplay + " from " + validGradesCount + " courses");
                        }

                        // Update UI
                        displayAcademicStats(gpaDisplay, totalCourses, totalUnits);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading academic stats: " + error.getMessage());
                        displayAcademicStats("N/A", 0, 0);
                    }
                });
    }

    private void displayAcademicStats(String gpa, int courses, int units) {
        if (gpaText != null) {
            gpaText.setText(gpa);
        }

        if (activeCoursesText != null) {
            activeCoursesText.setText(String.valueOf(courses));
        }

        if (creditsText != null) {
            creditsText.setText(String.valueOf(units));
        }

        // Attendance is not tracked yet
        if (attendanceText != null) {
            attendanceText.setText("N/A");
        }

        Log.d(TAG, "Academic stats displayed: GPA=" + gpa + ", Courses=" + courses + ", Units=" + units);
    }

    private void displayDefaultInfo() {
        if (greetingText != null) greetingText.setText("Welcome");
        if (userNameText != null) userNameText.setText("Student");
        if (gpaText != null) gpaText.setText("N/A");
        if (attendanceText != null) attendanceText.setText("N/A");
        if (activeCoursesText != null) activeCoursesText.setText("0");
        if (creditsText != null) creditsText.setText("0");
        if (pendingAssignmentsText != null) pendingAssignmentsText.setText("No Bills");

        Log.d(TAG, "Displaying default info");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "HomeFragment resumed - reloading data");
        loadUserData();
    }
}