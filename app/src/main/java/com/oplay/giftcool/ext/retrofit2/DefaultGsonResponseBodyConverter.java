package com.oplay.giftcool.ext.retrofit2;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.oplay.giftcool.config.AppDebugConfig;
import com.socks.library.KLog;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Created by zsigui on 16-4-19.
 */
final class DefaultGsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
	private final Gson gson;
	private final TypeAdapter<T> adapter;

	DefaultGsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
		this.gson = gson;
		this.adapter = adapter;
	}

	@Override public T convert(ResponseBody value) throws IOException {
		JsonReader jsonReader = gson.newJsonReader(value.charStream());
		try {
			T data = adapter.read(jsonReader);
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_WARN, data == null? "null" : data.toString());
			}
			return data;
		} finally {
			value.close();
		}
	}
}