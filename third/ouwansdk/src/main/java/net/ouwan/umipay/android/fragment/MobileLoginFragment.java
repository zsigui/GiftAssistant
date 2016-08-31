package net.ouwan.umipay.android.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import net.ouwan.umipay.android.Utils.Util_Resource;
import net.ouwan.umipay.android.Utils.VisitorUtil;
import net.ouwan.umipay.android.api.UmipaySDKManager;
import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.asynctask.TaskCMD;
import net.ouwan.umipay.android.config.SDKCacheConfig;
import net.ouwan.umipay.android.config.SDKConstantConfig;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.ouwan.umipay.android.entry.Visitor;
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_Mobile_Login_GetAccountList;
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_Mobile_Login_GetCode;
import net.ouwan.umipay.android.entry.gson.Gson_Login;
import net.ouwan.umipay.android.interfaces.Interface_Account_Listener_GetAccountList;
import net.ouwan.umipay.android.interfaces.Interface_Account_Listener_Login;
import net.ouwan.umipay.android.interfaces.Interface_Mobile_Login_Verificate_Code_Listener;
import net.ouwan.umipay.android.manager.ListenerManager;
import net.ouwan.umipay.android.manager.UmipayCommandTaskManager;
import net.ouwan.umipay.android.view.UmipayLoginInfoDialog;
import net.ouwan.umipay.android.weibo.Weibo;
import net.ouwan.umipay.android.weibo.WeiboAuthListener;
import net.ouwan.umipay.android.weibo.WeiboDialogError;
import net.ouwan.umipay.android.weibo.WeiboException;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.common.global.Global_Final_Common_Millisecond;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jimmy on 2016/8/14.
 */
