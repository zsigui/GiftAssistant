package com.oplay.giftcool.ui.fragment.login;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
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
import com.oplay.giftcool.manager.OuwanSDKManager;
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
public class OuwanLoginFragment extends BaseFragment implements TextView.OnEditorActionListener,
		OnItemClickListener<String>, View.OnFocusChangeListener, OnBackPressListener {

	private final static String PAGE_NAME = "偶玩登录页";
	private final static String ERR_PREFIX = "登录失败";
	private AutoCompleteTextView etUser;
	private TextView tvUserClear;
	private TextView tvPwdClear;
	private EditText etPwd;
	//使用条款先注释掉，估计后面还要改回来
//	private CheckedTextView ctvAgreeLaw;
//	private TextView tvLaw;
	private TextView btnLogin;
	private TextView tvAnotherLogin;
	private TextView tvForgetPwd;
	private ImageView ivMore;
	private PopupWindow mAccountPopup;
	private MaxRowListView mPopupListView;
	private AccountAdapter mAccountAdapter;
	private AccountAdapter mCompleteAdapter;
	private ArrayList<String> mData;
	private boolean mNeedEncrypt = true;

	public static OuwanLoginFragment newInstance() {
		return new OuwanLoginFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_login_ouwan);
		etUser = getViewById(R.id.et_input);
		tvUserClear = getViewById(R.id.tv_user_clear);
		tvPwdClear = getViewById(R.id.tv_pwd_clear);
		etPwd = getViewById(R.id.et_pwd);
//		ctvAgreeLaw = getViewById(R.id.ctv_law);
//		tvLaw = getViewById(R.id.tv_law);
		btnLogin = getViewById(R.id.btn_send);
		tvAnotherLogin = getViewById(R.id.tv_another_login);
		tvForgetPwd = getViewById(R.id.tv_forget_pwd);
		ivMore = getViewById(R.id.iv_more);
	}

	@Override
	protected void setListener() {
		getViewById(R.id.ll_pwd).setOnClickListener(this);
		getViewById(R.id.ll_input).setOnClickListener(this);
		btnLogin.setOnClickListener(this);
//		tvLaw.setOnClickListener(this);
//		ctvAgreeLaw.setOnClickListener(this);
		tvAnotherLogin.setOnClickListener(this);
		tvUserClear.setOnClickListener(this);
		tvPwdClear.setOnClickListener(this);
		tvForgetPwd.setOnClickListener(this);
		etUser.setOnEditorActionListener(this);
		etPwd.setOnEditorActionListener(this);
		ivMore.setOnClickListener(this);
		etUser.setOnFocusChangeListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		InputTextUtil.initPswFilter(etUser, etPwd, tvUserClear, tvPwdClear, btnLogin);
//		ctvAgreeLaw.setChecked(true);
		btnLogin.setEnabled(false);
		etUser.requestFocus();
		InputMethodUtil.showSoftInput(getActivity());
		mData = AccountManager.getInstance().readOuwanAccount();
		if (mData != null && mData.size() > 0) {
			String[] s = mData.get(0).split(",");
			etUser.setText(s[0]);
			if (AssistantApp.getInstance().isRememberPwd() && (s.length == 2 && !TextUtils.isEmpty(s[1]))) {
				etPwd.setText(s[1]);
				mNeedEncrypt = false;
			}
		}
		mAccountAdapter = new AccountAdapter(getContext(), mData, true);
		mCompleteAdapter = new AccountAdapter(getContext(), mData, true);
		View popup = View.inflate(getContext(), R.layout.listview_account_popup, null);
		mPopupListView = getViewById(popup, R.id.lv_popup_list_content);
		mPopupListView.setAdapter(mAccountAdapter);
		etUser.setAdapter(mCompleteAdapter);
		mAccountPopup = new PopupWindow(popup, LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT, true);
		mAccountAdapter.setListener(this);
		mCompleteAdapter.setListener(this);
		mAccountPopup.setOutsideTouchable(true);
		mAccountPopup.setBackgroundDrawable(new BitmapDrawable());
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
//			case R.id.tv_law:
//				 显示条款弹窗
//				break;
			case R.id.tv_forget_pwd:
				OuwanSDKManager.getInstance().showForgetPswView(getContext());
				break;
//			case R.id.ctv_law:
//				if (ctvAgreeLaw.isChecked()) {
//					ctvAgreeLaw.setChecked(false);
//				} else {
//					ctvAgreeLaw.setChecked(true);
//				}
//				break;
			case R.id.iv_more:
				if (mData == null || mData.size() == 0) {
					return;
				}
				etUser.clearFocus();
				if (mAccountPopup.isShowing()) {
					mAccountPopup.dismiss();
				} else {
					mAccountPopup.showAsDropDown(etUser);
				}
				break;
			case R.id.tv_another_login:
				((BaseAppCompatActivity) getActivity()).replaceFragWithTitle(R.id.fl_container,
						PhoneLoginFragment.newInstance(), getResources().getString(R.string.st_login_phone_title),
						false);
				break;
			case R.id.tv_user_clear:
				clearText(etUser);
				break;
			case R.id.tv_pwd_clear:
				clearText(etPwd);
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

	private void clearText(EditText et) {
		et.setText("");
		et.requestFocus();
		et.setSelection(0);
	}

	private void handleLogin() {
		showLoading();
		final ReqLogin login = new ReqLogin();
		if (!login.setOuwanUser(etUser.getText().toString(), etPwd.getText().toString(), mNeedEncrypt)) {
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
				if (mNeedEncrypt) {
					AccountManager.getInstance().writeOuwanAccount(login.getUsername() + "," + login.getPassword(),
							mData, false);
				} else {
					AccountManager.getInstance().writeOuwanAccount(login.getUsername() + ",",
							mData, false);
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
										MainActivity.sIsTodayFirstOpen = true;
										ScoreManager.getInstance().resetLocalTaskState();
										KLog.i("userUpdate", "LoginSuccess.userModel = " + userModel + ", setUser");
										AccountManager.getInstance().setUser(userModel);
										if (getActivity() != null) {
											((BaseAppCompatActivity) getActivity()).handleBackPressed();
										}
										return;
									}
									ToastUtil.blurErrorMsg(ERR_PREFIX, response.body());
									return;

								}
								ToastUtil.blurErrorResp(ERR_PREFIX, response);
							}

							@Override
							public void onFailure(Throwable t) {
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
		if (getActivity() != null) {
			((BaseAppCompatActivity) getActivity()).hideLoadingDialog();
		}
	}

	public void showLoading() {
		if (getActivity() != null) {
			((BaseAppCompatActivity) getActivity()).showLoadingDialog();
		}
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

	@Override
	public void onItemClick(String item, View view, int position) {
		switch (view.getId()) {
			case R.id.ll_item:
				String[] s = item.split(",");
				if (s.length == 2) {
					etUser.setText(s[0]);
					etPwd.setText(s[1]);
				} else {
					etUser.setText(s[0]);
				}
				etUser.dismissDropDown();
				if (mAccountPopup != null && mAccountPopup.isShowing()) {
					mAccountPopup.dismiss();
				}
				break;
			case R.id.iv_account_list_delete:
				AccountManager.getInstance().writeOuwanAccount(item, mData, true);
				if (mAccountPopup != null && mAccountPopup.isShowing()) {
					mAccountPopup.dismiss();
				}
				mCompleteAdapter.notifyDataChanged();
				mAccountAdapter.notifyDataChanged();
				break;
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		switch (v.getId()) {
			case R.id.et_input:
				tvPwdClear.setVisibility(View.GONE);
				if (hasFocus && !TextUtils.isEmpty(etUser.getText().toString().trim())) {
					tvUserClear.setVisibility(View.VISIBLE);
				} else {
					tvUserClear.setVisibility(View.GONE);
				}
				break;
			case R.id.et_pwd:
				tvUserClear.setVisibility(View.GONE);
				if (hasFocus && !TextUtils.isEmpty(etPwd.getText().toString().trim())) {
					tvPwdClear.setVisibility(View.VISIBLE);
				} else {
					tvPwdClear.setVisibility(View.GONE);
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
		if (etUser.isPopupShowing()) {
			etUser.dismissDropDown();
			return true;
		}
		return false;
	}
}
