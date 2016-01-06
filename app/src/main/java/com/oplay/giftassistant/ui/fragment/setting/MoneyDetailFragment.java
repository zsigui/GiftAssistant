package com.oplay.giftassistant.ui.fragment.setting;

import android.os.Bundle;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.KeyConfig;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_FullScreen;

/**
 *
 * 偶玩豆和积分明细
 *
 * Created by zsigui on 16-1-6.
 */
public class MoneyDetailFragment extends BaseFragment_FullScreen {
	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_webview_with_toolbar);
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

		if (type == KeyConfig.TYPE_ID_DETAIL_SCORE) {
			setTitleBar(R.string.st_score_detail);
		} else if (type == KeyConfig.TYPE_ID_DETAIL_BEAN) {
			setTitleBar(R.string.st_bean_detail);
		}
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
