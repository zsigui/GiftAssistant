package net.youmi.android.libs.platform.global;

import android.content.Context;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.global.Global_SharePreferences;
import net.youmi.android.libs.platform.PlatformConstant;
import net.youmi.android.libs.platform.SDKBuild;
import net.youmi.android.libs.platform.debug.Debug_AdLog;

/**
 * 保存appid、appSecure、customUserId
 *
 * @author zhitaocai edit on 2014-5-22
 */
public class Global_DeveloperConfig {

	private final static String APPCONFING_FILENAME = PlatformConstant.get_PlKey_SpFileName();

	// appID
	private final static String KEY_APPID = PlatformConstant.get_PlKey_SpFile_AppidKey();

	private final static String KEY_SALT_APPID = PlatformConstant.get_PlKey_SpFile_AppidSaltKey();

	// appSecure
	private final static String KEY_APPSecure = PlatformConstant.get_PlKey_SpFile_AppSecretKey();

	private final static String KEY_SALT_APPSecure = PlatformConstant.get_PlKey_SpFile_AppSecretSaltKey();

	// customUserID
	private final static String KEY_CUSTOM_USERID = PlatformConstant.get_PlKey_SpFile_UserIdKey();

	private final static String KEY_SALT_CUSTOM_USERID = PlatformConstant.get_PlKey_SpFile_UserIdSaltKey();

	// chn
	private final static String KEY_CHN = PlatformConstant.get_PlKey_SpFile_ChannelIdKey();

	private final static String KEY_SALT_CHN = PlatformConstant.get_PlKey_SpFile_ChannelIdSaltKey();

	// 屏蔽渠道号 sheild channel id
	private final static String KEY_SCHN = PlatformConstant.get_PlKey_SpFile_SheildChannelIdKey();

	private final static String KEY_SALT_SCHN = PlatformConstant.get_PlKey_SpFile_SheildChannelIdSaltKey();

	// 微信appid
	private final static String KEY_WEIXIN_APPID = PlatformConstant.get_PlKey_SpFile_WeixinAppidIdKey();

	private final static String KEY_SALT_WEIXIN_APPID = PlatformConstant.get_PlKey_SpFile_WeixinAppIdSaltKey();

	/**
	 * 渠道号在metadata里面的标识name
	 *
	 * @return YOUMI_CHANNEL
	 */
	public static String getKEY_ANDROIDMANIFEST_CHANNEL() {
		return PlatformConstant.get_PlKey_YoumiChannel();
	}

	/**
	 * 屏蔽渠道号在metadata里面的标识name
	 *
	 * @return SHEILD_CHANNEL
	 */
	public static String getKEY_ANDROIDMANIFEST_SHEILD_CHANNEL() {
		return PlatformConstant.get_PlKey_SheildChannel();
	}

	/**
	 * Appid (保留原始16位字符串)
	 */
	private static String mAppId;

	private static String mAppSecret;

	private static String mAppVersionName;

	private static int mAppVersionCode = -1;

	/**
	 * 推广渠道号，默认渠道为0，初始化值为-1，如果开发者没有设置则变为0，否则为开发者设置的大于0的值。
	 */
	private static int mYoumiChannelId = -1;

	/**
	 * 屏蔽渠道号，默认渠道为0，初始化值为-1，如果开发者没有设置则变为0，否则为开发者设置的大于0的值。
	 */
	private static int mShieldChannelId = -1;

	/**
	 * 开发者自定义UserID用于服务器回调积分赚取订单时唯一地标识一个用户。
	 */
	private static String mCustomUserId;

	/**
	 * 分享任务所用到的微信appid
	 */
	private static String mWeixinAppid;

	/**
	 * 是否为测试模式，默认为false
	 */
	private static boolean mIsInTestMode = false;

	/**
	 * 获取AppId
	 *
	 * @return
	 */
	public static String getAppID(Context context) {
		try {
			synchronized (APPCONFING_FILENAME) {
				if (SDKBuild.IS_WEIXIN_SDK) {
					mAppId = PlatformConstant.get_PlKey_OldWeixinSdk_Appid();
					return mAppId;
				}

				if (mAppId == null) {
					mAppId = Global_SharePreferences
							.getStringFromSharedPreferences(context, APPCONFING_FILENAME, KEY_APPID, KEY_SALT_APPID,
									null);
					if (Debug_SDK.isGlobalLog) {
						Debug_SDK.td(Debug_SDK.mGlobalTag, Global_DeveloperConfig.class,
								"get appid from sharedpreferences:" + mAppId);
					}
				} else {
					if (mAppId.length() <= 0) {
						mAppId = Global_SharePreferences
								.getStringFromSharedPreferences(context, APPCONFING_FILENAME, KEY_APPID,
										KEY_SALT_APPID, null);
						if (Debug_SDK.isGlobalLog) {
							Debug_SDK.td(Debug_SDK.mGlobalTag, Global_DeveloperConfig.class,
									"get appid from sharedpreferences:" + mAppId);
						}
					}
				}
			}
		} catch (Throwable e) {
			Debug_AdLog.e(e);
		}
		return mAppId;
	}

