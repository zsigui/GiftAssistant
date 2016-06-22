package com.oplay.giftcool.util.log;

import android.text.TextUtils;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.FileUtil;

import java.io.File;

/**
 * Created by zsigui on 16-6-21.
 */
public class FileLog {

    private static boolean sNeedWriteLog = false;
    private static String debugDirectory;
    private static String debugFileName;

    public static String getDebugDirectory() {
        initDebugFile();
        return debugDirectory;
    }


    public static String getDebugFileName() {
        initDebugFile();
        return debugFileName;
    }

    private static void initDebugFile() {
        if (debugDirectory == null || debugFileName == null) {
            File f = FileUtil.getOwnCacheDirectory(
                    AssistantApp.getInstance().getApplicationContext(), Global.LOGGING_CACHE_PATH, true);
            debugDirectory = f.getAbsolutePath();
            debugFileName = DateUtil.formatTime(System.currentTimeMillis(), "yyyyMMdd") + ".log";
        }
    }

    public static void printFile(String directory, String filename, String content) {
        printFile(directory, filename, content, null, true);
    }

    public static void printFile(String directory, String filename, String content, String charset, boolean isAppend) {
        if (!sNeedWriteLog) {
            return;
        }
        if (TextUtils.isEmpty(directory)) {
            directory = getDebugDirectory();
        }
        if (TextUtils.isEmpty(filename)) {
            filename = getDebugFileName();
        }
        if (TextUtils.isEmpty(charset)) {
            charset = FileUtil.DEFAULT_CHASET;
        }
        FileUtil.writeString(new File(directory, filename), content, charset, isAppend);
    }
}
