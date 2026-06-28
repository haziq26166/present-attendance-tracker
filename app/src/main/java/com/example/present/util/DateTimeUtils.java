package com.example.present.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {
    public static String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String getTodayFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }
}