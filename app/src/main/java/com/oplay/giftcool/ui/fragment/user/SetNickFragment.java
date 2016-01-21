package com.oplay.giftcool.ui.fragment.user;

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
import com.oplay.giftcool.handler.ScoreHandler;
import com.oplay.giftcool.listener.OnBackPressListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.model.data.req.ReqModifyNick;
import com.oplay.giftcool.model.data.resp.ModifyNick;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.InputMethodUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 16-1-17.
 */
public class SetNickFragment extends BaseFragment implements OnBackPressListener, TextWatcher {

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
	}

	@Override
	protected void lazyLoad() {

	}

	@Override
	public boolean onBack() {
		// 隐藏输入框
		InputMethodUtil.hideSoftInput(mActivity);

		// 做任务的情况下
		if (ScoreHandler.sIsTasking) {
			AccountManager.getInstance().updateUserInfo();
		}

		final String nick = etNick.getText().toString().trim();
		if (TextUtils.isEmpty(nick) ||
				nick.equals(AccountManager.getInstance().getUserInfo().nick)) {
			return false;
		}
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (!NetworkUtil.isConnected(getContext())) {
					ToastUtil.showShort("网络错误，修改昵称失败!");
					return;
				}
				ReqModifyNick modifyNick = new ReqModifyNick();
				modifyNick.newNick = nick;
				modifyNick.oldNick = AccountManager.getInstance().getUserInfo().nick;
				Global.getNetEngine().modifyUserNick(new JsonReqBase<ReqModifyNick>(modifyNick))
						.enqueue(new Callback<JsonRespBase<ModifyNick>>() {
							@Override
							public void onResponse(Response<JsonRespBase<ModifyNick>> response, Retrofit retrofit) {
								if (response != null && response.isSuccess()) {
									if (response.body() != null && response.body().getCode() == StatusCode.SUCCESS) {
										AccountManager.getInstance().getUserInfo().nick = response.body().getData().nick;
										ObserverManager.getInstance().notifyUserUpdate();
										return;
									}
									if (AppDebugConfig.IS_DEBUG) {
										KLog.d(AppDebugConfig.TAG_FRAG, (response.body() == null?
												"解析异常" : response.body().error()));
									}
								}
							}

							@Override
							public void onFailure(Throwable t) {
								if (AppDebugConfig.IS_DEBUG) {
									KLog.d(AppDebugConfig.TAG_FRAG, t);
								}
							}
						});
			}
		});
		return false;
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
	}
}
