package com.oplay.giftcool.ext.retrofit2.encrypt;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.util.encrypt.NetDataEncrypt;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * 重写Retrofit2的响应Converter，在响应返回后进行数据解密操作，再进行类型转换
 * <p/>
 * Created by zsigui on 15-12-21.
 */
final class GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final Gson gson;
    private final Type type;
    private final String requestUrl;

    GsonResponseBodyConverter(Gson gson, Type type, String requestUrl) {
        this.gson = gson;
        this.type = type;
        this.requestUrl = requestUrl;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        try {
            String json = NetDataEncrypt.getInstance().decrypt(value.bytes(), 0);

            if (TextUtils.isEmpty(json))
                return null;

            if (AppDebugConfig.IS_DEBUG) {
                AppDebugConfig.w(AppDebugConfig.TAG_ENCRYPT, "url : " + requestUrl);
                if (json.length() > 3500) {
                    int k = json.length() / 3500;
                    int start = 0;
                    for (int i = 0; i < k + 1; i++) {
                        int end = (start + 3500 > json.length() ? json.length() : start + 3500);
                        if (i == 0) {
                            AppDebugConfig.w(AppDebugConfig.TAG_ENCRYPT, "response data : " + json.substring(start, end));
                        } else {
                            AppDebugConfig.w(AppDebugConfig.TAG_ENCRYPT, json.substring(start, end));
                        }
                        start = end;
                    }
                } else {
                    AppDebugConfig.w(AppDebugConfig.TAG_ENCRYPT, "response data : " + json);
                }
            }
            AppDebugConfig.file(AppDebugConfig.TAG_ENCRYPT, null,
                    String.format(Locale.CHINA, "url : %s\nresponse data : %s", requestUrl, json));
            return gson.fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
            AppDebugConfig.w(AppDebugConfig.TAG_ENCRYPT, e);
            return null;
        }
    }
}
