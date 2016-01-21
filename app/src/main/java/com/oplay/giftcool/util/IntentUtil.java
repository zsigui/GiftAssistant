package com.oplay.giftcool.util;

import android.content.Context;
import android.content.Intent;

import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.ui.activity.GameDetailActivity;
import com.oplay.giftcool.ui.activity.GameListActivity;
import com.oplay.giftcool.ui.activity.GiftDetailActivity;
import com.oplay.giftcool.ui.activity.GiftListActivity;
import com.oplay.giftcool.ui.activity.LoginActivity;
import com.oplay.giftcool.ui.activity.SearchActivity;
import com.oplay.giftcool.ui.activity.SettingActivity;

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
	 * @param id 礼包id
	 */
	public static void jumpGiftDetail(Context context, int id) {
		Intent intent = new Intent(context, GiftDetailActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_GIFT_DETAIL);
		intent.putExtra(KeyConfig.KEY_DATA, id);
		context.startActivity(intent);
	}

	/**
	 * 跳转猜你喜欢列表界面
	 */
	public static void jumpGiftHotList(Context context, String gameKey) {
		Intent intent = new Intent(context, GiftListActivity.class);
		intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_GIFT_LIKE);
		intent.putExtra(KeyConfig.KEY_DATA, gameKey);
		context.startActivity(intent);
	}

	/**
	 * 跳转限量礼包列表界面
	 */
	public static void jumpGiftLimitList(Context context) {
		Intent intent = new Intent(context, GiftListActivity.class);
		intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_GIFT_LIMIT);
		context.startActivity(intent);
	}

	/**
	 * 跳转新鲜礼包列表界面
	 */
	public static void jumpGiftNewList(Context context) {
		Intent intent = new Intent(context, GiftListActivity.class);
		intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_GIFT_NEW);
		context.startActivity(intent);
	}

	/**
	 * 跳转游戏详情页面(游戏暂时用“游戏专区”固定)
	 *
	 * @param context 上下文
	 * @param id 游戏id
	 * @param status 跳转详情位置：1详情 2礼包
	 */
	public static void jumpGameDetail(Context context, int id, int status) {
		Intent intent = new Intent(context, GameDetailActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_GAME_DETAIL);
		intent.putExtra(KeyConfig.KEY_DATA, id);
		intent.putExtra(KeyConfig.KEY_STATUS, status);
		context.startActivity(intent);
	}

	/**
	 * 跳转标签游戏列表界面
	 *
	 * @param context 上下文
	 * @param type 显示列表类型
	 * @param title 标题名
	 */
	public static void jumpGameTagList(Context context, int type, String title) {
		Intent intent = new Intent(context, GameListActivity.class);
		intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_GAME_TYPE);
		intent.putExtra(KeyConfig.KEY_DATA, type);
		intent.putExtra(KeyConfig.KEY_NAME, title);
		context.startActivity(intent);
	}

	/**
	 * 跳转新游推荐列表界面
	 *
	 * @param context
	 */
	public static void jumpGameNewList(Context context) {
		Intent intent = new Intent(context, GameListActivity.class);
		intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_GAME_NEW);
		context.startActivity(intent);
	}

	/**
	 * 跳转热门游戏列表界面
	 *
	 * @param context
	 */
	public static void jumpGameHotList(Context context) {
		Intent intent = new Intent(context, GameListActivity.class);
		intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_GAME_HOT);
		context.startActivity(intent);
	}

	/**
	 * 跳转积分任务界面
	 */
	public static void jumpEarnScore(Context context) {
		Intent intent = new Intent(context, SettingActivity.class);
		intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_SCORE_TASK);
		context.startActivity(intent);
	}

	/**
	 * 跳转搜索游戏和礼包界面
	 */
	public static void jumpSearch(Context context) {
		Intent intent = new Intent(context, SearchActivity.class);
		context.startActivity(intent);
	}

	/**
	 * 跳转我的礼包界面
	 */
	public static void jumpMyGift(Context context) {
		Intent intent = new Intent(context, SettingActivity.class);
		intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_MY_GIFT_CODE);
		context.startActivity(intent);
	}

	/**
	 * 跳转设置界面
	 */
	public static void jumpSetting(Context context) {
		Intent intent = new Intent(context, SettingActivity.class);
		intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_SETTING);
		context.startActivity(intent);
	}

	/**
	 * 跳转我的钱包界面
	 */
	public static void jumpMyWallet(Context context) {
		Intent intent = new Intent(context, SettingActivity.class);
		intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_WALLET);
		context.startActivity(intent);
	}

	/**
	 * 跳转下载管理界面
	 */
	public static void jumpDownloadManager(Context context) {
		context.startActivity(getJumpDownloadManagerIntent(context));
	}

	public static Intent getJumpDownloadManagerIntent(Context context) {
		Intent intent = new Intent(context, SettingActivity.class);
		intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_DOWNLOAD);
		return intent;
	}


	/**
	 * 跳转登录界面
	 */
	public static void jumpLogin(Context context) {
		Intent intent = new Intent(context, LoginActivity.class);
		context.startActivity(intent);
	}

	/**
	 * 跳转反馈界面
	 */
	public static void jumpFeedBack(Context context) {
		Intent intent = new Intent(context, SettingActivity.class);
		intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_FEEDBACK);
		context.startActivity(intent);
	}

	/**
	 * 跳转用户信息界面
	 */
	public static void jumpUserInfo(Context context) {
		Intent intent = new Intent(context, SettingActivity.class);
		intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_USERINFO);
		context.startActivity(intent);
	}

	/**
	 * 跳转设置用户昵称界面
	 */
	public static void jumpUserSetNick(Context context) {
		Intent intent = new Intent(context, SettingActivity.class);
		intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_USER_SET_NICK);
		context.startActivity(intent);
	}

	/**
	 * 跳转设置用户头像界面
	 */
	public static void jumpUserSetAvatar(Context context) {
		Intent intent = new Intent(context, SettingActivity.class);
		intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_USER_SET_AVATAR);
		context.startActivity(intent);
	}
}
