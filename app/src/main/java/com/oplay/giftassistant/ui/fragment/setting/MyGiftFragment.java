package com.oplay.giftassistant.ui.fragment.setting;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.KeyConfig;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;

/**
 * Created by zsigui on 16-1-6.
 */
public class MyGiftFragment extends BaseFragment {

	private ViewPager mPager;
	private SmartTabLayout mTabLayout;

	private String[] mTitles;
	private Fragment[] mFragments;

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_vp_container);
		mPager = getViewById(R.id.vp_container);
		mTabLayout = getViewById(R.id.tab_layout);
	}

	@Override
	protected void setListener() {
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		mFragments = new Fragment[3];
		mFragments[0] = MyGiftListFragment.newInstance(KeyConfig.TYPE_KEY_SEIZED);
		mFragments[1] = MyGiftListFragment.newInstance(KeyConfig.TYPE_KEY_SEARCH);
		mFragments[2] = MyGiftListFragment.newInstance(KeyConfig.TYPE_KEY_OVERTIME);
		mTitles = new String[3];
		mTitles[0] = "已抢";
		mTitles[1] = "已淘";
		mTitles[2] = "已过期";
		mPager.setAdapter(new MyGiftPagerAdapter(getChildFragmentManager()));
		mTabLayout.setViewPager(mPager);
	}

	@Override
	protected void lazyLoad() {
	}

	public static MyGiftFragment newInstance() {
		return new MyGiftFragment();
	}

	public class MyGiftPagerAdapter extends FragmentPagerAdapter {

		public MyGiftPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments[position];
		}

		@Override
		public int getCount() {
			return mFragments.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mTitles[position];
		}


	}
}
