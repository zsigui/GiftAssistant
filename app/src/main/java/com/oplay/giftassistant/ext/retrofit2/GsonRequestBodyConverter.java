package com.oplay.giftassistant.ext.retrofit2;

import com.google.gson.Gson;
import com.oplay.giftassistant.util.encrypt.NetDataEncrypt;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;
import java.lang.reflect.Type;

import retrofit.Converter;

/**
 * 重写Retrofit2的请求Converter，在请求前进行先进行json构造，再进行数据加密操作
 *
 * Created by zsigui on 15-12-21.
 */
final class GsonRequestBodyConverter<T> implements Converter<T, RequestBody> {
	private static final MediaType MEDIA_TYPE = MediaType.parse("application/octet-stream; charset=UTF-8");

	private final Gson gson;
	private final Type type;
	private final int cmd;

	GsonRequestBodyConverter(Gson gson, Type type, int cmd) {
		this.gson = gson;
		this.type = type;
		this.cmd = cmd;
	}

	@Override public RequestBody convert(T value) throws IOException {
		String json = gson.toJson(value, type);
		byte[] data = NetDataEncrypt.getInstance().encrypt(json, cmd);
		return RequestBody.create(MEDIA_TYPE, data);
	}
}
