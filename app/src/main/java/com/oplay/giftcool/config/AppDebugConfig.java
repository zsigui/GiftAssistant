package com.oplay.giftcool.config;

import android.os.Debug;

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
//    public static final boolean IS_DEBUG = false;
//	public static final boolean IS_STATISTICS_SHOW = true;
    public static final boolean IS_DEBUG = true;
    public static final boolean IS_STATISTICS_SHOW = false;
    public static final int TEST_CHANNEL_ID = 10000;


    public static final String TAG_APP = "gcool_debug_app";

    public static final String TAG_FRAG = "gcool_debug_fragment";
    /* define some tag for debug below */
    public static final String TAG_SEARCH = "gcool_debug_search";

    public static final String TAG_UTIL = "gcool_debug_util";

    public static final String TAG_RECEIVER = "gcool_debug_receiver";

    public static final String TAG_ENCRYPT = "gcool_debug_encrypt";

    public static final String TAG_MANAGER = "gcool_debug_manager";

    public static final String TAG_WEBVIEW = "gcool_debug_webview";

    public static final String TAG_ACTIVITY = "gcool_debug_activity";

    public static final String TAG_SERVICE = "gcool_debug_service";

    public static final String TAG_ADAPTER = "gcool_debug_adapter";

    public static final String TAG_CHANNEL = "gcool_debug_channel";

    public static final String TAG_DEBUG_INFO = "gcool_debug_info";
    /**
     * 所有注解该TAG的LOG需要进行删除
     */
    public static final String TAG_WARN = "gcool_debug_warning";

    public static final String TAG_JPUSH = "gcool_debug_jpush";

    public static final String TAG_STATICS = "gcool_debug_statistics";

    public static final String TAG_DOWNLOAD = "gcool_debug_download";

    public static final String TAG_SHARE = "gcool_debug_share";

    public static final String TAG_GALLERY = "gcool_debug_gallery";

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
        if (AppDebugConfig.IS_DEBUG) {
            GCLog.file(STACKTRACE_INDEX, AppDebugConfig.TAG_DEBUG_INFO, null, null, object);
        }
    }

    public static void file(String tag, Object... object) {
        if (AppDebugConfig.IS_DEBUG) {
            GCLog.file(STACKTRACE_INDEX, tag, null, null, object);
        }
    }
}
