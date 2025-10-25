package com.akshajramakrishnan.hybrid_phishing_detection.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String PREF = "settings";
    private static final String KEY_UID = "uid";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_DARK = "dark_mode";
    private static final String KEY_AUTO_BLOCK = "auto_block";
    private static final String KEY_BROWSER_PKG = "browser_pkg";

    private final SharedPreferences prefs;

    public SharedPrefManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    // 🔹 Save user session after login
    public void saveUserSession(String uid, String email) {
        prefs.edit()
                .putString(KEY_UID, uid)
                .putString(KEY_EMAIL, email)
                .apply();
    }

    // 🔹 Check if logged in (fix for your LoginActivity)
    public boolean isLoggedIn() {
        return prefs.contains(KEY_UID) && prefs.getString(KEY_UID, "").length() > 0;
    }

    public String getUid() { return prefs.getString(KEY_UID, ""); }
    public String getEmail() { return prefs.getString(KEY_EMAIL, ""); }

    public void logout() {
        prefs.edit()
                .remove(KEY_UID)
                .remove(KEY_EMAIL)
                .apply();
    }

    public void setDarkMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_DARK, enabled).apply();
    }

    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK, false);
    }

    public void setAutoBlockEnabled(boolean val) {
        prefs.edit().putBoolean(KEY_AUTO_BLOCK, val).apply();
    }

    public boolean isAutoBlockEnabled() {
        return prefs.getBoolean(KEY_AUTO_BLOCK, false);
    }

    public void setDefaultBrowserPackage(String pkg) {
        prefs.edit().putString(KEY_BROWSER_PKG, pkg).apply();
    }

    public String getDefaultBrowserPackage() {
        return prefs.getString(KEY_BROWSER_PKG, "");
    }
}
