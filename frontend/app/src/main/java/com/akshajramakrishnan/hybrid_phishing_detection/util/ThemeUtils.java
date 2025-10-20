package com.akshajramakrishnan.hybrid_phishing_detection.util;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeUtils {
    public static void applyTheme(boolean darkMode) {
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}