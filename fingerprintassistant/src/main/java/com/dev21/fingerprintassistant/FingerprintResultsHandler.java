package com.dev21.fingerprintassistant;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * Created by Prajwal on 24/05/17.
 */

@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerprintResultsHandler extends FingerprintManager.AuthenticationCallback {
    private final String TAG = FingerprintResultsHandler.class.getSimpleName();
    private Context context;
    private CancellationSignal cancellationSignal;
    private boolean listeningState;
    private boolean authState;
    private FingerprintAuthListener authenticationListener;

    public FingerprintResultsHandler(Context context) {
        this.context = context;
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        publishResult(errorCode, errString, null, ResponseCode.AUTH_ERROR);
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        super.onAuthenticationHelp(helpCode, helpString);
        publishResult(helpCode, helpString, null, ResponseCode.AUTH_HELP);
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        authState = true;
        publishResult(-1, null, result, ResponseCode.AUTH_SUCCESS);
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        publishResult(-1, null, null, ResponseCode.AUTH_FAILED);
    }

    /**
     * @param manager      FingerprintManager instance
     * @param cryptoObject Crypto object
     */
    public void startListening(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {
        listeningState = true;
        cancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    /**
     * Stops fingerprint scanning
     */
    public void stopListening() {
        listeningState = false;
        authState = false;
        if (cancellationSignal != null) {
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
    }

    /**
     * @return Returns true if the fingerprint sensor is already scanning
     */
    public boolean isAlreadyListening() {
        return listeningState;
    }

    /**
     * @return Returns true if the authentication is already successful
     */
    public boolean isAlreadyAuthenticated() {
        return authState;
    }

    public void setFingerprintAuthListener(FingerprintAuthListener fingerprintAuthListener) {
        authenticationListener = fingerprintAuthListener;
    }

    private void publishResult(int helpOrErrorCode, CharSequence message, FingerprintManager.AuthenticationResult result, int authCode) {
        if (authenticationListener != null) {
            authenticationListener.onAuthentication(helpOrErrorCode, message, result, authCode);
        }
    }

    public void restartListening(FingerprintManager fingerprintManager, FingerprintManager.CryptoObject cryptoObject){
        stopListening();
        startListening(fingerprintManager,cryptoObject);
    }
}
