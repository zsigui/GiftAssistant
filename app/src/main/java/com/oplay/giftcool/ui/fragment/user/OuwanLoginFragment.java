package com.oplay.giftcool.ui.fragment.user;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.config.StatusCode;
import com.oplay.giftcool.config.UserTypeUtil;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.model.data.req.ReqLogin;
import com.oplay.giftcool.model.data.resp.UserModel;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.activity.MainActivity;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.InputMethodUtil;
import com.oplay.giftcool.util.InputTextUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.SPUtil;
import com.socks.library.KLog;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 16-1-11.
 */
public class OuwanLoginFragment extends BaseFragment implements TextView.OnEditorActionListener {

	private final static String PAGE_NAME = "偶玩登录页";
	private EditText etUser;
	private TextView tvClear;
	private EditText etPwd;
	private CheckedTextView ctvAgreeLaw;
	private TextView tvLaw;
	private TextView btnLogin;
	private TextView tvAnotherLogin;
	private TextView tvForgetPwd;

	public static OuwanLoginFragment newInstance() {
		return new OuwanLoginFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_login_ouwan);
		etUser = getViewById(R.id.et_input);
		tvClear = getViewById(R.id.tv_clear);
		etPwd = getViewById(R.id.et_pwd);
		ctvAgreeLaw = getViewById(R.id.ctv_law);
		tvLaw = getViewById(R.id.tv_law);
		btnLogin = getViewById(R.id.btn_send);
		tvAnotherLogin = getViewById(R.id.tv_another_login);
		tvForgetPwd = getViewById(R.id.tv_forget_pwd);
		getViewById(R.id.ll_pwd).setOnClickListener(this);
		getViewById(R.id.ll_input).setOnClickListener(this);
	}

	@Override
	protected void setListener() {
		btnLogin.setOnClickListener(this);
		tvLaw.setOnClickListener(this);
		ctvAgreeLaw.setOnClickListener(this);
		tvAnotherLogin.setOnClickListener(this);
		tvClear.setOnClickListener(this);
		tvForgetPwd.setOnClickListener(this);
		etUser.setOnEditorActionListener(this);
		etPwd.setOnEditorActionListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		InputTextUtil.initPswFilter(etUser, etPwd, tvClear, btnLogin);
		ctvAgreeLaw.setChecked(true);
		btnLogin.setEnabled(false);
		etUser.requestFocus();
		InputMethodUtil.showSoftInput(getActivity());
		etUser.setText(readFromHistory());
	}

	@Override
	protected void lazyLoad() {

	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.btn_send:
				handleLogin();
				break;
			case R.id.tv_law:
				// 显示条款弹窗
				break;
			case R.id.tv_forget_pwd:

				break;
			case R.id.ctv_law:
				if (ctvAgreeLaw.isChecked()) {
					ctvAgreeLaw.setChecked(false);
				} else {
					ctvAgreeLaw.setChecked(true);
				}
				break;
			case R.id.tv_another_login:
				((BaseAppCompatActivity) getActivity()).replaceFragWithTitle(R.id.fl_container,
						PhoneLoginFragment.newInstance(), getResources().getString(R.string.st_login_phone_title),
						false);
				break;
			case R.id.tv_clear:
				etUser.setText("");
				etPwd.setText("");
				etUser.requestFocus();
				etUser.setSelection(0);
				etUser.setImeOptions(EditorInfo.IME_ACTION_NEXT);
				etPwd.setImeOptions(EditorInfo.IME_ACTION_NEXT);
				break;
			case R.id.ll_input:
				etUser.setSelection(etUser.getText().length());
				etUser.requestFocus();
				break;
			case R.id.ll_pwd:
				etPwd.setSelection(etPwd.getText().length());
				etPwd.requestFocus();
				break;
		}
	}

	public void writeToHistory(String name) {
		String history = SPUtil.getString(getContext(), SPConfig.SP_LOGIN_FILE, SPConfig.KEY_LOGIN_OUWAN, "");
		if (history.contains(name)) {
			return;
		}
		if ("".equals(history)) {
			history = name;
		} else {
			history = name + "," + history;
		}
		SPUtil.putString(getContext(), SPConfig.SP_LOGIN_FILE, SPConfig.KEY_LOGIN_OUWAN, history);
	}

	public String readFromHistory() {
		String history = SPUtil.getString(getContext(), SPConfig.SP_LOGIN_FILE, SPConfig.KEY_LOGIN_OUWAN, "");
		return "".equals(history) ? "" : history.substring(0,
				(!history.contains(",") ? history.length() : history.indexOf(",")));
	}

	private void handleLogin() {
		showLoading();
		final ReqLogin login = new ReqLogin();
		if (!login.setOuwanUser(etUser.getText().toString(), etPwd.getText().toString())) {
			hideLoading();
			showToast("账号密码格式不符合要求");
			return;
		}
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (!NetworkUtil.isConnected(getContext())) {
					hideLoading();
					showToast("网络连接失败");
					return;
				}
				Global.getNetEngine().login(NetUrl.USER_OUWAN_LOGIN, new JsonReqBase<ReqLogin>(login))
						.enqueue(new Callback<JsonRespBase<UserModel>>() {
							@Override
							public void onResponse(Response<JsonRespBase<UserModel>> response, Retrofit retrofit) {
								hideLoading();
								if (response != null && response.isSuccess()) {
									if (response.body() != null
											&& response.body().getCode() == StatusCode.SUCCESS) {
										UserModel userModel = response.body().getData();
										userModel.userInfo.loginType = UserTypeUtil.TYPE_OUWAN;
										writeToHistory(login.getUsername());
										MainActivity.sIsTodayFirstOpen = true;
										AccountManager.getInstance().setUser(userModel);
										((BaseAppCompatActivity) getActivity()).handleBackPressed();
										return;
									}
									showToast("登录失败 - " + (response.body() == null ?
											"解析异常" : response.body().getMsg()));
									return;

								}
								showToast("登录失败 - 网络异常");
							}

							@Override
							public void onFailure(Throwable t) {
								hideLoading();
								if (AppDebugConfig.IS_DEBUG) {
									KLog.e(t);
								}
								showToast("登录失败 - 网络异常");
							}
						});
			}
		});
	}

	public void hideLoading() {
		((BaseAppCompatActivity) getActivity()).hideLoadingDialog();
	}

	public void showLoading() {
		((BaseAppCompatActivity) getActivity()).showLoadingDialog();
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		switch (actionId) {
			case EditorInfo.IME_ACTION_NEXT:
				if (TextUtils.isEmpty(etPwd.getText().toString().trim())) {
					etPwd.requestFocus();
					etPwd.setSelection(etPwd.getText().toString().length());
				} else {
					etUser.requestFocus();
					etUser.setSelection(etUser.getText().toString().length());
				}
				break;
			case EditorInfo.IME_ACTION_DONE:
				handleLogin();
				break;
		}
		return false;
	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}
}
