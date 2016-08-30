package net.ouwan.umipay.android.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import net.ouwan.umipay.android.Utils.Util_Resource;
import net.ouwan.umipay.android.api.UmipaySDKManager;
import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.asynctask.TaskCMD;
import net.ouwan.umipay.android.config.SDKCacheConfig;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.ouwan.umipay.android.entry.gson.Gson_Login;
import net.ouwan.umipay.android.interfaces.Interface_Account_Listener_Get_Registrable_Account;
import net.ouwan.umipay.android.interfaces.Interface_Account_Listener_Login;
import net.ouwan.umipay.android.manager.ListenerManager;
import net.ouwan.umipay.android.manager.UmipayAccountManager;
import net.ouwan.umipay.android.manager.UmipayCommandTaskManager;
import net.ouwan.umipay.android.view.PopupWindowAdapter;
import net.ouwan.umipay.android.view.UmipayLoginInfoDialog;

import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

/**
 * Created by jimmy on 2016/8/14.
 */
public class LightSDKLoginFragment extends BaseFragment implements TextView.OnEditorActionListener,
		Interface_Account_Listener_Login, Handler.Callback,
		Interface_Account_Listener_Get_Registrable_Account {
	private SDKCacheConfig mCacheConfig;


	private TextView mTitleTv;
	private Button mLoginBtn;

	private View mAutoLoginLayout;
	private CheckBox mAutoLoginCheckBox;

	private CheckBox mRememberPWCheckBox;
	private CheckBox mViewPswCheckBox;
	private View mClearAccountBtn;
	private View mSelectAccountBtn;

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
	private View mGoRegisterBtn;

	public static LightSDKLoginFragment newInstance() {
		LightSDKLoginFragment fragment = new LightSDKLoginFragment();
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
					"umipay_lightsdk_login_layout"), container, false);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		initViews();
		initListener();
		initPopupWindow();
		return mRootLayout;
	}

	private void getRegistrableAccount() {
		ListenerManager.setCommandGetRegistrableAccountListener(this);
		UmipayCommandTaskManager.getInstance(getActivity()).GetRegistrableAccountCommandTask();
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
			mLoginBtn = (Button) mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_login_btn"));

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

		UmipayAccount account = null;

		try {
			account = UmipayAccountManager.getInstance(getActivity()).getFirstNormalAccount();
		} catch (Exception e) {
			Debug_Log.e(e);
		}

		if (mLoginBtn != null) {
			mLoginBtn.setEnabled(true);
		}

		if (mSelectAccountBtn != null) {
			mSelectAccountBtn.setVisibility(View.VISIBLE);
		}

		if (mAutoLoginLayout != null) {
			mAutoLoginLayout.setVisibility(View.GONE);
		}
		//轻sdk默认自动登录
		if (mAutoLoginCheckBox != null) {
			mAutoLoginCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mCacheConfig.setAutoLogin(isChecked);
					mCacheConfig.save();
				}
			});
			mAutoLoginCheckBox.setChecked(true);
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
	protected void handleLoginMsg(MsgData data) {
		try {
			stopProgressDialog();
			if (data != null) {
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
		// 注册tab
		if (v.equals(mGoRegisterBtn)) {
			//轻sdk部分需要请求后台获取有效用户名和密码
			getRegistrableAccount();
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
			if (mAutoLoginCheckBox.isChecked() &&
					mRememberPWCheckBox != null && !mRememberPWCheckBox.isChecked()) {
				mRememberPWCheckBox.setChecked(true);
			}
			return;
		}
		// 显示密码
		if (mViewPswLayout != null) {
			mViewPswLayout.setOnClickListener(this);
		}
		// 登录btn
		if (v.equals(mLoginBtn)) {
			login();
			return;
		}

		// 选择账号btn
		if (v.equals(mSelectAccountBtn)) {
			if (mAccountList != null && mAccountList.size() > 0 && mPopupWindow != null) {
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
			}
			// 密码可见
			if (mPswEditor != null) {
				if (mViewPswCheckBox != null && mViewPswCheckBox.isChecked()) {
					mPswEditor.setTransformationMethod(null);
				} else {
					mPswEditor.setTransformationMethod(PasswordTransformationMethod
							.getInstance());
				}
				mPswEditor.setSelection(mPswEditor.getText().length());
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
					if (mGoRegisterBtn != null) {
						if (mAccountEditor.getText().length() > 0) {
							mGoRegisterBtn.setBackgroundResource(Util_Resource
									.getIdByReflection(getActivity(), "drawable", "umipay_btn_black"));
						} else {
							mGoRegisterBtn.setBackgroundResource(Util_Resource
									.getIdByReflection(getActivity(), "drawable", "umipay_btn_blue"));
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
	protected void handleGetRegistrableAccountMsg(MsgData data) {
		stopProgressDialog();
		if (data != null) {
			int code = data.getCode();
			String msg = data.getMsg();
			if (code == UmipaySDKStatusCode.SUCCESS) {
				replaceFragmentFromActivityFragmentManager(LightSDKRegisterFragment
						.newInstance((UmipayAccount) data.getData()));
			} else {
				msg = UmipaySDKStatusCode.handlerMessage(code, msg);
				toast(msg + "(" + code + ")");
			}
		}
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

	@Override
	public void onGetRegistrableAccount(int type, int code, String msg, UmipayAccount account) {
		sendMessage(MSG_GET_REGISTRABLE_ACCOUNT, new MsgData(code, msg, account));
	}
}
