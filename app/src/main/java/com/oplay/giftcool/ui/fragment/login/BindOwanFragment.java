package com.oplay.giftcool.ui.fragment.login;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.model.data.resp.UserModel;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.ToastUtil;

/**
 * Created by zsigui on 16-8-26.
 */
public class BindOwanFragment extends BaseFragment{

    private UserModel mData;

    private EditText etUser;
    private EditText etPwd;
    private TextView tvHint;
    private TextView btnSend;

    public static BindOwanFragment newInstance(UserModel um) {
        BindOwanFragment fragment = new BindOwanFragment();
        Bundle b = new Bundle();
        b.putSerializable(KeyConfig.KEY_DATA, um);
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
        if (mData == null) {
            ToastUtil.showShort(ConstString.TOAST_NO_RESPONSE_DATA);
            getActivity().onBackPressed();
            return;
        }
        if (mData.userInfo.phoneCanUseAsUname) {
            setContentView(R.layout.fragment_lbind_owan_pwd);
            tvHint = getViewById(R.id.tv_top_hint);
        } else {
            setContentView(R.layout.fragment_lbind_owan_user);
            etUser = getViewById(R.id.et_user);
        }
        etPwd = getViewById(R.id.et_pwd);
        btnSend = getViewById(R.id.btn_send);
    }

    @Override
    protected void setListener() {
        btnSend.setOnClickListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        if (mData.userInfo.phoneCanUseAsUname) {
            tvHint.setText(Html.fromHtml(String.format(
                    getString(R.string.st_lbind_owan_pwd_top_hint), mData.userInfo.phone)));
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
                break;
        }
    }

    @Override
    public String getPageName() {
        return "设置登录账号密码";
    }
}
