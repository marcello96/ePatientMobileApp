package com.epatient.mobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.epatient.mobile.R;
import com.epatient.mobile.global.AuthenticationApplication;
import com.epatient.mobile.model.AuthenticationRequest;
import com.epatient.mobile.model.AuthenticationResponse;
import com.epatient.mobile.service.PatientService;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private EditText usernameText;
    private EditText serverAddressText;
    private EditText passwordText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (((AuthenticationApplication)getApplication()).getToken() != null ) {
            finalizeActivity();
            return;
        }
        usernameText = findViewById(R.id.usernameText);
        serverAddressText = findViewById(R.id.serverAddressText);
        passwordText = findViewById(R.id.passwordText);
    }

    @Override
    protected void onDestroy() {
        if (!compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
        super.onDestroy();
    }

    public void onButtonClick(View view)  {
        if (isTextEmpty(passwordText) || isTextEmpty(usernameText) || isTextEmpty(serverAddressText)) {
            Toast.makeText(this, "Fill in all fields!", Toast.LENGTH_LONG).show();
            return;
        }
        String serverAddress = "http://" + serverAddressText.getText().toString() + "/";
        AuthenticationApplication authApp = ((AuthenticationApplication) getApplication());
        authApp.setServerAddress(serverAddress);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverAddress)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(new OkHttpClient.Builder()
                        .retryOnConnectionFailure(false)
                        .build())
                .build();
        PatientService patientService = retrofit.create(PatientService.class);
        AuthenticationRequest authRequest = new AuthenticationRequest();
        authRequest.setUsername(usernameText.getText().toString().trim());
        authRequest.setPassword(passwordText.getText().toString().trim());
        patientService.login(authRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<AuthenticationResponse>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(AuthenticationResponse authResponse) {
                        authApp.setToken(authResponse.getToken());
                        finalizeActivity();
                    }

                    @Override
                    public void onError(Throwable e) {
                        handleError(e);
                    }
                });
    }

    private void finalizeActivity() {
        final Intent intent = new Intent(LoginActivity.this, DeviceScanActivity.class);
        if (!compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
        startActivity(intent);
    }

    private void handleError(Throwable e) {
        System.out.println("error: "+ e);
        Toast.makeText(this, "Problem with Internet connection", Toast.LENGTH_LONG).show();
    }

    private boolean isTextEmpty(EditText editText) {
        return editText.getText().toString().trim().equals("");
    }
}
