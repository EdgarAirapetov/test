package com.numplates.nomera3.presentation.view.utils;

import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.numplates.nomera3.presentation.router.BaseAct;

/**
 * created by abelov on 04.06.18
 */
public class PermissionManager {

    /** To catch get location permission dialog result */
    public static final int PERMISSION_LOCATION_CODE = 1001;
    /** To catch get read external permission dialog result */
    public static final int PERMISSION_READ_EXTERNAL_CODE = 1002;
    public static final int PERMISSION_CAMERA_CODE = 1003;
    public static final int REQUEST_AUDIO_PERMISSION_RESULT = 1011;


    private static boolean checkRuntimePermission(AppCompatActivity act, String permission) {
        return ContextCompat.checkSelfPermission(act,
                permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkRuntimePermissions(AppCompatActivity act, String[] permissions) {
        for (int i = 0; i < permissions.length; i++) {
            if (!checkRuntimePermission(act, permissions[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean isPermissionGranted(String[] requestedPermissions, String[] responcedPermissions, int[] grantResults) {
        boolean grantedAll = true;
        for (int i = 0; i < requestedPermissions.length; i++) {
            boolean result = false;
            for (int j = 0; j < responcedPermissions.length; j++) {
                if (requestedPermissions[i].equals(responcedPermissions[j]) && grantResults[j] == PackageManager.PERMISSION_GRANTED) {
                    result = true;
                }
            }
            grantedAll = grantedAll && result;
        }
        return grantedAll;
    }

    public static int[] grantedResults(String[] requestedPermissions) {
        int[] res = new int[requestedPermissions.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = PackageManager.PERMISSION_GRANTED;
        }
        return res;
    }
}
