package com.akshajramakrishnan.hybrid_phishing_detection.data.model;

public class UrlResponse {
    private String finalUrl;
    private String verdict;
    private int score;
    private double mlProb;

    public String getFinalUrl() { return finalUrl; }
    public String getVerdict() { return verdict; }
    public int getScore() { return score; }
    public double getMlProb() { return mlProb; }
}