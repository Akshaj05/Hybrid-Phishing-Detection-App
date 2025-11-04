package com.akshajramakrishnan.hybrid_phishing_detection.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.akshajramakrishnan.hybrid_phishing_detection.data.model.BlockedUrl;
import java.util.List;

@Dao
public interface BlockedUrlDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBlockedUrl(BlockedUrl blockedUrl);

    @Query("SELECT * FROM blocked_urls WHERE uid = :uid ORDER BY timestamp DESC")
    List<BlockedUrl> getBlockedUrls(String uid);

    @Query("DELETE FROM blocked_urls WHERE uid = :uid")
    void clearAll(String uid);

}
