package com.akshajramakrishnan.hybrid_phishing_detection.ui.viewmodel;

import android.app.Application;

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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainViewModel extends AndroidViewModel {

    private final MutableLiveData<UrlResponse> scanResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final ApiService apiService;
    private final AppDatabase database;

    public MainViewModel(@NonNull Application application) {
        super(application);
        apiService = RetrofitClient.getApiService();
        database = AppDatabase.getInstance(application);
    }

    public LiveData<UrlResponse> getScanResult() {
        return scanResult;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void scanUrl(String url) {
        apiService.scanUrl(new UrlRequest(url)).enqueue(new Callback<UrlResponse>() {
            @Override
            public void onResponse(Call<UrlResponse> call, Response<UrlResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UrlResponse res = response.body();
                    scanResult.postValue(res);
                    // Save scan in Room DB
                    UrlScan scan = new UrlScan(url, res.getFinalUrl(), res.getVerdict(),
                            res.getScore(), res.getMlProb(), System.currentTimeMillis());
                    new Thread(() -> database.urlScanDao().insertUrlScan(scan)).start();
                } else {
                    errorLiveData.postValue("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UrlResponse> call, Throwable t) {
                errorLiveData.postValue("Connection failed: " + t.getMessage());
            }
        });
    }
}
