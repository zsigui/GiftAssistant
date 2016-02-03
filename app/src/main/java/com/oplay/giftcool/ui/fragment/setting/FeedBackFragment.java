package com.oplay.giftcool.ui.fragment.setting;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.model.data.req.ReqFeedBack;
import com.oplay.giftcool.model.data.resp.TaskReward;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
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
 * Created by zsigui on 16-1-6.
 */
public class FeedBackFragment extends BaseFragment implements TextWatcher, TextView.OnEditorActionListener {

	private final static String PAGE_NAME = "意见反馈";
	private RadioButton rbFunction;
	private RadioButton rbPay;
	private RadioButton rbOther;
	private EditText etContent;
	private TextView tvContentCount;
	private EditText etPhone;
	private TextView btnSend;

	public static FeedBackFragment newInstance() {
		return new FeedBackFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		if (!AccountManager.getInstance().isLogin()) {
			ToastUtil.showShort(mApp.getResources().getString(R.string.st_hint_un_login));
			IntentUtil.jumpLogin(getContext());
			getActivity().finish();
			return;
		}
		setContentView(R.layout.fragment_feedback);
		rbFunction = getViewById(R.id.rb_function);
		rbPay = getViewById(R.id.rb_pay);
		rbOther = getViewById(R.id.rb_other);
		etContent = getViewById(R.id.et_content);
		tvContentCount = getViewById(R.id.tv_content_count);
		etPhone = getViewById(R.id.et_phone);
		btnSend = getViewById(R.id.btn_send);
	}

	@Override
	protected void setListener() {
		btnSend.setOnClickListener(this);
		etContent.addTextChangedListener(this);
		etContent.setOnEditorActionListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		etContent.requestFocus();
		InputMethodUtil.showSoftInput(getActivity());
	}

	@Override
	protected void lazyLoad() {

	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.btn_send:
				handleCommit();
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
		if (s.toString().length() > 500) {
			s.subSequence(0, 500);
		}
		tvContentCount.setText(String.format("%s/500", s.toString().length()));
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		switch (actionId) {
			case EditorInfo.IME_ACTION_NEXT:
				etPhone.requestFocus();
				etPhone.setSelection(etPhone.getText().toString().trim().length());
				break;
			case EditorInfo.IME_ACTION_DONE:
				handleCommit();
				break;
		}
		return false;
	}

	private void handleCommit() {
		if (TextUtils.isEmpty(etContent.getText().toString().trim())
				|| TextUtils.isEmpty(etPhone.getText().toString().trim())) {
			ToastUtil.showShort("请填写完整反馈内容和联系方式");
			return;
		}
		if (etContent.getText().toString().trim().length() < 10) {
			ToastUtil.showShort("反馈信息有点少，麻烦更详细地描述你的反馈");
			return;
		}

		if (mIsLoading) {
			return;
		}
		mIsLoading = true;

		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				ReqFeedBack feedBack = new ReqFeedBack();
				feedBack.contact = etPhone.getText().toString();
				feedBack.content = etContent.getText().toString();
				if (rbFunction.isChecked()) {
					feedBack.type = 1;
				} else if (rbPay.isChecked()) {
					feedBack.type = 2;
				} else if (rbOther.isChecked()) {
					feedBack.type = 3;
				}
				feedBack.version = AppConfig.SDK_VER;
				Global.getNetEngine().postFeedBack(new JsonReqBase<ReqFeedBack>(feedBack))
						.enqueue(new Callback<JsonRespBase<TaskReward>>() {
							@Override
							public void onResponse(Response<JsonRespBase<TaskReward>> response, Retrofit retrofit) {
								mIsLoading = false;
								if (response != null && response.isSuccess()) {
									if (response.body() != null && response.body().isSuccess()) {
										ToastUtil.showShort("反馈成功，谢谢你");
										ScoreManager.getInstance().toastByCallback(response.body().getData());
										((BaseAppCompatActivity)getActivity()).handleBackPressed();
										return;
									}
									ToastUtil.showShort("提交失败-" + (response.body() == null ?
											"解析出错" : response.body().getMsg()));
									return;
								}
								ToastUtil.showShort("提交失败-" + (response == null ? "网络错误" : response.message()));
							}

							@Override
							public void onFailure(Throwable t) {
								if (AppDebugConfig.IS_DEBUG) {
									KLog.e(t);
								}
								ToastUtil.showShort("提交失败-网络异常");
								mIsLoading = false;
							}
						});
			}
		});
	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}
}
