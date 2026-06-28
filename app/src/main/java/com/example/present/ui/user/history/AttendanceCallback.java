package com.example.present.ui.user.history;

import com.example.present.data.model.AttendanceRecord; // Ensure this import matches your model path
import java.util.List;

public interface AttendanceCallback<T> {
    void onComplete(T result);
    void onError(Exception e);
}
