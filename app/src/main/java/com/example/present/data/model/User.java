package com.example.present.data.model;
import com.google.firebase.Timestamp;

public class User {
    public String uid;
    public String displayName;
    public String email;
    public String role;
    public Timestamp createdAt;

    public User() {} // Required for Firestore

    public User(String uid, String displayName, String email, String role) {
        this.uid = uid;
        this.displayName = displayName;
        this.email = email;
        this.role = role;
        this.createdAt = Timestamp.now();
    }
}