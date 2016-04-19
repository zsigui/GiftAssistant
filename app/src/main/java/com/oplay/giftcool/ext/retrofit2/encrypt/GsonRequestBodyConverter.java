package com.oplay.giftcool.ext.retrofit2.encrypt;

import com.google.gson.Gson;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.util.encrypt.NetDataEncrypt;
import com.socks.library.KLog;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;

/**
 * 重写Retrofit2的请求Converter，在请求前进行先进行json构造，再进行数据加密操作
 *
 * Created by zsigui on 15-12-21.
 */
final class GsonRequestBodyConverter<T> implements Converter<T, RequestBody> {
	private static final MediaType MEDIA_TYPE = MediaType.parse("application/octet-stream; charset=UTF-8");

	private final Gson gson;
	private final Type type;

	GsonRequestBodyConverter(Gson gson, Type type) {
		this.gson = gson;
		this.type = type;
	}

	@Override public RequestBody convert(T value) throws IOException {
		String json = gson.toJson(value, type);
		if (AppDebugConfig.IS_DEBUG) {
			KLog.d(AppDebugConfig.TAG_ENCRYPT, "request = " + json);
		}
		byte[] data = NetDataEncrypt.getInstance().encrypt(json, 0);
		return RequestBody.create(MEDIA_TYPE, data);
	}
}
