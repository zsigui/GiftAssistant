package net.ouwan.umipay.android.asynctask;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import net.ouwan.umipay.android.Utils.Util_Loadlib;
import net.ouwan.umipay.android.Utils.Util_Package;
import net.ouwan.umipay.android.api.GameParamInfo;
import net.ouwan.umipay.android.api.GameRolerInfo;
import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.config.ConstantString;
import net.ouwan.umipay.android.config.SDKConstantConfig;
import net.ouwan.umipay.android.config.SDKDebugConfig;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.ouwan.umipay.android.manager.UmipayAccountManager;
import net.ymfx.android.d.aa;
import net.ymfx.android.d.ab;
import net.ymfx.android.d.bb;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.common.global.Global_Runtime_SystemInfo;
import net.youmi.android.libs.common.network.Net_Client_Exception;
import net.youmi.android.libs.common.network.Net_HttpMonitor;
import net.youmi.android.libs.common.network.Net_HttpRequester;
import net.youmi.android.libs.platform.coder.Coder_SDKPswCoder;
import net.youmi.android.libs.platform.global.Global_DeveloperConfig;
import net.youmi.android.libs.platform.global.Global_Runtime_ClientId;
import net.youmi.android.libs.platform.network.Net_HttpByteArrayRequestExecutor;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.ParseException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * UmipayCommandTask
 *
 * @author zacklpx
 *         date 15-4-9
 *         description
 */
public abstract class UmipayCommandTask extends CommandTask<Void, CommandResponse> {

	private final static String CHARSET = "UTF-8";

	private Context mContext;
	private JSONObject mParams;
	private CommandResponseListener mListener;
	private int mCmd;
	private String mAppkey;
	private String mAppsecret;
	private String mSession;
	private String mServerUrl;
	private int mUid;
	private int mSdkVer;
	private int mPlatform;
	private Bundle[] mExtResponse;

	public UmipayCommandTask(Context context, int cmd, CommandResponseListener listener, Bundle... extResponse) {
		this.mContext = context;
		this.mCmd = cmd;
		this.mListener = listener;
		this.mServerUrl = GameParamInfo.getInstance(context).isTestMode() ? Coder_SDKPswCoder.decode
				(ConstantString.TEST_SERVER_URL, ConstantString.SERVER_URL_KEY) : Coder_SDKPswCoder.decode
				(ConstantString
				.SERVER_URL, ConstantString.SERVER_URL_KEY);
		mExtResponse = extResponse;
	}

	@Override
	protected CommandResponse doInBackground(Void... params) {
		CommandResponse response = new CommandResponse();
		response.setCmd(mCmd);
		if (!isCancelled() && !init()) {
			response.setCode(UmipaySDKStatusCode.ERR_10_MP_ERR_PARAM);
			return response;
		}
		if (!isCancelled()) {
			initCommonParams();
		}
		if (!isCancelled()) {
			addSpecificParams(mParams);
		}
		if (!isCancelled()) {
			getResponse(response);
		}
		return response;
	}

	@Override
	protected void onPostExecute(CommandResponse response) {
		mListener.onResponse(response, mExtResponse);
	}

	@Override
	protected void onCancelled() {
		mListener.onResponse(new CommandResponse(UmipaySDKStatusCode.CANCEL, null, null, mCmd), mExtResponse);
	}

	private boolean init() {
		mAppkey = Global_DeveloperConfig.getAppID(mContext);
		mAppsecret = Global_DeveloperConfig.getAppSecret(mContext);
		mSession = "";
		mUid = 0;
		UmipayAccount account = UmipayAccountManager.getInstance(mContext).getCurrentAccount();
		if (account != null) {
			mSession = account.getSession();
			mUid = account.getUid();
		}
		mSdkVer = SDKConstantConfig.UMIPAY_SDK_VERSION;
		mPlatform = SDKConstantConfig.SDK_PLATFORM_ANDROID;
		return !(TextUtils.isEmpty(mAppkey) || TextUtils.isEmpty(mAppsecret));
	}

