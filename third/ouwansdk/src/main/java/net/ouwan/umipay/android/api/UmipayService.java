package net.ouwan.umipay.android.api;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RemoteViews;

import net.ouwan.umipay.android.Utils.Util_ImageLoader;
import net.ouwan.umipay.android.Utils.Util_Resource;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.PushInfo;
import net.ouwan.umipay.android.entry.UmipayCommonAccount;
import net.ouwan.umipay.android.interfaces.Interface_GetPush_Listener;
import net.ouwan.umipay.android.manager.ErrorReportManager;
import net.ouwan.umipay.android.manager.ListenerManager;
import net.ouwan.umipay.android.manager.ProxyPushCacheManager;
import net.ouwan.umipay.android.manager.UmipayAccountManager;
import net.ouwan.umipay.android.manager.UmipayCommandTaskManager;
import net.ouwan.umipay.android.manager.UmipayCommonAccountCacheManager;
import net.youmi.android.libs.common.util.Util_System_Package;
import net.youmi.android.libs.webjs.view.webview.Flags_Browser_Config;

import java.util.ArrayList;
import java.util.List;

/**
 * UmipayService
 * Created by liangpeixing on 5/5/15.
 */
public class UmipayService extends IntentService implements Interface_GetPush_Listener {
    public final static int PUSH_NOTIFY_ID = 4687853;

    public static final int ACTION_NULL = -1;
    public static final int ACTION_PULL = 0;
    public static final int ACTION_NOTIFY = 1;
    public static final int ACTION_RUN = 2;
    public static final int ACTION_FLOATMENU_PULL = 3;
    public static final int ACTION_JS_NOTIFICATION = 4;
    public static final int ACTION_ERROR_REPORT = 5;
    public static final int ACTION_RESTART_CHANGE_ACCOUNT = 6;

    Handler mHandler;

