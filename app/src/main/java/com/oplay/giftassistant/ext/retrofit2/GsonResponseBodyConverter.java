package com.oplay.giftassistant.ext.retrofit2;

import com.google.gson.Gson;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.util.encrypt.NetDataEncrypt;
import com.socks.library.KLog;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.lang.reflect.Type;

import retrofit.Converter;

/**
 * 重写Retrofit2的响应Converter，在响应返回后进行数据解密操作，再进行类型转换
 *
 * Created by zsigui on 15-12-21.
 */
final class GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
	private final Gson gson;
	private final Type type;
	private final int cmd;

	GsonResponseBodyConverter(Gson gson, Type type, int cmd) {
		this.gson = gson;
		this.type = type;
		this.cmd = cmd;
	}

	@Override
	public T convert(ResponseBody value) throws IOException {
		try {
			KLog.e("response = " + value);
			String json = NetDataEncrypt.getInstance().decrypt(value.bytes(), cmd);
			KLog.e("response = " + json);
			return gson.fromJson(json, type);
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(AppDebugConfig.TAG_ENCRYPT, e);
			}
			return null;
		}
	}
}
