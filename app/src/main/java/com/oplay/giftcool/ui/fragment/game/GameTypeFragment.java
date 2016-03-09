package com.oplay.giftcool.ui.fragment.game;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.GameTagAdapter;
import com.oplay.giftcool.adapter.GameTypeMainAdapter;
import com.oplay.giftcool.adapter.other.AutoMeasureGridLayoutManager;
import com.oplay.giftcool.adapter.other.DividerItemDecoration;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.model.data.resp.GameTypeMain;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.socks.library.KLog;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 15-12-30.
 */
public class GameTypeFragment extends BaseFragment {

	private final static String PAGE_NAME = "游戏分类";
	private RecyclerView mTypeMainView;
	private RecyclerView mTagView;
	private GameTypeMainAdapter mGameTypeMainAdapter;
	private GameTagAdapter mTagAdapter;

	private int[] mResIds = new int[]{R.drawable.ic_tag_arpg, R.drawable.ic_tag_card, R.drawable.ic_tag_supernatural,
			R.drawable.ic_tag_action, R.drawable.ic_tag_stategy, R.drawable.ic_tag_round};

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
		mGameTypeMainAdapter = new GameTypeMainAdapter(getContext());
		mTagAdapter = new GameTagAdapter(getContext());
		mTypeMainView.setAdapter(mGameTypeMainAdapter);
		mTagView.setAdapter(mTagAdapter);
		mViewManager.showContent();
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
							if (response.body() != null && response.body().getCode() == NetStatusCode.SUCCESS) {
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
					}
				});
	}

	private void updateData(ArrayList<GameTypeMain> data) {
		if (data == null || data.size() == 0) {
			mViewManager.showEmpty();
			return;
		}
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

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}
}
