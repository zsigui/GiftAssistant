package com.oplay.giftcool.ui.fragment.game;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.oplay.giftcool.R;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.socks.library.KLog;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2015/12/27
 */
public class GameFragment extends BaseFragment implements ViewPager.OnPageChangeListener{

	private ViewPager mPager;
	private SmartTabLayout mTabLayout;

	private String[] mTabTitle = new String[]{"精品", "类别", "榜单"};
	private GameSuperFragment mSuperFragment;
	private GameTypeFragment mTypeFragment;
	private GameNoticeFragment mNoticeFragment;
	private int mCurrentPosition = 0;

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
	    mPager.setOffscreenPageLimit(2);
	    mTabLayout.setViewPager(mPager);
	    mPager.setCurrentItem(0);
	    mPager.addOnPageChangeListener(this);
    }

    @Override
    protected void lazyLoad() {
    }

	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

	}

	@Override
	public void onPageSelected(int position) {
		mCurrentPosition = position;
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	public void setPagePosition(int gamePosition) {
		if (mPager != null && mPager.getAdapter() != null) {
			mPager.setCurrentItem(gamePosition);
		}
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

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		KLog.d(mCurrentPosition);
		if (mSuperFragment!=null && mCurrentPosition == 0) {
			mSuperFragment.setUserVisibleHint(isVisibleToUser);
		}
		if (mTypeFragment!=null && mCurrentPosition == 1) {
			mTypeFragment.setUserVisibleHint(isVisibleToUser);
		}
		if (mNoticeFragment!=null && mCurrentPosition == 2) {
			mNoticeFragment.setUserVisibleHint(isVisibleToUser);
		}
		super.setUserVisibleHint(isVisibleToUser);
	}
}
