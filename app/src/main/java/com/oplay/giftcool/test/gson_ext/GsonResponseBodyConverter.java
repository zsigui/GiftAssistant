package com.oplay.giftcool.test.gson_ext;

import com.google.gson.Gson;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.test.TestActivity;
import com.oplay.giftcool.util.encrypt.NetDataEncrypt;
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
			String json = NetDataEncrypt.getInstance().decrypt(value.bytes(), cmd);
			KLog.d("resp json = " + json);
			KLog.d("resp cmd = " + cmd);
			TestActivity.RESP_DATA = json + "\n请求cmd = " + cmd;
			return gson.fromJson(json, type);
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(AppDebugConfig.TAG_ENCRYPT, e);
			}
			return null;
		}
	}
}