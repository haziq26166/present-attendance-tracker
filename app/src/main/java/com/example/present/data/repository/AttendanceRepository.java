package com.example.present.data.repository;

import com.example.present.data.model.AttendanceRecord;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;

public class AttendanceRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface AttendanceCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public void submitRecord(AttendanceRecord record, AttendanceCallback<Void> callback) {
        // Create a unique ID for the record (EventID + UserID)
        // This ensures a user can only check into an event ONCE (it overwrites if they scan again)
        String recordId = record.getEventId() + "_" + record.getUserId();

        db.collection("attendance")
                .document(recordId) // Using a flat structure
                .set(record)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getUserHistory(String userId, AttendanceCallback<List<AttendanceRecord>> callback) {
        // Now we query the top-level 'attendance' collection directly
        db.collection("attendance")
                .whereEqualTo("userId", userId)
                .orderBy("checkedInAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<AttendanceRecord> records = snapshots.toObjects(AttendanceRecord.class);
                    callback.onSuccess(records);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}