package com.akshajramakrishnan.hybrid_phishing_detection.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UrlResponse {

    private int id;

    @SerializedName("original_url")
    private String originalUrl;

    @SerializedName("final_url")
    private String finalUrl;

    private String verdict;
    private int score;
    private List<String> reasons;

    @SerializedName("ml_prob")
    private double mlProb;

    @SerializedName("time_ms")
    private long timeMs;

    public int getId() { return id; }
    public String getOriginalUrl() { return originalUrl; }
    public String getFinalUrl() { return finalUrl; }
    public String getVerdict() { return verdict; }
    public int getScore() { return score; }
    public List<String> getReasons() { return reasons; }
    public double getMlProb() { return mlProb; }
    public long getTimeMs() { return timeMs; }
}
