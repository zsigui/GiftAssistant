package com.oplay.giftcool.ext.retrofit2.encrypt;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.NetUrl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;


/**
 * Created by zsigui on 15-12-21.
 */
public final class GsonConverterFactory extends Converter.Factory {


    /**
     * Create an instance using a default {@link com.google.gson.Gson} instance for conversion. Encoding to JSON and
     * decoding from JSON (when no charset is specified by a header) will use UTF-8.
     */
    public static GsonConverterFactory create() {
        return create(new Gson());
    }

    /**
     * Create an instance using {@code gson} for conversion. Encoding to JSON and
     * decoding from JSON (when no charset is specified by a header) will use UTF-8.
     */
    public static GsonConverterFactory create(Gson gson) {
        return new GsonConverterFactory(gson);
    }

    private final Gson gson;

    private GsonConverterFactory(Gson gson) {
        if (gson == null) throw new NullPointerException("gson == null");
        this.gson = gson;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return new GsonResponseBodyConverter<>(gson, type, getUrlFromAnnotation(annotations));
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[]
            methodAnnotations, Retrofit retrofit) {
        return new GsonRequestBodyConverter<>(gson, type, getUrlFromAnnotation(methodAnnotations));
    }


    /**
     * 从 Method Annotation 中提取 POST/PUT/GET/DELETE 等注释的请求 URL 内容 <br />
     * 该操作对于使用 @Url @Path 等参数注释的方法无效
     */
    private String getUrlFromAnnotation(Annotation[] annotations) {
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
            result = (TextUtils.isEmpty(result)? NetUrl.getBaseUrl() + "unknown" : NetUrl.getBaseUrl() + result);
        }
        return result;
    }
}
