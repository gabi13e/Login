package com.example.login.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.login.R;
import com.example.login.CoursesAdapter;
import com.example.login.models.Course;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoursesFragment extends Fragment {

    private static final String TAG = "CoursesFragment";
    private static final String DATABASE_URL = "https://saintsgateportal-default-rtdb.firebaseio.com";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private CoursesAdapter coursesAdapter;
    private List<Course> coursesList;
    private DatabaseReference coursesRef;
    private DatabaseReference usersRef;
    private ValueEventListener coursesListener;
    private ValueEventListener enrolledListener;
    private String currentUserId;
    private List<String> enrolledCourseIds;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_courses, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewCourses);
        progressBar = view.findViewById(R.id.progress_bar);

        // Setup RecyclerView
        coursesList = new ArrayList<>();
        enrolledCourseIds = new ArrayList<>();
        coursesAdapter = new CoursesAdapter(coursesList, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(coursesAdapter);

        // Get current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();

            // Initialize Firebase with explicit database URL
            FirebaseDatabase database = FirebaseDatabase.getInstance(DATABASE_URL);
            coursesRef = database.getReference("courses");
            usersRef = database.getReference("users");

            android.util.Log.d(TAG, "Firebase Database URL: " + DATABASE_URL);
            android.util.Log.d(TAG, "Courses Reference: " + coursesRef.toString());

            // Setup enroll button listener
            setupEnrollListener();

            // Load enrolled courses first, then all courses
            loadEnrolledCourses();
        } else {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void setupEnrollListener() {
        coursesAdapter.setOnEnrollClickListener((course, position) -> {
            enrollInCourse(course, position);
        });
    }

    private void loadEnrolledCourses() {
        android.util.Log.d(TAG, "Loading enrolled courses for user: " + currentUserId);

        enrolledListener = usersRef.child(currentUserId).child("enrolledCourses")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        enrolledCourseIds.clear();

                        if (snapshot.exists()) {
                            for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                                String courseId = courseSnapshot.child("courseId").getValue(String.class);
                                if (courseId != null) {
                                    enrolledCourseIds.add(courseId);
                                }
                            }
                            android.util.Log.d(TAG, "Enrolled in " + enrolledCourseIds.size() + " courses");
                        }

                        // Now load all courses
                        loadAllCourses();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.e(TAG, "Error loading enrolled courses: " + error.getMessage());
                        loadAllCourses(); // Load courses anyway
                    }
                });
    }

    private void loadAllCourses() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        android.util.Log.d(TAG, "Loading all courses from Firebase...");
        android.util.Log.d(TAG, "Courses path: " + coursesRef.toString());

        coursesListener = coursesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                android.util.Log.d(TAG, "=== Firebase onDataChange called ===");
                android.util.Log.d(TAG, "Snapshot exists: " + snapshot.exists());
                android.util.Log.d(TAG, "Children count: " + snapshot.getChildrenCount());

                coursesList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                        android.util.Log.d(TAG, "Processing course: " + courseSnapshot.getKey());

                        try {
                            Course course = courseSnapshot.getValue(Course.class);
                            if (course != null) {
                                // Check if user is enrolled in this course
                                course.setEnrolled(enrolledCourseIds.contains(course.getCourseId()));

                                android.util.Log.d(TAG, "Course loaded: " + course.getCourseName() +
                                        " (ID: " + course.getCourseId() + ", Enrolled: " + course.isEnrolled() + ")");
                                coursesList.add(course);
                            } else {
                                android.util.Log.e(TAG, "Course object is null for key: " + courseSnapshot.getKey());
                            }
                        } catch (Exception e) {
                            android.util.Log.e(TAG, "Error parsing course: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                } else {
                    android.util.Log.e(TAG, "Snapshot does not exist at path: " + coursesRef.toString());
                }

                coursesAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                android.util.Log.d(TAG, "Total courses loaded: " + coursesList.size());

                if (coursesList.isEmpty()) {
                    Toast.makeText(
                            requireContext(),
                            "No courses available. Please check Firebase.",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                android.util.Log.e(TAG, "=== Firebase onCancelled called ===");
                android.util.Log.e(TAG, "Error code: " + error.getCode());
                android.util.Log.e(TAG, "Error message: " + error.getMessage());
                android.util.Log.e(TAG, "Error details: " + error.getDetails());

                progressBar.setVisibility(View.GONE);

                Toast.makeText(
                        requireContext(),
                        "Error: " + error.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void enrollInCourse(Course course, int position) {
        android.util.Log.d(TAG, "Enrolling in course: " + course.getCourseName());

        // Show loading
        Toast.makeText(requireContext(), "Enrolling...", Toast.LENGTH_SHORT).show();

        // Create course enrollment data
        Map<String, Object> enrollmentData = new HashMap<>();
        enrollmentData.put("courseId", course.getCourseId());
        enrollmentData.put("courseName", course.getCourseName());
        enrollmentData.put("courseCode", course.getCourseCode());
        enrollmentData.put("prelimGrade", 0.0);
        enrollmentData.put("midtermGrade", 0.0);
        enrollmentData.put("finalsGrade", 0.0);
        enrollmentData.put("enrolledAt", System.currentTimeMillis());

        // Save to Firebase
        usersRef.child(currentUserId)
                .child("enrolledCourses")
                .child(course.getCourseId())
                .setValue(enrollmentData)
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d(TAG, "Successfully enrolled in: " + course.getCourseName());

                    // Update local state
                    course.setEnrolled(true);
                    enrolledCourseIds.add(course.getCourseId());
                    coursesAdapter.notifyItemChanged(position);

                    Toast.makeText(
                            requireContext(),
                            "Successfully enrolled in " + course.getCourseName(),
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e(TAG, "Failed to enroll: " + e.getMessage());
                    Toast.makeText(
                            requireContext(),
                            "Failed to enroll: " + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove Firebase listeners to prevent memory leaks
        if (coursesRef != null && coursesListener != null) {
            coursesRef.removeEventListener(coursesListener);
        }
        if (usersRef != null && currentUserId != null && enrolledListener != null) {
            usersRef.child(currentUserId).child("enrolledCourses")
                    .removeEventListener(enrolledListener);
        }
    }
}