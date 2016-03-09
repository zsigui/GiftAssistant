package net.ouwan.umipay.android.api;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import net.ouwan.umipay.android.Utils.Util_Resource;
import net.ouwan.umipay.android.Utils.Util_ScreenShot;
import net.ouwan.umipay.android.Utils.VisitorUtil;
import net.ouwan.umipay.android.asynctask.TaskCMD;
import net.ouwan.umipay.android.config.SDKCacheConfig;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.ouwan.umipay.android.entry.Visitor;
import net.ouwan.umipay.android.entry.gson.Gson_Cmd_VerificateSMS;
import net.ouwan.umipay.android.entry.gson.Gson_Login;
import net.ouwan.umipay.android.interfaces.Interface_Account_Listener_Login;
import net.ouwan.umipay.android.interfaces.Interface_Account_Listener_Register;
import net.ouwan.umipay.android.interfaces.Interface_Verificate_SMS_Listener;
import net.ouwan.umipay.android.weibo.WeiboAuthListener;
import net.ouwan.umipay.android.manager.ListenerManager;
import net.ouwan.umipay.android.manager.UmipayAccountManager;
import net.ouwan.umipay.android.manager.UmipayCommandTaskManager;
import net.ouwan.umipay.android.view.PopupWindowAdapter;
import net.ouwan.umipay.android.view.UmipayLoginInfoDialog;
import net.ouwan.umipay.android.view.UmipayProgressDialog;
import net.ouwan.umipay.android.weibo.Weibo;
import net.ouwan.umipay.android.weibo.WeiboDialogError;
import net.ouwan.umipay.android.weibo.WeiboException;
import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.common.global.Global_Final_Common_Millisecond;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.PatternSyntaxException;

import static net.ouwan.umipay.android.config.SDKConstantConfig.*;

/**
 * UmipayActivity
 *
 * @author zacklpx
 *         date 15-3-6
 *         description
 */
