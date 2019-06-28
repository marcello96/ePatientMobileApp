package com.epatient.mobile.model;

import com.google.gson.annotations.Expose;

import lombok.Data;

@Data
public class AccountCreationRequest {
    @Expose
    private String username;
    @Expose
    private String password;
    @Expose
    private String firstname;
    @Expose
    private String lastname;
    @Expose
    private int age;
}
