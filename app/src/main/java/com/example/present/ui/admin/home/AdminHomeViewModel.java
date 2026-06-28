package com.example.present.ui.admin.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.present.data.model.AttendanceRecord;
import com.example.present.data.model.User;
import com.example.present.data.repository.AuthRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AdminHomeViewModel extends ViewModel {
    private final MutableLiveData<User> adminData = new MutableLiveData<>();
    private final MutableLiveData<Long> totalEvents = new MutableLiveData<>(0L);
    private final MutableLiveData<Long> todayCheckins = new MutableLiveData<>(0L);
    private final MutableLiveData<List<AttendanceRecord>> recentActivity = new MutableLiveData<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final AuthRepository authRepository;

    // Listener registrations for cleanup
    private ListenerRegistration eventsListener;
    private ListenerRegistration activityListener;
    private ListenerRegistration todayListener;

    public AdminHomeViewModel() {
        this.authRepository = new AuthRepository();
        loadAdminProfile();
        listenToStats();
    }

    private void loadAdminProfile() {
        if (authRepository.getCurrentUser() != null) {
            authRepository.fetchUserData(authRepository.getCurrentUser().getUid(), adminData::setValue, e -> {});
        }
    }

    private void listenToStats() {
        if (authRepository.getCurrentUser() == null) return;
        String uid = authRepository.getCurrentUser().getUid();

        // 1. Listen to Total Events (Specific to this Admin)
        eventsListener = db.collection("events")
                .whereEqualTo("organizerId", uid)
                .addSnapshotListener((value, error) -> {
                    if (value != null) totalEvents.setValue((long) value.size());
                });

        // 2. Listen to Global Recent Activity (Top-level 'attendance' collection)
        // We show the last 10 records across the entire app
        activityListener = db.collection("attendance")
                .orderBy("checkedInAt", Query.Direction.DESCENDING)
                .limit(10)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        recentActivity.setValue(value.toObjects(AttendanceRecord.class));
                    }
                });

        // 3. Listen to Today's Check-ins count
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Timestamp startOfToday = new Timestamp(cal.getTime());

        todayListener = db.collection("attendance")
                .whereGreaterThanOrEqualTo("checkedInAt", startOfToday)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        todayCheckins.setValue((long) value.size());
                    }
                });
    }

    public LiveData<User> getAdminData() { return adminData; }
    public LiveData<Long> getTotalEvents() { return totalEvents; }
    public LiveData<Long> getTodayCheckins() { return todayCheckins; }
    public LiveData<List<AttendanceRecord>> getRecentActivity() { return recentActivity; }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Remove all listeners to prevent memory leaks and unnecessary data usage
        if (eventsListener != null) eventsListener.remove();
        if (activityListener != null) activityListener.remove();
        if (todayListener != null) todayListener.remove();
    }
}