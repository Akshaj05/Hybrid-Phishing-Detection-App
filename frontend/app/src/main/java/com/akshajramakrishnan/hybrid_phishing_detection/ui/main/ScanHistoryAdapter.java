package com.akshajramakrishnan.hybrid_phishing_detection.ui.main;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.akshajramakrishnan.hybrid_phishing_detection.R;
import com.akshajramakrishnan.hybrid_phishing_detection.data.model.UrlScan;

import java.util.List;

public class ScanHistoryAdapter extends RecyclerView.Adapter<ScanHistoryAdapter.VH> {

    private final List<UrlScan> list;
    private final Context ctx;

    public ScanHistoryAdapter(Context ctx, List<UrlScan> list) {
        this.ctx = ctx; this.list = list;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_url_scan, parent, false));
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        UrlScan s = list.get(position);
        holder.urlText.setText(s.getOriginalUrl());
        holder.verdictText.setText(s.getVerdict() + " • " + s.getScore() + "%");
        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(ctx, ScanResultActivity.class);
            i.putExtra(ScanResultActivity.EXTRA_SCAN_ID, s.getId());
            ctx.startActivity(i);
        });
        holder.itemView.setOnLongClickListener(v -> {
            Toast.makeText(ctx, "Long press options (delete/block) can be added", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView urlText, verdictText;
        VH(View itemView) {
            super(itemView);
            urlText = itemView.findViewById(R.id.urlText);
            verdictText = itemView.findViewById(R.id.verdictText);
        }
    }
}

