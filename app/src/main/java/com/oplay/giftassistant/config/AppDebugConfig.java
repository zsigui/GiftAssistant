package com.oplay.giftassistant.config;

import android.os.Debug;
import android.util.Log;

import com.socks.library.KLog;

import java.lang.reflect.Field;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/19
 */
public class AppDebugConfig {
    /**
     * debug模式，发布打包需要置为false，可以通过混淆让调试的log文本从代码文件中消除，避免被反编译时漏泄相关信息。
     */
    public static final boolean IS_DEBUG = true;
    public static final boolean IS_FRAG_DEBUG = true;
	public static final boolean IS_PERM_DEBUG = true;

    public static final String TAG_APP = "op_app_debug";

    public static final String TAG_FRAG = "op_fragment_debug";
    /* define some tag for debug below */
    public static final String TAG_SEARCH = "op_search_debug";

    public static final String TAG_UTIL = "op_util_debug";

	public static final String TAG_ENCRYPT = "data_encrypt_debug";

	public static final String TAG_MANAGER = "data_manager_debug";




    public static void logMethodName(Object object) {
        if (IS_DEBUG) {
            try {
                Log.v(getLogTag(object), getMethodName());
            } catch (Throwable e) {
                if (IS_DEBUG) {
                    KLog.e(e);
                }
            }
        }
    }

    public static void logMethodName(Class<?> cls) {
        if (IS_DEBUG) {
            try {
                Log.v(getLogTag(cls), getMethodName());
            } catch (Throwable e) {
                if (IS_DEBUG) {
                    KLog.e(e);
                }
            }
        }
    }

    private static String getLogTag(Object object) {
        if (object instanceof String) {
            return (String) object;
        } else if (object == null) {
            return "[Null]";
        }
        return object.getClass().getSimpleName() + "[" + object.hashCode() + "]";
    }

    private static String getMethodName() {
        final Thread current = Thread.currentThread();
        final StackTraceElement trace = current.getStackTrace()[4];
        return trace.getMethodName();
    }

    public static void logParams(String tag, Object... params) {
        if (IS_DEBUG) {
            for (Object obj : params) {
                Log.i(tag, "" + obj);
            }
        }
    }

    public static void logNetworkRequest(Object object, String request, String response) {
        if (IS_DEBUG) {
            Log.i(getLogTag(object), String.format("【Request】:%s", request));
            Log.i(getLogTag(object), String.format("【Response】:%s", response));
        }
    }

    public static void logFields(Class<?> classType) {
        if (IS_DEBUG) {
            try {
                final String name = classType.getSimpleName();
                final Field[] fs = classType.getDeclaredFields();
                for (Field f : fs) {
                    Log.i(name, "Filed:" + f.getName());
                }
            } catch (Exception e) {
                if (IS_DEBUG) {
                    KLog.e(e);
                }
            }
        }
    }

    public static void logMethodWithParams(Object object, Object... params) {
        if (IS_DEBUG) {
            try {
                final StringBuilder sb = new StringBuilder();
                sb.append("{").append(Thread.currentThread().getName()).append("}")
                        .append(getMethodName()).append(":");
                for (Object obj : params) {
                    sb.append('[').append(obj).append("], ");
                }
                Log.v(getLogTag(object), sb.toString());
            } catch (Exception e) {
                if (IS_DEBUG) {
                    KLog.e(e);
                }
            }
        }
    }

    public static void logMemoryInfo() {
        if (IS_DEBUG) {
            try {

                final String tag = "MM_INFO";

                final long mb = 1024 * 1024l;
                //Get VM Heap Size by calling:
                Log.i(tag, "VM Heap Size:" + Runtime.getRuntime().totalMemory() / mb);

                // Get VM Heap Size Limit by calling:
                Log.i(tag, "VM Heap Size Limit:" + Runtime.getRuntime().maxMemory() / mb);

                // Get Allocated VM Memory by calling:
                Log.i(tag, "Allocated VM Memory:" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
                        .freeMemory()) / mb);

                //Get Native Allocated Memory by calling:
                Log.i(tag, "Native Allocated Memory:" + Debug.getNativeHeapAllocatedSize() / mb);
            } catch (Exception e) {
                if (IS_DEBUG) {
                    KLog.e(e);
                }
            }
        }

    }
}
