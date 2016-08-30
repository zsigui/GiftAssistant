package net.ouwan.umipay.android.handler;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayCommonAccount;
import net.ouwan.umipay.android.manager.UmipayAccountManager;
import net.ouwan.umipay.android.manager.UmipayCommonAccountCacheManager;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.common.util.Util_System_Package;
import net.youmi.android.libs.webjs.js.base.handler.Interface_Browser_Handler;
import net.youmi.android.libs.webjs.js.base.handler.Interface_SDK_Handler;
import net.youmi.android.libs.webjs.js.base.handler.JsHandler_abstract_Params_NoPsw_Handler;

import org.json.JSONObject;

public class JsHandler_ChangeCommonAccount_SaveSession extends JsHandler_abstract_Params_NoPsw_Handler {

	String packageName = null;
	Context context;

	@Override
	protected JSONObject toHandler(Interface_SDK_Handler sdkHandler,
	                               Interface_Browser_Handler browserHandler, JSONObject jo) {
		try {

			if (sdkHandler == null) {
				return toSimpleCodeJson(Err_Exception);
			}

			context = sdkHandler.getApplicationContext();

			if (context == null) {
				return toSimpleCodeJson(Err_Exception);
			}


			packageName = Basic_JSONUtil.getString(jo,
					"a", null);

			if(TextUtils.isEmpty(packageName)){
				return toSimpleCodeJson(Err_Exception);
			}

		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		// 线程处理

		try {
			Runnable runnable = new Runnable() {

				@Override
				public void run() {
					try {

						long ts = 0;
						ts = System.currentTimeMillis();
						UmipayCommonAccount account = new UmipayCommonAccount(packageName, context.getPackageName(),
								Util_System_Package.getAppNameforCurrentContext(context), ts);
						account.setUid(UmipayAccountManager.getInstance(context).getCurrentAccount().getUid());
						account.setSession(UmipayAccountManager.getInstance(context).getCurrentAccount().getSession());
						account.setUserName(UmipayAccountManager.getInstance(context).getCurrentAccount().getUserName
								());
						UmipayCommonAccountCacheManager.getInstance(context).addCommonAccount(account,UmipayCommonAccountCacheManager.COMMON_ACCOUNT_TO_CHANGE);

						Intent broadcastIntent = new Intent();
						broadcastIntent.setAction(UmipayCommonAccountCacheManager.ACTION_ACCOUNT_CHANGE);
						broadcastIntent.setPackage(packageName);context.sendBroadcast(broadcastIntent);
					} catch (Throwable e) {
						Debug_Log.e(e);
					}
				}
			};

			if (browserHandler.js_SDK_Handler_RunOnUIThread(runnable)) {
				return toSimpleCodeJson(OK);
			}

			return toSimpleCodeJson(Err_Exception);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}

		return null;
	}
}
