package com.oplay.giftcool.ext.retrofit2;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;
import com.oplay.giftcool.config.AppDebugConfig;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import retrofit2.Converter;

/**
 * Created by zsigui on 16-4-19.
 */
public final class DefaultGsonRequestBodyConverter<T> implements Converter<T, RequestBody> {
	private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");
	private static final Charset UTF_8 = Charset.forName("UTF-8");

	private final Gson gson;
	private final TypeAdapter<T> adapter;

	DefaultGsonRequestBodyConverter(Gson gson, TypeAdapter<T> adapter) {
		this.gson = gson;
		this.adapter = adapter;
	}

	@Override public RequestBody convert(T value) throws IOException {
		AppDebugConfig.d(AppDebugConfig.TAG_UTIL, value == null ? "null" : gson.toJson(value));
		if (value != null && String.class.getName().equalsIgnoreCase(value.getClass().getName())) {
			return RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), (String)value);
		} else {
			Buffer buffer = new Buffer();
			Writer writer = new OutputStreamWriter(buffer.outputStream(), UTF_8);
			JsonWriter jsonWriter = gson.newJsonWriter(writer);
			adapter.write(jsonWriter, value);
			jsonWriter.close();
			return RequestBody.create(MEDIA_TYPE, buffer.readByteString());
		}
	}
}