package com.oplay.giftcool.ui.fragment.game;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.GameDetailInfoAdapter;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.model.data.resp.GameDetail;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.ToastUtil;

/**
 * Created by zsigui on 16-5-5.
 */
public class GameDetailInfoFragment extends BaseFragment {

	private GameDetailInfoAdapter mAdapter;
	private RecyclerView rvContent;
	private GameDetail mData;

	public static GameDetailInfoFragment newInstance(String url) {
		GameDetailInfoFragment fragment = new GameDetailInfoFragment();
		Bundle bundle = new Bundle();
		bundle.putString(KeyConfig.KEY_URL, url);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_game_detail_info);
	}

	@Override
	protected void setListener() {

	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		if (getArguments() == null) {
			mViewManager.showEmpty();
			ToastUtil.showShort("获取数据为空");
			if (getActivity() != null) {
				getActivity().finish();
			}
			return;
		}
		mIsSwipeRefresh = true;
	}

	@Override
	protected void lazyLoad() {
	}

	@Override
	public String getPageName() {
		return null;
	}
}
