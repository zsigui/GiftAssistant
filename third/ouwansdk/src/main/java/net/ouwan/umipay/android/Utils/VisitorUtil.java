package net.ouwan.umipay.android.Utils;

import android.content.Context;

import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.Visitor;
import net.youmi.android.libs.common.coder.Coder_Md5;
import net.youmi.android.libs.common.global.Global_Runtime_SystemInfo;
import net.youmi.android.libs.platform.global.Global_Runtime_ClientId;


/**
 * Created by liangpeixing on 14-3-14.
 */
public class VisitorUtil {
	final private static String VISITOR_NAME_PSW = "0xad58d3";
	private Context mContext;

	public VisitorUtil(Context context) {
		mContext = context;
	}

	public Visitor getVisitorAccount() {
		String account = generateVisitorOpenid();
		String token = generateVisitorToken();
		return new Visitor(account, token);

	}

	/**
	 * 产生试玩账号名，硬件信息唯一
	 */
	private String generateVisitorOpenid() {
		String visitorName = "";
		try {
			Global_Runtime_ClientId runtime_cid = new Global_Runtime_ClientId(mContext);
			String imei = runtime_cid.getImei();
			String androidid = Global_Runtime_SystemInfo.getAndroidId(mContext);
			String source = imei + androidid + VISITOR_NAME_PSW;
			visitorName = Coder_Md5.md5(source).substring(0, 10);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return visitorName;
	}

	private String generateVisitorToken() {
		String visitorToken = "";
		try {
			Global_Runtime_ClientId runtime_cid = new Global_Runtime_ClientId(mContext);
			String imei = runtime_cid.getImei();
			String androidid = Global_Runtime_SystemInfo.getAndroidId(mContext);
			String source = androidid + imei + VISITOR_NAME_PSW;
			visitorToken = Coder_Md5.md5(source).substring(1, 15);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return visitorToken;
	}
}
