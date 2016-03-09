package net.ouwan.umipay.android.manager;

import android.content.Context;
import android.os.Handler;

import net.ouwan.umipay.android.Utils.Util_ErrorReport;
import net.ouwan.umipay.android.Utils.Util_Package;
import net.ouwan.umipay.android.api.GameParamInfo;
import net.ouwan.umipay.android.asynctask.CommandTask;
import net.ouwan.umipay.android.config.ConstantString;
import net.ouwan.umipay.android.config.SDKCacheConfig;
import net.ouwan.umipay.android.config.SDKConstantConfig;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.TracerouteContainer;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.common.global.Global_Final_Common_Millisecond;
import net.youmi.android.libs.common.global.Global_Runtime_SystemInfo;
import net.youmi.android.libs.common.network.Util_Network_Status;
import net.youmi.android.libs.platform.coder.Coder_SDKPswCoder;
import net.youmi.android.libs.platform.global.Global_Runtime_ClientId;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by mink on 16-1-12.
 */
public class ErrorReportManager {

	private static ErrorReportManager mInstance = null;

	private static final String SIM_OPERATOR_NAME = "getSimOperatorName";
	private static final String SIM_SERIAL_NUMBER = "getSimSerialNumber";

	private final String ERRORREPORT_SERVER_URL = Coder_SDKPswCoder.decode
			(ConstantString.ERRORREPORT_SERVER_URL, ConstantString.SERVER_URL_KEY);
	private final String ERRORREPORT_DNSPROVIDER = Coder_SDKPswCoder.decode
			(ConstantString.ERRORREPORT_DNSPROVIDER, ConstantString.SERVER_URL_KEY);
	private final String SERVER_HOSTNAME = Coder_SDKPswCoder.decode
			(ConstantString.SERVER_HOSTNAME, ConstantString.SERVER_URL_KEY);
	private final String BAIDU_HOSTNAME = Coder_SDKPswCoder.decode
			(ConstantString.BAIDU_HOSTNAME, ConstantString.SERVER_URL_KEY);

	//traceroute最大跳数为30
	private final int maxTtl = 30;
	//信息搜索超时设置为四分钟
	private final long TIMEOUT = 4 * Global_Final_Common_Millisecond.oneMinute_ms;
	private Runnable runnableTimeout;
	private Handler handlerTimeout;

	private Context mContext;
	private JSONObject mParams;
	private ErrorReportAsyncTask mErrorReportAsyncTask;

	private int errorCode;
	private String errorMsg;
	private long ttl;
	private long timestamp;
	private long startTime;

	private ErrorReportManager(Context context) {
		this.mContext = context;
	}

