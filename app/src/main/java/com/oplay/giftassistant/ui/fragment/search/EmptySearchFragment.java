package com.oplay.giftassistant.ui.fragment.search;

import android.os.Bundle;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;

/**
 * Created by zsigui on 15-12-22.
 */
public class EmptySearchFragment extends BaseFragment {

	public static EmptySearchFragment newInstance() {
		return new EmptySearchFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_empty_search);
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