public class UmipayActivity extends Activity implements TextView.OnEditorActionListener, View.OnClickListener,
		Handler.Callback, Interface_Verificate_SMS_Listener, Interface_Account_Listener_Login,
		Interface_Account_Listener_Register, WeiboAuthListener {
	private static final int MSG_LOGIN = 1;
	private static final int MSG_VERIFICATESMS = 2;
	private static final int MSG_REGIST = 3;
	private static final int MSG_QUICKREGIST = 4;

	private static final int LOGIN_VIEW = 1;
	private static final int REGISTER_VIEW = 2;
	private static final int BIND_MOBILE_VIEW = 3;
	private static final int REG_SUCCESS_View = 4;

	private static final int MSG_UPDATE_COUNTDOWN_BTN = 1;

	private static final int GET_VERIFICATE_CODE_CD = 60;

	private UmipayActivity mActivity;

	private SDKCacheConfig mCacheConfig;
	//当前界面
	private boolean mIsOnLoginView;
	private int mOnView;

	private Tencent mTencent;
	private Weibo mWeibo;
	private ViewFlipper mTabViewFlipper;
	private ViewFlipper mContectViewFlipper;
	private View mLoginTab;
	private View mRegisterTab;
	private View mRememberPWLayout;
	private View mAutoLoginLayout;
	private View mViewPswLayout;
	private CheckBox mRememberPWCheckBox;
	private CheckBox mAutoLoginCheckBox;
	private CheckBox mViewPswCheckBox;
	private CheckBox mOtherLoginCheckBox;
	private View mOtherLoginLayout;
	private Button mLoginBtn;
	private Button mQuickRegisterBtn;
	private EditText mAccountEditor;
	private EditText mPswEditor;
	private View mSelectAccountBtn;
	private View mQQOauth;
	private View mSinaOauth;
	private View mTrialIcon;
	private EditText mPhoneEditor;
	private Button mRegistBtn;
	private View mForgetPswTextView;
	private EditText mCodeEditor;
	private Button mSkipBtn;
	private Button mBindAndLoginBtn;
	private Button mGetCodeBtn;
	private View mAgreementTextView;

	private View mClearAccountBtn;
	private View mClearPswBtn;
	private View mClearPhoneBtn;

	private TextView mBindMobileTitleTV;

	private EditText mRegSuccessAccountEditor;
	private EditText mRegSuccessPswEditor;
	private Button mRegSuccessEnterGameBtn;

	private String mPhoneNum;
	private ArrayList<UmipayAccount> mAccountList;
	private PopupWindowAdapter mPopupWindowAdapter;
	private ListView mAccountListView;
	private PopupWindow mPopupWindow;
	private Handler mHandler = new Handler(UmipayActivity.this);

	private Handler mTimerHandler;
	private Timer mCountdownTimer;
	private int mCountdownText;

	private UmipayProgressDialog progressDialog;
	private UmipayAccount mLoginAccount;
	private int mLoginType;

	private InternalHandler mInternalHandler = new InternalHandler(Looper.getMainLooper());

	private int mCurrentVerificateType;

	private boolean canScreenShot = false;


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
			if(o instanceof  JSONObject) {
				JSONObject arg0 = (JSONObject)o;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = this;
		//以下设置会导致界面外区域，背后的界面触发点击事件
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
		mCacheConfig = SDKCacheConfig.getInstance(this);
		ListenerManager.setCommandLoginListener(this);

		//初始化界面
		initActivity();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//QQ
		if(requestCode == Constants.REQUEST_LOGIN && mTencent != null){
			mTencent.handleLoginData(data,mTencentAuthCallBack);
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

	private void initActivity() {

		mTencent = Tencent.createInstance(QQ_OAUTH_APPID, this);
		mWeibo = Weibo.getInstance(SINA_OAUTH_APPID, SINA_REDIRECT_URL);
		setContentView(Util_Resource.getIdByReflection(this, "layout", "umipay_main_dialog"));
		mTabViewFlipper = (ViewFlipper) findViewById(Util_Resource.getIdByReflection(this, "id",
				"umipay_main_tab_viewfilpper"));
		mContectViewFlipper = (ViewFlipper) findViewById(Util_Resource.getIdByReflection(this, "id",
				"umipay_main_content_viewflipper"));
		setupView();
	}

	private void setupView() {
		mAccountList = (ArrayList<UmipayAccount>) UmipayAccountManager.getInstance(getApplicationContext())
				.getNormalAccountList();
		if (mAccountList == null || mAccountList.size() == 0) {
			mTabViewFlipper.setDisplayedChild(1);
			mContectViewFlipper.setDisplayedChild(1);
			mIsOnLoginView = false;
			mOnView = REGISTER_VIEW;
		} else {
			mTabViewFlipper.setDisplayedChild(0);
			mContectViewFlipper.setDisplayedChild(0);
			mIsOnLoginView = true;
			mOnView = LOGIN_VIEW;
		}
		initViews();
		initListener();
		initPopupWindow();
	}

	private void setupLoginView() {
		if (mContectViewFlipper.getDisplayedChild() == 1) {
			mTabViewFlipper.showPrevious();
			mContectViewFlipper.setInAnimation(this, Util_Resource.getIdByReflection(this, "anim",
					"umipay_slide_left_in"));
			mContectViewFlipper.setOutAnimation(this, Util_Resource.getIdByReflection(this, "anim",
					"umipay_slide_right_out"));
			mContectViewFlipper.showPrevious();
			mIsOnLoginView = true;
			mOnView = LOGIN_VIEW;
			initViews();
			initListener();
		}
	}

	private void setupRegisterView() {
		if (mContectViewFlipper.getDisplayedChild() == 0) {
			mTabViewFlipper.showNext();
			mContectViewFlipper.setInAnimation(this, Util_Resource.getIdByReflection(this, "anim",
					"umipay_slide_right_in"));
			mContectViewFlipper.setOutAnimation(this, Util_Resource.getIdByReflection(this, "anim",
					"umipay_slide_left_out"));
			mContectViewFlipper.showNext();
			mIsOnLoginView = false;
			mOnView = REGISTER_VIEW;
			initViews();
			initListener();
		}
	}

	private void setupBindMobileView() {
		mIsOnLoginView = false;
		mOnView = BIND_MOBILE_VIEW;
		setContentView(Util_Resource.getIdByReflection(this, "layout", "umipay_bindmobile_layout"));
		mBindMobileTitleTV = (TextView) findViewById(Util_Resource.getIdByReflection(this, "id",
				"umipay_bindmobile_title_tv"));
		mPhoneEditor = (EditText) findViewById(Util_Resource.getIdByReflection(this, "id", "umipay_phone_box"));
		mCodeEditor = (EditText) findViewById(Util_Resource.getIdByReflection(this, "id", "umipay_code_box"));
		mGetCodeBtn = (Button) findViewById(Util_Resource.getIdByReflection(this, "id", "umipay_get_code_btn"));
		mSkipBtn = (Button) findViewById(Util_Resource.getIdByReflection(this, "id", "umipay_bindmobile_skip_btn"));
		mBindAndLoginBtn = (Button) findViewById(Util_Resource.getIdByReflection(this, "id", "umipay_bind_login_btn"));

		if (mBindMobileTitleTV != null) {
			//TODO set title
			String info = "偶玩游戏";
			if (mLoginType == UmipayLoginInfoDialog.REGISTER_AND_LOGIN) {
				info = mLoginAccount.getUserName() + "注册成功！";
			} else {
				info = mLoginAccount.getUserName() + "登录成功！";
			}
			mBindMobileTitleTV.setText(info);
		}
		if (!TextUtils.isEmpty(mPhoneNum) && mPhoneNum.length() == 11 && mPhoneEditor != null) {
			mPhoneEditor.setText(mPhoneNum);
			getVerificateCode();
		}
		if (mGetCodeBtn != null) {
			mGetCodeBtn.setOnClickListener(this);
		}
		if (mSkipBtn != null) {
			mSkipBtn.setOnClickListener(this);
		}
		if (mBindAndLoginBtn != null) {
			mBindAndLoginBtn.setOnClickListener(this);
		}

		initHandler();
	}

	private void setupReqSuccessView() {
		mIsOnLoginView = false;
		mOnView = REG_SUCCESS_View;
		canScreenShot = true;
		setContentView(Util_Resource.getIdByReflection(this, "layout", "umipay_regist_success_layout"));
		mRegSuccessAccountEditor = (EditText) findViewById(Util_Resource.getIdByReflection(this, "id",
				"umipay_reg_success_account_box"));
		mRegSuccessPswEditor = (EditText) findViewById(Util_Resource.getIdByReflection(this, "id",
				"umipay_reg_success_psw_box"));
		mRegSuccessEnterGameBtn = (Button) findViewById(Util_Resource.getIdByReflection(this, "id",
				"umipay_reg_success_entergame_btn"));

		if (mRegSuccessAccountEditor != null) {
			mRegSuccessAccountEditor.setText(mLoginAccount.getUserName());
			mRegSuccessAccountEditor.setEnabled(false);
		}

		if (mRegSuccessPswEditor != null) {
			mRegSuccessPswEditor.setText(mLoginAccount.getPsw());
			mRegSuccessPswEditor.setEnabled(false);
		}

		if (mRegSuccessEnterGameBtn != null) {
			mRegSuccessEnterGameBtn.setOnClickListener(this);
		}

	}

	private void initHandler() {
		mTimerHandler = new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				switch (msg.what) {
					case MSG_UPDATE_COUNTDOWN_BTN:
						if (mCountdownText > 0) {
							mCountdownText--;
							mGetCodeBtn.setText("重获（" + mCountdownText + "）");
						} else {
							if (mCountdownTimer != null) {
								mCountdownTimer.cancel();
							}
							mGetCodeBtn.setText("获取验证码");
							mGetCodeBtn.setEnabled(true);
						}
						break;
				}
				return true;
			}
		});
	}

	private void initViews() {
		View currentTabView = mTabViewFlipper.getCurrentView();
		View currentContectView = mContectViewFlipper.getCurrentView();
		mLoginTab = currentTabView.findViewById(Util_Resource
				.getIdByReflection(this, "id", "umipay_login_tab"));
		mRegisterTab = currentTabView.findViewById(Util_Resource
				.getIdByReflection(this, "id", "umipay_register_tab"));
		mRememberPWLayout = currentContectView.findViewById(Util_Resource
				.getIdByReflection(this, "id", "umipay_remember_pw_layout"));
		mAutoLoginLayout = currentContectView.findViewById(Util_Resource
				.getIdByReflection(this, "id", "umipay_autologin_layout"));
		mViewPswLayout = currentContectView.findViewById(Util_Resource
				.getIdByReflection(this, "id", "umipay_psw_cb_layout"));
		mRememberPWCheckBox = (CheckBox) currentContectView
				.findViewById(Util_Resource.getIdByReflection(this, "id",
						"umipay_remember_pw_cb"));
		mAutoLoginCheckBox = (CheckBox) currentContectView
				.findViewById(Util_Resource.getIdByReflection(this, "id",
						"umipay_autologin_cb"));
		mViewPswCheckBox = (CheckBox) currentContectView
				.findViewById(Util_Resource.getIdByReflection(this, "id",
						"umipay_psw_cb"));
		mOtherLoginCheckBox = (CheckBox) currentContectView
				.findViewById(Util_Resource.getIdByReflection(this, "id",
						"umipay_other_login_btn"));
		mOtherLoginLayout = currentContectView.findViewById(Util_Resource
				.getIdByReflection(this, "id", "umipay_other_login_layout"));
		mLoginBtn = (Button) currentContectView.findViewById(Util_Resource
				.getIdByReflection(this, "id", "umipay_login_btn"));
		mQuickRegisterBtn = (Button) currentContectView
				.findViewById(Util_Resource.getIdByReflection(this, "id",
						"umipay_quick_register_btn"));
		mAccountEditor = (EditText) currentContectView
				.findViewById(Util_Resource.getIdByReflection(this, "id",
						"umipay_name_box"));
		mPswEditor = (EditText) currentContectView.findViewById(Util_Resource
				.getIdByReflection(this, "id", "umipay_psw_box"));
		mSelectAccountBtn = currentContectView.findViewById(Util_Resource
				.getIdByReflection(this, "id", "umipay_account_select_btn"));
		mQQOauth = currentContectView.findViewById(Util_Resource
				.getIdByReflection(this, "id", "umipay_qq_oauth"));
		mSinaOauth = currentContectView.findViewById(Util_Resource
				.getIdByReflection(this, "id", "umipay_sina_oauth"));
		mTrialIcon = currentContectView.findViewById(Util_Resource
				.getIdByReflection(this, "id", "umipay_trial_imageview")); // 试玩图标
		mPhoneEditor = (EditText) currentContectView.findViewById(Util_Resource
				.getIdByReflection(this, "id", "umipay_phone_box"));
		mRegistBtn = (Button) currentContectView.findViewById(Util_Resource
				.getIdByReflection(this, "id", "umipay_register_btn"));
		mForgetPswTextView = currentContectView.findViewById(Util_Resource
				.getIdByReflection(this, "id", "umipay_forget_psw_tv"));
		mAgreementTextView = currentContectView.findViewById(Util_Resource
				.getIdByReflection(this, "id", "umipay_agreement"));

		mClearAccountBtn = currentContectView.findViewById(Util_Resource.getIdByReflection(this, "id",
				"umipay_reg_name_clear_btn"));
		mClearPswBtn = currentContectView.findViewById(Util_Resource.getIdByReflection(this, "id",
				"umipay_reg_psw_clear_btn"));
		mClearPhoneBtn = currentContectView.findViewById(Util_Resource.getIdByReflection(this, "id",
				"umipay_reg_phone_clear_btn"));


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
		initPswFilter();
		switch (mOnView) {
			case LOGIN_VIEW:
				UmipayAccount account = null;
				try {
					account = UmipayAccountManager.getInstance(this).getFirstNormalAccount();
				} catch (Exception e) {
					Debug_Log.e(e);
				}
				if (account != null) {
					mAccountEditor.setText(account.getUserName());
					mPswEditor.setText(account.getPsw());

					mAccountEditor.setSelection(mAccountEditor.getText().length());
					mPswEditor.setSelection(mPswEditor.getText().length());
				}
				mSelectAccountBtn.setVisibility(View.VISIBLE);
				mPswEditor.setTransformationMethod(PasswordTransformationMethod
						.getInstance());
				mPswEditor.setOnEditorActionListener(this);

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
				if (mRememberPWCheckBox != null) {
					if (account != null && account.isRemenberPsw()) {
						mRememberPWCheckBox.setChecked(true);
					} else {
						mRememberPWCheckBox.setChecked(false);
					}
					//自动登陆钩上了，肯定也得记住密码了
					if (mAutoLoginCheckBox.isChecked()) {
						mRememberPWCheckBox.setChecked(true);
					}
//					mRememberPWCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//						@Override
//						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//							if (!isChecked && mPswEditor != null) {
//								mPswEditor.setText("");
//							}
//						}
//					});
				}


				break;
			case REGISTER_VIEW:
				mPswEditor.setTransformationMethod(null);
				mPhoneEditor.setOnEditorActionListener(this);
				break;
			case BIND_MOBILE_VIEW:
				if (!TextUtils.isEmpty(mPhoneNum)) {
					mPhoneEditor.setText(mPhoneNum);
					mPhoneEditor.setSelection(mPhoneNum.length());
				}
				mCodeEditor.setOnEditorActionListener(this);
				break;
		}
	}

	private void initListener() {
		// 登陆Tab
		if (mLoginTab != null) {
			mLoginTab.setOnClickListener(this);
		}
		// 注册Tab
		if (mRegisterTab != null) {
			mRegisterTab.setOnClickListener(this);
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
		// 一键注册btn
		if (mQuickRegisterBtn != null) {
			mQuickRegisterBtn.setOnClickListener(this);
		}
		// 选择账号btn
		if (mSelectAccountBtn != null) {
			mSelectAccountBtn.setOnClickListener(this);
		}
		// 注册
		if (mRegistBtn != null) {
			mRegistBtn.setOnClickListener(this);
		}
		// 忘记密码
		if (mForgetPswTextView != null) {
			mForgetPswTextView.setOnClickListener(this);
		}
		// 跳过
		if (mSkipBtn != null) {
			mSkipBtn.setOnClickListener(this);
		}
		// 绑定&登录
		if (mBindAndLoginBtn != null) {
			mBindAndLoginBtn.setOnClickListener(this);
		}
		// 获取验证码
		if (mGetCodeBtn != null) {
			mGetCodeBtn.setOnClickListener(this);
		}
		// 阅读偶玩服务条款
		if (mAgreementTextView != null) {
			mAgreementTextView.setOnClickListener(this);
		}

		if (mClearAccountBtn != null) {
			mClearAccountBtn.setOnClickListener(this);
		}

		if (mClearPswBtn != null) {
			mClearPswBtn.setOnClickListener(this);
		}

		if (mClearPhoneBtn != null) {
			mClearPhoneBtn.setOnClickListener(this);
		}
	}

	/**
	 * 初始化PopupWindow
	 */
	private void initPopupWindow() {

		if (mAccountList.size() > 0) {
			// 设置自定义Adapter
			mPopupWindowAdapter = new PopupWindowAdapter(this, mHandler, mAccountList);
			// PopupWindow浮动下拉框布局
			View loginwindow = (View) this.getLayoutInflater().inflate(Util_Resource.getIdByReflection(this, "layout",
					"umipay_account_list"), null);
			mAccountListView = (ListView) loginwindow.findViewById(Util_Resource.getIdByReflection(this, "id",
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
					if (!mIsOnLoginView) {
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
					if (mIsOnLoginView) {
						if (mLoginBtn != null) {
							if (mAccountEditor.getText().length() > 0
									&& mPswEditor.getText().length() > 0) {
								mLoginBtn.setEnabled(true);
							} else {
								mLoginBtn.setEnabled(false);
							}
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
					if (mIsOnLoginView) {
						String userName = s.toString();
						if (!TextUtils.isEmpty(userName)) {
							UmipayAccount umipayAccount = UmipayAccountManager
									.getInstance(getApplicationContext())
									.getAccountByUserName(userName);
							if (null != umipayAccount && umipayAccount.isRemenberPsw()) {
								mPswEditor.setText(umipayAccount.getPsw());
								mRememberPWCheckBox.setChecked(true);
								return;
							}
						}
						mPswEditor.setText("");
					} else {
						if (TextUtils.isEmpty(s.toString())) {
							if (mQuickRegisterBtn != null) {
								mQuickRegisterBtn.setEnabled(true);
							}
							if (mClearAccountBtn != null) {
								mClearAccountBtn.setVisibility(View.GONE);
							}
						} else {
							if (mQuickRegisterBtn != null) {
								mQuickRegisterBtn.setEnabled(false);
							}
							if (mClearAccountBtn != null) {
								mClearAccountBtn.setVisibility(View.VISIBLE);
							}
						}
					}
				}
			});
		}

		if (mPhoneEditor != null) {
			mPhoneEditor.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {

				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {

				}

				@Override
				public void afterTextChanged(Editable s) {
					String phone = s.toString();
					if (TextUtils.isEmpty(phone)) {
						if (mClearPhoneBtn != null) {
							mClearPhoneBtn.setVisibility(View.GONE);
						}
					} else {
						if (mClearPhoneBtn != null) {
							mClearPhoneBtn.setVisibility(View.VISIBLE);
						}
					}
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

	private void startProgressDialog() {
		if (progressDialog == null) {
			progressDialog = UmipayProgressDialog.createDialog(this);
		}
		progressDialog.show();
	}

	private void stopProgressDialog() {
		if (progressDialog != null) {
			progressDialog.cancel();
			progressDialog = null;
		}
	}

	private void toast(String text) {
		Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	// 跳过绑定
	private void skipBind() {
		sendLoginResultMsg(UmipaySDKStatusCode.SUCCESS, null, mLoginType, mLoginAccount);
		this.finish();
	}

	// 一键注册 进入游戏
	private void enterGame() {
		sendLoginResultMsg(UmipaySDKStatusCode.SUCCESS, null, UmipayLoginInfoDialog.QUCIK_REGISTER, mLoginAccount);
		this.finish();
	}

	// 绑定&登录
	private void bindAndLogin() {
		String mobile = mPhoneEditor.getText().toString();
		String code = mCodeEditor.getText().toString();
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
		UmipayCommandTaskManager.getInstance(mActivity).VerificateSMSCommandTask(Interface_Verificate_SMS_Listener
				.TYPE_VERIFICATE_SMS, mobile, code);
		startProgressDialog();
	}

	// 获取验证码
	private void getVerificateCode() {
		String mobile = mPhoneEditor.getText().toString();
		if (TextUtils.isEmpty(mobile) || mobile.length() != 11 || mobile.charAt(0) != '1') {
			toast("请输入正确的手机号码");
			return;
		}
		mCurrentVerificateType = Interface_Verificate_SMS_Listener.TYPE_SEND_SMS;
		ListenerManager.setCommandVerificateSMSListener(this);
		UmipayCommandTaskManager.getInstance(mActivity).VerificateSMSCommandTask(Interface_Verificate_SMS_Listener
				.TYPE_SEND_SMS, mobile, null);
		startProgressDialog();
	}

	private void register() {
		String account = mAccountEditor.getEditableText().toString();
		String psw = mPswEditor.getEditableText().toString();
		mPhoneNum = mPhoneEditor.getEditableText().toString();
		if (TextUtils.isEmpty(account)) {
			toast("请输入有效的偶玩通行证账号");
			return;
		}
		if (TextUtils.isEmpty(psw) || psw.length() < 6) {
			toast("请输入6~32位长度的密码");
			return;
		}
		if (!TextUtils.isEmpty(mPhoneNum) && (mPhoneNum.length() != 11 || mPhoneNum.charAt(0) != '1')) {
			toast("请输入11位有效手机号码");
			return;
		}
		ListenerManager.setCommandRegistListener(this);
		UmipayCommandTaskManager.getInstance(mActivity).RegistCommandTask(account, psw, mPhoneNum);

		startProgressDialog();
	}

	/**
	 * 一键登录函数
	 */
	private void quickRegister() {
		ListenerManager.setCommandRegistListener(this);
		UmipayCommandTaskManager.getInstance(mActivity).QuickRegistCommandTask();
		startProgressDialog();
	}

	private void login() {
		String account = mAccountEditor.getEditableText().toString();
		String psw = mPswEditor.getEditableText().toString();
		boolean isRemember = mRememberPWCheckBox.isChecked();
		if (TextUtils.isEmpty(account)) {
			toast("请输入偶玩通行证账号~");
			return;
		}
		if (TextUtils.isEmpty(psw)) {
			toast("请输入偶玩通行证密码~");
			return;
		}
		UmipayCommandTaskManager.getInstance(this).LoginCommandTask(account, psw, isRemember);
		startProgressDialog();
	}

	private void oauthLogin(int type, String openid, String token, int expire, String authdata) {
			ListenerManager.setCommandLoginListener(this);
			UmipayCommandTaskManager.getInstance(mActivity).OauthLoginCommandTask(type, openid, token, expire, authdata);
			startProgressDialog();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mOnView == BIND_MOBILE_VIEW) {
				skipBind();
			} else if (mOnView == REG_SUCCESS_View) {
				enterGame();
			} else {
				sendLoginResultMsg(UmipaySDKStatusCode.LOGIN_CLOSE, null, 0, null);
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (MotionEvent.ACTION_DOWN == event.getAction()) {
			final int x = (int) event.getX();
			final int y = (int) event.getY();
			final int slop = ViewConfiguration.get(UmipayActivity.this).getScaledWindowTouchSlop();
			final View decorView = UmipayActivity.this.getWindow().getDecorView();
			if ((x < -slop) || (y < -slop) || (x > (decorView.getWidth() + slop)) || (y > (decorView.getHeight() +
					slop))) {
				closeInputMethod();
				return true;
			}
		}
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId != EditorInfo.IME_ACTION_DONE) {
			return false;
		}

		if (v.equals(mPswEditor) && mOnView == LOGIN_VIEW) {
			login();
			return false;
		}
		if (v.equals(mPhoneEditor) && mOnView == REGISTER_VIEW) {
			register();
			return false;
		}
		if (v.equals(mCodeEditor) && mOnView == BIND_MOBILE_VIEW) {
			bindAndLogin();
			return false;
		}
		return false;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (mOnView == REG_SUCCESS_View && hasFocus && canScreenShot) {

			String msg = "图片保存成功,请到相册查看";
			try {
					/*
					 * 此处是新增加的截图部分，用于注册成功回调时截图，截图命名为用户名的hash值，保存到媒体库中
					 */
				Util_ScreenShot.shot(mLoginAccount.getUserName(), this, getWindow().getDecorView());
			} catch (Throwable e) {
				Debug_Log.e(e);
				msg = "图片保存失败！";
			}
			Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();
			canScreenShot = false;
		}
		Debug_Log.dd("onWindowFocusChanged : " + hasFocus);
	}

	@Override
	public void onClick(View v) {
		// 登录tab
		if (v.equals(mLoginTab)) {
			setupLoginView();
			return;
		}
		// 注册tab
		if (v.equals(mRegisterTab)) {
			setupRegisterView();
			return;
		}
		// 记住密码cb
		if (v.equals(mRememberPWLayout)) {
			mRememberPWCheckBox.setChecked(!mRememberPWCheckBox.isChecked());
			// 取消记住密码同时取消自动登录
			if (!mRememberPWCheckBox.isChecked()
					&& mAutoLoginCheckBox.isChecked()) {
				mAutoLoginCheckBox.setChecked(false);
			}
			return;
		}
		// 自动登录cb
		if (v.equals(mAutoLoginLayout)) {
			mAutoLoginCheckBox.setChecked(!mAutoLoginCheckBox.isChecked());
			// 自动登录的同时勾选记住密码
			if (mAutoLoginCheckBox.isChecked()
					&& !mRememberPWCheckBox.isChecked()) {
				mRememberPWCheckBox.setChecked(true);
			}
			return;
		}
		// 显示密码
		if (v.equals(mViewPswLayout)) {
			mViewPswCheckBox.setChecked(!mViewPswCheckBox.isChecked());
			// 密码可见
			if (mViewPswCheckBox.isChecked()) {
				mPswEditor.setTransformationMethod(null);
			} else {
				mPswEditor.setTransformationMethod(PasswordTransformationMethod
						.getInstance());
			}
			mPswEditor.setSelection(mPswEditor.getText().length());
			return;
		}
		// 第三方登录
		if (v.equals(mOtherLoginCheckBox)) {
			if (mOtherLoginCheckBox.isChecked()) {
				mOtherLoginLayout.setVisibility(View.VISIBLE);
				mOtherLoginLayout.startAnimation(AnimationUtils.loadAnimation(
						this, Util_Resource.getIdByReflection(this, "anim",
								"umipay_other_login_show")
				));
			} else {
				mOtherLoginLayout.setVisibility(View.INVISIBLE);
				mOtherLoginLayout.startAnimation(AnimationUtils.loadAnimation(
						this, Util_Resource.getIdByReflection(this, "anim",
								"umipay_other_login_hide")
				));
			}
			return;
		}
		// 登录btn
		if (v.equals(mLoginBtn)) {
			login();
			return;
		}
		// 一键注册btn
		if (v.equals(mQuickRegisterBtn)) {
			quickRegister();
			return;
		}
		// QQ登录
		if (v.equals(mQQOauth)) {
//			ListenerManager.setOtherLoginListener(this);
			mTencent.login(this,QQ_OAUTH_APPID,mTencentAuthCallBack);
			return;
		}
		// weibo登录
		if (v.equals(mSinaOauth)) {
//			ListenerManager.setOtherLoginListener(this);
			if(mWeibo != null) {
				mWeibo.login(this,this);
			}
			return;
		}
		// 试玩图标
		if (v.equals(mTrialIcon)) {
			int expire = (int) (Global_Final_Common_Millisecond.oneDay_ms / 1000);
			VisitorUtil visitorUtil = new VisitorUtil(this);
			Visitor visitor = visitorUtil.getVisitorAccount();
			oauthLogin(UmipayAccount.TYPE_VISITOR, visitor.getAccount(),
					visitor.getToken(), expire,null);
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
		// 注册
		if (v.equals(mRegistBtn)) {
			register();
			return;
		}
		// 忘记密码
		if (v.equals(mForgetPswTextView)) {
			UmipaySDKManager.showRegetPswView(this);
			return;
		}
		// 跳过
		if (v.equals(mSkipBtn)) {
			skipBind();
			return;
		}
		// 绑定&登录
		if (v.equals(mBindAndLoginBtn)) {
			bindAndLogin();
			return;
		}
		// 获取验证码
		if (v.equals(mGetCodeBtn)) {
			getVerificateCode();
			return;
		}
		if (v.equals(mAgreementTextView)) {
			UmipaySDKManager.showAgreementView(this);
			return;
		}
		if (v.equals(mClearAccountBtn)) {
			if (mAccountEditor != null) {
				mAccountEditor.setText("");
			}
			return;
		}
		if (v.equals(mClearPswBtn)) {
			if (mPswEditor != null) {
				mPswEditor.setText("");
			}
			return;
		}
		if (v.equals(mClearPhoneBtn)) {
			if (mPhoneEditor != null) {
				mPhoneEditor.setText("");
			}
			return;
		}
		if (v.equals(mRegSuccessEnterGameBtn)) {
			enterGame();
			return;
		}
	}

	@Override
	public boolean handleMessage(Message message) {
		Bundle data = message.getData();
		switch (message.what) {
			case 1:
				accountSelect(data.getInt(PopupWindowAdapter.SELECT_KEY));
				break;
			case 2:
				accountDelete(data.getInt(PopupWindowAdapter.DELETE_KEY));
				break;
		}
		return false;
	}

	/**
	 * 关闭输入法
	 */
	private void closeInputMethod() {
		try {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm.isActive()) {
				imm.hideSoftInputFromWindow(UmipayActivity.this.getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	/**
	 * 点击选择账号
	 */
	private void accountSelect(int index) {
		mAccountEditor.setText(mAccountList.get(index).getUserName());
		mPswEditor.setText(mAccountList.get(index).getPsw());
		mAccountEditor.setSelection(mAccountEditor.getText().length());
		mPswEditor.setSelection(mPswEditor.getText().length());
		mRememberPWCheckBox.setChecked(mAccountList.get(index).isRemenberPsw());
		mPopupWindow.dismiss();
	}

	/**
	 * 点击删除账号
	 */
	private void accountDelete(final int index) {
		final UmipayAccount deleteAccount = mAccountList.get(index);
		new AlertDialog.Builder(this)
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
								UmipayAccountManager.getInstance(getApplication()).deleteAccount(deleteAccount);
								mAccountList.remove(index);
								//刷新下拉列表
								mPopupWindowAdapter.notifyDataSetChanged();
								toast("删除成功");

								UmipayCommandTaskManager.getInstance(mActivity).DeleteAccountCommandTask(deleteAccount
										.getUserName());

							}
						}
				).show();
	}

	@Override
	public void onVerificateSMS(Gson_Cmd_VerificateSMS gsonCmdVerificateSMS) {
		int code = gsonCmdVerificateSMS.getCode();
		String msg = gsonCmdVerificateSMS.getMessage();
		sendMessage(MSG_VERIFICATESMS, new MsgData(code, msg, null));
	}

	private void handleVerificateSMSMsg(MsgData data) {
		int code = data.getCode();
		String msg = data.getMsg();
		stopProgressDialog();
		Debug_Log.d("error code : " + code);
		if (mCurrentVerificateType == Interface_Verificate_SMS_Listener.TYPE_SEND_SMS) {
			if (code == UmipaySDKStatusCode.SUCCESS) {
				mGetCodeBtn.setEnabled(false);
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
				sendLoginResultMsg(code, null, mLoginType, mLoginAccount);
//				ListenerManager.callbackLoginSuccess(UmipaySDKManager.getShowLoginViewContext(), mLoginAccount,
//						mLoginType);
				this.finish();
			} else {
				msg = UmipaySDKStatusCode.handlerMessage(code, msg);
				toast("验证验证码失败(" + msg + ")");
			}
		}
	}


	@Override
	public void onLogin(int code, String msg, UmipayAccount account) {
		sendMessage(MSG_LOGIN, new MsgData(code, msg, account));
	}

	private void handleLoginMsg(MsgData data) {
		try {
			stopProgressDialog();
			if (data.getCode() == UmipaySDKStatusCode.SUCCESS) {
				UmipayAccount account = (UmipayAccount) data.getData();
				try {
					if (account.getBindMobile() != 0) {
						mLoginAccount = account;
						mLoginType = UmipayLoginInfoDialog.NORMAL_LOGIN;
						setupBindMobileView();
					} else {
						sendLoginResultMsg(UmipaySDKStatusCode.SUCCESS, null, UmipayLoginInfoDialog.NORMAL_LOGIN,
								account);

						this.finish();
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
	public void onRegist(int type, int code, String msg, UmipayAccount account) {
		switch (type) {
			case UmipayAccount.TYPE_REGIST:
				sendMessage(MSG_REGIST, new MsgData(code, msg, account));
				break;
			case UmipayAccount.TYPE_QUICK_REGIST:
				sendMessage(MSG_QUICKREGIST, new MsgData(code, msg, account));
				break;
		}
	}

	private void handleRegistMsg(MsgData data) {
		stopProgressDialog();
		int code = data.getCode();
		String msg = data.getMsg();
		if (code == UmipaySDKStatusCode.SUCCESS) {
			UmipayAccount account = (UmipayAccount) data.getData();
			try {
				if (!TextUtils.isEmpty(mPhoneNum) && mPhoneNum.length() == 11) {
					mLoginAccount = account;
					mLoginType = UmipayLoginInfoDialog.REGISTER_AND_LOGIN;
					setupBindMobileView();
				} else {
					sendLoginResultMsg(UmipaySDKStatusCode.SUCCESS, null, UmipayLoginInfoDialog.REGISTER_AND_LOGIN,
							account);
					this.finish();
				}
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
		} else {
			msg = UmipaySDKStatusCode.handlerMessage(code, msg);
			toast(msg + "(" + code + ")");
		}
	}

	private void handleQuickRegistMsg(MsgData data) {
		stopProgressDialog();
		int code = data.getCode();
		String msg = data.getMsg();
		if (code == UmipaySDKStatusCode.SUCCESS) {
			mLoginAccount = (UmipayAccount) data.getData();
			mLoginType = UmipayLoginInfoDialog.QUCIK_REGISTER;
			setupReqSuccessView();
		} else {
			msg = UmipaySDKStatusCode.handlerMessage(code, msg);
			toast(msg + "(" + code + ")");
		}
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
				case MSG_VERIFICATESMS:
					handleVerificateSMSMsg((MsgData) msg.obj);
					break;
				case MSG_REGIST:
					handleRegistMsg((MsgData) msg.obj);
					break;
				case MSG_QUICKREGIST:
					handleQuickRegistMsg((MsgData) msg.obj);
					break;
			}
		}
	}

	/**
	 * 重新调整布局大小
	 */
	private void resize(){
		try {
			//直接获取dimens对应的width和marginLeft，更新横竖屏切换后布局的大小
			int width = (int) getResources().getDimension(Util_Resource.getIdByReflection(this, "dimen", "umipay_main_diglog_width"));
			int marginLeft = (int) getResources().getDimension(Util_Resource.getIdByReflection(this, "dimen", "umipay_main_diglog_autologin_marginleft"));

			//调整Tab布局大小
			for(int i = 0;i < mTabViewFlipper.getChildCount();i++){
				View v = mTabViewFlipper.getChildAt(i);
				ViewGroup.LayoutParams lp= v.getLayoutParams();
				lp.width = width;
				v.setLayoutParams(lp);
			}

			//调整登陆、注册界面布局大小
			for(int i = 0;i < mContectViewFlipper.getChildCount();i++) {
				View v = mContectViewFlipper.getChildAt(i);
				ViewGroup.LayoutParams lp= v.getLayoutParams();
				lp.width = width;
				v.setLayoutParams(lp);
			}

			//调整自动登陆的marginLeft(避免与[忘记密码]错位重叠)
			if(mAutoLoginLayout != null) {
				RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mAutoLoginLayout.getLayoutParams();
				lp.setMargins(marginLeft, 0, 0, 0);
				mAutoLoginLayout.setLayoutParams(lp);
			}

			//绑定手机、注册成功界面调整布局大小
			if(mOnView == BIND_MOBILE_VIEW || mOnView == REG_SUCCESS_View) {
				View mContentLayout = findViewById(Util_Resource.getIdByReflection(this, "id",
						"umipay_main_content_layout"));
				if (mContentLayout != null) {
					ViewGroup.LayoutParams lp= mContentLayout.getLayoutParams();
					lp.width = width;
					mContentLayout.setLayoutParams(lp);
				}
			}

			//隐藏账户下拉列表
			if(mPopupWindow != null){
				mPopupWindow.dismiss();
			}
		}catch (Throwable e){
			Debug_Log.e(e);
		}
	}

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
	    //横竖屏切换时重新调整布局大小
	    resize();
    }
}
