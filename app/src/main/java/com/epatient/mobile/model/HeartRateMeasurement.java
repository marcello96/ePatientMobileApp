package com.epatient.mobile.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.joda.time.LocalDateTime;

import lombok.Data;

@Data
public class HeartRateMeasurement {
    @JsonProperty
    private int heartRate;
    @JsonProperty
    private LocalDateTime timestamp;
}
