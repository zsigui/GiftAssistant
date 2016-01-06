package com.oplay.giftassistant.ui.fragment.base;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;

/**
 * Created by zsigui on 16-1-6.
 */
public abstract class BaseFragment_FullScreen extends BaseFragment {

	private TextView tvTitle;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		tvTitle = getViewById(R.id.tv_bar_title);
		ImageView iv = getViewById(R.id.iv_bar_back);
		if (iv != null) {
			iv.setOnClickListener(this);
		}
		super.onViewCreated(view, savedInstanceState);
	}

	public void setTitleBar(String title) {
		if (tvTitle != null) {
			tvTitle.setText(title);
		}
	}

	public void setTitleBar(@StringRes int title) {
		setTitleBar(getResources().getString(title));
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.iv_bar_back) {
			mCanShowUI = false;
			if (getActivity() instanceof BaseAppCompatActivity) {
				((BaseAppCompatActivity) getActivity()).popOrExit();
			}
		}
	}
}
