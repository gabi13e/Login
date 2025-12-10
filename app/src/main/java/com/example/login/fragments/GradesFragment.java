package com.example.login.fragments;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login.CourseGrade;
import com.example.login.GradesAdapter;
import com.example.login.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GradesFragment extends Fragment {

    private static final String TAG = "GradesFragment";

    private RecyclerView gradesRecyclerView;
    private GradesAdapter gradesAdapter;
    private List<CourseGrade> gradeList;
    private LinearLayout emptyStateLayout;
    private TextView overallGpaText;
    private ProgressBar progressBar;

    private DatabaseReference usersRef;
    private String currentUserId;
    private ValueEventListener gradesListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grades, container, false);

        Button exportPdfButton = view.findViewById(R.id.btn_export_pdf);
        exportPdfButton.setOnClickListener(v -> exportGradesToPDF());

        // Initialize views
        gradesRecyclerView = view.findViewById(R.id.grades_recycler_view);
        emptyStateLayout = view.findViewById(R.id.empty_state_layout);
        overallGpaText = view.findViewById(R.id.overall_gpa_text);
        progressBar = view.findViewById(R.id.progress_bar);

        gradesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Firebase references
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("users");

            Log.d(TAG, "Current user ID: " + currentUserId);
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            showEmptyState();
            return view;
        }

        // Load grades from Firebase
        loadGradesFromFirebase();

        return view;
    }

    private void loadGradesFromFirebase() {
        Log.d(TAG, "Loading grades from Firebase for user: " + currentUserId);

        // Show loading
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        gradesRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);

        gradeList = new ArrayList<>();

        // Real-time listener for enrolled courses
        gradesListener = usersRef.child(currentUserId).child("enrolledCourses")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        gradeList.clear();

                        if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                            Log.d(TAG, "Found " + snapshot.getChildrenCount() + " enrolled courses");

                            for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                                String courseId = courseSnapshot.child("courseId").getValue(String.class);
                                String courseName = courseSnapshot.child("courseName").getValue(String.class);

                                // Get grades (default to 0 if not set)
                                Double prelimGrade = courseSnapshot.child("prelimGrade").getValue(Double.class);
                                Double midtermGrade = courseSnapshot.child("midtermGrade").getValue(Double.class);
                                Double finalsGrade = courseSnapshot.child("finalsGrade").getValue(Double.class);

                                // Use 0 if null (will display as N/A in adapter)
                                prelimGrade = (prelimGrade != null) ? prelimGrade : 0.0;
                                midtermGrade = (midtermGrade != null) ? midtermGrade : 0.0;
                                finalsGrade = (finalsGrade != null) ? finalsGrade : 0.0;

                                CourseGrade grade = new CourseGrade(
                                        courseId,
                                        courseName,
                                        prelimGrade,
                                        midtermGrade,
                                        finalsGrade
                                );

                                gradeList.add(grade);
                                Log.d(TAG, "Loaded grade for: " + courseName +
                                        " (P:" + prelimGrade + " M:" + midtermGrade + " F:" + finalsGrade + ")");
                            }

                            // Show grades
                            displayGrades();
                            calculateOverallGPA();

                        } else {
                            Log.d(TAG, "No enrolled courses found");
                            showEmptyState();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to load grades: " + error.getMessage());

                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }

                        if (error.getCode() == DatabaseError.PERMISSION_DENIED) {
                            Toast.makeText(getContext(),
                                    "Permission denied. Please check Firebase rules.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(),
                                    "Failed to load grades: " + error.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }

                        showEmptyState();
                    }
                });
    }

    private void displayGrades() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        emptyStateLayout.setVisibility(View.GONE);
        gradesRecyclerView.setVisibility(View.VISIBLE);

        gradesAdapter = new GradesAdapter(getContext(), gradeList);
        gradesRecyclerView.setAdapter(gradesAdapter);

        Log.d(TAG, "Displayed " + gradeList.size() + " grades");
    }

    private void showEmptyState() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        emptyStateLayout.setVisibility(View.VISIBLE);
        gradesRecyclerView.setVisibility(View.GONE);
        if (overallGpaText != null) {
            overallGpaText.setText("N/A");
        }
    }

    private void calculateOverallGPA() {
        if (gradeList.isEmpty()) {
            if (overallGpaText != null) {
                overallGpaText.setText("N/A");
            }
            return;
        }

        double totalAverage = 0;
        int validCourses = 0;

        // Only count courses with grades > 0 (actual grades, not N/A)
        for (CourseGrade grade : gradeList) {
            double avg = grade.getAverageGrade();
            if (avg > 0) {
                totalAverage += avg;
                validCourses++;
            }
        }

        if (validCourses > 0) {
            // Convert to 4.0 scale (assuming 100-point scale)
            double gpa = totalAverage / validCourses / 25.0;
            if (overallGpaText != null) {
                overallGpaText.setText(String.format(Locale.getDefault(), "%.2f", gpa));
            }
            Log.d(TAG, "Overall GPA calculated: " + String.format("%.2f", gpa) +
                    " (from " + validCourses + " courses)");
        } else {
            // No grades entered yet
            if (overallGpaText != null) {
                overallGpaText.setText("N/A");
            }
            Log.d(TAG, "No valid grades yet, showing N/A");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove Firebase listener to prevent memory leaks
        if (usersRef != null && currentUserId != null && gradesListener != null) {
            usersRef.child(currentUserId).child("enrolledCourses")
                    .removeEventListener(gradesListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "GradesFragment resumed");
    }

    private void exportGradesToPDF() {

        if (gradeList == null || gradeList.isEmpty()) {
            Toast.makeText(getContext(), "No grades to export", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        paint.setTextSize(18);
        paint.setFakeBoldText(true);
        canvas.drawText("Course Grades Report", 40, 50, paint);

        paint.setTextSize(14);
        paint.setFakeBoldText(false);

        int y = 100;

        for (CourseGrade grade : gradeList) {
            canvas.drawText("Course: " + grade.getCourseName(), 40, y, paint);
            y += 20;
            canvas.drawText("Prelim: " + grade.getPrelimGrade(), 60, y, paint);
            y += 20;
            canvas.drawText("Midterm: " + grade.getMidtermGrade(), 60, y, paint);
            y += 20;
            canvas.drawText("Finals: " + grade.getFinalsGrade(), 60, y, paint);
            y += 40;
        }

        pdfDocument.finishPage(page);

        // Save using MediaStore (Android 11+)
        try {
            String fileName = "Grades_Report_" + System.currentTimeMillis() + ".pdf";

            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/YourAppName");

            ContentResolver resolver = getContext().getContentResolver();
            Uri pdfUri = resolver.insert(MediaStore.Files.getContentUri("external"), values);

            OutputStream outputStream = resolver.openOutputStream(pdfUri);
            pdfDocument.writeTo(outputStream);
            outputStream.close();

            Toast.makeText(getContext(), "PDF saved in Documents/YourAppName", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error creating PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        pdfDocument.close();
    }

}