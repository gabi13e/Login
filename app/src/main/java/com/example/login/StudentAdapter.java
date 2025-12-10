package com.example.login;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private List<User> studentList;

    public StudentAdapter(List<User> studentList) {
        this.studentList = studentList;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        User student = studentList.get(position);

        // Set student info
        holder.studentName.setText(student.getFullName());
        holder.studentId.setText("ID: " + student.getSchoolId());

        // Set email if available
        if (student.getEmail() != null) {
            holder.email.setText(student.getEmail());
            holder.email.setVisibility(View.VISIBLE);
        } else {
            holder.email.setVisibility(View.GONE);
        }

        // Set department if available
        if (student.getDepartment() != null) {
            holder.department.setText(student.getDepartment());
            holder.department.setVisibility(View.VISIBLE);
        } else {
            holder.department.setVisibility(View.GONE);
        }

        // Set year level
        if (student.getYearLevel() != null) {
            holder.yearLevel.setText("Year " + student.getYearLevel());
            holder.yearLevel.setVisibility(View.VISIBLE);
        } else {
            holder.yearLevel.setVisibility(View.GONE);
        }

        // Get department color
        int color = getDepartmentColor(student.getDepartment());

        // Apply color to icon background and department badge
        holder.iconCard.setCardBackgroundColor(color);
        holder.department.setBackgroundColor(color);

        // Adjust text color for better visibility
        if (student.getDepartment() != null) {
            String dept = student.getDepartment().toLowerCase();

            if (dept.contains("criminology")) {
                // White text on black background
                holder.department.setTextColor(Color.WHITE);
            } else if (dept.contains("ba") || dept.contains("business")) {
                // Dark text on yellow background
                holder.department.setTextColor(Color.parseColor("#333333"));
            } else if (dept.contains("pe") || dept.contains("physical education")) {
                // White text on gray background
                holder.department.setTextColor(Color.WHITE);
            } else {
                // White text for all other colors
                holder.department.setTextColor(Color.WHITE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    /**
     * Returns the color code for each department
     * IT - Purple, Nursing - Green, BA - Yellow, Teacher Education - Blue,
     * HM - Pink, PE - Gray, Engineering - Orange, Criminology - Black
     */
    private int getDepartmentColor(String department) {
        if (department == null) return Color.parseColor("#7C4DFF"); // Default purple

        String dept = department.toLowerCase().trim();

        if (dept.contains("it") || dept.contains("information technology")) {
            return Color.parseColor("#7C4DFF"); // Purple
        } else if (dept.contains("nursing")) {
            return Color.parseColor("#4CAF50"); // Green
        } else if (dept.contains("ba") || dept.contains("business administration")) {
            return Color.parseColor("#FFC107"); // Yellow
        } else if (dept.contains("teacher education") || dept.contains("education")) {
            return Color.parseColor("#2196F3"); // Blue
        } else if (dept.contains("hm") || dept.contains("hospitality management")) {
            return Color.parseColor("#E91E63"); // Pink
        } else if (dept.contains("pe") || dept.contains("physical education")) {
            return Color.parseColor("#9E9E9E"); // Gray
        } else if (dept.contains("engineering")) {
            return Color.parseColor("#FF9800"); // Orange
        } else if (dept.contains("criminology")) {
            return Color.parseColor("#212121"); // Black
        } else {
            return Color.parseColor("#7C4DFF"); // Default purple
        }
    }

    /**
     * ViewHolder class
     */
    static class StudentViewHolder extends RecyclerView.ViewHolder {
        CardView iconCard;
        TextView studentName, studentId, email, department, yearLevel;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            iconCard = itemView.findViewById(R.id.student_icon_card);
            studentName = itemView.findViewById(R.id.student_name_text);
            studentId = itemView.findViewById(R.id.student_id_text);
            email = itemView.findViewById(R.id.email_text);
            department = itemView.findViewById(R.id.department_text);
            yearLevel = itemView.findViewById(R.id.year_level_text);
        }
    }
}