package net.ouwan.umipay.android.handler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import net.ouwan.umipay.android.api.UmipayService;
import net.ouwan.umipay.android.entry.PushInfo;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.webjs.js.base.handler.Interface_Browser_Handler;
import net.youmi.android.libs.webjs.js.base.handler.Interface_SDK_Handler;
import net.youmi.android.libs.webjs.js.base.handler.JsHandler_abstract_Params_NoPsw_Handler;

import org.json.JSONObject;

public class JsHandler_Push_Notification extends JsHandler_abstract_Params_NoPsw_Handler {

	@Override
	protected JSONObject toHandler(Interface_SDK_Handler sdkHandler,
	                               Interface_Browser_Handler browserHandler, JSONObject params) {
		// TODO Auto-generated method stub

		try {
			if (sdkHandler == null) {
				return toSimpleCodeJson(Err_Exception);
			}
			Context context = sdkHandler.getApplicationContext();
			if (context == null) {
				return toSimpleCodeJson(Err_Exception);
			}
			if (browserHandler == null) {
				return toSimpleCodeJson(Err_Exception);
			}
			if (params != null) {

				int id = Basic_JSONUtil.getInt(params, "a", -1);
				int type = Basic_JSONUtil.getInt(params, "b", -1);
				String iconUrl = Basic_JSONUtil.getString(params, "c", null);
				String title = Basic_JSONUtil.getString(params, "d", null);
				String content = Basic_JSONUtil.getString(params, "e", null);
				String uri = Basic_JSONUtil.getString(params, "f", null);
				long showtime = Basic_JSONUtil.getLong(params, "g", 0);

				if (id == -1 || type == -1 || TextUtils.isEmpty(iconUrl) || TextUtils.isEmpty(title) || TextUtils
						.isEmpty(content) || TextUtils.isEmpty(uri) || showtime == 0) {
					return toSimpleCodeJson(Err_Params);
				}

				PushInfo pushInfo = new PushInfo();
				pushInfo.setId(id);
				pushInfo.setType(type);
				pushInfo.setIconUrl(iconUrl);
				pushInfo.setTitle(title);
				pushInfo.setContent(content);
				pushInfo.setUri(uri);
				pushInfo.setShowTime_ms(showtime);

				showNotify(context, pushInfo);
				//不管读还是写都返回该类型的所有已读id
				return toSimpleCodeJson(OK);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return toSimpleCodeJson(Err_Exception);
	}

	void showNotify(Context context, PushInfo pushInfo) {

		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent alarmIntent = new Intent(context, UmipayService.class);
		alarmIntent.putExtra("action", UmipayService.ACTION_JS_NOTIFICATION);
		alarmIntent.putExtra("pushinfo", pushInfo);
		PendingIntent pIntent = PendingIntent.getService(context, pushInfo.getId(),
				alarmIntent, 0);
		alarmManager.cancel(pIntent);
		alarmManager.set(AlarmManager.RTC_WAKEUP, pushInfo.getShowTime_ms(), pIntent);
	}

}
