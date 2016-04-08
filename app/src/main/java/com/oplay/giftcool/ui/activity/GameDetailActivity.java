package com.oplay.giftcool.ui.activity;

import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.util.GameTypeUtil;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.game.GameDetailFragment;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ToastUtil;

/**
 * Created by zsigui on 15-12-31.
 */
public class GameDetailActivity extends BaseAppCompatActivity {


	private int mDetailId;
	private int mStatus;
	private int mStatusBarColorIndex;
	private int mType;
	private int[] mThemeColor = {R.color.co_rainbow_color_1, R.color.co_rainbow_color_2,
			R.color.co_rainbow_color_3, R.color.co_rainbow_color_4, R.color.co_rainbow_color_5};
	private int[] mThemeColorStr = {R.string.st_rainbow_color_1, R.string.st_rainbow_color_2,
			R.string.st_rainbow_color_3, R.string.st_rainbow_color_4, R.string.st_rainbow_color_5};

	@Override
	protected void processLogic() {
		loadData();
	}

	@Override
	protected int getStatusBarColor() {
		if (getIntent() != null) {
			mDetailId = getIntent().getIntExtra(KeyConfig.KEY_DATA, KeyConfig.TYPE_ID_DEFAULT);
			mStatus = getIntent().getIntExtra(KeyConfig.KEY_STATUS, GameTypeUtil.JUMP_STATUS_DETAIL);
			mType = getIntent().getIntExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_DEFAULT);
		}
		if (mType == KeyConfig.TYPE_ID_GAME_DETAIL) {
			mStatusBarColorIndex = (int) (Math.random() * 4);
			return getResources().getColor(mThemeColor[mStatusBarColorIndex]);
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
				replaceFragWithTitle(R.id.fl_container,
						GameDetailFragment.newInstance(mDetailId, mStatus,
								getResources().getString(mThemeColorStr[mStatusBarColorIndex])),
						getResources().getString(R.string.st_game_continue), true);
				if (mToolbar != null) {
					mToolbar.setBackgroundResource(mThemeColor[mStatusBarColorIndex]);
				}
				break;
			default:
				ToastUtil.showShort("传递类型错误 : " + mType);
		}
	}

	@Override
	protected void doBeforeFinish() {
		super.doBeforeFinish();
		if (MainActivity.sGlobalHolder == null) {
			IntentUtil.jumpHome(this, false);
		}
	}
}
