package com.akshajramakrishnan.hybrid_phishing_detection.data.repository;

import android.content.Context;
import com.akshajramakrishnan.hybrid_phishing_detection.data.local.AppDatabase;
import com.akshajramakrishnan.hybrid_phishing_detection.data.local.UrlScanDao;
import com.akshajramakrishnan.hybrid_phishing_detection.data.model.UrlScan;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainRepository {

    private final UrlScanDao urlScanDao;
    private final ExecutorService executorService;

    public MainRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.urlScanDao = db.urlScanDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // Save scan result
    public void insertScan(UrlScan scan) {
        executorService.execute(() -> urlScanDao.insertUrlScan(scan));
    }

    // Retrieve all scans
    public List<UrlScan> getAllScans() {
        return urlScanDao.getAllScans();
    }

    // Clear all scans
    public void clearHistory() {
        executorService.execute(urlScanDao::clearAll);
    }
}