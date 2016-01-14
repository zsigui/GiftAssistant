package com.oplay.giftassistant.manager;

import android.content.Context;

import com.oplay.giftassistant.AssistantApp;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.model.data.resp.UserSession;
import com.oplay.giftassistant.util.ToastUtil;
import com.socks.library.KLog;

import net.ouwan.umipay.android.api.GameParamInfo;
import net.ouwan.umipay.android.api.GameUserInfo;
import net.ouwan.umipay.android.api.InitCallbackListener;
import net.ouwan.umipay.android.api.PayCallbackListener;
import net.ouwan.umipay.android.api.UmipayBrowser;
import net.ouwan.umipay.android.api.UmipaySDKManager;
import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.api.UmipaymentInfo;
import net.ouwan.umipay.android.config.SDKConstantConfig;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.ouwan.umipay.android.global.Global_Url_Params;
import net.ouwan.umipay.android.manager.UmipayAccountManager;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.webjs.view.webview.Flags_Browser_Config;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.List;

/**
 * Created by zsigui on 16-1-14.
 */
public class OuwanSDKManager implements InitCallbackListener {

	private Context mContext = AssistantApp.getInstance().getApplicationContext();

	private static OuwanSDKManager manager = new OuwanSDKManager();

	public static OuwanSDKManager getInstance() {
		if (manager == null) {
			manager = new OuwanSDKManager();
		}
		return manager;
	}
	private int mRetryTime = 0;
	public void init() {
		GameParamInfo gameParamInfo = new GameParamInfo();
		gameParamInfo.setAppId("13480bc2ae0d5e32");//设置AppID
		gameParamInfo.setAppSecret("96f27505691e5f54");//设置AppSecret
		gameParamInfo.setTestMode(false); //设置测试模式，模式非测试模式
		UmipaySDKManager.initSDK(mContext, gameParamInfo, this, null);
	}

	public void login() {
		UserSession user = AccountManager.getInstance().getUserSesion();
		// 此处游戏账号只为占位
		GameUserInfo sdkUser = new GameUserInfo();
		sdkUser.setOpenId(user.openId);
		UmipayAccount account = new UmipayAccount(user.openId, user.session, UmipayAccount.TYPE_NORMAL);
		account.setGameUserInfo(sdkUser);
		UmipayAccountManager.getInstance(mContext).setCurrentAccount(account);
		UmipayAccountManager.getInstance(mContext).setIsLogout(false);
		UmipayAccountManager.getInstance(mContext).setLogin(true);
	}

	public void logout() {
		UmipayAccountManager.getInstance(mContext).setCurrentAccount(null);
		UmipayAccountManager.getInstance(mContext).setIsLogout(true);
		UmipayAccountManager.getInstance(mContext).setLogin(false);
	}

	@Override
	public void onSdkInitFinished(int code, String message) {
		if (AppDebugConfig.IS_DEBUG) {
			KLog.d("初始化结果 = " + code + ":" + message);
		}
		if (code != UmipaySDKStatusCode.SUCCESS) {
			// 再次执行，最多重试三次
			if (mRetryTime ++ < 3) {
				init();
			} else {
				ToastUtil.showLong("配置支付模块失败");
			}
		}
	}

	/**
	 *
	 * 购买礼包调用支付接口
	 *
	 * @param tradeNo 订单号
	 * @param pay 支付金额,元
	 * @param desc 订单描述
	 * @param userId 用户id
	 * @param listener 支付回调
	 */
	public void pay(String tradeNo, int pay, String desc, int userId, PayCallbackListener listener) {
		UmipaymentInfo payInfo = new UmipaymentInfo();
		payInfo.setAmount(pay * 10);
		payInfo.setTradeno(tradeNo);
		payInfo.setDesc(desc);
		payInfo.setRoleId(String.valueOf(userId));
		payInfo.setRoleName("");
		payInfo.setServiceType(UmipaymentInfo.SERVICE_TYPE_QUOTA);
		UmipaySDKManager.showPayView(mContext, payInfo, listener);
	}


	/**
	 * 充值偶玩豆接口
	 */
	public void recharge() {
		showOuwanChargeView(mContext);
	}

	/**
	 * 添加了偶玩客户端参数，以便直接跳转到充值页面
	 *
	 * @param context
	 */
	private void showOuwanChargeView(Context context) {
		try {
			List<NameValuePair> paramsList = Global_Url_Params.getDefaultRequestParams(context,
					SDKConstantConfig.get_UMIPAY_ACCOUNT_URL(context));
			paramsList.add(new BasicNameValuePair("recharge_source", String.valueOf(3)));//指定偶玩豆充值
			paramsList.add(new BasicNameValuePair("bankType", "upmp")); //指定银行卡充值类型
			final String title = "偶玩豆充值";
			UmipayBrowser.postUrl(
					context,
					title,
					SDKConstantConfig.get_UMIPAY_PAY_URL(context),
					paramsList,
					Flags_Browser_Config.FLAG_AUTO_CHANGE_TITLE | Flags_Browser_Config.FLAG_USE_YOUMI_JS_INTERFACES,
					null, null, UmipayBrowser.PAY_OUWAN
			);
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
	}
}
