package com.akshajramakrishnan.hybrid_phishing_detection.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.akshajramakrishnan.hybrid_phishing_detection.data.model.UrlScan;
import com.akshajramakrishnan.hybrid_phishing_detection.data.model.BlockedUrl;
import com.akshajramakrishnan.hybrid_phishing_detection.data.model.User;

@Database(
        entities = {UrlScan.class, BlockedUrl.class, User.class},
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UrlScanDao urlScanDao();
    public abstract BlockedUrlDao blockedUrlDao();
    public abstract UserDao userDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "safe_link_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
}
