package com.oplay.giftcool.ui.fragment.login;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.SelectAccountAdapter;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.listener.CommonAccountViewListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.DialogManager;
import com.oplay.giftcool.manager.OuwanSDKManager;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;

import net.ouwan.umipay.android.entry.UmipayCommonAccount;
import net.ouwan.umipay.android.manager.UmipayCommonAccountCacheManager;

import java.util.ArrayList;

/**
 * Created by zsigui on 16-9-6.
 */
public class SelectAccountFragment extends BaseFragment implements CommonAccountViewListener.ResultActionCallback {

    private TextView tvHint;
    private TextView btnSend;
    private ListView lvData;
    private SelectAccountAdapter mAdapter;


    public static SelectAccountFragment newInstance() {
        return new SelectAccountFragment();
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
        if (AccountManager.getInstance().isLogin()) {
            getActivity().finish();
            return;
        }
        ArrayList<UmipayCommonAccount> data = OuwanSDKManager.getInstance()
                .getAccountExceptPackage(UmipayCommonAccountCacheManager.COMMON_ACCOUNT,
                        getContext().getPackageName());
        if (data == null || data.isEmpty()) {
            getActivity().finish();
            return;
        }
        tvHint.setText(getString(R.string.st_lbind_choose_top_login_hint));
        mAdapter = new SelectAccountAdapter(getContext(), data);
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
                DialogManager.getInstance().showLoadingDialog(getChildFragmentManager());
                OuwanSDKManager.getInstance().onChooseAccount(
                        CommonAccountViewListener.CODE_SELECT_ACCOUNT,
                        mAdapter.getCheckedItem(),
                        this
                );
                break;
        }
    }

    @Override
    public String getPageName() {
        return "选择登录账号";
    }

    @Override
    public void onSuccess(Object obj) {
        DialogManager.getInstance().hideLoadingDialog();
        getActivity().finish();
    }

    @Override
    public void onFailed(int code, String msg) {
        DialogManager.getInstance().hideLoadingDialog();
    }

    @Override
    public void onCancel() {
        DialogManager.getInstance().hideLoadingDialog();
    }


}
