package com.oplay.giftcool.config;

import android.os.Debug;

import com.oplay.giftcool.BuildConfig;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.util.log.GCLog;

import retrofit2.Response;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/19
 */
public class AppDebugConfig {
    /**
     * debug模式，发布打包需要置为false，可以通过混淆让调试的log文本从代码文件中消除，避免被反编译时漏泄相关信息。
     */
    public static boolean IS_DEBUG = BuildConfig.LOG_DEBUG;
    public static boolean IS_FILE_DEBUG = BuildConfig.FILE_DEBUG;
    public static boolean IS_STATISTICS_SHOW = BuildConfig.STATISTICS_DEBUG;
    public static int TEST_CHANNEL_ID = BuildConfig.TEST_CHANNEL_ID;


    public static final String TAG_APP = "giftcool_app";

    public static final String TAG_FRAG = "giftcool_fragment";

    public static final String TAG_UTIL = "giftcool_util";

    public static final String TAG_WIDGET = "giftcool_widget";

    public static final String TAG_RECEIVER = "giftcool_receiver";

    public static final String TAG_ENCRYPT = "giftcool_encrypt";

    public static final String TAG_MANAGER = "giftcool_manager";

    public static final String TAG_WEBVIEW = "giftcool_webview";

    public static final String TAG_ACTIVITY = "giftcool_activity";

    public static final String TAG_SERVICE = "giftcool_service";

    public static final String TAG_ADAPTER = "giftcool_adapter";

    public static final String TAG_CHANNEL = "giftcool_channel";

    public static final String TAG_DEBUG_INFO = "giftcool_info";
    /**
     * 所有注解该TAG的LOG需要进行删除
     */
    public static final String TAG_WARN = "giftcool_warning";

    public static final String TAG_PUSH = "giftcool_push";

    public static final String TAG_STATICS = "giftcool_statistics";

    public static final String TAG_DOWNLOAD = "giftcool_download";

    public static final String TAG_SHARE = "giftcool_share";

    public static final String TAG_GALLERY = "giftcool_gallery";

    public static final int STACKTRACE_INDEX = 6;

    public static void logMemoryInfo() {
        if (IS_DEBUG) {
            try {

                final long mb = 1024 * 1024L;
                //Get VM Heap Size by calling:
                GCLog.i(STACKTRACE_INDEX, AppDebugConfig.TAG_DEBUG_INFO,
                        "VM Heap Size:" + Runtime.getRuntime().totalMemory() / mb);

                // Get VM Heap Size Limit by calling:
                GCLog.i(STACKTRACE_INDEX, AppDebugConfig.TAG_DEBUG_INFO,
                        "VM Heap Size Limit:" + Runtime.getRuntime().maxMemory() / mb);

                // Get Allocated VM Memory by calling:
                GCLog.i(STACKTRACE_INDEX, AppDebugConfig.TAG_DEBUG_INFO,
                        "Allocated VM Memory:" + (Runtime.getRuntime().totalMemory() -
                                Runtime.getRuntime().freeMemory()) / mb);

                //Get Native Allocated Memory by calling:
                GCLog.i(STACKTRACE_INDEX, AppDebugConfig.TAG_DEBUG_INFO,
                        "Native Allocated Memory:" + Debug.getNativeHeapAllocatedSize() / mb);
            } catch (Exception e) {
                AppDebugConfig.e(AppDebugConfig.TAG_DEBUG_INFO, e);
            }
        }

    }

    public static <T> void warnResp(String tag, Response<JsonRespBase<T>> response) {
        warnResp(STACKTRACE_INDEX + 1, tag, response);
    }

    public static <T> void warnResp(int stacktraceIndex, String tag, Response<JsonRespBase<T>> response) {
        if (IS_DEBUG) {
            if (response == null || !response.isSuccessful()) {
                GCLog.w(stacktraceIndex, tag,
                        response == null ?
                                ConstString.TOAST_SERVER_ERROR : response.code() + ": " + response.message());
            } else {
                JsonRespBase<T> respBase = response.body();
                GCLog.w(stacktraceIndex, tag, respBase == null ?
                        ConstString.TOAST_SERVER_BAD_CALLBACK : respBase.error());
            }
        }
    }

    public static void w(String tag, Object... object) {
        w(STACKTRACE_INDEX + 1, tag, object);
    }

    public static void w(int stacktraceIndex, String tag, Object... object) {
        if (AppDebugConfig.IS_DEBUG) {
            GCLog.w(stacktraceIndex, tag, object);
        }
    }

    public static void v() {
        if (AppDebugConfig.IS_DEBUG) {
            GCLog.v(STACKTRACE_INDEX, AppDebugConfig.TAG_DEBUG_INFO);
        }
    }

    public static void v(String tag, Object... object) {
        if (AppDebugConfig.IS_DEBUG) {
            GCLog.v(STACKTRACE_INDEX, tag, object);
        }
    }

    public static void d(Object object) {
        if (AppDebugConfig.IS_DEBUG) {
            GCLog.d(STACKTRACE_INDEX, AppDebugConfig.TAG_DEBUG_INFO, object);
        }
    }

    public static void d(String tag, Object... object) {
        if (AppDebugConfig.IS_DEBUG) {
            GCLog.d(STACKTRACE_INDEX, tag, object);
        }
    }

    public static void e(Object object) {
        if (AppDebugConfig.IS_DEBUG) {
            GCLog.e(STACKTRACE_INDEX, AppDebugConfig.TAG_DEBUG_INFO, object);
        }
    }

    public static void e(String tag, Object... object) {
        if (AppDebugConfig.IS_DEBUG) {
            GCLog.e(STACKTRACE_INDEX, tag, object);
        }
    }

    public static void file(Object object) {
        if (AppDebugConfig.IS_DEBUG && AppDebugConfig.IS_FILE_DEBUG) {
            GCLog.file(STACKTRACE_INDEX, AppDebugConfig.TAG_DEBUG_INFO, null, null, object);
        }
    }

    public static void file(String tag, String filename, Object... object) {
        if (AppDebugConfig.IS_DEBUG && AppDebugConfig.IS_FILE_DEBUG) {
            GCLog.file(STACKTRACE_INDEX, tag, null, filename, object);
        }
    }
}
