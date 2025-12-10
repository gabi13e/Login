package com.example.login;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.login.fragments.CoursesFragment;
import com.example.login.fragments.GradesFragment;
import com.example.login.fragments.HomeFragment;
import com.example.login.fragments.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Firebase
    private FirebaseAuth auth;

    // Bottom nav elements
    private LinearLayout navHomeLayout, navCoursesLayout, navGradesLayout, navProfileLayout;
    private ImageView navHomeIcon, navCoursesIcon, navGradesIcon, navProfileIcon;
    private TextView navHomeText, navCoursesText, navGradesText, navProfileText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "MainActivity onCreate started");

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Check if user is logged in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "No user logged in, redirecting to LoginActivity");
            navigateToLogin();
            return;
        }

        Log.d(TAG, "User logged in: " + currentUser.getEmail());

        // Set the new main layout with fragment container
        setContentView(R.layout.activity_main);

        // Initialize navigation views
        initializeNavigation();

        // Load home fragment by default
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            setActiveTab(0);
        }

        // Setup navigation listeners
        setupNavigationListeners();
    }

    private void initializeNavigation() {
        // Navigation layouts
        navHomeLayout = findViewById(R.id.nav_home_layout);
        navCoursesLayout = findViewById(R.id.nav_courses_layout);
        navGradesLayout = findViewById(R.id.nav_grades_layout);
        navProfileLayout = findViewById(R.id.nav_profile_layout);

        // Navigation icons
        navHomeIcon = findViewById(R.id.nav_home);
        navCoursesIcon = findViewById(R.id.nav_courses);
        navGradesIcon = findViewById(R.id.nav_grades);
        navProfileIcon = findViewById(R.id.nav_profile);

        // Navigation text
        navHomeText = findViewById(R.id.nav_home_text);
        navCoursesText = findViewById(R.id.nav_courses_text);
        navGradesText = findViewById(R.id.nav_grades_text);
        navProfileText = findViewById(R.id.nav_profile_text);

        Log.d(TAG, "Navigation views initialized");
    }

    private void setupNavigationListeners() {
        navHomeLayout.setOnClickListener(v -> {
            Log.d(TAG, "Home clicked");
            loadFragment(new HomeFragment());
            setActiveTab(0);
        });

        navCoursesLayout.setOnClickListener(v -> {
            Log.d(TAG, "Courses clicked");
            loadFragment(new CoursesFragment());
            setActiveTab(1);
        });

        navGradesLayout.setOnClickListener(v -> {
            Log.d(TAG, "Grades clicked");
            loadFragment(new GradesFragment());
            setActiveTab(2);
        });

        navProfileLayout.setOnClickListener(v -> {
            Log.d(TAG, "Profile clicked");
            loadFragment(new ProfileFragment());
            setActiveTab(3);
        });

        Log.d(TAG, "Navigation listeners setup complete");
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

        Log.d(TAG, "Fragment loaded: " + fragment.getClass().getSimpleName());
    }

    private void setActiveTab(int position) {
        // Define colors
        int inactiveColor = ContextCompat.getColor(this, R.color.inactive_gray);
        int activeColor = ContextCompat.getColor(this, R.color.blue_primary);

        // Reset all to inactive
        navHomeIcon.setColorFilter(inactiveColor);
        navCoursesIcon.setColorFilter(inactiveColor);
        navGradesIcon.setColorFilter(inactiveColor);
        navProfileIcon.setColorFilter(inactiveColor);

        navHomeText.setTextColor(inactiveColor);
        navCoursesText.setTextColor(inactiveColor);
        navGradesText.setTextColor(inactiveColor);
        navProfileText.setTextColor(inactiveColor);

        navHomeText.setTypeface(null, Typeface.NORMAL);
        navCoursesText.setTypeface(null, Typeface.NORMAL);
        navGradesText.setTypeface(null, Typeface.NORMAL);
        navProfileText.setTypeface(null, Typeface.NORMAL);

        // Set active tab
        switch (position) {
            case 0: // Home
                navHomeIcon.setColorFilter(activeColor);
                navHomeText.setTextColor(activeColor);
                navHomeText.setTypeface(null, Typeface.BOLD);
                Log.d(TAG, "Home tab activated");
                break;
            case 1: // Courses
                navCoursesIcon.setColorFilter(activeColor);
                navCoursesText.setTextColor(activeColor);
                navCoursesText.setTypeface(null, Typeface.BOLD);
                Log.d(TAG, "Courses tab activated");
                break;
            case 2: // Grades
                navGradesIcon.setColorFilter(activeColor);
                navGradesText.setTextColor(activeColor);
                navGradesText.setTypeface(null, Typeface.BOLD);
                Log.d(TAG, "Grades tab activated");
                break;
            case 3: // Profile
                navProfileIcon.setColorFilter(activeColor);
                navProfileText.setTextColor(activeColor);
                navProfileText.setTypeface(null, Typeface.BOLD);
                Log.d(TAG, "Profile tab activated");
                break;
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check authentication status when activity starts
        if (auth.getCurrentUser() == null) {
            Log.d(TAG, "User logged out, redirecting to LoginActivity");
            navigateToLogin();
        }
    }
}