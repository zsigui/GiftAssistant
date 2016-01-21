package com.oplay.giftcool.ext.gson;

import android.util.JsonToken;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Created by zsigui on 16-1-18.
 */
public class StringAdapter extends TypeAdapter<String> {
	@Override
	public void write(JsonWriter writer, String value) throws IOException {
		if (value == null) {
			writer.value("");
			return;
		}
		writer.value(value);
	}

	@Override
	public String read(JsonReader reader) throws IOException {
		if (reader.peek().ordinal() == JsonToken.NULL.ordinal()) {
			reader.nextNull();
			return null;
		}
		return reader.nextString();
	}
}
