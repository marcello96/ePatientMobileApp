package com.epatient.mobile.model;

import com.google.gson.annotations.Expose;

import org.joda.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeartRateMeasurement {
    @Expose
    private int heartRate;
    @Expose
    private LocalDateTime timestamp;
}
