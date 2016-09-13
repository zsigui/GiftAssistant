package com.oplay.giftcool.ui.fragment.login;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.OwanChooseAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.util.UserTypeUtil;
import com.oplay.giftcool.listener.OnBackPressListener;
import com.oplay.giftcool.manager.DialogManager;
import com.oplay.giftcool.manager.StatisticsManager;
import com.oplay.giftcool.model.data.req.ReqBindMainAccount;
import com.oplay.giftcool.model.data.resp.BindAccount;
import com.oplay.giftcool.model.data.resp.UserModel;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.MixUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ToastUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-8-27.
 */
public class ChooseOwanFragment extends BaseFragment implements OnBackPressListener {


    private TextView tvHint;
    private TextView btnSend;
    private ListView lvData;
    private UserModel mModel;
    private OwanChooseAdapter mAdapter;

    public static ChooseOwanFragment newInstance(UserModel um) {
        ChooseOwanFragment fragment = new ChooseOwanFragment();
        Bundle b = new Bundle();
        b.putSerializable(KeyConfig.KEY_DATA, um);
        fragment.setArguments(b);
        return fragment;
    }


    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_lbind_choose_owan);
        tvHint = getViewById(R.id.tv_top_hint);
        lvData = getViewById(R.id.lv_content);
        btnSend = getViewById(R.id.btn_send);
    }

    @Override
    protected void setListener() {
        btnSend.setOnClickListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        if (getArguments() == null) {
            ToastUtil.showShort(ConstString.TOAST_MISS_STATE);
            getActivity().onBackPressed();
            return;
        }
        mModel = (UserModel) getArguments().getSerializable(KeyConfig.KEY_DATA);
        if (mModel == null) {
            ToastUtil.showShort(ConstString.TOAST_MISS_STATE);
            getActivity().onBackPressed();
            return;
        }
        tvHint.setText(Html.fromHtml(String.format(getString(R.string.st_lbind_choose_top_hint),
                mModel.userInfo.phone)));
        mAdapter = new OwanChooseAdapter(getContext(), mModel.bindAccounts);
        lvData.setAdapter(mAdapter);
    }

    @Override
    protected void lazyLoad() {

    }

    private long mLastClickTime;

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_send:
                long curTime = System.currentTimeMillis();
                if (curTime - mLastClickTime <= Global.CLICK_TIME_INTERVAL) {
                    mLastClickTime = curTime;
                    return;
                }
                handleBindLogin();
                break;
        }
    }

    private void handleBindLogin() {
        if (!NetworkUtil.isConnected(getContext())) {
            ToastUtil.showShort(ConstString.TOAST_NET_ERROR);
            return;
        }
        DialogManager.getInstance().showLoadingDialog(getChildFragmentManager());
        final BindAccount account = mAdapter.getCheckedItem();
        if (account == null) {
            ToastUtil.showShort("请选择账号！");
            return;
        }
        if (!NetworkUtil.isConnected(getContext())) {
            ToastUtil.showShort(ConstString.TOAST_NET_ERROR);
            return;
        }
        ReqBindMainAccount data = new ReqBindMainAccount();
        data.cuid = account.uid;
        data.phone = mModel.userInfo.phone;
        data.token = mModel.token;
        JsonReqBase<ReqBindMainAccount> reqData = new JsonReqBase<>(data);
        Global.getNetEngine().bindMobileMainAccount(reqData)
                .enqueue(new Callback<JsonRespBase<UserModel>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<UserModel>> call,
                                           Response<JsonRespBase<UserModel>> response) {
                        if (call.isCanceled() || !mCanShowUI) {
                            return;
                        }
                        DialogManager.getInstance().hideLoadingDialog();
                        if (response != null && response.isSuccessful()) {
                            if (response.body() != null && response.body().isSuccess()) {
                                // 绑定并登录成功
                                UserModel um = response.body().getData();
                                um.userInfo.loginType = UserTypeUtil.TYPE_POHNE;
                                MixUtil.doLoginSuccessNext(getContext(), um);
                                if (AppDebugConfig.IS_STATISTICS_SHOW) {
                                    Map<String, String> keyVal = new HashMap<>();
                                    // 手机号: %s, 对应绑定偶玩账号: %s, 是否首次登录: %b, 绑定情况: %d, 是否空Context: %b
                                    keyVal.put("绑定信息", String.format(Locale.CHINA, "p:%s, u:%s",
                                            um.userInfo.phone, um.userInfo.username));
                                    StatisticsManager.getInstance().trace(getContext(),
                                            StatisticsManager.ID.USER_PHONE_BIND_MAIN,
                                            StatisticsManager.ID.STR_USER_PHONE_BIND_MAIN, "", keyVal, 0);
                                }
                                return;
                            }
                            // 连接上但是失败，处理为失败重来
                            if (getActivity() != null) {
                                getActivity().onBackPressed();
                            }
                        }
                        ToastUtil.blurErrorResp(response);
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<UserModel>> call, Throwable t) {
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
        return "选择默认登录账号";
    }

    @Override
    public boolean onBack() {
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
    }
}
