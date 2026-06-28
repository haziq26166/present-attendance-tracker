package com.example.present.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.database.PropertyName;

import java.util.List;

public class AttendanceRecord {
    // Fields
    public String displayName;
    public String email;
    public Timestamp checkedInAt;
    public String eventId;
    @PropertyName("eventName")
    public String eventName;
    public String venueName;
    public String userId;
    public String photoUrl;
    public String status;
    public List<String> verificationMethods;

    // 1. CRITICAL: Empty constructor required for Firestore
    public AttendanceRecord() {}

    // 2. GETTERS: These must exist so Firestore can read the data and Adapter can display it
    public String getUserId() { return userId; }
    public String getEventId() { return eventId; }
    public String getEventName() { return eventName; } // Added getter
    public String getVenueName() { return venueName; } // Added getter
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
    public Timestamp getCheckedInAt() { return checkedInAt; }
    public String getPhotoUrl() { return photoUrl; }
    public String getStatus() { return status; }
    public List<String> getVerificationMethods() { return verificationMethods; }

    // 3. SETTERS: Used when creating the record in QRFragment/Scanner
    public void setUserId(String userId) { this.userId = userId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setEventName(String eventName) { this.eventName = eventName; } // Added setter
    public void setVenueName(String venueName) { this.venueName = venueName; } // Added setter
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setEmail(String email) { this.email = email; }
    public void setCheckedInAt(Timestamp checkedInAt) { this.checkedInAt = checkedInAt; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setStatus(String status) { this.status = status; }
    public void setVerificationMethods(List<String> verificationMethods) {
        this.verificationMethods = verificationMethods;
    }
}