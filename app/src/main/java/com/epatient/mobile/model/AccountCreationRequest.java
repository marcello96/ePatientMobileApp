package com.epatient.mobile.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AccountCreationRequest {
    @JsonProperty
    private String username;
    @JsonProperty
    private String password;
    @JsonProperty
    private String firstname;
    @JsonProperty
    private String lastname;
    @JsonProperty
    private int age;
}
