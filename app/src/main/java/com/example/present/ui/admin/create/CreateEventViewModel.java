package com.example.present.ui.admin.create;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.present.data.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonObject;

public class CreateEventViewModel extends ViewModel {
    public enum CreateStep { DETAILS, MAP_GEOFENCE, DONE }

    public final MutableLiveData<CreateStep> step = new MutableLiveData<>(CreateStep.DETAILS);

    // Removed 'final' so we can re-initialize this object on reset
    public Event event = new Event();

    public void proceedToMap(String eventName, String desc, String date, String start, String end, String venue, int cap) {
        event.eventName = eventName;
        event.description = desc;
        event.date = date;
        event.startTime = start;
        event.endTime = end;
        event.venueName = venue;
        event.maxCapacity = cap;
        step.setValue(CreateStep.MAP_GEOFENCE);
    }

    public void finalizeEvent(double lat, double lng, int radius, String orgId) {
        // 1. Generate a Firestore ID early so we can put it IN the QR code
        String autoId = FirebaseFirestore.getInstance()
                .collection("events").document().getId();

        // 2. Safety: Ensure radius is at least 20m for GPS reliability
        int safeRadius = Math.max(radius, 20);

        event.eventId = autoId;
        event.venueLat = lat;
        event.venueLng = lng;
        event.radiusMeters = safeRadius;
        event.organizerId = orgId;

        // 3. Build the QR payload INCLUDING the eventId
        JsonObject json = new JsonObject();
        json.addProperty("eventId", autoId);
        json.addProperty("eventName", event.eventName);
        json.addProperty("venueName", event.venueName);
        json.addProperty("venueLat", lat);
        json.addProperty("venueLng", lng);
        json.addProperty("radiusMeters", safeRadius);

        event.qrPayload = json.toString();

        step.setValue(CreateStep.DONE);
    }

    public void resetViewModel() {
        // Replace the old event object with a clean one
        this.event = new Event();
        // Reset the navigation step to the beginning
        this.step.setValue(CreateStep.DETAILS);
    }
}