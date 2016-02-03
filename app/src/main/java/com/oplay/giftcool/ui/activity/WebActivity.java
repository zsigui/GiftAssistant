package com.oplay.giftcool.ui.activity;

import android.content.Intent;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.ActivityFragment;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

/**
 * Created by zsigui on 16-1-29.
 */
public class WebActivity extends BaseAppCompatActivity {

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
		String url = intent.getStringExtra(KeyConfig.KEY_URL);
		String title = intent.getStringExtra(KeyConfig.KEY_DATA);
		if (AppDebugConfig.IS_DEBUG) {
			KLog.e(AppDebugConfig.TAG_APP, "handle intent = " + intent + ", url = " + url);
		}
		replaceFragWithTitle(R.id.fl_container, ActivityFragment.newInstance(url), title);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleRedirect(intent);
	}
}
