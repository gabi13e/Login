package com.example.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

public class ThemeManager {

    private static ThemeManager instance;
    private static final String PREFS_NAME = "ThemePrefs";
    private static final String KEY_DEPARTMENT = "user_department";

    private String currentDepartment;
    private DepartmentTheme currentTheme;

    private ThemeManager() {
        // Private constructor for singleton
    }

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    // Save department to SharedPreferences
    public void setDepartment(Context context, String department) {
        this.currentDepartment = department;
        this.currentTheme = getDepartmentTheme(department);

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_DEPARTMENT, department).apply();
    }

    // Load department from SharedPreferences
    public void loadDepartment(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.currentDepartment = prefs.getString(KEY_DEPARTMENT, "General");
        this.currentTheme = getDepartmentTheme(currentDepartment);
    }

    // Get current department
    public String getCurrentDepartment() {
        return currentDepartment != null ? currentDepartment : "General";
    }

    // Get primary color
    public int getPrimaryColor() {
        return currentTheme != null ? currentTheme.primaryColor : Color.parseColor("#667eea");
    }

    // Get secondary color
    public int getSecondaryColor() {
        return currentTheme != null ? currentTheme.secondaryColor : Color.parseColor("#764ba2");
    }

    // Get accent color
    public int getAccentColor() {
        return currentTheme != null ? currentTheme.accentColor : Color.parseColor("#f093fb");
    }

    // Get light color (for backgrounds)
    public int getLightColor() {
        return currentTheme != null ? currentTheme.lightColor : Color.parseColor("#F0E6FF");
    }

    // Create gradient drawable for headers
    public GradientDrawable createGradientDrawable() {
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{getPrimaryColor(), getSecondaryColor(), getAccentColor()}
        );
        gradient.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        return gradient;
    }

    // Department theme mapping based on your school's departments
    private DepartmentTheme getDepartmentTheme(String department) {
        if (department == null) department = "General";

        // Convert to lowercase for case-insensitive matching
        String dept = department.toLowerCase().trim();

        switch (dept) {
            case "senior high":
            case "senior highschool":
            case "senior high school":
            case "snr high":
            case "shs":
                // Khaki/Tan theme
                return new DepartmentTheme(
                        Color.parseColor("#9E8B65"), // Dark Khaki
                        Color.parseColor("#C4A777"), // Khaki
                        Color.parseColor("#D4B896"), // Light Khaki
                        Color.parseColor("#F5F0E8")  // Very Light Khaki
                );

            case "information technology":
            case "it":
                // Violet theme
                return new DepartmentTheme(
                        Color.parseColor("#6A1B9A"), // Dark Violet
                        Color.parseColor("#8E24AA"), // Violet
                        Color.parseColor("#AB47BC"), // Light Violet
                        Color.parseColor("#F3E5F5")  // Very Light Violet
                );

            case "engineering":
                // Orange theme
                return new DepartmentTheme(
                        Color.parseColor("#E65100"), // Dark Orange
                        Color.parseColor("#FF6F00"), // Orange
                        Color.parseColor("#FF8F00"), // Light Orange
                        Color.parseColor("#FFF3E0")  // Very Light Orange
                );

            case "business administration":
            case "ba":
                // Yellow theme
                return new DepartmentTheme(
                        Color.parseColor("#F57F17"), // Dark Yellow/Gold
                        Color.parseColor("#FBC02D"), // Yellow
                        Color.parseColor("#FFEB3B"), // Light Yellow
                        Color.parseColor("#FFFDE7")  // Very Light Yellow
                );

            case "teacher education":
            case "teacher edu":
            case "education":
                // Blue theme
                return new DepartmentTheme(
                        Color.parseColor("#1565C0"), // Dark Blue
                        Color.parseColor("#1976D2"), // Blue
                        Color.parseColor("#42A5F5"), // Light Blue
                        Color.parseColor("#E3F2FD")  // Very Light Blue
                );

            case "physical education":
            case "pe":
                // Gray theme
                return new DepartmentTheme(
                        Color.parseColor("#424242"), // Dark Gray
                        Color.parseColor("#616161"), // Gray
                        Color.parseColor("#9E9E9E"), // Light Gray
                        Color.parseColor("#F5F5F5")  // Very Light Gray
                );

            case "nursing":
                // Green theme
                return new DepartmentTheme(
                        Color.parseColor("#2E7D32"), // Dark Green
                        Color.parseColor("#388E3C"), // Green
                        Color.parseColor("#66BB6A"), // Light Green
                        Color.parseColor("#E8F5E9")  // Very Light Green
                );

            case "criminology":
            case "criminal justice":
            case "crimjustice":
                // Black/Dark theme
                return new DepartmentTheme(
                        Color.parseColor("#000000"), // Black
                        Color.parseColor("#212121"), // Very Dark Gray
                        Color.parseColor("#424242"), // Dark Gray
                        Color.parseColor("#EEEEEE")  // Light Gray background
                );

            case "hospitality management":
            case "hm":
            case "hospitality":
                // Pink theme
                return new DepartmentTheme(
                        Color.parseColor("#C2185B"), // Dark Pink
                        Color.parseColor("#E91E63"), // Pink
                        Color.parseColor("#F06292"), // Light Pink
                        Color.parseColor("#FCE4EC")  // Very Light Pink
                );

            default: // General/Other departments
                return new DepartmentTheme(
                        Color.parseColor("#667eea"), // Purple Blue
                        Color.parseColor("#764ba2"), // Purple
                        Color.parseColor("#f093fb"), // Pink Purple
                        Color.parseColor("#F0E6FF")  // Very Light Purple
                );
        }
    }

    // Inner class to hold theme colors
    private static class DepartmentTheme {
        int primaryColor;
        int secondaryColor;
        int accentColor;
        int lightColor;

        DepartmentTheme(int primary, int secondary, int accent, int light) {
            this.primaryColor = primary;
            this.secondaryColor = secondary;
            this.accentColor = accent;
            this.lightColor = light;
        }
    }

    // Clear theme (for logout)
    public void clearTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        currentDepartment = null;
        currentTheme = null;
    }
}