package com.oplay.giftassistant.ui.fragment.search;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.IndexGameNewAdapter;
import com.oplay.giftassistant.adapter.IndexGiftNewAdapter;
import com.oplay.giftassistant.model.data.resp.SearchDataResult;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;
import com.oplay.giftassistant.ui.widget.NestedListView;
import com.oplay.giftassistant.util.ToastUtil;

/**
 * Created by zsigui on 15-12-22.
 */
public class ResultFragment extends BaseFragment implements View.OnClickListener {

	private ScrollView mContainer;
	private RelativeLayout mGameBar;
	private NestedListView mGameView;
	private RelativeLayout mGiftBar;
	private NestedListView mGiftView;

	private IndexGiftNewAdapter mGiftAdapter;
	private IndexGameNewAdapter mGameAdapter;


	public static ResultFragment newInstance() {
		return new ResultFragment();
	}


	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_search_data);

		if (!mApp.isAllowDownload()) {
			getViewById(R.id.ll_game).setVisibility(View.GONE);
		}
		mContainer = getViewById(R.id.sv_container);
		mGameView = getViewById(R.id.lv_game);
		mGameBar = getViewById(R.id.rl_game);
		mGiftView = getViewById(R.id.lv_gift);
		mGiftBar = getViewById(R.id.rl_gift);

		((TextView) getViewById(R.id.tv_game_title)).setText(Html.fromHtml("搜到的 <font color='f85454'>游戏</font>"));
		((TextView) getViewById(R.id.tv_gift_title)).setText(Html.fromHtml("搜到的 <font color='f85454'>礼包</font>"));

	}

	@Override
	protected void setListener() {
		mGameBar.setOnClickListener(this);
		mGiftBar.setOnClickListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		mGameAdapter = new IndexGameNewAdapter(getContext());
		mGiftAdapter = new IndexGiftNewAdapter(getActivity());

		mGameView.setAdapter(mGameAdapter);
		mGiftView.setAdapter(mGiftAdapter);
		mContainer.smoothScrollTo(0, 0);
	}

	@Override
	protected void lazyLoad() {
		mHasData = true;
	}

	public void updateData(SearchDataResult data) {
		if (data.games != null && mApp.isAllowDownload()) {
			mGameAdapter.updateData(data.games);
		}
		if (data.gifts != null) {
			mGiftAdapter.updateData(data.gifts);
		}
		mContainer.smoothScrollTo(0, 0);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.rl_game:
				ToastUtil.showShort("游戏被点击");
				break;
			case R.id.rl_gift:
				ToastUtil.showShort("礼包被点击");
				break;
		}
	}
}
