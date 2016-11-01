package com.oplay.giftcool.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.WebViewUrl;
import com.oplay.giftcool.config.util.GameTypeUtil;
import com.oplay.giftcool.config.util.TaskTypeUtil;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.OuwanSDKManager;
import com.oplay.giftcool.model.data.resp.UserModel;
import com.oplay.giftcool.model.data.resp.task.TaskInfoOne;
import com.oplay.giftcool.ui.activity.GameDetailActivity;
import com.oplay.giftcool.ui.activity.GameListActivity;
import com.oplay.giftcool.ui.activity.GiftDetailActivity;
import com.oplay.giftcool.ui.activity.GiftListActivity;
import com.oplay.giftcool.ui.activity.LoginActivity;
import com.oplay.giftcool.ui.activity.MainActivity;
import com.oplay.giftcool.ui.activity.MessageActivity;
import com.oplay.giftcool.ui.activity.PostDetailActivity;
import com.oplay.giftcool.ui.activity.PostListActivity;
import com.oplay.giftcool.ui.activity.SearchActivity;
import com.oplay.giftcool.ui.activity.ServerInfoActivity;
import com.oplay.giftcool.ui.activity.SettingActivity;
import com.oplay.giftcool.ui.activity.WebActivity;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2016/1/3
 */
public class IntentUtil {


    /**
     * 跳转wifi设置界面
     */
    public static void jumpWifiSetting(Context context) {
        Intent intent = new Intent();
        intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
        intent.putExtra("extra_prefs_show_button_bar", true);
        intent.putExtra("extra_prefs_set_next_text", "完成");
        intent.putExtra("extra_prefs_set_back_text", "返回");
        intent.putExtra("wifi_enable_next_on_connect", true);
        context.startActivity(intent);
    }


    /**
     * 跳转礼包详情页面
     *
     * @param context 上下文
     * @param id      礼包id
     */
    public static void jumpGiftDetail(Context context, int id) {
        Intent intent = new Intent(context, GiftDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_GIFT_DETAIL);
        intent.putExtra(KeyConfig.KEY_DATA, id);
        context.startActivity(intent);
    }

    /**
     * 跳转猜你喜欢列表界面
     */
    public static void jumpGiftHotList(Context context) {
        Intent intent = new Intent(context, GiftListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_GIFT_LIKE);
        context.startActivity(intent);
    }

    /**
     * 跳转限量礼包列表界面
     */
    public static void jumpGiftLimitList(Context context) {
        Intent intent = new Intent(context, GiftListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_GIFT_LIMIT);
        context.startActivity(intent);
    }

