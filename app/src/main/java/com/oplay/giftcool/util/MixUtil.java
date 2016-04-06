package com.oplay.giftcool.util;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.model.data.resp.InitQQ;
import com.oplay.giftcool.ui.activity.SplashActivity;
import com.socks.library.KLog;

import java.util.ArrayList;

/**
 * 混杂工具类，用于放置一些重复的方法
 * <p/>
 * Created by zsigui on 16-3-1.
 */
public class MixUtil {

    /**
     * 根据初始化结果配置官方QQ群信息
     */
    public static String[] getQQInfo() {
        String[] result = new String[2];
        ArrayList<InitQQ> qqInfo = AssistantApp.getInstance().getQQInfo();
        result[0] = "515318514";
        result[1] = "8MdlDK-VEslpLGRDOIlcqZUbSYuv0pNb";
        String qqStrServer = "";
        if (qqInfo != null && qqInfo.size() > 0) {
            for (InitQQ item : qqInfo) {
                qqStrServer = item.qq + ',';
            }
            if (qqStrServer.length() > 0) {
                qqStrServer = qqStrServer.substring(0, qqStrServer.length() - 1);
            }
        }
        if (!TextUtils.isEmpty(qqStrServer)) {
            result[0] = qqStrServer;
            // 选择第一个作为默认跳转加入
            assert qqInfo != null;
            result[1] = qqInfo.get(0).key;
        }
        return result;
    }

    /**
     * 判断传入的Url是否指明需要先进行登录
     */
    public static boolean isUrlNeedLoginFirst(Context context, String url) {
        if (context == null || url == null) {
            if (AppDebugConfig.IS_DEBUG) {
                KLog.d(AppDebugConfig.TAG_UTIL, "context和url不允许定义为空");
            }
            return false;
        }
        final String key = "need_validate=";
        int index = url.indexOf(key);
        if (index != -1 && index + key.length() < url.length()) {
            index += key.length();
            int last_index = url.indexOf('&', index);
            if (last_index == -1) {
                last_index = url.length();
            }
            final String val = url.substring(index, last_index);
            if(("true".equalsIgnoreCase(val) || "1".equals(val))) {
                return MixUtil.needLoginFirst(context);
            }
        }
        return false;
    }

    /**
     * 对于由登录要求的执行登录判断
     */
    public static boolean needLoginFirst(Context context) {
        if (!AccountManager.getInstance().isLogin()) {
            IntentUtil.jumpLogin(context);
            return true;
        }
        return false;
    }

    /**
     * 创建桌面快捷方式
     */
    public static void createDesktopShortcut(Context context) {
        if (isShortcutInstalled(context)) {
            return;
        }
        Intent intent = new Intent();
        intent.setAction(Manifest.permission.INSTALL_SHORTCUT);
        String title = context.getResources().getString(R.string.app_name);
        // 设置打开快捷打开应用位置，并与应用绑定
        Intent shortcutIntent = new Intent(context, SplashActivity.class);
        shortcutIntent.setAction(Intent.ACTION_MAIN);
        shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        Intent.ShortcutIconResource iconResource = Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_launcher);
        // 快捷方式名称
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, iconResource);
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        // 不允许重建
        intent.putExtra("duplicate", false);
        context.sendBroadcast(intent);
    }

    /**
     * 判断是否创建了桌面快捷方式
     */
    public static boolean isShortcutInstalled(Context context) {
        boolean isInstallShortcut = false;
        final ContentResolver cr = context.getContentResolver();
        String AUTHORITY = null;
        /*
         * 根据版本号设置Uri的AUTHORITY
         */
        if (AppInfoUtil.getAppVerCode(context) >= 19) {
            AUTHORITY = "com.android.launcher3.settings";
        } else if (AppInfoUtil.getAppVerCode(context) >= 8) {
            AUTHORITY = "com.android.launcher2.settings";
        } else {
            AUTHORITY = "com.android.launcher.settings";
        }

        Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites?notify=true");
        Cursor c = cr.query(CONTENT_URI,
                new String[]{"title", "iconResource"}, "title=?",
                new String[]{context.getString(R.string.app_name)}, null);
        if (c != null && c.getCount() > 0) {
            isInstallShortcut = true;
        }
        return isInstallShortcut;
    }
}
