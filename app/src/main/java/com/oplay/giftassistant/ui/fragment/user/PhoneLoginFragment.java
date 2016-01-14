package com.oplay.giftassistant.ui.fragment.user;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AutoCompleteTextView;
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
public class PhoneLoginFragment extends BaseFragment_WithName {

	private AutoCompleteTextView etPhone;
	private TextView tvClear;
	private EditText etCode;
	private TextView btnSendCode;
	private CheckedTextView ctvAgreeLaw;
	private TextView tvLaw;
	private TextView btnLogin;
	private TextView tvAnotherLogin;

	// 倒计时剩余时间，每次发送后刷新
	private final static int RESEND_DURATION = 60;
	private int sSendCodeRemainTime = 0;

	private Handler mHandler = new Handler(Looper.getMainLooper());
	private Runnable setTimeRunnable = new Runnable() {
		@Override
		public void run() {
			if (sSendCodeRemainTime == 0) {
				btnSendCode.setEnabled(true);
				btnSendCode.setText(getResources().getString(R.string.st_login_phone_send_code));
			} else {
				btnSendCode.setText(
						String.format(getResources().getString(R.string.st_login_phone_resend_code),
								sSendCodeRemainTime));
				sSendCodeRemainTime--;
				mHandler.postDelayed(setTimeRunnable, 1000);
			}
		}
	};

	public static PhoneLoginFragment newInstance() {
		return new PhoneLoginFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_login_phone);
		etPhone = getViewById(R.id.et_input);
		tvClear = getViewById(R.id.tv_clear);
		etCode = getViewById(R.id.et_phone_code);
		btnSendCode = getViewById(R.id.tv_send_code);
		ctvAgreeLaw = getViewById(R.id.ctv_law);
		tvLaw = getViewById(R.id.tv_law);
		btnLogin = getViewById(R.id.btn_send);
		tvAnotherLogin = getViewById(R.id.tv_another_login);
	}

	@Override
	protected void setListener() {
		btnLogin.setOnClickListener(this);
		tvLaw.setOnClickListener(this);
		ctvAgreeLaw.setOnClickListener(this);
		btnSendCode.setOnClickListener(this);
		tvAnotherLogin.setOnClickListener(this);
		tvClear.setOnClickListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		InputTextUtil.initPswFilter(etPhone, etCode, tvClear, btnLogin);
		ctvAgreeLaw.setChecked(true);
		btnLogin.setEnabled(false);
		if (sSendCodeRemainTime > 0) {
			mHandler.post(setTimeRunnable);
			btnSendCode.setEnabled(false);
		}
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
			case R.id.tv_send_code:
				handleGetCode();
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
						OuwanLoginFragment.newInstance(), getResources().getString(R.string.st_login_ouwan_title),
						false);
				break;
			case R.id.tv_clear:
				etPhone.setText("");
				break;
		}
	}

	private void handleGetCode() {
		showLoading();
		final ReqLogin login = new ReqLogin();
		if (!login.setPhoneUser(etPhone.getText().toString())) {
			hideLoading();
			showToast("手机号码格式不符合要求");
			return;
		}
		btnSendCode.setEnabled(false);
		btnSendCode.setText(String.format(getResources().getString(R.string.st_login_phone_resend_code),
				sSendCodeRemainTime));
		sSendCodeRemainTime = RESEND_DURATION;
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (!NetworkUtil.isConnected(getContext())) {
					hideLoading();
					showToast("发送失败 - 网络异常");
					return;
				}
				mHandler.postDelayed(setTimeRunnable, 1000);


				Global.getNetEngine().login(NetUrl.USER_PHONE_LOGIN_FIRST, new JsonReqBase<ReqLogin>(login))
						.enqueue(new Callback<JsonRespBase<UserModel>>() {
							@Override
							public void onResponse(Response<JsonRespBase<UserModel>> response, Retrofit retrofit) {
								hideLoading();
								if (response != null && response.isSuccess()) {
									if (response.body() != null
											&& response.body().getCode() == StatusCode.SUCCESS) {
										KLog.e(response.body().getData());
										showToast("短信已经发送，请注意接收");
										return;
									}
									showToast("发送失败 - " + (response.body() == null ?
											"解析异常" : response.body().getMsg()));
									resetRemain();
									return;
								}
								showToast("发送失败 - 解析异常");
								resetRemain();

							}

							@Override
							public void onFailure(Throwable t) {
								hideLoading();
								if (AppDebugConfig.IS_DEBUG) {
									KLog.e(t);
								}
								showToast("发送失败 - 网络异常");
								resetRemain();
							}
						});
			}
		});
	}

	private void resetRemain() {
		mHandler.removeCallbacks(setTimeRunnable);
		sSendCodeRemainTime = 0;
		mHandler.postAtFrontOfQueue(setTimeRunnable);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mHandler.removeCallbacks(setTimeRunnable);
	}

	private void handleLogin() {
		showLoading();
		final ReqLogin login = new ReqLogin();
		if (!login.setPhoneUser(etPhone.getText().toString(), etCode.getText().toString())) {
			hideLoading();
			showToast("手机号码格式不符合要求");
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

				Global.getNetEngine().login(NetUrl.USER_PHONE_LOGIN_SECOND, new JsonReqBase<ReqLogin>(login))
						.enqueue(new Callback<JsonRespBase<UserModel>>() {
							@Override
							public void onResponse(Response<JsonRespBase<UserModel>> response, Retrofit retrofit) {
								hideLoading();
								if (response != null && response.isSuccess()) {
									if (response.body() != null
											&& response.body().getCode() == StatusCode.SUCCESS) {
										AccountManager.getInstance().setUser(response.body().getData());
										((BaseAppCompatActivity) getActivity()).handleBackPressed();
										return;
									}
									showToast("登录失败 - " + (response.body() == null ?
											"解析异常" : response.body().getMsg()));

								}
								showToast("登录失败 - 解析异常");
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