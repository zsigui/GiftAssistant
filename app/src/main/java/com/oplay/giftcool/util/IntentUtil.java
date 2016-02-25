package com.oplay.giftcool.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.ui.activity.GameDetailActivity;
import com.oplay.giftcool.ui.activity.GameListActivity;
import com.oplay.giftcool.ui.activity.GiftDetailActivity;
import com.oplay.giftcool.ui.activity.GiftListActivity;
import com.oplay.giftcool.ui.activity.LoginActivity;
import com.oplay.giftcool.ui.activity.MainActivity;
import com.oplay.giftcool.ui.activity.SearchActivity;
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
	 * @param id 礼包id
	 */
	public static void jumpGiftDetail(Context context, int id) {
		Intent intent = new Intent(context, GiftDetailActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

//	private static ConfirmDialog mConfirmDialog;
//
//	private static void loginConfirm(final Context context, final int type) {
//		if (!(context instanceof FragmentActivity)) {
//			return;
//		}
//		if (mConfirmDialog != null) {
//			mConfirmDialog.dismissAllowingStateLoss();
//			mConfirmDialog = null;
//		}
//		mConfirmDialog = ConfirmDialog.newInstance();
//		mConfirmDialog.setListener(new BaseFragment_Dialog.OnDialogClickListener() {
//			@Override
//			public void onCancel() {
//				mConfirmDialog.dismissAllowingStateLoss();
//				mConfirmDialog = null;
//			}
//
//			@Override
//			public void onConfirm() {
//				Intent intent = new Intent(context, LoginActivity.class);
//				intent.putExtra(KeyConfig.KEY_TYPE, type);
//				context.startActivity(intent);
//				mConfirmDialog.dismissAllowingStateLoss();
//				mConfirmDialog = null;
//			}
//		});
//		FragmentManager fm = ((FragmentActivity)context).getSupportFragmentManager();
//		Context appContext = AssistantApp.getInstance().getApplicationContext();
//		mConfirmDialog.setTitle(appContext.getResources().getString(R.string.st_hint_dialog_login_title));
//		mConfirmDialog.setContent(appContext.getResources().getString(R.string.st_hint_dialog_login_content));
//		mConfirmDialog.show(fm, appContext.getResources().getString(R.string.st_hint_dialog_login_tag));
//	}

	/**
	 * 跳转登录界面（根据最后一次登录判断）
	 */
	public static void jumpLogin(Context context) {
		ToastUtil.showShort("需要先登录~");
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

	/**
	 * 跳转礼包首页
	 */
	public static void jumpHome(Context context) {
		Intent intent = new Intent(context, MainActivity.class);
		context.startActivity(intent);
	}

	/**
	 * 跳转活动页面
	 */
	public static void jumpActivityWeb(Context context, String url, String title) {
		Intent intent = new Intent(context, WebActivity.class);
		intent.putExtra(KeyConfig.KEY_URL, url);
		intent.putExtra(KeyConfig.KEY_DATA, title);
		context.startActivity(intent);
	}

	/**
	 * 添加Q群信息
	 */
	public static boolean joinQQGroup(Context context, String qqKey) {
		Intent intent = new Intent();
		intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq" +
				".com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + qqKey));
		// 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面
//		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			context.startActivity(intent);
			return true;
		} catch (Exception e) {
			// 未安装手Q或安装的版本不支持
			return false;
		}
	}
}
