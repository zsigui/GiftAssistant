package net.ouwan.umipay.android.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.text.TextUtils;

import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayCommonAccount;
import net.ouwan.umipay.android.manager.UmipayAccountManager;
import net.ouwan.umipay.android.manager.UmipayCommonAccountCacheManager;
import net.youmi.android.libs.common.coder.Coder_Md5;
import net.youmi.android.libs.common.util.Util_System_Intent;
import net.youmi.android.libs.common.util.Util_System_Package;

/**
 * Utils_Package
 *
 * @author zacklpx
 *         date 15-1-30
 *         description
 */
public class Util_Package {

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

    public synchronized static void startUmiAppWithSession(Context context, String packageName) {
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
                //实际还要打开app
                if (Util_System_Intent.startActivityByPackageName(context, packageName)) {
                    //发送广播
                    context.sendBroadcast(broadcastIntent);
                }
            } else {
                // 未登录情况下跳转，直接打开即可
                Util_System_Intent.startActivityByPackageName(context, packageName);
            }
        } catch (Throwable e) {
            Debug_Log.e(e);
        }
    }
}
