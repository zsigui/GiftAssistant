package com.oplay.giftcool.ext.retrofit2;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.oplay.giftcool.config.AppDebugConfig;

import java.io.IOException;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Created by zsigui on 16-4-19.
 */
final class DefaultGsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final Gson gson;
    private final TypeAdapter<T> adapter;
    private final String requestUrl;

    DefaultGsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter, String requestUrl) {
        this.gson = gson;
        this.adapter = adapter;
        this.requestUrl = requestUrl;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        try {
            String json = value.string();
            T data = adapter.fromJson(json);
            if (AppDebugConfig.IS_DEBUG) {
                AppDebugConfig.w(AppDebugConfig.TAG_ENCRYPT, "url : " + requestUrl);
                if (json.length() > 3500) {
                    int k = json.length() / 3500;
                    int start = 0;
                    for (int i = 0; i < k + 1; i++) {
                        int end = (start + 3500 > json.length() ? json.length() : start + 3500);
                        if (i == 0) {
                            AppDebugConfig.w(AppDebugConfig.TAG_ENCRYPT, "response data : " + json.substring(start,
                                    end));
                        } else {
                            AppDebugConfig.w(AppDebugConfig.TAG_ENCRYPT, json.substring(start, end));
                        }
                        start = end;
                    }
                } else {
                    AppDebugConfig.w(AppDebugConfig.TAG_ENCRYPT, "response data : " + json);
                }
            }
            if (AppDebugConfig.IS_FILE_DEBUG) {
                AppDebugConfig.file(AppDebugConfig.TAG_ENCRYPT, null,
                        String.format(Locale.CHINA, "url : %s\nresponse data : %s", requestUrl, json));
            }
            return data;
        } catch (Exception e){
            AppDebugConfig.d(AppDebugConfig.TAG_ENCRYPT, e);
            return null;
        }
    }
}