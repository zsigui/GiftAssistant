package com.oplay.giftcool.model.data.resp.message;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zsigui on 16-3-7.
 */
public class MessageData implements Serializable {

	@SerializedName("jpush_message_list")
	public ArrayList<PushMessage> mPushMessages;
}
