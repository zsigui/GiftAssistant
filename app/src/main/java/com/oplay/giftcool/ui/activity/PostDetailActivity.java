package com.oplay.giftcool.ui.activity;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.postbar.PostDetailFragment;
import com.oplay.giftcool.util.ToastUtil;

/**
 * Created by zsigui on 16-4-11.
 */
public class PostDetailActivity extends BaseAppCompatActivity {


	@Override
	protected void processLogic() {
		if (getIntent() == null) {
			ToastUtil.showShort(ConstString.TEXT_ENTER_ERROR);
			finish();
			return;
		}
		final int mPostId = getIntent().getIntExtra(KeyConfig.KEY_DATA, 0);
		replaceFrag(R.id.fl_container, PostDetailFragment.newInstance(mPostId));
	}

	@Override
	protected void initView() {
		setContentView(R.layout.activity_toolbar_with_share);

	}
}
