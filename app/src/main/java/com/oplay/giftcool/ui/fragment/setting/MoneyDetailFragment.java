package com.oplay.giftcool.ui.fragment.setting;

import android.os.Bundle;
import android.view.View;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.WebViewUrl;
import com.oplay.giftcool.listener.OnBackPressListener;
import com.oplay.giftcool.listener.OnShareListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.ui.activity.SettingActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_WebView;
import com.oplay.giftcool.ui.fragment.dialog.WebViewDialog;

/**
 *
 * 偶玩豆和积分明细
 *
 * Created by zsigui on 16-1-6.
 */
public class MoneyDetailFragment extends BaseFragment_WebView implements OnBackPressListener{

	private final static String PAGE_NAME = "钱包明细";
    // 防止重复点击
    private long mLastClickTiem = 0;

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
		final int type = getArguments().getInt(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_DEFAULT);
		if (type == KeyConfig.TYPE_ID_DEFAULT) {
			showToast("错误传递类型");
			return;
		}
		// do something
		AccountManager.getInstance().syncCookie();
		if (getActivity() instanceof SettingActivity) {
			SettingActivity activity = ((SettingActivity) getActivity());
			activity.showRightBtn(View.VISIBLE, mApp.getResources().getString(R.string.st_wallet_money_note));
			activity.setRightBtnEnabled(true);
			activity.setRightBtnListener(new OnShareListener() {
				@Override
				public void share() {
                    long time = System.currentTimeMillis();
                    if (time - mLastClickTiem < 500) {
                        mLastClickTiem = time;
                        return;
                    }
					WebViewDialog dialog;
					if (type == KeyConfig.TYPE_ID_DETAIL_BEAN) {
						dialog = WebViewDialog.newInstance(
                                mApp.getResources().getString(R.string.st_wallet_bean_note), WebViewUrl.getWebUrl(WebViewUrl.OUWAN_BEAN_DETAIL_NOTE));
					} else {
						dialog = WebViewDialog.newInstance(
								mApp.getResources().getString(R.string.st_wallet_score_note), WebViewUrl.getWebUrl(WebViewUrl.SCORE_DETAIL_NOTE));
					}
					dialog.show(getChildFragmentManager(), WebViewDialog.class.getSimpleName());
				}
			});
		}
		if (type == KeyConfig.TYPE_ID_DETAIL_BEAN) {
			loadUrl(WebViewUrl.getWebUrl(WebViewUrl.OUWAN_BEAN_DETAIL));
		} else {
			loadUrl(WebViewUrl.getWebUrl(WebViewUrl.SCORE_DETAIL));
		}
		mIsSwipeRefresh = true;
	}

	@Override
	protected void lazyLoad() {
	}

	@Override
	public boolean onBack() {
		// 隐藏输入框
		if (getActivity() != null) {
			((SettingActivity) getActivity()).showRightBtn(View.GONE, "");
		}
		return super.onBack();
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
