package com.oplay.giftcool.ui.activity;

import com.oplay.giftcool.R;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.user.PhoneLoginFragment;

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
