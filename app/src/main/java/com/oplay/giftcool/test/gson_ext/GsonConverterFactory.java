package com.oplay.giftcool.test.gson_ext;

import com.google.gson.Gson;
import com.oplay.giftcool.ext.retrofit2.http.Cmd;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import retrofit.Converter;

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
	public Converter<ResponseBody, ?> fromResponseBody(Type type, Annotation[] annotations) {
		int cmd = 0;
		for (Annotation annotation : annotations) {
			if (annotation instanceof Cmd) {
				cmd = ((Cmd) annotation).value();
			}
		}
		return new GsonResponseBodyConverter<>(gson, type, cmd);
	}

	@Override public Converter<?, RequestBody> toRequestBody(Type type, Annotation[] annotations) {
		return new GsonRequestBodyConverter<>(gson, type);
	}
}
