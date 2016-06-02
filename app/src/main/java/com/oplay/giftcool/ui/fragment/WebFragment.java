package com.oplay.giftcool.ui.fragment;

import android.os.Bundle;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_WebView;
import com.oplay.giftcool.util.ToastUtil;

/**
 * 活动WEB页面
 *
 * Created by zsigui on 16-1-29.
 */
public class WebFragment extends BaseFragment_WebView {
	private static final String PAGE_NAME = "活动页面";

	/**
	 * 标识是否处于签到界面
	 */
	private boolean isInSignInView = false;

	public static WebFragment newInstance(String url) {
		WebFragment fragment = new WebFragment();
		Bundle bundle = new Bundle();
		bundle.putString(KeyConfig.KEY_URL, url);
		fragment.setArguments(bundle);
		return fragment;
	}

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
			mViewManager.showEmpty();
			ToastUtil.showShort("获取数据为空");
			if (getActivity() != null) {
				getActivity().finish();
			}
			return;
		}
		String url = getArguments().getString(KeyConfig.KEY_URL);
		if (url != null && url.toLowerCase().contains("checkin")) {
			isInSignInView = true;
		}
		loadUrl(url);
		mIsSwipeRefresh = true;
	}

	@Override
	protected void lazyLoad() {
		if (!mIsSwipeRefresh) {
			reloadPage();
		}
		mIsSwipeRefresh = false;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (isInSignInView) {
			ScoreManager.getInstance().initTaskState(getContext());
			ScoreManager.getInstance().setTaskFinished(true);
		}
	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}
}
