package com.oplay.giftcool.receiver;

import android.content.Context;
import android.content.Intent;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.PushMessageManager;
import com.oplay.giftcool.manager.StatisticsManager;
import com.oplay.giftcool.model.data.resp.message.PushMessageExtra;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zsigui on 16-7-6.
 */
public class MiPushReceiver extends PushMessageReceiver {

    private static final String TITLE = "title";
    private static final String CONTENT = "content";
    private static final String MSG_ID = "id";
    private static int tryCount = 0;
    private Intent mCurIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        mCurIntent = intent;
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            AppDebugConfig.d(AppDebugConfig.TAG_PUSH, "接收到推送，action = " + intent.getAction());
            if (PushMessageManager.Action.M_RECEIVED.equals(action)
                    || PushMessageManager.Action.M_OPENED.equals(action)) {
                MiPushMessage m = new MiPushMessage();
                m.setContent(intent.getStringExtra(CONTENT));
                m.setTitle(intent.getStringExtra(TITLE));
                m.setMessageId(intent.getStringExtra(MSG_ID));
                onReceiveMessage(context, m);
                if (PushMessageManager.Action.M_RECEIVED.equals(action)) {
                    onNotificationMessageArrived(context, m);
                } else {
                    onNotificationMessageClicked(context, m);
                }
            } else {
                super.onReceive(context, intent);
            }
        }
    }

    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage miPushMessage) {
        // 不显示在状态栏的自定义消息，根据需要进行额外工作
        AppDebugConfig.d(AppDebugConfig.TAG_PUSH, "onReceivePassThroughMessage : " + miPushMessage.getContent());
        AppDebugConfig.d(AppDebugConfig.TAG_PUSH, "收到的miPush = " + miPushMessage.toString());
        String extra = miPushMessage.getContent();
        if (extra == null) {
            return;
        }
        try {
            PushMessageExtra msg = AssistantApp.getInstance().getGson().fromJson(extra, PushMessageExtra
                    .class);
            mCurIntent.putExtra(MSG_ID, miPushMessage.getMessageId());
            mCurIntent.putExtra(TITLE, miPushMessage.getTitle());
            mCurIntent.putExtra(CONTENT, miPushMessage.getContent());
            PushMessageManager.getInstance().handleCustomMessage(context, msg, mCurIntent, true);
        } catch (Throwable t) {
            AppDebugConfig.w(AppDebugConfig.TAG_PUSH, t);
        }
    }

    @Override
    public void onReceiveMessage(Context context, final MiPushMessage miPushMessage) {
        // 用户接收到通知
        AppDebugConfig.d(AppDebugConfig.TAG_PUSH, "onReceiveMessage ,附加: " + miPushMessage.getContent());
        AppDebugConfig.d(AppDebugConfig.TAG_PUSH, "收到的miPush = " + miPushMessage.toString());
    }

    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage miPushMessage) {
        // 用户接收到通知
//        final String extra = miPushMessage.getContent();
        AppDebugConfig.d(AppDebugConfig.TAG_PUSH, "onNotificationMessageArrived ,附加: " + miPushMessage.getContent());
        AppDebugConfig.d(AppDebugConfig.TAG_PUSH, "收到的miPush = " + miPushMessage.toString());
//        if (extra == null) {
//            return;
//        }
        if (AppDebugConfig.IS_STATISTICS_SHOW) {
//            ThreadUtil.runInThread(new Runnable() {
//                @Override
//                public void run() {
//                    final PushMessageExtra msg = AssistantApp.getInstance().getGson().fromJson(extra, PushMessageExtra
//                            .class);
            Map<String, String> kv = new HashMap<>();
            kv.put("消息ID", miPushMessage.getMessageId());
            kv.put("推送SDK", "小米");
            kv.put("总计", String.format("%s-%s-小米", miPushMessage.getMessageId(),
                    miPushMessage.getTitle()));
            StatisticsManager.getInstance().trace(
                    context,
                    StatisticsManager.ID.PUSH_MESSAGE_RECEIVED,
                    StatisticsManager.ID.STR_PUSH_MESSAGE_RECEIVED,
                    kv, 1);
//                }
//            });
        }
    }

    @Override
    public void onNotificationMessageClicked(Context context, final MiPushMessage miPushMessage) {
        // 用户点击了通知，打开对应界面
        // 不显示在状态栏的自定义消息，根据需要进行额外工作
        String extra = miPushMessage.getContent();
        AppDebugConfig.d(AppDebugConfig.TAG_PUSH, "处理打开的消息，附加:" + extra);
        AppDebugConfig.d(AppDebugConfig.TAG_PUSH, "收到的miPush = " + miPushMessage.toString());
        if (extra == null) {
            return;
        }
        final PushMessageExtra msg = AssistantApp.getInstance().getGson().fromJson(extra, PushMessageExtra
                .class);
        PushMessageManager.getInstance().handleNotifyMessage(context, msg);
        AccountManager.getInstance().obtainUnreadPushMessageCount();

        if (AppDebugConfig.IS_STATISTICS_SHOW) {
//            ThreadUtil.runInThread(new Runnable() {
//                @Override
//                public void run() {
                    MiPushClient.reportMessageClicked(context, miPushMessage.getMessageId());
                    Map<String, String> kv = new HashMap<>();
                    kv.put("消息ID", miPushMessage.getMessageId());
                    kv.put("推送SDK", "小米");
                    kv.put("总计", String.format("%s-%s-小米", miPushMessage.getMessageId(),
                            miPushMessage.getTitle()));
                    StatisticsManager.getInstance().trace(
                            context,
                            StatisticsManager.ID.PUSH_MESSAGE_OPENED,
                            StatisticsManager.ID.STR_PUSH_MESSAGE_OPENED,
                            kv, 1);
//                }
//            });
        }
    }

    @Override
    public void onCommandResult(Context context, MiPushCommandMessage miPushCommandMessage) {
        AppDebugConfig.d(AppDebugConfig.TAG_PUSH, "onCommandResult is called : " + miPushCommandMessage.getCommand());
        super.onCommandResult(context, miPushCommandMessage);
        String command = miPushCommandMessage.getCommand();
        if (MiPushClient.COMMAND_SET_ALIAS.equals(command)) {
            if (miPushCommandMessage.getResultCode() == ErrorCode.SUCCESS) {
                AppDebugConfig.d(AppDebugConfig.TAG_PUSH, "小米设置Alias成功: " + MiPushClient.getAllAlias(context));
                PushMessageManager.getInstance().orHasSetAliasSign(PushMessageManager.SdkType.MI);
                tryCount = 0;
            } else {
                AppDebugConfig.d(AppDebugConfig.TAG_PUSH, "小米设置Alias失败");
                PushMessageManager.getInstance().andHasSetAliasSign(PushMessageManager.SdkType.MI_F);
                if (tryCount ++ < 3) {
                    PushMessageManager.getInstance().updateJPushTagAndAlias(context);
                }
            }
        }
    }
}
