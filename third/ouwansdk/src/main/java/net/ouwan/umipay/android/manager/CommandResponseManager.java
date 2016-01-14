package net.ouwan.umipay.android.manager;

import android.content.Context;

import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.asynctask.CommandResponse;
import net.ouwan.umipay.android.asynctask.TaskCMD;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.ouwan.umipay.android.interfaces.Interface_Account_Listener_Login;
import net.ouwan.umipay.android.interfaces.Interface_Account_Listener_Register;
import net.ouwan.umipay.android.interfaces.Interface_Verificate_SMS_Listener;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;

import org.json.JSONObject;

/**
 * CommandResponseManager
 *
 * @author zacklpx
 *         date 15-3-4
 *         description
 */
public class CommandResponseManager {
	public static void handleResponse(Context context, CommandResponse response, Object params) {
		if (response == null) {
			return;
		}
		switch (response.getCmd()) {
			case TaskCMD.MP_CMD_INIT:
				handleInitResult(context, response);
				break;
			case TaskCMD.MP_CMD_GETACCOUNTLIST:
				handleGetAccountsResult(context, response);
				break;
			case TaskCMD.MP_CMD_SMSOP:
				handleVerificateSMSResult(context, response, (Interface_Verificate_SMS_Listener) params);
				break;
			case TaskCMD.MP_CMD_OPENLOGIN:
				handleLoginResult(context, response, (Interface_Account_Listener_Login) params);
				break;
			case TaskCMD.MP_CMD_OPENREGISTER:
				handleRegistResult(context, response, (Interface_Account_Listener_Register) params);
				break;
			case TaskCMD.MP_CMD_OPENTHIRDLOGIN:
				handleThirdLoginResult(context, response, (Interface_Account_Listener_Login) params);
				break;
			case TaskCMD.MP_CMD_QUICKREGISTER:
				handleQuickRegistResult(context, response, (Interface_Account_Listener_Register) params);
				break;
			case TaskCMD.MP_CMD_OPENUSERDELETE:
				handleDeleteAccountResult(context, response);
				break;
			case TaskCMD.MP_CMD_PUSHROLEINFO:
				handlePushRoleInfoResult(context, response);
		}
	}

	/**
	 * 处理拉取用户列表结果
	 *
	 * @param context  context
	 * @param response {@link net.ouwan.umipay.android.asynctask.CommandResponse}命令字返回结果
	 */
	private static void handleGetAccountsResult(Context context, CommandResponse response) {
		if (response == null) {
			return;
		}
		if (response.getCode() == UmipaySDKStatusCode.SUCCESS) {
			UmipayAccountManager.getInstance(context).updateAccountList();
			Debug_Log.d("Load accounts success!");
		} else {
			Debug_Log.d("Load account failed! code = " + response.getCode() + ", msg = " + response.getMsg());
		}
	}

	/**
	 * 处理初始化结果
	 *
	 * @param context  context
	 * @param response {@link net.ouwan.umipay.android.asynctask.CommandResponse}命令字返回结果
	 */
	private static void handleInitResult(Context context, CommandResponse response) {
		ListenerManager.callbackInitFinish(response.getCode(), response.getMsg());
		//TODO 初始化完成要预加载

	}

	/**
	 * 处理短信验证码结果
	 *
	 * @param context  context
	 * @param response {@link net.ouwan.umipay.android.asynctask.CommandResponse}命令字返回结果
	 */
	private static void handleVerificateSMSResult(Context context, CommandResponse response,
	                                              Interface_Verificate_SMS_Listener listener) {
		if (listener == null) {
			return;
		}
		int code = response.getCode();
		String msg = UmipaySDKStatusCode.handlerMessage(code, response.getMsg());
		int type = Basic_JSONUtil.getInt((JSONObject) response.getResult(), "type", Interface_Verificate_SMS_Listener
				.TYPE_SEND_SMS);
//		if (code == UmipaySDKStatusCode.SUCCESS) {
//			listener.onVerificateSuccess(type);
//		} else {
//			listener.onVerificateFailed(code, msg, type);
//		}
	}

