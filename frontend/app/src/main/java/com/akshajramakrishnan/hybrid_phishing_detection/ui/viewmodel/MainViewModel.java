package com.akshajramakrishnan.hybrid_phishing_detection.ui.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.akshajramakrishnan.hybrid_phishing_detection.data.local.AppDatabase;
import com.akshajramakrishnan.hybrid_phishing_detection.data.model.UrlRequest;
import com.akshajramakrishnan.hybrid_phishing_detection.data.model.UrlResponse;
import com.akshajramakrishnan.hybrid_phishing_detection.data.model.UrlScan;
import com.akshajramakrishnan.hybrid_phishing_detection.network.ApiService;
import com.akshajramakrishnan.hybrid_phishing_detection.network.RetrofitClient;
import com.akshajramakrishnan.hybrid_phishing_detection.util.SharedPrefManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainViewModel extends AndroidViewModel {

    private final MutableLiveData<UrlResponse> scanResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final ApiService apiService;
    private final AppDatabase database;
    private final SharedPrefManager pref;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public MainViewModel(@NonNull Application application) {
        super(application);
        apiService = RetrofitClient.getApiService();
        database = AppDatabase.getInstance(application);
        pref = new SharedPrefManager(application.getApplicationContext());
    }


    public LiveData<UrlResponse> getScanResult() {
        return scanResult;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void scanUrl(String url) {
        Log.d("SCAN_REQ", new com.google.gson.Gson().toJson(new UrlRequest(url)));
        apiService.scanUrl(new UrlRequest(url)).enqueue(new Callback<UrlResponse>() {
            @Override
            public void onResponse(Call<UrlResponse> call, Response<UrlResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UrlResponse res = response.body();
                    Log.d("SCAN_RESULT_DEBUG", "Response from backend → " + new com.google.gson.Gson().toJson(res));

                    // Post result
                    scanResult.postValue(res);

                    // Save to Room
                    UrlScan scan = new UrlScan(
                            pref.getUid() != null ? pref.getUid() : "guest",
                            url,
                            res.getFinalUrl(),
                            res.getVerdict(),
                            res.getScore(),
                            res.getMlProb(),
                            res.getTimeMs()  // ✅ use backend’s actual timeMs
                    );
                    new Thread(() -> database.urlScanDao().insertUrlScan(scan)).start();
                }
            }



            @Override
            public void onFailure(Call<UrlResponse> call, Throwable t) {
                errorLiveData.postValue("Connection failed: " + t.getMessage());
            }
        });
    }
}
