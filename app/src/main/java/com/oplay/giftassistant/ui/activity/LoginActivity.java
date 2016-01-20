package com.oplay.giftassistant.ui.activity;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.user.PhoneLoginFragment;

/**
 * Created by zsigui on 16-1-6.
 */
public class LoginActivity extends BaseAppCompatActivity {

	@Override
	protected void initView() {
		setContentView(R.layout.activity_common_with_back);
	}

	@Override
	protected void processLogic() {
		replaceFragWithTitle(R.id.fl_container, PhoneLoginFragment.newInstance(),
				getResources().getString(R.string.st_login_phone_title), false);
	}
}
