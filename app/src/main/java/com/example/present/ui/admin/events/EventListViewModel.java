package com.example.present.ui.admin.events;

import android.util.Log; // Add this
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.present.data.model.Event;
import com.example.present.data.repository.AuthRepository;
import com.example.present.data.repository.EventRepository;
import java.util.List;

public class EventListViewModel extends ViewModel {
    private final MutableLiveData<List<Event>> events = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(); // Add this
    private final EventRepository repository = new EventRepository();
    private final AuthRepository auth = new AuthRepository();

    public void loadEvents() {
        if (auth.getCurrentUser() == null) {
            errorMessage.setValue("User not logged in");
            return;
        }

        String adminUid = auth.getCurrentUser().getUid();
        Log.d("EventListVM", "Loading events for Admin UID: " + adminUid);

        repository.getAdminEvents(adminUid, new EventRepository.EventCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> result) {
                Log.d("EventListVM", "Events found: " + (result != null ? result.size() : 0));
                events.setValue(result);
            }
            @Override
            public void onError(String message) {
                Log.e("EventListVM", "Firestore Error: " + message);
                errorMessage.setValue(message);
            }
        });
    }

    public LiveData<List<Event>> getEvents() { return events; }
    public LiveData<String> getErrorMessage() { return errorMessage; } // Add this
}