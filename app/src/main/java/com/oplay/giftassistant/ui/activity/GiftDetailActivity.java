package com.oplay.giftassistant.ui.activity;

import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.KeyConfig;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.gift.GiftDetailFragment;
import com.oplay.giftassistant.util.ToastUtil;

/**
 * Created by zsigui on 15-12-31.
 */
public class GiftDetailActivity extends BaseAppCompatActivity {


	@Override
	protected void processLogic() {
		loadData();
	}

	@Override
	protected void initView() {
		setContentView(R.layout.activity_common_with_back);
	}

	@Override
	protected void initMenu(@NonNull Toolbar toolbar) {
		super.initMenu(toolbar);
	}

	public void loadData() {
		if (getIntent() == null) {
			ToastUtil.showShort("跳转失败，获取不到礼包ID");
			return;
		}
		int detailId = getIntent().getIntExtra(KeyConfig.KEY_DATA, KeyConfig.TYPE_ID_DEFAULT);
		replaceFrag(R.id.fl_container, GiftDetailFragment.newInstance(detailId), true);
	}
}
