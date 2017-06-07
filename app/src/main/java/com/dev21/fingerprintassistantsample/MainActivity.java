package com.dev21.fingerprintassistantsample;

import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.dev21.fingerprintassistant.interfaces.FingerprintAuthListener;
import com.dev21.fingerprintassistant.helper.FingerprintHelper;
import com.dev21.fingerprintassistant.helper.FingerprintResultsHandler;
import com.dev21.fingerprintassistant.util.ResponseCode;

public class MainActivity extends AppCompatActivity implements FingerprintAuthListener {
    private final String TAG = MainActivity.class.getSimpleName();
    private FingerprintHelper fingerPrintHelper;
    private FingerprintResultsHandler fingerprintResultsHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerForFingerprintService();
        }

    }

    private void registerForFingerprintService() {

        fingerPrintHelper = new FingerprintHelper(this, "Test");

        switch (fingerPrintHelper.checkAndEnableFingerPrintService()) {

            case ResponseCode.FINGERPRINT_SERVICE_INITIALISATION_SUCCESS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    fingerprintResultsHandler = new FingerprintResultsHandler(this);
                    fingerprintResultsHandler.setFingerprintAuthListener(this);
                    fingerprintResultsHandler.startListening(fingerPrintHelper.getFingerprintManager(), fingerPrintHelper.getCryptoObject());
                }
                showToast("Fingerprint sensor started scanning");
                break;
            case ResponseCode.OS_NOT_SUPPORTED:
                showToast("OS doesn't support fingerprint api");
                break;
            case ResponseCode.FINGER_PRINT_SENSOR_UNAVAILABLE:
                showToast("Fingerprint sensor not found");
                break;
            case ResponseCode.ENABLE_FINGER_PRINT_SENSOR_ACCESS:
                showToast("Give access to use fingerprint sensor");
                break;
            case ResponseCode.NO_FINGER_PRINTS_ARE_ENROLLED:
                showToast("No fingerprints found");
                break;
            case ResponseCode.FINGERPRINT_SERVICE_INITIALISATION_FAILED:
                showToast("Fingerprint service initialisation failed");
                break;
            case ResponseCode.DEVICE_NOT_KEY_GUARD_SECURED:
                showToast("Device is not key guard protected");
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start fingerprint scanning and listen for fingerprint callbacks
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (fingerprintResultsHandler != null && !fingerprintResultsHandler.isAlreadyListening()) {
                fingerprintResultsHandler.startListening(fingerPrintHelper.getFingerprintManager(), fingerPrintHelper.getCryptoObject());
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop fingerprint scanning and listening for fingerprint callbacks
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (fingerprintResultsHandler != null) {
                fingerprintResultsHandler.stopListening();
            }
        }
    }

    @Override
    public void onAuthentication(int helpOrErrorCode, CharSequence infoString, FingerprintManager.AuthenticationResult authenticationResult, int authCode) {
        switch (authCode) {
            case ResponseCode.AUTH_ERROR:
                // Show appropriate message
                break;
            case ResponseCode.AUTH_FAILED:
                // Show appropriate message
                showToast("Authentication Failed");
                break;
            case ResponseCode.AUTH_HELP:
                // Show appropriate message
                break;
            case ResponseCode.AUTH_SUCCESS:
                // Do whatever you want
                showToast("Authentication Success");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    fingerprintResultsHandler.restartListening(fingerPrintHelper.getFingerprintManager(), fingerPrintHelper.getCryptoObject());
                }
                break;
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
