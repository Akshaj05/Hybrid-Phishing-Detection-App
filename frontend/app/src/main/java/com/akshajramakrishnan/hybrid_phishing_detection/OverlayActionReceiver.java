package com.akshajramakrishnan.hybrid_phishing_detection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.akshajramakrishnan.hybrid_phishing_detection.ui.main.MainActivity;

public class OverlayActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("com.akshajramakrishnan.hybrid_phishing_detection.SCAN_URL".equals(action)) {
            String url = intent.getStringExtra("url");
            Log.d("OVERLAY_RECEIVER", "Received URL from overlay: " + url);

            Intent openApp = new Intent(context, MainActivity.class);
            openApp.putExtra("copied_url", url);
            openApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(openApp);
        }
    }
}
