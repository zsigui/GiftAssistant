package com.oplay.giftcool.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.config.WebViewUrl;
import com.oplay.giftcool.config.util.TaskTypeUtil;
import com.oplay.giftcool.config.util.UserTypeUtil;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.DialogManager;
import com.oplay.giftcool.manager.OuwanSDKManager;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.manager.SocketIOManager;
import com.oplay.giftcool.manager.StatisticsManager;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.IndexPostNew;
import com.oplay.giftcool.model.data.resp.InitQQ;
import com.oplay.giftcool.model.data.resp.UserModel;
import com.oplay.giftcool.model.data.resp.task.ShareTask;
import com.oplay.giftcool.model.data.resp.task.TaskInfoOne;
import com.oplay.giftcool.model.data.resp.task.TaskInfoTwo;
import com.oplay.giftcool.receiver.AccountReceiver;
import com.oplay.giftcool.sharesdk.ShareSDKManager;
import com.oplay.giftcool.ui.activity.LoginActivity;
import com.oplay.giftcool.ui.activity.MainActivity;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.login.BindOwanFragment;

import net.ouwan.umipay.android.entry.UmipayCommonAccount;
import net.ouwan.umipay.android.manager.UmipayCommonAccountCacheManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
            case TaskTypeUtil.INFO_TWO_SHARE_SPECIFIC_GIFT:
                try {
                    IndexGiftNew o = AssistantApp.getInstance().getGson().fromJson(infoTwo.data, IndexGiftNew.class);
                    ShareSDKManager.getInstance(context).shareGift(context, fm, o, "task");
                } catch (Throwable t) {
                    AppDebugConfig.d(AppDebugConfig.TAG_UTIL, t);
                }
                break;
            case TaskTypeUtil.INFO_TWO_SHARE_SPECIFIC_ACTIVITY:
                try {
                    IndexPostNew o = AssistantApp.getInstance().getGson().fromJson(infoTwo.data, IndexPostNew.class);
                    ShareSDKManager.getInstance(context).shareActivity(context, fm, o, "task");
                } catch (Throwable t) {
                    AppDebugConfig.d(AppDebugConfig.TAG_UTIL, t);
                }
                break;
            case TaskTypeUtil.INFO_TWO_SHARE:
                try {
                    ShareTask o = AssistantApp.getInstance().getGson().fromJson(infoTwo.data, ShareTask.class);
                    ShareSDKManager.getInstance(context).share(fm, context, o.shareDialogTitle, o.title, o.desc,
                            o.desc, o.url, o.icon, null, o.contentType);
                } catch (Throwable t) {
                    AppDebugConfig.d(AppDebugConfig.TAG_UTIL, t);
                }
                break;
            default:
                ToastUtil.showShort(ConstString.TOAST_VERSION_NOT_SUPPORT);
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

    public static boolean isAppHost(String host) {
        return WebViewUrl.getBaseUrl().contains(host)
                || NetUrl.getBaseUrl().contains(host)
                || host.contains(WebViewUrl.URL_DOMAIN);
    }

    public static void handleViewUri(Context context, Uri uri) {
        if (uri == null || context == null) {
            AppDebugConfig.d(AppDebugConfig.TAG_UTIL, "uri = " + uri + ", context = " + context);
            return;
        }
        try {
            TaskInfoOne info = new TaskInfoOne();
            String pathAction = uri.getEncodedPath();
            int index = pathAction.indexOf('/', 1);
            info.action = pathAction.substring(1, index);
            if ("AutoLogin".equalsIgnoreCase(info.action)) {
                // 跳转切换账号登录形式
                String apkName = uri.getQueryParameter("apkname");
                int uid = Integer.parseInt(uri.getQueryParameter("cuid"));
                String sid = uri.getQueryParameter("sid");
                String uname = uri.getQueryParameter("uname");
                String packname = uri.getQueryParameter("packname");
                UmipayCommonAccount account = new UmipayCommonAccount(context.getPackageName(), packname, apkName, 0);
                account.setUid(uid);
                account.setSession(sid);
                account.setUserName(uname);
                UmipayCommonAccountCacheManager.getInstance(context).addCommonAccount(account,
                        UmipayCommonAccountCacheManager.COMMON_ACCOUNT_TO_CHANGE);
                OuwanSDKManager.getInstance().showChangeAccountView();
            } else {
                info.data = uri.getQueryParameter("data");
                String id = uri.getQueryParameter("id");
                info.id = (TextUtils.isEmpty(id) ? 0 : Integer.parseInt(id));
                IntentUtil.handleJumpInfo(context, info);
                OuwanSDKManager.getInstance().showChangeAccountView();
            }
        } catch (Throwable t) {
            AppDebugConfig.w(AppDebugConfig.TAG_UTIL, t);
        }
    }

    public static void doPhoneLoginSuccessNext(Context context, UserModel um) {
        um.userInfo.loginType = UserTypeUtil.TYPE_POHNE;
        doLoginSuccessNext(context, um);

        if (AppDebugConfig.IS_STATISTICS_SHOW) {
            Map<String, String> keyVal = new HashMap<>();
            // 手机号: %s, 对应绑定偶玩账号: %s, 是否首次登录: %b, 绑定情况: %d, 是否空Context: %b
            keyVal.put("登录信息", String.format(Locale.CHINA, "p:%s, u:%s, i:%b, b:%d, c:%b",
                    um.userInfo.phone, um.userInfo.username,
                    um.userInfo.isFirstLogin, um.userInfo.bindOuwanStatus, (context == null)));
            StatisticsManager.getInstance().trace(context,
                    StatisticsManager.ID.USER_PHONE_LOGIN,
                    StatisticsManager.ID.STR_USER_PHONE_LOGIN, "手机登录", keyVal, 0);
        }
    }

    public static void doLoginSuccessNext(Context context, UserModel um) {
        MainActivity.sIsTodayFirstOpen = true;
        AccountManager.getInstance().notifyUserAll(um);
        SocketIOManager.getInstance().connectOrReConnect(true);
        ScoreManager.getInstance().initTaskState();

        Global.sHasShowedSignInHint = Global.sHasShowedLotteryHint = false;

        AppDebugConfig.d(AppDebugConfig.TAG_WARN, "context = " + (context instanceof LoginActivity) + ", activity = "
                + context);
        if (context != null) {
            if (AssistantApp.getInstance().getSetupOuwanAccount() == KeyConfig.KEY_LOGIN_NOT_BIND
                    || um.userInfo.bindOuwanStatus == 1) {
                if (context instanceof LoginActivity) {
                    ((LoginActivity) context).doLoginBack();
                } else if (context instanceof BaseAppCompatActivity) {
                    ((BaseAppCompatActivity) context).finish();
                }
            } else {
                if (context instanceof LoginActivity) {
                    // 未绑定偶玩账号，需要绑定
                    ((BaseAppCompatActivity) context).replaceFragWithTitle(R.id.fl_container,
                            BindOwanFragment.newInstance(um, true),
                            context.getResources().getString(um.userInfo.phoneCanUseAsUname ?
                                    R.string.st_login_bind_owan_title_1
                                    : R.string.st_login_bind_owan_title_2), false);
                } else {
                    IntentUtil.jumpBindOwan(AssistantApp.getInstance(), um);
                }
            }
        } else {
            if (AssistantApp.getInstance().getSetupOuwanAccount() != KeyConfig.KEY_LOGIN_NOT_BIND
                    && um.userInfo.bindOuwanStatus != 1) {
                IntentUtil.jumpBindOwan(AssistantApp.getInstance(), um);
            }
        }
//        else {
//            if (AssistantApp.getInstance().getSetupOuwanAccount() != KeyConfig.KEY_LOGIN_NOT_BIND
//                    && um.userInfo.bindOuwanStatus == 0) {
//                IntentUtil.jumpBindOwan(context, um);
//            }
//        }
    }

    public static void sendSelectAccountBroadcast(Context context) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(AccountReceiver.ACTION_SELECT);
        broadcastIntent.setPackage(context.getPackageName());
        //发送广播
        context.sendBroadcast(broadcastIntent);
    }
}
