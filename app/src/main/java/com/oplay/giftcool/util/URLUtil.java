package com.oplay.giftcool.util;

import android.text.TextUtils;

import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.NetUrl;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

/**
 * Created by zsigui on 16-1-22.
 */
public class URLUtil {
    public static String getHost(String url) {
        if (!TextUtils.isEmpty(url)) {
            String[] parts = url.split("\\?");
            if (parts.length > 0) {
                return parts[0];
            }
        }
        return url;
    }

    public static Map<String, String> getParams(String url) {
        Map<String, String> params = new HashMap<>();
        if (!TextUtils.isEmpty(url)) {
            String[] parts = url.split("\\?");
            if (parts.length > 1) {
                String paramStr = parts[1];
                String[] pair = paramStr.split("&");
                if (pair.length > 0) {
                    for (int i = 0; i < pair.length; i++) {
                        String[] kv = pair[i].split("=");
                        params.put(kv[0].trim(), kv[1].trim());
                    }
                }
            }
        }
        return params;
    }

    /**
     * 从 Method Annotation 中提取 POST/PUT/GET/DELETE 等注释的请求 URL 内容 <br />
     * 该操作对于使用 @Url @Path 等参数注释的方法无效
     */
    public static String getUrlFromAnnotation(Annotation[] annotations) {

        String result = null;
        try {
            for (Annotation annotation : annotations) {
                if (annotation instanceof POST) {
                    result = ((POST) annotation).value();
                } else if (annotation instanceof GET) {
                    result = ((GET) annotation).value();
                } else if (annotation instanceof PUT) {
                    result = ((PUT) annotation).value();
                } else if (annotation instanceof DELETE) {
                    result = ((DELETE) annotation).value();
                }
            }
        } catch (Throwable t) {
            AppDebugConfig.w(AppDebugConfig.TAG_ENCRYPT, t);
        } finally {
            result = (TextUtils.isEmpty(result) ? NetUrl.getBaseUrl() + "unknown" : NetUrl.getBaseUrl() + result);
        }
        return result;
    }
}
