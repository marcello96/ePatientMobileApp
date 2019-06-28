package com.epatient.mobile.model;

import com.google.gson.annotations.Expose;

import lombok.Data;

@Data
public class AuthenticationRequest {
    @Expose
    private String username;
    @Expose
    private String password;
}
