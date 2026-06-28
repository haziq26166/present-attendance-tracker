package com.example.present.ui.admin.attendees;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.present.data.model.AttendanceRecord;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;

public class AttendeeListViewModel extends ViewModel {
    private final MutableLiveData<List<AttendanceRecord>> attendees = new MutableLiveData<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void loadAttendees(String eventId) {
        FirebaseFirestore.getInstance().collection("attendance")
                .whereEqualTo("eventId", eventId) // MUST match the field name in Firestore
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        List<AttendanceRecord> list = value.toObjects(AttendanceRecord.class);
                        attendees.setValue(list);
                    }
                });
    }

    public LiveData<List<AttendanceRecord>> getAttendees() { return attendees; }
}