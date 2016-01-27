package com.oplay.giftcool.ui.fragment.setting;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.StatusCode;
import com.oplay.giftcool.listener.OnBackPressListener;
import com.oplay.giftcool.listener.OnShareListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.model.data.req.ReqModifyNick;
import com.oplay.giftcool.model.data.resp.ModifyNick;
import com.oplay.giftcool.model.data.resp.UserModel;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.activity.SettingActivity;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.InputMethodUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 16-1-17.
 */
public class SetNickFragment extends BaseFragment implements OnBackPressListener, TextWatcher {

	private final static String PAGE_NAME = "设置昵称";
	private final static String TOAST_FAILED = "修改失败";
	private TextView etNick;
	private TextView tvClear;


	public static SetNickFragment newInstance() {
		return new SetNickFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_user_set_nick);
		etNick = getViewById(R.id.et_nick);
		tvClear = getViewById(R.id.iv_clear);
	}

	@Override
	protected void setListener() {
		etNick.addTextChangedListener(this);
		tvClear.setOnClickListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		if (!AccountManager.getInstance().isLogin()) {
			etNick.setText("未知");
			getActivity().onBackPressed();
			IntentUtil.jumpLogin(getContext());
			return;
		}
		etNick.setText(AccountManager.getInstance().getUserInfo().nick);
		if (getActivity() != null) {
			((SettingActivity) getActivity()).setSaveVisibility(View.VISIBLE);
			((SettingActivity) getActivity()).setSaveListener(new OnShareListener() {
				@Override
				public void share() {
					KLog.e();
					handleSave();
				}
			});
		}
	}

	@Override
	protected void lazyLoad() {

	}

	@Override
	public boolean onBack() {
		// 隐藏输入框
		InputMethodUtil.hideSoftInput(mActivity);
		if (getActivity() != null) {
			((SettingActivity) getActivity()).setSaveVisibility(View.GONE);
		}
		return false;
	}

	public void showLoading() {
		if (getActivity() != null) {
			((BaseAppCompatActivity) getActivity()).showLoadingDialog("修改昵称中...");
		}
	}

	public void hideLoading() {
		if (getActivity() != null) {
			((BaseAppCompatActivity) getActivity()).hideLoadingDialog();
		}
	}


	private void handleSave() {
		if (mIsLoading) {
			return;
		}
		mIsLoading = true;
		showLoading();
		final String nick = etNick.getText().toString().trim();
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				ReqModifyNick modifyNick = new ReqModifyNick();
				modifyNick.newNick = nick;
				modifyNick.oldNick = AccountManager.getInstance().getUserInfo().nick;
				Global.getNetEngine().modifyUserNick(new JsonReqBase<ReqModifyNick>(modifyNick))
						.enqueue(new Callback<JsonRespBase<ModifyNick>>() {
							@Override
							public void onResponse(Response<JsonRespBase<ModifyNick>> response, Retrofit retrofit) {
								hideLoading();
								if (!mCanShowUI) {
									return;
								}
								mIsLoading = false;
								if (response != null && response.isSuccess()) {
									if (response.body() != null && response.body().getCode() == StatusCode.SUCCESS) {
										UserModel model = AccountManager.getInstance().getUser();
										model.userInfo.nick = response.body().getData().nick;
										AccountManager.getInstance().setUser(model);
										ScoreManager.getInstance().toastByCallback(response.body().getData());
										ToastUtil.showShort("修改成功");
										getActivity().onBackPressed();
										return;
									}
									if (AppDebugConfig.IS_DEBUG) {
										KLog.d(AppDebugConfig.TAG_FRAG, (response.body() == null ?
												"解析异常" : response.body().error()));
									}
									ToastUtil.blurErrorMsg(TOAST_FAILED, response.body());
									return;
								}
								ToastUtil.blurErrorResp(TOAST_FAILED, response);
							}

							@Override
							public void onFailure(Throwable t) {
								hideLoading();
								if (!mCanShowUI) {
									return;
								}
								mIsLoading = false;
								if (AppDebugConfig.IS_DEBUG) {
									KLog.d(AppDebugConfig.TAG_FRAG, t);
								}
								ToastUtil.blurThrow(TOAST_FAILED);
							}
						});
			}
		});
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.iv_clear:
				etNick.setText("");
				break;
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	@Override
	public void afterTextChanged(Editable s) {
		if (s.toString().trim().length() > 0) {
			tvClear.setVisibility(View.VISIBLE);
		} else {
			tvClear.setVisibility(View.GONE);
		}
		final String nick = s.toString().trim();
		if (TextUtils.isEmpty(nick) ||
				nick.equals(AccountManager.getInstance().getUserInfo().nick)) {
			if (getActivity() != null) {
				((SettingActivity) getActivity()).setSaveEnable(false);
			}
		} else {
			if (getActivity() != null) {
				((SettingActivity) getActivity()).setSaveEnable(true);
			}
		}
	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}
}
