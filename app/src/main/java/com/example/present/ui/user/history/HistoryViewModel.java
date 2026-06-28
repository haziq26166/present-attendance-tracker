package com.example.present.ui.user.history;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.present.data.model.AttendanceRecord;
import com.example.present.data.repository.AuthRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.List;

/**
 * ViewModel responsible for providing a real-time stream of attendance records.
 * It removes the need for manual refreshes by using a Firestore SnapshotListener.
 */
public class HistoryViewModel extends ViewModel {
    private final MutableLiveData<List<AttendanceRecord>> records = new MutableLiveData<>();
    private final AuthRepository authRepository;
    private ListenerRegistration historyListener;

    public HistoryViewModel() {
        this.authRepository = new AuthRepository();
        startListeningToHistory();
    }

    /**
     * Attaches a real-time listener to Firestore.
     * It filters by the current User ID and sorts by the most recent check-in.
     */
    private void startListeningToHistory() {
        if (authRepository.getCurrentUser() == null) return;

        String uid = authRepository.getCurrentUser().getUid();

        // Path: attendance collection -> filter by userId -> sort by date
        // addSnapshotListener makes this "Real-Time"
        historyListener = FirebaseFirestore.getInstance().collection("attendance")
                .whereEqualTo("userId", uid)
                .orderBy("checkedInAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        // In a production app, you might push an error state to the UI here
                        return;
                    }

                    if (snapshots != null) {
                        // Automatically converts Firestore documents into AttendanceRecord objects
                        // These objects must contain eventName, venueName, and photoUrl fields
                        List<AttendanceRecord> historyList = snapshots.toObjects(AttendanceRecord.class);
                        records.setValue(historyList);
                    }
                });
    }

    /**
     * Observed by HistoryFragment to update the total count and the RecyclerView.
     */
    public LiveData<List<AttendanceRecord>> getRecords() {
        return records;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // CRITICAL: Remove the listener when the ViewModel is destroyed
        // This prevents memory leaks and unnecessary data usage when the user leaves the screen
        if (historyListener != null) {
            historyListener.remove();
        }
    }
}