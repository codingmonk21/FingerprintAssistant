package com.dev21.fingerprintassistant;

import android.hardware.fingerprint.FingerprintManager;

/**
 * Created by Prajwal on 26/05/17.
 */

public interface FingerprintAuthListener {
    public void onAuthentication(int helpOrErrorCode, CharSequence infoString, FingerprintManager.AuthenticationResult authenticationResult, int authCode);
}