	/**
	 * 设置 AppId,保留16位
	 *
	 * @param appid
	 */
	public static void setAppID(Context context, String appid) {
		try {
			synchronized (APPCONFING_FILENAME) {
				if (SDKBuild.IS_WEIXIN_SDK) {
					mAppId = PlatformConstant.get_PlKey_OldWeixinSdk_Appid();
					return;
				}
				if (appid != null) {
					appid = appid.trim();
					if (appid.length() > 0) {

						// _srcLogAppid = a;
						// 将appid转为11位
						// _appid = a.substring(0, 1)
						// + Coder_CECoder
						// .converHexTo64_GwRule(a.substring(1));

						mAppId = appid;// 设置Appid

						Global_SharePreferences
								.saveEncodeStringToSharedPreferences(context, APPCONFING_FILENAME, KEY_APPID, appid,
										KEY_SALT_APPID);

						if (Debug_SDK.isGlobalLog) {
							Debug_SDK.td(Debug_SDK.mGlobalTag, Global_DeveloperConfig.class, "设置Appid:%s", appid);
						}
					}
				}
			}
		} catch (Throwable e) {
			Debug_AdLog.e(e);
		}
	}

	/**
	 * 获取密钥
	 *
	 * @param context
	 * @return
	 */
	public static String getAppSecret(Context context) {
		try {
			synchronized (APPCONFING_FILENAME) {
				if (SDKBuild.IS_WEIXIN_SDK) {
					mAppSecret = PlatformConstant.get_PlKey_OldWeixinSdk_AppSecret();
					return mAppSecret;
				}
				if (mAppSecret != null) {
					return mAppSecret;
				} else {
					mAppSecret = Global_SharePreferences
							.getStringFromSharedPreferences(context, APPCONFING_FILENAME, KEY_APPSecure,
									KEY_SALT_APPSecure,
									null);
					if (Debug_SDK.isGlobalLog) {
						Debug_SDK.td(Debug_SDK.mGlobalTag, Global_DeveloperConfig.class,
								"get appsecret from sharedpreferences:" + mAppSecret);
					}
				}
			}
		} catch (Throwable e) {
			Debug_AdLog.e(e);
		}
		return mAppSecret;
	}

	/**
	 * 设置密钥
	 *
	 * @param context
	 * @param secret
	 */
	public static void setAppSecret(Context context, String secret) {
		try {
			synchronized (APPCONFING_FILENAME) {
				if (SDKBuild.IS_WEIXIN_SDK) {
					mAppSecret = PlatformConstant.get_PlKey_OldWeixinSdk_AppSecret();
					return;
				}
				if (secret != null) {
					secret = secret.trim();
					if (secret.length() > 0) {
						mAppSecret = secret;

						// 保存到文件中
						Global_SharePreferences
								.saveEncodeStringToSharedPreferences(context, APPCONFING_FILENAME, KEY_APPSecure,
										secret,
										KEY_SALT_APPSecure);
						if (Debug_SDK.isGlobalLog) {
							Debug_SDK.td(Debug_SDK.mGlobalTag, Global_DeveloperConfig.class, "设置AppSec:%s",
									mAppSecret);
						}
					}
				}
			}
		} catch (Throwable e) {
			Debug_AdLog.e(e);
		}
	}

	/**
	 * 获取开发者自定义UserID。<br/>
	 * 自定义UserID用于服务器回调积分赚取订单时唯一地标识一个用户。
	 *
	 * @param context
	 * @return
	 */
	public static String getCustomUserId(Context context) {

		try {
			synchronized (APPCONFING_FILENAME) {
				if (mCustomUserId == null) {
					mCustomUserId = Global_SharePreferences
							.getStringFromSharedPreferences(context, APPCONFING_FILENAME, KEY_CUSTOM_USERID,
									KEY_SALT_CUSTOM_USERID, null);
				} else {
					if (mCustomUserId.length() <= 0) {
						mCustomUserId = Global_SharePreferences
								.getStringFromSharedPreferences(context, APPCONFING_FILENAME, KEY_CUSTOM_USERID,
										KEY_SALT_CUSTOM_USERID, null);
					}
				}
			}
		} catch (Throwable e) {
			Debug_AdLog.e(e);
		}

		return mCustomUserId;
	}

