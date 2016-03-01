package com.oplay.giftcool.ui.fragment.login;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.AccountAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.config.StatusCode;
import com.oplay.giftcool.config.UserTypeUtil;
import com.oplay.giftcool.listener.OnBackPressListener;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.ScoreManager;
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
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

import net.ouwan.umipay.android.view.MaxRowListView;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 16-1-11.
 */
public class PhoneLoginFragment extends BaseFragment implements TextView.OnEditorActionListener,
		OnItemClickListener<String>, View.OnFocusChangeListener, OnBackPressListener {

	private final static String PAGE_NAME = "手机号登录";
	private final static String ERR_PREFIX = "登录失败";
	private AutoCompleteTextView etPhone;
	private EditText etCode;
	private TextView tvPhoneClear;
	private TextView tvCodeClear;
	private EditText etClearFocus;
	private TextView btnSendCode;
	//	private CheckedTextView ctvAgreeLaw;
//	private TextView tvLaw;
	private TextView btnLogin;
	private TextView tvAnotherLogin;
	private LinearLayout llPhone;
	private ImageView ivMore;

	private PopupWindow mAccountPopup;
	private AccountAdapter mAccountAdapter;
	private AccountAdapter mCompleteAdapter;
	private ArrayList<String> mData;

	// 倒计时剩余时间，每次发送后刷新
	private final static int RESEND_DURATION = 60;
	private int sSendCodeRemainTime = 0;

	private Handler mHandler = new Handler(Looper.getMainLooper());
	private Runnable setTimeRunnable = new Runnable() {
		@Override
		public void run() {
			if (sSendCodeRemainTime == 0) {
				btnSendCode.setEnabled(true);
				btnSendCode.setTextColor(AssistantApp.getInstance().getResources().getColor(R.color.co_btn_green));
				btnSendCode.setText(getResources().getString(R.string.st_login_phone_send_code));
			} else {
				sSendCodeRemainTime--;
				btnSendCode.setText(
						String.format(getResources().getString(R.string.st_login_phone_resend_code),
								sSendCodeRemainTime));
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
		tvPhoneClear = getViewById(R.id.tv_user_clear);
		tvCodeClear = getViewById(R.id.tv_code_clear);
		etClearFocus = getViewById(R.id.et_clear_focus);
		etCode = getViewById(R.id.et_phone_code);
		btnSendCode = getViewById(R.id.tv_send_code);
		ivMore = getViewById(R.id.iv_more);
//		ctvAgreeLaw = getViewById(R.id.ctv_law);
//		tvLaw = getViewById(R.id.tv_law);
		btnLogin = getViewById(R.id.btn_send);
		tvAnotherLogin = getViewById(R.id.tv_another_login);
		llPhone = getViewById(R.id.ll_input);
	}

	@Override
	protected void setListener() {
		btnLogin.setOnClickListener(this);
//		tvLaw.setOnClickListener(this);
//		ctvAgreeLaw.setOnClickListener(this);
		btnSendCode.setOnClickListener(this);
		tvAnotherLogin.setOnClickListener(this);
		tvPhoneClear.setOnClickListener(this);
		tvCodeClear.setOnClickListener(this);
		etPhone.setOnEditorActionListener(this);
		etCode.setOnEditorActionListener(this);
		getViewById(R.id.ll_code).setOnClickListener(this);
		llPhone.setOnClickListener(this);
		ivMore.setOnClickListener(this);
		etPhone.setOnFocusChangeListener(this);
		etCode.setOnFocusChangeListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		InputTextUtil.initPswFilter(etPhone, etCode, tvPhoneClear, tvCodeClear, btnLogin, btnSendCode, false);
//		ctvAgreeLaw.setChecked(true);
		btnLogin.setEnabled(false);
		btnSendCode.setEnabled(false);
		initHint();
	}

	private void initHint() {
		mData = AccountManager.getInstance().readPhoneAccount();
		if (mData != null && mData.size() > 0) {
			etPhone.setText(mData.get(0));
			etCode.requestFocus();
			ivMore.setVisibility(View.VISIBLE);
		} else {
			etPhone.requestFocus();
			ivMore.setVisibility(View.GONE);
		}
		InputMethodUtil.showSoftInput(getActivity());
		mAccountAdapter = new AccountAdapter(getContext(), mData, false);
		mCompleteAdapter = new AccountAdapter(getContext(), mData, false);
		View popup = View.inflate(getContext(), R.layout.listview_account_popup, null);
		MaxRowListView popupListView = getViewById(popup, R.id.lv_popup_list_content);
		popupListView.setAdapter(mAccountAdapter);
		etPhone.setAdapter(mCompleteAdapter);
		mAccountPopup = new PopupWindow(popup, LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT, true);
		mAccountAdapter.setListener(this);
		mCompleteAdapter.setListener(this);
		mAccountPopup.setOutsideTouchable(true);
		mAccountPopup.setBackgroundDrawable(new BitmapDrawable());
		etPhone.setDropDownBackgroundDrawable(null);
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
			case R.id.tv_send_code:
				handleGetCode();
				break;
			case R.id.iv_more:
				etClearFocus.requestFocus();
				InputMethodUtil.hideSoftInput(getActivity());
				if (mData == null || mData.size() == 0) {
					return;
				}
				if (mAccountPopup.isShowing()) {
					mAccountPopup.dismiss();
				} else {
					mAccountPopup.showAsDropDown(llPhone);
				}
				break;
			case R.id.tv_another_login:
				((BaseAppCompatActivity) getActivity()).replaceFragWithTitle(R.id.fl_container,
						OuwanLoginFragment.newInstance(), getResources().getString(R.string.st_login_ouwan_title),
						false);
				break;
			case R.id.tv_user_clear:
				clearText(etPhone);
				btnSendCode.setEnabled(false);
				btnSendCode.setTextColor(AssistantApp.getInstance().getResources().getColor(R.color.co_btn_grey));
				break;
			case R.id.tv_code_clear:
				clearText(etCode);
				break;
			case R.id.ll_input:
				etPhone.setSelection(etPhone.getText().length());
				etPhone.requestFocus();
				break;
			case R.id.ll_code:
				etCode.setSelection(etCode.getText().length());
				etCode.requestFocus();
				break;
			default:
				if (mAccountPopup != null && mAccountPopup.isShowing()) {
					mAccountPopup.dismiss();
				}
				if (etPhone.isPopupShowing()) {
					etPhone.dismissDropDown();
				}
		}
	}

	private void clearText(EditText et) {
		et.setText("");
		et.requestFocus();
		et.setSelection(0);
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
		btnSendCode.setTextColor(AssistantApp.getInstance().getResources().getColor(R.color.co_btn_grey));
		sSendCodeRemainTime = RESEND_DURATION;
		mHandler.postDelayed(setTimeRunnable, 1000);
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (!NetworkUtil.isConnected(AssistantApp.getInstance().getApplicationContext())) {
					hideLoading();
					showToast("发送失败 - 网络异常");
					sSendCodeRemainTime = 0;
					return;
				}


				Global.getNetEngine().login(NetUrl.USER_PHONE_LOGIN_FIRST, new JsonReqBase<ReqLogin>(login))
						.enqueue(new Callback<JsonRespBase<UserModel>>() {
							@Override
							public void onResponse(Response<JsonRespBase<UserModel>> response, Retrofit retrofit) {
								if (!mCanShowUI) {
									return;
								}
								hideLoading();
								if (response != null && response.isSuccess()) {
									if (response.body() != null
											&& response.body().getCode() == StatusCode.SUCCESS) {
										showToast("短信已经发送，请注意接收");
										return;
									}
									showToast("发送失败 - " + (response.body() == null ?
											"解析异常" : response.body().getMsg()));
									resetRemain();
									return;
								}
								showToast("发送失败 - 返回出错:" + (response == null ? "" : response.message()));
								resetRemain();

							}

							@Override
							public void onFailure(Throwable t) {
								if (!mCanShowUI) {
									return;
								}
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
		etCode.requestFocus();
		etCode.setSelection(etCode.getText().toString().length());
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
										UserModel userModel = response.body().getData();
										userModel.userInfo.loginType = UserTypeUtil.TYPE_POHNE;
										MainActivity.sIsTodayFirstOpen = true;
										AccountManager.getInstance().writePhoneAccount(login.getPhone(), mData, false);
										AccountManager.getInstance().notifyUserAll(userModel);
										ScoreManager.getInstance().resetLocalTaskState();
										ScoreManager.getInstance().toastByCallback(userModel, false);
										((BaseAppCompatActivity) getActivity()).handleBackPressed();
										return;
									}
									ToastUtil.blurErrorMsg(ERR_PREFIX, response.body());
									return;
								}
								ToastUtil.blurErrorResp(ERR_PREFIX, response);
							}

							@Override
							public void onFailure(Throwable t) {
								if (!mCanShowUI) {
									return;
								}
								hideLoading();
								if (AppDebugConfig.IS_DEBUG) {
									KLog.e(t);
								}
								ToastUtil.blurThrow(ERR_PREFIX);
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
				etCode.requestFocus();
				etCode.setSelection(etCode.getText().toString().length());
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

	@Override
	public void onItemClick(String item, View view, int position) {
		switch (view.getId()) {
			case R.id.ll_item:
				etPhone.setText(item);
				etCode.requestFocus();
				break;
			case R.id.iv_account_list_delete:
				if (item.equals(etPhone.getText().toString().trim())) {
					etPhone.setText("");
					etCode.setText("");
					etPhone.requestFocus();
				}
				AccountManager.getInstance().writePhoneAccount(item, mData, true);
				if (mData == null || mData.size() == 0) {
					ivMore.setVisibility(View.GONE);
				}
				mCompleteAdapter.notifyDataChanged();
				mAccountAdapter.notifyDataChanged();
				break;
		}
		etPhone.dismissDropDown();
		if (mAccountPopup != null && mAccountPopup.isShowing()) {
			mAccountPopup.dismiss();
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		switch (v.getId()) {
			case R.id.et_input:
				tvCodeClear.setVisibility(View.GONE);
				if (hasFocus && !TextUtils.isEmpty(etPhone.getText().toString().trim())) {
					tvPhoneClear.setVisibility(View.VISIBLE);
				} else {
					tvPhoneClear.setVisibility(View.GONE);
				}
				break;
			case R.id.et_phone_code:
				tvPhoneClear.setVisibility(View.GONE);
				if (hasFocus && !TextUtils.isEmpty(etCode.getText().toString().trim())) {
					tvCodeClear.setVisibility(View.VISIBLE);
				} else {
					tvCodeClear.setVisibility(View.GONE);
				}
				break;
		}
	}

	@Override
	public boolean onBack() {
		if (mAccountPopup != null && mAccountPopup.isShowing()) {
			mAccountPopup.dismiss();
			return true;
		}
		if (etPhone.isPopupShowing()) {
			etPhone.dismissDropDown();
			return true;
		}
		return false;
	}
}
