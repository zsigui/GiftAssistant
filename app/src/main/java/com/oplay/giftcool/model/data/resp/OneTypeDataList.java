package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 分页返回数据对象
 *
 * Created by zsigui on 15-12-31.
 */
public class OneTypeDataList<T> implements Serializable{

	@SerializedName("data")
	public ArrayList<T> data;

	@SerializedName("page_id")
	public int page;

	@SerializedName("page_size")
	public int pageSize = 10;

	@SerializedName("is_end_page")
	public boolean isEndPage;
}
