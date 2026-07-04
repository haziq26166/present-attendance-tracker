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

public class HistoryViewModel extends ViewModel {
    private final MutableLiveData<List<AttendanceRecord>> records = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final AuthRepository authRepository;
    private ListenerRegistration historyListener;
    private boolean isListenerAttached = false;

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

        // Remove any existing listener
        if (historyListener != null) {
            historyListener.remove();
            historyListener = null;
        }

        isLoading.setValue(true);
        isListenerAttached = true;

        historyListener = FirebaseFirestore.getInstance().collection("attendance")
                .whereEqualTo("userId", uid)
                .orderBy("checkedInAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    isLoading.setValue(false);

                    if (e != null) {
                        return;
                    }

                    if (snapshots != null) {
                        List<AttendanceRecord> historyList = snapshots.toObjects(AttendanceRecord.class);
                        records.setValue(historyList);
                    }
                });
    }

    /**
     * Manually refresh data by reattaching the listener
     */
    public void refreshData() {
        if (authRepository.getCurrentUser() == null) return;

        isLoading.setValue(true);

        // Reattach listener to get fresh data
        if (historyListener != null) {
            historyListener.remove();
            historyListener = null;
        }

        startListeningToHistory();
    }

    public LiveData<List<AttendanceRecord>> getRecords() {
        return records;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        isListenerAttached = false;
        if (historyListener != null) {
            historyListener.remove();
            historyListener = null;
        }
    }
}