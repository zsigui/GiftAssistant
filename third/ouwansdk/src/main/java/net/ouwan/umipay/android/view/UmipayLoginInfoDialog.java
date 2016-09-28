package net.ouwan.umipay.android.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
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
import net.ouwan.umipay.android.entry.UmipayCommonAccount;
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
public class UmipayLoginInfoDialog extends Dialog implements Interface_Account_Listener_Login, View.OnClickListener,Animation.AnimationListener {
	public static final int NORMAL_LOGIN = 0;
	public static final int AUTO_LOGIN = 1;
	public static final int REGISTER_AND_LOGIN = 2;
	public static final int QUCIK_REGISTER = 3;

	private final static long AUTO_LOGIN_CANCEL_TIME = 4000;
	private final static long AUTO_LOGIN_TIPS_TIME = 3000;
	private final static int AUTO_LOGIN_SUCCESS_MSG = 1;
	private final static int AUTO_LOGIN_HIDE_TIP = 2;
	private final static int AUTO_LOGIN_HIDE_TIP_ANIMATION = 3;

	private static final int MSG_LOGIN = 1;

	Context mContext;
	UmipayLoginInfoDialog mDialog;
	Animation animation_tips_in;
	Animation animation_process;
	Animation animation_tips_out;

	TextView mTipsTextView;
	View mContentLinearLayout;
	View mProgressView;
	View mEnterGameTextView;
	TextView mStatusTextView;
	TextView mUsernameTextView;
	Button mCancel;
	String mStatus;
	String mUsername;
	boolean mIsAutoLogin;
	Object mAccount;
	private ViewGroup mRootLayout;
	private Handler mHandler;
	private long mStartTime;
	private boolean mIsCancel;
	private InternalHandler mInternalHandler = new InternalHandler(Looper.getMainLooper());
	private CommandTask mCurrentTask;

	public UmipayLoginInfoDialog(Context context, String username, String status, boolean isAutoLogin,
	                             Object account) {

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
		initAnimation();
	}

