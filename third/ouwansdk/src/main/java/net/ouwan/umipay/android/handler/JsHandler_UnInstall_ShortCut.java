package net.ouwan.umipay.android.handler;

import android.content.Context;
import android.content.Intent;

import net.ouwan.umipay.android.debug.Debug_Log;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.common.util.Util_System_Intent_Network;
import net.youmi.android.libs.common.util.Util_System_Permission;
import net.youmi.android.libs.webjs.js.base.handler.Interface_Browser_Handler;
import net.youmi.android.libs.webjs.js.base.handler.Interface_SDK_Handler;
import net.youmi.android.libs.webjs.js.base.handler.JsHandler_abstract_Params_NoPsw_Handler;
import net.youmi.android.libs.webjs.js.model.JsModel_ShortCut;

import org.json.JSONObject;

public class JsHandler_UnInstall_ShortCut extends JsHandler_abstract_Params_NoPsw_Handler {

	@Override
	protected JSONObject toHandler(Interface_SDK_Handler sdkHandler,
	                               Interface_Browser_Handler browserHandler, JSONObject jo) {
		try {

			if (sdkHandler == null) {
				return toSimpleCodeJson(Err_Exception);
			}

			final Context context = sdkHandler.getApplicationContext();

			if (context == null) {
				return toSimpleCodeJson(Err_Exception);
			}

			String url = Basic_JSONUtil.getString(jo, "a", null);

			if (url == null) {
				return toSimpleCodeJson(Err_Params);// 参数错误
			}

			String iconUrl = Basic_JSONUtil.getString(jo, "b", null);

			String wapTitle = Basic_JSONUtil.getString(jo, "c", "网页快捷方式");

			String browserPackageName = null;

			String browserMainActivity = null;

			try {

				JSONObject cnJo = Basic_JSONUtil.getJsonObject(jo, "d",
						null);
				if (cnJo != null) {

					browserPackageName = Basic_JSONUtil.getString(cnJo,
							"a", null);

					if (browserPackageName != null) {
						browserMainActivity = Basic_JSONUtil.getString(
								cnJo, "b", null);
					}

				}

			} catch (Throwable e) {
				Debug_Log.e(e);
			}

			String fn = Basic_JSONUtil.getString(jo, "e", null);
			String receiveCallbackUrl = Basic_JSONUtil.getString(jo, "f", null);

			final JsModel_ShortCut shortCut = new JsModel_ShortCut();
			shortCut.setCallBackFunction(fn);
			shortCut.setComponentPackageName(browserPackageName);
			shortCut.setComponentActivityName(browserMainActivity);
			shortCut.setIconUrl(iconUrl);
			shortCut.setTitle(wapTitle);
			shortCut.setUrl(url);
			shortCut.setReceiveCallBackPageUrl(receiveCallbackUrl);

			if (Util_System_Permission.isWithPermission(context, "com.android.launcher.permission" +
					".UNINSTALL_SHORTCUT")) {
				// 线程处理

				Runnable runnable = new Runnable() {

					@Override
					public void run() {
						try {

							if (shortCut == null) {
								return;
							}
							if (!shortCut.isParamOk()) {
								return;
							}
							Intent shortcutIntent = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");
							shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
									shortCut.getTitle());
							// 获取打开网页的Intent
							Intent intent = Util_System_Intent_Network.getToWebUrlIntent(
									context.getApplicationContext(), shortCut.getComponentPackageName(),
									shortCut.getComponentActivityName(), shortCut.getUrl());
							if (intent == null) {
								return;
							}

							shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);

							context.sendBroadcast(shortcutIntent);

						} catch (Throwable e) {
							Debug_Log.e(e);
						}
					}
				};

				if (browserHandler.js_SDK_Handler_RunOnUIThread(runnable)) {
					return toSimpleCodeJson(OK);
				}

				return toSimpleCodeJson(Err_Exception);
			}


		} catch (Throwable e) {
			Debug_Log.e(e);
		}

		return null;
	}
}
