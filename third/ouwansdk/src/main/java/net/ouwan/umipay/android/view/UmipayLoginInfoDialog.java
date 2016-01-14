package net.ouwan.umipay.android.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.ouwan.umipay.android.Utils.Util_Resource;
import net.ouwan.umipay.android.api.UmipayActivity;
import net.ouwan.umipay.android.api.UmipaySDKManager;
import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.asynctask.CommandTask;
import net.ouwan.umipay.android.asynctask.TaskCMD;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.ouwan.umipay.android.entry.gson.Gson_Login;
import net.ouwan.umipay.android.interfaces.Interface_Account_Listener_Login;
import net.ouwan.umipay.android.manager.ListenerManager;
import net.ouwan.umipay.android.manager.UmipayCommandTaskManager;

/**
 * UmipayLoginInfoDialog
 *
 * @author zacklpx
 *         date 15-3-10
 *         description
 */
public class UmipayLoginInfoDialog extends Dialog implements Interface_Account_Listener_Login, View.OnClickListener {
	public static final int NORMAL_LOGIN = 0;
	public static final int AUTO_LOGIN = 1;
	public static final int REGISTER_AND_LOGIN = 2;
	public static final int QUCIK_REGISTER = 3;

	private final static long AUTO_LOGIN_CANCEL_TIME = 4000;
	private final static long AUTO_LOGIN_TIPS_TIME = 3000;
	private final static int AUTO_LOGIN_SUCCESS_MSG = 1;
	private final static int AUTO_LOGIN_HIDE_TIP = 2;

	private static final int MSG_LOGIN = 1;

	Context mContext;
	UmipayLoginInfoDialog mDialog;

	View mProgressView;
	View mEnterGameTextView;
	TextView mStatusTextView;
	TextView mUsernameTextView;
	Button mCancel;
	Button mAccountBtn;
	String mStatus;
	String mUsername;
	boolean mIsAutoLogin;
	UmipayAccount mAccount;
	private ViewGroup mRootLayout;
	private Handler mHandler;
	private WindowManager mWindowManager;
	private int mScreenWidth;
	private int mScreenHeight;
	private long mStartTime;
	private long viewtime_ms = 0;
	private boolean mIsCancel;
	private InternalHandler mInternalHandler = new InternalHandler(Looper.getMainLooper());
	private CommandTask mCurrentTask;

	public UmipayLoginInfoDialog(Context context, String username, String status, boolean isAutoLogin,
	                             UmipayAccount account) {

		super(context, Util_Resource.getIdByReflection(context, "style",
				"umipay_progress_dialog_theme"));
		this.setCancelable(false);
		this.mContext = context;
		this.mDialog = this;
		this.mStatus = status;
		this.mUsername = username;
		this.mIsAutoLogin = isAutoLogin;
		this.mAccount = account;
		initViews();
		setContentView(mRootLayout);
		setLayoutParams();
		initHandler(mContext);
	}

