package com.oplay.giftcool.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.manager.AlarmClockManager;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.manager.PushMessageManager;
import com.oplay.giftcool.util.SystemUtil;

import cn.jpush.android.service.PushService;

/**
 * Created by zsigui on 16-3-14.
 */
public class StartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || TextUtils.isEmpty(intent.getAction())) {
            AppDebugConfig.d(AppDebugConfig.TAG_RECEIVER, "empty intent or action");
            return;
        }
        try {
            String action = intent.getAction();
            AppDebugConfig.d(AppDebugConfig.TAG_RECEIVER, "action = " + action
                    + "，category = " + intent.getCategories());
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ObserverManager.getInstance().notifyUserActionUpdate(
                        ObserverManager.UserActionListener.ACTION_CHANGE_NET_STATE,
                        ObserverManager.UserActionListener.ACTION_CODE_SUCCESS);
            }
            if (!SystemUtil.isServiceRunning(context, PushService.class.getName())) {
                AppDebugConfig.d(AppDebugConfig.TAG_RECEIVER, "push service is stopped, re-initial again!");
                PushMessageManager.getInstance().initPush(context);
            } else {
                AppDebugConfig.d(AppDebugConfig.TAG_RECEIVER, "push service is running");
            }
            AlarmClockManager.getInstance().startWakeAlarm(context.getApplicationContext());
        } catch (Throwable t) {
            AppDebugConfig.w(AppDebugConfig.TAG_RECEIVER, t);
        }
    }
}
