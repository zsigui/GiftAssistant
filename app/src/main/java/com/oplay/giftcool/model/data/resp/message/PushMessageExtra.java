package com.oplay.giftcool.model.data.resp.message;

import android.support.v4.app.NotificationCompat;

import com.google.gson.annotations.SerializedName;
import com.oplay.giftcool.manager.PushMessageManager;

import java.io.Serializable;

/**
 * 推送消息的额外协定数据结构
 *
 * Created by zsigui on 16-3-8.
 */
public class PushMessageExtra implements Serializable {

	/**
	 * 是否静默
	 */
	@SerializedName("notify_type")
	public int notifyType = PushMessageManager.NotifyType.DEFAULT;

	/**
	 * 推送类型
	 */
	@SerializedName("push_type")
	public int type;

	/**
	 * 广播通知样式
	 */
	@SerializedName("builder_id")
	public int builderId;

	/**
	 * 内容
	 */
	@SerializedName("content")
	public String content;

	/**
	 * 标题
	 */
	@SerializedName("title")
	public String title;

	/**
	 * 通知时间
	 */
	@SerializedName("broadcast_time")
	public String broadcastTime;

	/**
	 * 额外内容
	 */
	@SerializedName("data")
	public String extraJson;

	/**
	 * 展示渠道列表字符串，使用','分割，空时不过滤显示
	 */
	@SerializedName("chnid_list")
	public String chnIdList;

	/**
	 * 通知等级，分为 -2:最低,特定情况提示 -1:低 0:正常 1:高 2:最高,非常重要紧急事件，客户端默认0，通常使用0/1两优先级即可
	 */
	@SerializedName("notify_priority")
	public int notifyPriority = NotificationCompat.PRIORITY_DEFAULT;

}
