package com.oplay.giftcool.manager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.StrictMode;
import android.text.TextUtils;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.socks.library.KLog;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;

import java.util.Map;

/**
 * 统计工具管理器
 * <p/>
 * Created by zsigui on 16-3-11.
 */
public class StatisticsManager {


	/*
	 说明: ID组成 2字母 + 6位数字 (2字母代表类型缩写)
	 gm = game 游戏事件  ;  gf = gift 礼包事件  ; us = 用户事件  ; ap = 应用事件
	 */
	public interface ID {
		// 礼包首页推荐图
		String GIFT_BANNER = "gf000001";
		// 抢礼包-偶玩豆支付
		String GIFT_BEAN_SEIZE = "gf000002";
		// 抢礼包-金币支付
		String GIFT_SCORE_SEIZE = "gf000003";
		// 首页0元礼包项点击事件
		String GIFT_ZERO_ITEM = "gf000004";
		// 首页限量礼包项点击事件
		String GIFT_LIMIT_ITEM = "gf000005";
		// 抢礼包点击事件
		String GIFT_SEIZE_CLICK = "gf000006";
		// 游戏首页推荐图
		String GAME_BANNER = "gm000001";
		// 搜索
		String USER_SEARCH = "us000001";
		// 求礼包-提交
		String USER_HOPE_GIFT_SUCCESS = "us000002";
		// 求礼包-取消
		String USER_HOPE_GIFT_QUICK = "us000003";
		// 手机登录
		String USER_PHONE_LOGIN = "us000004";
		// 偶玩账号登录
		String USER_OUWAN_LOGIN = "us000005";
		// 登录状态保存方式登录
		String USER_LOGIN_WITH_SESSION = "us000006";
		// 消息中心消息被点击
		String USER_MESSAGE_CENTER_CLICK = "us000007";
		// 更新弹窗
		String APP_UPDATE = "ap000001";
		// 活动弹窗
		String APP_ACTIVITY = "ap000002";
		// 推送消息已接收
		String APP_PUSH_RECEIVED = "ap000003";
		// 推送消息已打开
		String APP_PUSH_OPENED = "ap000004";
		// 签到弹窗
		String APP_SIGN_IN = "ap000005";

	}

	private final String TC_APP_KEY = "0CC59F66C9823F0D3EF90AC61D9735FB";
	private final String UMENG_APP_KEY = "56cbc68067e58e32bb00231a";

	private static StatisticsManager sInstance;
	private boolean mIsInit = false;

	public static StatisticsManager getInstance() {
		if (sInstance == null) {
			sInstance = new StatisticsManager();
		}
		return sInstance;
	}

	private StatisticsManager() {
	}

