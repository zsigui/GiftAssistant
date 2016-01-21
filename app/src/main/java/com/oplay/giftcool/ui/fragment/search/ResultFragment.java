package com.oplay.giftcool.ui.fragment.search;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.NestedGameListAdapter;
import com.oplay.giftcool.adapter.NestedGiftListAdapter;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.model.data.resp.SearchDataResult;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.ui.widget.NestedListView;

/**
 * Created by zsigui on 15-12-22.
 */
public class ResultFragment extends BaseFragment implements View.OnClickListener {

	private final static String PAGE_NAME = "搜索结果页";
	private ScrollView mContainer;
	private RelativeLayout mGameBar;
	private NestedListView mGameView;
	private RelativeLayout mGiftBar;
	private NestedListView mGiftView;
	private LinearLayout llGame;
	private LinearLayout llGift;

	private NestedGiftListAdapter mGiftAdapter;
	private NestedGameListAdapter mGameAdapter;
	private SearchDataResult mData;

	public static ResultFragment newInstance(SearchDataResult data) {
		ResultFragment fragment = new ResultFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(KeyConfig.KEY_DATA, data);
		fragment.setArguments(bundle);
		return fragment;
	}


	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_search_data);

		if (!AssistantApp.getInstance().isAllowDownload()) {
			getViewById(R.id.ll_game).setVisibility(View.GONE);
		}
		mContainer = getViewById(R.id.sv_container);
		mGameView = getViewById(R.id.lv_game);
		mGameBar = getViewById(R.id.rl_game);
		mGiftView = getViewById(R.id.lv_gift);
		mGiftBar = getViewById(R.id.rl_gift);
		llGame = getViewById(R.id.ll_game);
		llGift = getViewById(R.id.ll_gift);

		((TextView) getViewById(R.id.tv_game_title)).setText(Html.fromHtml("搜到的 <font color='#f85454'>游戏</font>"));
		((TextView) getViewById(R.id.tv_gift_title)).setText(Html.fromHtml("搜到的 <font color='#f85454'>礼包</font>"));

	}

	@Override
	protected void setListener() {
		mGameBar.setOnClickListener(this);
		mGiftBar.setOnClickListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		mGameAdapter = new NestedGameListAdapter(getContext(), null);
		mGiftAdapter = new NestedGiftListAdapter(getContext());

		if (getArguments() != null) {
			mData = (SearchDataResult) getArguments().getSerializable(KeyConfig.KEY_DATA);
		}
		mGameView.setAdapter(mGameAdapter);
		mGiftView.setAdapter(mGiftAdapter);
		updateData(mData);
		mContainer.smoothScrollTo(0, 0);
	}

	@Override
	protected void lazyLoad() {
		mHasData = true;
	}

	public void updateData(SearchDataResult data) {
		if (data == null || mGameAdapter == null || mGiftAdapter == null
				|| mContainer == null) {
			return;
		}
		if (mData != null) {
			mGameAdapter.setDatas(mData.games);
			mGiftAdapter.setData(mData.gifts);
			if (mData.games == null || mData.games.size() == 0) {
				llGame.setVisibility(View.GONE);
			} else {
				llGame.setVisibility(View.VISIBLE);
			}
			if (mData.gifts == null || mData.gifts.size() == 0
					|| !AssistantApp.getInstance().isAllowDownload()) {
				llGift.setVisibility(View.GONE);
			} else {
				llGift.setVisibility(View.VISIBLE);
			}
		}
		mGameAdapter.updateData(data.games);
		mGiftAdapter.updateData(data.gifts);
		mContainer.smoothScrollTo(0, 0);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.rl_game:
				break;
			case R.id.rl_gift:
				break;
		}
	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}
}
