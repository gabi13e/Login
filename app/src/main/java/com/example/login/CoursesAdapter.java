package com.example.login;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.login.R;
import com.example.login.models.Course;
import java.util.List;

public class CoursesAdapter extends RecyclerView.Adapter<CoursesAdapter.CourseViewHolder> {

    private List<Course> coursesList;
    private Context context;
    private OnCourseClickListener clickListener;
    private OnEnrollClickListener enrollListener;

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    public interface OnEnrollClickListener {
        void onEnrollClick(Course course, int position);
    }

    public CoursesAdapter(List<Course> coursesList, Context context) {
        this.coursesList = coursesList;
        this.context = context;
    }

    public void setOnCourseClickListener(OnCourseClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnEnrollClickListener(OnEnrollClickListener listener) {
        this.enrollListener = listener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = coursesList.get(position);

        holder.courseName.setText(course.getCourseName());
        holder.courseCode.setText(course.getCourseCode());
        holder.instructor.setText("👨‍🏫 " + course.getInstructor());
        holder.credits.setText(course.getCredits() + " Units");
        holder.schedule.setText("🗓️ " + course.getSchedule());
        holder.room.setText("📍 " + course.getRoom());

        // Set enrollment status
        if (course.isEnrolled()) {
            holder.enrollButton.setText("Enrolled");
            holder.enrollButton.setEnabled(false);
            holder.enrollButton.setBackgroundColor(context.getResources().getColor(R.color.green_accent));
        } else {
            holder.enrollButton.setText("Enroll");
            holder.enrollButton.setEnabled(true);
            holder.enrollButton.setBackgroundColor(context.getResources().getColor(R.color.blue_primary));
        }

        // Card click listener
        holder.courseCard.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onCourseClick(course);
            }
        });

        // Enroll button click listener
        holder.enrollButton.setOnClickListener(v -> {
            if (enrollListener != null && !course.isEnrolled()) {
                enrollListener.onEnrollClick(course, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return coursesList.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        CardView courseCard;
        TextView courseName, courseCode, instructor, credits, schedule, room;
        Button enrollButton;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            courseCard = itemView.findViewById(R.id.course_card);
            courseName = itemView.findViewById(R.id.course_name);
            courseCode = itemView.findViewById(R.id.course_code);
            instructor = itemView.findViewById(R.id.course_instructor);
            credits = itemView.findViewById(R.id.course_credits);
            schedule = itemView.findViewById(R.id.course_schedule);
            room = itemView.findViewById(R.id.course_room);
            enrollButton = itemView.findViewById(R.id.enroll_button);
        }
    }
}