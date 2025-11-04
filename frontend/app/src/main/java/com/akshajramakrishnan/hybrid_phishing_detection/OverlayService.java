package com.akshajramakrishnan.hybrid_phishing_detection;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class OverlayService extends Service {
    private WindowManager windowManager;
    private View overlayView;
    private static final String CHANNEL_ID = "OverlayPromptChannel";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        // Immediately start foreground to avoid crash
        startForeground(200, buildNotification());
        Log.d("OVERLAY_SERVICE", "Foreground started successfully");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String copiedUrl = intent != null ? intent.getStringExtra("url") : null;
        if (copiedUrl == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        showOverlay(copiedUrl);
        return START_STICKY;
    }

    private void showOverlay(String url) {
        try {
            if (windowManager != null && overlayView != null) return;

            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            int layoutFlag = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    : WindowManager.LayoutParams.TYPE_PHONE;

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    layoutFlag,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            params.y = 150;

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            overlayView = inflater.inflate(R.layout.overlay_scan_prompt, null);

            TextView textView = overlayView.findViewById(R.id.overlayText);
            textView.setText("Scan copied link?");

            Button scanBtn = overlayView.findViewById(R.id.scanBtn);
            scanBtn.setOnClickListener(v -> {
                Log.d("OVERLAY_SERVICE", "Scan button clicked: " + url);

                Intent broadcastIntent = new Intent("com.akshajramakrishnan.hybrid_phishing_detection.SCAN_URL");
                broadcastIntent.setPackage(getPackageName());
                broadcastIntent.putExtra("url", url);

                sendBroadcast(broadcastIntent);
                Log.d("OVERLAY_SERVICE", "Broadcast sent to OverlayActionReceiver");

                removeOverlay();
                stopSelf();
            });


            overlayView.setOnClickListener(v -> removeOverlay());

            windowManager.addView(overlayView, params);
            Log.d("OVERLAY_SERVICE", "Overlay displayed for URL: " + url);

        } catch (Exception e) {
            Log.e("OVERLAY_SERVICE", "Error displaying overlay", e);
        }
    }

    private void removeOverlay() {
        if (windowManager != null && overlayView != null) {
            try {
                windowManager.removeView(overlayView);
                overlayView = null;
                Log.d("OVERLAY_SERVICE", "Overlay removed");
            } catch (Exception e) {
                Log.e("OVERLAY_SERVICE", "Error removing overlay", e);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeOverlay();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Overlay Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SafeLink Overlay Active")
                .setContentText("Tap 'Scan' to analyze the copied URL.")
                .setSmallIcon(R.drawable.ic_search)
                .setOngoing(true)
                .build();
    }
}
