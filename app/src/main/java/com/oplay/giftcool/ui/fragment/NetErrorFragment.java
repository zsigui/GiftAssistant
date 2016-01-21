package com.oplay.giftcool.ui.fragment;

import android.os.Bundle;
import android.view.View;

import com.oplay.giftcool.R;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.IntentUtil;

/**
 * Created by zsigui on 15-12-22.
 */
public class NetErrorFragment extends BaseFragment implements View.OnClickListener {

    private View mRetry;
    private View mSetting;
    private OnRetryListener mOnRetryListener;

	public static NetErrorFragment newInstance() {
		return new NetErrorFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_error_net);
        mRetry = getViewById(R.id.v_err);
        mSetting = getViewById(R.id.v_wifi);
	}

    public void setOnRetryListener(OnRetryListener onRetryListener) {
        mOnRetryListener = onRetryListener;
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
                if (mOnRetryListener != null) {
                    mOnRetryListener.onRetry();
                }
                break;
            case R.id.v_wifi:
                IntentUtil.jumpWifiSetting(getContext());
                break;
        }
    }

	@Override
	public String getPageName() {
		return null;
	}

	public interface OnRetryListener {
        void onRetry();
    }
}
