package com.akshajramakrishnan.hybrid_phishing_detection.ui.main;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akshajramakrishnan.hybrid_phishing_detection.R;
import com.akshajramakrishnan.hybrid_phishing_detection.data.local.AppDatabase;
import com.akshajramakrishnan.hybrid_phishing_detection.data.model.UrlScan;
import com.akshajramakrishnan.hybrid_phishing_detection.util.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ScanHistoryActivity extends Fragment {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private final List<UrlScan> scans = new ArrayList<>();
    SharedPrefManager pref;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_scan_history, container, false);
        recyclerView = v.findViewById(R.id.historyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        recyclerView.setPadding(8, 8, 8, 8);
        recyclerView.setClipToPadding(false);

        db = AppDatabase.getInstance(requireContext());
        pref = new SharedPrefManager(requireContext());
        adapter = new HistoryAdapter(scans);
        recyclerView.setAdapter(adapter);

        loadHistory();
        return v;
    }

    private void loadHistory() {
        String uid = pref.getUid(); // Make sure you have SharedPrefManager initialized
        if (uid == null || uid.isEmpty()) {
            uid = "guest"; // fallback, optional
        }

        String finalUid = uid;
        Executors.newSingleThreadExecutor().execute(() -> {
            List<UrlScan> list = db.urlScanDao().getScansForUser(finalUid); // ✅ filter by user

            requireActivity().runOnUiThread(() -> {
                scans.clear();
                scans.addAll(list);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {

        private final List<UrlScan> data;

        HistoryAdapter(List<UrlScan> data) { this.data = data; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_url_scan, parent, false);
            return new VH(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            UrlScan scan = data.get(position);
            holder.urlText.setText(scan.getOriginalUrl());
            holder.verdictText.setText("Verdict: " + capitalize(scan.getVerdict()));
            holder.dateText.setText(android.text.format.DateFormat.format("dd MMM yyyy HH:mm", scan.getTimestamp()));
            holder.scoreText.setText(scan.getScore() + "%");

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ScanResultActivity.class);
                intent.putExtra(ScanResultActivity.EXTRA_SCAN_ID, scan.getId());
                startActivity(intent);
            });

            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Scan")
                        .setMessage("Do you want to delete this scan entry?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            Executors.newSingleThreadExecutor().execute(() -> {
                                db.urlScanDao().deleteScan(scan);
                                requireActivity().runOnUiThread(() -> {
                                    data.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(requireContext(), "Scan deleted", Toast.LENGTH_SHORT).show();
                                });
                            });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView urlText, verdictText, scoreText, dateText;

            VH(@NonNull View itemView) {
                super(itemView);
                urlText = itemView.findViewById(R.id.urlText);
                verdictText = itemView.findViewById(R.id.verdictText);
                scoreText = itemView.findViewById(R.id.scoreText);
                dateText = itemView.findViewById(R.id.dateText);
            }
        }

        private String capitalize(String s) {
            if (s == null || s.isEmpty()) return s;
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        }
    }
}
