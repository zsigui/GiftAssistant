package com.oplay.giftassistant.ui.activity;

import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.KeyConfig;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.game.GameDetailFragment;
import com.oplay.giftassistant.ui.fragment.gift.GiftDetailFragment;
import com.oplay.giftassistant.util.ToastUtil;

/**
 * Created by zsigui on 15-12-31.
 */
public class DetailActivity extends BaseAppCompatActivity {


	private int mDetailId;
	private String mDetailName;
	private int mStatusBarColor;
	private int mType;

	@Override
	protected void processLogic() {
		loadData();
	}

	@Override
	protected int getStatusBarColor() {
		if (getIntent() != null) {
			mDetailId = getIntent().getIntExtra(KeyConfig.KEY_DATA, KeyConfig.TYPE_ID_DEFAULT);
			mDetailName = getIntent().getStringExtra(KeyConfig.KEY_NAME);
			mType = getIntent().getIntExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_DEFAULT);
		}
		if (mType == KeyConfig.TYPE_ID_GAME_DETAIL) {
			int[] colors = {R.color.co_rainbow_color_1, R.color.co_rainbow_color_2,
					R.color.co_rainbow_color_3, R.color.co_rainbow_color_4};
			mStatusBarColor = colors[(int) (Math.random() * 4)];
			return getResources().getColor(mStatusBarColor);
		} else {
			return super.getStatusBarColor();
		}
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
		switch (mType) {
			case KeyConfig.TYPE_ID_GAME_DETAIL:
				replaceFragWithTitle(R.id.fl_container, GameDetailFragment.newInstance(mDetailId, mStatusBarColor),
						"游戏专区", true);
				if (mToolbar != null) {
					mToolbar.setBackgroundResource(mStatusBarColor);
				}
				break;
			case KeyConfig.TYPE_ID_GIFT_DETAIL:
				replaceFragWithTitle(R.id.fl_container, GiftDetailFragment.newInstance(mDetailId),
						mDetailName, true);
				/*replaceFragWithTitle(R.id.fl_container, GiftWebDetailFragment.newInstance(mDetailId),
						mDetailName, true);*/
				break;
			default:
				ToastUtil.showShort("传递类型错误 : " + mType);
		}
	}
}
