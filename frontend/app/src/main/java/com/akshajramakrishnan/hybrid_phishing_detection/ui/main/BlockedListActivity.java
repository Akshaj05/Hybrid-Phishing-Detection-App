package com.akshajramakrishnan.hybrid_phishing_detection.ui.main;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akshajramakrishnan.hybrid_phishing_detection.R;
import com.akshajramakrishnan.hybrid_phishing_detection.data.local.AppDatabase;
import com.akshajramakrishnan.hybrid_phishing_detection.data.model.BlockedUrl;
import com.akshajramakrishnan.hybrid_phishing_detection.util.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class BlockedListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BlockedListAdapter adapter;
    private Button clearAllBtn;
    private SharedPrefManager pref;
    private AppDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_list);

        ImageButton backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.blockedRecyclerView);
        clearAllBtn = findViewById(R.id.btnClearBlocked);
        pref = new SharedPrefManager(this);
        db = AppDatabase.getInstance(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BlockedListAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        loadBlockedUrls();

        clearAllBtn.setOnClickListener(v -> {
            String uid = pref.getUid();
            if (uid != null) {
                AsyncTask.execute(() -> {
                    db.blockedUrlDao().clearAll(uid);
                    runOnUiThread(() -> {
                        adapter.clear();
                        Toast.makeText(this, "Cleared all blocked URLs", Toast.LENGTH_SHORT).show();
                    });
                });
            } else {
                Toast.makeText(this, "Login required", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBlockedUrls() {
        String uid = pref.getUid();
        if (uid == null) {
            Toast.makeText(this, "Login required", Toast.LENGTH_SHORT).show();
            return;
        }

        AsyncTask.execute(() -> {
            List<BlockedUrl> list = db.blockedUrlDao().getBlockedUrls(uid);
            runOnUiThread(() -> {
                adapter.setData(list);
                if (list.isEmpty()) {
                    Toast.makeText(this, "No blocked URLs", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
