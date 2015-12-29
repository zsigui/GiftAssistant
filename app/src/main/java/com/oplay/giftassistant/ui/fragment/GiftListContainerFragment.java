package com.oplay.giftassistant.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.TimeViewPagerAdapter;
import com.oplay.giftassistant.model.data.resp.IndexGiftNew;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zsigui on 15-12-29.
 */
public class GiftListContainerFragment extends BaseFragment{

	private static final String KEY_DATA = "key_data";

	private ViewPager mPager;
	private SmartTabLayout mTabLayout;

	public static GiftListContainerFragment newInstance(ArrayList<ArrayList<IndexGiftNew>> data) {
		GiftListContainerFragment fragment = new GiftListContainerFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(KEY_DATA, data);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_limit);
		mPager = getViewById(R.id.vp_container);
		mTabLayout = getViewById(R.id.tab_layout);
	}

	@Override
	protected void setListener() {
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void processLogic(Bundle savedInstanceState) {
		if (getArguments() == null) {
			throw new IllegalStateException("need to put argument's type of ArrayList<ArrayList<IndexGiftNew>> on it");
		}
		Serializable s = getArguments().getSerializable(KEY_DATA);
		if (s == null) {
			throw new IllegalArgumentException("need to put argument's type of ArrayList<ArrayList<IndexGiftNew>> on it");
		}
		ArrayList<ArrayList<IndexGiftNew>> data = (ArrayList<ArrayList<IndexGiftNew>>) s;
		ArrayList<Fragment> fragments = new ArrayList<>(data.size());
		for(ArrayList<IndexGiftNew> d : data) {
			Fragment f = GiftListDataFragment.newInstance(d);
			fragments.add(f);
		}

		TimeViewPagerAdapter adapter = new TimeViewPagerAdapter(getChildFragmentManager(), fragments);
		mPager.setAdapter(adapter);
		mTabLayout.setViewPager(mPager);
	}

	@Override
	protected void lazyLoad() {
		mHasData = true;
	}

}
