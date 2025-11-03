package com.akshajramakrishnan.hybrid_phishing_detection.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.akshajramakrishnan.hybrid_phishing_detection.ClipboardListenerService;
import com.akshajramakrishnan.hybrid_phishing_detection.R;
import com.akshajramakrishnan.hybrid_phishing_detection.ui.viewmodel.MainViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private static final int OVERLAY_PERMISSION_REQ = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ✅ Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) throw new IllegalStateException("NavHostFragment not found!");
        NavController navController = navHostFragment.getNavController();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);

        // ✅ ViewModel setup
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // ✅ Handle URLs from intents
        handleIncomingUrl(getIntent());

        // ✅ Request overlay permission first
        requestOverlayPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ✅ Start clipboard service only when activity is visible
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
            startClipboardService();
        }
    }

    private void handleIncomingUrl(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            Uri data = intent.getData();
            String incomingUrl = data.toString();
            Log.d("INTENT_URL", "Received external URL: " + incomingUrl);
            if (viewModel != null) viewModel.setIncomingUrl(incomingUrl);
        }

        if (intent != null && intent.hasExtra("copied_url")) {
            String copiedUrl = intent.getStringExtra("copied_url");
            Log.d("MAIN_ACTIVITY", "Received copied URL: " + copiedUrl);
            if (viewModel != null) viewModel.setIncomingUrl(copiedUrl);
        }
    }

    private void startClipboardService() {
        Log.d("SERVICE", "Starting ClipboardListenerService...");
        Intent serviceIntent = new Intent(this, ClipboardListenerService.class);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            Log.d("SERVICE", "ClipboardListenerService started successfully");
        } catch (Exception e) {
            Log.e("SERVICE_ERROR", "Failed to start ClipboardListenerService", e);
        }
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Log.w("PERMISSION", "Overlay permission not granted, requesting...");
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ);
            } else {
                startClipboardService();
            }
        } else {
            startClipboardService();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                Log.d("PERMISSION", "Overlay permission granted.");
                startClipboardService();
            } else {
                Log.w("PERMISSION", "Overlay permission denied by user.");
            }
        }
    }
}
