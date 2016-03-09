package net.ouwan.umipay.android.Utils;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.TracerouteContainer;
import net.youmi.android.libs.common.network.Util_Network_Status;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据上报工具
 * 用于网络异常时获取网络状态等相关信息
 * Created by mink on 16-02-26.
 *
 * @author mink
 */

public class Util_ErrorReport {

	private static final String PING = "PING";
	private static final String FROM_PING = "From";
	private static final String SMALL_FROM_PING = "from";
	private static final String PARENTHESE_OPEN_PING = "(";
	private static final String PARENTHESE_CLOSE_PING = ")";
	private static final String TIME_PING = "time=";
	private static final String EXCEED_PING = "exceed";
	private static final String UNREACHABLE_PING = "100%";



	public static String getCPU_API() {
		return Build.CPU_ABI;
	}

	public static String getDeviceOsVersion() {
		return Build.VERSION.RELEASE;
	}

	public static String getDeviceType() {
		return Build.MODEL;
	}

	public static String getHardware() {
		return Build.HARDWARE;
	}

	/**
	 * 获取设备ID
	 * @param paramContext
	 * @param paramString
	 * @return
	 */
	public static String getInfoByName(Context paramContext, String paramString) {
		TelephonyManager localTelephonyManager = (TelephonyManager) paramContext.getSystemService("phone");
		if (localTelephonyManager != null) ;
		try {
			Object localObject = localTelephonyManager.getClass().getDeclaredMethod(paramString, new Class[0]).invoke
					(localTelephonyManager, new Object[0]);
			if ((localObject instanceof String))
				return (String) localObject;
			String str = String.valueOf(localObject);
			return str;
		} catch (NoSuchMethodException localNoSuchMethodException) {
			localNoSuchMethodException.printStackTrace();
			return null;
		} catch (InvocationTargetException localInvocationTargetException) {
			while (true)
				localInvocationTargetException.printStackTrace();
		} catch (IllegalAccessException localIllegalAccessException) {
			while (true)
				localIllegalAccessException.printStackTrace();
		}
	}

	public static int getSDKVersion() {
		return Build.VERSION.SDK_INT;
	}

	/**
	 * 获得网络类型
	 * @param context
	 * @return
	 */
	public static String getNetWorkType(Context context) {
		StringBuffer type = new StringBuffer();
		try {
			switch (Util_Network_Status.getNetworkType(context)) {
				case Util_Network_Status.TYPE_2G:
					type.append("2G");
					break;
				case Util_Network_Status.TYPE_3G:
					type.append("3G");
					break;
				case Util_Network_Status.TYPE_4G:
					type.append("4G");
					break;
				case Util_Network_Status.TYPE_WIFI:
					type.append("WIFI");
					break;
				default:
					type.append("UNKNOWN");
			}

		} catch (Throwable e) {
			Debug_Log.e(e);
		}

		return type.toString();
	}

