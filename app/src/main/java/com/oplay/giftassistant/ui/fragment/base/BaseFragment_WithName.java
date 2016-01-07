package com.oplay.giftassistant.ui.fragment.base;

/**
 * Created by zsigui on 16-1-7.
 */
public abstract class BaseFragment_WithName extends BaseFragment {

	private String mTitleName;

	public String getTitleName() {
		return mTitleName;
	}

	public void setTitleName(String titleName) {
		mTitleName = titleName;
	}
}
