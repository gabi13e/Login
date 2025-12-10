package com.example.login.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login.R;
import com.example.login.TeacherCoursesAdapter;
import com.example.login.models.Course;
import com.example.login.models.StudentEnrollment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TeacherDashboardFragment extends Fragment {

    private static final String TAG = "TeacherDashboard";
    private static final String DATABASE_URL = "https://saintsgateportal-default-rtdb.firebaseio.com";

    // Views
    private TextView teacherNameText;
    private TextView greetingText;
    private TextView classesCountText;
    private TextView studentsCountText;
    private TextView avgGradeText;
    private RecyclerView coursesRecyclerView;
    private ProgressBar progressBar;
    private ImageView notificationIcon;

    // Data
    private TeacherCoursesAdapter adapter;
    private List<Course> teacherCourses;
    private DatabaseReference database;
    private String currentTeacherId;
    private String teacherFullName;

    // Statistics
    private int totalStudents = 0;
    private double averageGrade = 0.0;

    // Listeners for cleanup
    private ValueEventListener coursesListener;
    private ValueEventListener studentsListener;
    private ValueEventListener enrollmentsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_dashboard, container, false);

        // Initialize views
        initializeViews(view);

        // Setup RecyclerView
        teacherCourses = new ArrayList<>();
        adapter = new TeacherCoursesAdapter(teacherCourses, getContext());
        coursesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        coursesRecyclerView.setAdapter(adapter);

        // Get current teacher
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentTeacherId = currentUser.getUid();

            // Initialize Firebase
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(DATABASE_URL);
            database = firebaseDatabase.getReference();

            // Setup click listener
            adapter.setOnCourseClickListener(course -> openStudentList(course));

            // Load teacher data
            loadTeacherProfile();
            loadDashboardData();
        }

        return view;
    }

    private void initializeViews(View view) {
        teacherNameText = view.findViewById(R.id.teacher_name);
        greetingText = view.findViewById(R.id.greeting_text);
        classesCountText = view.findViewById(R.id.classes_count);
        studentsCountText = view.findViewById(R.id.students_count);
        avgGradeText = view.findViewById(R.id.avg_grade);
        coursesRecyclerView = view.findViewById(R.id.teacher_courses_recycler);
        progressBar = view.findViewById(R.id.progress_bar);
        notificationIcon = view.findViewById(R.id.notification_icon);

        // Set greeting based on time
        setGreeting();

        // Setup notification click
        notificationIcon.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "No new notifications", Toast.LENGTH_SHORT).show();
        });
    }

    private void setGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hour >= 5 && hour < 12) {
            greeting = "Good Morning";
        } else if (hour >= 12 && hour < 17) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }

        greetingText.setText(greeting);
    }

    private void loadTeacherProfile() {
        database.child("users").child(currentTeacherId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String firstName = snapshot.child("firstname").getValue(String.class);

                            // Show only first name
                            if (firstName != null && !firstName.isEmpty()) {
                                teacherNameText.setText(capitalize(firstName));
                            } else {
                                teacherNameText.setText("Teacher");
                            }

                            Log.d(TAG, "Loaded teacher: " + firstName);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading teacher profile: " + error.getMessage());
                    }
                });
    }

    private void loadDashboardData() {
        progressBar.setVisibility(View.VISIBLE);

        // Load courses with real-time listener
        loadTeacherCourses();

        // Load students count with real-time listener
        loadStudentCount();

        // Load statistics with real-time listener
        loadStudentStatistics();
    }

    private void loadTeacherCourses() {
        coursesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                teacherCourses.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                        Course course = courseSnapshot.getValue(Course.class);
                        if (course != null) {
                            teacherCourses.add(course);
                        }
                    }
                }

                // Update classes count
                classesCountText.setText(String.valueOf(teacherCourses.size()));

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                Log.d(TAG, "Courses updated: " + teacherCourses.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(),
                        "Error loading courses: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };
        database.child("courses").addValueEventListener(coursesListener);
    }

    private void loadStudentCount() {
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

                totalStudents = studentCount;
                studentsCountText.setText(String.valueOf(totalStudents));

                Log.d(TAG, "Students updated: " + totalStudents);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading students: " + error.getMessage());
            }
        };
        database.child("users").addValueEventListener(studentsListener);
    }

    private void loadStudentStatistics() {
        enrollmentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Double> allGrades = new ArrayList<>();

                if (snapshot.exists()) {
                    for (DataSnapshot enrollmentSnapshot : snapshot.getChildren()) {
                        // Collect grades
                        Double prelim = enrollmentSnapshot.child("prelimGrade").getValue(Double.class);
                        Double midterm = enrollmentSnapshot.child("midtermGrade").getValue(Double.class);
                        Double finals = enrollmentSnapshot.child("finalsGrade").getValue(Double.class);

                        if (prelim != null && prelim > 0) allGrades.add(prelim);
                        if (midterm != null && midterm > 0) allGrades.add(midterm);
                        if (finals != null && finals > 0) allGrades.add(finals);
                    }
                }

                // Calculate average grade
                if (!allGrades.isEmpty()) {
                    double sum = 0;
                    for (Double grade : allGrades) {
                        sum += grade;
                    }
                    averageGrade = sum / allGrades.size();
                    avgGradeText.setText(String.format("%.1f%%", averageGrade));
                } else {
                    avgGradeText.setText("N/A");
                }

                Log.d(TAG, "Average grade updated: " + averageGrade);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading statistics: " + error.getMessage());
            }
        };
        database.child("enrollments").addValueEventListener(enrollmentsListener);
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

    private void openStudentList(Course course) {
        // Navigate to student list fragment
        StudentListFragment fragment = StudentListFragment.newInstance(
                course.getCourseId(),
                course.getCourseName(),
                course.getCourseCode()
        );

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
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
        if (enrollmentsListener != null) {
            database.child("enrollments").removeEventListener(enrollmentsListener);
        }

        Log.d(TAG, "Listeners removed");
    }
}