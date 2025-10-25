package com.akshajramakrishnan.hybrid_phishing_detection.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.akshajramakrishnan.hybrid_phishing_detection.data.model.UrlScan;
import java.util.List;

@Dao
public interface UrlScanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUrlScan(UrlScan urlScan);

    @Query("SELECT * FROM url_scans WHERE user_id = :uid ORDER BY timestamp DESC")
    List<UrlScan> getScansForUser(String uid);

    @Query("SELECT * FROM url_scans WHERE id = :id LIMIT 1")
    UrlScan getScanById(int id);

    @Delete
    void deleteScan(UrlScan scan);

    @Query("DELETE FROM url_scans WHERE user_id = :uid")
    void clearAllForUser(String uid);
}
