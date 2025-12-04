package com.example.login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminDashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth auth;
    private DatabaseReference database;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private TextView welcomeText;
    private TextView dateText;
    private TextView totalUsersCount;
    private TextView totalStudentsCount;
    private TextView totalTeachersCount;
    private TextView totalAdminsCount;
    private TextView userCountText;
    private TextView emptyStateText;
    private TextView managementTitle;
    private TextView listTitle;

    private EditText searchInput;
    private Button addUserButton;
    private Button refreshButton;

    private TextView studentsTab;
    private TextView teachersTab;

    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private List<User> filteredUserList;

    private ImageView menuIcon;

    private String currentTab = "students"; // "students" or "teachers"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        // Check if user is logged in
        if (auth.getCurrentUser() == null) {
            navigateToLogin();
            return;
        }

        // Initialize views
        initializeViews();

        // Setup toolbar and drawer
        setupToolbar();
        setupDrawer();

        // Setup RecyclerView
        setupRecyclerView();

        // Load admin data
        loadAdminData();

        // Load users
        loadAllUsers();

        // Set up listeners
        setupListeners();

        // Set current date
        setCurrentDate();
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        welcomeText = findViewById(R.id.welcome_text);
        dateText = findViewById(R.id.date_text);
        totalUsersCount = findViewById(R.id.total_users_count);
        totalStudentsCount = findViewById(R.id.total_students_count);
        totalTeachersCount = findViewById(R.id.total_teachers_count);
        totalAdminsCount = findViewById(R.id.total_admins_count);
        userCountText = findViewById(R.id.user_count_text);
        emptyStateText = findViewById(R.id.empty_state_text);
        managementTitle = findViewById(R.id.management_title);
        listTitle = findViewById(R.id.list_title);

        searchInput = findViewById(R.id.search_input);
        addUserButton = findViewById(R.id.add_user_button);
        refreshButton = findViewById(R.id.refresh_button);

        studentsTab = findViewById(R.id.students_tab);
        teachersTab = findViewById(R.id.teachers_tab);

        usersRecyclerView = findViewById(R.id.users_recycler_view);
        menuIcon = findViewById(R.id.menu_icon);

        userList = new ArrayList<>();
        filteredUserList = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupRecyclerView() {
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(this, filteredUserList,
                new UserAdapter.OnUserActionListener() {
                    @Override
                    public void onView(User user) {
                        viewUserDetails(user);
                    }

                    @Override
                    public void onEdit(User user) {
                        editUser(user);
                    }

                    @Override
                    public void onDelete(User user) {
                        deleteUser(user);
                    }
                });
        usersRecyclerView.setAdapter(userAdapter);
    }

    private void setupListeners() {
        // Menu icon
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Tab switching
        studentsTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToTab("students");
            }
        });

        teachersTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToTab("teachers");
            }
        });

        // Add user button
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddUserDialog();
            }
        });

        // Refresh button
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAllUsers();
                Toast.makeText(AdminDashboardActivity.this,
                        "Refreshed", Toast.LENGTH_SHORT).show();
            }
        });

        // Search functionality
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void switchToTab(String tab) {
        currentTab = tab;

        // Update tab UI
        if (tab.equals("students")) {
            studentsTab.setBackground(getResources().getDrawable(R.drawable.active_tab_background));
            studentsTab.setTextColor(getResources().getColor(android.R.color.white));
            teachersTab.setBackground(null);
            teachersTab.setTextColor(getResources().getColor(R.color.blue_primary));

            managementTitle.setText("Student Management");
            listTitle.setText("Student List");
            addUserButton.setText("Add Student");
        } else {
            teachersTab.setBackground(getResources().getDrawable(R.drawable.active_tab_background));
            teachersTab.setTextColor(getResources().getColor(android.R.color.white));
            studentsTab.setBackground(null);
            studentsTab.setTextColor(getResources().getColor(R.color.blue_primary));

            managementTitle.setText("Teacher Management");
            listTitle.setText("Teacher List");
            addUserButton.setText("Add Teacher");
        }

        // Filter and display users
        filterUsersByRole();
    }

    private void setCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        dateText.setText(currentDate);
    }

    private void loadAdminData() {
        String userId = auth.getCurrentUser().getUid();

        database.child("users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String firstName = snapshot.child("firstName").getValue(String.class);
                            String lastName = snapshot.child("lastName").getValue(String.class);
                            String role = snapshot.child("role").getValue(String.class);

                            // Check if user is actually an admin
                            if (!"admin".equals(role)) {
                                Toast.makeText(AdminDashboardActivity.this,
                                        "Access denied. Admin only.",
                                        Toast.LENGTH_SHORT).show();
                                navigateToLogin();
                                return;
                            }

                            // Display admin data
                            welcomeText.setText("Welcome, " + firstName + " " + lastName + "!");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AdminDashboardActivity.this,
                                "Error loading admin data",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadAllUsers() {
        database.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                int totalUsers = 0;
                int totalStudents = 0;
                int totalTeachers = 0;
                int totalAdmins = 0;

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = new User();
                    user.setId(userSnapshot.getKey());
                    user.setSchoolId(userSnapshot.child("schoolId").getValue(String.class));
                    user.setFirstName(userSnapshot.child("firstName").getValue(String.class));
                    user.setLastName(userSnapshot.child("lastName").getValue(String.class));
                    user.setEmail(userSnapshot.child("email").getValue(String.class));
                    user.setRole(userSnapshot.child("role").getValue(String.class));
                    user.setDepartment(userSnapshot.child("department").getValue(String.class));
                    user.setYearLevel(userSnapshot.child("yearLevel").getValue(String.class));

                    userList.add(user);
                    totalUsers++;

                    if ("student".equals(user.getRole())) {
                        totalStudents++;
                    } else if ("teacher".equals(user.getRole())) {
                        totalTeachers++;
                    } else if ("admin".equals(user.getRole())) {
                        totalAdmins++;
                    }
                }

                // Update statistics
                totalUsersCount.setText(String.valueOf(totalUsers));
                totalStudentsCount.setText(String.valueOf(totalStudents));
                totalTeachersCount.setText(String.valueOf(totalTeachers));
                totalAdminsCount.setText(String.valueOf(totalAdmins));

                // Filter by current tab
                filterUsersByRole();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDashboardActivity.this,
                        "Error loading users",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterUsersByRole() {
        filteredUserList.clear();

        for (User user : userList) {
            if (currentTab.equals("students") && "student".equals(user.getRole())) {
                filteredUserList.add(user);
            } else if (currentTab.equals("teachers") && "teacher".equals(user.getRole())) {
                filteredUserList.add(user);
            }
        }

        userAdapter.notifyDataSetChanged();
        userCountText.setText(filteredUserList.size() + " " + currentTab);

        // Show/hide empty state
        if (filteredUserList.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            usersRecyclerView.setVisibility(View.GONE);
            emptyStateText.setText("No " + currentTab + " found");
        } else {
            emptyStateText.setVisibility(View.GONE);
            usersRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void filterUsers(String query) {
        filteredUserList.clear();

        if (query.isEmpty()) {
            filterUsersByRole();
            return;
        }

        String lowerCaseQuery = query.toLowerCase();
        for (User user : userList) {
            // Filter by current tab role
            boolean roleMatch = false;
            if (currentTab.equals("students") && "student".equals(user.getRole())) {
                roleMatch = true;
            } else if (currentTab.equals("teachers") && "teacher".equals(user.getRole())) {
                roleMatch = true;
            }

            if (roleMatch) {
                String fullName = (user.getFirstName() + " " + user.getLastName()).toLowerCase();
                String schoolId = user.getSchoolId() != null ? user.getSchoolId().toLowerCase() : "";

                if (fullName.contains(lowerCaseQuery) || schoolId.contains(lowerCaseQuery)) {
                    filteredUserList.add(user);
                }
            }
        }

        userAdapter.notifyDataSetChanged();
        userCountText.setText(filteredUserList.size() + " " + currentTab);
    }

    private void openAddUserDialog() {
        Intent intent = new Intent(this, AddEditUserActivity.class);
        intent.putExtra("defaultRole", currentTab.equals("students") ? "student" : "teacher");
        startActivity(intent);
    }

    private void viewUserDetails(User user) {
        Intent intent = new Intent(this, UserDetailsActivity.class);
        intent.putExtra("userId", user.getId());
        startActivity(intent);
    }

    private void editUser(User user) {
        Intent intent = new Intent(this, AddEditUserActivity.class);
        intent.putExtra("userId", user.getId());
        intent.putExtra("mode", "edit");
        startActivity(intent);
    }

    private void deleteUser(User user) {
        String userType = "teacher".equals(user.getRole()) ? "teacher" : "student";
        new AlertDialog.Builder(this)
                .setTitle("Delete " + (userType.equals("teacher") ? "Teacher" : "Student"))
                .setMessage("Are you sure you want to delete " +
                        user.getFirstName() + " " + user.getLastName() + "?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performDelete(user);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performDelete(User user) {
        database.child("users").child(user.getId()).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "User deleted successfully",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to delete user",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            // Already on dashboard
        } else if (id == R.id.nav_users) {
            // Already showing users
        } else if (id == R.id.nav_attendance) {
            Toast.makeText(this, "Attendance feature coming soon", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_events) {
            Toast.makeText(this, "Events feature coming soon", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            Toast.makeText(this, "Settings feature coming soon", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
            performLogout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void performLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Clear remember me
                        SharedPreferences sharedPref = getSharedPreferences("SaintsGatePrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("remember_me", false);
                        editor.apply();

                        // Sign out from Firebase
                        auth.signOut();

                        Toast.makeText(AdminDashboardActivity.this,
                                "Logged out successfully", Toast.LENGTH_SHORT).show();

                        // Navigate to login
                        navigateToLogin();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}