	public static ErrorReportManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new ErrorReportManager(context);
		}
		return mInstance;
	}


	/**
	 * 网络异常上报接口，一次上报未执行完毕不执行下次操作
	 * @param handler 用于执行延时操作的Handler
	 * @param code 异常返回错误码
	 * @param msg 异常返回错误信息
	 * @param ttl 异常耗时
	 * @param timestamp 时间戳
	 */
	public void report(Handler handler, int code, String msg, long ttl, long timestamp) {
		if(!SDKCacheConfig.getInstance(mContext).isEnableErrorReport()){
			Debug_Log.dd("Disable ErrorReport !");
			return;
		}
		if (mErrorReportAsyncTask == null) {
			initParams(handler, code, msg, ttl, timestamp);
			mErrorReportAsyncTask = new ErrorReportAsyncTask();
			mErrorReportAsyncTask.execute();
		}
	}

	private void initParams(Handler handler, int code, String msg, long ttl, long timestamp) {
		this.handlerTimeout = handler;
		this.errorCode = code;
		this.errorMsg = msg;
		this.ttl = ttl;
		this.timestamp = timestamp;
		this.mParams = new JSONObject();
	}

	/**
	 * 错误上报信息搜集异步类
	 */
	private class ErrorReportAsyncTask extends CommandTask<Void, Void> {
		@Override
		public Void doInBackground(Void... params) {
			try {
				startTime = System.currentTimeMillis();
				// timeout task
				new TimeOutAsyncTask().execute();
				//可选信息
				Global_Runtime_ClientId runtime_cid = new Global_Runtime_ClientId(mContext);
				String imei = runtime_cid.getImei();
				String imsi = runtime_cid.getImsi();
				String cid = runtime_cid.getCid();
				String androidid = Global_Runtime_SystemInfo.getAndroidId(mContext);
				String apn = Util_Network_Status.getApn(mContext);
				String mac = Global_Runtime_SystemInfo.getMac(mContext);
				String sig = Util_Package.getPackageSignature(mContext);

				int chn = Integer.valueOf(GameParamInfo.getInstance(mContext).getChannelId());
				int subchn = Integer.valueOf(GameParamInfo.getInstance(mContext).getSubChannelId());

				Basic_JSONUtil.put(mParams, "appid", GameParamInfo.getInstance(mContext).getAppId());
				Basic_JSONUtil.put(mParams, "umisdkv", SDKConstantConfig.UMIPAY_SDK_VERSION);//sdk版本
				Basic_JSONUtil.put(mParams, "code", errorCode);//返回错误码
				Basic_JSONUtil.put(mParams, "msg", errorMsg);//返回错误信息
				Basic_JSONUtil.put(mParams, "ttl", ttl);//网络错误处理消耗时间
				Basic_JSONUtil.put(mParams, "timestamp", timestamp);//时间戳

				Basic_JSONUtil.put(mParams, "imei", imei);
				Basic_JSONUtil.put(mParams, "imsi", imsi);
				Basic_JSONUtil.put(mParams, "cid", cid);
				Basic_JSONUtil.put(mParams, "androidid", androidid);
				Basic_JSONUtil.put(mParams, "apn", apn);
				Basic_JSONUtil.put(mParams, "mac", mac);
				Basic_JSONUtil.put(mParams, "sig", sig);
				Basic_JSONUtil.put(mParams, "chn", chn);
				Basic_JSONUtil.put(mParams, "subchn", subchn);

				Basic_JSONUtil.put(mParams, "pm", Util_ErrorReport.getDeviceType());//手机型号
				Basic_JSONUtil.put(mParams, "deviceid", Util_ErrorReport.getInfoByName(mContext, "getDeviceId"));//设备ID
				Basic_JSONUtil.put(mParams, "nett", Util_ErrorReport.getNetWorkType(mContext));//网络类型
				Basic_JSONUtil.put(mParams, "neto", Util_ErrorReport.getInfoByName(mContext, "getNetworkOperator"));//网络提供商数字名字
				Basic_JSONUtil.put(mParams, "neton", Util_ErrorReport.getInfoByName(mContext,
						"getNetworkOperatorName"));//网络提供商名字
				Basic_JSONUtil.put(mParams, "localdns", Util_ErrorReport.getDhcpInfoByName(mContext, "dns1"));//获取本地dns域名地址
				Basic_JSONUtil.put(mParams, "localip", Util_ErrorReport.getLocalHostIp());//本地ip

				Basic_JSONUtil.put(mParams, "sysv", Util_ErrorReport.getDeviceOsVersion());//系统版本号
				Basic_JSONUtil.put(mParams, "sdkv", Util_ErrorReport.getSDKVersion());//SDK版本号
				Basic_JSONUtil.put(mParams, "cpuabi", Util_ErrorReport.getCPU_API());//cpu类型
				Basic_JSONUtil.put(mParams, "hardware", Util_ErrorReport.getHardware());//硬件设备名

				Basic_JSONUtil.put(mParams, "sim_on", Util_ErrorReport.getInfoByName(mContext, SIM_OPERATOR_NAME));//SIM卡服务商名字
				Basic_JSONUtil.put(mParams, "sim_num", Util_ErrorReport.getInfoByName(mContext, SIM_SERIAL_NUMBER));//SIM卡编码

				StringBuffer loadpagetime = new StringBuffer();

				loadpagetime.append(Util_ErrorReport.getLoadPageTime(BAIDU_HOSTNAME)).append(";;").append
						(Util_ErrorReport.getLoadPageTime
								(SERVER_HOSTNAME));
				Basic_JSONUtil.put(mParams, "loadpagetime", loadpagetime);//加载指定页面耗时

				StringBuffer lookupresult = new StringBuffer();
				lookupresult.append(Util_ErrorReport.lookup(SERVER_HOSTNAME, null)).append(";;").append
						(Util_ErrorReport.lookup
								(SERVER_HOSTNAME, ERRORREPORT_DNSPROVIDER));

				Basic_JSONUtil.put(mParams, "lookup", lookupresult.toString());//lookup结果

				StringBuffer digresult = new StringBuffer();
				digresult
						.append(Util_ErrorReport.dig(SERVER_HOSTNAME, null)).append(Util_ErrorReport.lookup(";;",
						null)).append(Util_ErrorReport.dig
						(SERVER_HOSTNAME, ERRORREPORT_DNSPROVIDER));

				Basic_JSONUtil.put(mParams, "dig", digresult.toString());//
				Basic_JSONUtil.put(mParams, "pingnet", Util_ErrorReport.pingNet(SERVER_HOSTNAME));//系统ping命令
				StringBuffer sb = new StringBuffer();
				String ipToPing = "";

				//执行ping模拟traceroute
				for (int t = 1; t <= maxTtl && !isCancelled(); t++) {
					TracerouteContainer tr = Util_ErrorReport.launchPing(SERVER_HOSTNAME, ipToPing, t);
					sb.append(tr);
					if (tr.getIsSuccessful()) {
						if (tr.getTTL() == 1) {
							ipToPing = tr.getIpToPing();
						} else if (ipToPing != null && ipToPing.equals(tr.getIp())) {
							t = maxTtl;
						}
					}
				}
				Basic_JSONUtil.put(mParams, "traceroute", sb.toString());//traceroute结果
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
			return null;
		}

		@Override
		public void onPostExecute(Void result) {
			//未超时直接上报错误信息
			if (!isCancelled()) {
				Basic_JSONUtil.put(mParams, "timeout", "false");//是否timeout
				new UploadErrprReportAsyncTask().execute();
			}
		}

		@Override
		protected void onCancelled() {
			//超时，取消当前正在执行的相关消息搜集操作，直接上报
			Basic_JSONUtil.put(mParams, "timeout", "true");//是否timeout
			new UploadErrprReportAsyncTask().execute();
		}
	}

	/**
	 * 错误信息上报异步类
	 */
	private class UploadErrprReportAsyncTask extends CommandTask<Void, Void> {
		@Override
		public Void doInBackground(Void... params) {
			if (mParams == null) {
				return null;
			}
			// TODO REPORT
			try {
				Basic_JSONUtil.put(mParams, "elapsedtime", System.currentTimeMillis() - startTime + " ms");//总消耗时间
				HttpResponse response = null;
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(ERRORREPORT_SERVER_URL);
				StringEntity entity = new StringEntity(mParams.toString());
				entity.setContentType("application/json");
				entity.setContentEncoding("UTF-8");
				post.setEntity(entity);

				response = client.execute(post);

				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					HttpEntity result_entity = response.getEntity();
					String result_str = EntityUtils.toString(result_entity);
					if ("SUCCESS".equals(result_str)) {
						Debug_Log.dd("ErrorReport Success");
					}
				}
			} catch (IOException e) {
				Debug_Log.e(e);
			}
			return null;
		}

		@Override
		public void onPostExecute(Void result) {
			mErrorReportAsyncTask = null;
			if (handlerTimeout != null && runnableTimeout != null) {
				handlerTimeout.removeCallbacks(runnableTimeout);
			}
		}
	}

	/**
	 * 超时处理异步类
	 */
	private class TimeOutAsyncTask extends CommandTask<Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				if (handlerTimeout == null) {
					handlerTimeout = new Handler(mContext.getMainLooper());
				}
				// stop old timeout
				if (runnableTimeout != null) {
					handlerTimeout.removeCallbacks(runnableTimeout);
				}
				// define timeout
				runnableTimeout = new Runnable() {
					@Override
					public void run() {
						try {
							if (mErrorReportAsyncTask != null && !mErrorReportAsyncTask.isCancelled()) {
								mErrorReportAsyncTask.cancel(true);
							}
						} catch (Throwable e) {
							Debug_Log.e(e);
						}
					}
				};
				// launch timeout after a delay
				handlerTimeout.postDelayed(runnableTimeout, TIMEOUT);
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
		}
	}
}
