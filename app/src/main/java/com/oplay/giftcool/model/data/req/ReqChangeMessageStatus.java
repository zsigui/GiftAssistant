package com.oplay.giftcool.model.data.req;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 推送消息更改状态实体
 *
 * Created by zsigui on 16-3-8.
 */
public class ReqChangeMessageStatus implements Serializable {

	/**
	 * 待修改状态消息列表
	 */
	@SerializedName("jpush_message_id_list")
	public ArrayList<Integer> pushMsgIds;

	/**
	 * 阅读状态，暂时只接受‘已读’状态
	 */
	@SerializedName("status")
	public int status;
}
