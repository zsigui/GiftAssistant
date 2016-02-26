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
import com.oplay.giftcool.config.GameTypeUtil;
import com.oplay.giftcool.config.GiftTypeUtil;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.manager.PayManager;
import com.oplay.giftcool.model.AppStatus;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.SearchDataResult;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.ui.widget.NestedListView;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.util.IntentUtil;

/**
 * Created by zsigui on 15-12-22.
 */
public class ResultFragment extends BaseFragment implements View.OnClickListener, OnItemClickListener<IndexGameNew>{

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
		mGameAdapter = new NestedGameListAdapter(getContext(), null, this);
		mGiftAdapter = new NestedGiftListAdapter(getContext());
		mGiftAdapter.setListener(new OnItemClickListener<IndexGiftNew>() {
			@Override
			public void onItemClick(IndexGiftNew gift, View v, int position) {
				switch (v.getId()) {
					case R.id.rl_recommend:
						IntentUtil.jumpGiftDetail(getContext(), gift.id);
						break;
					case R.id.btn_send:
						if (gift.giftType == GiftTypeUtil.GIFT_TYPE_ZERO_SEIZE) {
							// 对于0元抢，先跳转到游戏详情
							IntentUtil.jumpGiftDetail(getContext(), gift.id);
						} else {
							PayManager.getInstance().seizeGift(getContext(), gift, (GiftButton) v);
						}
						break;
				}
			}
		});

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
			mGameAdapter.setData(mData.games);
			mGiftAdapter.setData(mData.gifts);
			if (mData.games == null || mData.games.size() == 0) {
				llGift.setVisibility(View.GONE);
			} else {
				llGift.setVisibility(View.VISIBLE);
			}
			if (mData.gifts == null || mData.gifts.size() == 0
					|| !AssistantApp.getInstance().isAllowDownload()) {
				llGame.setVisibility(View.GONE);
			} else {
				llGame.setVisibility(View.VISIBLE);
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

	@Override
	public void release() {
		super.release();
		mContainer = null;
		mGameBar = null;
		mGameView = null;
		mGiftBar = null;
		mGiftView = null;
		llGame = null;
		llGift = null;
		if (mGameAdapter != null) {
			mGameAdapter.release();
			mGameAdapter = null;
		}
		if (mGiftAdapter != null) {
			mGiftAdapter.release();
			mGiftAdapter = null;
		}
		mData = null;
	}

	@Override
	public void onItemClick(IndexGameNew item, View view, int position) {
		if (view.getId() == R.id.tv_download) {
			if (item != null && !AppStatus.DISABLE.equals(item.appStatus)) {
				item.handleOnClick(getActivity().getSupportFragmentManager());
			}
		}else {
			IntentUtil.jumpGameDetail(getContext(), item.id, GameTypeUtil.JUMP_STATUS_DETAIL);
		}
	}
}
