package com.dev21.fingerprintassistant;

/**
 * Created by Prajwal on 26/05/17.
 */

public class ResponseCode {
    public static final int FINGER_PRINT_SENSOR_UNAVAILABLE = 100;
    public static final int ENABLE_FINGER_PRINT_SENSOR_ACCESS = 200;
    public static final int NO_FINGER_PRINTS_ARE_ENROLLED = 300;
    public static final int OS_NOT_SUPPORTED = 400;
    public static final int FINGERPRINT_SERVICE_INITIALISATION_FAILED = 500;
    public static final int FINGERPRINT_SERVICE_INITIALISATION_SUCCESS = 600;
    public static final int DEVICE_NOT_KEY_GUARD_SECURED = 700;
    
    public static final int AUTH_ERROR = 1000;
    public static final int AUTH_FAILED = 2000;
    public static final int AUTH_HELP = 3000;
    public static final int AUTH_SUCCESS = 4000;
}
