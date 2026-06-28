package com.example.present.ui.user.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.present.data.model.AttendanceRecord;
import com.example.present.data.model.User;
import com.example.present.data.repository.AttendanceRepository;
import com.example.present.data.repository.AuthRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.List;
import com.example.present.data.repository.AttendanceRepository.AttendanceCallback;

public class UserHomeViewModel extends ViewModel {
    private final MutableLiveData<User> userData = new MutableLiveData<>();
    private final MutableLiveData<List<AttendanceRecord>> recentActivity = new MutableLiveData<>();

    // 1. ADD: LiveData for the total count
    private final MutableLiveData<Integer> totalAttended = new MutableLiveData<>(0);

    private final AttendanceRepository attendanceRepository;
    private final AuthRepository authRepository;
    private ListenerRegistration attendanceListener;

    public UserHomeViewModel() {
        this.attendanceRepository = new AttendanceRepository();
        this.authRepository = new AuthRepository();
        loadData();
    }

    private void loadData() {
        if (authRepository.getCurrentUser() == null) return;
        String uid = authRepository.getCurrentUser().getUid();

        authRepository.fetchUserData(uid, userData::setValue, e -> {});

        // 2. START: Real-time listener for the total count
        startAttendanceListener(uid);

        // Load history (one-time fetch for the list)
        attendanceRepository.getUserHistory(uid, new AttendanceCallback<List<AttendanceRecord>>() {
            @Override
            public void onSuccess(List<AttendanceRecord> records) {
                if (records != null) {
                    recentActivity.setValue(records.size() > 3 ? records.subList(0, 3) : records);
                }
            }
            @Override
            public void onError(String message) {}
        });
    }

    private void startAttendanceListener(String uid) {
        // 3. CRITICAL: Ensure "userId" matches the field name in your AttendanceRecord.java
        attendanceListener = FirebaseFirestore.getInstance().collection("attendance")
                .whereEqualTo("userId", uid)
                .addSnapshotListener((snapshots, e) -> {
                    if (snapshots != null) {
                        totalAttended.setValue(snapshots.size());
                    }
                });
    }

    public LiveData<User> getUserData() { return userData; }
    public LiveData<List<AttendanceRecord>> getRecentActivity() { return recentActivity; }
    public LiveData<Integer> getTotalAttended() { return totalAttended; }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (attendanceListener != null) {
            attendanceListener.remove();
        }
    }
}