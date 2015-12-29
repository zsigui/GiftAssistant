package com.oplay.giftassistant.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.oplay.giftassistant.util.DateUtil;

import java.util.ArrayList;

/**
 * Created by zsigui on 15-12-29.
 */
public class TimeViewPagerAdapter extends FragmentStatePagerAdapter {

	private ArrayList<Fragment> mFragments;
	private ArrayList<String> mTitles;

	public TimeViewPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	public TimeViewPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
		super(fm);
		mFragments = fragments;
		initTitle();
	}

	private void initTitle() {
		mTitles = new ArrayList<>();
		if (mFragments.size() == 1) {
			mTitles.add("今天");
		} else if (mFragments.size() == 2) {
			mTitles.add("今天");
			mTitles.add("昨天");
		} else if (mFragments.size() == 3) {
			mTitles.add("今天");
			mTitles.add("昨天");
			mTitles.add("前天");
		}else if (mFragments.size() > 3) {
			mTitles.add("今天");
			mTitles.add("昨天");
			mTitles.add("前天");
			for (int i = 3; i < mFragments.size(); i++) {
				mTitles.add(DateUtil.getDate("MM-dd", i * -1));
			}
		}
	}

	public void setFragments(ArrayList<Fragment> fragments) {
		mFragments = fragments;
		initTitle();
	}


	@Override
	public Fragment getItem(int position) {
		return getCount() == 0 ? null : mFragments.get(position);
	}

	@Override
	public int getCount() {
		return mFragments == null ? 0 : mFragments.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mTitles == null ? null : mTitles.get(position);
	}
}
