package com.akshajramakrishnan.hybrid_phishing_detection.data.repository;

import android.content.Context;

import com.akshajramakrishnan.hybrid_phishing_detection.data.local.AppDatabase;
import com.akshajramakrishnan.hybrid_phishing_detection.data.local.BlockedUrlDao;
import com.akshajramakrishnan.hybrid_phishing_detection.data.local.UrlScanDao;
import com.akshajramakrishnan.hybrid_phishing_detection.data.local.UserDao;
import com.akshajramakrishnan.hybrid_phishing_detection.data.model.BlockedUrl;
import com.akshajramakrishnan.hybrid_phishing_detection.data.model.UrlScan;
import com.akshajramakrishnan.hybrid_phishing_detection.data.model.User;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainRepository {

    private final UrlScanDao urlScanDao;
    private final UserDao userDao;
    private final BlockedUrlDao blockedUrlDao;
    private final ExecutorService executorService;

    public MainRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.urlScanDao = db.urlScanDao();
        this.userDao = db.userDao();
        this.blockedUrlDao = db.blockedUrlDao();
        this.executorService = Executors.newFixedThreadPool(2);
    }

    // insert scan asynchronously
    public void insertScanAsync(UrlScan scan) {
        executorService.execute(() -> urlScanDao.insertUrlScan(scan));
    }

    public long insertScanSync(UrlScan scan) {
        return urlScanDao.insertUrlScan(scan);
    }

    public List<UrlScan> getScansForUser(String uid) {
        return urlScanDao.getScansForUser(uid);
    }

    public UrlScan getScanById(int id) {
        return urlScanDao.getScanById(id);
    }

    public void insertUser(User u) {
        executorService.execute(() -> userDao.insertUser(u));
    }

    public User getUserByUid(String uid) {
        return userDao.getUserByUid(uid);
    }

    public void insertBlockedUrl(BlockedUrl b) {
        executorService.execute(() -> blockedUrlDao.insertBlockedUrl(b));
    }

    public List<BlockedUrl> getBlockedForUser(String uid) {
        return blockedUrlDao.getBlockedUrls(uid);
    }
}
