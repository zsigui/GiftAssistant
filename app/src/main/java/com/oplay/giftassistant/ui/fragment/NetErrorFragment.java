package com.oplay.giftassistant.ui.fragment;

import android.os.Bundle;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;

/**
 * Created by zsigui on 15-12-22.
 */
public class NetErrorFragment extends BaseFragment {

	public static NetErrorFragment newInstance() {
		return new NetErrorFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_error_net);
	}

	@Override
	protected void setListener() {

	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {

	}

	@Override
	protected void lazyLoad() {

	}
}
