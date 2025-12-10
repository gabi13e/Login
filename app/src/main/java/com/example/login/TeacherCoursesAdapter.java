package com.example.login;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login.R;
import com.example.login.models.Course;

import java.util.List;

public class TeacherCoursesAdapter extends RecyclerView.Adapter<TeacherCoursesAdapter.CourseViewHolder> {

    private List<Course> coursesList;
    private Context context;
    private OnCourseClickListener clickListener;

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    public TeacherCoursesAdapter(List<Course> coursesList, Context context) {
        this.coursesList = coursesList;
        this.context = context;
    }

    public void setOnCourseClickListener(OnCourseClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = coursesList.get(position);

        holder.courseCodeText.setText(course.getCourseCode() != null ? course.getCourseCode() : "N/A");
        holder.courseNameText.setText(course.getCourseName() != null ? course.getCourseName() : "No Name");

        // Simple description - just show course code and name
        holder.courseDescText.setText("Tap to view students");
        holder.courseDescText.setVisibility(View.VISIBLE);

        // Set click listener
        holder.courseCard.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onCourseClick(course);
            }
        });
    }

    @Override
    public int getItemCount() {
        return coursesList.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        CardView courseCard;
        TextView courseCodeText;
        TextView courseNameText;
        TextView courseDescText;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            courseCard = itemView.findViewById(R.id.course_card);
            courseCodeText = itemView.findViewById(R.id.course_code_text);
            courseNameText = itemView.findViewById(R.id.course_name_text);
            courseDescText = itemView.findViewById(R.id.course_desc_text);
        }
    }
}