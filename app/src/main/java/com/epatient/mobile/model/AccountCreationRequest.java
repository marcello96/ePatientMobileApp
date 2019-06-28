package com.epatient.mobile.model;

import com.google.gson.annotations.Expose;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
