package com.oplay.giftassistant.ui.activity;

import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.drawer.SettingFragment;

/**
 * Created by zsigui on 16-1-5.
 */
public class SettingActivity extends BaseAppCompatActivity {


	@Override
	protected void initView() {
		setContentView(R.layout.activity_common_with_back);
	}

	@Override
	protected void processLogic() {
		replaceFrag(R.id.fl_container, SettingFragment.newInstance());
	}


	@Override
	protected void initMenu(@NonNull Toolbar toolbar) {
		super.initMenu(toolbar);
		setBarTitle("设置");
	}
}
