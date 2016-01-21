package com.oplay.giftcool.test.gson_ext;

import com.google.gson.Gson;
import com.oplay.giftcool.test.TestActivity;
import com.oplay.giftcool.util.encrypt.NetDataEncrypt;
import com.socks.library.KLog;
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

	GsonRequestBodyConverter(Gson gson, Type type) {
		this.gson = gson;
		this.type = type;
	}

	@Override public RequestBody convert(T value) throws IOException {
		String json = gson.toJson(value, type);
		KLog.d("req json = " + json);
		int cmd = getCmdByJson(json);
		KLog.d("req cmd = " + cmd);
		TestActivity.REQ_DATA = json + "\n请求cmd = " + cmd;
		byte[] data = NetDataEncrypt.getInstance().encrypt(json, cmd);
		return RequestBody.create(MEDIA_TYPE, data);
	}

	private int getCmdByJson(String json) {
		int t = json.indexOf(":", json.indexOf("\"cmd\"") + 5) + 1;
		return Integer.parseInt(json.substring(t, json.indexOf(",", t + 1)));
	}
}
