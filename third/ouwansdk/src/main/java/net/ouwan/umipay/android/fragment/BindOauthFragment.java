package net.ouwan.umipay.android.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import net.ouwan.umipay.android.Utils.Util_Resource;
import net.ouwan.umipay.android.api.UmipaySDKManager;
import net.ouwan.umipay.android.api.UmipaySDKStatusCode;
import net.ouwan.umipay.android.asynctask.TaskCMD;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.entry.UmipayAccount;
import net.ouwan.umipay.android.entry.gson.Gson_Login;
import net.ouwan.umipay.android.interfaces.Interface_Bind_Oauth_Listener;
import net.ouwan.umipay.android.manager.ListenerManager;
import net.ouwan.umipay.android.manager.UmipayAccountManager;
import net.ouwan.umipay.android.manager.UmipayCommandTaskManager;

import java.util.regex.PatternSyntaxException;

/**
 * Created by jimmy on 2016/8/14.
 */
public class BindOauthFragment extends BaseFragment implements TextView.OnEditorActionListener,
		Interface_Bind_Oauth_Listener {

	public static final String ARG_ACCOUNT = "umipay_account";

	private UmipayAccount mUmipayAccount;
	private CheckBox mRememberPWCheckBox;

	private ViewStub mViewStub;
	private View mContentLayout;
	private Button mLoginBtn;
	private TextView mTitle;
	private View mRememberPWLayout;
	private EditText mAccountEditText;
	private TextView mAccountTextView;
	private EditText mPswEditor;
	private View mClearPswBtn;
	private CheckBox mViewPswCheckBox;
	private View mViewPswLayout;

	public static BindOauthFragment newInstance(UmipayAccount account) {
		Bundle bundle = new Bundle();
		bundle.putSerializable(ARG_ACCOUNT, account);
		BindOauthFragment fragment = new BindOauthFragment();
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
					"umipay_bindoauth_layout"), container, false);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		initViews();
		initPswFilter();
		initListener();
		return mRootLayout;
	}

	private void initViews() {
		if (mRootLayout != null) {

			String username = mUmipayAccount.getUserName();
			String fieldName = TextUtils.isEmpty(username)?"umipay_bindoauth_account_psw_vb":"umipay_bindoauth_psw_vb";

			mViewStub = (ViewStub) mRootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(),
					"id",
					fieldName));
			if (mViewStub != null) {
				mContentLayout =  mViewStub.inflate();
			}

			if(mContentLayout != null){
				mAccountEditText = (EditText) mContentLayout
						.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
								"umipay_name_box_et"));
				mAccountTextView = (TextView) mContentLayout
						.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
								"umipay_name_box_tv"));
				mPswEditor = (EditText) mContentLayout.findViewById(Util_Resource
						.getIdByReflection(getActivity(), "id", "umipay_psw_box"));
				mClearPswBtn = mContentLayout.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
						"umipay_reg_psw_clear_btn"));
				mRememberPWLayout = mContentLayout.findViewById(Util_Resource
						.getIdByReflection(getActivity(), "id", "umipay_remember_pw_layout"));
				mRememberPWCheckBox = (CheckBox) mContentLayout
						.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
								"umipay_remember_pw_cb"));
				mViewPswLayout = mContentLayout.findViewById(Util_Resource
						.getIdByReflection(getActivity(), "id", "umipay_psw_cb_layout"));

				mViewPswCheckBox = (CheckBox) mContentLayout
						.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
								"umipay_psw_cb"));

				mLoginBtn = (Button) mContentLayout.findViewById(Util_Resource
						.getIdByReflection(getActivity(), "id", "umipay_login_btn"));
				mTitle = (TextView) mRootLayout.findViewById(Util_Resource.getIdByReflection(getActivity(), "id",
						"umipay_title_tv"));
				if(mTitle != null){
					mTitle.setText("设置偶玩账号");
				}
			};
		}

		if(mAccountTextView != null){
			String content = mAccountTextView.getText().toString();
			mAccountTextView.setText(String.format(content,mUmipayAccount.getUserName()));
		}
		if (mPswEditor != null) {
			mPswEditor.setTransformationMethod(PasswordTransformationMethod
					.getInstance());
			mPswEditor.setOnEditorActionListener(this);
		}
	}

	private void initListener() {
		if (mLoginBtn != null) {
			mLoginBtn.setOnClickListener(this);
		}
		// 显示密码
		if (mViewPswLayout != null) {
			mViewPswLayout.setOnClickListener(this);
		}
		if(mRememberPWLayout != null){
			mRememberPWLayout.setOnClickListener(this);
		}
	}

	//绑定偶玩账号
	private void bindOauth() {
		String account = null;
		if (mAccountEditText != null) {
			account = mAccountEditText.getEditableText().toString();
		}
		if(mAccountTextView != null){
			account = mUmipayAccount.getUserName();
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
		// 记住密码lo
		if (mRememberPWLayout != null) {
			mRememberPWLayout.setOnClickListener(this);
		}
		ListenerManager.setmCommandBindOauth(this);
		mUmipayAccount.setRemenberPsw(mRememberPWCheckBox.isChecked());
		UmipayCommandTaskManager.getInstance(getActivity()).BindOauthCommandTask(account, psw);
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
						String username = null;
						if(mAccountEditText != null){
							username = mAccountEditText.getEditableText().toString();
						}else{
							username = mUmipayAccount.getUserName();
						}
						if (!TextUtils.isEmpty(username)
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
		if (mAccountEditText != null) {
			mAccountEditText.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {

				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					if (mLoginBtn != null && mAccountEditText != null) {
						if ( mAccountEditText.getText().length() > 0
								&& mPswEditor.getText().length() > 0) {
							mLoginBtn.setEnabled(true);
						} else {
							mLoginBtn.setEnabled(false);
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

	@Override
	public void onBindOauth(int code, String msg) {
		sendMessage(MSG_BIND_OAUTH, new MsgData(code, msg, null));
	}

	@Override
	protected void handleBindOauthMsg(MsgData data) {
		if (data != null) {
			int code = data.getCode();
			String msg = data.getMsg();
			stopProgressDialog();
			Debug_Log.d("error code : " + code);
			if (code == UmipaySDKStatusCode.SUCCESS) {
				sendLoginResultMsg(code, null, mUmipayAccount.getBindOauth(), mUmipayAccount);
				getActivity().finish();
			} else {
				msg = UmipaySDKStatusCode.handlerMessage(code, msg);
				toast("设定主账号失败(" + msg + ")，code = " + code);
			}
		} else {
			toast("设定账号失败");
		}
	}

	@Override
	protected void handleOnClick(View v) {
		if (v.equals(mLoginBtn)) {
			bindOauth();
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
		// 记住密码cb
		if (v.equals(mRememberPWLayout)) {
			mRememberPWCheckBox.setChecked(!mRememberPWCheckBox.isChecked());
			return;
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId != EditorInfo.IME_ACTION_DONE) {
			return false;
		}
		if (v.equals(mPswEditor)) {
			bindOauth();
			return false;
		}
		return false;
	}
}
