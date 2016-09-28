package net.ouwan.umipay.android.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.ouwan.umipay.android.Utils.Util_Resource;
import net.ouwan.umipay.android.api.UmipaySDKManager;
import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.asynctask.TaskCMD;
import net.ouwan.umipay.android.config.SDKCacheConfig;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_VerificateSMS;
import net.ouwan.umipay.android.entry.gson.Gson_Login;
import net.ouwan.umipay.android.interfaces.Interface_Verificate_SMS_Listener;
import net.ouwan.umipay.android.manager.ListenerManager;
import net.ouwan.umipay.android.manager.UmipayCommandTaskManager;
import net.youmi.android.libs.common.global.Global_Final_Common_Millisecond;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jimmy on 2016/8/14.
 */
public class BindMobileFragment extends BaseFragment implements TextView.OnEditorActionListener,
		Interface_Verificate_SMS_Listener {

	public static final String ARG_ACCOUNT = "umipay_account";
	private UmipayAccount mUmipayAccount;
	private TextView mTitleTv;


	private Button mBindAndLoginBtn;
	private Button mSkipBtn;
	private EditText mPhoneEditor;
	private EditText mCodeEditor;
	private TextView mGetCodeTv;

	private Handler mTimerHandler;
	private Timer mCountdownTimer;
	private int mCurrentVerificateType;
	private int mCountdownText;
	private static final int MSG_UPDATE_COUNTDOWN_BTN = 1;
	private static final int GET_VERIFICATE_CODE_CD = 60;


	public static BindMobileFragment newInstance(UmipayAccount account) {
		Bundle bundle = new Bundle();
		bundle.putSerializable(ARG_ACCOUNT, account);
		BindMobileFragment fragment = new BindMobileFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		if (bundle != null) {
			mUmipayAccount = (UmipayAccount) bundle.getSerializable(ARG_ACCOUNT);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		try {
			mRootLayout = inflater.inflate(Util_Resource.getIdByReflection(getActivity(), "layout",
					"umipay_bindmobile_layout"), container, false);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		initViews();
		initListener();
		initHandler();
		return mRootLayout;
	}

	private void initViews() {
		if (mRootLayout != null) {
			mTitleTv = (TextView) mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_title_tv"));
			mPhoneEditor = (EditText) mRootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
					"umipay_phone_box"));
			mCodeEditor = (EditText) mRootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
					"umipay_code_box"));
			mGetCodeTv = (TextView) mRootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
					"umipay_get_code_btn"));

			mSkipBtn = (Button) mRootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
					"umipay_bindmobile_skip_btn"));

			mBindAndLoginBtn = (Button) mRootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
					"umipay_bind_login_btn"));
		}
		if (mTitleTv != null) {
			mTitleTv.setText(getActivity().getResources().getString(Util_Resource.getIdByReflection(getActivity(),
					"string",
					"umipay_titile_bind_mobile")));
		}
		if (mGetCodeTv != null) {
			mGetCodeTv.setText("获取验证码");
			mGetCodeTv.setEnabled(true);
		}
		if (mCodeEditor != null) {
			mCodeEditor.setOnEditorActionListener(this);
		}
	}

	private void initListener() {
		// 获取验证码
		if (mGetCodeTv != null) {
			mGetCodeTv.setOnClickListener(this);
		}
		// 跳过
		if (mSkipBtn != null) {
			mSkipBtn.setOnClickListener(this);
		}
		// 绑定&登录
		if (mBindAndLoginBtn != null) {
			mBindAndLoginBtn.setOnClickListener(this);
		}
//		if (mClearPhoneBtn != null) {
//			mClearPhoneBtn.setOnClickListener(this);
//		}
	}

	private void initHandler() {
		mTimerHandler = new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				try {
					switch (msg.what) {
						case MSG_UPDATE_COUNTDOWN_BTN:
							if (mCountdownText > 0) {
								mCountdownText--;
								if (mGetCodeTv != null) {
									mGetCodeTv.setText("重获（" + mCountdownText + "）");
								} else if (mCountdownTimer != null) {
									mCountdownTimer.cancel();
								}
							} else {
								if (mCountdownTimer != null) {
									mCountdownTimer.cancel();
								}
								if (mGetCodeTv != null) {
									mGetCodeTv.setText("获取验证码");
									mGetCodeTv.setEnabled(true);
								}
							}
							break;
					}
				} catch (Throwable e) {
					Debug_Log.e(e);
				}
				return true;
			}
		});
	}

	// 获取验证码
	private void getVerificateCode() {
		String mobile = null;
		if (mPhoneEditor != null) {
			mobile = mPhoneEditor.getText().toString();
		}
		if (TextUtils.isEmpty(mobile) || mobile.length() != 11 || mobile.charAt(0) != '1') {
			toast("请输入正确的手机号码");
			return;
		}
		mCurrentVerificateType = Interface_Verificate_SMS_Listener.TYPE_SEND_SMS;
		ListenerManager.setCommandVerificateSMSListener(this);
		UmipayCommandTaskManager.getInstance(getActivity()).VerificateSMSCommandTask(Interface_Verificate_SMS_Listener
				.TYPE_SEND_SMS, mobile, null);
		startProgressDialog();
	}

	private void sendLoginResultMsg(int code, String msg, int loginType, UmipayAccount account) {
		Gson_Login gsonLogin = new Gson_Login(UmipaySDKManager.getShowLoginViewContext(), code,
				msg, null);
		if (code == UmipaySDKStatusCode.SUCCESS) {
			Gson_Login.Login_Data loginData = gsonLogin.new Login_Data();
			loginData.setLoginType(loginType);
			loginData.setAccount(account);
			gsonLogin.setData(loginData);
		}
		ListenerManager.sendMessage(TaskCMD.MP_CMD_OPENLOGIN, gsonLogin);
	}


	// 绑定&登录
	private void bindAndLogin() {
		String mobile = null;
		if (mPhoneEditor != null) {
			mobile = mPhoneEditor.getText().toString();
		}
		String code = null;
		if (mCodeEditor != null) {
			code = mCodeEditor.getText().toString();
		}
		if (TextUtils.isEmpty(mobile) || mobile.length() != 11 || mobile.charAt(0) != '1') {
			toast("请输入正确的手机号码");
			return;
		}
		if (TextUtils.isEmpty(code)) {
			toast("请输入正确的验证码");
			return;
		}
		mCurrentVerificateType = Interface_Verificate_SMS_Listener.TYPE_VERIFICATE_SMS;
		ListenerManager.setCommandVerificateSMSListener(this);
		UmipayCommandTaskManager.getInstance(getActivity()).VerificateSMSCommandTask(Interface_Verificate_SMS_Listener
				.TYPE_VERIFICATE_SMS, mobile, code);
		startProgressDialog();
	}

	// 跳过绑定
	private void skipBind() {
		sendLoginResultMsg(UmipaySDKStatusCode.SUCCESS, null, mUmipayAccount.getOauthType(), mUmipayAccount);
		getActivity().finish();
	}

	@Override
	protected void handleOnClick(View v) {

		// 获取验证码
		if (v.equals(mGetCodeTv)) {
			getVerificateCode();
			return;
		}
		// 绑定&登录
		if (v.equals(mBindAndLoginBtn)) {
			bindAndLogin();
			return;
		}

		// 跳过
		if (v.equals(mSkipBtn)) {
			skipBind();
			return;
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (v.equals(mCodeEditor)) {
			bindAndLogin();
			return false;
		}
		return false;
	}

	@Override
	public void onVerificateSMS(Gson_Cmd_VerificateSMS gsonCmdVerificateSMS) {
		MsgData data = null;
		if (gsonCmdVerificateSMS != null) {
			int code = gsonCmdVerificateSMS.getCode();
			String msg = gsonCmdVerificateSMS.getMessage();
			data = new MsgData(code, msg, null);
		}
		sendMessage(MSG_VERIFICATESMS, data);
	}

	@Override
	protected void handleVerificateSMSMsg(MsgData data) {
		stopProgressDialog();
		try {
			int code = data.getCode();
			String msg = data.getMsg();
			Debug_Log.d("error code : " + code);
			if (mCurrentVerificateType == Interface_Verificate_SMS_Listener.TYPE_SEND_SMS) {
				if (code == UmipaySDKStatusCode.SUCCESS) {
					mGetCodeTv.setEnabled(false);
					mCountdownText = GET_VERIFICATE_CODE_CD;
					mCountdownTimer = new Timer();
					mCountdownTimer.schedule(new TimerTask() {
						                         @Override
						                         public void run() {
							                         mTimerHandler.sendEmptyMessage(MSG_UPDATE_COUNTDOWN_BTN);
						                         }
					                         }, Global_Final_Common_Millisecond.oneSecond_ms,
							Global_Final_Common_Millisecond.oneSecond_ms
					);
				} else {
					msg = UmipaySDKStatusCode.handlerMessage(code, msg);
					toast("获取验证码失败(" + msg + ")");
				}
			}
			if (mCurrentVerificateType == Interface_Verificate_SMS_Listener.TYPE_VERIFICATE_SMS) {
				if (code == UmipaySDKStatusCode.SUCCESS) {
					sendLoginResultMsg(code, null, mUmipayAccount.getOauthType(), mUmipayAccount);
					if(SDKCacheConfig.getInstance(getActivity()).isShowBoard()) {
						replaceFragmentFromActivityFragmentManager(UmipayAnnouncementFragment.newInstance());
					}else {
						getActivity().finish();
					}
				} else {
					msg = UmipaySDKStatusCode.handlerMessage(code, msg);
					toast("验证验证码失败(" + msg + ")");
				}
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
			toast("验证验证码失败");
		}
	}
}
