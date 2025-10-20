package com.akshajramakrishnan.hybrid_phishing_detection.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

// Model for API request
class UrlRequest {
    private String url;

    public UrlRequest(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}

// Model for API response
class UrlResponse {
    private String finalUrl;
    private String verdict;
    private int score;
    private double mlProb;

    public String getFinalUrl() { return finalUrl; }
    public String getVerdict() { return verdict; }
    public int getScore() { return score; }
    public double getMlProb() { return mlProb; }
}

public interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("/scanUrl")
    Call<UrlResponse> scanUrl(@Body UrlRequest request);
}
