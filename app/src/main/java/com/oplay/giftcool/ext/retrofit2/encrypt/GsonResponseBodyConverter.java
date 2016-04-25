package com.oplay.giftcool.ext.retrofit2.encrypt;

import com.google.gson.Gson;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.util.encrypt.NetDataEncrypt;
import com.socks.library.KLog;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * 重写Retrofit2的响应Converter，在响应返回后进行数据解密操作，再进行类型转换
 *
 * Created by zsigui on 15-12-21.
 */
final class GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
	private final Gson gson;
	private final Type type;
	GsonResponseBodyConverter(Gson gson, Type type) {
		this.gson = gson;
		this.type = type;
	}

	@Override
	public T convert(ResponseBody value) throws IOException {
		try {
			String json = NetDataEncrypt.getInstance().decrypt(value.bytes(), 0);
			if (AppDebugConfig.IS_DEBUG) {
				if (json.length() > 3500) {
					int k = json.length() / 3500;
					int start = 0;
					for (int i = 0; i < k + 1; i++) {
						int end = (start + 3500 > json.length() ? json.length(): start + 3500);
						if (i == 0) {
							KLog.d(AppDebugConfig.TAG_ENCRYPT, "response = " + json.substring(start, end));
						} else {
							KLog.d(AppDebugConfig.TAG_ENCRYPT, json.substring(start, end));
						}
						start = end;
					}
				} else {
					KLog.d(AppDebugConfig.TAG_ENCRYPT, "response = " + json);
				}
			}
			return gson.fromJson(json, type);
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(AppDebugConfig.TAG_ENCRYPT, e);
			}
			return null;
		}
	}
}
