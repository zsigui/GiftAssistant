package net.ouwan.umipay.android.api;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Gravity;
import android.webkit.CookieManager;
import android.widget.Toast;

import net.ouwan.umipay.android.Utils.Util_FileHelper;
import net.ouwan.umipay.android.Utils.Util_Loadlib;
import net.ouwan.umipay.android.Utils.Util_Manifest;
import net.ouwan.umipay.android.asynctask.TaskCMD;
import net.ouwan.umipay.android.config.ConstantString;
import net.ouwan.umipay.android.config.SDKCacheConfig;
import net.ouwan.umipay.android.config.SDKConstantConfig;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.ouwan.umipay.android.entry.UmipayCommonAccount;
import net.ouwan.umipay.android.global.Global_Url_Params;
import net.ouwan.umipay.android.manager.ChannelManager;
import net.ouwan.umipay.android.manager.ListenerManager;
import net.ouwan.umipay.android.manager.PushPullAlarmManager;
import net.ouwan.umipay.android.manager.UmipayAccountManager;
import net.ouwan.umipay.android.manager.UmipayCommandTaskManager;
import net.ouwan.umipay.android.manager.UmipayCommonAccountCacheManager;
import net.ouwan.umipay.android.view.UmipayExitDialog;
import net.ouwan.umipay.android.view.UmipayLoginInfoDialog;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.global.Global_Runtime_SystemInfo;
import net.youmi.android.libs.common.network.Util_Network_Status;
import net.youmi.android.libs.platform.coder.Coder_SDKPswCoder;
import net.youmi.android.libs.platform.global.Global_DeveloperConfig;
import net.youmi.android.libs.platform.global.Global_Runtime_ClientId;
import net.youmi.android.libs.webjs.view.webview.Flags_Browser_Config;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * UmipaySDKManager
 *
 * @author zacklpx
 *         date 15-1-27
 *         description
 */
public class UmipaySDKManager {

	private static Context mShowLoginViewContext;

