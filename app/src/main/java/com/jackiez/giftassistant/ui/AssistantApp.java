package com.jackiez.giftassistant.ui;

import android.app.Application;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class AssistantApp extends Application {

    private static AssistantApp sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static AssistantApp getInstance() {
        return sInstance;
    }
}
