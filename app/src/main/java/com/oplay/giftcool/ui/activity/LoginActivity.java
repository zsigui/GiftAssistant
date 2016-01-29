package com.oplay.giftcool.ui.activity;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.login.PhoneLoginFragment;

/**
 * Created by zsigui on 16-1-6.
 */
public class LoginActivity extends BaseAppCompatActivity {

	private int type = 0;

	@Override
	protected void initView() {
		setContentView(R.layout.activity_common_with_back);
	}

	@Override
	protected void processLogic() {
		if (getIntent() != null)
			type = getIntent().getIntExtra(KeyConfig.KEY_TYPE, 0);

		if (type == 0) {
			replaceFragWithTitle(R.id.fl_container, PhoneLoginFragment.newInstance(),
					getResources().getString(R.string.st_login_phone_title), false);
		} else {
			replaceFragWithTitle(R.id.fl_container, PhoneLoginFragment.newInstance(),
					getResources().getString(R.string.st_login_ouwan_title), false);
		}
	}
}