	private void setLayoutParams() {
		try {
			mWindowManager = (WindowManager) mContext.getApplicationContext()
					.getSystemService(Context.WINDOW_SERVICE);
			DisplayMetrics displayMetrics = new DisplayMetrics();
			mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
			mScreenWidth = displayMetrics.widthPixels;
			mScreenHeight = displayMetrics.heightPixels;
			WindowManager.LayoutParams mLayoutParams = getWindow().getAttributes();  //获取对话框当前的参数值
			mLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
			mLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
			getWindow().setAttributes(mLayoutParams);     //设置生效
			getWindow().getDecorView().setPadding(0, 0, 0, 0);//默认样式有pading,需要修改才能占满
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	public void show(long viewtime_ms) {
		setViewTime(viewtime_ms);
		Animation animation = AnimationUtils.loadAnimation(mContext, Util_Resource.getIdByReflection(mContext, "anim",
				"umipay_progress_move"));

		try {
			if (animation != null) {
				mProgressView.startAnimation(animation);
			}
			if (!mIsAutoLogin) {
				mCancel.setVisibility(View.GONE);
				mAccountBtn.setVisibility(View.VISIBLE);
				mHandler.sendEmptyMessageDelayed(AUTO_LOGIN_HIDE_TIP, viewtime_ms + 500);
			} else {
				//自动登录
				mCancel.setVisibility(View.VISIBLE);
				mAccountBtn.setVisibility(View.GONE);
				mCancel.setOnClickListener(this);
				mEnterGameTextView.setVisibility(View.INVISIBLE);
				if (mAccount.getOauthType() == UmipayAccount.TYPE_NORMAL) {
					login(mAccount);
				} else {
					oauthLogin(mAccount);
				}
				mStartTime = System.currentTimeMillis();
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		super.show();
	}

	private void setViewTime(final long time) {
		viewtime_ms = time;
	}

	private void initHandler(final Context context) {
		mHandler = new Handler(context.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				try {
					if (msg.what == AUTO_LOGIN_SUCCESS_MSG) {
						sendLoginResultMsg(AUTO_LOGIN, mAccount);
//						ListenerManager.callbackLoginSuccess(context, mAccount, AUTO_LOGIN);
						mHandler.removeMessages(AUTO_LOGIN_SUCCESS_MSG);
						mHandler.sendEmptyMessageDelayed(AUTO_LOGIN_HIDE_TIP, AUTO_LOGIN_TIPS_TIME);
						mCancel.setVisibility(View.GONE);
						mAccountBtn.setVisibility(View.VISIBLE);
						mEnterGameTextView.setVisibility(View.VISIBLE);
						String username = ListenerManager.getUserName(mAccount.getUserName(),
								mAccount.getOauthType());
						mStatusTextView.setText("欢迎您回来！");
						mUsernameTextView.setText(username);
					}
					if (msg.what == AUTO_LOGIN_HIDE_TIP) {
						dismiss();
						mHandler.removeMessages(AUTO_LOGIN_SUCCESS_MSG);
						mHandler.removeMessages(AUTO_LOGIN_HIDE_TIP);
					}
				} catch (Throwable e) {
					Debug_Log.e(e);
				}
			}
		};
	}

	private void initViews() {
		if (mContext == null) {
			return;
		}
		mRootLayout = (ViewGroup) ViewGroup.inflate(mContext, Util_Resource.getIdByReflection(mContext, "layout",
				"umipay_logininfo_layout"), null);
		mStatusTextView = (TextView) mRootLayout.findViewById(Util_Resource.getIdByReflection(mContext, "id",
				"umipay_logininfo_status_tv"));
		mStatusTextView.setText(mStatus);
		mUsernameTextView = (TextView) mRootLayout.findViewById(Util_Resource.getIdByReflection(mContext, "id",
				"umipay_logininfo_username_tv"));
		mUsernameTextView.setText(mUsername);
		mProgressView = mRootLayout.findViewById(Util_Resource.getIdByReflection(mContext, "id",
				"umipay_logininfo_progress"));
		mEnterGameTextView = mRootLayout.findViewById(Util_Resource.getIdByReflection(mContext, "id",
				"umipay_logininfo_entergame_tv"));
		mCancel = (Button) mRootLayout.findViewById(Util_Resource.getIdByReflection(mContext, "id",
				"umipay_logininfo_cancel"));
		mAccountBtn = (Button) mRootLayout.findViewById(Util_Resource.getIdByReflection(mContext, "id",
				"umipay_logininfo_account"));
		mAccountBtn.setOnClickListener(this);


	}


	private void login(UmipayAccount account) {
		ListenerManager.setCommandLoginListener(mDialog);
		mCurrentTask = UmipayCommandTaskManager.getInstance(mContext).LoginCommandTask(account.getUserName(), account
				.getPsw(),true);
	}

	private void oauthLogin(UmipayAccount account) {
		ListenerManager.setCommandLoginListener(mDialog);
		UmipayCommandTaskManager.getInstance(mContext).OauthLoginCommandTask(account.getOauthType(), account
				.getOauthID(), account.getOauthToken(), account.getOauthExpire(), null);
	}

	long pastTime_ms() {
		return System.currentTimeMillis() - mStartTime;
	}

	public void toast(String text) {
		Toast toast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	@Override
	public void onClick(View v) {
		if (v.equals(mCancel)) {
			//取消登录
			dismiss();
			//移除登录成功的消息
			mHandler.removeMessages(AUTO_LOGIN_SUCCESS_MSG);
			mIsCancel = true;
			Toast.makeText(mContext, "取消自动登录", Toast.LENGTH_SHORT).show();
			if (mCurrentTask != null && mCurrentTask.getStatus() == CommandTask.Status.RUNNING) {
				mCurrentTask.cancel(true);
			}
			//取消自动登录，则跳转到登录界面
			Intent intent = new Intent(mContext, UmipayActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
			return;
		}
		if (v.equals(mAccountBtn)) {
			mHandler.removeMessages(AUTO_LOGIN_HIDE_TIP);
			dismiss();
			UmipaySDKManager.showAccountManagerView(mContext);
		}
	}

	private void handleLoginMsg(MsgData data) {
		int code = data.getCode();
		String msg = data.getMsg();
		if (code == UmipaySDKStatusCode.SUCCESS) {
			UmipayAccount account = (UmipayAccount) data.getData();
			try {
				mAccount = account;
				long pasttime = pastTime_ms();
				if (mIsCancel) {
					return;
				}
				if (pasttime > AUTO_LOGIN_CANCEL_TIME) {
					mHandler.sendEmptyMessage(AUTO_LOGIN_SUCCESS_MSG);
				} else {
					mHandler.sendEmptyMessageDelayed(AUTO_LOGIN_SUCCESS_MSG, AUTO_LOGIN_CANCEL_TIME - pastTime_ms());
				}
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
		} else {
			try {
				dismiss();
				msg = UmipaySDKStatusCode.handlerMessage(code, msg);
				toast(msg + "(" + code + ")");
				//还没有点击取消键时，登录失败则到登录界面
				if (!mIsCancel) {
					//自动登录失败，跑到登录界面
					Intent intent = new Intent(mContext, UmipayActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(intent);
				}
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
		}
	}


	@Override
	public void onLogin(int code, String msg, UmipayAccount account) {
		sendMessage(MSG_LOGIN, new MsgData(code, msg, account));
//		if (data instanceof Gson_Cmd_Login) {
//			Gson_Cmd_Login gsonCmdLogin = (Gson_Cmd_Login) data;
//			int code = gsonCmdLogin.getCode();
//			String msg = gsonCmdLogin.getMessage();
//			Gson_Cmd_Login.Cmd_Login_Data cmdLoginData = gsonCmdLogin.getData();
//			if (code == UmipaySDKStatusCode.SUCCESS) {
//				String username = mAccount.getUserName();
//				String psw = mAccount.getPsw();
//				UmipayAccount umipayAccount = UmipayAccountManager.getInstance(mContext).getAccountByUserName
//						(username);
//				if (umipayAccount == null) {
//					umipayAccount = new UmipayAccount(username, psw);
//				} else {
//					umipayAccount.setPsw(psw);
//				}
//				GameUserInfo userInfo = new GameUserInfo();
//				userInfo.setOpenId(cmdLoginData.getOpenid());
//				userInfo.setSign(cmdLoginData.getSign());
//				userInfo.setTimestamp_s(cmdLoginData.getTs());
//				umipayAccount.setGameUserInfo(userInfo);
//
//				umipayAccount.setUid(cmdLoginData.getUid());
//				umipayAccount.setSession(cmdLoginData.getSession());
//				umipayAccount.setBindMobile(cmdLoginData.getBindmobile());
//				umipayAccount.setLastLoginTime_ms(System.currentTimeMillis());
//				umipayAccount.setRemenberPsw(true);
//
//				UmipayAccountManager.getInstance(mContext).saveAccount(umipayAccount);
//				UmipayAccountManager.getInstance(mContext).setCurrentAccount(umipayAccount);
//				UmipayAccountManager.getInstance(mContext).setLogin(true);
//				if (umipayAccount.isRemenberPsw()) {
//					LocalPasswordManager.getInstance(mContext).putPassword(umipayAccount.getUserName(),
//							umipayAccount.getPsw());
//				} else {
//					LocalPasswordManager.getInstance(mContext).removePassword(umipayAccount.getUserName());
//				}
//
//				sendMessage(MSG_LOGIN, new MsgData(UmipaySDKStatusCode.SUCCESS, null, umipayAccount));
//			} else {
//				sendMessage(MSG_LOGIN, new MsgData(code, msg, null));
//			}
//		}
//		if (data instanceof Gson_Cmd_OauthLogin) {
//			Gson_Cmd_OauthLogin cmdOauthLogin = (Gson_Cmd_OauthLogin) data;
//			int code = cmdOauthLogin.getCode();
//			String msg = cmdOauthLogin.getMessage();
//			Gson_Cmd_OauthLogin.Cmd_ThirdLogin_Data cmdThirdLoginData = cmdOauthLogin.getData();
//			if (code == UmipaySDKStatusCode.SUCCESS) {
//				String oauthId = mAccount.getOauthID();
//				String oauthToken = mAccount.getOauthToken();
//				int oauthType = mAccount.getOauthType();
//				int oaythExpire = mAccount.getOauthExpire();
//				UmipayAccount umipayAccount = UmipayAccountManager.getInstance(mContext).getAccountByOauthId
//						(oauthId, oauthType);
//				if (umipayAccount == null) {
//					umipayAccount = new UmipayAccount(oauthId, oauthToken, oauthType);
//				} else {
//					umipayAccount.setOauthToken(oauthToken);
//				}
//				umipayAccount.setOauthExpire(oaythExpire);
//
//				GameUserInfo userInfo = new GameUserInfo();
//				userInfo.setOpenId(cmdThirdLoginData.getOpenid());
//				userInfo.setSign(cmdThirdLoginData.getSign());
//				userInfo.setTimestamp_s(cmdThirdLoginData.getTs());
//				umipayAccount.setGameUserInfo(userInfo);
//
//				umipayAccount.setUserName(cmdThirdLoginData.getUsername());
//				umipayAccount.setUid(cmdThirdLoginData.getUid());
//				umipayAccount.setSession(cmdThirdLoginData.getSession());
//				umipayAccount.setLastLoginTime_ms(System.currentTimeMillis());
//
//				UmipayAccountManager.getInstance(mContext).saveAccount(umipayAccount);
//				UmipayAccountManager.getInstance(mContext).setCurrentAccount(umipayAccount);
//				UmipayAccountManager.getInstance(mContext).setLogin(true);
//				if (umipayAccount.isRemenberPsw()) {
//					LocalPasswordManager.getInstance(mContext).putPassword(umipayAccount.getUserName(),
//							umipayAccount.getPsw());
//				} else {
//					LocalPasswordManager.getInstance(mContext).removePassword(umipayAccount.getUserName());
//				}
//
//				sendMessage(MSG_LOGIN, new MsgData(code, msg, umipayAccount));
//			} else {
//				sendMessage(MSG_LOGIN, new MsgData(code, msg, null));
//			}
//		}
	}

	public ViewGroup getRootLayout() {
		return mRootLayout;
	}

	private class MsgData {
		private int code;
		private String msg;
		private Object data;

		public MsgData(int code, String msg, Object data) {
			this.code = code;
			this.msg = msg;
			this.data = data;
		}

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

		public Object getData() {
			return data;
		}

		public void setData(Object data) {
			this.data = data;
		}
	}

	private void sendMessage(int what, Object obj) {
		Message msg = mInternalHandler.obtainMessage(what, obj);
		msg.sendToTarget();
	}

	private class InternalHandler extends Handler {
		public InternalHandler(Looper mainLooper) {
			super(mainLooper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_LOGIN:
					handleLoginMsg((MsgData) msg.obj);
					break;
			}
		}
	}

	private void sendLoginResultMsg(int loginType, UmipayAccount account) {
		Gson_Login gsonLogin = new Gson_Login(UmipaySDKManager.getShowLoginViewContext(), UmipaySDKStatusCode.SUCCESS,
				null, null);
		Gson_Login.Login_Data loginData = gsonLogin.new Login_Data();
		loginData.setLoginType(loginType);
		loginData.setAccount(account);
		gsonLogin.setData(loginData);
		ListenerManager.sendMessage(TaskCMD.MP_CMD_OPENLOGIN, gsonLogin);
	}
}
