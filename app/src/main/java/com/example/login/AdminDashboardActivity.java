package com.example.login;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView totalUsersCount, totalStudentsCount, totalTeachersCount, totalCoursesCount;
    private TextView welcomeText, dateText;
    private TextView studentsTab, teachersTab;
    private TextView managementTitle, listTitle, userCountText;
    private EditText searchInput;
    private Button filterButton, addUserButton, refreshButton;
    private RecyclerView usersRecyclerView;
    private TextView emptyStateText;
    private Spinner sortSpinner;

    // Firebase
    private DatabaseReference database;
    private DatabaseReference usersRef;
    private DatabaseReference coursesRef;
    private ValueEventListener usersListener;
    private ValueEventListener coursesListener;

    private Handler dateHandler;
    private Runnable dateRunnable;


    // Data
    private List<User> allUsers = new ArrayList<>();
    private List<User> filteredUsers = new ArrayList<>();
    private UserAdapter userAdapter;
    private boolean isStudentTabActive = true;
    private String selectedYearFilter = "All";
    private String currentSortOption = "Name (A-Z)";

    // Filter buttons
    private Button btnFilterAll, btnFilter1st, btnFilter2nd, btnFilter3rd, btnFilter4th, btnFilter5th;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().getReference();
        usersRef = database.child("users");
        coursesRef = database.child("courses");

        initializeViews();
        setupNavigationDrawer();
        setupTabs();
        setupSearchAndFilter();
        setupSortSpinner();
        setupRecyclerView();
        setCurrentDate();
        setupCoursesCard();

        // Load data from Firebase with real-time listeners
        loadUsersFromFirebase();
        loadCoursesCount();
    }

    private void initializeViews() {
        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Date and Welcome
        welcomeText = findViewById(R.id.welcome_text);
        dateText = findViewById(R.id.date_text);

        // Stats
        totalUsersCount = findViewById(R.id.total_users_count);
        totalStudentsCount = findViewById(R.id.total_students_count);
        totalTeachersCount = findViewById(R.id.total_teachers_count);
        totalCoursesCount = findViewById(R.id.total_courses_count);

        // Tabs
        studentsTab = findViewById(R.id.students_tab);
        teachersTab = findViewById(R.id.teachers_tab);
        managementTitle = findViewById(R.id.management_title);
        listTitle = findViewById(R.id.list_title);
        userCountText = findViewById(R.id.user_count_text);

        // Search & Filter
        searchInput = findViewById(R.id.search_input);
        filterButton = findViewById(R.id.filter_button);

        // Filter buttons
        btnFilterAll = findViewById(R.id.btn_filter_all);
        btnFilter1st = findViewById(R.id.btn_filter_1st);
        btnFilter2nd = findViewById(R.id.btn_filter_2nd);
        btnFilter3rd = findViewById(R.id.btn_filter_3rd);
        btnFilter4th = findViewById(R.id.btn_filter_4th);
        btnFilter5th = findViewById(R.id.btn_filter_5th);

        // Action buttons
        addUserButton = findViewById(R.id.add_user_button);
        refreshButton = findViewById(R.id.refresh_button);

        // RecyclerView & empty state
        usersRecyclerView = findViewById(R.id.users_recycler_view);
        emptyStateText = findViewById(R.id.empty_state_text);

        // Sort spinner
        sortSpinner = findViewById(R.id.sort_spinner);

        // Setup menu icon to open drawer
        findViewById(R.id.menu_icon).setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );
    }

    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_logout) {
                    showLogoutConfirmDialog();
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    private void showLogoutConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Logout");
        builder.setMessage("Are you sure you want to logout?");

        builder.setPositiveButton("Yes, Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                performLogout();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        });
    }

    private void performLogout() {
        Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void setupCoursesCard() {
        CardView manageCoursesCard = findViewById(R.id.manage_courses_card);

        // Set click listener for manage courses card
        manageCoursesCard.setOnClickListener(v -> {
            Toast.makeText(AdminDashboardActivity.this, "Manage Courses - Coming Soon!", Toast.LENGTH_SHORT).show();
        });

        // Set click listener for manage users card
        CardView manageUsersCard = findViewById(R.id.manage_users_card);
        manageUsersCard.setOnClickListener(v -> {
            Toast.makeText(AdminDashboardActivity.this, "Manage Users - Coming Soon!", Toast.LENGTH_SHORT).show();
        });

        // Notification icon
        findViewById(R.id.notification_icon).setOnClickListener(v ->
                Toast.makeText(this, "Notifications coming soon!", Toast.LENGTH_SHORT).show()
        );
    }

    private void loadCoursesCount() {
        // Real-time listener for courses count
        coursesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int coursesCount = 0;
                if (snapshot.exists()) {
                    coursesCount = (int) snapshot.getChildrenCount();
                }
                totalCoursesCount.setText(String.valueOf(coursesCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDashboardActivity.this,
                        "Failed to load courses count: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                totalCoursesCount.setText("0");
            }
        };
        coursesRef.addValueEventListener(coursesListener);
    }

    private void setupTabs() {
        studentsTab.setOnClickListener(v -> switchTab(true));
        teachersTab.setOnClickListener(v -> switchTab(false));
    }

    private void setupSearchAndFilter() {
        // Search functionality
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAndDisplayUsers();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter button toggle
        filterButton.setOnClickListener(v -> {
            View filterContainer = findViewById(R.id.filter_container);
            if (filterContainer.getVisibility() == View.GONE) {
                filterContainer.setVisibility(View.VISIBLE);
            } else {
                filterContainer.setVisibility(View.GONE);
            }
        });

        // Year level filter buttons
        btnFilterAll.setOnClickListener(v -> selectYearFilter("All", btnFilterAll));
        btnFilter1st.setOnClickListener(v -> selectYearFilter("1st Year", btnFilter1st));
        btnFilter2nd.setOnClickListener(v -> selectYearFilter("2nd Year", btnFilter2nd));
        btnFilter3rd.setOnClickListener(v -> selectYearFilter("3rd Year", btnFilter3rd));
        btnFilter4th.setOnClickListener(v -> selectYearFilter("4th Year", btnFilter4th));
        btnFilter5th.setOnClickListener(v -> selectYearFilter("5th Year", btnFilter5th));

        // Action buttons
        addUserButton.setOnClickListener(v -> {
            if (isStudentTabActive) {
                Intent intent = new Intent(AdminDashboardActivity.this, AddStudentActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(AdminDashboardActivity.this, AddTeacherActivity.class);
                startActivity(intent);
            }
        });

        refreshButton.setOnClickListener(v -> {
            Toast.makeText(this, "Data is already real-time!", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSortSpinner() {
        String[] sortOptions = {"Name (A-Z)", "Name (Z-A)", "ID (Ascending)", "ID (Descending)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSortOption = sortOptions[position];
                filterAndDisplayUsers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRecyclerView() {
        userAdapter = new UserAdapter(filteredUsers, this);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(userAdapter);
    }

    private void loadUsersFromFirebase() {
        // Remove previous listener if exists
        if (usersListener != null) {
            usersRef.removeEventListener(usersListener);
        }

        usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allUsers.clear();

                int studentCount = 0;
                int teacherCount = 0;
                int adminCount = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        user.setUserId(snapshot.getKey());
                        allUsers.add(user);

                        // Count by role (case-insensitive)
                        String role = user.getRole();
                        if (role != null) {
                            String roleLower = role.toLowerCase().trim();
                            if (roleLower.equals("student")) {
                                studentCount++;
                            } else if (roleLower.equals("teacher")) {
                                teacherCount++;
                            } else if (roleLower.equals("admin")) {
                                adminCount++;
                            }
                        }
                    }
                }

                // Update statistics
                updateStatistics(studentCount, teacherCount, adminCount);

                // Filter and display users
                filterAndDisplayUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AdminDashboardActivity.this,
                        "Failed to load users: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        usersRef.addValueEventListener(usersListener);
    }

    private void updateStatistics(int students, int teachers, int admins) {
        int totalUsers = students + teachers; // Only students and teachers, not admins

        totalUsersCount.setText(String.valueOf(totalUsers));
        totalStudentsCount.setText(String.valueOf(students));
        totalTeachersCount.setText(String.valueOf(teachers));
    }

    private void filterAndDisplayUsers() {
        filteredUsers.clear();
        String searchQuery = searchInput.getText().toString().toLowerCase().trim();

        for (User user : allUsers) {
            // Filter by role (student or teacher) - case-insensitive
            boolean roleMatches = false;
            String userRole = user.getRole();
            if (userRole != null) {
                String roleLower = userRole.toLowerCase().trim();
                if (isStudentTabActive && roleLower.equals("student")) {
                    roleMatches = true;
                } else if (!isStudentTabActive && roleLower.equals("teacher")) {
                    roleMatches = true;
                }
            }

            if (!roleMatches) continue;

            // Filter by year level (only for students)
            if (isStudentTabActive && !selectedYearFilter.equals("All")) {
                if (user.getYearLevel() == null || !user.getYearLevel().equalsIgnoreCase(selectedYearFilter)) {
                    continue;
                }
            }

            // Filter by search query
            if (!searchQuery.isEmpty()) {
                String name = user.getFullName().toLowerCase();
                String schoolId = user.getSchoolId() != null ? user.getSchoolId().toLowerCase() : "";
                String userId = user.getUserId() != null ? user.getUserId().toLowerCase() : "";

                if (!name.contains(searchQuery) && !schoolId.contains(searchQuery) && !userId.contains(searchQuery)) {
                    continue;
                }
            }

            filteredUsers.add(user);
        }

        // Sort the filtered list
        sortUsers();

        // Update UI
        if (filteredUsers.isEmpty()) {
            usersRecyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText("No " + (isStudentTabActive ? "students" : "teachers") + " found");
        } else {
            usersRecyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        }

        userCountText.setText(filteredUsers.size() + " " + (isStudentTabActive ? "students" : "teachers"));
        userAdapter.notifyDataSetChanged();
    }

    private void sortUsers() {
        switch (currentSortOption) {
            case "Name (A-Z)":
                Collections.sort(filteredUsers, (u1, u2) -> {
                    String name1 = u1.getFullName();
                    String name2 = u2.getFullName();
                    return name1.compareToIgnoreCase(name2);
                });
                break;
            case "Name (Z-A)":
                Collections.sort(filteredUsers, (u1, u2) -> {
                    String name1 = u1.getFullName();
                    String name2 = u2.getFullName();
                    return name2.compareToIgnoreCase(name1);
                });
                break;
            case "ID (Ascending)":
                Collections.sort(filteredUsers, (u1, u2) -> {
                    String id1 = u1.getSchoolId() != null ? u1.getSchoolId() : "";
                    String id2 = u2.getSchoolId() != null ? u2.getSchoolId() : "";
                    return id1.compareToIgnoreCase(id2);
                });
                break;
            case "ID (Descending)":
                Collections.sort(filteredUsers, (u1, u2) -> {
                    String id1 = u1.getSchoolId() != null ? u1.getSchoolId() : "";
                    String id2 = u2.getSchoolId() != null ? u2.getSchoolId() : "";
                    return id2.compareToIgnoreCase(id1);
                });
                break;
        }
    }

    private void selectYearFilter(String yearLevel, Button selectedButton) {
        selectedYearFilter = yearLevel;

        // Reset all buttons
        resetFilterButton(btnFilterAll);
        resetFilterButton(btnFilter1st);
        resetFilterButton(btnFilter2nd);
        resetFilterButton(btnFilter3rd);
        resetFilterButton(btnFilter4th);
        resetFilterButton(btnFilter5th);

        // Highlight selected button
        selectedButton.setBackgroundResource(R.drawable.filter_chip_background);
        selectedButton.setTextColor(getResources().getColor(R.color.blue_primary));

        // Filter and display
        filterAndDisplayUsers();
    }

    private void resetFilterButton(Button button) {
        button.setBackgroundResource(R.drawable.filter_chip_background_inactive);
        button.setTextColor(getResources().getColor(android.R.color.darker_gray));
    }

    private void setCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        dateText.setText(currentDate);
    }

    private void startRealtimeDateUpdate() {
        dateHandler = new Handler(Looper.getMainLooper());
        dateRunnable = new Runnable() {
            @Override
            public void run() {
                setCurrentDate();
                // Update every minute (60000 milliseconds)
                dateHandler.postDelayed(this, 60000);
            }
        };
        // Start the updates
        dateHandler.post(dateRunnable);
    }

    private void stopRealtimeDateUpdate() {
        if (dateHandler != null && dateRunnable != null) {
            dateHandler.removeCallbacks(dateRunnable);
        }
    }

    private void switchTab(boolean isStudentTab) {
        isStudentTabActive = isStudentTab;

        if (isStudentTab) {
            studentsTab.setBackgroundResource(R.drawable.active_tab_background);
            studentsTab.setTextColor(getResources().getColor(android.R.color.white));
            teachersTab.setBackgroundResource(android.R.color.transparent);
            teachersTab.setTextColor(getResources().getColor(R.color.blue_primary));

            managementTitle.setText("Student Management");
            listTitle.setText("Student List");
            addUserButton.setText("Add Student");

            // Show year filter for students
            findViewById(R.id.filter_container).setVisibility(View.GONE);
            filterButton.setVisibility(View.VISIBLE);
        } else {
            teachersTab.setBackgroundResource(R.drawable.active_tab_background);
            teachersTab.setTextColor(getResources().getColor(android.R.color.white));
            studentsTab.setBackgroundResource(android.R.color.transparent);
            studentsTab.setTextColor(getResources().getColor(R.color.blue_primary));

            managementTitle.setText("Teacher Management");
            listTitle.setText("Teacher List");
            addUserButton.setText("Add Teacher");

            // Hide year filter for teachers
            findViewById(R.id.filter_container).setVisibility(View.GONE);
            filterButton.setVisibility(View.GONE);
        }

        // Reset filter
        selectedYearFilter = "All";
        selectYearFilter("All", btnFilterAll);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();

            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                findViewById(R.id.scroll_view_or_main_content).setVisibility(View.VISIBLE);
                findViewById(R.id.fragment_container).setVisibility(View.GONE);
            }
        } else if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop real-time date updates
        stopRealtimeDateUpdate();

        // Remove all listeners to prevent memory leaks
        if (usersListener != null && usersRef != null) {
            usersRef.removeEventListener(usersListener);
        }
        if (coursesListener != null && coursesRef != null) {
            coursesRef.removeEventListener(coursesListener);
        }
    }}