	/**
	 * 进行统计工具初始化
	 *
	 * @param context   上下文
	 * @param channelId 渠道Id
	 */
	public void init(Context context, int channelId) {
		if (AppDebugConfig.IS_STATISTICS_SHOW) {
			if (mIsInit) {
				return;
			}
			// TalkingData
//			TCAgent.init(context, TC_APP_KEY,  String.valueOf(channelId));

			// 友盟
			AnalyticsConfig.setAppkey(context, UMENG_APP_KEY);
			AnalyticsConfig.setChannel("m" + channelId);   //友盟渠道号不能纯数字
			AnalyticsConfig.enableEncrypt(true);
			boolean debugMode = false;
			if (AppConfig.TEST_MODE) {
				debugMode = true;
			} else if (AssistantApp.getInstance().getChannelId() == AppDebugConfig.TEST_CHANNEL_ID) {
				debugMode = true;
			}
			MobclickAgent.setDebugMode(debugMode);
			MobclickAgent.openActivityDurationTrack(false);     //禁止默认的页面统计


			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_STATICS, "umeng statistics is initialed");
				if (AppConfig.TEST_MODE) {
					if (AppDebugConfig.IS_DEBUG) {
						KLog.d(AppDebugConfig.TAG_STATICS, getDeviceInfo(context));
					}
				}
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().build());
			}
			mIsInit = true;
		}
	}

	public void onResume(Activity context) {
		if (AppDebugConfig.IS_STATISTICS_SHOW) {
			if (mIsInit) {
//			TCAgent.onResume(context);
				MobclickAgent.onResume(context);
			}
		}
	}

	public void onPause(Activity context) {
		if (AppDebugConfig.IS_STATISTICS_SHOW) {
			if (mIsInit) {
//			TCAgent.onPause(context);
				MobclickAgent.onPause(context);
			}
		}
	}

	public void onPageStart(Context context, String name) {
		if (AppDebugConfig.IS_STATISTICS_SHOW) {
			if (mIsInit) {
//			TCAgent.onPageStart(context, code);
				MobclickAgent.onPageStart(name);
			}
		}
	}

	public void onPageEnd(Context context, String name) {
		if (AppDebugConfig.IS_STATISTICS_SHOW) {
			if (mIsInit) {
//			TCAgent.onPageEnd(context, code);
				MobclickAgent.onPageEnd(name);
			}
		}
	}

	public void trace(final Context context, final String id) {
		if (AppDebugConfig.IS_STATISTICS_SHOW) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_STATICS, "id = " + id + ", isInit = " + mIsInit);
			}
			if (mIsInit) {
				Global.THREAD_POOL.execute(new Runnable() {
					@Override
					public void run() {

//			TCAgent.onEvent(context, id);
						MobclickAgent.onEvent(context, id);
					}
				});
			}
		}
	}

	public void trace(final Context context, final String id, final String subtitle) {
		if (AppDebugConfig.IS_STATISTICS_SHOW) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_STATICS, "id = " + id + ", subtitle = " + subtitle + ", isInit = " +
						mIsInit);
			}
//			KLog.d(AppDebugConfig.TAG_WARN, "mIsinit = " + mIsInit);
			if (mIsInit) {
				Global.THREAD_POOL.execute(new Runnable() {
					@Override
					public void run() {

//			TCAgent.onEvent(context, title, subtitle);
						MobclickAgent.onEvent(context, id, subtitle);
					}
				});
			}
		}
	}

	public void trace(final Context context, final String id, final Map keyMap, final int val) {
		if (AppDebugConfig.IS_STATISTICS_SHOW) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_STATICS, "id = " + id + ", keyMap = " + keyMap + ", val = " + val + ", " +
						"isInit = " + mIsInit);
			}
			if (mIsInit) {
				Global.THREAD_POOL.execute(new Runnable() {
					@Override
					public void run() {

//			TCAgent.onEvent(context, title, subtitle, keyMap);
						if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
							keyMap.put("__ct__", String.valueOf(val));
							MobclickAgent.onEvent(context, id, keyMap);
						} else {
							MobclickAgent.onEventValue(context, id, keyMap, val);
						}
					}
				});
			}
		}
	}

	public void exit(Context context) {
		if (AppDebugConfig.IS_STATISTICS_SHOW) {
			if (mIsInit) {
				MobclickAgent.onKillProcess(context);
			}
			mIsInit = false;
		}
	}


	@SuppressLint("NewApi")

	public static boolean checkPermission(Context context, String permission) {
		boolean result = false;

		if (Build.VERSION.SDK_INT >= 23) {
			if (context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
				result = true;
			}
		} else {
			PackageManager pm = context.getPackageManager();

			if (pm.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
				result = true;
			}
		}

		return result;
	}

	/**
	 * 友盟测试设备数据采集
	 *
	 * @param context
	 * @return
	 */
	public static String getDeviceInfo(Context context) {
		try {
			org.json.JSONObject json = new org.json.JSONObject();
			android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context.getSystemService
					(Context.TELEPHONY_SERVICE);

			String device_id = null;

			if (checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
				device_id = tm.getDeviceId();
			}

			android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context.getSystemService(Context
					.WIFI_SERVICE);

			String mac = wifi.getConnectionInfo().getMacAddress();

			json.put("mac", mac);

			if (TextUtils.isEmpty(device_id)) {
				device_id = mac;
			}


			if (TextUtils.isEmpty(device_id)) {
				device_id = android.provider.Settings.Secure.getString(context.getContentResolver(),
						android.provider.Settings.Secure.ANDROID_ID);
			}

			json.put("device_id", device_id);

			return json.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
