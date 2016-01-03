package com.oplay.giftassistant.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 分页返回数据对象
 *
 * Created by zsigui on 15-12-31.
 */
public class OneTypeDataList<T> implements Serializable{

	@SerializedName("")
	public ArrayList<T> data;

	public int page;

	public int pageSize = 10;

	public int isEndPage;
}
