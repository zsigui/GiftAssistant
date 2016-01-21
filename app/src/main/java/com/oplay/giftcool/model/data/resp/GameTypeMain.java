package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-1-10.
 */
public class GameTypeMain implements Serializable{

	@SerializedName("label_id")
	public int id;

	public int icon;

	@SerializedName("name")
	public String name;

}
