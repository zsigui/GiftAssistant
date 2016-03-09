package com.oplay.giftcool.manager;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.model.data.resp.message.PushMessageExtra;
import com.oplay.giftcool.model.data.resp.message.PushMessageApp;
import com.oplay.giftcool.ui.activity.MainActivity;
import com.oplay.giftcool.ui.fragment.gift.GiftFragment;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.SPUtil;
import com.socks.library.KLog;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.data.JPushLocalNotification;

/**
 * 推送消息管理器
 * <p/>
 * Created by zsigui on 16-3-8.
 */
public class PushMessageManager {


	public static abstract class Status {

		// 0元礼包推送
		public static final int ACTION_GIFT_ZERO = 1;
		// 每日限量礼包推送
		public static final int ACTION_GIFT_LIMIT = 2;
		// 普通礼包
		public static final int ACTION_GIFT_NEW = 3;
		// 长久未打开应用提示推送
		public static final int ACTION_LONG_UNOPEN = 4;
		// 启动应用推送
		public static final int ACTION_WAKE = 5;
	}

	// 本地JPUSH推送的Id
	public final static int LOCAL_JPUSH_ID = 10012;

	private static PushMessageManager sInstance;

	public static PushMessageManager getInstance() {
		if (sInstance == null) {
			sInstance = new PushMessageManager();
		}
		return sInstance;
	}

	private PushMessageManager() {
	}

	/**
	 * 处理推送消息行为
	 */
	public void handleShowMessage(Context context, PushMessageExtra data) {

		if (AppDebugConfig.IS_DEBUG) {
			KLog.d(AppDebugConfig.TAG_JPUSH, "MainActivity = " + MainActivity.sGlobalHolder + ", type = " + data.type
					+ " , extra = " + data.extraJson);
		}
		switch (data.type) {
			case Status.ACTION_GIFT_ZERO:
				IntentUtil.jumpHome(context, true);
				if (MainActivity.sGlobalHolder != null) {
					MainActivity.sGlobalHolder.jumpToIndexGift(GiftFragment.POS_ZERO);
				}
				break;
			case Status.ACTION_GIFT_LIMIT:
				IntentUtil.jumpGiftLimitList(context, true);
				break;
			case Status.ACTION_GIFT_NEW:
				IntentUtil.jumpHome(context, true);
				if (MainActivity.sGlobalHolder != null) {
					MainActivity.sGlobalHolder.jumpToIndexGift(GiftFragment.POS_NEW);
				}
				break;
			case Status.ACTION_LONG_UNOPEN:
				handleLongUnOpenAction(context, data);
				break;
			case Status.ACTION_WAKE:
				IntentUtil.jumpHome(context, true);
				break;
		}
	}

	/**
	 * 处理多日未打开应用的唤醒行为
	 */
	private void handleLongUnOpenAction(Context context, PushMessageExtra message) {
		long lastOpenTime =
				SPUtil.getLong(context, SPConfig.SP_APP_CONFIG_FILE, SPConfig.KEY_LOGIN_LAST_OPEN_TIME, 0);
		long curTime = System.currentTimeMillis();

		Gson gson = AssistantApp.getInstance().getGson();
		int diffDay = 3;
		boolean isForcePush = false;
		if (!TextUtils.isEmpty(message.extraJson)) {
			PushMessageApp data = gson.fromJson(message.extraJson, PushMessageApp.class);
			diffDay = data.day;
			isForcePush = data.isForcePush;
		}

		if (curTime - lastOpenTime < 1000 * 60 * 60 * 24 * diffDay) {
			// 没有相隔3天
			return;
		}
		if (!isForcePush && AssistantApp.getInstance().isPushedToday()) {
			// 今天已经推送过其他
			return;
		}

		// 进行本地推送通知
		JPushLocalNotification notification = new JPushLocalNotification();
		notification.setBuilderId(message.builderId);
		notification.setContent(message.content);
		notification.setTitle(message.title);

		// 构建通知
		PushMessageExtra extra = new PushMessageExtra();
		extra.type = Status.ACTION_WAKE;
		notification.setExtras(gson.toJson(extra, PushMessageExtra.class));
		notification.setBroadcastTime(DateUtil.getTime(message.broadcastTime));
		notification.setNotificationId(LOCAL_JPUSH_ID);
		JPushInterface.addLocalNotification(context, notification);
		AssistantApp.getInstance().setPushedToday();
	}
}
