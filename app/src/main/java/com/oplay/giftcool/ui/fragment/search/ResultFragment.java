package com.oplay.giftcool.ui.fragment.search;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
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

import java.util.ArrayList;

/**
 * Created by zsigui on 15-12-22.
 */
public class ResultFragment extends BaseFragment implements View.OnClickListener, OnItemClickListener<IndexGameNew>{

	private final static String PAGE_NAME = "搜索结果页";
	private ScrollView mContainer;
	private NestedListView mGameView;
	private NestedListView mGiftView;
	private NestedListView mGuessGiftView;
	private LinearLayout llGame;
	private LinearLayout llGift;
	private LinearLayout llGuessGift;

	private NestedGameListAdapter mGameAdapter;
	private NestedGiftListAdapter mGiftAdapter;
	private NestedGiftListAdapter mGuessGiftAdapter;

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
		mGiftView = getViewById(R.id.lv_gift);
		mGuessGiftView = getViewById(R.id.lv_like);
		llGame = getViewById(R.id.ll_game);
		llGift = getViewById(R.id.ll_gift);
		llGuessGift = getViewById(R.id.ll_like);

		((TextView) getViewById(R.id.tv_game_title)).setText(Html.fromHtml("搜到的 <font color='#f85454'>游戏</font>"));
		((TextView) getViewById(R.id.tv_gift_title)).setText(Html.fromHtml("搜到的 <font color='#f85454'>礼包</font>"));
		((TextView) getViewById(R.id.tv_like_title)).setText(Html.fromHtml("猜你喜欢的 <font color='#f85454'>礼包</font>"));

	}

	@Override
	protected void setListener() {
		/**
		 * 定义礼包项的点击事件
		 */
		OnItemClickListener<IndexGiftNew> giftItemClickListener = new OnItemClickListener<IndexGiftNew>() {
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
		};
		mGiftAdapter.setListener(giftItemClickListener);
		mGuessGiftAdapter.setListener(giftItemClickListener);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		mGameAdapter = new NestedGameListAdapter(getContext(), null, this);
		mGiftAdapter = new NestedGiftListAdapter(getContext());
		mGuessGiftAdapter = new NestedGiftListAdapter(getContext());

		SearchDataResult data = null;
		if (getArguments() != null) {
			data = (SearchDataResult) getArguments().getSerializable(KeyConfig.KEY_DATA);
		}
		mGameView.setAdapter(mGameAdapter);
		mGiftView.setAdapter(mGiftAdapter);
		mGuessGiftView.setAdapter(mGuessGiftAdapter);
		updateData(data);
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
		mGameAdapter.setData(data.games);
		mGiftAdapter.setData(data.gifts);
		mGuessGiftAdapter.setData(data.guessGift);
		if (!AssistantApp.getInstance().isAllowDownload()) {
			data.games = null;
		}
		showDataView(data.games, llGame);
		showDataView(data.gifts, llGift);
		showDataView(data.guessGift, llGuessGift);
		mGameAdapter.updateData(data.games);
		mGiftAdapter.updateData(data.gifts);
		mGuessGiftAdapter.updateData(data.guessGift);
		mContainer.smoothScrollTo(0, 0);
	}

	private void showDataView(ArrayList data, View v) {
		if (data == null || data.size() == 0) {
			v.setVisibility(View.GONE);
		} else {
			v.setVisibility(View.VISIBLE);
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
		mGameView = null;
		mGiftView = null;
		mGuessGiftView = null;
		llGame = null;
		llGift = null;
		llGuessGift = null;
		if (mGameAdapter != null) {
			mGameAdapter.release();
			mGameAdapter = null;
		}
		if (mGiftAdapter != null) {
			mGiftAdapter.release();
			mGiftAdapter = null;
		}
		if (mGuessGiftAdapter != null) {
			mGuessGiftAdapter.release();
			mGuessGiftAdapter = null;
		}
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
