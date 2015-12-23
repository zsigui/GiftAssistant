package com.oplay.giftassistant.ui.fragment;

import android.os.Bundle;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;
import com.oplay.giftassistant.ui.widget.circleprogress.CircleProgress;
import com.socks.library.KLog;

/**
 *
 *
 * Created by zsigui on 15-12-23.
 */
public class LoadingFragment extends BaseFragment {

	private CircleProgress mProgress;

	/**
	 * provide a global instance for the reason that loading fragment will be used frequently
	 *
	 */
	public static LoadingFragment newInstance() {
		return new LoadingFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_data_loading);
		mProgress = getViewById(R.id.fl_search_container);
	}

	@Override
	protected void setListener() {

	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		startAnim();
	}

	@Override
	protected void lazyLoad() {
		KLog.v("lazyLoad is called, but nothing need to be do here");
	}

	@Override
	protected void onUserVisible() {
		super.onUserVisible();
		startAnim();
	}

	@Override
	protected void onUserInVisible() {
		super.onUserInVisible();
		stopAnim();
	}

	private void startAnim() {
		if (mProgress != null) {
			mProgress.startAnim();
		}
	}

	private void stopAnim() {
		if (mProgress != null) {
			mProgress.reset();
		}
	}
}