    /**
     * 跳转新鲜礼包列表界面
     */
    public static void jumpGiftNewList(Context context) {
        Intent intent = new Intent(context, GiftListActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_GIFT_NEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }


    /**
     * 跳转限量免费礼包列表界面
     */
    public static void jumpGiftFreeList(Context context) {
        Intent intent = new Intent(context, GiftListActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_GIFT_FREE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }


    /**
     * 默认跳转游戏详情页面的详情部分
     *
     * @param context 上下文
     * @param id      游戏id
     */
    public static void jumpGameDetail(Context context, int id) {
        jumpGameDetail(context, id, GameTypeUtil.JUMP_STATUS_DETAIL);
    }

    /**
     * 跳转游戏详情页面(游戏暂时用“游戏专区”固定)
     *
     * @param context 上下文
     * @param id      游戏id
     * @param status  跳转详情位置：1详情 2礼包
     */
    public static void jumpGameDetail(Context context, int id, int status) {
        Intent intent = new Intent(context, GameDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(KeyConfig.KEY_DATA, id);
        intent.putExtra(KeyConfig.KEY_STATUS, status);
        context.startActivity(intent);
    }

    /**
     * 跳转标签游戏列表界面
     *
     * @param context 上下文
     * @param type    显示列表类型
     * @param title   标题名
     */
    public static void jumpGameTagList(Context context, int type, String title) {
        Intent intent = new Intent(context, GameListActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_GAME_TYPE);
        intent.putExtra(KeyConfig.KEY_DATA, type);
        intent.putExtra(KeyConfig.KEY_NAME, title);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 跳转新游推荐列表界面
     *
     * @param context
     */
    public static void jumpGameNewList(Context context) {
        Intent intent = new Intent(context, GameListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_GAME_NEW);
        context.startActivity(intent);
    }

    /**
     * 跳转热门游戏列表界面
     */
    public static void jumpGameHotList(Context context) {
        Intent intent = new Intent(context, GameListActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_GAME_HOT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 跳转金币任务界面
     */
    public static void jumpEarnScore(Context context) {
        if (MixUtil.needLoginFirst(context)) {
            return;
        }
        Intent intent = new Intent(context, SettingActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 跳转搜索游戏和礼包界面
     */
    public static void jumpSearch(Context context) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 跳转我的礼包界面
     */
    public static void jumpMyGift(Context context) {
        if (MixUtil.needLoginFirst(context)) {
            return;
        }
        Intent intent = new Intent(context, SettingActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_MY_GIFT_CODE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 跳转设置界面
     */
    public static void jumpSetting(Context context) {
        Intent intent = new Intent(context, SettingActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_SETTING);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 跳转我的钱包界面
     */
    public static void jumpMyWallet(Context context) {
        if (MixUtil.needLoginFirst(context)) {
            return;
        }
        Intent intent = new Intent(context, SettingActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_WALLET);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 跳转我的首充券界面
     */
    public static void jumpMyCoupon(Context context) {
        if (MixUtil.needLoginFirst(context)) {
            return;
        }
        Intent intent = new Intent(context, SettingActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_MY_COUPON);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 跳转我的关注界面
     */
    public static void jumpMyAttention(Context context) {
        if (MixUtil.needLoginFirst(context)) {
            return;
        }
        Intent intent = new Intent(context, SettingActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_MY_ATTENTION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 跳转下载管理界面
     */
    public static void jumpDownloadManager(Context context) {
        Intent intent = new Intent(context, SettingActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_DOWNLOAD);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 跳转消息中心
     */
    public static void jumpMessageCentral(Context context) {
        if (MixUtil.needLoginFirst(context)) {
            return;
        }
        Intent intent = new Intent(context, MessageActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_MSG);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 跳转新礼包通知消息列表
     */
    public static void jumpNewGiftNotify(Context context) {
        if (MixUtil.needLoginFirst(context)) {
            return;
        }
        Intent intent = new Intent(context, MessageActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_MSG_NEW_GIFT_NOTIFY);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 跳转收到的赞消息列表
     */
    public static void jumpAdmireMessage(Context context) {
        if (MixUtil.needLoginFirst(context)) {
            return;
        }
        Intent intent = new Intent(context, MessageActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_MSG_ADMIRE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 跳转系统消息列表
     */
    public static void jumpSystemMessage(Context context) {
        if (MixUtil.needLoginFirst(context)) {
            return;
        }
        Intent intent = new Intent(context, MessageActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_MSG_SYSTEM);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 跳转收到的回复消息列表
     */
    public static void jumpCommentMessage(Context context) {
        if (MixUtil.needLoginFirst(context)) {
            return;
        }
        Intent intent = new Intent(context, MessageActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_MSG_COMMENT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 跳转登录界面（根据最后一次登录判断）
     */
    public static void jumpLogin(Context context) {
        ToastUtil.showShort(ConstString.TOAST_LOGIN_FIRST);
        jumpLoginNoToast(context);
    }

    /**
     * 跳转登录界面（根据最后一次登录判断）
     */
    public static void jumpLoginNoToast(Context context) {
        if (AccountManager.getInstance().isPhoneLogin()) {
            jumpLogin(context, KeyConfig.TYPE_ID_PHONE_LOGIN);
        } else {
            jumpLogin(context, KeyConfig.TYPE_ID_OUWAN_LOGIN);
        }
    }

    /**
     * 跳转登录界面
     */
    public static void jumpLogin(Context context, int type) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, type);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 跳转绑定账号界面
     */
    public static void jumpBindOwan(Context context, UserModel um) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_BIND_OUWAN);
        intent.putExtra(KeyConfig.KEY_DATA, um);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 跳转反馈界面
     */
    public static void jumpFeedBack(Context context) {
        if (MixUtil.needLoginFirst(context)) {
            return;
        }
        Intent intent = new Intent(context, SettingActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_FEEDBACK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 跳转用户信息界面
     */
    public static void jumpUserInfo(Context context) {
        if (MixUtil.needLoginFirst(context)) {
            return;
        }
        Intent intent = new Intent(context, SettingActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_USERINFO);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 跳转设置用户昵称界面
     */
    public static void jumpUserSetNick(Context context) {
        if (MixUtil.needLoginFirst(context)) {
            return;
        }
        Intent intent = new Intent(context, SettingActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_USER_SET_NICK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 跳转设置用户头像界面
     */
    public static void jumpUserSetAvatar(Context context) {
        if (MixUtil.needLoginFirst(context)) {
            return;
        }
        Intent intent = new Intent(context, SettingActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_USER_SET_AVATAR);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 跳转礼包首页
     */
    public static void jumpHome(Context context, boolean isNewTask) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
//		Util_System_Intent.startActivityByPackageName(context, AppConfig.PACKAGE_NAME);
    }

    public static void jumpHome(Context context, int type, int data) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(AppConfig.PACKAGE_NAME() + ".action.Main");
        intent.putExtra(KeyConfig.KEY_TYPE, type);
        intent.putExtra(KeyConfig.KEY_DATA, String.valueOf(data));
        context.startActivity(intent);
    }

    /**
     * 跳转抽奖活动页面
     */
    public static void jumpLottery(Context context) {
        IntentUtil.jumpActivityWeb(context, WebViewUrl.getWebUrl(WebViewUrl.LOTTERY),
                context.getString(R.string.st_post_lottery));
    }

    /**
     * 跳转每日签到页面
     */
    public static void jumpSignIn(Context context) {
        IntentUtil.jumpActivityWeb(context, WebViewUrl.getWebUrl(WebViewUrl.SIGN_IN),
                context.getString(R.string.st_post_sign_in));
    }

    /**
     * 跳转活动页面
     */
    public static void jumpActivityWeb(Context context, String url, String title) {
        if (MixUtil.isUrlNeedLoginFirst(context, url)) {
            return;
        }
        Intent intent = new Intent(context, WebActivity.class);
        intent.putExtra(KeyConfig.KEY_DATA, url);
        intent.putExtra(KeyConfig.KEY_TITLE, title);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 添加Q群信息
     */
    public static boolean joinQQGroup(Context context, String qqKey) {
        String qqPackageName = "com.tencent.mobileqq";
        if (!InstallAppUtil.isAppInstalled(AssistantApp.getInstance().getApplicationContext(), qqPackageName)) {
            ToastUtil.showShort(ConstString.TOAST_OPEN_QQ_GROUP_FAIL);
            return false;
        }
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq" +
                ".com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + qqKey));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }

    /**
     * 跳转活动详情页面
     */
    public static void jumpPostDetail(Context context, int postId) {
        Intent intent = new Intent(context, PostDetailActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_POST_REPLY_DETAIL);
        intent.putExtra(KeyConfig.KEY_DATA, postId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 跳转活动详情页面，并指定来源
     */
    public static void jumpPostDetail(Context context, int postId, String from) {
        Intent intent = new Intent(context, PostDetailActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_POST_REPLY_DETAIL);
        intent.putExtra(KeyConfig.KEY_DATA, postId);
        intent.putExtra(KeyConfig.KEY_DATA_T, from);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 跳转活动评论详情页面
     */
    public static void jumpPostReplyDetail(Context context, int postId, int commentId) {
        Intent intent = new Intent(context, PostDetailActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_POST_COMMENT_DETAIL);
        intent.putExtra(KeyConfig.KEY_DATA, postId);
        intent.putExtra(KeyConfig.KEY_DATA_O, commentId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 跳转官方活动列表页面
     */
    public static void jumpPostOfficialList(Context context) {
        Intent intent = new Intent(context, PostListActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_POST_OFFICIAL);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 隐式跳转到指定action处
     */
    public static void jumpImplicit(Context context, String action, int type, String data) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(KeyConfig.KEY_DATA, data);
        intent.putExtra(KeyConfig.KEY_TYPE, type);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 处理额外信息类型为一(进行页面跳转)的数据
     */
    public static void handleJumpInfo(Context context, TaskInfoOne taskInfo) {
        AppDebugConfig.d(AppDebugConfig.TAG_WARN, "id = " + taskInfo.id + ", action = " + taskInfo.action + ", equal = " + ("GiftDetail".equalsIgnoreCase(taskInfo.action)));
        final String ACTION_PREFIX = AppConfig.PACKAGE_NAME() + ".action.";
        if ("GameDetail".equalsIgnoreCase(taskInfo.action)) {
            IntentUtil.jumpGameDetail(context, taskInfo.id, Integer.parseInt(taskInfo.data));
        } else if ("GiftDetail".equalsIgnoreCase(taskInfo.action)) {
            IntentUtil.jumpGiftDetail(context, taskInfo.id);
        } else if ("PostDetail".equalsIgnoreCase(taskInfo.action)) {
            if (TextUtils.isEmpty(taskInfo.data)) {
                IntentUtil.jumpPostDetail(context, taskInfo.id);
            } else {
                IntentUtil.jumpPostReplyDetail(context, taskInfo.id, Integer.parseInt(taskInfo.data));
            }
        } else if ("Sdk".equalsIgnoreCase(taskInfo.action)) {
            switch (taskInfo.id) {
                case TaskTypeUtil.INFO_ONE_SDK_RECHARGE:
                    OuwanSDKManager.getInstance().recharge();
                    break;
                case TaskTypeUtil.INFO_ONE_SDK_BIND_OUWAN:
                    OuwanSDKManager.getInstance().showBindOuwanView(context);
                    break;
                case TaskTypeUtil.INFO_ONE_SDK_BIND_PHONE:
                    OuwanSDKManager.getInstance().showBindPhoneView(context);
                    break;
            }
        } else {
            IntentUtil.jumpImplicit(context, ACTION_PREFIX + taskInfo.action.toUpperCase(), taskInfo.id, taskInfo.data);
            if ("Main".equalsIgnoreCase(taskInfo.action)) {
                if (context != null && context instanceof Activity) {
                    ((Activity) context).finish();
                }
            }
        }
    }

    public static void startBrowser(Context context, String url) {
        if (context == null) {
            return;
        }
        Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(in);
    }

    /**
     * 跳转开测开服列表界面
     */
    public static void jumpServerInfo(Context context) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, ServerInfoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 唤醒url指定应用
     */
    public static void jumpUri(Context context, String url) {
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_APP_BROWSER);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }

    /**
     * 跳转选择登录账号界面
     */
    public static void jumpSelectAccount(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_SELECT_ACCOUNT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }
}
