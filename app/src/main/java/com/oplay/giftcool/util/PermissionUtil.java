package com.oplay.giftcool.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.download.silent.SilentDownloadManager;
import com.oplay.giftcool.manager.StatisticsManager;

import net.youmi.android.libs.common.util.Util_System_Permission;

/**
 * Created by zsigui on 16-5-6.
 */
public class PermissionUtil {

    private static final byte ALL_CODE = 0;
    public static final byte WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    public static final byte READ_PHONE_STATE_CODE = 2;
    public static final byte READ_SMS = 3;

    public static void judgePermission(Activity context) {
        if (!Util_System_Permission.isWith_WRITE_EXTERNAL_STORAGE_Permission(context)
                && !Util_System_Permission.isWith_READ_PHONE_STATE_Permission(context)) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_PHONE_STATE},
                    ALL_CODE);
        } else if (!Util_System_Permission.isWith_WRITE_EXTERNAL_STORAGE_Permission(context)) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        } else if (!Util_System_Permission.isWith_READ_PHONE_STATE_Permission(context)) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_PHONE_STATE},
                    READ_PHONE_STATE_CODE);
        }
    }

    public static void judgeSmsPermission(Context context, Fragment fragment){
        if (!Util_System_Permission.isWithPermission(context, Manifest.permission.READ_SMS)) {
            fragment.requestPermissions(new String[]{ Manifest.permission.READ_SMS
            }, READ_SMS);
        }
    }

    public static void doAfterRequest(Context context, int requestCode, int[] grantResults) {
        switch (requestCode) {
            case ALL_CODE:
                if (grantResults != null && grantResults.length > 0) {
                    if (grantResults.length == 1) {
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            resetExternalStorage(context);
                        }
                    } else if (grantResults.length == 2) {
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            resetExternalStorage(context);
                        }
                        if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                            resetPhoneState(context);
                        }
                    }
                }
                break;
            case WRITE_EXTERNAL_STORAGE_REQUEST_CODE:
                if (grantResults != null && grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    resetExternalStorage(context);
                }
                break;
            case READ_PHONE_STATE_CODE:
                if (grantResults != null && grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    resetPhoneState(context);
                }
                break;
        }
    }

    private static void resetPhoneState(Context context) {
        CommonUtil.initMobileInfoModel(context);
        StatisticsManager.getInstance().init(context, AssistantApp.getInstance().getChannelId());
    }

    private static void resetExternalStorage(Context context) {
        SilentDownloadManager.getInstance().resetDownloadDir();
        ApkDownloadManager.getInstance(context).resetDownloadDir();
    }
}
