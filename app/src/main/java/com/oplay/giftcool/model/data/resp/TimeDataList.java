package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2016/1/3
 */
public class TimeDataList<T> implements Serializable{

	@SerializedName("data")
    public ArrayList<T> data;

	@SerializedName("date")
    public String date;

	@SerializedName("page_id")
	public int page;

	@SerializedName("page_size")
	public int pageSize;
}
