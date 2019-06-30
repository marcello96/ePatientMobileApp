package com.epatient.mobile.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AuthenticationResponse {
    @JsonProperty
    private String token;
}
