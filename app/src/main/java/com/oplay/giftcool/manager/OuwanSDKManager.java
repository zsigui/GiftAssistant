package com.oplay.giftcool.manager;

import android.content.Context;
import android.os.Handler;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.model.data.resp.UserSession;

import net.ouwan.umipay.android.api.AccountCallbackListener;
import net.ouwan.umipay.android.api.ActionCallbackListener;
import net.ouwan.umipay.android.api.GameParamInfo;
import net.ouwan.umipay.android.api.GameUserInfo;
import net.ouwan.umipay.android.api.InitCallbackListener;
import net.ouwan.umipay.android.api.PayCallbackListener;
import net.ouwan.umipay.android.api.UmipayBrowser;
import net.ouwan.umipay.android.api.UmipaySDKManager;
import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.config.SDKConstantConfig;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.ouwan.umipay.android.global.Global_Url_Params;
import net.ouwan.umipay.android.manager.ListenerManager;
import net.ouwan.umipay.android.manager.UmipayAccountManager;
import net.youmi.android.libs.webjs.view.webview.Flags_Browser_Config;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.List;

/**
 * Created by zsigui on 16-1-14.
 */
public class OuwanSDKManager implements InitCallbackListener, ActionCallbackListener, PayCallbackListener {

    private Context mContext = AssistantApp.getInstance().getApplicationContext();
    private Handler mHandler = new Handler(mContext.getMainLooper());

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
        gameParamInfo.setAppId(AppConfig.APP_KEY);//设置AppID
        gameParamInfo.setAppSecret(AppConfig.APP_SECRET);//设置AppSecret
//		gameParamInfo.setTestMode(AppConfig.TEST_MODE); //设置测试模式，模式非测试模式
        gameParamInfo.setTestMode(false);
        gameParamInfo.setChannelId(AssistantApp.getInstance().getChannelId() + "");
        gameParamInfo.setSubChannelId("0");
        UmipaySDKManager.initSDK(mContext, gameParamInfo, this, new AccountCallbackListener() {
            @Override
            public void onLogin(int code, GameUserInfo userInfo) {
                AppDebugConfig.d(AppDebugConfig.TAG_MANAGER, "onLogin = " + code);
            }

            @Override
            public void onLogout(int code, Object params) {
                // 修改密码，可能会调用该登出接口
                AppDebugConfig.d(AppDebugConfig.TAG_MANAGER, "onLogout = " + code + ", " + params);
                AccountManager.getInstance().logout();
            }
        });
        ListenerManager.setPayCallbackListener(this);
        ListenerManager.setActionCallbackListener(this);
    }

    public void login() {
        UserSession user = AccountManager.getInstance().getUserSesion();
        // 此处游戏账号只为占位
        GameUserInfo sdkUser = new GameUserInfo();
        sdkUser.setOpenId(user.openId);
        UmipayAccount account;
        if (AccountManager.getInstance().isPhoneLogin()) {
            account = new UmipayAccount(user.openId, null, UmipayAccount.TYPE_PHONE);
            account.setUserName(AccountManager.getInstance().getUserInfo().username);
        } else {
            account = new UmipayAccount(user.openId, null, UmipayAccount.TYPE_NORMAL);
        }
        account.setSession(user.session);
        account.setUid(user.uid);
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

    /**
     * 初始化结果回调
     *
     * @param code
     * @param message
     */
    // 现在有个问题，网络加载问题？退出程序才加载
    @Override
    public void onSdkInitFinished(int code, String message) {
        AppDebugConfig.d(AppDebugConfig.TAG_MANAGER, "初始化结果 = " + code + ":" + message);
        if (code != UmipaySDKStatusCode.SUCCESS) {
            // 再次执行，最多重试三次
            if (mRetryTime++ < 3) {
                init();
            }
        }
    }

    /**
     * 购买礼包调用支付接口
     *
     * @param tradeNo  订单号
     * @param pay      支付金额,元
     * @param desc     订单描述
     * @param userId   用户id
     * @param listener 支付回调
     */
//	public void pay(String tradeNo, int pay, String desc, int userId, PayCallbackListener listener) {
//		UmipaymentInfo payInfo = new UmipaymentInfo();
//		payInfo.setAmount(pay * 10);
//		payInfo.setTradeno(tradeNo);
//		payInfo.setDesc(desc);
//		payInfo.setRoleId(String.valueOf(userId));
//		payInfo.setRoleName("");
//		payInfo.setServiceType(UmipaymentInfo.SERVICE_TYPE_QUOTA);
//		UmipaySDKManager.showPayView(mContext, payInfo, listener);
//	}


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
            AppDebugConfig.w(AppDebugConfig.TAG_MANAGER, e);
        }
    }

    public void showOuwanModifyPwdView(Context context) {
        jumpToDestUrl(context, "修改登录密码", "modifypsw", UmipayBrowser.ACTION_MODIFY_PSW);
    }

    public void showChangePhoneView(Context context) {
        jumpToDestUrl(context, "修改绑定手机", "changephone", UmipayBrowser.ACTION_CHANGE_PHONE);
    }

    public void showBindPhoneView(Context context) {
        jumpToDestUrl(context, "绑定手机号码", "bindphone", UmipayBrowser.ACTION_BIND_PHONE);
    }

    public void showBindOuwanView(Context context) {
        jumpToDestUrl(context, "绑定偶玩账号", "bindoauth", UmipayBrowser.ACTION_BIND_OUWAN);
    }

    public void showForgetPswView(Context context) {
        UmipaySDKManager.showRegetPswView(context);
    }

    private void jumpToDestUrl(Context context, String title, String dest, int actionType) {
        String url = SDKConstantConfig.get_UMIPAY_JUMP_URL(context);
        List<NameValuePair> paramsList = Global_Url_Params.getDefaultRequestParams(context,
                SDKConstantConfig.get_UMIPAY_ACCOUNT_URL(context));
        int payType = UmipayBrowser.NOT_PAY;
        paramsList.add(new BasicNameValuePair("dest", dest));
        paramsList.add(new BasicNameValuePair("from", "giftcool"));
        UmipayBrowser.postUrl(context, title, url, paramsList, Flags_Browser_Config.FLAG_AUTO_CHANGE_TITLE |
                Flags_Browser_Config.FLAG_USE_YOUMI_JS_INTERFACES, null, null, payType, actionType);
    }

    @Override
    public void onActionCallback(final int action, final int code) {
        AppDebugConfig.d(AppDebugConfig.TAG_MANAGER, "action = " + action + ", code = " + code);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ObserverManager.getInstance().notifyUserActionUpdate(action, code);
            }
        });
    }

    @Override
    public void onPay(int code) {
        AppDebugConfig.d(AppDebugConfig.TAG_MANAGER, "pay_result：" + code);
        if (code == UmipaySDKStatusCode.PAY_FINISH) {
            AccountManager.getInstance().updatePartUserInfo();
        }
    }
}
