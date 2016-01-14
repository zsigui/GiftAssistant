package net.ouwan.umipay.android.asynctask.handler;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.config.SDKConstantConfig;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.ouwan.umipay.android.entry.gson.Gson_Login;
import net.ouwan.umipay.android.manager.ListenerManager;
import net.ouwan.umipay.android.view.UmipayLoginInfoDialog;

/**
 * RspHandler_Cmd_Login
 *
 * @author zacklpx
 *         date 15-4-28
 *         description
 */
public class RspHandler_Cmd_Login extends CommonRspHandler<Gson_Login> {
	@Override
	public void toHandle(Gson_Login data) {
		int code = data.getCode();
		Context context = data.getContext();
		if (code == UmipaySDKStatusCode.SUCCESS) {
			try {
				Gson_Login.Login_Data loginData = data.getData();
				int loginType = loginData.getLoginType();
				UmipayAccount account = loginData.getAccount();


				CookieSyncManager.createInstance(context.getApplicationContext());
				CookieManager.getInstance().setCookie(SDKConstantConfig.get_HOST_URL(context), String.format
						("OPENID=%s", account.getGameUserInfo().getOpenId()));
				CookieManager.getInstance().setCookie(SDKConstantConfig.get_HOST_URL(context), String.format
						("SID=%s", account.getSession()));
				CookieSyncManager.getInstance().sync();

				String username = ListenerManager.getUserName(account.getUserName(), account.getOauthType());
				if (loginType == UmipayLoginInfoDialog.NORMAL_LOGIN) {
					UmipayLoginInfoDialog dialog = new UmipayLoginInfoDialog(context, username, "欢迎您回来！", false, null);
					dialog.show(3000);
				}
				if (loginType == UmipayLoginInfoDialog.REGISTER_AND_LOGIN || loginType == UmipayLoginInfoDialog
						.QUCIK_REGISTER) {
					UmipayLoginInfoDialog dialog = new UmipayLoginInfoDialog(context, username, "账号注册成功！", false,
							null);
					dialog.show(3000);
				}

				try {
					if (ListenerManager.getAccountCallbackListener() != null) {
						ListenerManager.getAccountCallbackListener().onLogin(UmipaySDKStatusCode.SUCCESS, account
								.getGameUserInfo());
					}
				} catch (Throwable e) {
					Debug_Log.e(e);
				}
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
		} else {
			try {
				if (ListenerManager.getAccountCallbackListener() != null) {
					ListenerManager.getAccountCallbackListener().onLogin(UmipaySDKStatusCode.LOGIN_CLOSE, null);
				}
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
		}
	}
}
