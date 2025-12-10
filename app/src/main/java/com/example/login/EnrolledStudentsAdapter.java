package com.example.login;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EnrolledStudentsAdapter extends RecyclerView.Adapter<EnrolledStudentsAdapter.StudentViewHolder> {

    private Context context;
    private List<EnrolledStudent> studentList;
    private DecimalFormat gradeFormat;
    private SimpleDateFormat dateFormat;

    public EnrolledStudentsAdapter(Context context, List<EnrolledStudent> studentList) {
        this.context = context;
        this.studentList = studentList;
        this.gradeFormat = new DecimalFormat("0.00");
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_enrolled_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        EnrolledStudent student = studentList.get(position);

        holder.studentName.setText(student.getFullName());
        holder.studentId.setText("ID: " + (student.getSchoolId() != null ? student.getSchoolId() : "N/A"));
        holder.yearLevel.setText(student.getYearLevel() != null ? student.getYearLevel() : "N/A");

        // Format grades
        holder.prelimGrade.setText("Prelim: " + gradeFormat.format(student.getPrelimGrade()));
        holder.midtermGrade.setText("Midterm: " + gradeFormat.format(student.getMidtermGrade()));
        holder.finalsGrade.setText("Finals: " + gradeFormat.format(student.getFinalsGrade()));

        // Calculate and display average
        double average = student.getAverageGrade();
        holder.averageGrade.setText("Average: " + gradeFormat.format(average));

        // Set average grade color based on value
        if (average >= 75) {
            holder.averageGrade.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.averageGrade.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }

        // Format enrollment date
        if (student.getEnrolledAt() > 0) {
            String enrolledDate = dateFormat.format(new Date(student.getEnrolledAt()));
            holder.enrolledDate.setText("Enrolled: " + enrolledDate);
        } else {
            holder.enrolledDate.setText("Enrolled: N/A");
        }
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView studentName, studentId, yearLevel;
        TextView prelimGrade, midtermGrade, finalsGrade, averageGrade;
        TextView enrolledDate;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.student_name);
            studentId = itemView.findViewById(R.id.student_id);
            yearLevel = itemView.findViewById(R.id.year_level);
            prelimGrade = itemView.findViewById(R.id.prelim_grade);
            midtermGrade = itemView.findViewById(R.id.midterm_grade);
            finalsGrade = itemView.findViewById(R.id.finals_grade);
            averageGrade = itemView.findViewById(R.id.average_grade);
            enrolledDate = itemView.findViewById(R.id.enrolled_date);
        }
    }
}