package com.akshajramakrishnan.hybrid_phishing_detection;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class ClipboardListenerService extends Service {

    private static final String CHANNEL_ID = "ClipboardMonitorChannel";
    private ClipboardManager clipboardManager;
    private boolean isForeground = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("CLIPBOARD", "Service created");

        createNotificationChannel();

        // 💡 Delay foreground promotion on Android 14+ to avoid ForegroundServiceStartNotAllowedException
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            new Handler(Looper.getMainLooper()).postDelayed(this::tryStartForeground, 3000);
        } else {
            tryStartForeground();
        }

        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboardManager == null) {
            Log.e("CLIPBOARD", "ClipboardManager not available!");
            stopSelf();
            return;
        }

        clipboardManager.addPrimaryClipChangedListener(() -> {
            ClipData clipData = clipboardManager.getPrimaryClip();
            if (clipData != null && clipData.getItemCount() > 0) {
                CharSequence copied = clipData.getItemAt(0).getText();
                if (copied != null) {
                    String text = copied.toString().trim();
                    Log.d("CLIPBOARD", "Copied text: " + text);
                    if (text.matches("(?i)https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+")) {
                        showOverlay(text);
                    }
                }
            }
        });
    }

    private void tryStartForeground() {
        if (isForeground) return;

        Notification notification = buildNotification();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(101, notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
            } else {
                startForeground(101, notification);
            }
            isForeground = true;
            Log.d("CLIPBOARD", "Foreground service started successfully.");
        } catch (Exception e) {
            Log.e("CLIPBOARD", "Failed to start foreground: " + e.getMessage(), e);
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SafeLink Clipboard Monitor")
                .setContentText("Monitoring copied links for quick scanning.")
                .setSmallIcon(R.drawable.ic_search)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Clipboard Monitor",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void showOverlay(String url) {
        Log.d("CLIPBOARD", "Triggering overlay for URL: " + url);

        Intent overlayIntent = new Intent(this, OverlayService.class);
        overlayIntent.putExtra("url", url);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(overlayIntent);
            } else {
                startService(overlayIntent);
            }
        } catch (Exception e) {
            Log.e("CLIPBOARD", "Failed to start overlay: " + e.getMessage(), e);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
