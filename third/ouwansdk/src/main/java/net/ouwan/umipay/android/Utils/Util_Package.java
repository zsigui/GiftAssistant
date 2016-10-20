package net.ouwan.umipay.android.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.text.TextUtils;

import net.ouwan.umipay.android.api.UmipayFloatMenu;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayCommonAccount;
import net.ouwan.umipay.android.manager.UmipayAccountManager;
import net.ouwan.umipay.android.manager.UmipayCommonAccountCacheManager;
import net.youmi.android.libs.common.coder.Coder_Md5;
import net.youmi.android.libs.common.util.Util_System_Intent;
import net.youmi.android.libs.common.util.Util_System_Package;
import net.youmi.android.libs.common.util.Util_System_Runtime;

import static net.ouwan.umipay.android.api.UmipayFloatMenu.DEST_ACTIVITY_REORDER_TO_FRONT;

/**
 * Utils_Package
 *
 * @author zacklpx
 *         date 15-1-30
 *         description
 */
public class Util_Package {


    public static boolean sNeedStopExecute;

    public static String getPackageSignature(Context context) {
        try {
            Signature[] sign = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    PackageManager.GET_SIGNATURES).signatures;
            return Coder_Md5.md5(sign[0].toByteArray());
        } catch (Throwable e) {
            Debug_Log.e(e);
            return null;
        }
    }

    public synchronized static void startUmiAppWithSession(final Context context, final String packageName,
                                                           final long timeout) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            return;
        }
        try {
            if (UmipayAccountManager.getInstance(context).isLogin()) {
                long ts = System.currentTimeMillis();
                UmipayCommonAccount account = new UmipayCommonAccount(packageName, context.getPackageName(),
                        Util_System_Package.getAppNameforCurrentContext(context), ts);
                account.setUid(UmipayAccountManager.getInstance(context).getCurrentAccount().getUid());
                account.setSession(UmipayAccountManager.getInstance(context).getCurrentAccount().getSession());
                account.setUserName(UmipayAccountManager.getInstance(context).getCurrentAccount().getUserName());
                UmipayCommonAccountCacheManager.getInstance(context).addCommonAccount(account,
                        UmipayCommonAccountCacheManager.COMMON_ACCOUNT_TO_CHANGE);
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(UmipayCommonAccountCacheManager.ACTION_ACCOUNT_CHANGE);
                broadcastIntent.setPackage(packageName);
                broadcastIntent.putExtra(UmipayFloatMenu.SRC_PACKAGENAME, context.getApplicationInfo().packageName);
                broadcastIntent.putExtra(UmipayFloatMenu.ACTION_ACCOUNT_CHANGE_CALLBACK, UmipayFloatMenu
                        .ACTION_ACCOUNT_CHANGE_CALLBACK);
                //发送广播
                broadcastIntent.putExtra(DEST_ACTIVITY_REORDER_TO_FRONT, true);
                context.sendBroadcast(broadcastIntent);
                sNeedStopExecute = true;
                Util_System_Runtime.getInstance().runInUiThreadDelayed_ms(new Runnable() {
                    public void run() {
                        //execute the task
                        if (sNeedStopExecute) {
                            if (!TextUtils.isEmpty(packageName)) {
                                Util_System_Intent.startActivityByPackageName(context, packageName);
                            }
                        }
                    }
                }, timeout);
            } else {
                // 未登录情况下跳转，直接打开即可
                Util_System_Intent.startActivityByPackageName(context, packageName);
            }
        } catch (Throwable e) {
            Debug_Log.e(e);
        }
    }
}
