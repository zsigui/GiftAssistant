package com.oplay.giftcool.ui.fragment.game;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.GameTagAdapter;
import com.oplay.giftcool.adapter.GameTypeMainAdapter;
import com.oplay.giftcool.adapter.other.AutoMeasureGridLayoutManager;
import com.oplay.giftcool.adapter.other.DividerItemDecoration;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.StatusCode;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.model.data.resp.GameTypeMain;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.IntentUtil;
import com.socks.library.KLog;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 15-12-30.
 */
public class GameTypeFragment extends BaseFragment {


	private RecyclerView mTypeMainView;
	private RecyclerView mTagView;
	private GameTypeMainAdapter mGameTypeMainAdapter;
	private GameTagAdapter mTagAdapter;

	private int[] mResIds = new int[]{R.drawable.ic_tag_arpg, R.drawable.ic_tag_card, R.drawable.ic_tag_supernatural,
			R.drawable.ic_tag_action, R.drawable.ic_tag_stategy, R.drawable.ic_tag_round};

	private OnItemClickListener<GameTypeMain> mMainItemClickListener;
	private OnItemClickListener<GameTypeMain> mTagItemClickListener;

	public static GameTypeFragment newInstance() {
		return new GameTypeFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_game_type);
		mTypeMainView = getViewById(R.id.rv_type);
		mTagView = getViewById(R.id.rv_tag);
	}

	@Override
	protected void setListener() {
		mMainItemClickListener = new OnItemClickListener<GameTypeMain>() {
			@Override
			public void onItemClick(GameTypeMain item, View view, int position) {
				IntentUtil.jumpGameTagList(getContext(), item.id, item.name);
			}
		};
		mTagItemClickListener = new OnItemClickListener<GameTypeMain>() {
			@Override
			public void onItemClick(GameTypeMain item, View view, int position) {
				IntentUtil.jumpGameTagList(getContext(), item.id, item.name);
			}
		};
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		GridLayoutManager typeLayoutManager = new GridLayoutManager(getContext(), 3);
		AutoMeasureGridLayoutManager tagLayoutManager = new AutoMeasureGridLayoutManager(getContext(), 3);
		DividerItemDecoration itemDecoration1 = new DividerItemDecoration(getContext(),
				GridLayoutManager.VERTICAL);
		DividerItemDecoration itemDecoration2 = new DividerItemDecoration(getContext(),
				GridLayoutManager.HORIZONTAL);
		mTagView.addItemDecoration(itemDecoration1);
		mTagView.addItemDecoration(itemDecoration2);

		mTagView.setLayoutManager(tagLayoutManager);
		mTypeMainView.setLayoutManager(typeLayoutManager);
		mGameTypeMainAdapter = new GameTypeMainAdapter(mTypeMainView);
		mTagAdapter = new GameTagAdapter(mTagView);
		mTypeMainView.setAdapter(mGameTypeMainAdapter);
		mTagView.setAdapter(mTagAdapter);
		mGameTypeMainAdapter.setItemClickListener(mMainItemClickListener);
		mTagAdapter.setItemClickListener(mTagItemClickListener);
	}

	@Override
	protected void lazyLoad() {
		refreshInitConfig();
		Global.getNetEngine().obtainIndexGameType(new JsonReqBase<Void>())
				.enqueue(new Callback<JsonRespBase<ArrayList<GameTypeMain>>>() {
					@Override
					public void onResponse(Response<JsonRespBase<ArrayList<GameTypeMain>>> response,
					                       Retrofit retrofit) {
						if (!mCanShowUI) {
							return;
						}
						if (response != null && response.isSuccess()) {
							if (response.body() != null && response.body().getCode() == StatusCode.SUCCESS) {
								refreshSuccessEnd();
								updateData(response.body().getData());
								return;
							}
							if (AppDebugConfig.IS_DEBUG) {
								KLog.d(AppDebugConfig.TAG_APP,
										(response.body() == null ? "解析失败" : response.body().error()));
							}
						}
						refreshFailEnd();
					}

					@Override
					public void onFailure(Throwable t) {
						if (!mCanShowUI) {
							return;
						}
						if (AppDebugConfig.IS_DEBUG) {
							KLog.d(AppDebugConfig.TAG_UTIL, t);
						}
						refreshFailEnd();
						updateData(initStashData());
					}
				});
	}

	private void updateData(ArrayList<GameTypeMain> data) {
		if (data == null)
			return;
		mHasData = true;
		mViewManager.showContent();
		ArrayList<GameTypeMain> header = new ArrayList<>();
		ArrayList<GameTypeMain> footer = new ArrayList<>();
		int headerCount = (data.size() < 6 ? data.size() : 6);
		int k = 0;
		for (; k < headerCount; k++) {
			header.add(data.get(k));
		}
		for (; k < data.size(); k++) {
			footer.add(data.get(k));
		}
		updateTypeMain(header);
		updateTag(footer);
	}

	public void updateTypeMain(ArrayList<GameTypeMain> data) {
		if (data == null) {
			return;
		}
		int i = 0;
		for (GameTypeMain item : data) {
			item.icon = mResIds[i++];
		}
		mGameTypeMainAdapter.updateData(data);
	}

	public void updateTag(ArrayList<GameTypeMain> data) {
		if (data == null) {
			return;
		}
		mTagAdapter.updateData(data);
	}

	private ArrayList<GameTypeMain> initStashData() {
		ArrayList<GameTypeMain> typeData = new ArrayList<GameTypeMain>();
		GameTypeMain g1 = new GameTypeMain();
		g1.id = 1;
		g1.name = "卡牌";
		typeData.add(g1);
		GameTypeMain g2 = new GameTypeMain();
		g2.id = 2;
		g2.name = "回合";
		typeData.add(g2);
		GameTypeMain g3 = new GameTypeMain();
		g3.id = 3;
		g3.name = "动作";
		typeData.add(g3);
		GameTypeMain g4 = new GameTypeMain();
		g4.id = 4;
		g4.name = "ARPG";
		typeData.add(g4);
		GameTypeMain g5 = new GameTypeMain();
		g5.id = 5;
		g5.name = "策略";
		typeData.add(g5);
		GameTypeMain g6 = new GameTypeMain();
		g6.id = 6;
		g6.name = "仙侠";
		typeData.add(g6);

		for (int i = 0; i < 15; i++) {
			GameTypeMain g = new GameTypeMain();
			g.id = i + 15;
			g.name = "策略塔防";
			typeData.add(g);
		}
		return typeData;
	}
}
