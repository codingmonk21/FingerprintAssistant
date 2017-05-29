package com.dev21.fingerprintassistant;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import static android.content.Context.FINGERPRINT_SERVICE;
import static android.content.Context.KEYGUARD_SERVICE;

/**
 * Created by Prajwal on 24/05/17.
 */

public class FingerprintHelper {
    private final String TAG = FingerprintHelper.class.getSimpleName();
    private Context mContext;
    private KeyStore keyStore;
    private String KEY_NAME;
    private Cipher cipher;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintManager fingerprintManager;

    /**
     * @param context Context
     * @param keyName Name of the key to be stored in android keystore
     */
    public FingerprintHelper(Context context, String keyName) {
        mContext = context;
        KEY_NAME = keyName;
    }

    /**
     * @return Returns result code, an integer which can be any one of the following,<br/><br/>
     * <b>FINGERPRINT_SENSOR_NOT_FOUND - 100<br/><br/></b>
     * <b>PERMISSION_NOT_GRANTED_TO_ACCESS_FINGERPRINT_SENSOR - 200<br/><br/></b>
     * <b>NO_FINGER_PRINTS_ENROLLED_IN_THE_DEVICE - 300<br/><br/></b>
     * <b>OS_DOES_NOT_SUPPORT_FINGERPRINT_API - 400<br/><br/></b>
     * <b>FAILED_INITIALISING_FINGERPRINT_SERVICE - 500<br/><br/></b>
     * <b>FINGERPRINT_INITIALISATION_SUCCESS- 600<br/><br/></b>
     * <b>KEY_GUARD_DISABLED - 700<br/><br/></b>
     */
    public int checkAndEnableFingerPrintService() {

        KeyguardManager keyguardManager = (KeyguardManager) mContext.getSystemService(KEYGUARD_SERVICE);
        fingerprintManager = (FingerprintManager) mContext.getSystemService(FINGERPRINT_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!fingerprintManager.isHardwareDetected()) {
                // Check if fingerprint sensor is available
                return ResponseCode.FINGER_PRINT_SENSOR_UNAVAILABLE;
            } else {
                // Check if permission is granted to access fingerprint sensor
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                    return ResponseCode.ENABLE_FINGER_PRINT_SENSOR_ACCESS;
                } else {
                    // Check if device is keyguard protected
                    if (!keyguardManager.isKeyguardSecure()) {
                        return ResponseCode.DEVICE_NOT_KEY_GUARD_SECURED;
                    } else {
                        // Check if any fingerprints are enrolled
                        if (!fingerprintManager.hasEnrolledFingerprints()) {
                            return ResponseCode.NO_FINGER_PRINTS_ARE_ENROLLED;
                        } else {

                            if (generateKey()) {
                                if (cipherInit()) {
                                    cryptoObject = new FingerprintManager.CryptoObject(cipher);
                                    return ResponseCode.FINGERPRINT_SERVICE_INITIALISATION_SUCCESS;
                                } else {
                                    return ResponseCode.FINGERPRINT_SERVICE_INITIALISATION_FAILED;
                                }
                            } else {
                                return ResponseCode.FINGERPRINT_SERVICE_INITIALISATION_FAILED;
                            }
                        }
                    }
                }
            }
        } else {
            return ResponseCode.OS_NOT_SUPPORTED;
        }
    }

    /**
     * @return Returns crypto object to be passed to startListening() method
     */
    public FingerprintManager.CryptoObject getCryptoObject() {
        return cryptoObject;
    }

    /**
     * @return Fingerprint manager instance to be passed to startListening() method
     */
    public FingerprintManager getFingerprintManager() {
        return fingerprintManager;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            return false;
        }


        try {
            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
            return true;
        } catch (NoSuchAlgorithmException |
                InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            return false;
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            return false;
        }
    }
}
