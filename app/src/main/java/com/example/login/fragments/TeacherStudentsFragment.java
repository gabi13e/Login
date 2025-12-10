package com.example.login.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login.R;
import com.example.login.adapters.AllStudentsAdapter;
import com.example.login.models.StudentEnrollment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TeacherStudentsFragment extends Fragment {

    private static final String TAG = "TeacherStudentsFragment";
    private static final String DATABASE_URL = "https://saintsgateportal-default-rtdb.firebaseio.com";

    private SearchView searchView;
    private RecyclerView studentsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private TextView studentCountText;

    private AllStudentsAdapter adapter;
    private List<StudentEnrollment> allStudentsList;
    private List<StudentEnrollment> filteredStudentsList;

    private DatabaseReference usersRef;
    private ValueEventListener studentsListener;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_students, container, false);

        // Initialize views
        searchView = view.findViewById(R.id.search_view);
        studentsRecyclerView = view.findViewById(R.id.students_recycler);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyStateText = view.findViewById(R.id.empty_state_text);
        studentCountText = view.findViewById(R.id.student_count_text);

        // Setup RecyclerView
        allStudentsList = new ArrayList<>();
        filteredStudentsList = new ArrayList<>();
        adapter = new AllStudentsAdapter(filteredStudentsList, getContext());
        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        studentsRecyclerView.setAdapter(adapter);

        // Setup search functionality
        setupSearchView();

        // Get current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();

            // Initialize Firebase
            FirebaseDatabase database = FirebaseDatabase.getInstance(DATABASE_URL);
            usersRef = database.getReference("users");

            Log.d(TAG, "Current teacher ID: " + currentUserId);

            // Load all students
            loadStudents();
        } else {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterStudents(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterStudents(newText);
                return true;
            }
        });
    }

    private void filterStudents(String query) {
        filteredStudentsList.clear();

        if (query.isEmpty()) {
            filteredStudentsList.addAll(allStudentsList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (StudentEnrollment student : allStudentsList) {
                if (student.getStudentName().toLowerCase().contains(lowerCaseQuery) ||
                        student.getStudentId().toLowerCase().contains(lowerCaseQuery) ||
                        student.getEmail().toLowerCase().contains(lowerCaseQuery)) {
                    filteredStudentsList.add(student);
                }
            }
        }

        adapter.notifyDataSetChanged();
        updateStudentCount();
    }

    private void loadStudents() {
        Log.d(TAG, "Loading all students...");
        progressBar.setVisibility(View.VISIBLE);
        studentsRecyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.GONE);

        studentsListener = usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allStudentsList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String userId = userSnapshot.getKey();
                        String role = userSnapshot.child("role").getValue(String.class);

                        // Only load students
                        if ("Student".equalsIgnoreCase(role)) {

                            // Build student name
                            String firstName = userSnapshot.child("firstname").getValue(String.class);
                            String middleName = userSnapshot.child("middlename").getValue(String.class);
                            String lastName = userSnapshot.child("lastname").getValue(String.class);

                            String fullName = buildFullName(firstName, middleName, lastName);

                            String studentId = userSnapshot.child("schoolId").getValue(String.class);
                            if (studentId == null || studentId.isEmpty()) {
                                studentId = userId.substring(0, Math.min(8, userId.length()));
                            }

                            String email = userSnapshot.child("email").getValue(String.class);
                            if (email == null) email = "";

                            StudentEnrollment studentInfo = new StudentEnrollment(
                                    userId,
                                    fullName,
                                    studentId,
                                    email,
                                    "",  // courseId - not needed here
                                    0.0, // prelim
                                    0.0, // midterm
                                    0.0  // finals
                            );

                            allStudentsList.add(studentInfo);
                            Log.d(TAG, "Added student: " + fullName);
                        }
                    }
                }

                // Update filtered list
                filteredStudentsList.clear();
                filteredStudentsList.addAll(allStudentsList);

                progressBar.setVisibility(View.GONE);

                if (allStudentsList.isEmpty()) {
                    showEmptyState("No students found");
                } else {
                    studentsRecyclerView.setVisibility(View.VISIBLE);
                    emptyStateText.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    updateStudentCount();
                    Log.d(TAG, "Loaded " + allStudentsList.size() + " students");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading students: " + error.getMessage());
                progressBar.setVisibility(View.GONE);
                showEmptyState("Error: " + error.getMessage());
            }
        });
    }

    private String buildFullName(String firstName, String middleName, String lastName) {
        String fullName = "";
        if (firstName != null && !firstName.isEmpty()) fullName += capitalize(firstName) + " ";
        if (middleName != null && !middleName.isEmpty()) fullName += capitalize(middleName) + " ";
        if (lastName != null && !lastName.isEmpty()) fullName += capitalize(lastName);
        fullName = fullName.trim();
        return fullName.isEmpty() ? "No Name Set" : fullName;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private void updateStudentCount() {
        if (studentCountText != null) {
            studentCountText.setText("Total Students: " + filteredStudentsList.size());
        }
    }

    private void showEmptyState(String message) {
        studentsRecyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
        emptyStateText.setText(message);
        if (studentCountText != null) {
            studentCountText.setText("Total Students: 0");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (usersRef != null && studentsListener != null) {
            usersRef.removeEventListener(studentsListener);
        }
    }
}