	private void setLayoutParams() {
		try {
			WindowManager.LayoutParams mLayoutParams = getWindow().getAttributes();  //获取对话框当前的参数值
			mLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
			mLayoutParams.width = (int) getContext().getResources().getDimension(Util_Resource.getIdByReflection(getContext(), "dimen", "umipay_main_diglog_width"));
			getWindow().setAttributes(mLayoutParams);     //设置生效
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	public void show(long viewtime_ms) {
		try {
			if (!mIsAutoLogin) {
				mHandler.sendEmptyMessageDelayed(AUTO_LOGIN_HIDE_TIP_ANIMATION,viewtime_ms + 500);

				if(mContentLinearLayout != null){
					mContentLinearLayout.setVisibility(View.GONE);
				}
				if(mTipsTextView != null) {
					if(animation_tips_in != null){
						mTipsTextView.startAnimation(animation_tips_in);
					}
				}
			} else {
				//自动登录
				//自动登录才播放动画
				if (animation_process != null) {
					mProgressView.startAnimation(animation_process);
				}
				if(mContentLinearLayout != null){
					mContentLinearLayout.setVisibility(View.VISIBLE);
				}
				if(mTipsTextView != null) {
					mTipsTextView.setVisibility(View.GONE);
				}
				mCancel.setOnClickListener(this);
				mStatusTextView.setText("欢迎您!");
				if(mAccount instanceof  UmipayAccount) {
					UmipayAccount account= (UmipayAccount)mAccount;
					if(mUsernameTextView != null) {
						mUsernameTextView.setText(account.getUserName());
					}
					if (account.getOauthType() == UmipayAccount.TYPE_NORMAL) {
						login(account);
					} else if (account.getOauthType() == UmipayAccount.TYPE_MOBILE) {
						autologin(account);
					} else {
						oauthLogin(account);
					}
				}else if(mAccount instanceof  UmipayCommonAccount){
					UmipayCommonAccount account= (UmipayCommonAccount)mAccount;
					if(mUsernameTextView != null) {
						mUsernameTextView.setText(account.getUserName());
					}
					autologin(account);
				}
				mStartTime = System.currentTimeMillis();
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		super.show();
	}

	private void initHandler(final Context context) {
		mHandler = new Handler(context.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				try {
					if (msg.what == AUTO_LOGIN_SUCCESS_MSG) {
						//登录成功只会返回UmipayAccount
						sendLoginResultMsg(AUTO_LOGIN, (UmipayAccount) mAccount);
						mHandler.removeMessages(AUTO_LOGIN_SUCCESS_MSG);
						mHandler.sendEmptyMessageDelayed(AUTO_LOGIN_HIDE_TIP_ANIMATION, AUTO_LOGIN_TIPS_TIME);
						if(mContentLinearLayout != null){
							mContentLinearLayout.setVisibility(View.GONE);
						}
						if(mProgressView != null && mProgressView.getAnimation() != null){
							mProgressView.getAnimation().cancel();
						}
						if(mTipsTextView != null) {
								if(animation_tips_in != null){
									mTipsTextView.startAnimation(animation_tips_in);
								}

						}
						Intent intent = new Intent(mContext, UmipayActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.setAction(UmipayActivity.ACTION_ANNOUNCEMENT);
						mContext.startActivity(intent);
					}
					if(msg.what == AUTO_LOGIN_HIDE_TIP_ANIMATION){
						if(mTipsTextView != null) {
							if (animation_tips_out != null) {
								mTipsTextView.startAnimation(animation_tips_out);
							}
						}
						mHandler.removeMessages(AUTO_LOGIN_SUCCESS_MSG);
						mHandler.removeMessages(AUTO_LOGIN_HIDE_TIP);
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

	private void initAnimation(){
		animation_process = AnimationUtils.loadAnimation(mContext, Util_Resource.getIdByReflection(mContext, "anim",
				"umipay_progress_move"));

		animation_tips_in = AnimationUtils.loadAnimation(mContext,  Util_Resource.getIdByReflection(mContext, "anim", "umipay_wmtoast_in"));
		if(animation_tips_in != null) {
			animation_tips_in.setAnimationListener(this);
		}

		animation_tips_out = AnimationUtils.loadAnimation(mContext, Util_Resource.getIdByReflection(mContext, "anim", "umipay_wmtoast_out"));
		if(animation_tips_out != null){
			animation_tips_out.setAnimationListener(this);
		}
	}
	private void initViews() {
		if (mContext == null) {
			return;
		}
		mRootLayout = (ViewGroup) ViewGroup.inflate(mContext, Util_Resource.getIdByReflection(mContext, "layout",
				"umipay_logininfo_layout"), null);
		mTipsTextView = (TextView) mRootLayout.findViewById(Util_Resource.getIdByReflection(mContext, "id",
				"umipay_logininfo_tips_tv"));
		mContentLinearLayout =  mRootLayout.findViewById(Util_Resource.getIdByReflection(mContext, "id",
				"umipay_logininfo_content_ll"));
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


	}

	private void autologin(UmipayCommonAccount account) {
		ListenerManager.setCommandLoginListener(mDialog);
		mCurrentTask = UmipayCommandTaskManager.getInstance(mContext).AutoLoginCommandTask(account.getUserName(),account.getUid(),account.getSession());
	}
	private void autologin(UmipayAccount account) {
		ListenerManager.setCommandLoginListener(mDialog);
		mCurrentTask = UmipayCommandTaskManager.getInstance(mContext).AutoLoginCommandTask(account.getUserName(),account.getUid(),account.getSession());
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
	}

	public ViewGroup getRootLayout() {
		return mRootLayout;
	}

	@Override
	public void onAnimationStart(Animation animation) {
		if(animation.equals(animation_tips_in)){
			if(mTipsTextView != null ) {
				if(mAccount != null) {
					String username = null;
					if(mAccount instanceof UmipayAccount){
						username = ((UmipayAccount)mAccount).getUserName();
					}else if(mAccount instanceof  UmipayCommonAccount){
						username = ((UmipayCommonAccount)mAccount).getUserName();
					}
					setUserName(mTipsTextView, username);
				}else{
					setUserName(mTipsTextView,mUsername);
				}
				mTipsTextView.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if(animation.equals(animation_tips_out)){
			if(mHandler != null){
				mHandler.sendEmptyMessage(AUTO_LOGIN_HIDE_TIP);
			}
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {

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

	private void setUserName(TextView tv,String username){
		if(tv == null || tv.getText() == null || TextUtils.isEmpty(username)){
			return;
		}
		try {
			StringBuffer username_str = new StringBuffer();
			StringBuffer content = new StringBuffer();
			if(username.length() >32){
				username_str.append(username.substring(0,32)).append("...");
			}else{
				username_str.append(username);
			}
			content.append(String.format(tv.getText().toString(), username_str.toString()));
			int start = content.indexOf(username_str.toString());
			int end = username.length();
			SpannableString spannableString = new SpannableString(content);

			ForegroundColorSpan span = new ForegroundColorSpan(Color.YELLOW);
			spannableString.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			tv.setText(spannableString);
		}catch (Throwable e){
			Debug_Log.e(e);
		}
	}
}
