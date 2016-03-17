package com.oplay.giftcool.manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.GameTypeUtil;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.model.data.resp.message.PushMessageApp;
import com.oplay.giftcool.model.data.resp.message.PushMessageDetail;
import com.oplay.giftcool.model.data.resp.message.PushMessageExtra;
import com.oplay.giftcool.ui.activity.MainActivity;
import com.oplay.giftcool.ui.fragment.gift.GiftFragment;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.SPUtil;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Locale;

import cn.jpush.android.api.CustomPushNotificationBuilder;
import cn.jpush.android.api.JPushInterface;

/**
 * 推送消息管理器
 * <p/>
 * Created by zsigui on 16-3-8.
 */
public class PushMessageManager {

	/**
	 * 推送消息的类型
	 */
	public static abstract class Status {

		// 0元礼包推送
		public static final int ACTION_GIFT_ZERO = 1;
		// 每日限量礼包推送
		public static final int ACTION_GIFT_LIMIT = 2;
		// 普通礼包
		public static final int ACTION_GIFT_NEW = 3;
		// 长久未打开应用提示推送，该推送为静默推送
		public static final int ACTION_LONG_UNOPEN = 4;
		// 启动应用推送
		public static final int ACTION_WAKE = 5;
		// 跳转礼包详情页面
		public static final int ACTION_GIFT_DETAIL = 6;
		// 跳转游戏详情页面
		public static final int ACTION_GAME_DETAIL = 7;
	}

	/**
	 * 设定通知的类型
	 */
	public static abstract class NotifyType {
		/**
		 * 默认状态，默认通知
		 */
		public static final int DEFAULT = 0;
		/**
		 * 通知提醒
		 */
		public static final int NOTIFY = 1;
		/**
		 * 静默，且不统计
		 */
		public static final int SLIENT = 2;
		/**
		 * 静默，但需要统计
		 */
		public static final int SLIENT_BUT_STATICS = 3;
	}

	/**
	 * 设定通知优先级
	 */
	public static abstract class NotifyPriority {

		/**
		 * 低优先级
		 */
		public static final int LOW = -1;
		/**
		 * 正常通知方式
		 */
		public static final int NORMAL = 0;
		/**
		 * 着重提醒方式
		 */
		public static final int HIGH = 1;
	}

	// 本地JPUSH推送的Id
	public static int LOCAL_JPUSH_ID = 10012;
	private static final int BROADCAST_REQUEST = 1;
	private static PushMessageManager sInstance;
	private WeakReference<Bitmap> mBitmapWeakReference;
	private NotificationManager mNotificationManager;

	// 无震动响铃的起始接收时间点
	private final int START_HOUR = 22;
	private final int START_MINUTE = 30;
	private final int END_HOUR = 8;
	private final int END_MINUTE = 30;

	public static PushMessageManager getInstance() {
		if (sInstance == null) {
			sInstance = new PushMessageManager();
		}
		return sInstance;
	}

	private PushMessageManager() {
	}

