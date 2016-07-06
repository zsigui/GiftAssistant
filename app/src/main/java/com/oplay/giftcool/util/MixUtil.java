package com.oplay.giftcool.util;

import android.app.ActivityManager;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.util.TaskTypeUtil;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.DialogManager;
import com.oplay.giftcool.model.data.resp.InitQQ;
import com.oplay.giftcool.model.data.resp.task.TaskInfoTwo;
import com.oplay.giftcool.sharesdk.ShareSDKManager;

import java.util.ArrayList;
import java.util.List;

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
            AppDebugConfig.d(AppDebugConfig.TAG_UTIL, "context和url不允许定义为空");
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
            if (("true".equalsIgnoreCase(val) || "1".equals(val))) {
                return MixUtil.needLoginFirst(context);
            }
        }
        return false;
    }

    /**
     * a异或b
     */
    public static boolean xor(boolean a, boolean b) {
        return (a && !b) || (b && !a);
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
     * 处理额外信息类型为二(执行特定代码段)的数据
     */
    public static void executeLogicCode(Context context, FragmentManager fm, TaskInfoTwo infoTwo) {
        if (context == null) {
            AppDebugConfig.w(AppDebugConfig.STACKTRACE_INDEX + 1, AppDebugConfig.TAG_UTIL,
                    "context is not allowed to be null");
            return;
        }
        if (fm == null) {
            AppDebugConfig.w(AppDebugConfig.STACKTRACE_INDEX + 1, AppDebugConfig.TAG_UTIL,
                    "fm is not allowed to be null");
            return;
        }
        switch (infoTwo.type) {
            case TaskTypeUtil.INFO_TWO_SHARE_GCOOL:
                ShareSDKManager.getInstance(context).shareGCool(context, fm);
                break;
            case TaskTypeUtil.INFO_TWO_REQUEST_GIFT:
                DialogManager.getInstance().showHopeGift(fm, 0, "", true);
                break;
            case TaskTypeUtil.INFO_TWO_SHOW_UPGRADE:
                final boolean isUpdate =
                        DialogManager.getInstance().showUpdateDialog(context, fm, true);
                if (!isUpdate) {
                    ToastUtil.showShort(ConstString.TOAST_VERSION_NEWEST);
                }
                break;
            case TaskTypeUtil.INFO_TWO_JOIN_QQ_GROUP:
                IntentUtil.joinQQGroup(context, infoTwo.data);
                break;
        }
    }

    public static int calculatePercent(int remainCount, int totalCount) {
        return (int) (Math.ceil(remainCount * 100.0 / totalCount));
    }

    /**
     * 判断进程是否为主进程，因为推送服务运行在另外的进程，如不判断会导致实例被初始化两次
     */
    public static boolean isInMainProcess(Context context) {
        ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = context.getPackageName();
        int myPid = android.os.Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }
}
