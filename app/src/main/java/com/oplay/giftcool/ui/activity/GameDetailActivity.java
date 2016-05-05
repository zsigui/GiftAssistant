package com.oplay.giftcool.ui.activity;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.config.util.GameTypeUtil;
import com.oplay.giftcool.engine.NoEncryptEngine;
import com.oplay.giftcool.ext.retrofit2.DefaultGsonConverterFactory;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.game.GameDetailFragment;
import com.oplay.giftcool.util.IntentUtil;

import retrofit2.Retrofit;

/**
 * Created by zsigui on 15-12-31.
 */
public class GameDetailActivity extends BaseAppCompatActivity {


	private int mDetailId;
	private int mStatus;
	private int mStatusBarColorIndex;
	private int[] mThemeColor = {R.color.co_rainbow_color_1, R.color.co_rainbow_color_2,
			R.color.co_rainbow_color_3, R.color.co_rainbow_color_4, R.color.co_rainbow_color_5};
	private int[] mThemeColorStr = {R.string.st_rainbow_color_1, R.string.st_rainbow_color_2,
			R.string.st_rainbow_color_3, R.string.st_rainbow_color_4, R.string.st_rainbow_color_5};

	private NoEncryptEngine mEngine;

	public NoEncryptEngine getEngine() {
		return mEngine;
	}

	@Override
	protected void processLogic() {
		loadData();
		mEngine = new Retrofit.Builder()
				.baseUrl(NetUrl.getBaseUrl())
				.client(AssistantApp.getInstance().getHttpClient())
				.addConverterFactory(DefaultGsonConverterFactory.create(AssistantApp.getInstance().getGson()))
				.build()
				.create(NoEncryptEngine.class);
	}

	@Override
	protected int getStatusBarColor() {
		if (getIntent() != null) {
			mDetailId = getIntent().getIntExtra(KeyConfig.KEY_DATA, KeyConfig.TYPE_ID_DEFAULT);
			mStatus = getIntent().getIntExtra(KeyConfig.KEY_STATUS, GameTypeUtil.JUMP_STATUS_DETAIL);
		}
		mStatusBarColorIndex = (int) (Math.random() * 4);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			return getResources().getColor(mThemeColor[mStatusBarColorIndex]);
		} else {
			return getResources().getColor(mThemeColor[mStatusBarColorIndex], null);
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
		replaceFragWithTitle(R.id.fl_container,
				GameDetailFragment.newInstance(mDetailId, mStatus,
						getResources().getString(mThemeColorStr[mStatusBarColorIndex])),
				getResources().getString(R.string.st_game_continue), true);
		if (mToolbar != null) {
			mToolbar.setBackgroundResource(mThemeColor[mStatusBarColorIndex]);
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
