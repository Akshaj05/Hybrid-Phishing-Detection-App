package com.akshajramakrishnan.hybrid_phishing_detection;

import android.app.Application;
import com.akshajramakrishnan.hybrid_phishing_detection.util.ThemeUtils;

public class SafeLinkApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        boolean dark = getSharedPreferences("settings", MODE_PRIVATE)
                .getBoolean("dark_mode", false);
        ThemeUtils.applyTheme(dark);
    }
}