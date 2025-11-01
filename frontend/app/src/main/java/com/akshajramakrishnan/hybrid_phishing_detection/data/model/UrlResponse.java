package com.akshajramakrishnan.hybrid_phishing_detection.data.model;

import java.util.List;
import java.util.Map;

public class UrlResponse {
    private String finalUrl;
    private String verdict;
    private int score;
    private double mlProb;

    private List<String> reasons;
    private Map<String, Object> features;

    public List<String> getReasons() { return reasons; }
    public Map<String, Object> getFeatures() { return features; }


    public String getFinalUrl() { return finalUrl; }
    public String getVerdict() { return verdict; }
    public int getScore() { return score; }
    public double getMlProb() { return mlProb; }
}