package com.jackiez.giftassistant.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.jackiez.giftassistant.R;
import com.jackiez.giftassistant.ui.fragment.GiftFragment;
import com.jackiez.giftassistant.ui.fragment.MoocRecyclerViewFragment;

import cn.bingoogolapple.bgaindicator.BGAFixedIndicator;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class MainActivity extends BaseActivity {

	private ViewPager mContainerPager;
	private BGAFixedIndicator mIndicator;
	private Fragment[] mFragments;
	private String[] mTitles;


    @Override
    protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.activity_main);
	    mIndicator = getViewById(R.id.fixIndicator);
	    mContainerPager = getViewById(R.id.vpContainer);

    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
	    mFragments = new Fragment[2];
	    mFragments[0] = new GiftFragment();
	    mFragments[1] = new MoocRecyclerViewFragment();
	    mTitles = new String[2];
	    mTitles[0] = "标题1";
	    mTitles[1] = "标题2";
	    mContainerPager.setAdapter(new ContentViewPagerAdapter(getSupportFragmentManager()));
		mIndicator.initData(0, mContainerPager);
    }

	class ContentViewPagerAdapter extends FragmentPagerAdapter {

		public ContentViewPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return mTitles.length;
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments[position];
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mTitles[position];
		}
	}
}
