package com.epatient.mobile.global;


import android.app.Application;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticationApplication extends Application {
    private String token;
    private String serverAddress;
}
