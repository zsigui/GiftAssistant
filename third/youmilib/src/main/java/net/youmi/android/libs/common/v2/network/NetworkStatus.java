package net.youmi.android.libs.common.v2.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.util.Util_System_Permission;

/**
 * 网络状态相关工具类，后续待需求明确可以扩展
 *
 * @author zhitaocai
 */
public class NetworkStatus {

	/**
	 * 检查当前网络是否可用 1.检查联网权限 2.检查网络状态
	 *
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {

		// Allows applications to open network sockets.
		if (!Util_System_Permission.isWith_INTERNET_Permission(context)) {
			return false;
		}

		// Allows applications to access information about networks
		if (!Util_System_Permission.isWith_ACCESS_NETWORK_STATE_Permission(context)) {
			return true;
		}

		try {
			ConnectivityManager connectivityManager =
					(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
			if (activeNetworkInfo != null) {
				if (activeNetworkInfo.isAvailable()) {
					return true;
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, NetworkStatus.class, e);
			}
		}

		return false;
	}

	/**
	 * 获取当前移动网络APN名字
	 *
	 * @param context
	 * @return
	 */
	public static String getApn(Context context) {
		try {

			if (Util_System_Permission.isWith_ACCESS_NETWORK_STATE_Permission(context)) {
				// 具有网络访问权限时才可以获取apn
				ConnectivityManager connectivityManager =
						(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
				if (activeNetworkInfo != null) {
					if (activeNetworkInfo.isAvailable()) {
						switch (activeNetworkInfo.getType()) {
							case ConnectivityManager.TYPE_WIFI: // wifi网络
								if (Debug_SDK.isNetLog) {
									Debug_SDK.ti(Debug_SDK.mNetTag, NetworkStatus.class, "当前网络为wifi网络");
								}
								return APN.APN_WIFI;
							case ConnectivityManager.TYPE_MOBILE: // 手机网络
								// 判断接入点
								String apn = activeNetworkInfo.getExtraInfo();
								if (apn != null) {
									apn = apn.trim().toLowerCase();
									if (apn.length() > 25) {
										return apn.substring(0, 25);
									} else {
										return apn;
									}
								} else {
									// 未知接入点，返回 APN_UNKNOW
									return APN.APN_UNKNOW;
								}
							default:
								break;
						}
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, NetworkStatus.class, e);
			}
		}
		return APN.APN_UNKNOW;
	}

	/**
	 * 粗略获取当前网络类型
	 *
	 * @param context
	 * @return <ul>
	 * <li><code>{@link net.youmi.android.libs.common.v2.network.NetworkStatus.Type#TYPE_UNKNOWN}</code> 当前网络不可用</li>
	 * <li><code>{@link net.youmi.android.libs.common.v2.network.NetworkStatus.Type#TYPE_2G}</code></li>
	 * <li><code>{@link net.youmi.android.libs.common.v2.network.NetworkStatus.Type#TYPE_3G}</code></li>
	 * <li><code>{@link net.youmi.android.libs.common.v2.network.NetworkStatus.Type#TYPE_4G}</code></li>
	 * <li><code>{@link net.youmi.android.libs.common.v2.network.NetworkStatus.Type#TYPE_WIFI}</code></li>
	 * </ul>
	 */
	public static int getNetworkType(Context context) {
		// 不具有连接网络权限
		if (!Util_System_Permission.isWith_INTERNET_Permission(context)) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.tw(Debug_SDK.mNetTag, NetworkStatus.class, "Please Add \"%s\" To AndroidManifest.xml",
						"android.permission.INTERNET");
			}
		}

		// 1、判断是否允许程序访问有关GSM网络信息
		// 不具有访问GSM网络权限
		if (!Util_System_Permission.isWith_ACCESS_NETWORK_STATE_Permission(context)) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.tw(Debug_SDK.mNetTag, NetworkStatus.class, "Please Add \"%s\" To AndroidManifest.xml",
						"android.permission.ACCESS_NETWORK_STATE");
			}
		}
		// 2、判断是否允许程序访问Wi-Fi网络状态信息,经过测试，不需要检测这个权限也可以获取到网络信息,包括当前是wifi网络也可以判断出来，但是不配置的话就不能使用wifi网络
		// 不具有访问WIFI网络权限
		if (!Util_System_Permission.isWith_ACCESS_WIFI_STATE_Permission(context)) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.tw(Debug_SDK.mNetTag, NetworkStatus.class, "Please Add \"%s\" To AndroidManifest.xml",
						"android.permission.ACCESS_WIFI_STATE");
			}
		}

		try {

			ConnectivityManager connectivityManager =
					(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
			if (activeNetworkInfo != null) {
				// 网络不可用
				if (!activeNetworkInfo.isAvailable()) {
					if (Debug_SDK.isNetLog) {
						Debug_SDK.te(Debug_SDK.mNetTag, NetworkStatus.class, "当前网络不可用");
					}
					return Type.TYPE_UNKNOWN;
				}
				// 网络可用
				else {

					// 判断当前网络类型
					switch (activeNetworkInfo.getType()) {
						case ConnectivityManager.TYPE_WIFI: // wifi网络
							if (Debug_SDK.isNetLog) {
								Debug_SDK.ti(Debug_SDK.mNetTag, NetworkStatus.class, "当前网络为wifi网络");
							}
							return Type.TYPE_WIFI;
						case ConnectivityManager.TYPE_MOBILE: // 手机网络
							if (Debug_SDK.isNetLog) {
								Debug_SDK.ti(Debug_SDK.mNetTag, NetworkStatus.class, "当前网络为手机网络");
							}
							try {
								// 判断手机网络类型
								switch (activeNetworkInfo.getSubtype()) {
									case TelephonyManager.NETWORK_TYPE_CDMA: // 14-64kbps,电信2G网络
									case TelephonyManager.NETWORK_TYPE_IDEN: // 25kbps,
									case TelephonyManager.NETWORK_TYPE_1xRTT: // 50-100kbps,
									case TelephonyManager.NETWORK_TYPE_EDGE: // 50-100kbps,移动2G网络，基于gprs的，2.75代
									case TelephonyManager.NETWORK_TYPE_GPRS: // 100kbps,联通2G网络，基于gsm，2.5代
										if (Debug_SDK.isNetLog) {
											Debug_SDK.ti(Debug_SDK.mNetTag, NetworkStatus.class, "当前网络为2G网络");
										}
										return Type.TYPE_2G;

									case TelephonyManager.NETWORK_TYPE_EVDO_0: // 400-1000kbps,电信3G网络
									case TelephonyManager.NETWORK_TYPE_EVDO_A: // 600-1400kbps,电信3G网络
									case TelephonyManager.NETWORK_TYPE_EVDO_B: // 5Mbps,电信3G网络
									case TelephonyManager.NETWORK_TYPE_UMTS: // 400-7000kbps
									case TelephonyManager.NETWORK_TYPE_HSPA: // 700-1700kbps,WCDMA，应用于R99,R4，
									case TelephonyManager.NETWORK_TYPE_HSDPA: // 2-14Mbps,基于WCDMA联通3G网络（3
									// .5G）——高速下行，一般部署在其他城市，应用于R5
									case TelephonyManager.NETWORK_TYPE_HSUPA: // 1-23Mbps,基于WCDMA联通3G网络（3
									// .5G）——高速上行，一般部署在重要城市，应用于R6
									case TelephonyManager.NETWORK_TYPE_EHRPD: // 1-2Mbps,电信3G网络
									case TelephonyManager.NETWORK_TYPE_HSPAP: // 10-20Mbps,也称HSPA+，目前全球最快的WCDMA商用网络
										if (Debug_SDK.isNetLog) {
											Debug_SDK.ti(Debug_SDK.mNetTag, NetworkStatus.class, "当前网络为3G网络");
										}
										return Type.TYPE_3G;

									case TelephonyManager.NETWORK_TYPE_LTE: // 10+Mbps,3G到4G的一个过渡，准4G网络
										if (Debug_SDK.isNetLog) {
											Debug_SDK.ti(Debug_SDK.mNetTag, NetworkStatus.class, "当前网络为4G网络");
										}
										return Type.TYPE_4G;

									case TelephonyManager.NETWORK_TYPE_UNKNOWN: // 未知网络
										return Type.TYPE_UNKNOWN;
									default:
										if (Debug_SDK.isNetLog) {
											Debug_SDK.ti(Debug_SDK.mNetTag, NetworkStatus.class, "当前网络为3G网络");
										}
										return Type.TYPE_3G;
								}

							} catch (Exception e) {
								if (Debug_SDK.isNetLog) {
									Debug_SDK.te(Debug_SDK.mNetTag, NetworkStatus.class, e);
								}
							}
							return Type.TYPE_3G;
						default:
							break;
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, NetworkStatus.class, e);
			}
		}
		return Type.TYPE_UNKNOWN;

	}

	public static class Type {

		public final static int TYPE_UNKNOWN = -1;

		public final static int TYPE_WIFI = 100;

		public final static int TYPE_2G = 101;

		public final static int TYPE_3G = 102;

		public final static int TYPE_4G = 103;
	}

	public static class APN {

		/**
		 * apn wifi
		 */
		public final static String APN_WIFI = "wifi";

		// -------------------------------------------------------------------------------------
		// 移动apn

		/**
		 * apn cmnet 移动 2G 3G 4G通用
		 */
		public final static String APN_CMNET = "cmnet";

		/**
		 * apn cmwap 移动 2G 3G 4G通用 ，但是好像不适用4G
		 */
		public final static String APN_CMWAP = "cmwap";

		// -------------------------------------------------------------------------------------
		// 联通apn

		/**
		 * apn uninet 联通 2G
		 */
		public final static String APN_UNINET = "uninet";

		/**
		 * apn uniwap 联通 2G
		 */
		public final static String APN_UNIWAP = "uniwap";

		/**
		 * apn 3gnet 联通3G
		 */
		public final static String APN_3GNET = "3gnet";

		/**
		 * apn 3gwap 联通3G
		 */
		public final static String APN_3GWAP = "3gwap";

		/**
		 * 联通4G 现阶段中国联通4G业务APN接入点可以设置为3gnet。以后待数据改造完成且通过验证后，中国联通LTE以及3G定制终端接入点会启用wonet接入点。
		 */
		public final static String APN_WONET = "wonet";

		// -------------------------------------------------------------------------------------
		// 电信apn

		/**
		 * apn #777 ctwap 电信2G
		 */
		public final static String APN_CTWAP = "ctwap";

		/**
		 * apn #777 ctnet 电信3G 理论速率 3.1M
		 */
		public final static String APN_CTNET = "ctnet";

		/**
		 * 电信4G 理论速率100M
		 */
		public final static String APN_CTLTE = "ctlte";

		// -------------------------------------------------------------------------------------

		/**
		 * apn internet
		 */
		public final static String APN_INTERNET = "internet";

		/**
		 * apn unknow
		 */
		public final static String APN_UNKNOW = "";

	}
}