	/**
	 * 设置开发者自定义UserID<br/>
	 * 自定义UserID用于服务器回调积分赚取订单时唯一地标识一个用户。
	 *
	 * @param context
	 * @param customUserid
	 */
	public static void setCustomUserId(Context context, String customUserid) {
		try {
			synchronized (APPCONFING_FILENAME) {
				if (customUserid != null) {
					customUserid = customUserid.trim();
					if (customUserid.length() > 0) {
						mCustomUserId = customUserid;
						Global_SharePreferences
								.saveEncodeStringToSharedPreferences(context, APPCONFING_FILENAME, KEY_CUSTOM_USERID,
										customUserid, KEY_SALT_CUSTOM_USERID);
					}
				}
			}
		} catch (Throwable e) {
			Debug_AdLog.e(e);
		}
	}

	/**
	 * 检查appid和密码是否有设置
	 *
	 * @return
	 */
	public static boolean isAppID_Secret_NotEmpty(Context context) {
		try {
			String appid = getAppID(context);
			String appSecret = getAppSecret(context);
			return ((appid != null) && (appid.length() > 0) && (appSecret != null) && (appSecret.length() > 0));
		} catch (Throwable e) {
			Debug_AdLog.e(e);
		}
		return false;
	}

	/**
	 * 获取App版本名
	 *
	 * @param context
	 * @return
	 */
	public static String getAppVersionName(Context context) {
		if (mAppVersionName == null) {
			try {
				if (context == null) {
					return mAppVersionName;
				}
				mAppVersionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
			} catch (Throwable e) {
				Debug_AdLog.e(e);
			}
		}
		return mAppVersionName;
	}

	/**
	 * 获取App版本号
	 *
	 * @param context
	 * @return
	 */
	public static int getAppVersionCode(Context context) {
		if (mAppVersionCode < 0) {
			try {
				if (context == null) {
					return mAppVersionCode;
				}
				mAppVersionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
			} catch (Throwable e) {
				Debug_AdLog.e(e);
			}
		}

		return mAppVersionCode;
	}

	/**
	 * 设置有米广告推广渠道号 <br>
	 * 1-9999为有米官方保留渠道 <br>
	 * 10000-29999为有米官方为各大应用市场等定义或者保留的渠道号 <br>
	 * 30000-65535开发者可以自由使用
	 *
	 * @param context
	 * @param secret
	 */
	public static void setChannelID(Context context, int channel) {
		try {
			synchronized (APPCONFING_FILENAME) {
				if (channel < 0) {
					Debug_AdLog.e(PlatformConstant.get_Tips_ChannelIdError_tooSmall());
					return;
				}
				if (channel > 65535) {
					Debug_AdLog.e(PlatformConstant.get_Tips_ChannelIdError_tooBig());
					return;
				}
				// 1、更新内存值
				mYoumiChannelId = channel;

				// 2、保存到文件中
				boolean isSuccess = Global_SharePreferences
						.saveEncodeStringToSharedPreferences(context, APPCONFING_FILENAME, KEY_CHN, mYoumiChannelId +
										"",
								KEY_SALT_CHN);
				if (Debug_SDK.isGlobalLog) {
					Debug_SDK.td(Debug_SDK.mGlobalTag, Global_DeveloperConfig.class, "set channel id:%d file save:%b",
							mYoumiChannelId, isSuccess);
				}
			}
		} catch (Throwable e) {
			Debug_AdLog.e(e);
		}
	}

	/**
	 * 设置有米广告屏蔽渠道号 <br>
	 * <p/>
	 * * 1-9999为有米官方保留渠道 <br>
	 * 10000-29999为有米官方为各大应用市场等定义或者保留的渠道号 <br>
	 * 30000-65535开发者可以自由使用
	 *
	 * @param context
	 * @param secret
	 */
	public static void setShieldChannelID(Context context, int channel) {
		try {
			synchronized (APPCONFING_FILENAME) {
				if (channel < 0) {
					Debug_AdLog.e(PlatformConstant.get_Tips_ChannelIdError_tooSmall());
					return;
				}
				if (channel > 65535) {
					Debug_AdLog.e(PlatformConstant.get_Tips_ChannelIdError_tooBig());
					return;
				}
				// 1、更新内存值
				mYoumiChannelId = channel;

				// 2、保存到文件中
				boolean isSuccess = Global_SharePreferences
						.saveEncodeStringToSharedPreferences(context, APPCONFING_FILENAME, KEY_SCHN, mShieldChannelId
										+ "",
								KEY_SALT_SCHN);
				if (Debug_SDK.isGlobalLog) {
				}
			}
		} catch (Throwable e) {
			Debug_AdLog.e(e);
		}
	}

