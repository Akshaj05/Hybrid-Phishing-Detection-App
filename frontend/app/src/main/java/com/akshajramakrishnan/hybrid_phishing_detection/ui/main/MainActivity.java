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

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            throw new IllegalStateException("NavHostFragment not found!");
        }

        NavController navController = navHostFragment.getNavController();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Handle any incoming intent
        handleIncomingIntent(getIntent());

        requestOverlayPermission();
        startClipboardService();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent == null) return;

        // ✅ Case 1: Coming from overlay
        if (intent.hasExtra("copied_url")) {
            String url = intent.getStringExtra("copied_url");
            Log.d("MAIN_ACTIVITY", "Received from overlay: " + url);
            viewModel.setIncomingUrl(url);
            return;
        }

        // ✅ Case 2: Coming from external “Open with” (browser intent)
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            String url = intent.getData().toString();
            Log.d("MAIN_ACTIVITY", "Received via ACTION_VIEW: " + url);
            viewModel.setIncomingUrl(url);
        }
    }

    private void startClipboardService() {
        try {
            Intent serviceIntent = new Intent(this, ClipboardListenerService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            Log.d("SERVICE", "ClipboardListenerService started");
        } catch (Exception e) {
            Log.e("SERVICE_ERROR", "Failed to start ClipboardListenerService", e);
        }
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Log.w("PERMISSION", "Overlay permission not granted");
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ);
            }
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
                Log.w("PERMISSION", "Overlay permission denied.");
            }
        }
    }
}
