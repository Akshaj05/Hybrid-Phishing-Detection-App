package com.akshajramakrishnan.hybrid_phishing_detection;

import android.app.Service;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class OverlayService extends Service {
    private WindowManager windowManager;
    private View overlayView;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String copiedUrl = intent.getStringExtra("url");
        if (copiedUrl == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        showOverlay(copiedUrl);
        return START_STICKY;
    }

    private void showOverlay(String url) {
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
            Intent broadcastIntent = new Intent("com.akshajramakrishnan.hybrid_phishing_detection.SCAN_URL");
            broadcastIntent.putExtra("url", url);
            sendBroadcast(broadcastIntent);
            removeOverlay();
        });

        overlayView.setOnClickListener(v -> removeOverlay());

        windowManager.addView(overlayView, params);
        Log.d("OVERLAY_SERVICE", "Overlay displayed for URL: " + url);
    }

    private void removeOverlay() {
        if (windowManager != null && overlayView != null) {
            windowManager.removeView(overlayView);
            overlayView = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeOverlay();
    }
}
