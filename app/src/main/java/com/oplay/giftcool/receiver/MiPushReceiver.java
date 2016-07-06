package com.oplay.giftcool.receiver;

/**
 * Created by zsigui on 16-7-6.
 */
//public class MiPushReceiver extends PushMessageReceiver {
//
//    private final String EXTRA_NAME = "extra";
//
//    @Override
//    public void onReceivePassThroughMessage(Context context, MiPushMessage miPushMessage) {
//        // 不显示在状态栏的自定义消息，根据需要进行额外工作
//        Intent intent = new Intent();
//        String extra = miPushMessage.getExtra().get(EXTRA_NAME);
//        intent.putExtra(JPushInterface.EXTRA_MSG_ID, miPushMessage.getMessageId());
//        intent.putExtra(JPushInterface.EXTRA_EXTRA, extra);
//        intent.putExtra(JPushInterface.EXTRA_NOTIFICATION_ID, miPushMessage.getNotifyId());
//        AppDebugConfig.d(AppDebugConfig.TAG_JPUSH, "action: " + intent.getAction() + ", 自定义消息，内容:" + extra);
//        if (extra == null) {
//            return;
//        }
//        PushMessageExtra msg = AssistantApp.getInstance().getGson().fromJson(extra, PushMessageExtra
//                .class);
//        PushMessageManager.getInstance().handleCustomMessage(context, msg, intent);
//    }
//
//    @Override
//    public void onReceiveMessage(Context context, final MiPushMessage miPushMessage) {
//        // 用户接收到通知
//        String extra = miPushMessage.getExtra().get(EXTRA_NAME);
//        AppDebugConfig.d(AppDebugConfig.TAG_JPUSH, "接收到通知消息,附加: " + extra);
//        if (extra == null) {
//            return;
//        }
//        final PushMessageExtra msg = AssistantApp.getInstance().getGson().fromJson(extra, PushMessageExtra
//                .class);
//        if (AppDebugConfig.IS_STATISTICS_SHOW) {
//            ThreadUtil.runInThread(new Runnable() {
//                @Override
//                public void run() {
//                    Map<String, String> kv = new HashMap<>();
//                    kv.put("消息ID", miPushMessage.getMessageId());
//                    kv.put("消息标题", msg.title);
//                    kv.put("消息内容", msg.content);
//                    kv.put("总计", String.format("%s-%s-%s", miPushMessage.getMessageId(), msg.title, msg.content));
//                    StatisticsManager.getInstance().trace(
//                            AssistantApp.getInstance().getApplicationContext(),
//                            StatisticsManager.ID.PUSH_MESSAGE_RECEIVED,
//                            StatisticsManager.ID.STR_PUSH_MESSAGE_RECEIVED,
//                            kv, 1);
//                }
//            });
//        }
//    }
//
//    @Override
//    public void onNotificationMessageClicked(Context context, final MiPushMessage miPushMessage) {
//        // 用户点击了通知，打开对应界面
//        // 不显示在状态栏的自定义消息，根据需要进行额外工作
//        String extra = miPushMessage.getExtra().get(EXTRA_NAME);
//        AppDebugConfig.d(AppDebugConfig.TAG_JPUSH, "处理打开的消息，附加:" + extra);
//        if (extra == null) {
//            return;
//        }
//        final PushMessageExtra msg = AssistantApp.getInstance().getGson().fromJson(extra, PushMessageExtra
//                .class);
//        PushMessageManager.getInstance().handleNotifyMessage(context, msg);
//        AccountManager.getInstance().obtainUnreadPushMessageCount();
//
//        if (AppDebugConfig.IS_STATISTICS_SHOW) {
//            ThreadUtil.runInThread(new Runnable() {
//                @Override
//                public void run() {
//                    Map<String, String> kv = new HashMap<>();
//                    kv.put("消息ID", miPushMessage.getMessageId());
//                    kv.put("消息标题", msg.title);
//                    kv.put("消息内容", msg.content);
//                    kv.put("总计", String.format("%s-%s-%s", miPushMessage.getMessageId(), msg.title, msg.content));
//                    StatisticsManager.getInstance().trace(
//                            AssistantApp.getInstance().getApplicationContext(),
//                            StatisticsManager.ID.PUSH_MESSAGE_OPENED,
//                            StatisticsManager.ID.STR_PUSH_MESSAGE_OPENED,
//                            kv, 1);
//                }
//            });
//        }
//    }
//}
