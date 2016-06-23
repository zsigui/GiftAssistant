package com.oplay.giftcool.ui.fragment.setting;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.listener.OnBackPressListener;
import com.oplay.giftcool.listener.OnShareListener;
import com.oplay.giftcool.listener.ToolbarListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.DialogManager;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.model.data.req.ReqModifyNick;
import com.oplay.giftcool.model.data.resp.ModifyNick;
import com.oplay.giftcool.model.data.resp.UserModel;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.InputMethodUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ToastUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-1-17.
 */
public class SetNickFragment extends BaseFragment implements OnBackPressListener, TextWatcher {

    private final static String PAGE_NAME = "设置昵称";
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
//            ToastUtil.showShort(mApp.getResources().getString(R.string.st_hint_un_login));
            ToastUtil.showShort(ConstString.TOAST_SESSION_UNAVAILABLE);
            IntentUtil.jumpLoginNoToast(getContext());
            if (getActivity() != null) {
                getActivity().finish();
            }
            return;
        }
        etNick.setText(AccountManager.getInstance().getUserInfo().nick);
        if (getContext() != null && getContext() instanceof ToolbarListener) {
            ((ToolbarListener) getContext()).showRightBtn(View.VISIBLE,
                    mApp.getResources().getString(R.string.st_user_set_nick_save));
            ((ToolbarListener) getContext()).setRightBtnListener(new OnShareListener() {
                @Override
                public void share() {
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
        if (getContext() != null && getContext() instanceof ToolbarListener) {
            ((ToolbarListener) getContext()).showRightBtn(View.GONE, "");
        }
        return false;
    }

    public void showLoading() {
        if (getContext() != null && getChildFragmentManager() != null) {
            DialogManager.getInstance().showLoadingDialog(getChildFragmentManager(),
                    getContext().getString(R.string.st_user_set_nick_loading));
        }
    }

    public void hideLoading() {
        if (getContext() != null && getChildFragmentManager() != null) {
            DialogManager.getInstance().hideLoadingDialog();
        }
    }

    /**
     * 修改用户昵称的网络请求声明
     */
    private Call<JsonRespBase<ModifyNick>> mCall;

    /**
     * 处理上传保存用户昵称事件
     */
    private void handleSave() {
        if (mIsLoading) {
            return;
        }
        mIsLoading = true;
        showLoading();
        final String nick = etNick.getText().toString().trim();
        if (!NetworkUtil.isConnected(getContext())) {
            refreshFailEnd();
            return;
        }
        if (mCall != null) {
            mCall.cancel();
        }
        ReqModifyNick modifyNick = new ReqModifyNick();
        modifyNick.newNick = nick;
        modifyNick.oldNick = AccountManager.getInstance().getUserInfo().nick;
        mCall = Global.getNetEngine().modifyUserNick(new JsonReqBase<ReqModifyNick>(modifyNick));
        mCall.enqueue(new Callback<JsonRespBase<ModifyNick>>() {
            @Override
            public void onResponse(Call<JsonRespBase<ModifyNick>> call, Response<JsonRespBase<ModifyNick>> response) {
                if (!mCanShowUI || call.isCanceled()) {
                    return;
                }
                hideLoading();
                mIsLoading = false;
                if (response != null && response.isSuccessful()) {
                    if (response.body() != null && response.body().getCode() == NetStatusCode.SUCCESS) {
                        UserModel model = AccountManager.getInstance().getUser();
                        model.userInfo.nick = response.body().getData().nick;
                        AccountManager.getInstance().notifyUserAll(model);
                        ToastUtil.showShort(ConstString.TOAST_MODIFY_SUCCESS);
                        ScoreManager.getInstance().setTaskFinished(true);
                        if (getActivity() != null) {
                            getActivity().onBackPressed();
                        }
                        return;
                    }
                    ToastUtil.blurErrorResp(response);
                    AccountManager.getInstance().judgeIsSessionFailed(response.body());
                    return;
                }
                ToastUtil.blurErrorResp(response);
            }

            @Override
            public void onFailure(Call<JsonRespBase<ModifyNick>> call, Throwable t) {
                if (!mCanShowUI || call.isCanceled()) {
                    return;
                }
                hideLoading();
                mIsLoading = false;
                ToastUtil.blurThrow(t);
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
    public String getPageName() {
        return PAGE_NAME;
    }

    @Override
    public void release() {
        super.release();
        if (mCall != null) {
            mCall.cancel();
            mCall = null;
        }
    }
}
