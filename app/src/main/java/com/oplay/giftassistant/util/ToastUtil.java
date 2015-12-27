package com.oplay.giftassistant.util;

import android.support.annotation.StringRes;
import android.widget.Toast;

import com.oplay.giftassistant.AssistantApp;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2015/12/27
 */
public class ToastUtil {
    public static void show(CharSequence msg, int duration) {
        Toast.makeText(AssistantApp.getInstance().getApplicationContext(), msg, duration).show();
    }

    public static void show(@StringRes int resId, int duration) {
        Toast.makeText(AssistantApp.getInstance().getApplicationContext(), resId, duration).show();
    }

    public static void showLong(CharSequence msg) {
        show(msg, Toast.LENGTH_LONG);
    }

    public static void showLong(@StringRes int resId) {
        show(resId, Toast.LENGTH_LONG);
    }

    public static void showShort(CharSequence msg) {
        show(msg, Toast.LENGTH_SHORT);
    }

    public static void showShort(@StringRes int resId) {
        show(resId, Toast.LENGTH_SHORT);
    }
}
