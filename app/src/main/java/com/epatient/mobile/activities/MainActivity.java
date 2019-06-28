package com.epatient.mobile.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.epatient.mobile.R;
import com.epatient.mobile.global.AuthenticationApplication;
import com.epatient.mobile.helpers.CustomBluetoothProfile;
import com.epatient.mobile.helpers.HeartRateUtil;
import com.epatient.mobile.model.HeartRateMeasurement;
import com.epatient.mobile.service.PatientService;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.LocalDateTime;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    boolean isListeningHeartRate = false;

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    BluetoothAdapter bluetoothAdapter;
    BluetoothGatt bluetoothGatt;
    BluetoothDevice bluetoothDevice;

    Button btnStartConnecting, btnGetHeartRate;
    EditText txtPhysicalAddress;
    TextView txtState, txtByte;
    private String mDeviceName;
    private String mDeviceAddress;
    private Timer hrScheduler;

    private String token;
    private PatientService patientService;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    private static final int INTERVAL_SEC = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        JodaTimeAndroid.init(this);

        initializeObjects();
        initilaizeComponents();
        initializeEvents();

        getBoundedDevice();

        initializeRetrofit();
    }

    @Override
    protected void onDestroy() {
        if (!compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
        super.onDestroy();
    }

    private void initializeRetrofit() {
        AuthenticationApplication authApp = (AuthenticationApplication) getApplication();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(authApp.getServerAddress())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(new OkHttpClient.Builder()
                        .retryOnConnectionFailure(false)
                        .build())
                .build();
        patientService = retrofit.create(PatientService.class);
        token = "Bearer " + authApp.getToken();
    }

    void getBoundedDevice() {

        mDeviceName = getIntent().getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = getIntent().getStringExtra(EXTRAS_DEVICE_ADDRESS);
        txtPhysicalAddress.setText(mDeviceAddress);

        Set<BluetoothDevice> boundedDevice = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bd : boundedDevice) {
            if (bd.getName().contains("ePatientMobileApp")) {
                txtPhysicalAddress.setText(bd.getAddress());
            }
        }
    }

    void initializeObjects() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        hrScheduler = new Timer();
    }

    void initilaizeComponents() {
        btnStartConnecting = findViewById(R.id.btnStartConnecting);
        btnGetHeartRate = findViewById(R.id.btnGetHeartRate);
        txtPhysicalAddress = findViewById(R.id.txtPhysicalAddress);
        txtState = findViewById(R.id.txtState);
        txtByte = findViewById(R.id.txtByte);
    }

    void initializeEvents() {
        btnStartConnecting.setOnClickListener(v -> startConnecting());
        btnGetHeartRate.setOnClickListener(v -> handleHrBtnClick());
    }

    void startConnecting() {

        String address = txtPhysicalAddress.getText().toString();
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);

        bluetoothGatt = bluetoothDevice.connectGatt(this, true, bluetoothGattCallback);
    }

    void stateConnected() {
        bluetoothGatt.discoverServices();
        runOnUiThread(() -> txtState.setText("Connected"));
    }

    void stateDisconnected() {
        bluetoothGatt.disconnect();
        runOnUiThread(() -> txtState.setText("Disconnected"));
    }

    void handleHrBtnClick() {
        if (isListeningHeartRate) {
            isListeningHeartRate = false;
            stopHeartRateScanning();
            btnGetHeartRate.setText(R.string.start_measurement);
        }
        isListeningHeartRate = true;
        startHeartRateScanning();
        btnGetHeartRate.setText(R.string.stop_measurement);
    }

    void startHeartRateScanning() {
        hrScheduler.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                scanHeartRate();
            }
        }, 0, 1000 * INTERVAL_SEC);
    }

    void stopHeartRateScanning() {
        hrScheduler.cancel();
    }

    void scanHeartRate() {
        runOnUiThread(() -> txtByte.setText("..."));
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(CustomBluetoothProfile.HeartRate.service)
                .getCharacteristic(CustomBluetoothProfile.HeartRate.controlCharacteristic);
        bchar.setValue(HeartRateUtil.getHealthRateWriteBytes());
        bluetoothGatt.writeCharacteristic(bchar);
    }

    void listenHeartRate() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(CustomBluetoothProfile.HeartRate.service)
                .getCharacteristic(CustomBluetoothProfile.HeartRate.measurementCharacteristic);
        bluetoothGatt.setCharacteristicNotification(bchar, true);
        BluetoothGattDescriptor descriptor = bchar.getDescriptor(CustomBluetoothProfile.HeartRate.descriptor);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
    }

    final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                stateConnected();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                stateDisconnected();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            listenHeartRate();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            processHeartRateResponse(characteristic.getValue());
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            processHeartRateResponse(characteristic.getValue());
        }
    };

    void processHeartRateResponse(byte[] bytes) {
        HeartRateMeasurement heartRateData = new HeartRateMeasurement();
        heartRateData.setHeartRate(HeartRateUtil.extractHeartRate(bytes));
        heartRateData.setTimestamp(LocalDateTime.now());
        runOnUiThread(() -> txtByte.setText(String.format("%d bpm", heartRateData.getHeartRate())));

        patientService.sendMeasurement(token, heartRateData).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(ResponseBody responseBody) {
                        responseBody.close();
                    }

                    @Override
                    public void onError(Throwable e) {
                        handleError(e);
                    }
                });

    }

    private void handleError(Throwable e) {
        System.out.println("error: "+ e);
        Toast.makeText(this, "Problem with Internet connection", Toast.LENGTH_LONG).show();
    }
}
