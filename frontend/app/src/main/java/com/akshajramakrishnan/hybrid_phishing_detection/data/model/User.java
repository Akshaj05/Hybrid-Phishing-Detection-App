package com.akshajramakrishnan.hybrid_phishing_detection.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String uid;        // Firebase UID
    private String name;
    private String email;
    private boolean isLoggedIn;

    public User(String uid, String name, String email, boolean isLoggedIn) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.isLoggedIn = isLoggedIn;
    }

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public boolean isLoggedIn() { return isLoggedIn; }
    public void setLoggedIn(boolean loggedIn) { isLoggedIn = loggedIn; }
}