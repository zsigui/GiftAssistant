package com.oplay.giftcool.ui.activity;

import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.game.GameListFragment;

/**
 * Created by zsigui on 16-1-4.
 */
public class GameListActivity extends BaseAppCompatActivity {

	private int type;
	private String mSearchKey;
	private int mGameTypeId;
	private String mGameTypeName;


	@Override
	protected void initView() {
		setContentView(R.layout.activity_common_with_back);

		if (getIntent() != null) {
			type = getIntent().getIntExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_GAME_NEW);
			if (type == KeyConfig.TYPE_ID_GAME_SEARCH) {
				mSearchKey = getIntent().getStringExtra(KeyConfig.KEY_SEARCH);
			} else if (type == KeyConfig.TYPE_ID_GAME_TYPE) {
				mGameTypeId = getIntent().getIntExtra(KeyConfig.KEY_DATA, -1);
				mGameTypeName = getIntent().getStringExtra(KeyConfig.KEY_NAME);
			}
		}
	}

	@Override
	protected void initMenu(@NonNull Toolbar toolbar) {
		super.initMenu(toolbar);
		switch (type) {
			case KeyConfig.TYPE_ID_GAME_NEW:
				setBarTitle("新游推荐");
				break;
			case KeyConfig.TYPE_ID_GAME_TYPE:
				setBarTitle("游戏-" + mGameTypeName);
				break;
			case KeyConfig.TYPE_ID_GAME_SEARCH:
				setBarTitle("游戏搜索");
				break;
			case KeyConfig.TYPE_ID_GAME_HOT:
				setBarTitle("热门手游");
		}
	}

	@Override
	protected void processLogic() {
		switch (type) {
			case KeyConfig.TYPE_ID_GAME_NEW:
				replaceFrag(R.id.fl_container, GameListFragment.newInstance(NetUrl.GAME_GET_NEW, mSearchKey), false);
				break;
			case KeyConfig.TYPE_ID_GAME_HOT:
				replaceFrag(R.id.fl_container, GameListFragment.newInstance(NetUrl.GAME_GET_HOT, mSearchKey), false);
				break;
			case KeyConfig.TYPE_ID_GAME_SEARCH:
				replaceFrag(R.id.fl_container, GameListFragment.newInstance(NetUrl.GAME_GET_SEARCH, mSearchKey),
						false);
				break;
			case KeyConfig.TYPE_ID_GAME_TYPE:
				replaceFrag(R.id.fl_container, GameListFragment.newInstance(NetUrl.GAME_GET_TYPE, mGameTypeId), false);
				break;
		}
	}
}
