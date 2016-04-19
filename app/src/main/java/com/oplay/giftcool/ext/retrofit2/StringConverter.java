package com.oplay.giftcool.ext.retrofit2;

import com.socks.library.KLog;

import java.io.IOException;

import retrofit2.Converter;

/**
 * Created by zsigui on 16-4-19.
 */
public class StringConverter<T> implements Converter<T, String> {

	@Override
	public String convert(T value) throws IOException {
		KLog.d("value.class = " + value.getClass() + ", String.class = " + String.class);
		if (String.class.getName().equalsIgnoreCase(value.getClass().getName())) {
			return (String)value;
		}
		return null;
	}
}