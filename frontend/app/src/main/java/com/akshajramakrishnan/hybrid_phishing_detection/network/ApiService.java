package com.akshajramakrishnan.hybrid_phishing_detection.network;

import com.akshajramakrishnan.hybrid_phishing_detection.data.model.*;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;


public interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("api/scan")
    Call<UrlResponse> scanUrl(@Body UrlRequest request);
}