	public NotificationManager getNotificationManager(Context context) {
		if (mNotificationManager == null) {
			mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		return mNotificationManager;
	}

	/**
	 * 初始化推送设置
	 */
	public void initPush(Context context) {
		JPushInterface.init(context);
		JPushInterface.setDebugMode(AppConfig.TEST_MODE);
		// 设置通知静默时间，不震动和响铃，晚上10点30分-早上8点
		JPushInterface.setSilenceTime(context, START_HOUR, START_MINUTE, END_HOUR, END_MINUTE);
		// 设置默认的通知栏样式
		CustomPushNotificationBuilder builder = new CustomPushNotificationBuilder(
				context, R.layout.view_status_bar_notify, R.id.iv_icon, R.id.tv_title, R.id.tv_content);
		builder.statusBarDrawable = R.mipmap.ic_launcher;
		builder.notificationFlags = PendingIntent.FLAG_ONE_SHOT;
		builder.layoutIconDrawable = R.mipmap.ic_launcher;
		JPushInterface.setDefaultPushNotificationBuilder(builder);
		// 设置保留最近通知条数 5
		JPushInterface.setLatestNotificationNumber(context, 5);

	}

	/**
	 * 重新初始化推送设置，会判断推送服务是否存在，不存在则重启，存在则不做处理
	 */
	public void reInitPush(Context context) {
//		if (JPushInterface.isPushStopped(context)) {
			JPushInterface.init(context);
//		}
	}

	/**
	 * 处理多日未打开应用的唤醒行为
	 */
	private void handleLongUnOpenAction(Context context, PushMessageExtra message, Intent intent) {

		long lastOpenTime =
				SPUtil.getLong(context, SPConfig.SP_APP_CONFIG_FILE, SPConfig.KEY_LAST_PUSH_TIME, 0);
		long curTime = System.currentTimeMillis();

		Gson gson = AssistantApp.getInstance().getGson();
		int diffDay = 3;
		boolean isForcePush = false;
		if (!TextUtils.isEmpty(message.extraJson)) {
			PushMessageApp data = gson.fromJson(message.extraJson, PushMessageApp.class);
			diffDay = data.day;
			isForcePush = data.isForcePush;
		}

		if (!isForcePush && (AssistantApp.getInstance().isPushedToday()
				|| curTime - lastOpenTime < 1000 * 60 * 60 * 24 * diffDay)) {
			// 今天已经推送过其他
			return;
		}

		// 进行本地推送通知
		buildNotification(context, message, intent);
		// 构建通知
		message.type = Status.ACTION_WAKE;

		Notification n = buildNotification(context, message, intent);
		getNotificationManager(context).notify(LOCAL_JPUSH_ID++, n);
		AssistantApp.getInstance().setPushedToday();
	}

	/**
	 * 处理礼包/游戏详情页面
	 */
	private void handleDetailAction(Context context, PushMessageExtra message, boolean isGift) {
		if (TextUtils.isEmpty(message.extraJson)) {
			// 额外数据部分不能为空
			return;
		}
		Gson gson = AssistantApp.getInstance().getGson();
		PushMessageDetail data = gson.fromJson(message.extraJson, PushMessageDetail.class);
		if (data.status == 0) {
			data.status = GameTypeUtil.JUMP_STATUS_DETAIL;
		}
		if (isGift) {
			IntentUtil.jumpGiftDetail(context, data.id, true);
		} else {
			IntentUtil.jumpGameDetail(context, data.id, data.status, true);
		}
	}

	/**
	 * 处理广播收到的通知栏消息
	 */
	public void handleNotifyMessage(Context context, PushMessageExtra data, Intent intent) {

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
			case Status.ACTION_WAKE:
				IntentUtil.jumpHome(context, true);
				break;
			case Status.ACTION_GIFT_DETAIL:
				handleDetailAction(context, data, true);
				break;
			case Status.ACTION_GAME_DETAIL:
				handleDetailAction(context, data, false);
				break;
		}
	}


	/**
	 * 处理自定义消息
	 */
	public void handleCustomMessage(Context context, PushMessageExtra msg, Intent intent) {
		if (isFilter(msg)) {
			//
			return;
		}
		switch (msg.notifyType) {
			case NotifyType.SLIENT:
				// 直接处理静默推送
				handleSilentMessage(context, msg, intent);
				break;
			case NotifyType.SLIENT_BUT_STATICS:
				// 复制意图消息，广播已经接收到了通知推送
				broadcastReceived(context, intent);
				break;
			case NotifyType.NOTIFY:
			case NotifyType.DEFAULT:
				// 复制意图消息，广播已经接收到了通知推送
				broadcastReceived(context, intent);
				// 创建显示在状态栏的通知消息
				Notification notification = buildNotification(context, msg, intent);
				getNotificationManager(context).notify(PushMessageManager.LOCAL_JPUSH_ID, notification);
		}
	}

