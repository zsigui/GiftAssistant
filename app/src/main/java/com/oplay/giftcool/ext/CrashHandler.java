package com.oplay.giftcool.ext;

import android.os.Process;

import com.oplay.giftcool.config.AppDebugConfig;

/**
 * Created by zsigui on 16-6-20.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static CrashHandler sInstance;
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    private CrashHandler() {}

    public static CrashHandler getInstance() {
        if (sInstance == null) {
            sInstance = new CrashHandler();
        }
        return sInstance;
    }

    public void init() {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleThrowable(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            Process.killProcess(Process.myPid());
            System.exit(1);
        }
    }

    private boolean handleThrowable(Throwable ex) {
        if (ex == null)
            return false;

        ex.printStackTrace();
        AppDebugConfig.file(AppDebugConfig.TAG_APP, "crash.log", ex);
        return true;
    }
}