	/**
	 * 获取app推广渠道号
	 *
	 * @return
	 */
	public static int getChannelID(Context context) {
		if (mYoumiChannelId < 0) {
			try {
				String channelIdStr = Global_SharePreferences
						.getStringFromSharedPreferences(context, APPCONFING_FILENAME, KEY_CHN, KEY_SALT_CHN, "0");
				mYoumiChannelId = Integer.parseInt(channelIdStr);
			} catch (Throwable e) {
				Debug_AdLog.e(e);
			}
		}
		return mYoumiChannelId;
	}

	/**
	 * 获取app屏蔽渠道号
	 *
	 * @return
	 */
	public static int getShieldChannelID(Context context) {
		if (mShieldChannelId < 0) {
			try {
				String channelIdStr = Global_SharePreferences
						.getStringFromSharedPreferences(context, APPCONFING_FILENAME, KEY_SCHN, KEY_SALT_SCHN, "0");
				mShieldChannelId = Integer.parseInt(channelIdStr);
			} catch (Throwable e) {
				Debug_AdLog.e(e);
			}
		}
		return mShieldChannelId;
	}

	// /**
	// * 获取app推广渠道号(原来使用与meta的)
	// *
	// * @return
	// */
	// public static int getChannelID(Context context) {
	// if (mYoumiChannelId < 0) {
	// try {
	// mYoumiChannelId = Util_System_Package.getIntFromMetaData(context,
	// getKEY_ANDROIDMANIFEST_CHANNEL(), 0);
	// if (Debug_SDK.isGlobalLog) {
	// Debug_SDK.td(Debug_SDK.mGlobalTag,
	// Global_DeveloperConfig.class,
	// "channel id:%d", mYoumiChannelId);
	// }
	// } catch (Throwable e) {
	// Debug_AdLog.e(e);
	// }
	// }
	// return mYoumiChannelId;
	// }

	public static String getWeixinAppId(Context context) {

		try {
			synchronized (APPCONFING_FILENAME) {
				if (Basic_StringUtil.isNullOrEmpty(mWeixinAppid)) {
					mWeixinAppid = Global_SharePreferences
							.getStringFromSharedPreferences(context, APPCONFING_FILENAME, KEY_WEIXIN_APPID,
									KEY_SALT_WEIXIN_APPID,
									null);
				}
			}
		} catch (Throwable e) {
			Debug_AdLog.e(e);
		}

		return mWeixinAppid;
	}

	public static void setWeixinAppId(Context context, String weixinAppid) {
		try {
			synchronized (APPCONFING_FILENAME) {
				if (weixinAppid != null) {
					weixinAppid = weixinAppid.trim();
					if (weixinAppid.length() > 0) {
						mWeixinAppid = weixinAppid;
						Global_SharePreferences
								.saveEncodeStringToSharedPreferences(context, APPCONFING_FILENAME, KEY_WEIXIN_APPID,
										weixinAppid,
										KEY_SALT_WEIXIN_APPID);
					}
				}
			}
		} catch (Throwable e) {
			Debug_AdLog.e(e);
		}
	}

	/**
	 * 设置测试模式
	 *
	 * @param flag
	 */
	public static void setTestMode(boolean flag) {
		mIsInTestMode = flag;
	}

	/**
	 * 是否为测试模式
	 *
	 * @return
	 */
	public static boolean isInTestMode() {
		return mIsInTestMode;
	}

	//
	// /**
	// * 是否启用app自动更新服务，默认为true，开发者可以设置为false。
	// */
	// private static boolean _isEnableUpdateApp = true;
	//
	// /**
	// * 是否启用app自动更新服务
	// *
	// * @return
	// */
	// static boolean isEnableUpdateApp() {
	// return _isEnableUpdateApp;
	// }
	//
	// /**
	// * 不启用app自动更新服务
	// */
	// static void disableUpdateApp() {
	// _isEnableUpdateApp = false;
	// }
	//
	// /**
	// * 是否可以删除webview cache 如果true则可以删除，false则不可以删除，默认为true
	// */
	// private static boolean _canClearWebviewCache = true;
	//
	// static void setDonotClearWebviewCache() {
	// _canClearWebviewCache = false;
	// }
	//
	// /**
	// * 是否可以删除webview cache 如果true则可以删除，false则不可以删除，默认为true
	// *
	// * @return
	// */
	// static boolean canClearWebviewCache() {
	// return _canClearWebviewCache;
	// }
	//
	// // //////////////
	// // 指定该App的目标运行环境
	// private static String destUI = "0";
	//
	// static void setDestUI(String ui) {
	// destUI = ui;
	// }
	//
	// static String getDestUI() {
	// return destUI;
	// }

}
