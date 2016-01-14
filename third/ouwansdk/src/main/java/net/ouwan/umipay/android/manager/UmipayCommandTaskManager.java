package net.ouwan.umipay.android.manager;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import net.ouwan.umipay.android.api.GameRolerInfo;
import net.ouwan.umipay.android.asynctask.CommandResponse;
import net.ouwan.umipay.android.asynctask.CommandResponseListener;
import net.ouwan.umipay.android.asynctask.CommandTask;
import net.ouwan.umipay.android.asynctask.TaskCMD;
import net.ouwan.umipay.android.asynctask.UmipayCommandTask;
import net.ouwan.umipay.android.asynctask.parser.CommonRspParser;
import net.ouwan.umipay.android.asynctask.parser.RspParser_Cmd_DeleteAccount;
import net.ouwan.umipay.android.asynctask.parser.RspParser_Cmd_GetAccount;
import net.ouwan.umipay.android.asynctask.parser.RspParser_Cmd_GetPush;
import net.ouwan.umipay.android.asynctask.parser.RspParser_Cmd_Init;
import net.ouwan.umipay.android.asynctask.parser.RspParser_Cmd_Login;
import net.ouwan.umipay.android.asynctask.parser.RspParser_Cmd_OauthLogin;
import net.ouwan.umipay.android.asynctask.parser.RspParser_Cmd_PushGameInfo;
import net.ouwan.umipay.android.asynctask.parser.RspParser_Cmd_QuickRegist;
import net.ouwan.umipay.android.asynctask.parser.RspParser_Cmd_RedPoint;
import net.ouwan.umipay.android.asynctask.parser.RspParser_Cmd_Regist;
import net.ouwan.umipay.android.asynctask.parser.RspParser_Cmd_VerificateSMS;
import net.ouwan.umipay.android.config.SDKConstantConfig;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.common.coder.Coder_Md5;
import net.youmi.android.libs.common.global.Global_Runtime_SystemInfo;
import net.youmi.android.libs.common.network.Util_Network_Status;
import net.youmi.android.libs.common.util.Util_System_Display_DisplayInfo;
import net.youmi.android.libs.common.util.Util_System_SDCard_Util;
import net.youmi.android.libs.platform.global.Global_DeveloperConfig;
import net.youmi.android.libs.platform.global.Global_Runtime_ClientId;

import org.json.JSONObject;

import java.util.Locale;

/**
 * UmipayCommandTaskManager
 *
 * @author zacklpx
 *         date 15-4-9
 *         description
 */
public class UmipayCommandTaskManager implements CommandResponseListener {

	private static UmipayCommandTaskManager sInstance;
	private Context mContext;

	private UmipayCommandTaskManager(Context context) {
		this.mContext = context.getApplicationContext();
	}

