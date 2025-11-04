package com.akshajramakrishnan.hybrid_phishing_detection.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "blocked_urls")
public class BlockedUrl {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String url;
    private String uid;
    private long timestamp;

    public BlockedUrl(String url, String uid, long timestamp) {
        this.url = url;
        this.uid = uid;
        this.timestamp = timestamp;
    }

    public BlockedUrl() {}

    //getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
