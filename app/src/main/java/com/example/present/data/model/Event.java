package com.example.present.data.model;
import com.google.firebase.Timestamp;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;

public class Event implements Serializable {
    public String eventId;
    @PropertyName("eventName")
    public String eventName;

    public String description;
    public String date;
    public String startTime;
    public String endTime;
    public String venueName;
    public double venueLat;
    public double venueLng;
    public int radiusMeters;
    public int maxCapacity;
    public String organizerId;
    public String qrPayload;
    public Timestamp createdAt;

    public Event() {}
}