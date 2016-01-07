package com.oplay.giftassistant.ui.fragment.setting;

import android.os.Bundle;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.KeyConfig;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_WithName;

/**
 *
 * 偶玩豆和积分明细
 *
 * Created by zsigui on 16-1-6.
 */
public class MoneyDetailFragment extends BaseFragment_WithName {
	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_webview);
	}

	@Override
	protected void setListener() {

	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		if (getArguments() == null) {
			return;
		}
		int type = getArguments().getInt(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_DEFAULT);
		if (type == KeyConfig.TYPE_ID_DEFAULT) {
			showToast("错误传递类型");
			return;
		}
		// do something
	}

	@Override
	protected void lazyLoad() {

	}

	public static MoneyDetailFragment newInstance(int type) {
		MoneyDetailFragment fragment = new MoneyDetailFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(KeyConfig.KEY_TYPE, type);
		fragment.setArguments(bundle);
		return fragment;
	}
}
