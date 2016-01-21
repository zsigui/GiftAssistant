package com.oplay.giftcool.ui.fragment.setting;

import android.os.Bundle;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.WebViewUrl;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_WebView;

/**
 *
 * 偶玩豆和积分明细
 *
 * Created by zsigui on 16-1-6.
 */
public class MoneyDetailFragment extends BaseFragment_WebView {

	private final static String PAGE_NAME = "钱包明细";

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_webview);
		mProgressBar = getViewById(R.id.pb_percent);
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
		AccountManager.getInstance().syncCookie();
		if (type == KeyConfig.TYPE_ID_DETAIL_BEAN) {
			loadUrl(WebViewUrl.OUWAN_BEAN_DETAIL);
		} else {
			loadUrl(WebViewUrl.SCORE_DETAIL);
		}
		mIsLoading = true;
	}

	@Override
	protected void lazyLoad() {
		mIsLoading = true;
		reloadPage();
	}

	public static MoneyDetailFragment newInstance(int type) {
		MoneyDetailFragment fragment = new MoneyDetailFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(KeyConfig.KEY_TYPE, type);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}
}
