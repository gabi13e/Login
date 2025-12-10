package com.example.login;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class GradesAdapter extends RecyclerView.Adapter<GradesAdapter.GradeViewHolder> {

    private Context context;
    private List<CourseGrade> gradeList;

    public GradesAdapter(Context context, List<CourseGrade> gradeList) {
        this.context = context;
        this.gradeList = gradeList;
    }

    @NonNull
    @Override
    public GradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_grade, parent, false);
        return new GradeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GradeViewHolder holder, int position) {
        CourseGrade grade = gradeList.get(position);

        holder.courseName.setText(grade.getCourseName());

        // Display grades or "N/A" if not yet graded
        holder.prelimGrade.setText(grade.getPrelimGrade() > 0
                ? String.format(Locale.getDefault(), "%.0f", grade.getPrelimGrade())
                : "N/A");

        holder.midtermGrade.setText(grade.getMidtermGrade() > 0
                ? String.format(Locale.getDefault(), "%.0f", grade.getMidtermGrade())
                : "N/A");

        holder.finalsGrade.setText(grade.getFinalsGrade() > 0
                ? String.format(Locale.getDefault(), "%.0f", grade.getFinalsGrade())
                : "N/A");

        // Calculate and display average
        if (grade.getAverageGrade() > 0) {
            holder.averageGrade.setText(String.format(Locale.getDefault(), "%.1f", grade.getAverageGrade()));
        } else {
            holder.averageGrade.setText("N/A");
        }
    }

    @Override
    public int getItemCount() {
        return gradeList.size();
    }

    public static class GradeViewHolder extends RecyclerView.ViewHolder {
        TextView courseName;
        TextView prelimGrade;
        TextView midtermGrade;
        TextView finalsGrade;
        TextView averageGrade;

        public GradeViewHolder(@NonNull View itemView) {
            super(itemView);
            courseName = itemView.findViewById(R.id.grade_course_name);
            prelimGrade = itemView.findViewById(R.id.grade_prelim);
            midtermGrade = itemView.findViewById(R.id.grade_midterm);
            finalsGrade = itemView.findViewById(R.id.grade_finals);
            averageGrade = itemView.findViewById(R.id.grade_average);
        }
    }
}