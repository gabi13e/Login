package com.example.login.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.login.R;
import com.example.login.StudentsGradeAdapter;
import com.example.login.models.StudentEnrollment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class StudentListFragment extends Fragment {

    private static final String TAG = "StudentListFragment";
    private static final String DATABASE_URL = "https://saintsgateportal-default-rtdb.firebaseio.com";
    private static final String ARG_COURSE_ID = "course_id";
    private static final String ARG_COURSE_NAME = "course_name";
    private static final String ARG_COURSE_CODE = "course_code";

    private String courseId;
    private String courseName;
    private String courseCode;

    private TextView courseTitle;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private StudentsGradeAdapter adapter;
    private List<StudentEnrollment> students;
    private DatabaseReference usersRef;

    public static StudentListFragment newInstance(String courseId, String courseName, String courseCode) {
        StudentListFragment fragment = new StudentListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COURSE_ID, courseId);
        args.putString(ARG_COURSE_NAME, courseName);
        args.putString(ARG_COURSE_CODE, courseCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            courseId = getArguments().getString(ARG_COURSE_ID);
            courseName = getArguments().getString(ARG_COURSE_NAME);
            courseCode = getArguments().getString(ARG_COURSE_CODE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_list, container, false);

        // Initialize views
        courseTitle = view.findViewById(R.id.course_title);
        recyclerView = view.findViewById(R.id.students_recycler);
        progressBar = view.findViewById(R.id.progress_bar);

        // Set course info
        courseTitle.setText(courseCode + " - " + courseName);

        // Setup RecyclerView
        students = new ArrayList<>();
        adapter = new StudentsGradeAdapter(students, getContext(), courseId, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance(DATABASE_URL);
        usersRef = database.getReference("users");

        // Load enrolled students
        loadEnrolledStudents();

        return view;
    }

    private void loadEnrolledStudents() {
        progressBar.setVisibility(View.VISIBLE);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                students.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String userId = userSnapshot.getKey();

                        android.util.Log.d(TAG, "Checking user: " + userId);

                        // Check if user is enrolled in this course
                        DataSnapshot enrolledCourses = userSnapshot.child("enrolledCourses");
                        if (enrolledCourses.hasChild(courseId)) {
                            DataSnapshot courseData = enrolledCourses.child(courseId);

                            // Try multiple field names for student name
                            String studentName = userSnapshot.child("fullName").getValue(String.class);
                            if (studentName == null || studentName.isEmpty()) {
                                studentName = userSnapshot.child("name").getValue(String.class);
                            }
                            if (studentName == null || studentName.isEmpty()) {
                                studentName = userSnapshot.child("firstName").getValue(String.class);
                                String lastName = userSnapshot.child("lastName").getValue(String.class);
                                if (studentName != null && lastName != null) {
                                    studentName = studentName + " " + lastName;
                                }
                            }
                            if (studentName == null || studentName.isEmpty()) {
                                studentName = "No Name Set";
                            }

                            // Try multiple field names for student ID
                            String studentId = userSnapshot.child("studentId").getValue(String.class);
                            if (studentId == null || studentId.isEmpty()) {
                                studentId = userSnapshot.child("schoolId").getValue(String.class);
                            }
                            if (studentId == null || studentId.isEmpty()) {
                                studentId = userId.substring(0, Math.min(8, userId.length()));
                            }

                            String email = userSnapshot.child("email").getValue(String.class);
                            if (email == null) email = "";

                            // Get grades
                            Double prelimGrade = courseData.child("prelimGrade").getValue(Double.class);
                            Double midtermGrade = courseData.child("midtermGrade").getValue(Double.class);
                            Double finalsGrade = courseData.child("finalsGrade").getValue(Double.class);

                            android.util.Log.d(TAG, "Student found: " + studentName + " (ID: " + studentId + ")");

                            StudentEnrollment enrollment = new StudentEnrollment(
                                    userId,
                                    studentName,
                                    studentId,
                                    email,
                                    courseId,
                                    prelimGrade != null ? prelimGrade : 0.0,
                                    midtermGrade != null ? midtermGrade : 0.0,
                                    finalsGrade != null ? finalsGrade : 0.0
                            );

                            students.add(enrollment);
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                android.util.Log.d(TAG, "Total students loaded: " + students.size());

                if (students.isEmpty()) {
                    Toast.makeText(requireContext(),
                            "No students enrolled yet",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(),
                        "Error loading students: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void refreshStudentList() {
        loadEnrolledStudents();
    }
}