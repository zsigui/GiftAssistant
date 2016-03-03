package com.oplay.giftcool.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.socks.library.KLog;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by zsigui on 16-3-3.
 */
public class JPushReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_JPUSH, "action: " + intent.getAction());
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
				if (AppDebugConfig.IS_DEBUG) {
					KLog.d(AppDebugConfig.TAG_JPUSH, "action: " + intent.getAction() + ", 自定义消息，内容:" + intent
							.getExtras().getString(JPushInterface.EXTRA_MESSAGE));
				}
			} else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
				// 用户点击了通知，打开对应界面
				// 不显示在状态栏的自定义消息，根据需要进行额外工作
				if (AppDebugConfig.IS_DEBUG) {
					KLog.d(AppDebugConfig.TAG_JPUSH, "action: " + intent.getAction() + ", 处理打开的消息，附加:" + intent
							.getExtras().getString(JPushInterface.EXTRA_EXTRA));
				}
			}
		}
	}
}
