package com.epatient.mobile.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AuthenticationRequest {
    @JsonProperty
    private String username;
    @JsonProperty
    private String password;
}
