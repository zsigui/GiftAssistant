package com.oplay.giftassistant.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2015/12/27
 */
public class GameFragment extends BaseFragment {

	private ViewPager mPager;
	private SmartTabLayout mTabLayout;

	private String[] mTabTitle = new String[]{"精品", "类别", "榜单"};
	private GameSuperFragment mSuperFragment;
	private GameTypeFragment mTypeFragment;
	private GameNoticeFragment mNoticeFragment;

	public static GameFragment newInstance() {
		return new GameFragment();
	}

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_games);
	    mPager = getViewById(R.id.vp_container);
	    mTabLayout = getViewById(R.id.tab_layout);
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
	    mPager.setAdapter(new IndexGamePagerAdapter(getChildFragmentManager()));
	    mTabLayout.setViewPager(mPager);
	    mPager.setCurrentItem(0);
    }

    @Override
    protected void lazyLoad() {

    }

	public class IndexGamePagerAdapter extends FragmentPagerAdapter {

		public IndexGamePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if (position == 0) {
				if (mSuperFragment == null) {
					mSuperFragment = GameSuperFragment.newInstance();
				}
				return mSuperFragment;
			} else if (position == 1) {
				if (mTypeFragment == null) {
					mTypeFragment = GameTypeFragment.newInstance();
				}
				return mTypeFragment;
			} else if (position == 2) {
				if (mNoticeFragment == null) {
					mNoticeFragment = GameNoticeFragment.newInstance();
				}
				return mNoticeFragment;
			}
			return null;
		}

		@Override
		public int getCount() {
			return mTabTitle.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mTabTitle[position];
		}
	}
}
