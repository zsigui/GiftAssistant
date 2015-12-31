package com.oplay.giftassistant.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zsigui on 15-12-31.
 */
public class OneTypeGameList<T> implements Serializable{

	@SerializedName("")
	public ArrayList<T> data;

	public int page;

	public int pageSize;

	public int isEndPage;
}
