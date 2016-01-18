package com.oplay.giftassistant.ui.fragment.user;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.config.StatusCode;
import com.oplay.giftassistant.listener.OnBackPressListener;
import com.oplay.giftassistant.manager.AccountManager;
import com.oplay.giftassistant.manager.ObserverManager;
import com.oplay.giftassistant.model.data.req.ReqModifyNick;
import com.oplay.giftassistant.model.data.resp.ModifyNick;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;
import com.oplay.giftassistant.util.NetworkUtil;
import com.oplay.giftassistant.util.ToastUtil;
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
		if (!AccountManager.getInstance().isLogin()) {
			getActivity().onBackPressed();
			ToastUtil.showShort("未登录");
			return;
		}
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
		etNick.setText(AccountManager.getInstance().getUserInfo().nick);
	}

	@Override
	protected void lazyLoad() {

	}

	@Override
	public boolean onBack() {
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (NetworkUtil.isConnected(getContext())) {
					ToastUtil.showShort("网络错误，修改昵称失败!");
					return;
				}
				ReqModifyNick modifyNick = new ReqModifyNick();
				modifyNick.newNick = etNick.getText().toString().trim();
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
								ToastUtil.showShort("网络异常，修改昵称失败!");
							}

							@Override
							public void onFailure(Throwable t) {
								if (NetworkUtil.isConnected(getContext())) {
									ToastUtil.showShort("网络错误，设置昵称失败!");
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
