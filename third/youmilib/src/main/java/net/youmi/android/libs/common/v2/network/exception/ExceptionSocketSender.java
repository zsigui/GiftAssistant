package net.youmi.android.libs.common.v2.network.exception;

import android.content.Context;

import net.youmi.android.libs.common.CommonConstant;
import net.youmi.android.libs.common.basic.Basic_Random;
import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.v2.network.NetworkStatus;

import org.json.JSONObject;

/**
 * 主要用于异常上报
 *
 * @author mosida created on 2013-6-17
 * @author zhitaocai edit on 2014-5-15
 */
public class ExceptionSocketSender {

	/**
	 * 异常上报地址 exrep.youmi.net
	 */
	private final static String monitor_host = CommonConstant.get_Url_ErrorReport();

	/**
	 * 异常上报端口
	 */
	private final static int monitor_port = 5002;

	/**
	 * wifi下异常上报采用tcp
	 */
	private final static String monitor_wifi_type = "tcp";

	/**
	 * wifi下发生请求异常时，%3的概率上报本次错误
	 */
	private final static int monitor_wifi_rate = 3;

	private final static String monitor_4g_type = "tcp";

	private final static int monitor_4g_rate = 3;

	private final static String monitor_3g_type = "tcp";

	private final static int monitor_3g_rate = 3;

	private final static String monitor_2g_type = "tcp";

	private final static int monitor_2g_rate = 3;

	private Context mContext;

	private String mSendType = "udp";

	public ExceptionSocketSender(Context context) {
		mContext = context;
	}

	/**
	 * 是否需要发送记录
	 * <p/>
	 * 不是每个异常都上报，是随机上报的
	 *
	 * @param networkState
	 * @return
	 */
	boolean isNeedToSend() {
		if (Debug_SDK.isNetLog) {
			Debug_SDK.ti(Debug_SDK.mNetTag, ExceptionSocketSender.class, "触发异常上报：判断是否上报本次异常");
		}

		if (Basic_StringUtil.isNullOrEmpty(monitor_host)) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, ExceptionSocketSender.class, "触发异常上报信息：host is null!");
			}
			return false;
		}

		int rdNum = Basic_Random.nextInt(100);

		// 根据不同的网络来判断是否需要异常上报
		switch (NetworkStatus.getNetworkType(mContext)) {
			case NetworkStatus.Type.TYPE_2G:
				mSendType = monitor_2g_type;
				if (rdNum < monitor_2g_rate) {
					return true;
				}
				break;
			case NetworkStatus.Type.TYPE_3G:
				mSendType = monitor_3g_type;
				if (rdNum < monitor_3g_rate) {
					return true;
				}
				break;
			case NetworkStatus.Type.TYPE_4G:
				mSendType = monitor_4g_type;
				if (rdNum < monitor_4g_rate) {
					return true;
				}
				break;
			case NetworkStatus.Type.TYPE_WIFI:
				mSendType = monitor_wifi_type;
				if (rdNum < monitor_wifi_rate) {
					return true;
				}
				break;
		}
		return false;
	}

	public void send(JSONObject jsonObject) {

		if (jsonObject == null) {
			return;
		}
		try {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.ti(Debug_SDK.mNetTag, ExceptionSocketSender.class, "触发异常上报：准备上报");
				Debug_SDK.ti(Debug_SDK.mNetTag, ExceptionSocketSender.class, "触发异常上报：上报方式:%s", mSendType);
			}
			if (mSendType.equals("udp")) {
				UDPSocketSender.send(jsonObject, monitor_host, monitor_port);
			} else if (mSendType.equals("tcp")) {
				TCPSocketSender.send(jsonObject, monitor_host, monitor_port);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, ExceptionSocketSender.class, e);
			}
		}
	}
}
