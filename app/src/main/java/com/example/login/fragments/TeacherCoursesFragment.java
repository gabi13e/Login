package com.example.login.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.login.fragments.StudentListFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TeacherCoursesFragment extends Fragment {

    private static final String TAG = "TeacherCoursesFragment";
    private static final String DATABASE_URL = "https://saintsgateportal-default-rtdb.firebaseio.com";

    private RecyclerView coursesRecyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;
    private TextView emptyStateText;
    private TextView courseCountBadge;
    private ImageView filterIcon;
    private TeacherCoursesAdapter adapter;
    private List<Course> teacherCoursesList;

    private DatabaseReference coursesRef;
    private ValueEventListener coursesListener;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_courses, container, false);

        // Initialize views
        coursesRecyclerView = view.findViewById(R.id.teacher_courses_recycler);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyStateLayout = view.findViewById(R.id.empty_state_layout);
        emptyStateText = view.findViewById(R.id.empty_state_text);
        courseCountBadge = view.findViewById(R.id.course_count_badge);
        filterIcon = view.findViewById(R.id.filter_icon);

        // Setup RecyclerView with Linear Layout (1 column)
        teacherCoursesList = new ArrayList<>();
        adapter = new TeacherCoursesAdapter(teacherCoursesList, getContext());
        coursesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        coursesRecyclerView.setAdapter(adapter);

        // Setup click listener
        setupCourseClickListener();

        // Setup filter icon click
        filterIcon.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Filter feature coming soon", Toast.LENGTH_SHORT).show();
        });

        // Get current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();

            // Initialize Firebase
            FirebaseDatabase database = FirebaseDatabase.getInstance(DATABASE_URL);
            coursesRef = database.getReference("courses");

            Log.d(TAG, "Current teacher ID: " + currentUserId);

            // Load all courses
            loadTeacherCourses();
        } else {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void setupCourseClickListener() {
        adapter.setOnCourseClickListener(course -> {
            // Navigate to StudentListFragment with course details
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

            Log.d(TAG, "Navigating to student list for course: " + course.getCourseName());
        });
    }

    private void loadTeacherCourses() {
        Log.d(TAG, "Loading courses...");
        progressBar.setVisibility(View.VISIBLE);
        coursesRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);

        coursesListener = coursesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                teacherCoursesList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                        try {
                            Course course = courseSnapshot.getValue(Course.class);

                            if (course != null) {
                                teacherCoursesList.add(course);
                                Log.d(TAG, "Added course: " + course.getCourseName());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing course: " + e.getMessage());
                        }
                    }
                }

                progressBar.setVisibility(View.GONE);

                if (teacherCoursesList.isEmpty()) {
                    showEmptyState("No courses available");
                } else {
                    coursesRecyclerView.setVisibility(View.VISIBLE);
                    emptyStateLayout.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();

                    // Update course count badge
                    courseCountBadge.setText(String.valueOf(teacherCoursesList.size()));

                    Log.d(TAG, "Loaded " + teacherCoursesList.size() + " courses");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading courses: " + error.getMessage());
                progressBar.setVisibility(View.GONE);
                showEmptyState("Error: " + error.getMessage());
            }
        });
    }

    private void showEmptyState(String message) {
        coursesRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
        emptyStateText.setText(message);
        courseCountBadge.setText("0");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (coursesRef != null && coursesListener != null) {
            coursesRef.removeEventListener(coursesListener);
        }
    }
}