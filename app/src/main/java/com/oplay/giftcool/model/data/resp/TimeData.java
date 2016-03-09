package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by mink on 16-3-7.
 */
public class TimeData<T> implements Serializable {

	@SerializedName("data")
	public T data;
	@SerializedName("date")
	public String date;
}
