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
import com.oplay.giftcool.config.AppDebugConfig;
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
import com.socks.library.KLog;

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
            ToastUtil.showShort(mApp.getResources().getString(R.string.st_hint_un_login));
            IntentUtil.jumpLogin(getContext());
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
            ToastUtil.showShort("请填写完整反馈内容和联系方式");
            return;
        }
        if (etContent.getText().toString().trim().length() < 10) {
            ToastUtil.showShort("反馈信息有点少，麻烦更详细地描述你的反馈(不少于10个字)");
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
                    ToastUtil.showShort(ConstString.TEXT_NET_ERROR);
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
                                ToastUtil.showShort("反馈成功，谢谢你");
                                ScoreManager.getInstance().setTaskFinished(true);
                                if (getActivity() != null) {
                                    getActivity().onBackPressed();
                                }
                                return;
                            }
                            ToastUtil.showShort("提交失败-" + (response.body() == null ?
                                    "解析出错" : response.body().getMsg()));
                            return;
                        }
                        ToastUtil.showShort("提交失败-" + (response == null ? "网络错误" : response.message()));
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<Void>> call, Throwable t) {
                        mIsLoading = false;
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        if (AppDebugConfig.IS_DEBUG) {
                            KLog.e(t);
                        }
                        ToastUtil.showShort("提交失败-网络异常");
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
