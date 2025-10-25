package com.akshajramakrishnan.hybrid_phishing_detection.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.akshajramakrishnan.hybrid_phishing_detection.R;
import com.akshajramakrishnan.hybrid_phishing_detection.data.model.BlockedUrl;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BlockedListAdapter extends RecyclerView.Adapter<BlockedListAdapter.ViewHolder> {

    private List<BlockedUrl> blockedList;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    public BlockedListAdapter(List<BlockedUrl> list) {
        this.blockedList = list;
    }

    public void setData(List<BlockedUrl> list) {
        this.blockedList = list;
        notifyDataSetChanged();
    }

    public void clear() {
        blockedList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_blocked_url, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BlockedUrl item = blockedList.get(position);
        holder.urlText.setText(item.getUrl());
        holder.dateText.setText(sdf.format(item.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return blockedList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView urlText, dateText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            urlText = itemView.findViewById(R.id.blockedUrlText);
            dateText = itemView.findViewById(R.id.blockedDateText);
        }
    }
}
