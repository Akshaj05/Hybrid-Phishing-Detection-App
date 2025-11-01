package com.akshajramakrishnan.hybrid_phishing_detection.data.model;

public class UrlRequest {
    private String url;
    private boolean use_ml;

    public UrlRequest(String url) {
        this.url = url;
        this.use_ml = true;
    }

    public boolean isUse_ml() {
        return use_ml;
    }

    public String getUrl() {
        return url;
    }
}
