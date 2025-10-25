package com.akshajramakrishnan.hybrid_phishing_detection.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "url_scans")
public class UrlScan {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "user_id")
    private String userId;

    private String originalUrl;
    private String finalUrl;
    private String verdict;
    private int score;
    private double mlProb;
    private long timestamp;

    // ✅ Full constructor including userId
    public UrlScan(String userId, String originalUrl, String finalUrl, String verdict,
                   int score, double mlProb, long timestamp) {
        this.userId = userId;
        this.originalUrl = originalUrl;
        this.finalUrl = finalUrl;
        this.verdict = verdict;
        this.score = score;
        this.mlProb = mlProb;
        this.timestamp = timestamp;
    }

    // ✅ No-arg constructor for Room
    public UrlScan() {}

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public String getFinalUrl() { return finalUrl; }
    public void setFinalUrl(String finalUrl) { this.finalUrl = finalUrl; }

    public String getVerdict() { return verdict; }
    public void setVerdict(String verdict) { this.verdict = verdict; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public double getMlProb() { return mlProb; }
    public void setMlProb(double mlProb) { this.mlProb = mlProb; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
