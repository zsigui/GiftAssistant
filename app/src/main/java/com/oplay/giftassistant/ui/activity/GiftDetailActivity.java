package com.oplay.giftassistant.ui.activity;

import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.KeyConfig;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.gift.GiftDetailFragment;
import com.oplay.giftassistant.util.ToastUtil;

/**
 * Created by zsigui on 15-12-31.
 */
public class GiftDetailActivity extends BaseAppCompatActivity {

	private ImageView ivLimitTag;

	@Override
	protected void processLogic() {
		loadData();
	}

	@Override
	protected void initView() {
		setContentView(R.layout.activity_gift_detail);
	}

	@Override
	protected void initMenu(@NonNull Toolbar toolbar) {
		super.initMenu(toolbar);
		ivLimitTag = getViewById(toolbar, R.id.iv_tool_limit);
		showLimitTag(false);
	}

	public void loadData() {
		if (getIntent() == null) {
			ToastUtil.showShort("跳转失败，获取不到礼包ID");
			return;
		}
		int detailId = getIntent().getIntExtra(KeyConfig.KEY_DATA, KeyConfig.TYPE_ID_DEFAULT);
		replaceFrag(R.id.fl_container, GiftDetailFragment.newInstance(detailId), true);
	}

	public void showLimitTag(boolean isShow){
		if (ivLimitTag != null) {
			if (isShow) {
				ivLimitTag.setVisibility(View.VISIBLE);
			} else {
				ivLimitTag.setVisibility(View.GONE);
			}
		}
	}
}