	/**
	 * 初始化通用参数
	 */
	private void initCommonParams() {
		mParams = new JSONObject();
		try {
			String imei = Global_Runtime_SystemInfo.getImei(mContext);
			String imsi = Global_Runtime_SystemInfo.getImsi(mContext);
			String cid = new Global_Runtime_ClientId(mContext).getCid();
			String mac = Global_Runtime_SystemInfo.getMac(mContext);
			String sig = Util_Package.getPackageSignature(mContext);
			int chn = Integer.valueOf(GameParamInfo.getInstance(mContext).getChannelId());
			int subchn = Integer.valueOf(GameParamInfo.getInstance(mContext).getSubChannelId());
			GameRolerInfo rolerInfo = GameRolerInfo.getCurrentGameRolerInfo();
			String gsid = "";
			if (rolerInfo != null && TextUtils.isEmpty(rolerInfo.getServerId())) {
				gsid = rolerInfo.getServerId();
			}
			Basic_JSONUtil.putString(mParams, "imei", imei);
			Basic_JSONUtil.putString(mParams, "imsi", imsi);
			Basic_JSONUtil.putString(mParams, "cid", cid);
			Basic_JSONUtil.putString(mParams, "mac", mac);
			Basic_JSONUtil.putString(mParams, "sig", sig);
			Basic_JSONUtil.putInt(mParams, "chn", chn);
			Basic_JSONUtil.putInt(mParams, "subchn", subchn);
			Basic_JSONUtil.putString(mParams, "gsid", gsid);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	private void getResponse(final CommandResponse response) {
		try {
			if (mParams == null || mParams.length() == 0) {
				response.setCode(UmipaySDKStatusCode.ERR_11_ERR_PACK);
				return;
			}
			//封装消息
			bb pack2send = null;
			boolean isUnsatisfiedLinkError = false;
			try {
				pack2send = ab.p(mUid, mSdkVer, mPlatform, mAppkey, mAppsecret, mSession, mParams.toString(), mCmd);
			} catch (UnsatisfiedLinkError e) {
				isUnsatisfiedLinkError = true;
			}
			if (isUnsatisfiedLinkError) {
				String libName = Coder_SDKPswCoder.decode(ConstantString.SO_LIB_NAME_YMFX, ConstantString
						.SO_LIB_NAME_KEY);
                if(!Util_Loadlib.loadlib(mContext, libName)){
                    //加载失败
                    response.setCode(UmipaySDKStatusCode.ERR_NO_SOLIB);
                    return;
                }
				try {
					pack2send = ab.p(mUid, mSdkVer, mPlatform, mAppkey, mAppsecret, mSession, mParams.toString(), mCmd);

				}catch (Throwable e) {
					response.setCode(UmipaySDKStatusCode.ERR_WRONG_SOLIB);
					return;
				}
			}
			//封装失败
			if (pack2send == null) {
				response.setCode(UmipaySDKStatusCode.ERR_12_ERR_ENCODE);
				return;
			}
			if (pack2send.getA() != UmipaySDKStatusCode.SUCCESS) {
				response.setCode(UmipaySDKStatusCode.ERR_12_ERR_ENCODE);
				response.setMsg(pack2send.getA() + "-" + pack2send.getC());
				return;
			}
			Net_HttpRequester requester = new Net_HttpRequester();
			requester.setEncodingCharset(CHARSET);
			requester.setRequestUrl(mServerUrl);
			requester.setShowReqMsgInLog(!SDKDebugConfig.isRelease);
			requester.setPostDataByteArray(pack2send.getB());
			requester.setRequestHeaders(new ArrayList<Header>());

			Header header = new Header() {
				@Override
				public String getName() {
					return "APPID";
				}

				@Override
				public String getValue() {
					return mAppkey;
				}

				@Override
				public HeaderElement[] getElements() throws ParseException {
					return new HeaderElement[0];
				}
			};
			requester.getRequestHeaders().add(header);
			Net_HttpByteArrayRequestExecutor httpExecutor = new Net_HttpByteArrayRequestExecutor(mContext, requester);
			Net_HttpMonitor monitor = new Net_HttpMonitor();
			httpExecutor.setMonitor(monitor);
			httpExecutor.execute();
			//Http请求结果
			byte[] rsp = httpExecutor.getResult();
			//触发自动解析DNS逻辑，能解决本地DNS解析有误问题
			if (rsp == null) {
				Debug_Log.d(requester.getHostIp());
				requester.getByName();
				httpExecutor.setHttpRequester(requester);
				httpExecutor.execute();
				rsp = httpExecutor.getResult();
			}
			//还是获取不到response，只能报错了
			if (rsp == null) {
				int errorCode = UmipaySDKStatusCode.ERR_15_MP_ERR_NETWORK;
				String errorMsg = "";
				switch (monitor.getClientException()) {
					case Net_Client_Exception.NoException:
						errorMsg = monitor.getHttpCode() + " " + monitor.getHttpReasonPhrase();
						break;
					case Net_Client_Exception.ConnectionPoolTimeoutException:
						errorMsg = "ConnectionPoolTimeoutException";
						break;
					case Net_Client_Exception.HttpHostConnectException:
						errorMsg = "HttpHostConnectException";
						break;
					case Net_Client_Exception.SocketTimeoutException:
						errorMsg = "SocketTimeoutException";
						break;
					case Net_Client_Exception.ConnectTimeoutException:
						errorMsg = "ConnectTimeoutException";
						break;
					case Net_Client_Exception.UnknownHostException:
						errorMsg = "UnknownHostException";
						break;
				}
				response.setCode(errorCode);
				response.setMsg(errorMsg);
				return;

			}

			//解包消息
			aa rspUnpack = ab.up(mUid, mSdkVer, mPlatform, mAppkey, mAppsecret, mSession, rsp, mCmd);
			//解包失败
			if (rspUnpack == null) {
				response.setCode(UmipaySDKStatusCode.ERR_13_ERR_DECODE);
				return;
			}
			response.setCode(rspUnpack.getA());
			response.setResult(Basic_JSONUtil.toJsonObject(rspUnpack.getB()));
		} catch (Throwable e) {
			Debug_Log.e(e);
			response.setCode(UmipaySDKStatusCode.ERR_DEFAULT);
			response.setMsg(e.getMessage());
		}
	}

	public abstract void addSpecificParams(final JSONObject params);

}
