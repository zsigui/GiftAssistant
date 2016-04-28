package com.oplay.giftcool.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.PushMessageManager;
import com.oplay.giftcool.manager.StatisticsManager;
import com.oplay.giftcool.model.data.resp.message.PushMessageExtra;
import com.socks.library.KLog;

import java.util.HashMap;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by zsigui on 16-3-3.
 */
public class JPushReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		long startTime = System.currentTimeMillis();
		if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_JPUSH, "action: " + intent.getAction());
			}
			try {
				if (context == null) {
					context = AssistantApp.getInstance().getApplicationContext();
				}
				if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
					if (!JPushInterface.getConnectionState(context)) {
						// 此时JPush连接中断
						if (AppDebugConfig.IS_DEBUG) {
							boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
							KLog.d(AppDebugConfig.TAG_JPUSH, "action: " + intent.getAction() + ", connected:" +
									connected);
						}
						// 重新连接
						JPushInterface.init(AssistantApp.getInstance().getApplicationContext());
					}
				} else if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
					// 传递RegistrationId给服务器
					if (AppDebugConfig.IS_DEBUG) {
						KLog.d(AppDebugConfig.TAG_JPUSH, "action: " + intent.getAction()
								+ ", register id:" + JPushInterface.getRegistrationID(context));
					}
				} else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
					// 不显示在状态栏的自定义消息，根据需要进行额外工作
					String extra = intent.getExtras().getString(JPushInterface.EXTRA_EXTRA);
					if (AppDebugConfig.IS_DEBUG) {
						KLog.d(AppDebugConfig.TAG_JPUSH, "action: " + intent.getAction() + ", 自定义消息，内容:" + extra);
					}
					if (extra == null) {
						return;
					}
					if (AssistantApp.getInstance().getGson() == null) {
						AssistantApp.getInstance().initGson();
					}
					PushMessageExtra msg = AssistantApp.getInstance().getGson().fromJson(extra, PushMessageExtra
							.class);
					PushMessageManager.getInstance().handleCustomMessage(context, msg, intent);
//					PushMessageManager.getInstance().handleNotifyMessage(context, msg);

				} else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
					// 用户点击了通知，打开对应界面
					// 不显示在状态栏的自定义消息，根据需要进行额外工作
					final Bundle bundle = intent.getExtras();
					String extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
					if (AppDebugConfig.IS_DEBUG) {
						KLog.d(AppDebugConfig.TAG_JPUSH, "action: " + intent.getAction() + ", 处理打开的消息，附加:" + extra);
					}
					if (extra == null) {
						return;
					}
					if (AssistantApp.getInstance().getGson() == null) {
						AssistantApp.getInstance().initGson();
					}
					final PushMessageExtra msg = AssistantApp.getInstance().getGson().fromJson(extra, PushMessageExtra
							.class);
					PushMessageManager.getInstance().handleNotifyMessage(context, msg, intent);
					AccountManager.getInstance().obtainUnreadPushMessageCount();

					if (AppDebugConfig.IS_STATISTICS_SHOW) {

						Map<String, String> kv = new HashMap<>();
						String msgId = bundle.getString(JPushInterface.EXTRA_MSG_ID);
						kv.put("消息ID", msgId);
						kv.put("消息标题", msg.title);
						kv.put("消息内容", msg.content);
						kv.put("总计", String.format("%s-%s-%s", msgId, msg.title, msg.content));
						StatisticsManager.getInstance().trace(
								AssistantApp.getInstance().getApplicationContext(),
								StatisticsManager.ID.PUSH_MESSAGE_OPENED,
								StatisticsManager.ID.STR_PUSH_MESSAGE_OPENED,
								kv, 1);
					}
				} else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
					// 用户接收到通知
					final Bundle bundle = intent.getExtras();
					String extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
					if (AppDebugConfig.IS_DEBUG) {
						KLog.d(AppDebugConfig.TAG_JPUSH, "action: " + intent.getAction() + ", 接收到通知消息,附加: "
								+ bundle.getString(JPushInterface.EXTRA_EXTRA));
					}
					if (extra == null) {
						return;
					}
					if (AssistantApp.getInstance().getGson() == null) {
						AssistantApp.getInstance().initGson();
					}
					final PushMessageExtra msg = AssistantApp.getInstance().getGson().fromJson(extra, PushMessageExtra
							.class);
					if (AppDebugConfig.IS_STATISTICS_SHOW) {
						Map<String, String> kv = new HashMap<>();
						String msgId = bundle.getString(JPushInterface.EXTRA_MSG_ID);
						kv.put("消息ID", msgId);
						kv.put("消息标题", msg.title);
						kv.put("消息内容", msg.content);
						kv.put("总计", String.format("%s-%s-%s", msgId, msg.title, msg.content));
						StatisticsManager.getInstance().trace(
								AssistantApp.getInstance().getApplicationContext(),
								StatisticsManager.ID.PUSH_MESSAGE_RECEIVED,
								StatisticsManager.ID.STR_PUSH_MESSAGE_RECEIVED,
								kv, 1);
					}
				}
			} catch (Throwable t) {
				if (AppDebugConfig.IS_DEBUG) {
					KLog.d(AppDebugConfig.TAG_JPUSH, t);
				}
			} finally {
				if (AppDebugConfig.IS_DEBUG) {
					KLog.d(AppDebugConfig.TAG_JPUSH, "deal time is " + (System.currentTimeMillis() - startTime) + " " +
							"ms");
				}
			}
		}
	}
}
