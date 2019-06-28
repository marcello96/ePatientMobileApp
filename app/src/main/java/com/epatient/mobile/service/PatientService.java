package com.epatient.mobile.service;

import com.epatient.mobile.model.AccountCreationRequest;
import com.epatient.mobile.model.AuthenticationRequest;
import com.epatient.mobile.model.AuthenticationResponse;
import com.epatient.mobile.model.HeartRateMeasurement;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface PatientService {
    @POST("/user-management/patient")
    Single<ResponseBody> createAccount(@Body AccountCreationRequest accountCreationRequest);

    @POST("/login")
    Single<AuthenticationResponse> login(@Body AuthenticationRequest request);

    @POST("/patient/measurement")
    Single<ResponseBody> sendMeasurement(@Header("Authorization") String tokenHeader, @Body HeartRateMeasurement measurement);
}