    public UmipayService() {
        super("UmipayService");
        mHandler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            int actionType = intent.getIntExtra("action", ACTION_NULL);
            Debug_Log.dd("service handle actionType = " + actionType);
            switch (actionType) {
                case ACTION_PULL: {
                    pullPushInfo();
                    //设置定时显示下个推送
//					showNotifycationWithDelay();
                    break;
                }
                case ACTION_NOTIFY: {
                    int pushId = intent.getIntExtra("waitingPushId", 0);
                    Debug_Log.dd("onHandleIntent receive at " + new java.util.Date().toLocaleString() + " pushId = " +
                            pushId);
                    PushInfo lastPushInfo = ProxyPushCacheManager.getInstance(this).getPushInfo(pushId);
                    if (lastPushInfo == null) {
                        return;
                    }
                    showNotify(lastPushInfo);
                    ProxyPushCacheManager.getInstance(this).consumePushInfo(lastPushInfo);
                    showNotifycationWithDelay();
                    break;
                }
                case ACTION_RUN: {
                    PushInfo lastPushInfo = (PushInfo) intent.getSerializableExtra("pushinfo");
                    actionPush(lastPushInfo);
                    break;
                }
                case ACTION_FLOATMENU_PULL: {
                    pullNotice();
                    break;
                }
                case ACTION_JS_NOTIFICATION: {
                    PushInfo pushInfo = (PushInfo) intent.getSerializableExtra("pushinfo");
                    showNotify(pushInfo);
                    break;
                }
                case ACTION_ERROR_REPORT: {
                    errorReport(intent);
                    break;
                }
                case ACTION_RESTART_CHANGE_ACCOUNT: {
                    if (UmipayAccountManager.getInstance(this).isLogin()) {
                        UmipayCommonAccount account = UmipayCommonAccountCacheManager.getInstance(this)
                                .getCommonAccountByPackageName(this.getPackageName(),
                                        UmipayCommonAccountCacheManager.COMMON_ACCOUNT_TO_CHANGE);

                        if (account != null) {
                            UmipayActivity.showChangeAccountDialog(this);
                        }
                    }
                    break;
                }
                default:
                    break;
            }
            this.stopSelf();
        } catch (Throwable e) {
            Debug_Log.e(e);
        }
    }

    private void pullPushInfo() {
        ListenerManager.setCommandGetPushListener(this);
        UmipayCommandTaskManager.getInstance(this).GetPushCommandTask();
    }

    private void pullNotice() {
        UmipayCommandTaskManager.getInstance(this).GetFloatMenuPushCommandTask();
    }

    //定时显示Notifycation
    void showNotifycationWithDelay() {
        try {
            PushInfo lastPushInfo = ProxyPushCacheManager.getInstance(this).getLastPushInfo();
            if (lastPushInfo == null) {
                Debug_Log.dd("lastPushInfo = null");
                return;
            }
            Debug_Log.dd(lastPushInfo.toString());

            if (!ProxyPushCacheManager.getInstance(this).checkPushInfo(lastPushInfo)) {
                Debug_Log.dd("Check lastPushInfo failed! id =  " + lastPushInfo.getId());
                ProxyPushCacheManager.getInstance(this).consumePushInfo(lastPushInfo);
                showNotifycationWithDelay();
                return;
            }
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent alarmIntent = new Intent(this, UmipayService.class);
            alarmIntent.putExtra("action", ACTION_NOTIFY);
            alarmIntent.putExtra("waitingPushId", lastPushInfo.getId());
            PendingIntent pIntent = PendingIntent.getService(this, lastPushInfo.getId(),
                    alarmIntent, 0);
            alarmManager.cancel(pIntent);
            alarmManager.set(AlarmManager.RTC_WAKEUP, lastPushInfo.getShowTime_ms(), pIntent);
        } catch (Throwable e) {
            Debug_Log.e(e);
        }
    }

    void showNotify(PushInfo lastPushInfo) {
        if (lastPushInfo == null) {
            return;
        }
        // 初始化NotificationManager:
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context
                .NOTIFICATION_SERVICE);
        int appIcon = Util_System_Package.getAppLaunchInfo(this, getPackageName()).getIconResourceId();

        int icon = Util_Resource.getIdByReflection(this, "drawable", "umipay_ic_notify");
        // //
        // 通知图标
        CharSequence tickerText = lastPushInfo.getTitle();
        long when = lastPushInfo.getShowTime_ms(); // 通知产生的时间，会在通知信息里显示
        Intent notificationIntent = new Intent(this, UmipayService.class);
        notificationIntent.putExtra("action", ACTION_RUN);
        notificationIntent.putExtra("pushinfo", lastPushInfo);
        PendingIntent contentIntent = PendingIntent.getService(this, lastPushInfo.getId(),
                notificationIntent, PendingIntent.FLAG_ONE_SHOT
                        | PendingIntent.FLAG_UPDATE_CURRENT
        );
        // 用上面的属性初始化Nofification
        Notification notification;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(icon);
        builder.setTicker(tickerText);
        builder.setWhen(when);
        notification = builder
                .setContentTitle(lastPushInfo.getTitle())
                .setContentText(lastPushInfo.getContent())
                .setContentIntent(contentIntent).build();

        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.sound = null;
        notification.defaults |= Notification.DEFAULT_VIBRATE;


        Bitmap bitmap = Util_ImageLoader.syncLoadBitmap(getApplicationContext(), lastPushInfo.getIconUrl());
        int iconViewId = getContentViewIconId(notification.contentView.getLayoutId());
        if (iconViewId > 0) {
            RemoteViews remoteViews = notification.contentView;
            if (bitmap != null) {
                //hdpi:96*96
                bitmap.setDensity(240);
                remoteViews.setImageViewBitmap(iconViewId, bitmap);
            } else {
                remoteViews.setImageViewResource(iconViewId, appIcon);
            }
        }
        mNotificationManager.notify(lastPushInfo.getId(), notification);
    }

    private int getContentViewIconId(int layoutId) {
        ViewGroup viewGroup = (ViewGroup) ViewGroup.inflate(getApplicationContext(), layoutId, null);
        List<ImageView> list = new ArrayList<ImageView>();
        getImageViews(viewGroup, list);
        for (ImageView imageView : list) {
            if (imageView.getId() > 0 && imageView.getVisibility() == View.VISIBLE) {
                return imageView.getId();
            }
        }
        return 0;
    }

    public void getImageViews(View parent, List<ImageView> list) {
        if (parent == null) {
            return;
        }
        if (parent instanceof ImageView) {
            list.add((ImageView) parent);
        } else if (parent instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) parent;
            int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                getImageViews(viewGroup.getChildAt(i), list);
            }
        }
    }

    private void actionPush(PushInfo pushInfo) {
        if (pushInfo == null) {
            return;
        }
        try {
            if (pushInfo.getType() == 0) {
                String html = pushInfo.getUri();
                UmipayBrowser.loadUrl(this, pushInfo.getTitle(), html, Flags_Browser_Config.FLAG_AUTO_CHANGE_TITLE
                        | Flags_Browser_Config.FLAG_USE_YOUMI_JS_INTERFACES, null, null);
            } else {
                Intent gameintent = Intent.getIntent(pushInfo.getUri());
                startActivity(gameintent);
            }

        } catch (Throwable e) {
            Debug_Log.e(e);
        }
    }

    private void errorReport(Intent intent) {
        Debug_Log.dd("UmipayService errorReport");
        if (intent == null) {
            return;
        }
        try {
            int errorCode = intent.getIntExtra("errorCode", 0);
            String errorMsg = intent.getStringExtra("errorMsg");
            long ttl = intent.getLongExtra("ttl", 0l);
            long timestamp = intent.getLongExtra("timestamp", 0l);
            //记录错误log并收集当前相关信息，考虑到需要测试网络状况等，所以放到service里面在后台执行相关操作
            ErrorReportManager.getInstance(UmipayService.this).report(mHandler, errorCode, errorMsg, ttl, timestamp);
        } catch (Throwable e) {
            Debug_Log.e(e);
        }
    }

    @Override
    public void onGetPushFinish() {
        showNotifycationWithDelay();
    }

    @Override
    public void onDestroy() {
        Debug_Log.dd("UmipayService onDestroy");
        super.onDestroy();
    }
}
