package com.oplay.giftcool.ui.activity;

import android.content.Intent;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.WebFragment;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

/**
 * Created by zsigui on 16-1-29.
 */
public class WebActivity extends BaseAppCompatActivity {

	/**
	 * 标识是否处于签到界面
	 */
	private boolean isInSignInView = false;

	@Override
	protected void initView() {
		setContentView(R.layout.activity_common_with_back);
	}

	@Override
	protected void processLogic() {
		if (getIntent() == null) {
			ToastUtil.showShort("参数获取失败，请重新进入");
			finish();
			return;
		}
		handleRedirect(getIntent());
	}

	private void handleRedirect(Intent intent) {
		String url = intent.getStringExtra(KeyConfig.KEY_DATA);
		String title = intent.getStringExtra(KeyConfig.KEY_TITLE);
		KLog.d(AppDebugConfig.TAG_WARN, "url = " + url);
		if (url != null && url.toLowerCase().contains("checkin")) {
			KLog.d(AppDebugConfig.TAG_WARN, "isInSignInView = " + isInSignInView);
			isInSignInView = true;
		}
		if (AppDebugConfig.IS_DEBUG) {
			KLog.e(AppDebugConfig.TAG_APP, "handle intent = " + intent + ", url = " + url);
		}
		replaceFragWithTitle(R.id.fl_container, WebFragment.newInstance(url), title);
	}

	@Override
	protected void doBeforeFinish() {
		super.doBeforeFinish();
		KLog.d(AppDebugConfig.TAG_WARN, "close web, isInSignInView = " + isInSignInView);
		if (isInSignInView) {
			ScoreManager.getInstance().initSignInState(this);
			ScoreManager.getInstance().setTaskFinished(true);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleRedirect(intent);
	}
}
