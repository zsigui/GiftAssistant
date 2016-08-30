package net.ouwan.umipay.android.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import net.ouwan.umipay.android.Utils.Util_Resource;
import net.ouwan.umipay.android.api.UmipaySDKManager;
import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.asynctask.TaskCMD;
import net.ouwan.umipay.android.config.SDKCacheConfig;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.ouwan.umipay.android.entry.gson.Gson_Login;
import net.ouwan.umipay.android.interfaces.Interface_Account_Listener_Register;
import net.ouwan.umipay.android.manager.ListenerManager;
import net.ouwan.umipay.android.manager.UmipayCommandTaskManager;
import net.ouwan.umipay.android.view.UmipayLoginInfoDialog;

import java.util.regex.PatternSyntaxException;

/**
 * Created by jimmy on 2016/8/14.
 */
public class RegisterFragment extends BaseFragment implements
		Interface_Account_Listener_Register {

	private SDKCacheConfig mCacheConfig;
	private TextView mTitleTv;
	private CheckBox mAgreementCheckBox;
	private View mClearAccountBtn;
	private View mGoToLoginBtn;
	private View mViewPswLayout;
	private CheckBox mViewPswCheckBox;
	private View mQuickRegisterBtn;
	private EditText mAccountEditor;
	private EditText mPswEditor;
	private Button mRegistBtn;
	private View mAgreementTextView;
	private View mClearPswBtn;

	public static RegisterFragment newInstance() {
		RegisterFragment fragment = new RegisterFragment();
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
					"umipay_register_layout"), container, false);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		initViews();
		initListener();
		return mRootLayout;
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
			mAgreementCheckBox = (CheckBox) mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_agreement_cb"));
			mGoToLoginBtn = mRootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
					"umipay_register_to_login_tv"));
			mViewPswLayout = mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_psw_cb_layout"));
			mViewPswCheckBox = (CheckBox) mRootLayout
					.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
							"umipay_psw_cb"));
			mQuickRegisterBtn = mRootLayout
					.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
							"umipay_quick_register_btn"));
			mAccountEditor = (EditText) mRootLayout
					.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
							"umipay_name_box"));
			mPswEditor = (EditText) mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_psw_box"));

			mRegistBtn = (Button) mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_register_btn"));
			mAgreementTextView = mRootLayout.findViewById(Util_Resource
					.getIdByReflection(getActivity(), "id", "umipay_agreement"));
			mClearAccountBtn = mRootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
					"umipay_reg_name_clear_btn"));
			mClearPswBtn = mRootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
					"umipay_reg_psw_clear_btn"));
		}
		if (mTitleTv != null) {
			mTitleTv.setText(getActivity().getResources().getString(Util_Resource.getIdByReflection(getActivity(),
					"string",
					"umipay_titile_register")));
		}
		if (mPswEditor != null) {
			initPswFilter();
		}
	}

	private void initListener() {
		//前往登录页
		if (mGoToLoginBtn != null) {
			mGoToLoginBtn.setOnClickListener(this);
		}
		// 显示密码
		if (mViewPswLayout != null) {
			mViewPswLayout.setOnClickListener(this);
		}
		// 一键注册btn
		if (mQuickRegisterBtn != null) {
			mQuickRegisterBtn.setOnClickListener(this);
		}
		// 注册
		if (mRegistBtn != null) {
			mRegistBtn.setOnClickListener(this);
		}
		// 阅读偶玩服务条款
		if (mAgreementTextView != null) {
			mAgreementTextView.setOnClickListener(this);
		}
		if (mAgreementCheckBox != null) {
			mAgreementCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (mRegistBtn != null) {
						mRegistBtn.setEnabled(isChecked);
					}
					if (mQuickRegisterBtn != null) {
						mQuickRegisterBtn.setEnabled(isChecked);
					}
				}
			});
			//默认勾选
			mAgreementCheckBox.setChecked(true);
		}

		if (mClearAccountBtn != null) {
			mClearAccountBtn.setOnClickListener(this);
		}

		if (mClearPswBtn != null) {
			mClearPswBtn.setOnClickListener(this);
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
					String editable = mAccountEditor.getText().toString();
					String str = stringFilter(editable);
					if (!editable.equals(str)) {
						mAccountEditor.setText(str);
						// 设置新的光标所在位置
						mAccountEditor.setSelection(str.length());
					}
				}

				@Override
				public void afterTextChanged(Editable s) {
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


	@Override
	protected void handleOnClick(View v) {
		if (v.equals(mGoToLoginBtn)) {
			if (mCacheConfig != null && mCacheConfig.isLightSDK()) {
				replaceFragmentFromActivityFragmentManager(LightSDKLoginFragment
						.newInstance());
			} else if (mCacheConfig != null && mCacheConfig.isMobileLogin()) {
				replaceFragmentFromActivityFragmentManager(MobileLoginFragment.newInstance
						());
			} else {
				replaceFragmentFromActivityFragmentManager(LoginFragment.newInstance());
			}
			return;
		}
		// 显示密码
		if (v.equals(mViewPswLayout)) {
			if (mViewPswCheckBox != null) {
				mViewPswCheckBox.setChecked(!mViewPswCheckBox.isChecked());
			}
			// 密码可见
			if (mPswEditor != null) {
				if (mViewPswCheckBox.isChecked()) {
					mPswEditor.setTransformationMethod(null);
				} else {
					mPswEditor.setTransformationMethod(PasswordTransformationMethod
							.getInstance());
				}
				mPswEditor.setSelection(mPswEditor.getText().length());
			}
			return;
		}
		// 一键注册btn
		if (v.equals(mQuickRegisterBtn)) {
			quickRegister();
			return;
		}
		// 注册
		if (v.equals(mRegistBtn)) {
			register();
			return;
		}
		if (v.equals(mAgreementTextView)) {
			UmipaySDKManager.showAgreementView(getActivity());
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
	}

	/**
	 * 一键登录函数
	 */
	private void quickRegister() {
		ListenerManager.setCommandRegistListener(this);
		UmipayCommandTaskManager.getInstance(getActivity()).QuickRegistCommandTask();
		startProgressDialog();
	}

	private void register() {
		String account = null;
		if (mAccountEditor != null) {
			account = mAccountEditor.getEditableText().toString();
		}
		String psw = null;
		if (mPswEditor != null) {
			psw = mPswEditor.getEditableText().toString();
		}
		if (TextUtils.isEmpty(account)) {
			toast("请输入有效的偶玩通行证账号");
			return;
		}
		if (TextUtils.isEmpty(psw) || psw.length() < 6) {
			toast("请输入6~32位长度的密码");
			return;
		}
		ListenerManager.setCommandRegistListener(this);
		UmipayCommandTaskManager.getInstance(getActivity()).RegistCommandTask(account, psw, "");

		startProgressDialog();
	}

	protected void handleRegistMsg(MsgData data) {
		stopProgressDialog();
		try {
			int code = data.getCode();
			String msg = data.getMsg();
			if (code == UmipaySDKStatusCode.SUCCESS) {
				UmipayAccount account = (UmipayAccount) data.getData();
				try {
					if (account.getBindMobile() == 1) {
						replaceFragmentFromActivityFragmentManager(BindMobileFragment
								.newInstance(account));
					} else {
						sendLoginResultMsg(UmipaySDKStatusCode.SUCCESS, null, UmipayLoginInfoDialog.REGISTER_AND_LOGIN,
								account);
						getActivity().finish();
					}
				} catch (Throwable e) {
					Debug_Log.e(e);
				}
			} else {
				msg = UmipaySDKStatusCode.handlerMessage(code, msg);
				toast(msg + "(" + code + ")");
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}

	protected void handleQuickRegistMsg(MsgData data) {
		stopProgressDialog();
		try {
			int code = data.getCode();
			String msg = data.getMsg();
			if (code == UmipaySDKStatusCode.SUCCESS) {
				replaceFragmentFromActivityFragmentManager(QuickRegisterSuccessFragment
						.newInstance());
			} else {
				msg = UmipaySDKStatusCode.handlerMessage(code, msg);
				toast(msg + "(" + code + ")");
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
}
