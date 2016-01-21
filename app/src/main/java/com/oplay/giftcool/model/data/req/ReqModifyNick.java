package com.oplay.giftcool.model.data.req;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 修改用户昵称请求
 *
 * Created by zsigui on 16-1-5.
 */
public class ReqModifyNick implements Serializable {

	@SerializedName("new_nick")
	public String newNick;

	@SerializedName("old_nick")
	public String oldNick;
}
