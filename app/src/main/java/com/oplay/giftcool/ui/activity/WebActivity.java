package com.oplay.giftcool.ui.activity;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.ActivityFragment;
import com.oplay.giftcool.util.ToastUtil;

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
		String url = getIntent().getStringExtra(KeyConfig.KEY_URL);
		String title = getIntent().getStringExtra(KeyConfig.KEY_DATA);
		replaceFragWithTitle(R.id.fl_container, ActivityFragment.newInstance(url), title);
	}
}
