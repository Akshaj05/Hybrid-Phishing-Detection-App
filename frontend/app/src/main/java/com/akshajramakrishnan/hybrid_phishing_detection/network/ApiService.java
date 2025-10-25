package com.akshajramakrishnan.hybrid_phishing_detection.network;

import com.akshajramakrishnan.hybrid_phishing_detection.data.model.*;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("api/scan")
    Call<UrlResponse> scanUrl(@Body UrlRequest request);
    @POST("/report")
    Call<ResponseBody> generateReport(@Body Map<String, Object> scanJson);



}
