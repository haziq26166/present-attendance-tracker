package com.example.present.data.repository;

import com.example.present.data.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class EventRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface EventCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    /**
     * Updated to use the eventId generated in the ViewModel.
     * This ensures the Document ID in Firestore matches the ID inside the QR Code.
     */
    public void createEvent(Event event, EventCallback<Void> callback) {
        // Use the eventId already set in the object (from CreateEventViewModel)
        // If for some reason it's null, generate one as a fallback.
        String id = (event.eventId != null && !event.eventId.isEmpty())
                ? event.eventId
                : db.collection("events").document().getId();

        event.eventId = id;

        db.collection("events").document(id).set(event)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getAdminEvents(String adminUid, EventCallback<List<Event>> callback) {
        db.collection("events")
                .whereEqualTo("organizerId", adminUid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> events = querySnapshot.toObjects(Event.class);
                    // Map the Firestore Document ID to the eventId field for each item
                    for (int i = 0; i < querySnapshot.size(); i++) {
                        events.get(i).eventId = querySnapshot.getDocuments().get(i).getId();
                    }
                    callback.onSuccess(events);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Optional: Helper to fetch a single event by ID if needed for verification
     */
    public void getEventById(String eventId, EventCallback<Event> callback) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event event = documentSnapshot.toObject(Event.class);
                    if (event != null) {
                        event.eventId = documentSnapshot.getId();
                        callback.onSuccess(event);
                    } else {
                        callback.onError("Event not found");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}