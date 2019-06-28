package com.epatient.mobile.model;

import com.google.gson.annotations.Expose;

import lombok.Data;

@Data
public class AuthenticationResponse {
    @Expose
    private String token;
}
