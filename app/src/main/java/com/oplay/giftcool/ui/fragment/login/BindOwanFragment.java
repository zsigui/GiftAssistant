package com.oplay.giftcool.ui.fragment.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.util.UserTypeUtil;
import com.oplay.giftcool.listener.OnBackPressListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.DialogManager;
import com.oplay.giftcool.model.data.req.ReqLogin;
import com.oplay.giftcool.model.data.resp.UserModel;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.activity.LoginActivity;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.oplay.giftcool.util.ViewUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-8-26.
 */
public class BindOwanFragment extends BaseFragment implements OnBackPressListener, TextWatcher {

    private UserModel mData;

    private EditText etUser;
    private EditText etPwd;
    //    private TextView tvHint;
    private TextView btnSend;
    private LinearLayout llUser;
    private TextView tvAward;

    private JsonReqBase<ReqLogin> reqData;
    private boolean mNeedBackPhoneLogin = false;

    public static BindOwanFragment newInstance(UserModel um, boolean mNeedBack) {
        BindOwanFragment fragment = new BindOwanFragment();
        Bundle b = new Bundle();
        b.putSerializable(KeyConfig.KEY_DATA, um);
        b.putBoolean(KeyConfig.KEY_DATA_O, mNeedBack);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        if (getArguments() == null) {
            ToastUtil.showShort(ConstString.TOAST_NO_RESPONSE_DATA);
            getActivity().onBackPressed();
            return;
        }
        mData = (UserModel) getArguments().getSerializable(KeyConfig.KEY_DATA);
        mNeedBackPhoneLogin = getArguments().getBoolean(KeyConfig.KEY_DATA_O);
        if (mData == null) {
            ToastUtil.showShort(ConstString.TOAST_NO_RESPONSE_DATA);
            getActivity().onBackPressed();
            return;
        }

        setContentView(R.layout.fragment_lbind_owan_user);
        etUser = getViewById(R.id.et_user);
        etPwd = getViewById(R.id.et_pwd);
        llUser = getViewById(R.id.ll_user);
        btnSend = getViewById(R.id.btn_send);
        tvAward = getViewById(R.id.tv_award);
    }

    @Override
    protected void setListener() {
        btnSend.setOnClickListener(this);
        if (etUser != null) {
            etUser.addTextChangedListener(this);
        }
        etPwd.addTextChangedListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        if (mData.userInfo.phoneCanUseAsUname) {
            etUser.setTextColor(ViewUtil.getColor(getContext(), R.color.co_btn_green));
            etUser.setEnabled(false);
            etUser.setText(mData.userInfo.phone);
            llUser.setBackgroundColor(ViewUtil.getColor(getContext(), R.color.co_transparent));
        }
        reqData = new JsonReqBase<>(new ReqLogin());
        btnSend.setEnabled(false);
        tvAward.setVisibility(mData.userInfo.isFirstLogin ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void lazyLoad() {

    }

    private long mLastClickTime = 0;

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_send:
                long curTime = System.currentTimeMillis();
                if (curTime - mLastClickTime < Global.CLICK_TIME_INTERVAL) {
                    mLastClickTime = curTime;
                    return;
                }
                handleBindOwan();
                break;
        }
    }

    private void handleBindOwan() {
        final String user = etUser.getText().toString().trim();
        String pwd = etPwd.getText().toString().trim();
        boolean canPass = reqData.data.setOuwanUser(user, pwd, false);
        if (!canPass) {
            ToastUtil.showShort(ConstString.TOAST_OUWAN_ERROR);
            return;
        }
        mData.userInfo.username = user;
        DialogManager.getInstance().showLoadingDialog(getChildFragmentManager());
        if (!NetworkUtil.isConnected(getContext())) {
            ToastUtil.showShort(ConstString.TOAST_NET_ERROR);
            return;
        }
        Global.getNetEngine().bindOwanAccount(reqData)
                .enqueue(new Callback<JsonRespBase<Void>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<Void>> call, Response<JsonRespBase<Void>> response) {
                        if (call.isCanceled() || !mCanShowUI) {
                            return;
                        }
                        DialogManager.getInstance().hideLoadingDialog();
                        if (response != null && response.isSuccessful()
                                && response.body() != null && response.body().isSuccess()) {
                            ToastUtil.showShort("成功设置偶玩账号了耶!");
                            mData.userInfo.bindOuwanStatus = 1;
                            mData.userInfo.loginType = UserTypeUtil.TYPE_OUWAN;
                            AccountManager.getInstance().notifyUserAll(mData);
                            reqData.data.setOuwanUser(reqData.data.getUsername(), reqData.data.getPassword(), true);
                            AccountManager.getInstance().writeOuwanAccount(reqData.data.getUsername() + ","
                                            + reqData.data.getPassword(), AccountManager.getInstance()
                                    .readOuwanAccount(),
                                    false);
                            // 请求更新数据
                            ((LoginActivity) getActivity()).doLoginBack();
                            return;
                        }
                        ToastUtil.blurErrorResp(response);
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<Void>> call, Throwable t) {
                        if (call.isCanceled() || !mCanShowUI) {
                            return;
                        }
                        DialogManager.getInstance().hideLoadingDialog();
                        ToastUtil.blurThrow(t);
                    }
                });
    }

    @Override
    public String getPageName() {
        return "设置登录账号密码";
    }

    @Override
    public boolean onBack() {
        if (AssistantApp.getInstance().getSetupOuwanAccount() == KeyConfig.KEY_LOGIN_SET_BIND_CAN_JUMP) {
            // 可以被跳过，则直接返回
            return false;
        }
        AccountManager.getInstance().logout(false);
        if (mNeedBackPhoneLogin) {
            // 回退则通知退出
            if (getActivity() != null) {
                if (AssistantApp.getInstance().getPhoneLoginType() == 1) {
                    ((BaseAppCompatActivity) getActivity()).replaceFragWithTitle(R.id.fl_container,
                            PhoneLoginNewFragment.newInstance(),
                            getResources().getString(R.string.st_login_phone_new_title),
                            false);
                } else {
                    ((BaseAppCompatActivity) getActivity()).replaceFragWithTitle(R.id.fl_container,
                            PhoneLoginFragment.newInstance(),
                            getResources().getString(R.string.st_login_phone_title),
                            false);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!etUser.getText().toString().trim().isEmpty()
                && !etPwd.getText().toString().trim().isEmpty()) {
            btnSend.setEnabled(true);
        } else {
            btnSend.setEnabled(false);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
