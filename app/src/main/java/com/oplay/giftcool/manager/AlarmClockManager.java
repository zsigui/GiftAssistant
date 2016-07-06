package com.oplay.giftcool.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.download.silent.SilentDownloadManager;
import com.oplay.giftcool.receiver.StartReceiver;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.SystemUtil;
import com.oplay.giftcool.util.ThreadUtil;

/**
 * Created by zsigui on 16-3-14.
 */
public class AlarmClockManager {

    // 10秒唤醒闹钟设置
    public static final int ALARM_WAKE_ELAPSED_TIME = 30 * 1000;
    private static final int ALARM_WAKE_REQUEST_CODE = 0xF01;
    private static final int NOTIFY_GIFT_UPDATE_ELAPSED_COUNT = 2;

    public static AlarmClockManager sInstance;

    public static AlarmClockManager getInstance() {
        if (sInstance == null) {
            sInstance = new AlarmClockManager();
        }
        return sInstance;
    }

    private AlarmClockManager() {
        mObserverGame = false;
    }


    private AlarmManager mManager;

    // 唤醒闹钟的意图
    private PendingIntent alarmSender = null;
    // 一次唤醒间隔时间
    private int mElapsedTime = ALARM_WAKE_ELAPSED_TIME;
    // 唤醒的次数
    private int mWakeCount = 0;
    private int mBackgroundWakeCount = 0;
    // 是否允许通知礼包更新
    private boolean mAllowNotifyGiftUpdate;

    public AlarmManager getAlarmManager(Context context) {
        if (mManager == null) {
            mManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }
        return mManager;
    }

    public void setElapsedTime(int elapsedTime) {
        mElapsedTime = elapsedTime;
    }

    /**
     * 获取唤醒允许礼包更新状态
     */
    public boolean isAllowNotifyGiftUpdate() {
        return mAllowNotifyGiftUpdate;
    }

    /**
     * 设置是否允许通知礼包更新
     */
    public void setAllowNotifyGiftUpdate(boolean allowNotifyGiftUpdate) {
        mAllowNotifyGiftUpdate = allowNotifyGiftUpdate;
    }


    private boolean mObserverGame = false;

    public boolean isObserverGame() {
        return mObserverGame;
    }

    public void setObserverGame(boolean observerGame) {
        AppDebugConfig.d(AppDebugConfig.TAG_MANAGER, "task : 设置下载标志");
        mObserverGame = observerGame;
    }

    /**
     * 启动唤醒闹钟
     */
    public void startWakeAlarm(final Context context) {

        ThreadUtil.runInThread(new Runnable() {
            @Override
            public void run() {
                try {
                    initAndSetWakeAlarm(context);

                    if (mObserverGame
                            && mBackgroundWakeCount > 0
                            && mBackgroundWakeCount < 20) {
                        // 处理观察试玩游戏
                        ScoreManager.getInstance().judgePlayTime(context, mElapsedTime / 1000);
                    }

                    mWakeCount++;
                    if (mAllowNotifyGiftUpdate && mBackgroundWakeCount == 0
                            && mWakeCount % NOTIFY_GIFT_UPDATE_ELAPSED_COUNT == 0
                            && NetworkUtil.isWifiConnected(context)) {
                        // 允许的情况下，且程序位于前台,每唤醒2次通知一次更新
                        ObserverManager.getInstance().notifyGiftUpdate(ObserverManager.STATUS.GIFT_UPDATE_PART);
                        SilentDownloadManager.getInstance().startDownload();
                    }
                } catch (Throwable t) {
                    AppDebugConfig.w(AppDebugConfig.TAG_MANAGER, t);
                }
            }
        });

    }

    private void resetWakeElapsed() {
        if (!SystemUtil.isMyAppInForeground()) {
            // 判断应用是否处于后台
            AppDebugConfig.d(AppDebugConfig.TAG_MANAGER,
                    "Wake Alarm is running when app in background! elapsed time = " + mElapsedTime
                            + ", background count = " + mBackgroundWakeCount);
            mBackgroundWakeCount++;
            if (mBackgroundWakeCount > 5) {
                // 20分钟
                mElapsedTime = 40 * ALARM_WAKE_ELAPSED_TIME;
            } else if (mBackgroundWakeCount > 3) {
                // 1分钟
                mElapsedTime = 2 * ALARM_WAKE_ELAPSED_TIME;
            }
        } else {
            mBackgroundWakeCount = 0;
            mElapsedTime = ALARM_WAKE_ELAPSED_TIME;
        }
    }


    public void initAndSetWakeAlarm(final Context context) {
        resetWakeElapsed();
        if (alarmSender == null) {
            Intent startIntent = new Intent(context, StartReceiver.class);
            startIntent.setAction(Action.ALARM_WAKE);
            try {
                AppDebugConfig.d(AppDebugConfig.TAG_MANAGER, "set alarm");
                alarmSender = PendingIntent.getBroadcast(context, ALARM_WAKE_REQUEST_CODE,
                        startIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            } catch (Exception e) {
                AppDebugConfig.w(AppDebugConfig.TAG_MANAGER, "unable to start broadcast");
            }
        }
        AlarmManager am = getAlarmManager(context);
        am.cancel(alarmSender);
        am.set(AlarmManager.RTC, System.currentTimeMillis() + mElapsedTime, alarmSender);
        AppDebugConfig.d(AppDebugConfig.TAG_MANAGER, "initAndSetWakeAlarm is exec success," +
                "new elpasedTime = " + mElapsedTime + ", alarm_wake = " + Action.ALARM_WAKE);
    }

    public interface Action {
        String ALARM_WAKE = AppConfig.PACKAGE_NAME + ".clock_action.ALARM_WAKE";
//		String ALARM_PLAY_GAME = AppConfig.PACKAGE_NAME + ".clock_action.PLAY_GAME";
    }

    public interface Category {
        String GCOOL_DEFAULT = AppConfig.PACKAGE_NAME;
    }
}
