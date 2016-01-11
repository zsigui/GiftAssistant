package com.oplay.giftassistant.ui.fragment.user;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.config.NetUrl;
import com.oplay.giftassistant.config.StatusCode;
import com.oplay.giftassistant.manager.AccountManager;
import com.oplay.giftassistant.model.data.req.ReqLogin;
import com.oplay.giftassistant.model.data.resp.UserModel;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_WithName;
import com.oplay.giftassistant.util.InputTextUtil;
import com.oplay.giftassistant.util.NetworkUtil;
import com.socks.library.KLog;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 16-1-11.
 */
public class OuwanLoginFragment extends BaseFragment_WithName {

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
	}

	@Override
	protected void setListener() {
		btnLogin.setOnClickListener(this);
		tvLaw.setOnClickListener(this);
		ctvAgreeLaw.setOnClickListener(this);
		tvAnotherLogin.setOnClickListener(this);
		tvClear.setOnClickListener(this);
		tvForgetPwd.setOnClickListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		InputTextUtil.initPswFilter(etUser, etPwd, tvClear, btnLogin);
		ctvAgreeLaw.setChecked(true);
		btnLogin.setEnabled(false);
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
				break;
		}
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
				/*if (!NetworkUtil.isConnected(getContext())) {
					hideLoading();
					showToast("网络连接失败");
					return;
				}*/
				KLog.e(NetworkUtil.getConnectedType(getContext()));
				KLog.e(NetworkUtil.isConnectedOrConnecting(getContext()));
				KLog.e(NetworkUtil.isAvailable(getContext()));
				KLog.e(NetworkUtil.isConnected(getContext()));

				Global.getNetEngine().login(NetUrl.USER_PHONE_LOGIN_SECOND, new JsonReqBase<ReqLogin>(login))
						.enqueue(new Callback<JsonRespBase<UserModel>>() {
							@Override
							public void onResponse(Response<JsonRespBase<UserModel>> response, Retrofit retrofit) {
								hideLoading();
								if (response != null && response.isSuccess()) {
									if (response.body() != null
											&& response.body().getCode() == StatusCode.SUCCESS) {
										AccountManager.getInstance().setUser(response.body().getData());
										((BaseAppCompatActivity) getActivity()).popOrExit();
										return;
									}
									showToast("登录失败 - " + (response.body() == null ?
											"解析异常" : response.body().getMsg()));

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
}
