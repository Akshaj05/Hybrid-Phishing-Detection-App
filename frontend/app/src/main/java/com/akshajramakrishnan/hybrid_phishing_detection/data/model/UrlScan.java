package com.akshajramakrishnan.hybrid_phishing_detection.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "url_scans")
public class UrlScan {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String originalUrl;
    private String finalUrl;
    private String verdict;
    private int score;
    private double mlProb;
    private long timestamp;

    public UrlScan(String originalUrl, String finalUrl, String verdict, int score, double mlProb, long timestamp) {
        this.originalUrl = originalUrl;
        this.finalUrl = finalUrl;
        this.verdict = verdict;
        this.score = score;
        this.mlProb = mlProb;
        this.timestamp = timestamp;
    }

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getOriginalUrl() { return originalUrl; }
    public String getFinalUrl() { return finalUrl; }
    public String getVerdict() { return verdict; }
    public int getScore() { return score; }
    public double getMlProb() { return mlProb; }
    public long getTimestamp() { return timestamp; }
}