public class MobileLoginFragment extends BaseFragment implements TextView.OnEditorActionListener,
		Interface_Account_Listener_Login, Interface_Account_Listener_GetAccountList,
		Interface_Mobile_Login_Verificate_Code_Listener {
	public static final String VERIFICATE_SMS = "sms";
	public static final String VERIFICATE_VOICE = "voice";

	private String mVerificateType = VERIFICATE_SMS;

	private Tencent mTencent;
	private Weibo mWeibo;

	private SDKCacheConfig mCacheConfig;

	private TextView mTitleTv;
	private TextView mSwitchGetCodeTypeTv;

	private Button mLoginBtn;
	private View mSwitchLoginTypeBtn;
	private View mGoRegisterBtn;
	private View mAutoLoginLayout;
	private CheckBox mAutoLoginCheckBox;
	private CheckBox mGetVoiceCheckBox;

	private CheckBox mOtherLoginCheckBox;
	private View mOtherLoginTv;
	private View mOtherLoginLayout;
	private View mQQOauth;
	private View mSinaOauth;
	private View mTrialIcon;
	private View mGetVoiceCodeLayout;

	private EditText mPhoneEditor;
	private EditText mCodeEditor;
	private TextView mGetCodeTv;

	private Handler mTimerHandler;
	private Timer mCountdownTimer;
	private int mCountdownText;
	private static final int MSG_UPDATE_COUNTDOWN_BTN = 1;
	private static final int GET_VERIFICATE_CODE_CD = 60;

	/**
	 * ************************
	 * <p/>
	 * qq登录授权回调
	 * <p/>
	 * **************************
	 */
	private IUiListener mTencentAuthCallBack = new IUiListener() {
		@Override
		public void onComplete(Object o) {
			if (o instanceof JSONObject) {
				JSONObject arg0 = (JSONObject) o;
				Debug_Log.dd("QQ OauthLogin onComplete");
				String openid = Basic_JSONUtil.getString(arg0, "openid", null);
				String access_token = Basic_JSONUtil.getString(arg0,
						"access_token", null);
				String expires_in_str = Basic_JSONUtil.getString(arg0,
						"expires_in", "0");
				String authdata = null;
				int expire = 1000;
				try {
					authdata = o.toString();
					expire = Integer.valueOf(expires_in_str);
				} catch (Throwable e) {
					Debug_Log.e(e);
				}
				oauthLogin(UmipayAccount.TYPE_QQ, openid, access_token, expire, authdata);
			}
		}

		@Override
		public void onError(UiError arg0) {
			Debug_Log.d("QQ Auth Error:" + arg0.errorMessage);
		}

		@Override
		public void onCancel() {
			Debug_Log.d("QQ Auth Cancel");
		}
	};
	/**
	 * ************************
	 * <p/>
	 * 微博授权回调
	 * <p/>
	 * **************************
	 */
	private WeiboAuthListener mWeiboAuthListener = new WeiboAuthListener() {

		@Override
		public void onComplete(Bundle values) {
			Debug_Log.d("Sina Auth Complete");
			String uid = null;
			String access_token = null;
			String expires_in_str = null;
			String authdata = null;
			int expire = 1000;
			try {
				authdata = values.toString();
				uid = values.getString("uid");
				access_token = values.getString("access_token");
				expires_in_str = values.getString("expires_in");
				expire = Integer.valueOf(expires_in_str);
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
			String log = String.format("Sina Auth uid:%s,access_token：%s", uid,
					access_token);
			Debug_Log.d(log);
			oauthLogin(UmipayAccount.TYPE_SINA, uid, access_token, expire, authdata);

		}

		@Override
		public void onWeiboException(WeiboException arg0) {
			Debug_Log.d("Sina Auth Exception:" + arg0.getMessage());
		}

		@Override
		public void onError(WeiboDialogError arg0) {
			Debug_Log.d("Sina Auth Error:" + arg0.getMessage());
		}

		@Override
		public void onCancel() {
			Debug_Log.d("Sina Auth Cancel");
		}
	};

	public static MobileLoginFragment newInstance() {
		MobileLoginFragment fragment = new MobileLoginFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mCacheConfig = SDKCacheConfig.getInstance(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		try {
			mRootLayout = inflater.inflate(Util_Resource.getIdByReflection(getActivity(), "layout",
					"umipay_mobile_login_layout"), container, false);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		initViews();
		initListener();
		initHandler();
		return mRootLayout;
	}

	private void oauthLogin(int type, String openid, String token, int expire, String authdata) {
		ListenerManager.setCommandLoginListener(this);
		UmipayCommandTaskManager.getInstance(getActivity()).OauthLoginCommandTask(type, openid, token, expire,
				authdata);
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

	private void initViews() {
		mTencent = Tencent.createInstance(SDKConstantConfig.QQ_OAUTH_APPID, getActivity());
		mWeibo = Weibo.getInstance(SDKConstantConfig.SINA_OAUTH_APPID, SDKConstantConfig.SINA_REDIRECT_URL);

		if (mRootLayout != null) {
			mTitleTv = (TextView) mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_title_tv"));
			mSwitchGetCodeTypeTv = (TextView) mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_switch_getcode_ttv"));
			mGoRegisterBtn = mRootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
					"umipay_login_to_register_tv"));
			mAutoLoginLayout = mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_autologin_layout"));
			mAutoLoginCheckBox = (CheckBox) mRootLayout
					.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
							"umipay_autologin_cb"));
			mOtherLoginCheckBox = (CheckBox) mRootLayout
					.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
							"umipay_other_login_btn"));
			mGetVoiceCheckBox = (CheckBox) mRootLayout
					.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
							"umipay_getvoice_code_cb"));
			mOtherLoginLayout = mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_other_login_layout"));
			mGetVoiceCodeLayout = mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_getvoice_code_ll"));
			mOtherLoginTv = mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_other_login_tv"));
			mSwitchLoginTypeBtn = mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_switch_login_type_btn"));
			mLoginBtn = (Button) mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_login_btn"));
			mQQOauth = mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_qq_oauth"));
			mSinaOauth = mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_sina_oauth"));
			mTrialIcon = mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_trial_imageview")); // 试玩图标
