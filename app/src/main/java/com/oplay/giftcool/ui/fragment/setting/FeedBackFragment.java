package com.oplay.giftcool.ui.fragment.setting;

import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.listener.OnBackPressListener;
import com.oplay.giftcool.listener.OnShareListener;
import com.oplay.giftcool.listener.ToolbarListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.model.data.req.ReqFeedBack;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.InputMethodUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.MixUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ToastUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-1-6.
 */
public class FeedBackFragment extends BaseFragment implements TextWatcher, TextView.OnEditorActionListener,
        OnBackPressListener {

    private final static String PAGE_NAME = "意见反馈";
    //	private RadioButton rbFunction;
//	private RadioButton rbPay;
//	private RadioButton rbOther;
    private EditText etContent;
    private TextView tvContentCount;
    private EditText etPhone;
//    private TextView btnSend;
    private TextView tvTypeTitle;

    public static FeedBackFragment newInstance() {
        return new FeedBackFragment();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        if (!AccountManager.getInstance().isLogin()) {
//            ToastUtil.showShort(mApp.getResources().getString(R.string.st_hint_un_login));
            ToastUtil.showShort(ConstString.TOAST_SESSION_UNAVAILABLE);
            IntentUtil.jumpLoginNoToast(getContext());
            if (getActivity() != null) {
                getActivity().finish();
            }
            return;
        }
        setContentView(R.layout.fragment_feedback);
//		rbFunction = getViewById(R.id.rb_function);
//		rbPay = getViewById(R.id.rb_pay);
//		rbOther = getViewById(R.id.rb_other);
        etContent = getViewById(R.id.et_content);
        tvContentCount = getViewById(R.id.tv_content_count);
        etPhone = getViewById(R.id.et_phone);
//        btnSend = getViewById(R.id.btn_send);
        tvTypeTitle = getViewById(R.id.tv_type_title);
    }

    @Override
    protected void setListener() {
//        btnSend.setOnClickListener(this);
        etContent.addTextChangedListener(this);
        etContent.setOnEditorActionListener(this);
        tvTypeTitle.setOnClickListener(this);
        etPhone.addTextChangedListener(this);
    }

    private long mLastClickTime = 0;

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        etContent.requestFocus();
        tvTypeTitle.setText(Html.fromHtml(String.format("%s  <font color='#f85454'>%s</font>",
                getResources().getString(R.string.st_feedback_type_title), MixUtil.getQQInfo()[0])));
        InputMethodUtil.showSoftInput(getActivity());
        if (getContext() != null && getContext() instanceof ToolbarListener) {
            ((ToolbarListener) getContext()).showRightBtn(View.VISIBLE,
                    mApp.getResources().getString(R.string.st_feedback_btn));
            ((ToolbarListener) getContext()).setRightBtnEnabled(false);
            ((ToolbarListener) getContext()).setRightBtnListener(new OnShareListener() {
                @Override
                public void share() {
                    final long curTime = System.currentTimeMillis();
                    if (curTime - mLastClickTime < Global.CLICK_TIME_INTERVAL) {
                        mLastClickTime = curTime;
                        return;
                    }
                    handleCommit();
                }
            });
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
                handleCommit();
                break;
            case R.id.tv_type_title:
                IntentUtil.joinQQGroup(getContext(), MixUtil.getQQInfo()[1]);
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
        final int length = s.toString().trim().length();
        if (length > 500) {
            s.subSequence(0, 500);
        }
        tvContentCount.setText(String.format("%s/500", s.toString().length()));
        if (length < 10 || etPhone.getText().toString().trim().isEmpty()) {
            if (getContext() != null && getContext() instanceof ToolbarListener) {
                ((ToolbarListener) getContext()).setRightBtnEnabled(false);
            }
        } else {
            if (getContext() != null && getContext() instanceof ToolbarListener) {
                ((ToolbarListener) getContext()).setRightBtnEnabled(true);
            }
        }
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

    /**
     * 发送反馈消息的网络请求声明
     */
    private Call<JsonRespBase<Void>> mCall;

    private void handleCommit() {
        if (TextUtils.isEmpty(etContent.getText().toString().trim())
                || TextUtils.isEmpty(etPhone.getText().toString().trim())) {
            ToastUtil.showShort(ConstString.TOAST_FEEDBACK_LACK);
            return;
        }
        if (etContent.getText().toString().trim().length() < 10) {
            ToastUtil.showShort(ConstString.TOAST_FEEDBACK_CONTENT_NOT_ENOUGH);
            return;
        }

        if (mIsLoading) {
            return;
        }
        mIsLoading = true;

        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                if (!NetworkUtil.isConnected(getContext())) {
                    ToastUtil.showShort(ConstString.TOAST_NET_ERROR);
                    mIsLoading = false;
                    return;
                }
                if (mCall != null) {
                    mCall.cancel();
                }
                ReqFeedBack feedBack = new ReqFeedBack();
                feedBack.contact = etPhone.getText().toString();
                feedBack.content = etContent.getText().toString();
                feedBack.type = 1;
                mCall = Global.getNetEngine().postFeedBack(new JsonReqBase<ReqFeedBack>(feedBack));
                mCall.enqueue(new Callback<JsonRespBase<Void>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<Void>> call, Response<JsonRespBase<Void>> response) {
                        mIsLoading = false;
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        if (response != null && response.isSuccessful()) {
                            if (response.body() != null && response.body().isSuccess()) {
                                ToastUtil.showShort(ConstString.TOAST_FEEDBACK_SUCCESS);
                                ScoreManager.getInstance().setTaskFinished(true);
                                if (getActivity() != null) {
                                    getActivity().onBackPressed();
                                }
                                return;
                            }
                            AccountManager.getInstance().judgeIsSessionFailed(response.body());
                            ToastUtil.blurErrorResp(response);
                            return;
                        }
                        ToastUtil.blurErrorResp(response);
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<Void>> call, Throwable t) {
                        mIsLoading = false;
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        ToastUtil.blurThrow(t);
                    }
                });
            }
        });
    }

    @Override
    public String getPageName() {
        return PAGE_NAME;
    }

    @Override
    public boolean onBack() {
        // 隐藏输入框
        InputMethodUtil.hideSoftInput(mActivity);
        if (getContext() != null && getContext() instanceof ToolbarListener) {
            ((ToolbarListener) getContext()).showRightBtn(View.GONE, "");
        }
        return false;
    }
}
