package net.ouwan.umipay.android.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
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
import net.ouwan.umipay.android.entry.gson.Gson_Login;
import net.ouwan.umipay.android.interfaces.Interface_Account_Listener_Login;
import net.ouwan.umipay.android.manager.ListenerManager;
import net.ouwan.umipay.android.manager.UmipayAccountManager;
import net.ouwan.umipay.android.manager.UmipayCommandTaskManager;
import net.ouwan.umipay.android.view.PopupWindowAdapter;
import net.ouwan.umipay.android.view.UmipayLoginInfoDialog;
import net.ouwan.umipay.android.weibo.Weibo;
import net.ouwan.umipay.android.weibo.WeiboAuthListener;
import net.ouwan.umipay.android.weibo.WeiboDialogError;
import net.ouwan.umipay.android.weibo.WeiboException;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.common.global.Global_Final_Common_Millisecond;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

/**
 * Created by jimmy on 2016/8/14.
 */
public class LoginFragment extends BaseFragment implements TextView.OnEditorActionListener,
		Interface_Account_Listener_Login, Handler.Callback {
	private Tencent mTencent;
	private Weibo mWeibo;

	private SDKCacheConfig mCacheConfig;
	private TextView mTitleTv;

	private Button mLoginBtn;
	private View mOtherLoginTv;
	private View mSwitchLoginTypeBtn;
	private View mGoRegisterBtn;
	private View mAutoLoginLayout;
	private CheckBox mAutoLoginCheckBox;
	private CheckBox mOtherLoginCheckBox;
	private CheckBox mRememberPWCheckBox;
	private CheckBox mViewPswCheckBox;
	private View mClearAccountBtn;
	private View mSelectAccountBtn;
	private View mOtherLoginLayout;
	private View mQQOauth;
	private View mSinaOauth;
	private View mTrialIcon;
	private View mForgetPswTextView;
	private View mViewPswLayout;
	private EditText mAccountEditor;
	private EditText mPswEditor;
	private PopupWindowAdapter mPopupWindowAdapter;


	private ListView mAccountListView;
	private ArrayList<UmipayAccount> mAccountList;
	private View mClearPswBtn;
	private View mRememberPWLayout;
	private PopupWindow mPopupWindow;
	private Handler mHandler = new Handler(this);
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

	public static LoginFragment newInstance() {
		LoginFragment fragment = new LoginFragment();
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
					"umipay_login_layout")

					, container, false);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		initViews();
		initListener();
		initPopupWindow();
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
			mGoRegisterBtn = mRootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
					"umipay_login_to_register_tv"));
			mRememberPWLayout = mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_remember_pw_layout"));
			mAutoLoginLayout = mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_autologin_layout"));
			mAutoLoginCheckBox = (CheckBox) mRootLayout
					.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
							"umipay_autologin_cb"));
			mRememberPWCheckBox = (CheckBox) mRootLayout
					.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
							"umipay_remember_pw_cb"));
			mOtherLoginCheckBox = (CheckBox) mRootLayout
					.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
							"umipay_other_login_btn"));
			mOtherLoginLayout = mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_other_login_layout"));
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

			mForgetPswTextView = mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_forget_psw_tv"));

			mAccountEditor = (EditText) mRootLayout
					.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
							"umipay_name_box"));
			mPswEditor = (EditText) mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_psw_box"));

			mSelectAccountBtn = mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_account_select_btn"));

			mClearAccountBtn = mRootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
					"umipay_reg_name_clear_btn"));
			mClearPswBtn = mRootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
					"umipay_reg_psw_clear_btn"));
			mViewPswLayout = mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_psw_cb_layout"));
			mViewPswCheckBox = (CheckBox) mRootLayout
					.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
							"umipay_psw_cb"));
		}
		// 初始化第三方登录是否显示
		if (mOtherLoginCheckBox != null && mOtherLoginLayout != null && mCacheConfig != null) {
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

		if (mLoginBtn != null) {
			mLoginBtn.setEnabled(true);
		}

		UmipayAccount account = null;
		try {
			account = UmipayAccountManager.getInstance(getActivity()).getFirstNormalAccount();
		} catch (Exception e) {
			Debug_Log.e(e);
		}
		if (mAccountEditor != null) {
			if (account != null) {
				mAccountEditor.setText(account.getUserName());
				mAccountEditor.setSelection(mAccountEditor.getText().length());
			}
		}
		if (mPswEditor != null) {
			if (account != null) {
				mPswEditor.setText(account.getPsw());
				mPswEditor.setSelection(mPswEditor.getText().length());
			}
			mPswEditor.setTransformationMethod(PasswordTransformationMethod
					.getInstance());
			mPswEditor.setOnEditorActionListener(this);
			initPswFilter();
		}
		if (mSelectAccountBtn != null) {
			mSelectAccountBtn.setVisibility(View.VISIBLE);
		}

		if (mAutoLoginCheckBox != null && mCacheConfig != null) {
			mAutoLoginCheckBox.setChecked(mCacheConfig.isAutoLogin());
			mAutoLoginCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mCacheConfig.setAutoLogin(isChecked);
					mCacheConfig.save();
				}
			});
		}
		if (mRememberPWCheckBox != null) {
			if (account != null && account.isRemenberPsw()) {
				mRememberPWCheckBox.setChecked(true);
			} else {
				mRememberPWCheckBox.setChecked(false);
			}

			//无论轻sdk还是正常sdk,自动登陆选上则记住密码必定选上
			if (mAutoLoginCheckBox != null && mAutoLoginCheckBox.isChecked()) {
				mRememberPWCheckBox.setChecked(true);
			}
		}

		if (mTitleTv != null) {
			mTitleTv.setText(getActivity().getResources().getString(Util_Resource.getIdByReflection(getActivity(),
					"string",
					"umipay_titile_login")));
		}
		if (mCacheConfig != null) {
			mCacheConfig.setMobileLogin(false);
			mCacheConfig.save();
		}
	}

	private void initListener() {
		//切换登陆方式按钮
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
		// 记住密码lo
		if (mRememberPWLayout != null) {
			mRememberPWLayout.setOnClickListener(this);
		}
		// 自动登陆lo
		if (mAutoLoginLayout != null) {
			mAutoLoginLayout.setOnClickListener(this);
		}
		// 显示密码
		if (mViewPswLayout != null) {
			mViewPswLayout.setOnClickListener(this);
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
		// 选择账号btn
		if (mSelectAccountBtn != null) {
			mSelectAccountBtn.setOnClickListener(this);
		}
		if (mClearAccountBtn != null) {
			mClearAccountBtn.setOnClickListener(this);
		}

		if (mClearPswBtn != null) {
			mClearPswBtn.setOnClickListener(this);
		}

		if (mForgetPswTextView != null) {
			mForgetPswTextView.setOnClickListener(this);
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
		sendMessage(MSG_LOGIN, new MsgData(code, msg, account));
	}

	private void login() {
		//正常账号登录
		String account = null;
		if (mAccountEditor != null) {
			account = mAccountEditor.getEditableText().toString();
		}
		String psw = null;
		if (mPswEditor != null) {
			psw = mPswEditor.getEditableText().toString();
		}
		boolean isRemember = false;
		if (mRememberPWCheckBox != null) {
			isRemember = mRememberPWCheckBox.isChecked();
		}
		if (TextUtils.isEmpty(account)) {
			toast("请输入偶玩通行证账号~");
			return;
		}
		if (TextUtils.isEmpty(psw)) {
			toast("请输入偶玩通行证密码~");
			return;
		}
		ListenerManager.setCommandLoginListener(this);
		UmipayCommandTaskManager.getInstance(getActivity()).LoginCommandTask(account, psw, isRemember);
		startProgressDialog();
	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);
		//隐藏账户下拉列表
		if (mPopupWindow != null) {
			mPopupWindow.dismiss();
		}
	}

	@Override
	protected void handleLoginMsg(MsgData data) {
		try {
			stopProgressDialog();
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
		if (v.equals(mPswEditor)) {
			login();
			return false;
		}
		return false;
	}

	@Override
	protected void handleOnClick(View v) {
		// 登录tab
		if (v.equals(mSwitchLoginTypeBtn)) {
			if (mOtherLoginCheckBox != null && mOtherLoginCheckBox.isChecked() && mOtherLoginLayout != null) {
				//界面切换收起第三方登陆
				mOtherLoginLayout.setVisibility(View.INVISIBLE);
				mOtherLoginCheckBox.setChecked(false);
			}
			replaceFragmentFromActivityFragmentManager(MobileLoginFragment.newInstance
					());
			return;
		}
		// 注册tab
		if (v.equals(mGoRegisterBtn)) {
			if (mOtherLoginCheckBox != null && mOtherLoginCheckBox.isChecked() && mOtherLoginLayout != null) {
				//界面切换收起第三方登陆
				mOtherLoginLayout.setVisibility(View.INVISIBLE);
				mOtherLoginCheckBox.setChecked(false);
			}
			//正常sdk部分直接跳转到注册页面
			replaceFragmentFromActivityFragmentManager(RegisterFragment.newInstance());
			return;
		}
		// 记住密码cb
		if (v.equals(mRememberPWLayout)) {
			mRememberPWCheckBox.setChecked(!mRememberPWCheckBox.isChecked());
			// 无论轻sdk还是正常sdk,取消记住密码同时取消自动登录
			if (!mRememberPWCheckBox.isChecked()
					&& mAutoLoginCheckBox != null && mAutoLoginCheckBox.isChecked()) {
				mAutoLoginCheckBox.setChecked(false);
			}
			return;
		}
		// 自动登录cb
		if (v.equals(mAutoLoginLayout)) {
			mAutoLoginCheckBox.setChecked(!mAutoLoginCheckBox.isChecked());
			if (mAutoLoginCheckBox.isChecked()
					&& mRememberPWCheckBox != null && !mRememberPWCheckBox.isChecked()) {
				mRememberPWCheckBox.setChecked(true);
			}
			return;
		}
		// 显示密码
		if (mViewPswLayout != null) {
			mViewPswLayout.setOnClickListener(this);
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
			//checkbox点击会自动切换
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
		if (v.equals(mQQOauth)) {
			mTencent.login(this, SDKConstantConfig.QQ_OAUTH_APPID, mTencentAuthCallBack);
			return;
		}
		// weibo登录
		if (v.equals(mSinaOauth)) {
			if (mWeibo != null) {
				mWeibo.login(getActivity(), mWeiboAuthListener);
			}
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
		// 选择账号btn
		if (v.equals(mSelectAccountBtn)) {
			if (mAccountList.size() > 0) {
				mPopupWindow.setWidth(mAccountEditor.getWidth());
				mPopupWindow.setHeight(mAccountEditor.getHeight() * 3);
				mPopupWindow.showAsDropDown(mAccountEditor, 0, 0);
			}
			return;
		}
		// 忘记密码
		if (v.equals(mForgetPswTextView)) {
			UmipaySDKManager.showRegetPswView(getActivity());
			return;
		}
		// 显示密码
		if (v.equals(mViewPswLayout)) {
			if (mViewPswCheckBox != null) {
				mViewPswCheckBox.setChecked(!mViewPswCheckBox.isChecked());
				// 密码可见
				if (mPswEditor != null) {
					mPswEditor.setTransformationMethod(mViewPswCheckBox.isChecked() ? null :
							PasswordTransformationMethod
							.getInstance());
					mPswEditor.setSelection(mPswEditor.getText().length());
				}
			}
			return;
		}

	}

	/**
	 * 初始化PopupWindow
	 */
	private void initPopupWindow() {
		try {
			mAccountList = (ArrayList<UmipayAccount>) UmipayAccountManager.getInstance(getActivity()
					.getApplicationContext())
					.getNormalAccountList();
			if (mAccountList != null && mAccountList.size() > 0) {
				// 设置自定义Adapter
				mPopupWindowAdapter = new PopupWindowAdapter(getActivity(), mHandler, mAccountList);
				// PopupWindow浮动下拉框布局
				View loginwindow = (View) getActivity().getLayoutInflater().inflate(Util_Resource.getIdByReflection
						(getActivity(), "layout",
								"umipay_account_list"), null);
				mAccountListView = (ListView) loginwindow.findViewById(Util_Resource.getIdByReflection(getActivity(),
						"id",

						"umipay_account_list"));
				mAccountListView.setAdapter(mPopupWindowAdapter);
				mPopupWindow = new PopupWindow(loginwindow, LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT, true);
				mPopupWindow.setOutsideTouchable(true);
				//避免软键盘和下拉菜单同时存在时错位
				mPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
				// 这一句是为了实现弹出PopupWindow后，当点击屏幕其他部分及Back键时PopupWindow会消失，
				mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x000000));

			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	/**
	 * @Title: initPswFilter
	 * @Description:只能输入非空格的ASCII码
	 */
	private void initPswFilter() {
		if (mPswEditor != null) {
			mPswEditor.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					String editable = mPswEditor.getText().toString();
					String str = stringFilter(editable);
					if (!editable.equals(str)) {
						mPswEditor.setText(str);
						// 设置新的光标所在位置
						mPswEditor.setSelection(str.length());
					}

					if (mLoginBtn != null) {
						if (mAccountEditor.getText().length() > 0
								&& mPswEditor.getText().length() > 0) {
							mLoginBtn.setEnabled(true);
						} else {
							mLoginBtn.setEnabled(false);
						}
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
					if (TextUtils.isEmpty(s.toString())) {
						if (mClearPswBtn != null) {
							mClearPswBtn.setVisibility(View.GONE);
						}
					} else {
						if (mClearPswBtn != null) {
							mClearPswBtn.setVisibility(View.VISIBLE);
						}
					}
				}


			});
		}
		if (mAccountEditor != null) {
			mAccountEditor.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {

				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					if (mLoginBtn != null) {
						if (mAccountEditor.getText().length() > 0
								&& mPswEditor.getText().length() > 0) {
							mLoginBtn.setEnabled(true);
						} else {
							mLoginBtn.setEnabled(false);
						}
					} else {
						String editable = mAccountEditor.getText().toString();
						String str = stringFilter(editable);
						if (!editable.equals(str)) {
							mAccountEditor.setText(str);
							// 设置新的光标所在位置
							mAccountEditor.setSelection(str.length());
						}
					}
				}

				@Override
				public void afterTextChanged(Editable s) {
					String userName = s.toString();
					if (!TextUtils.isEmpty(userName)) {
						UmipayAccount umipayAccount = UmipayAccountManager
								.getInstance(getActivity().getApplicationContext())
								.getAccountByUserName(userName);
						if (null != umipayAccount && umipayAccount.isRemenberPsw()) {
							mPswEditor.setText(umipayAccount.getPsw());
							mRememberPWCheckBox.setChecked(true);
							return;
						}
					}
					mPswEditor.setText("");
				}
			});
		}

	}

	private String stringFilter(String text) throws PatternSyntaxException {
		String returnstr = "";
		for (char c : text.toCharArray()) {
			if (c > 32 & c <= 126) {
				returnstr += c;
			}
		}
		return returnstr;
	}

	/**
	 * 点击选择账号
	 */
	private void accountSelect(int index) {
		if (mAccountEditor != null) {
			mAccountEditor.setText(mAccountList.get(index).getUserName());
			mAccountEditor.setSelection(mAccountEditor.getText().length());
		}
		if (mPswEditor != null) {
			mPswEditor.setText(mAccountList.get(index).getPsw());
			mPswEditor.setSelection(mPswEditor.getText().length());
		}
		if (mRememberPWCheckBox != null) {
			mRememberPWCheckBox.setChecked(mAccountList.get(index).isRemenberPsw());
		}
		if (mPopupWindow != null) {
			mPopupWindow.dismiss();
		}
	}

	/**
	 * 点击删除账号
	 */
	private void accountDelete(final int index) {
		final UmipayAccount deleteAccount = mAccountList.get(index);
		new AlertDialog.Builder(getActivity())
				.setTitle("账号删除")
				.setMessage("确定删除账号" + deleteAccount.getUserName() + "？")
				.setNegativeButton("取消", null)
				.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								//删除的账号是不是判断上次登陆账号
								if (deleteAccount.getUserName().equals(mAccountEditor
										.getText().toString())) {
									mAccountEditor.setText("");
									mPswEditor.setText("");
									mAccountEditor.setSelection(mAccountEditor.getText().length());
									mPswEditor.setSelection(mPswEditor.getText().length());
								}
								//删除账号
								UmipayAccountManager.getInstance(getActivity().getApplication()).deleteAccount
										(deleteAccount);
								mAccountList.remove(index);
								//刷新下拉列表
								mPopupWindowAdapter.notifyDataSetChanged();
								toast("删除成功");

								UmipayCommandTaskManager.getInstance(getActivity()).DeleteAccountCommandTask
										(deleteAccount
												.getUserName());

							}
						}
				).show();
	}

	@Override
	public boolean handleMessage(Message message) {
		if (message != null) {
			Bundle data = message.getData();
			switch (message.what) {
				case 1:
					accountSelect(data.getInt(PopupWindowAdapter.SELECT_KEY));
					break;
				case 2:
					accountDelete(data.getInt(PopupWindowAdapter.DELETE_KEY));
					break;
			}
		}
		return false;
	}
}
