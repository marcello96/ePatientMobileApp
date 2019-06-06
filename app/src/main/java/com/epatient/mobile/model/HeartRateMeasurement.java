package com.epatient.mobile.model;

import org.joda.time.LocalDateTime;

public class HeartRateMeasurement {
    private int value;
    private LocalDateTime time;

    public HeartRateMeasurement(int value, LocalDateTime time) {
        this.value = value;
        this.time = time;
    }

    public int getValue() {
        return value;
    }

    public LocalDateTime getTime() {
        return time;
    }
}
