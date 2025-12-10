package com.example.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private Context context;

    public UserAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        // Set user data
        holder.userName.setText(user.getFullName());
        holder.userId.setText("ID: " + (user.getSchoolId() != null ? user.getSchoolId() : "N/A"));
        holder.userEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");

        // Display year level and department for all users
        String yearLevel = user.getYearLevel() != null ? user.getYearLevel() : "";
        String department = user.getDepartment() != null ? user.getDepartment() : "";

        if ("student".equalsIgnoreCase(user.getRole())) {
            // For students, show year level and department
            if (!yearLevel.isEmpty() && !department.isEmpty()) {
                holder.userInfo.setText(yearLevel + " • " + department);
            } else if (!yearLevel.isEmpty()) {
                holder.userInfo.setText(yearLevel);
            } else if (!department.isEmpty()) {
                holder.userInfo.setText(department);
            } else {
                holder.userInfo.setText("N/A");
            }
        } else if ("teacher".equalsIgnoreCase(user.getRole())) {
            // For teachers, show department
            holder.userInfo.setText(!department.isEmpty() ? department : "N/A");
        } else {
            // For admins or other roles, show role and department
            String role = user.getRole() != null ? user.getRole() : "N/A";
            if (!department.isEmpty()) {
                holder.userInfo.setText(role + " • " + department);
            } else {
                holder.userInfo.setText(role);
            }
        }

        // EDIT button - NOW FUNCTIONAL
        holder.editButton.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(context, EditUserActivity.class);

                // Pass all user data to EditUserActivity (with null checks)
                intent.putExtra("USER_ID", user.getUserId() != null ? user.getUserId() : "");
                intent.putExtra("SCHOOL_ID", user.getSchoolId() != null ? user.getSchoolId() : "");
                intent.putExtra("FIRST_NAME", user.getFirstname() != null ? user.getFirstname() : "");
                intent.putExtra("MIDDLE_NAME", user.getMiddlename() != null ? user.getMiddlename() : "");
                intent.putExtra("LAST_NAME", user.getLastname() != null ? user.getLastname() : "");
                intent.putExtra("EMAIL", user.getEmail() != null ? user.getEmail() : "");
                intent.putExtra("DEPARTMENT", user.getDepartment() != null ? user.getDepartment() : "");
                intent.putExtra("ROLE", user.getRole() != null ? user.getRole() : "");
                intent.putExtra("YEAR_LEVEL", user.getYearLevel() != null ? user.getYearLevel() : "");

                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "Error opening edit screen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });

        // DELETE button
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete User")
                    .setMessage("Are you sure you want to delete " + user.getFullName() + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        DatabaseReference userRef = FirebaseDatabase.getInstance()
                                .getReference("users").child(user.getUserId());
                        userRef.removeValue().addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, user.getFullName() + " deleted successfully", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // Click on entire item to view details
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "View details: " + user.getFullName(), Toast.LENGTH_SHORT).show();
            // You can implement a UserDetailsActivity here
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userId, userEmail, userInfo;
        ImageView editButton, deleteButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userId = itemView.findViewById(R.id.user_id);
            userEmail = itemView.findViewById(R.id.user_email);
            userInfo = itemView.findViewById(R.id.user_info);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}