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

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<User> userList;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onView(User user);
        void onEdit(User user);
        void onDelete(User user);
    }

    public UserAdapter(Context context, List<User> userList, OnUserActionListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        // Set user data
        holder.userName.setText(user.getFullName());
        holder.userSchoolId.setText(user.getSchoolId());

        // Department info
        String departmentYear = user.getDepartment();
        if (user.getYearLevel() != null && !user.getYearLevel().isEmpty()) {
            departmentYear += " - " + user.getYearLevel();
        }
        holder.userDepartment.setText(departmentYear);

        // Set role badge with appropriate color
        String role = user.getRole();
        holder.userRole.setText(role != null ? role.toUpperCase() : "USER");

        if ("admin".equals(role)) {
            holder.roleBadge.setCardBackgroundColor(
                    context.getResources().getColor(R.color.warning)); // Orange
        } else if ("teacher".equals(role)) {
            holder.roleBadge.setCardBackgroundColor(
                    context.getResources().getColor(android.R.color.holo_blue_dark)); // Blue
        } else if ("student".equals(role)) {
            holder.roleBadge.setCardBackgroundColor(
                    context.getResources().getColor(R.color.success)); // Green
        } else {
            holder.roleBadge.setCardBackgroundColor(
                    context.getResources().getColor(R.color.text_secondary1)); // Gray
        }

        // Set click listeners
        holder.viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onView(user);
            }
        });

        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onEdit(user);
            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDelete(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView userSchoolId;
        TextView userDepartment;
        TextView userRole;
        CardView roleBadge;
        Button viewButton;
        Button editButton;
        Button deleteButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userSchoolId = itemView.findViewById(R.id.user_school_id);
            userDepartment = itemView.findViewById(R.id.user_department);
            userRole = itemView.findViewById(R.id.user_role);
            roleBadge = itemView.findViewById(R.id.role_badge);
            viewButton = itemView.findViewById(R.id.view_button);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}