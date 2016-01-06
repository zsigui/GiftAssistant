package com.oplay.giftassistant.ui.fragment.setting;

import android.os.Bundle;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_FullScreen;

/**
 * Created by zsigui on 16-1-6.
 */
public class MyGiftFragment extends BaseFragment_FullScreen {
	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_vp_container);
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

	public static MyGiftFragment newInstance() {
		return new MyGiftFragment();
	}
}