	public static UmipayCommandTaskManager getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new UmipayCommandTaskManager(context);
		}
		return sInstance;
	}

	/**
	 * 初始化接口
	 */
	public CommandTask StartInitCommandTask() {
		return new UmipayCommandTask(mContext, TaskCMD.MP_CMD_INIT, this) {
			@Override
			public void addSpecificParams(final JSONObject params) {
				Global_Runtime_ClientId clientId = new Global_Runtime_ClientId(mContext);
				Util_System_Display_DisplayInfo displayInfo = Util_System_Display_DisplayInfo.getInstance(mContext);
				String bd = clientId.getBd();
				String andid = Global_Runtime_SystemInfo.getAndroidId(mContext);
				String dd = Global_Runtime_SystemInfo.getDeviceModel();
				String dv = Global_Runtime_SystemInfo.getManufacturerInfo();
				String po = Global_Runtime_SystemInfo.getDeviceOsRelease();
				String board = Build.BOARD;
				String brand = Build.BRAND;
				String device = Build.DEVICE;
				boolean ie = clientId.isEmulator();
				int sw = displayInfo.getDisplayWidth();
				int sh = displayInfo.getDisplayHeight();
				int sd = displayInfo.getScreen_DensityLevel();
				int ver = SDKConstantConfig.API_SDK_VERSION;
				String pn = mContext.getPackageName();
				String av = Global_DeveloperConfig.getAppVersionName(mContext);
				int avc = Global_DeveloperConfig.getAppVersionCode(mContext);
				String apn = Util_Network_Status.getApn(mContext);
				String cn = Global_Runtime_SystemInfo.getOperatorName(mContext);
				String sds = Util_System_SDCard_Util.IsSdCardCanWrite(mContext) ? "1" : "0";
				// -----------------------------------------------------------------------------------------------------
				// 33 运行状态 attribute attr 整型 1 (1 << 0)：【可拨打电话】
				// 2 (1 << 1）：【可发送短信】
				// 4 (1 << 2)：【可获取定位信息(即可以通过GPS获取当前用户位置)】
				// 8 (1 << 3)：【处于wifi，3G网络环境】
				// 16 (1 << 4)：【硬件设备是否为平板】
				// 32 (1 << 5) :【该应用是否为Phone UI】
				// 64 (1 << 6) :【是否越狱】
				// 注:1 << 2 [表示1向左移2位]
				int attr = 0;
				try {
					if (Util_Network_Status.isNetworkAvailable(mContext)) {
						// 是否可拨打电话 1<<0
						attr = attr | (1 << 0);
						// 是否可发送短信 1<<1
						attr = attr | (1 << 1);
					}
					// 是否为wifi 1<<3
					if (Util_Network_Status.getNetworkType(mContext) == Util_Network_Status.TYPE_WIFI) {
						attr = attr | (1 << 3);
					}
					// 硬件设备是否为平板 1<<4
					// 该应用是否为PhoneUI 1<<5
					attr = attr | (1 << 5);
				} catch (Throwable ignored) {
				}
				String cc = Locale.getDefault().getCountry();
				String lc = Locale.getDefault().getLanguage();
				int service = SDKConstantConfig.UMIPAY_SDK_SERVICE;

				Basic_JSONUtil.putString(params, "bd", bd);
				Basic_JSONUtil.putString(params, "andid", andid);
				Basic_JSONUtil.putString(params, "dd", dd);
				Basic_JSONUtil.putString(params, "dv", dv);
				Basic_JSONUtil.putString(params, "po", po);
				Basic_JSONUtil.putString(params, "board", board);
				Basic_JSONUtil.putString(params, "brand", brand);
				Basic_JSONUtil.putString(params, "device", device);
				Basic_JSONUtil.putBoolean(params, "ie", ie);
				Basic_JSONUtil.putInt(params, "sw", sw);
				Basic_JSONUtil.putInt(params, "sh", sh);
				Basic_JSONUtil.putInt(params, "sd", sd);
				Basic_JSONUtil.putInt(params, "ver", ver);
				Basic_JSONUtil.putString(params, "pn", pn);
				Basic_JSONUtil.putString(params, "av", av);
				Basic_JSONUtil.putInt(params, "avc", avc);
				Basic_JSONUtil.putString(params, "apn", apn);
				Basic_JSONUtil.putString(params, "cn", cn);
				Basic_JSONUtil.putString(params, "sds", sds);
				Basic_JSONUtil.putInt(params, "attr", attr);
				Basic_JSONUtil.putString(params, "cc", cc);
				Basic_JSONUtil.putString(params, "lc", lc);
				Basic_JSONUtil.putInt(params, "service", service);
			}
		}.execute();
	}

	public CommandTask GetAccountListCommandTask() {
		return new UmipayCommandTask(mContext, TaskCMD.MP_CMD_GETACCOUNTLIST, this) {
			@Override
			public void addSpecificParams(JSONObject params) {
				//没有参数
			}
		}.execute();
	}

	public CommandTask LoginCommandTask(final String username, final String psw, boolean isRememberPsw) {
		Bundle extResponse= new Bundle();
		extResponse.putString("username", username);
		extResponse.putString("psw", psw);
		extResponse.putBoolean("isRememberPsw", isRememberPsw);
		return new UmipayCommandTask(mContext, TaskCMD.MP_CMD_OPENLOGIN, this, extResponse) {
			@Override
			public void addSpecificParams(JSONObject params) {
				String user = username.toLowerCase();
				byte[] psw_md5_bytes = Coder_Md5.md5_16(psw);
				byte[] user_bytes = user.getBytes();
				byte[] bytedata = Coder_Md5.concatByteArrays(psw_md5_bytes, user_bytes);
				String psw_md5 = Coder_Md5.md5(bytedata);
				Basic_JSONUtil.putString(params, "username", user);
				Basic_JSONUtil.putString(params, "password", psw_md5);
			}
		}.execute();
	}

	public CommandTask VerificateSMSCommandTask(final int type, final String mobile, final String code) {
		return new UmipayCommandTask(mContext, TaskCMD.MP_CMD_SMSOP, this) {
			@Override
			public void addSpecificParams(JSONObject params) {
				Basic_JSONUtil.putInt(params, "type", type);
				Basic_JSONUtil.putString(params, "mobile", mobile);
				Basic_JSONUtil.putString(params, "code", code);
			}
		}.execute();
	}

	public CommandTask RegistCommandTask(final String username, final String psw, final String phoneNum) {
		Bundle extResponse = new Bundle();
		extResponse.putString("username", username);
		extResponse.putString("psw", psw);
		return new UmipayCommandTask(mContext, TaskCMD.MP_CMD_OPENREGISTER, this, extResponse) {
			@Override
			public void addSpecificParams(JSONObject params) {
				Basic_JSONUtil.putString(params, "username", username);
				Basic_JSONUtil.putString(params, "password", psw);
				Basic_JSONUtil.putString(params, "mobile", phoneNum);
			}
		}.execute();
	}

	public CommandTask QuickRegistCommandTask() {
		return new UmipayCommandTask(mContext, TaskCMD.MP_CMD_QUICKREGISTER, this) {
			@Override
			public void addSpecificParams(JSONObject params) {

			}
		}.execute();
	}

	public CommandTask OauthLoginCommandTask(final int type, final String openid, final String token,
	                                         final int expire, final String authdata) {
		Bundle extResponse = new Bundle();
		extResponse.putInt("type", type);
		extResponse.putString("openid", openid);
		extResponse.putString("token", token);
		extResponse.putInt("expire", expire);
		extResponse.putString("authdata", authdata);
		return new UmipayCommandTask(mContext, TaskCMD.MP_CMD_OPENTHIRDLOGIN, this, extResponse) {
			@Override
			public void addSpecificParams(JSONObject params) {
				Basic_JSONUtil.putString(params, "openid", openid);
				Basic_JSONUtil.putString(params, "token", token);
				Basic_JSONUtil.putInt(params, "type", type);
				Basic_JSONUtil.putInt(params, "expire", expire);
				Basic_JSONUtil.putString(params, "authdata", authdata);
			}
		}.execute();
	}

	public CommandTask DeleteAccountCommandTask(final String username) {
		return new UmipayCommandTask(mContext, TaskCMD.MP_CMD_OPENUSERDELETE, this) {
			@Override
			public void addSpecificParams(JSONObject params) {
				Basic_JSONUtil.putString(params, "username", username);
			}
		}.execute();
	}

	public CommandTask PushRoleInfoCommandTask(final int type, final GameRolerInfo gameRolerInfo) {
		return new UmipayCommandTask(mContext, TaskCMD.MP_CMD_PUSHROLEINFO, this) {
			@Override
			public void addSpecificParams(JSONObject params) {
				String openid = "";
				try {
					openid = UmipayAccountManager.getInstance(mContext).getCurrentAccount().getGameUserInfo()
							.getOpenId();
				} catch (Throwable ignore) {
				}
				Basic_JSONUtil.putString(params, "openid", openid);
				Basic_JSONUtil.putInt(params, "type", type);
				Basic_JSONUtil.putString(params, "svrid", gameRolerInfo.getServerId());
				Basic_JSONUtil.putString(params, "svrname", gameRolerInfo.getServerName());
				Basic_JSONUtil.putString(params, "rid", gameRolerInfo.getRoleId());
				Basic_JSONUtil.putString(params, "rname", gameRolerInfo.getRoleName());
				Basic_JSONUtil.putString(params, "rgrade", gameRolerInfo.getRoleLevel());
				Basic_JSONUtil.putInt(params, "balance", gameRolerInfo.getBalance());
				Basic_JSONUtil.putString(params, "vip", gameRolerInfo.getVip());
			}
		}.execute();
	}

	public CommandTask GetPushCommandTask() {
		return new UmipayCommandTask(mContext, TaskCMD.MP_CMD_OPENGETPUSHLIST, this) {
			@Override
			public void addSpecificParams(JSONObject params) {
				//No specific params
			}
		}.execute();
	}

	public CommandTask GetFloatMenuPushCommandTask() {
		if (!UmipayAccountManager.getInstance(mContext).isLogin()) {
			return null;
		}
		return new UmipayCommandTask(mContext, TaskCMD.MP_CMD_REDPOINT, this) {
			@Override
			public void addSpecificParams(JSONObject params) {
				UmipayAccount currentAccount = UmipayAccountManager.getInstance(mContext).getCurrentAccount();
				String openid = "";
				try {
					openid = currentAccount.getGameUserInfo().getOpenId();
				} catch (Throwable ignored) {
				}
				Basic_JSONUtil.putString(params, "openid", openid);
			}
		}.execute();
	}


	@Override
	public void onResponse(CommandResponse response, Bundle... extResponse) {
		if (response == null) {
			return;
		}
		int cmd = response.getCmd();
		CommonRspParser parser = null;
		switch (cmd) {
			case TaskCMD.MP_CMD_INIT:
				parser = new RspParser_Cmd_Init(mContext);
				break;
			case TaskCMD.MP_CMD_GETACCOUNTLIST:
				parser = new RspParser_Cmd_GetAccount(mContext);
				break;
			case TaskCMD.MP_CMD_OPENLOGIN:
				parser = new RspParser_Cmd_Login(mContext);
				break;
			case TaskCMD.MP_CMD_SMSOP:
				parser = new RspParser_Cmd_VerificateSMS(mContext);
				break;
			case TaskCMD.MP_CMD_OPENREGISTER:
				parser = new RspParser_Cmd_Regist(mContext);
				break;
			case TaskCMD.MP_CMD_QUICKREGISTER:
				parser = new RspParser_Cmd_QuickRegist(mContext);
				break;
			case TaskCMD.MP_CMD_OPENTHIRDLOGIN:
				parser = new RspParser_Cmd_OauthLogin(mContext);
				break;
			case TaskCMD.MP_CMD_OPENUSERDELETE:
				parser = new RspParser_Cmd_DeleteAccount(mContext);
				break;
			case TaskCMD.MP_CMD_PUSHROLEINFO:
				parser = new RspParser_Cmd_PushGameInfo(mContext);
				break;
			case TaskCMD.MP_CMD_OPENGETPUSHLIST:
				parser = new RspParser_Cmd_GetPush(mContext);
				break;
			case TaskCMD.MP_CMD_REDPOINT:
				parser = new RspParser_Cmd_RedPoint(mContext);
				break;
		}

		if (parser != null) {
			parser.parseResponse(response, extResponse);
		}
	}
}
