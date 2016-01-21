package com.oplay.giftcool.ui.fragment.search;

import android.os.Bundle;

import com.oplay.giftcool.R;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;

/**
 * Created by zsigui on 15-12-22.
 */
public class EmptySearchFragment extends BaseFragment {

	private final static String PAGE_NAME = "空搜索页";
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

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}
}
