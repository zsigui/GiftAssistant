package net.ouwan.umipay.android.global;

import android.content.Context;
import android.util.Log;

import net.ouwan.umipay.android.api.GameParamInfo;
import net.ouwan.umipay.android.api.GameUserInfo;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.ouwan.umipay.android.manager.UmipayAccountManager;
import net.youmi.android.libs.common.global.Global_Runtime_SystemInfo;
import net.youmi.android.libs.platform.global.Global_Runtime_ClientId;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Global_Url_Params
 *
 * @author zacklpx
 *         date 15-3-18
 *         description
 */
public class Global_Url_Params {
	public static List<NameValuePair> getDefaultRequestParams(Context context, String url) {
		List<NameValuePair> paramsList = new ArrayList<NameValuePair>();
		try {
			UmipayAccount account = UmipayAccountManager.getInstance(context).getCurrentAccount();
			GameUserInfo userInfo = null;
			if (account != null) {
				userInfo = account.getGameUserInfo();
			}
			String appkey = null;
			String channel = null;
			String childChannel = null;
			String openid = null;
			String session = null;
			Global_Runtime_ClientId clientId = new Global_Runtime_ClientId(context);
			String imei = clientId.getImei();
			String imsi = clientId.getImsi();
			String cid = clientId.getCid();
			String androidid = Global_Runtime_SystemInfo.getAndroidId(context);

			GameParamInfo gameParamInfo = GameParamInfo.getInstance(context);
			if (gameParamInfo != null) {
				appkey = gameParamInfo.getAppId();
				channel = gameParamInfo.getChannelId();
				childChannel = gameParamInfo.getSubChannelId();
			}
			if (userInfo != null) {
				openid = userInfo.getOpenId();
				session = account.getSession();
			}
			paramsList.add(new BasicNameValuePair("appkey", appkey));
			paramsList.add(new BasicNameValuePair("chnid", channel));
			paramsList.add(new BasicNameValuePair("subchnid", childChannel));

			paramsList.add(new BasicNameValuePair("url", url));

			paramsList.add(new BasicNameValuePair("openid", openid));
			paramsList.add(new BasicNameValuePair("sid", session));

			paramsList.add(new BasicNameValuePair("imei", imei));
			paramsList.add(new BasicNameValuePair("imsi", imsi));
			paramsList.add(new BasicNameValuePair("cid", cid));
			paramsList.add(new BasicNameValuePair("andid", androidid));
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return paramsList;
	}
}
