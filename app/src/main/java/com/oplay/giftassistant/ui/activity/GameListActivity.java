package com.oplay.giftassistant.ui.activity;

import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.NetUrl;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.game.GameListFragment;

/**
 * Created by zsigui on 16-1-4.
 */
public class GameListActivity extends BaseAppCompatActivity {

	public static String KEY_TYPE = "key_type";
	public static String KEY_SEARCH = "key_search";

	// 新游推荐
	public static final int TYPE_GAME_NEW = 0;
	// 热门游戏
	public static final int TYPE_GAME_HOT = 2;
	// 游戏搜索
	public static final int TYPE_GAME_SEARCH = 3;
	private int type;
	private String mSearchKey;


	@Override
	protected void initView() {
		setContentView(R.layout.activity_common_with_back);

		if (getIntent() != null) {
			type = getIntent().getIntExtra(KEY_TYPE, TYPE_GAME_NEW);
			if (type == TYPE_GAME_SEARCH) {
				mSearchKey = getIntent().getStringExtra(KEY_SEARCH);
			}
		}
	}

	@Override
	protected void initMenu(@NonNull Toolbar toolbar) {
		super.initMenu(toolbar);
		switch (type) {
			case TYPE_GAME_NEW:
				setBarTitle("新游推荐");
				break;
			case TYPE_GAME_SEARCH:
				setBarTitle("游戏搜索");
				break;
			case TYPE_GAME_HOT:
				setBarTitle("热门手游");
		}
	}

	@Override
	protected void processLogic() {
		switch (type) {
			case TYPE_GAME_NEW:
				replaceFrag(R.id.fl_container, GameListFragment.newInstance(NetUrl.GAME_GET_NEW, mSearchKey), false);
				break;
			case TYPE_GAME_HOT:
				replaceFrag(R.id.fl_container, GameListFragment.newInstance(NetUrl.GAME_GET_HOT, mSearchKey), false);
				break;
			case TYPE_GAME_SEARCH:
				replaceFrag(R.id.fl_container, GameListFragment.newInstance(NetUrl.GAME_GET_SEARCH, mSearchKey),
						false);
				break;
		}
	}
}
