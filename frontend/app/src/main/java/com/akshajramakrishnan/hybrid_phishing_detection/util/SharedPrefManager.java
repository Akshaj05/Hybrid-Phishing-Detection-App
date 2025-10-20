package com.akshajramakrishnan.hybrid_phishing_detection.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String PREF_NAME = "user_pref";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_REMEMBER = "key_remember";
    private final SharedPreferences prefs;
    private static SharedPrefManager instance;

    public SharedPrefManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefManager getInstance(Context ctx) {
        if (instance == null) instance = new SharedPrefManager(ctx);
        return instance;
    }

    public void setRemembered(String email) {
        prefs.edit().putBoolean(KEY_REMEMBER, true).putString(KEY_EMAIL, email).apply();
    }

    public boolean isRemembered() {
        return prefs.getBoolean(KEY_REMEMBER, false);
    }

    public void setLogin(String email) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_EMAIL, email)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public void logout() {
        prefs.edit().clear().apply();
    }
}