	/**
	 * @Title: initSDK
	 * @Description:初始化接口，异步操作
	 */
	public static void initSDK(final Context context, GameParamInfo gameparams, InitCallbackListener initListener,
	                           AccountCallbackListener accountListener) {
		try {
			Context mAppContext;
			//设置初始化回调
			ListenerManager.setInitCallbackListener(initListener);
			//设置账户登入登出回调
			ListenerManager.setAccountCallbackListener(accountListener);

			//检测初始化参数

			if (context == null || gameparams == null || TextUtils.isEmpty(gameparams.getAppId()) || TextUtils.isEmpty
					(gameparams.getAppSecret()) || initListener == null || accountListener == null) {
				ListenerManager.callbackInitFinish(UmipaySDKStatusCode.PARAMETER_ERR,
						null);
				return;
			}

			mAppContext = context.getApplicationContext();

			try {
				JSONObject versionJson = new JSONObject(Util_FileHelper.readFileAssets(mAppContext, "umipaysdkinfo" +
						".json"));
				String sdkVersion = Integer.toString(SDKConstantConfig.UMIPAY_SDK_VERSION);
				if (!sdkVersion.equals(Basic_JSONUtil.getString(versionJson, "sdkversion", ""))) {
					//版本号不对
					ListenerManager.callbackInitFinish(UmipaySDKStatusCode.ERR_WRONG_SDKVERSION, "版本号不匹配");
					return;
				}
			} catch (Throwable e) {
				Debug_Log.e(e);
				//解析版本号出错
				ListenerManager.callbackInitFinish(UmipaySDKStatusCode.ERR_WRONG_SDKVERSION, "解析版本号错误");
				return;
			}
			//如果代码没有设置渠道id，则从manifest读取渠道
			String chn = gameparams.getChannelId();
			String subChn = gameparams.getSubChannelId();

			//如果没有setChannel,则读取manifest里面的渠道与子渠道id
			try {
				if (TextUtils.isEmpty(chn)) {
					chn = Util_Manifest.getInt(mAppContext, "UMIPAY_CHANNEL") + "";
				}

				if (TextUtils.isEmpty(gameparams.getSubChannelId())) {
					subChn = Util_Manifest.getInt(mAppContext, "UMIPAY_SUBCHANNEL") + "";
				}
			} catch (Exception e) {
				Debug_Log.e(e);
				Toast.makeText(context, "获取渠道号错误", Toast.LENGTH_SHORT).show();
				ListenerManager.callbackInitFinish(UmipaySDKStatusCode.ERR_WRONG_CHANNELINFO, "获取渠道号错误");
				return;
			}

			//检查渠道号，如果包设置的渠道号为0，则读缓存。如果包设置的渠道号不为0，则以包设置的渠道号为准且更新本地缓存
			ChannelManager channelManager = ChannelManager.getInstance(mAppContext);
			if (chn.equals("0")) {
				chn = channelManager.getChannel();
				subChn = channelManager.getSubChannel();
				if (null == chn) {
					chn = "0";
				}
				if (null == subChn) {
					subChn = "0";
				}
			}

			channelManager.saveChannels(chn, subChn);
			//设置Channel
			gameparams.setChannelId(chn);
			gameparams.setSubChannelId(subChn);
			GameParamInfo.copy(context, gameparams);
			GameParamInfo.getInstance(context).save();
			Global_DeveloperConfig.setAppID(mAppContext, gameparams.getAppId());
			Global_DeveloperConfig.setAppSecret(mAppContext, gameparams.getAppSecret());

			String libName = Coder_SDKPswCoder.decode(ConstantString.SO_LIB_NAME_YMFX, ConstantString.SO_LIB_NAME_KEY);
			if (!Util_Loadlib.loadlib(context, libName)) {
				ListenerManager.callbackInitFinish(UmipaySDKStatusCode.ERR_NO_SOLIB, null);
				return;
			}
			String libentryexstdName = Coder_SDKPswCoder.decode(ConstantString.SO_LIB_NAME_ENTRYEXSTD,
					ConstantString.SO_LIB_NAME_KEY);
			if (!Util_Loadlib.loadlib(context, libentryexstdName)) {
				ListenerManager.callbackInitFinish(UmipaySDKStatusCode.ERR_NO_SOLIB, null);
				return;
			}

			UmipayCommandTaskManager.getInstance(context).StartInitCommandTask();

			UmipayCommandTaskManager.getInstance(context).GetAccountListCommandTask();


			//开始时先验证原有登录态
			UmipayCommandTaskManager.getInstance(context).ValiDateSessions();

			//运行push
			PushPullAlarmManager.getInstance(context).startPolling();

		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	/**
	 * @param @param context
	 * @param @param listener 传入参数名字
	 * @return void 返回类型
	 * @Title: showLoginView
	 * @Description:显示登录界面，如果存在有效账号，这直接使用上次账号自动登录
	 * @date 2013-1-7 下午3:31:21
	 * @throw
	 */
	public static void showLoginView(Context context) {
		mShowLoginViewContext = context;
		try {
			if (context == null) {
				Debug_Log.d("ShowLoginView failed : " + UmipaySDKStatusCode.handlerMessage(UmipaySDKStatusCode
						.PARAMETER_ERR, null));
				ListenerManager.getAccountCallbackListener().onLogin(UmipaySDKStatusCode.LOGIN_CLOSE, null);
				return;
			}

			UmipayCommonAccount accountToChange = UmipayCommonAccountCacheManager.getInstance(context).popCommonAccountToChange();
			UmipayAccount lastAccount = UmipayAccountManager.getInstance(context).getFirstAccount();
			boolean autoLogin = SDKCacheConfig.getInstance(context).isAutoLogin();
			boolean isLogout = UmipayAccountManager.getInstance(context).isLogout();

			if(accountToChange != null && !TextUtils.isEmpty(accountToChange.getUserName())) {
				UmipayLoginInfoDialog dialog = new UmipayLoginInfoDialog(context, accountToChange.getUserName(), "...切换账号中，请稍等...", true,
						accountToChange);
				dialog.show(0);
			}else if(!isLogout && autoLogin && lastAccount != null && lastAccount.getOauthType() == UmipayAccount.TYPE_MOBILE && !TextUtils.isEmpty(lastAccount.getUserName())){
				//最后使用手机账号登录的，且勾选自动登录的，使用session进行自动登录
				UmipayLoginInfoDialog dialog = new UmipayLoginInfoDialog(context, "", "...自动登录中，请稍等...", true,
						lastAccount);
				dialog.show(0);
			}else if (!isLogout && autoLogin && lastAccount != null && !TextUtils.isEmpty(lastAccount.getUserName()) &&
					!TextUtils
							.isEmpty(lastAccount.getPsw()) &&
					(lastAccount.getOauthType() == UmipayAccount.TYPE_NORMAL || lastAccount.getOauthType() ==
							UmipayAccount.TYPE_VISITOR)) {
				//自动登录
				UmipayLoginInfoDialog dialog = new UmipayLoginInfoDialog(context, "", "...自动登录中，请稍等...", true,
						lastAccount);
				dialog.show(0);
			} else {
				//手动登录
				Intent intent = new Intent(context, UmipayActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				context.startActivity(intent);
			}
			UmipayAccountManager.getInstance(context).setIsLogout(false);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	public static Context getShowLoginViewContext() {
		return mShowLoginViewContext;
	}

	public static void showRegetPswView(Context context) {
		try {
			List<NameValuePair> paramsList = Global_Url_Params.getDefaultRequestParams(context,
					SDKConstantConfig.get_UMIPAY_FORGET_URL(context));
			UmipayBrowser.postUrl(context, "取回密码", SDKConstantConfig.get_UMIPAY_JUMP_URL(context), paramsList,
					Flags_Browser_Config.FLAG_AUTO_CHANGE_TITLE | Flags_Browser_Config.FLAG_USE_YOUMI_JS_INTERFACES,
					null, null, UmipayBrowser.NOT_PAY);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	public static void showAgreementView(Context context) {
		try {
			UmipayBrowser.loadUrl(context, "偶玩服务条款", SDKConstantConfig.get_UMIPAY_AGREEMENT_URL(context),
					Flags_Browser_Config.FLAG_AUTO_CHANGE_TITLE | Flags_Browser_Config.FLAG_USE_YOUMI_JS_INTERFACES,
					null, null);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	public static void showAccountManagerView(Context context) {
		try {
			if (!UmipayAccountManager.getInstance(context).isLogin()) {
				toast(context, "请先登录！");
				return;
			}
			List<NameValuePair> paramsList = Global_Url_Params.getDefaultRequestParams(context,
					SDKConstantConfig.get_UMIPAY_ACCOUNT_URL(context));
			//游戏账户中心入口 1，小红点账户中心入口 2，偶豌豆入口 3
			paramsList.add(new BasicNameValuePair("from", "1"));
			UmipayBrowser.postUrl(context, "账户管理", SDKConstantConfig.get_UMIPAY_JUMP_URL(context), paramsList,
					Flags_Browser_Config.FLAG_AUTO_CHANGE_TITLE | Flags_Browser_Config.FLAG_USE_YOUMI_JS_INTERFACES,
					null, null, UmipayBrowser.NOT_PAY);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	/**
	 * 客户端手动登出账号
	 *
	 * @param context
	 */
	public static void logoutAccount(Context context, Object params) {
		try {
			UmipayAccountManager.getInstance(context).setLogin(false);
			UmipayAccountManager.getInstance(context).setIsLogout(true);
			UmipayAccountManager.getInstance(context).setCurrentAccount(null);
			ListenerManager.sendMessage(TaskCMD.MP_CMD_LOGOUT, params);
			CookieManager.getInstance().removeSessionCookie();
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}


	private static void toast(Context context, String text) {
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	/**
	 * @param @param context
	 * @param @param payinfo 传入参数名字
	 * @return void 返回类型
	 * @Title: showPayView
	 * @Description:显示支付界面
	 * @date 2013-1-9 下午5:32:50
	 * @throw
	 */
	public static void showPayView(Context context, UmipaymentInfo payinfo, PayCallbackListener listener) {
		try {
			ListenerManager.setPayCallbackListener(listener);
			//如果非登陆状态
			if (!UmipayAccountManager.getInstance(context).isLogin()) {
				Debug_Log.d("支付失败，请先登录游戏");
				return;
			}
			if (context == null) {
				Debug_Log.d("Show PayView failed，context == null");
				return;
			}
			if (payinfo == null) {
				Debug_Log.d("Show PayView failed，PayInfo == null");
				return;
			}
			Context mAppContext = context.getApplicationContext();
			UmipayAccount account = UmipayAccountManager.getInstance(mAppContext).getCurrentAccount();
			if (account == null) {
				Debug_Log.d("Show PayView failed，Account == null");
				return;
			}
			GameUserInfo userInfo = account.getGameUserInfo();
			if (userInfo == null) {
				Debug_Log.d("Show PayView failed，UserInfo == null");
				return;
			}
			GameParamInfo gameParamInfo = GameParamInfo.getInstance(context);
			if (gameParamInfo == null) {
				Debug_Log.d("Show PayView failed，GameParamInfo == null");
				return;
			}

			//SDK信息
			int version = 0;
			int service = 0;

			//用户信息
			String openid = userInfo.getOpenId();
			String sid = account.getSession();

			//游戏信息
			String appkey = gameParamInfo.getAppId();

			//订单信息
			int amount = payinfo.getAmount();
			String svrid = payinfo.getServerId();
			String cdata = payinfo.getCustomInfo();
			String rid = payinfo.getRoleId();
			String rname = payinfo.getRoleName();
			String rgrade = payinfo.getRoleGrade();
			//业务类型
			int servicetype = payinfo.getServiceType();
			//单次支付
			int singlepay = payinfo.IsPaySetSinglePayMode() ? (payinfo.IsSinglePayMode() ? 1 : 0) : (gameParamInfo
					.isSinglePayMode() ? 1 : 0);
			//最小支付金额
			int minfee = payinfo.IsPaySetMinFee() ? payinfo.getMinFee() : gameParamInfo.getMinFee();
			//定额支付信息
			String desc = payinfo.getDesc();
			String tradeno = payinfo.getTradeno();
			int money = payinfo.getPayMoney();
			//渠道信息
			String chnid = gameParamInfo.getChannelId();
			if (Basic_StringUtil.isNullOrEmpty(chnid)) {
				chnid = "0";
			}
			String subchnid = gameParamInfo.getChannelId();
			if (Basic_StringUtil.isNullOrEmpty(subchnid)) {
				subchnid = "0";
			}

			version = SDKConstantConfig.UMIPAY_SDK_VERSION;
			service = SDKConstantConfig.UMIPAY_SDK_SERVICE;

			//可选信息
			Global_Runtime_ClientId runtime_cid = new Global_Runtime_ClientId(context);
			String imei = runtime_cid.getImei();
			String imsi = runtime_cid.getImsi();
			String cid = runtime_cid.getCid();
			String androidid = Global_Runtime_SystemInfo.getAndroidId(context);
			String apn = Util_Network_Status.getApn(context);

			if (Basic_StringUtil.isNullOrEmpty(openid) || Basic_StringUtil.isNullOrEmpty(sid)) {
				Debug_Log.d("缺少用户相关信息，充值失败");
				return;
			}
			List<NameValuePair> paramsList = new ArrayList<NameValuePair>();

			paramsList.add(new BasicNameValuePair("version", version + ""));
			paramsList.add(new BasicNameValuePair("service", service + ""));

			paramsList.add(new BasicNameValuePair("openid", openid));
			paramsList.add(new BasicNameValuePair("sid", sid));

			paramsList.add(new BasicNameValuePair("appkey", appkey));

			paramsList.add(new BasicNameValuePair("amount", amount + ""));
			paramsList.add(new BasicNameValuePair("svrid", svrid));
			paramsList.add(new BasicNameValuePair("cdata", cdata));
			paramsList.add(new BasicNameValuePair("rid", rid));
			paramsList.add(new BasicNameValuePair("rname", rname));
			paramsList.add(new BasicNameValuePair("rgrade", rgrade));
			paramsList.add(new BasicNameValuePair("chnid", chnid));
			paramsList.add(new BasicNameValuePair("subchnid", subchnid));
			paramsList.add(new BasicNameValuePair("servicetype", servicetype + ""));
			paramsList.add(new BasicNameValuePair("singlepay", singlepay + ""));
			paramsList.add(new BasicNameValuePair("minfee", minfee + ""));
			paramsList.add(new BasicNameValuePair("desc", desc));
			paramsList.add(new BasicNameValuePair("tradeno", tradeno));
			paramsList.add(new BasicNameValuePair("money", money + ""));

			paramsList.add(new BasicNameValuePair("imei", imei));
			paramsList.add(new BasicNameValuePair("imsi", imsi));
			paramsList.add(new BasicNameValuePair("cid", cid));
			paramsList.add(new BasicNameValuePair("andid", androidid));
			paramsList.add(new BasicNameValuePair("apn", apn));

			Debug_Log.d("jump pay: Setup PayView...");
			UmipayBrowser.postUrl(context, "充值", SDKConstantConfig.get_UMIPAY_PAY_URL(context), paramsList,
					Flags_Browser_Config.FLAG_AUTO_CHANGE_TITLE | Flags_Browser_Config.FLAG_USE_YOUMI_JS_INTERFACES,
					null, null, UmipayBrowser.PAY_GAME);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	public static void setGameRolerInfo(final Context context, int type, GameRolerInfo gameRolerInfo) {
		try {
			if (gameRolerInfo == null || UmipayAccountManager.getInstance(context).getCurrentAccount() == null) {
				return;
			}
			GameRolerInfo.setCurrentGameRolerInfo(gameRolerInfo);
			UmipayCommandTaskManager.getInstance(context).PushRoleInfoCommandTask(type, gameRolerInfo);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	public static void exitSDK(Context context, ExitDialogCallbackListener exitDialogCallbackListener) {
		if (context == null || exitDialogCallbackListener == null) {
			Debug_Log.d(UmipaySDKStatusCode.handlerMessage(UmipaySDKStatusCode.PARAMETER_ERR, null));
			return;
		}
		try {
			new UmipayExitDialog(context, exitDialogCallbackListener).show();
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}
}
