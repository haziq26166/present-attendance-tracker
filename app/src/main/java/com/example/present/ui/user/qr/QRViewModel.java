package com.example.present.ui.user.qr;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.present.data.model.Event;

public class QRViewModel extends ViewModel {
    public enum QRStep { QR_SCAN, GPS_CHECK, PHOTO_CAPTURE, DONE }

    private final MutableLiveData<QRStep> currentStep = new MutableLiveData<>(QRStep.QR_SCAN);
    private final MutableLiveData<Event> scannedEvent = new MutableLiveData<>();
    private final MutableLiveData<Double> currentDistance = new MutableLiveData<>(0.0);
    private final MutableLiveData<String> photoUrl = new MutableLiveData<>();

    public LiveData<QRStep> getCurrentStep() { return currentStep; }
    public LiveData<Event> getScannedEvent() { return scannedEvent; }
    public LiveData<Double> getCurrentDistance() { return currentDistance; }
    public LiveData<String> getPhotoUrl() { return photoUrl; }

    public void setCurrentStep(QRStep step) {
        if (currentStep.getValue() != step) {
            currentStep.postValue(step);
        }
    }

    public void setScannedEvent(Event event) {
        scannedEvent.postValue(event);
        currentStep.postValue(QRStep.GPS_CHECK);
    }

    public void setGpsPassed(double distance) {
        // Only transition if we are currently in the GPS_CHECK phase
        // to prevent looping or accidental double-triggers
        if (currentStep.getValue() == QRStep.GPS_CHECK) {
            currentDistance.postValue(distance);
            currentStep.postValue(QRStep.PHOTO_CAPTURE);
        }
    }

    public void setPhotoUploaded(String url) {
        photoUrl.postValue(url);
        currentStep.postValue(QRStep.DONE);
    }
}