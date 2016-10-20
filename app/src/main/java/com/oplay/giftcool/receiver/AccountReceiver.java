package com.oplay.giftcool.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.manager.OuwanSDKManager;

import net.ouwan.umipay.android.Utils.Util_Package;
import net.ouwan.umipay.android.api.UmipayFloatMenu;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.manager.UmipayCommonAccountCacheManager;

/**
 * Created by zsigui on 16-9-5.
 */
public class AccountReceiver extends BroadcastReceiver {

    public static final String ACTION_SELECT = AppConfig.PACKAGE_NAME() + ".account.select";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();
            AppDebugConfig.d(AppDebugConfig.TAG_WARN, "receiver action = " + action);
            if (UmipayCommonAccountCacheManager.ACTION_ACCOUNT_CHANGE.equals(action)) {
                OuwanSDKManager.getInstance().showChangeAccountView();
            } else if (UmipayFloatMenu.ACTION_ACCOUNT_CHANGE_CALLBACK.equals(action)) {
                handleOpenAppCallback(context, intent);
            } else if (ACTION_SELECT.equalsIgnoreCase(action)) {
                OuwanSDKManager.getInstance().showSelectAccountView();
            }
        } catch (Throwable e) {
            Debug_Log.e(e);
        }
    }

    private void handleOpenAppCallback(Context context, Intent intent) {
        Util_Package.sNeedStopExecute = false;
        Intent newIntent = new Intent();
        String _packageName = intent.getExtras().getString(UmipayFloatMenu.DEST_PACKAGENAME);
        String _className = intent.getExtras().getString(UmipayFloatMenu.DEST_CLASSNAME);
        int taskid = 0;
        try {
            taskid = intent.getExtras().getInt(UmipayFloatMenu.DEST_TASKID);
        } catch (Throwable e) {
            Debug_Log.e(e);
        }
        if (taskid != 0) {
            //要求当前应用有android.permission.REORDER_TASKS权限
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            am.moveTaskToFront(taskid, 0);
        } else if (_packageName != null && _className != null) {
            //要求被打开activity的exported属性为true或者设置相同android:sharedUserId
            ComponentName componentName = new ComponentName(_packageName, _className);
            newIntent.setComponent(componentName);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            newIntent.setAction("change_account");
            context.startActivity(newIntent);
        }
    }
}
