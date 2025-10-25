package com.akshajramakrishnan.hybrid_phishing_detection.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.akshajramakrishnan.hybrid_phishing_detection.data.model.User;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    User getUserByUid(String uid);
}
