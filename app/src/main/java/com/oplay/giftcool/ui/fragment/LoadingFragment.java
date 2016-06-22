package com.oplay.giftcool.ui.fragment;

import android.os.Bundle;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;

/**
 * 加载页面
 * <p/>
 * Created by zsigui on 15-12-23.
 */
public class LoadingFragment extends BaseFragment {


    /**
     * provide a global instance for the reason that loading fragment will be used frequently
     */
    public static LoadingFragment newInstance() {
        return new LoadingFragment();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_data_loading);
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
    }

    @Override
    protected void lazyLoad() {
        AppDebugConfig.v(AppDebugConfig.TAG_FRAG, "lazyLoad is called, but nothing need to be do here");
    }


    @Override
    public String getPageName() {
        return null;
    }
}
