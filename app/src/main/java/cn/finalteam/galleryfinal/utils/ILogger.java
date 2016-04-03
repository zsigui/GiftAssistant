package cn.finalteam.galleryfinal.utils;


import com.oplay.giftcool.config.AppDebugConfig;
import com.socks.library.KLog;

/**
 * Desction:
 * Author:pengjianbo
 * Date:2016/1/29 0029 17:51
 */
public final class ILogger {

    public static final String DEFAULT_TAG = "GalleryFinal";
    private static final boolean DEBUG = AppDebugConfig.IS_DEBUG;

    //no instance
    private ILogger() {
    }

    public static void d(String message, Object... args) {
        if (DEBUG) {
            KLog.d(DEFAULT_TAG, message, args);
        }
    }

    public static void e(Throwable throwable) {
        if (DEBUG) {
            KLog.e(DEFAULT_TAG, throwable);
        }
    }

    public static void e(String message, Object... args) {
        if (DEBUG) {
            KLog.e(DEFAULT_TAG, message, args);
        }
    }

    public static void e(Throwable throwable, String message, Object... args) {
        if (DEBUG) {
            KLog.e(DEFAULT_TAG, throwable, message, args);
        }
    }

    public static void i(String message, Object... args) {
        if (DEBUG) {
            KLog.i(DEFAULT_TAG, message, args);
        }
    }

    public static void v(String message, Object... args) {
        if (DEBUG) {
            KLog.v(DEFAULT_TAG, message, args);
        }
    }

    public static void w(String message, Object... args) {
        if (DEBUG) {
            KLog.w(DEFAULT_TAG, message, args);
        }
    }

    public static void a(String message, Object... args) {
        if (DEBUG) {
            KLog.a(DEFAULT_TAG, message, args);
        }
    }

    /**
     * Formats the json content and print it
     *
     * @param json the json content
     */
    public static void json(String json) {
        if (DEBUG) {
            KLog.json(json);
        }
    }

    /**
     * Formats the json content and print it
     *
     * @param xml the xml content
     */
    public static void xml(String xml) {
        if (DEBUG) {
            KLog.xml(xml);
        }
    }


}
