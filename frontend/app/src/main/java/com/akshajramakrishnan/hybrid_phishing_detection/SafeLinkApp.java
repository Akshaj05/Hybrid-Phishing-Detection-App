package com.akshajramakrishnan.hybrid_phishing_detection;

import android.app.Application;
import android.content.SharedPreferences;

import com.akshajramakrishnan.hybrid_phishing_detection.util.ThemeUtils;

public class SafeLinkApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Use Application context safely
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);

        // Apply theme using App context (Activity = null)
        ThemeUtils.applyTheme(darkMode, this);
    }
}

