package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.login.fragments.TeacherDashboardFragment;
import com.example.login.fragments.TeacherCoursesFragment;
import com.example.login.fragments.TeacherStudentsFragment;
import com.example.login.fragments.TeacherProfileFragment;
import com.google.firebase.auth.FirebaseAuth;

public class TeacherMainActivity extends AppCompatActivity {

    private static final String TAG = "TeacherMainActivity";

    // Navigation layouts
    private LinearLayout navDashboardLayout;
    private LinearLayout navCoursesLayout;
    private LinearLayout navStudentsLayout;
    private LinearLayout navProfileLayout;

    // Navigation icons
    private ImageView navDashboard;
    private ImageView navCourses;
    private ImageView navStudents;
    private ImageView navProfile;

    // Navigation texts
    private TextView navDashboardText;
    private TextView navCoursesText;
    private TextView navStudentsText;
    private TextView navProfileText;

    // Colors
    private int activeColor;
    private int inactiveColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_main);

        Log.d(TAG, "TeacherMainActivity created");

        // Initialize colors
        activeColor = ContextCompat.getColor(this, android.R.color.holo_purple);
        inactiveColor = ContextCompat.getColor(this, android.R.color.darker_gray);

        // Initialize navigation views
        initializeNavigationViews();

        // Setup click listeners
        setupNavigationListeners();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new TeacherDashboardFragment());
            setActiveNavItem(0); // Dashboard is active by default
        }
    }

    private void initializeNavigationViews() {
        // Navigation layouts
        navDashboardLayout = findViewById(R.id.nav_dashboard_layout);
        navCoursesLayout = findViewById(R.id.nav_courses_layout);
        navStudentsLayout = findViewById(R.id.nav_students_layout);
        navProfileLayout = findViewById(R.id.nav_profile_layout);

        // Navigation icons
        navDashboard = findViewById(R.id.nav_dashboard);
        navCourses = findViewById(R.id.nav_courses);
        navStudents = findViewById(R.id.nav_students);
        navProfile = findViewById(R.id.nav_profile);

        // Navigation texts
        navDashboardText = findViewById(R.id.nav_dashboard_text);
        navCoursesText = findViewById(R.id.nav_courses_text);
        navStudentsText = findViewById(R.id.nav_students_text);
        navProfileText = findViewById(R.id.nav_profile_text);

        Log.d(TAG, "Navigation views initialized");
    }

    private void setupNavigationListeners() {
        navDashboardLayout.setOnClickListener(v -> {
            Log.d(TAG, "Dashboard clicked");
            loadFragment(new TeacherDashboardFragment());
            setActiveNavItem(0);
        });

        navCoursesLayout.setOnClickListener(v -> {
            Log.d(TAG, "Courses clicked");
            loadFragment(new TeacherCoursesFragment());
            setActiveNavItem(1);
        });

        navStudentsLayout.setOnClickListener(v -> {
            Log.d(TAG, "Students clicked");
            loadFragment(new TeacherStudentsFragment());
            setActiveNavItem(2);
        });

        navProfileLayout.setOnClickListener(v -> {
            Log.d(TAG, "Profile clicked");
            // CHANGED: Load TeacherProfileFragment instead of ProfileFragment
            loadFragment(new TeacherProfileFragment());
            setActiveNavItem(3);
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void setActiveNavItem(int position) {
        // Reset all to inactive state
        setNavItemInactive(navDashboard, navDashboardText);
        setNavItemInactive(navCourses, navCoursesText);
        setNavItemInactive(navStudents, navStudentsText);
        setNavItemInactive(navProfile, navProfileText);

        // Set selected item to active state
        switch (position) {
            case 0: // Dashboard
                setNavItemActive(navDashboard, navDashboardText);
                break;
            case 1: // Courses
                setNavItemActive(navCourses, navCoursesText);
                break;
            case 2: // Students
                setNavItemActive(navStudents, navStudentsText);
                break;
            case 3: // Profile
                setNavItemActive(navProfile, navProfileText);
                break;
        }
    }

    private void setNavItemActive(ImageView icon, TextView text) {
        icon.setColorFilter(activeColor);
        text.setTextColor(activeColor);
        text.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void setNavItemInactive(ImageView icon, TextView text) {
        icon.setColorFilter(inactiveColor);
        text.setTextColor(inactiveColor);
        text.setTypeface(null, android.graphics.Typeface.NORMAL);
    }

    private void logout() {
        Log.d(TAG, "Logging out");

        // Clear theme
        com.example.login.ThemeManager.getInstance().clearTheme(this);

        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();

        // Clear remember me
        getSharedPreferences("SaintsGatePrefs", MODE_PRIVATE)
                .edit()
                .putBoolean("remember_me", false)
                .apply();

        // Navigate to login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        // Check if we're on the dashboard fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof TeacherDashboardFragment) {
            // If on dashboard, minimize app instead of going back
            moveTaskToBack(true);
        } else {
            // If on other fragment, go back to dashboard
            loadFragment(new TeacherDashboardFragment());
            setActiveNavItem(0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "TeacherMainActivity resumed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "TeacherMainActivity paused");
    }
}