	/**
	 * 当前应用所在渠道是否过滤对该信息的处理
	 */
	private boolean isFilter(PushMessageExtra msg) {
		if (!TextUtils.isEmpty(msg.chnIdList)) {
			String curChnId = String.valueOf(AssistantApp.getInstance().getChannelId());
			for (String chnId : msg.chnIdList.split(",")) {
				if (chnId.length() > 0 && curChnId.equals(chnId.trim())) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * 处理静默消息
	 */
	private void handleSilentMessage(Context context, PushMessageExtra data, Intent intent) {
		switch (data.type) {
			case Status.ACTION_LONG_UNOPEN:
				data.type = Status.ACTION_WAKE;
				Bundle bundle = intent.getExtras();
				bundle.putString(JPushInterface.EXTRA_EXTRA,
						AssistantApp.getInstance().getGson().toJson(data, PushMessageExtra.class));
				intent.putExtras(bundle);
				handleLongUnOpenAction(context, data, intent);
				break;
		}
	}

	/**
	 * 通知广播消息已经接收到
	 *
	 * @param context 上下文
	 * @param intent  接收到的消息意图
	 */
	private void broadcastReceived(Context context, Intent intent) {
		Intent broadcastIntent = (Intent) intent.clone();
		broadcastIntent.setAction(JPushInterface.ACTION_NOTIFICATION_RECEIVED);
		context.sendBroadcast(broadcastIntent);
	}

	/**
	 * 创建默认的通知消息
	 */
	private Notification buildNotification(Context context, PushMessageExtra msg, Intent intent) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		if (mBitmapWeakReference == null || mBitmapWeakReference.get() == null) {
			Bitmap bp = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
			mBitmapWeakReference = new WeakReference<Bitmap>(bp);
		}
		builder.setLargeIcon(mBitmapWeakReference.get());
		builder.setContentText(msg.content);
		builder.setContentTitle(TextUtils.isEmpty(msg.title) ?
				context.getResources().getString(R.string.app_name) : msg.title);
		builder.setAutoCancel(true);
//		builder.setExtras(intent.getExtras());
		// 设置通知时间
		String date;
		if (!TextUtils.isEmpty(msg.broadcastTime)) {
			date = msg.broadcastTime;
			builder.setWhen(DateUtil.getTime(msg.broadcastTime));
		} else {
			long curTime = System.currentTimeMillis();
			date = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss", Locale.CHINA).format(curTime);
			builder.setWhen(curTime);
		}
		int hour = 12;
		int minute = 0;
		try {
			hour = Integer.parseInt(date.substring(12, 14));
			minute = Integer.parseInt(date.substring(15, 17));
		} catch (Exception ignored) {}
		if ((hour == START_HOUR && minute > START_MINUTE)
				|| (hour == END_HOUR && minute <= END_MINUTE)
				|| hour > START_HOUR
				|| hour < END_HOUR) {
			builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS);
		} else {
			builder.setDefaults(NotificationCompat.DEFAULT_ALL);
		}


		// 设置状态栏图标
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			builder.setSmallIcon(R.drawable.ic_notification_144);
			builder.setColor(Color.RED);
		} else {
			builder.setSmallIcon(R.drawable.ic_stat_notify);
		}


		builder.setTicker(msg.content);
		builder.setPriority(msg.notifyPriority);

		// 设置通知意图
		intent.setAction(JPushInterface.ACTION_NOTIFICATION_OPENED);
		PendingIntent pendingIntent =
				PendingIntent.getBroadcast(context, BROADCAST_REQUEST, intent, PendingIntent
						.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);
		Notification notification = builder.build();
		notification.flags = NotificationCompat.FLAG_SHOW_LIGHTS | NotificationCompat.FLAG_AUTO_CANCEL;
		return notification;
	}

	/**
	 * 退出前进行清理工作
	 */
	public void exit(Context context) {
		JPushInterface.clearLocalNotifications(context);
//		JPushInterface.onKillProcess(context);
	}
}