	/**
	 * 获取本机IP地址
	 *
	 * @return
	 */
	public static String getLocalHostIp() {
		try {
			for (Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces(); nis.hasMoreElements();
					) {

				NetworkInterface netInf = nis.nextElement();
				for (Enumeration<InetAddress> ipAddr = netInf.getInetAddresses(); ipAddr.hasMoreElements(); ) {
					InetAddress inetAddress = ipAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress
							())) {
						return inetAddress.getHostAddress();
					}
				}
			}
		} catch (SocketException ex) {

		}
		return null;
	}

	/**
	 * 使用系统ping命令，并返回前5条数据，ping超时时间为100ms
	 * 通过抛出异常，允许超时AyncTask调用cancel方法后直接结束后续的错误信息搜集
	 * @param url
	 * @return
	 */
	public static String pingNet(String url) throws IOException {
		StringBuilder result = new StringBuilder();
		result.append("#ping "+url+" : ");
			String cmd = "/system/bin/ping -c 5 -w 100 " + url;
			Process p = null;
			String line;
			p = Runtime.getRuntime().exec(cmd + url);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			// Construct the response from ping
			while ((line = stdInput.readLine()) != null) {
				result.append(line);
			}
			p.destroy();
			return replaceBlank(result.toString());
	}
	/**
	 * Launches ping command
	 * 通过抛出异常，允许超时AyncTask调用cancel方法后直接结束后续的错误信息搜集
	 * @param url
	 *            The url to ping
	 * @return The ping string
	 */
	public static TracerouteContainer launchPing(String url,String ipToPing,int ttl) throws IOException {
		// Build ping command with parameters
		Process p;
		String command = "";
		String s;
		String res = "";
		String mIpToPing = "";
		float elapsedTime=0;
		String format = "ping -c 1 -t %d ";

		mIpToPing = ipToPing;
		command = String.format(format, ttl);

		long startTime = System.nanoTime();
		// Launch command
			p = Runtime.getRuntime().exec(command + url);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

			// Construct the response from ping
			while ((s = stdInput.readLine()) != null) {
				res += s + "\n";
				if (s.contains(FROM_PING) || s.contains(SMALL_FROM_PING)) {
					// We store the elapsedTime when the line from ping comes
					elapsedTime = (System.nanoTime() - startTime) / 1000000.0f;
				}
			}
			p.destroy();

		if (!TextUtils.isEmpty(res)){
			String ip = parseIpFromPing(res);
			mIpToPing = (ttl==1)?parseIpToPingFromPing(res):mIpToPing;
			InetAddress inetAddr = null;
			String hostname = null;
			try {
				inetAddr = InetAddress.getByName(ip);
				hostname = inetAddr.getHostName();
			} catch (UnknownHostException e) {
				Debug_Log.e(e);
			}
			boolean isSuccessful = !res.contains(UNREACHABLE_PING) || res.contains(EXCEED_PING);
			return new TracerouteContainer(hostname,mIpToPing,ip,elapsedTime,isSuccessful,ttl);
		}
		return new TracerouteContainer("","","",0,false,ttl);
	}
	/**
	 * 计算加载某个页面所需的事件，单位：ms
	 * 若加载失败，则返回-1，表示连接错误
	 *
	 * @param url
	 */
	public static String  getLoadPageTime(String url) {
		StringBuffer result = new StringBuffer();
		result.append("load "+url+" ");
		long t = -1;
		long startTime = System.currentTimeMillis();
		String content = doGet(url, "");
		if (content != null && !content.equals("")) {
			t = System.currentTimeMillis() - startTime;
		}
		result.append(t).append(" ms");
		return result.toString();
	}

	/**
	 * WIFI连接下，根据域名获取动态IP信息
	 *
	 * @param context
	 * @param name
	 * @return
	 */
	public static String getDhcpInfoByName(Context context, String name) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager != null) {
			DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
			try {
				Field filed = dhcpInfo.getClass().getDeclaredField(name);
				Integer info = filed.getInt(dhcpInfo);
				return int2IpStr(info);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 将整形转换为点十六进制表示法
	 *
	 * @param iInfo
	 * @return
	 */
	public static String int2IpStr(int iInfo) {
		return (iInfo & 0xFF) + "." + (iInfo >> 8 & 0xFF) + "." + (iInfo >> 16 & 0xFF) + "." + (iInfo >> 24 & 0xFF);
	}


	/**
	 * 字节数组转换为十六进制字符串
	 *
	 * @param bs
	 * @return
	 */
	public static String byte2hex(byte... bs) {
		char[] hex = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
		StringBuilder sb = new StringBuilder();
		for (int n = 0; n < bs.length; n++) {
			sb.append(hex[bs[n] >> 4 & 0xF]);
			sb.append(hex[bs[n] & 0xF]);
		}
		return sb.toString();
	}

	/**
	 * 根据输入的URL和参数执行httpGet请求，并返回获取到的相应字符串
	 *
	 * @param requestUrl
	 * @param params
	 * @return
	 */
	public static String doGet(String requestUrl, String params) {
		String realUrl = requestUrl;
		if (!realUrl.startsWith("http://")) {
			realUrl = "http://" + realUrl;
		}
		if (params != null && !params.equals("")) {
			realUrl += "?" + params;
		}
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(realUrl);
		try {
			HttpResponse response = client.execute(httpGet);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();
				return EntityUtils.toString(entity);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}



	/**
	 * 使用特定的Dns来解析域名获取IP地址，其操作类似Lookup
	 * 相比于dig，获取的信息内容比较少
	 *
	 * @param name     要用于解析出IP地址的域名
	 * @param DnsProvider Dns域名服务器解析地址
	 * @return
	 */
	public static String lookup(String name, String DnsProvider) {
		StringBuilder result = new StringBuilder();
		result.append("#Lookup " + name + "'s ARecord by " + DnsProvider + " : ");
		try {
			Lookup lookup = new Lookup(name, Type.A);
			if (DnsProvider != null && !DnsProvider.equals("")) {
				lookup.setResolver(new SimpleResolver(DnsProvider));
			}
			else {
				lookup.setResolver(new SimpleResolver());
			}
			lookup.run();

			// 结果输出组合字符串
			if (lookup.getResult() == Lookup.SUCCESSFUL) {
				Name[] aliases = lookup.getAliases();
				if (aliases.length > 0) {
					result.append("# aliases:");
					for (int i = 0; i < aliases.length; i++) {
						result.append(aliases[i]);
						if (i < aliases.length - 1) {
							result.append(";");
						}
					}
				}
				Record[] answers = lookup.getAnswers();
				result.append("# answers:");
				for (int i = 0; i < answers.length; i++) {
					result.append(answers[i]);
					if (i < answers.length - 1) {
						result.append(";");
					}
				}

			} else {
				result.append("#Lookup fail, errorstring:");
				result.append(lookup.getErrorString());
			}

		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return replaceBlank(result.toString());
	}


	/**
	 * 根据指定DNS域名解析服务器，使用dig命令获取指定
	 * 记录类型和内部包类型
	 *
	 * @param dnsName
	 * @param provider
	 * @return
	 */
	public static String dig(String dnsName, String provider) {
		StringBuilder result = new StringBuilder();
		int type = Type.A;
		int dClass = DClass.IN;
		result.append("#dig "+dnsName+"'s ARecord and IN by "+provider+": ");
		try {
			Resolver res = null;
			if (provider != null && !provider.equals("")) {
				res = new SimpleResolver(provider);
			} else {
				res = new SimpleResolver();
			}

			// 模拟执行dig
			Record rec = Record.newRecord(Name.fromString(dnsName, Name.root), type, dClass);
			Message query = Message.newQuery(rec);
			long startTime = System.currentTimeMillis();
			Message response = res.send(query);
			long endTime = System.currentTimeMillis();

			// 组合输出字符串
			result.append(Util_ErrorReport.replaceBlank(response.toString()));
			result.append(";; Query time: ").append(endTime - startTime).append(" ms");
		} catch (Throwable e) {
			Debug_Log.e(e);
		}

		return result.toString();
	}

	/**
	 * 用空格替换字符串中的制表符、换行符
	 * @param str
	 * @return
	 */
	public static String replaceBlank(String str) {
		String dest = "";
		if (str!=null) {
			Pattern p = Pattern.compile("\t|\r|\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll(" ");
		}
		return dest;
	}

	/**
	 * Gets the ip from the string returned by a ping
	 *
	 * @param ping
	 *            The string returned by a ping command
	 * @return The ip contained in the ping
	 */
	private static String parseIpFromPing(String ping) {
		String ip = "";
		if (ping.contains(FROM_PING)) {
			// Get ip when ttl exceeded
			int index = ping.indexOf(FROM_PING);

			ip = ping.substring(index + 5);
			if (ip.contains(PARENTHESE_OPEN_PING)) {
				// Get ip when in parenthese
				int indexOpen = ip.indexOf(PARENTHESE_OPEN_PING);
				int indexClose = ip.indexOf(PARENTHESE_CLOSE_PING);

				ip = ip.substring(indexOpen + 1, indexClose);
			} else {
				// Get ip when after from
				ip = ip.substring(0, ip.indexOf("\n"));
				if (ip.contains(":")) {
					index = ip.indexOf(":");
				} else {
					index = ip.indexOf(" ");
				}

				ip = ip.substring(0, index);
			}
		} else {
			// Get ip when ping succeeded
			int indexOpen = ping.indexOf(PARENTHESE_OPEN_PING);
			int indexClose = ping.indexOf(PARENTHESE_CLOSE_PING);

			ip = ping.substring(indexOpen + 1, indexClose);
		}

		return ip;
	}

	/**
	 * Gets the final ip we want to ping (example: if user fullfilled google.fr, final ip could be 8.8.8.8)
	 *
	 * @param ping
	 *            The string returned by a ping command
	 * @return The ip contained in the ping
	 */
	private static String parseIpToPingFromPing(String ping) {
		String ip = "";
		if (ping.contains(PING)) {
			// Get ip when ping succeeded
			int indexOpen = ping.indexOf(PARENTHESE_OPEN_PING);
			int indexClose = ping.indexOf(PARENTHESE_CLOSE_PING);

			ip = ping.substring(indexOpen + 1, indexClose);
		}

		return ip;
	}

	/**
	 * Gets the time from ping command (if there is)
	 *
	 * @param ping
	 *            The string returned by a ping command
	 * @return The time contained in the ping
	 */
	private static String parseTimeFromPing(String ping) {
		String time = "";
		if (ping.contains(TIME_PING)) {
			int index = ping.indexOf(TIME_PING);

			time = ping.substring(index + 5);
			index = time.indexOf(" ");
			time = time.substring(0, index);
		}

		return time;
	}
}