//		mClearPhoneBtn = rootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
//				"umipay_reg_phone_clear_btn"));


			mPhoneEditor = (EditText) mRootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
					"umipay_phone_box"));
			mCodeEditor = (EditText) mRootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
					"umipay_code_box"));
			mGetCodeTv = (TextView) mRootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
					"umipay_get_code_btn"));
		}
		initVerificateType();
		if (mTitleTv != null) {
			mTitleTv.setText(getActivity().getResources().getString(Util_Resource.getIdByReflection(getActivity(),
					"string",
					"umipay_titile_mobile_login")));
		}
		// 初始化第三方登录是否显示
		if (mOtherLoginCheckBox != null && mOtherLoginLayout != null) {
			// 检查用户配置是否显示第三方登录
			mOtherLoginCheckBox.setChecked(mCacheConfig.isEnableOtherLogin());
			// 根据用户配置选择初始时显示或不显示第三方登录，去除动画效果
			if (mOtherLoginCheckBox.isChecked()) {
				mOtherLoginLayout.setVisibility(View.VISIBLE);
			} else {
				mOtherLoginLayout.setVisibility(View.INVISIBLE);
			}
			// 监听第三方登录的mOtherLoginCheckBox是否有改变，改变时更改用户配置
			mOtherLoginCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mCacheConfig.setEnableOtherLogin(isChecked);
				}
			});

		}
		if (mGetVoiceCheckBox != null) {
			mGetVoiceCheckBox.setChecked(false);
			mGetVoiceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mVerificateType = (mGetVoiceCheckBox.isChecked() ? VERIFICATE_VOICE : VERIFICATE_SMS);
					initVerificateType();
				}
			});
		}
		if (mAutoLoginCheckBox != null) {
			mAutoLoginCheckBox.setChecked(mCacheConfig.isAutoLogin());
			mAutoLoginCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mCacheConfig.setAutoLogin(isChecked);
					mCacheConfig.save();
				}
			});
		}
		if (mLoginBtn != null) {
			mLoginBtn.setEnabled(true);
		}
		if (mGetCodeTv != null) {
			mGetCodeTv.setText("获取验证码");
			mGetCodeTv.setEnabled(true);
		}
		if (mCodeEditor != null) {
			mCodeEditor.setOnEditorActionListener(this);
		}
		if (mCacheConfig != null) {
			mCacheConfig.setMobileLogin(true);
			mCacheConfig.save();
		}
	}

	private void initListener() {
		//切换登陆方式按钮
		if(mOtherLoginTv != null){
			mOtherLoginTv.setOnClickListener(this);
		}
		if (mSwitchLoginTypeBtn != null) {
			mSwitchLoginTypeBtn.setOnClickListener(this);
		}
		//前往注册页
		if (mGoRegisterBtn != null) {
			mGoRegisterBtn.setOnClickListener(this);
		}
		// 自动登陆lo
		if (mAutoLoginLayout != null) {
			mAutoLoginLayout.setOnClickListener(this);
		}
		// 第三方登录cb
		if (mOtherLoginCheckBox != null) {
			mOtherLoginCheckBox.setOnClickListener(this);
		}
		// qq登录
		if (mQQOauth != null) {
			mQQOauth.setOnClickListener(this);
		}
		// weibo登录
		if (mSinaOauth != null) {
			mSinaOauth.setOnClickListener(this);
		}
		// 试玩图标
		if (mTrialIcon != null) {
			mTrialIcon.setOnClickListener(this);
		}
		// 登录btn
		if (mLoginBtn != null) {
			mLoginBtn.setOnClickListener(this);
		}
		// 获取验证码
		if (mGetCodeTv != null) {
			mGetCodeTv.setOnClickListener(this);
		}

		//获取语音验证码
		if (mGetVoiceCodeLayout != null) {
			mGetVoiceCodeLayout.setOnClickListener(this);
		}

//		if (mClearPhoneBtn != null) {
//			mClearPhoneBtn.setOnClickListener(this);
//		}
	}

	private void initHandler() {
		mTimerHandler = new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
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
								mGetCodeTv.setText("重获验证码");
								mGetCodeTv.setEnabled(true);
							}
						}
						break;
				}
				return true;
			}
		});
	}


	@Override
	protected void handleGetAccountListMsg(MsgData data) {
		stopProgressDialog();
		try {
			if (data.getCode() == UmipaySDKStatusCode.SUCCESS) {
				ArrayList<Gson_Cmd_Mobile_Login_GetAccountList.Cmd_Account_List_Item_Data> selectAccountList =
						(ArrayList<Gson_Cmd_Mobile_Login_GetAccountList.Cmd_Account_List_Item_Data>) data
								.getData();
				if (selectAccountList != null && selectAccountList.size() > 0) {
					if (selectAccountList.size() == 1) {
						//只有一个账号直接登陆
						ListenerManager.setCommandLoginListener(this);
						String username = selectAccountList.get(0).getUsername();
						String mobile = selectAccountList.get(0).getMobile();
						String calling_code = selectAccountList.get(0).getCallingCode();
						int uid = selectAccountList.get(0).getUid();
						int ts = selectAccountList.get(0).getTs();
						UmipayCommandTaskManager.getInstance(getActivity()).MobileLoginCommandTask(calling_code,
								mobile, uid, ts);
					} else {
						//多个账号时跳转到设置主账号界面
						SelectAccountFragment selectAccountFragment = SelectAccountFragment.newInstance();
						selectAccountFragment.setSelectAccountList(selectAccountList);
						replaceFragmentFromActivityFragmentManager(selectAccountFragment);

					}
				} else {
					toast("账号列表获取失败");
				}
			} else {
				String msg = UmipaySDKStatusCode.handlerMessage(data.getCode(), data.getMsg());
				toast(msg + "(" + data.getCode() + ")");
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		//QQ
		if (requestCode == Constants.REQUEST_LOGIN && mTencent != null) {
			mTencent.handleLoginData(data, mTencentAuthCallBack);
		}
		super.onActivityResult(requestCode, resultCode, data);
		/**
		 * 下面两个注释掉的代码，仅当sdk支持sso时有效
		 * 没啥用 2015-04-30
		 */
		if (mTencent != null) {
			mTencent.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onLogin(int code, String msg, UmipayAccount account) {
		sendMessage(MSG_LOGIN_MOBILE, new MsgData(code, msg, account));
	}

	private void login() {
		//手机验证码登录
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
		ListenerManager.setCommandGetAccountListListener(this);
		UmipayCommandTaskManager.getInstance(getActivity()).MobileLoginGetAccountListCommandTask(null, mobile, code);
		startProgressDialog();
	}

	// 获取验证码
	private void getMobileLoginVerificateCode() {
		String mobile = null;
		if (mPhoneEditor != null) {
			mobile = mPhoneEditor.getText().toString();
		}
		if (TextUtils.isEmpty(mobile) || mobile.length() != 11 || mobile.charAt(0) != '1') {
			toast("请输入正确的手机号码");
			return;
		}
		ListenerManager.setCommandMobileLoginVerificateSMSListener(this);
		UmipayCommandTaskManager.getInstance(getActivity()).MobileLoginGetSMSCommandTask(mobile,
				null, mVerificateType);
		startProgressDialog();
	}

	protected void handleMobileLoginVerificateCodeMsg(MsgData data) {
		stopProgressDialog();
		try {
			int code = data.getCode();
			String msg = data.getMsg();
			Debug_Log.d("error code : " + code);
			if (code == UmipaySDKStatusCode.SUCCESS) {
				if (mGetVoiceCheckBox.isChecked()) {
					toast("请接听电话");
				}
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
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	private void initVerificateType() {
		String verifyTypeTips = getActivity().getResources().getString(Util_Resource.getIdByReflection(getActivity(),
				"string",
				"umipay_verificate_voice_tips"));
		mVerificateType = VERIFICATE_SMS;
		if (mGetVoiceCheckBox != null && mGetVoiceCheckBox.isChecked()) {
			verifyTypeTips = getActivity().getResources().getString(Util_Resource.getIdByReflection(getActivity(),
					"string",
					"umipay_verificate_sms_tips"));
			mVerificateType = VERIFICATE_VOICE;
		}
		SpannableStringBuilder style = new SpannableStringBuilder(verifyTypeTips);
		if (style.length() > 4) {

			style.setSpan(new ForegroundColorSpan(getActivity().getResources().getColor(Util_Resource
					.getIdByReflection(getActivity(),
							"color",
							"umipay_dodger_blue"))), style.length() - 4, style.length(), Spannable
					.SPAN_EXCLUSIVE_EXCLUSIVE);
			if (mSwitchGetCodeTypeTv != null) {
				mSwitchGetCodeTypeTv.setText(style);
			}
		}
		mCountdownText = -1;
		if (mGetCodeTv != null) {
			String btnText = VERIFICATE_VOICE.equals(mVerificateType)?"重获语音验证码":"重获验证码";
			mGetCodeTv.setText(btnText);
		}
		if (mCodeEditor != null) {
			String hintText = "";
			hintText = VERIFICATE_VOICE.equals(mVerificateType)?getActivity().getResources().getString(Util_Resource
					.getIdByReflection(getActivity(),
							"string",
							"umipay_get_voice_code_hint")):getActivity().getResources().getString(Util_Resource
					.getIdByReflection(getActivity(),
							"string",
							"umipay_bindmobile_code_hint"));
			mCodeEditor.setHint(hintText);
		}
	}

	@Override
	protected void handleMobileLoginMsg(MsgData data) {
		stopProgressDialog();
		try {
			if (data.getCode() == UmipaySDKStatusCode.SUCCESS) {
				UmipayAccount account = (UmipayAccount) data.getData();
				try {
					if (account.getBindOauth() != 0) {
						replaceFragmentFromActivityFragmentManager(BindOauthFragment
								.newInstance(account));
					} else if (account.getBindMobile() != 0) {
						replaceFragmentFromActivityFragmentManager(BindMobileFragment
								.newInstance(account));
					} else {
						sendLoginResultMsg(UmipaySDKStatusCode.SUCCESS, null, UmipayLoginInfoDialog.NORMAL_LOGIN,
								account);
						getActivity().finish();
					}
				} catch (Throwable e) {
					Debug_Log.e(e);
				}
			} else {
				String msg = UmipaySDKStatusCode.handlerMessage(data.getCode(), data.getMsg());
				toast(msg + "(" + data.getCode() + ")");
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId != EditorInfo.IME_ACTION_DONE) {
			return false;
		}

		if (v.equals(mCodeEditor)) {
			login();
			return false;
		}
		return false;
	}

	@Override
	protected void handleOnClick(View v) {
		// 登录tab
		if (v.equals(mSwitchLoginTypeBtn)) {
			if (mOtherLoginCheckBox != null && mOtherLoginCheckBox.isChecked()) {
				//界面切换收起第三方登陆
				mOtherLoginLayout.setVisibility(View.INVISIBLE);
				mOtherLoginLayout.startAnimation(AnimationUtils.loadAnimation(
						getActivity(), Util_Resource.getIdByReflection(getActivity(), "anim",
								"umipay_other_login_hide")));
				mOtherLoginCheckBox.setChecked(false);
			}
			replaceFragmentFromActivityFragmentManager(LoginFragment.newInstance());

			return;
		}
		// 注册tab
		if (v.equals(mGoRegisterBtn)) {
			if (mOtherLoginCheckBox != null && mOtherLoginCheckBox.isChecked()) {
				//界面切换收起第三方登陆
				mOtherLoginLayout.setVisibility(View.INVISIBLE);
				mOtherLoginCheckBox.setChecked(false);
			}
			//正常sdk部分直接跳转到注册页面
			replaceFragmentFromActivityFragmentManager(RegisterFragment.newInstance());
			return;
		}
		// 自动登录cb
		if (v.equals(mAutoLoginLayout)) {
			if (mAutoLoginCheckBox != null) {
				mAutoLoginCheckBox.setChecked(!mAutoLoginCheckBox.isChecked());
			}
			return;
		}
		// 第三方登录

		if(v.equals(mOtherLoginTv) && mOtherLoginCheckBox != null){

			//此时checkbox未改
			if (!mOtherLoginCheckBox.isChecked()) {
				mOtherLoginLayout.setVisibility(View.VISIBLE);
				mOtherLoginLayout.startAnimation(AnimationUtils.loadAnimation(
						getActivity(), Util_Resource.getIdByReflection(getActivity(), "anim",
								"umipay_other_login_show")
				));
				mOtherLoginCheckBox.setChecked(true);
			} else {
				mOtherLoginLayout.setVisibility(View.INVISIBLE);
				mOtherLoginLayout.startAnimation(AnimationUtils.loadAnimation(
						getActivity(), Util_Resource.getIdByReflection(getActivity(), "anim",
								"umipay_other_login_hide")
				));
				mOtherLoginCheckBox.setChecked(false);
			}
		}
		if (v.equals(mOtherLoginCheckBox) ) {
			if (mOtherLoginLayout != null) {
				if (mOtherLoginCheckBox.isChecked()) {
					mOtherLoginLayout.setVisibility(View.VISIBLE);
					mOtherLoginLayout.startAnimation(AnimationUtils.loadAnimation(
							getActivity(), Util_Resource.getIdByReflection(getActivity(), "anim",
									"umipay_other_login_show")
					));
				} else {
					mOtherLoginLayout.setVisibility(View.INVISIBLE);
					mOtherLoginLayout.startAnimation(AnimationUtils.loadAnimation(
							getActivity(), Util_Resource.getIdByReflection(getActivity(), "anim",
									"umipay_other_login_hide")
					));
				}
			}
			return;
		}
		// 登录btn
		if (v.equals(mLoginBtn)) {
			login();
			return;
		}
		// QQ登录
		if (v.equals(mQQOauth) && mTencent != null) {
			mTencent.login(this, SDKConstantConfig.QQ_OAUTH_APPID, mTencentAuthCallBack);
			return;
		}
		// weibo登录
		if (v.equals(mSinaOauth) && mWeibo != null) {
			mWeibo.login(getActivity(), mWeiboAuthListener);
			return;
		}
		// 试玩图标
		if (v.equals(mTrialIcon)) {
			int expire = (int) (Global_Final_Common_Millisecond.oneDay_ms / 1000);
			VisitorUtil visitorUtil = new VisitorUtil(getActivity());
			Visitor visitor = visitorUtil.getVisitorAccount();
			oauthLogin(UmipayAccount.TYPE_VISITOR, visitor.getAccount(),
					visitor.getToken(), expire, null);
			return;
		}
		// 获取验证码
		if (v.equals(mGetCodeTv)) {
			getMobileLoginVerificateCode();
			return;
		}

		//切换获取验证码方式
		if (v.equals(mGetVoiceCodeLayout)) {
			if (mGetVoiceCheckBox != null) {
				mGetVoiceCheckBox.setChecked(!mGetVoiceCheckBox.isChecked());
			}
			initVerificateType();
			return;
		}
	}

	@Override
	public void onGetAccountList(int code, String msg, List accountList) {
		sendMessage(MSG_MOBILE_LOGIN_GETACCOUNTLIST, new MsgData(code, msg, accountList));
	}

	@Override
	public void onMobileLoginGetCode(Gson_Cmd_Mobile_Login_GetCode gsonCmdMobileLoginVerificateCode) {
		MsgData data = null;
		try {
			int code = gsonCmdMobileLoginVerificateCode.getCode();
			String msg = gsonCmdMobileLoginVerificateCode.getMessage();
			data = new MsgData(code, msg, null);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		sendMessage(MSG_MOBILE_LOGIN_VERIFICATESMS, data);
	}
}
