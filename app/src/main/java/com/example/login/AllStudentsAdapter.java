package com.example.login.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login.R;
import com.example.login.models.StudentEnrollment;

import java.util.List;

public class AllStudentsAdapter extends RecyclerView.Adapter<AllStudentsAdapter.StudentViewHolder> {

    private List<StudentEnrollment> studentsList;
    private Context context;

    public AllStudentsAdapter(List<StudentEnrollment> studentsList, Context context) {
        this.studentsList = studentsList;
        this.context = context;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_info, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        StudentEnrollment student = studentsList.get(position);

        holder.studentNameText.setText(student.getStudentName());
        holder.studentIdText.setText("ID: " + student.getStudentId());
        holder.emailText.setText(student.getEmail());
    }

    @Override
    public int getItemCount() {
        return studentsList.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        CardView studentCard;
        TextView studentNameText;
        TextView studentIdText;
        TextView emailText;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            studentCard = itemView.findViewById(R.id.student_card);
            studentNameText = itemView.findViewById(R.id.student_name_text);
            studentIdText = itemView.findViewById(R.id.student_id_text);
            emailText = itemView.findViewById(R.id.email_text);
        }
    }
}