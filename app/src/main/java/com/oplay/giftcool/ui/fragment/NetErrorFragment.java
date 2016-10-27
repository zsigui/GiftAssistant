package com.oplay.giftcool.ui.fragment;

import android.os.Bundle;
import android.view.View;

import com.oplay.giftcool.R;
import com.oplay.giftcool.ui.activity.SearchActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;

/**
 * 加载出错页面
 * <p>
 * Created by zsigui on 15-12-22.
 */
public class NetErrorFragment extends BaseFragment implements View.OnClickListener {

    private View mRetry;
    private View mSetting;

    public static NetErrorFragment newInstance() {
        return new NetErrorFragment();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_error_net);
        mRetry = getViewById(R.id.v_err);
        mSetting = getViewById(R.id.v_wifi);
    }

    @Override
    protected void setListener() {
        mRetry.setOnClickListener(this);
        mSetting.setOnClickListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {

    }

    @Override
    protected void lazyLoad() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.v_err:
            case R.id.v_wifi:
                if (getContext() != null && getContext() instanceof SearchActivity) {
                    ((SearchActivity) getContext()).reSearch();
                }
                break;
        }
    }

    @Override
    public String getPageName() {
        return null;
    }
}
