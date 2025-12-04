package com.example.login;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.login.fragments.CoursesFragment;
import com.example.login.fragments.GradesFragment;
import com.example.login.fragments.HomeFragment;
import com.example.login.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    // Bottom nav layouts
    private LinearLayout navHomeLayout, navCoursesLayout, navGradesLayout, navProfileLayout;
    private ImageView navHomeIcon, navCoursesIcon, navGradesIcon, navProfileIcon;
    private TextView navHomeText, navCoursesText, navGradesText, navProfileText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init bottom nav views
        navHomeLayout = findViewById(R.id.nav_home_layout);
        navCoursesLayout = findViewById(R.id.nav_courses_layout);
        navGradesLayout = findViewById(R.id.nav_grades_layout);
        navProfileLayout = findViewById(R.id.nav_profile_layout);

        navHomeIcon = findViewById(R.id.nav_home);
        navCoursesIcon = findViewById(R.id.nav_courses);
        navGradesIcon = findViewById(R.id.nav_grades);
        navProfileIcon = findViewById(R.id.nav_profile);

        navHomeText = findViewById(R.id.nav_home_text);
        navCoursesText = findViewById(R.id.nav_courses_text);
        navGradesText = findViewById(R.id.nav_grades_text);
        navProfileText = findViewById(R.id.nav_profile_text);

        // Load HomeFragment by default
        replaceFragment(new HomeFragment());
        highlightNav(navHomeLayout);

        // Nav click listeners
        navHomeLayout.setOnClickListener(v -> {
            replaceFragment(new HomeFragment());
            highlightNav(navHomeLayout);
        });

        navCoursesLayout.setOnClickListener(v -> {
            replaceFragment(new CoursesFragment());
            highlightNav(navCoursesLayout);
        });

        navGradesLayout.setOnClickListener(v -> {
            replaceFragment(new GradesFragment());
            highlightNav(navGradesLayout);
        });

        navProfileLayout.setOnClickListener(v -> {
            replaceFragment(new ProfileFragment());
            highlightNav(navProfileLayout);
        });
    }

    // Replace fragment
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    // Highlight selected nav
    private void highlightNav(LinearLayout selectedLayout) {
        // Reset all to default
        navHomeIcon.setColorFilter(getResources().getColor(R.color.gray));
        navCoursesIcon.setColorFilter(getResources().getColor(R.color.gray));
        navGradesIcon.setColorFilter(getResources().getColor(R.color.gray));
        navProfileIcon.setColorFilter(getResources().getColor(R.color.gray));

        navHomeText.setTextColor(getResources().getColor(R.color.gray));
        navCoursesText.setTextColor(getResources().getColor(R.color.gray));
        navGradesText.setTextColor(getResources().getColor(R.color.gray));
        navProfileText.setTextColor(getResources().getColor(R.color.gray));

        // Highlight selected
        if (selectedLayout == navHomeLayout) {
            navHomeIcon.setColorFilter(getResources().getColor(R.color.blue_primary));
            navHomeText.setTextColor(getResources().getColor(R.color.blue_primary));
        } else if (selectedLayout == navCoursesLayout) {
            navCoursesIcon.setColorFilter(getResources().getColor(R.color.blue_primary));
            navCoursesText.setTextColor(getResources().getColor(R.color.blue_primary));
        } else if (selectedLayout == navGradesLayout) {
            navGradesIcon.setColorFilter(getResources().getColor(R.color.blue_primary));
            navGradesText.setTextColor(getResources().getColor(R.color.blue_primary));
        } else if (selectedLayout == navProfileLayout) {
            navProfileIcon.setColorFilter(getResources().getColor(R.color.blue_primary));
            navProfileText.setTextColor(getResources().getColor(R.color.blue_primary));
        }
    }
}
