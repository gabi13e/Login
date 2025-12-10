package com.example.login;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.login.R;
import com.example.login.fragments.StudentListFragment;
import com.example.login.models.StudentEnrollment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentsGradeAdapter extends RecyclerView.Adapter<StudentsGradeAdapter.ViewHolder> {

    private static final String DATABASE_URL = "https://saintsgateportal-default-rtdb.firebaseio.com";

    private List<StudentEnrollment> students;
    private Context context;
    private String courseId;
    private StudentListFragment fragment;
    private DatabaseReference usersRef;

    public StudentsGradeAdapter(List<StudentEnrollment> students, Context context, String courseId, StudentListFragment fragment) {
        this.students = students;
        this.context = context;
        this.courseId = courseId;
        this.fragment = fragment;
        this.usersRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("users");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_grade, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentEnrollment student = students.get(position);

        holder.studentName.setText(student.getStudentName());
        holder.studentId.setText("ID: " + student.getStudentId());
        holder.prelimGrade.setText(String.format("Prelim:\n%.2f", student.getPrelimGrade()));
        holder.midtermGrade.setText(String.format("Midterm:\n%.2f", student.getMidtermGrade()));
        holder.finalsGrade.setText(String.format("Finals:\n%.2f", student.getFinalsGrade()));

        double average = (student.getPrelimGrade() + student.getMidtermGrade() + student.getFinalsGrade()) / 3;
        holder.averageGrade.setText(String.format("Average: %.2f", average));

        // Edit grades button
        holder.editGradesBtn.setOnClickListener(v -> {
            showGradeDialog(student, position);
        });

        // Drop student button
        holder.dropStudentBtn.setOnClickListener(v -> {
            showDropStudentDialog(student, position);
        });
    }

    private void showGradeDialog(StudentEnrollment student, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Grades for " + student.getStudentName());

        // Create layout
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Prelim input
        TextView prelimLabel = new TextView(context);
        prelimLabel.setText("Prelim Grade:");
        prelimLabel.setTextSize(16);
        layout.addView(prelimLabel);

        final EditText prelimInput = new EditText(context);
        prelimInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        prelimInput.setText(String.valueOf(student.getPrelimGrade()));
        layout.addView(prelimInput);

        // Midterm input
        TextView midtermLabel = new TextView(context);
        midtermLabel.setText("Midterm Grade:");
        midtermLabel.setTextSize(16);
        midtermLabel.setPadding(0, 20, 0, 0);
        layout.addView(midtermLabel);

        final EditText midtermInput = new EditText(context);
        midtermInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        midtermInput.setText(String.valueOf(student.getMidtermGrade()));
        layout.addView(midtermInput);

        // Finals input
        TextView finalsLabel = new TextView(context);
        finalsLabel.setText("Finals Grade:");
        finalsLabel.setTextSize(16);
        finalsLabel.setPadding(0, 20, 0, 0);
        layout.addView(finalsLabel);

        final EditText finalsInput = new EditText(context);
        finalsInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        finalsInput.setText(String.valueOf(student.getFinalsGrade()));
        layout.addView(finalsInput);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            try {
                double prelim = Double.parseDouble(prelimInput.getText().toString());
                double midterm = Double.parseDouble(midtermInput.getText().toString());
                double finals = Double.parseDouble(finalsInput.getText().toString());

                // Validate grades (0-100)
                if (prelim < 0 || prelim > 100 || midterm < 0 || midterm > 100 ||
                        finals < 0 || finals > 100) {
                    Toast.makeText(context, "Grades must be between 0 and 100", Toast.LENGTH_SHORT).show();
                    return;
                }

                updateGrades(student.getUserId(), prelim, midterm, finals, position);

            } catch (NumberFormatException e) {
                Toast.makeText(context, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void showDropStudentDialog(StudentEnrollment student, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Drop Student")
                .setMessage("Are you sure you want to drop " + student.getStudentName() + " from this course?")
                .setPositiveButton("Drop", (dialog, which) -> {
                    dropStudent(student.getUserId(), position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateGrades(String userId, double prelim, double midterm, double finals, int position) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("prelimGrade", prelim);
        updates.put("midtermGrade", midterm);
        updates.put("finalsGrade", finals);

        usersRef.child(userId)
                .child("enrolledCourses")
                .child(courseId)
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    // Update local data
                    students.get(position).setPrelimGrade(prelim);
                    students.get(position).setMidtermGrade(midterm);
                    students.get(position).setFinalsGrade(finals);
                    notifyItemChanged(position);

                    Toast.makeText(context, "Grades updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update grades: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void dropStudent(String userId, int position) {
        usersRef.child(userId)
                .child("enrolledCourses")
                .child(courseId)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Student dropped successfully", Toast.LENGTH_SHORT).show();

                    // Remove from local list
                    students.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, students.size());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to drop student: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView studentCard;
        TextView studentName, studentId, prelimGrade, midtermGrade, finalsGrade, averageGrade;
        Button editGradesBtn, dropStudentBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            studentCard = itemView.findViewById(R.id.student_card);
            studentName = itemView.findViewById(R.id.student_name);
            studentId = itemView.findViewById(R.id.student_id);
            prelimGrade = itemView.findViewById(R.id.prelim_grade);
            midtermGrade = itemView.findViewById(R.id.midterm_grade);
            finalsGrade = itemView.findViewById(R.id.finals_grade);
            averageGrade = itemView.findViewById(R.id.average_grade);
            editGradesBtn = itemView.findViewById(R.id.edit_grades_btn);
            dropStudentBtn = itemView.findViewById(R.id.drop_student_btn);
        }
    }
}