	/**
	 * 处理登录结果
	 *
	 * @param context  context
	 * @param response {@link net.ouwan.umipay.android.asynctask.CommandResponse}命令字返回结果
	 */
	private static void handleLoginResult(Context context, CommandResponse response, Interface_Account_Listener_Login
			listener) {
		if (listener == null) {
			return;
		}
		int code = response.getCode();
		String msg = UmipaySDKStatusCode.handlerMessage(code, response.getMsg());
		UmipayAccount account = null;
		try {
			account = (UmipayAccount) response.getResult();
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
//		if (code != UmipaySDKStatusCode.SUCCESS || account == null) {
//			listener.onLoginFailed(code, msg);
//		} else {
//			listener.onLoginSuccess(account);
//		}
	}

	/**
	 * 处理注册结果
	 *
	 * @param context  context
	 * @param response {@link net.ouwan.umipay.android.asynctask.CommandResponse}命令字返回结果
	 */
	private static void handleRegistResult(Context context, CommandResponse response,
	                                       Interface_Account_Listener_Register
			                                       listener) {
		if (listener == null) {
			return;
		}
		int code = response.getCode();
		String msg = UmipaySDKStatusCode.handlerMessage(code, response.getMsg());
		UmipayAccount account = null;
		try {
			account = (UmipayAccount) response.getResult();
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
//		if (code != UmipaySDKStatusCode.SUCCESS || account == null) {
//			listener.onRegisterFailed(code, msg);
//		} else {
//			listener.onRegisterSuccess(account);
//		}
	}

	/**
	 * 处理快速注册结果
	 *
	 * @param context  context
	 * @param response {@link net.ouwan.umipay.android.asynctask.CommandResponse}命令字返回结果
	 */
	private static void handleQuickRegistResult(Context context, CommandResponse response,
	                                            Interface_Account_Listener_Register
			                                            listener) {
		if (listener == null) {
			return;
		}
		int code = response.getCode();
		String msg = UmipaySDKStatusCode.handlerMessage(code, response.getMsg());
		UmipayAccount account = null;
		try {
			account = (UmipayAccount) response.getResult();
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
//		if (code != UmipaySDKStatusCode.SUCCESS || account == null) {
//			listener.onRegisterFailed(code, msg);
//		} else {
//			listener.onRegisterSuccess(account);
//		}
	}

	/**
	 * 处理登录结果
	 *
	 * @param context  context
	 * @param response {@link net.ouwan.umipay.android.asynctask.CommandResponse}命令字返回结果
	 */
	private static void handleThirdLoginResult(Context context, CommandResponse response,
	                                           Interface_Account_Listener_Login
			                                           listener) {
		if (listener == null) {
			return;
		}
		int code = response.getCode();
		String msg = UmipaySDKStatusCode.handlerMessage(code, response.getMsg());
		UmipayAccount account = null;
		try {
			account = (UmipayAccount) response.getResult();
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
//		if (code != UmipaySDKStatusCode.SUCCESS || account == null) {
//			listener.onLoginFailed(code, msg);
//		} else {
//			listener.onLoginSuccess(account);
//		}
	}

	/**
	 * 处理登录结果
	 *
	 * @param context  context
	 * @param response {@link net.ouwan.umipay.android.asynctask.CommandResponse}命令字返回结果
	 */
	private static void handleDeleteAccountResult(Context context, CommandResponse response) {
		int code = response.getCode();
		String msg = UmipaySDKStatusCode.handlerMessage(code, response.getMsg());
		if (code == UmipaySDKStatusCode.SUCCESS) {
			Debug_Log.d("Delete account success!");
		} else {
			Debug_Log.d("Delete account failed! code = " + code + " msg = " + msg);
		}
	}

	/**
	 * 处理登录结果
	 *
	 * @param context  context
	 * @param response {@link net.ouwan.umipay.android.asynctask.CommandResponse}命令字返回结果
	 */
	private static void handlePushRoleInfoResult(Context context, CommandResponse response) {
		int code = response.getCode();
		String msg = UmipaySDKStatusCode.handlerMessage(code, response.getMsg());
		if (code == UmipaySDKStatusCode.SUCCESS) {
			Debug_Log.d("Push RoleInfo success!");
		} else {
			Debug_Log.d("Push RoleInfo failed! code = " + code + " msg = " + msg);
		}
	}
}
