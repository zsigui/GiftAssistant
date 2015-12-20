package com.oplay.giftassistant.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by zsigui on 15-12-16.
 */
public class HomeAdapter extends FragmentPagerAdapter {

	private List<String> mTitles;
	private List<Fragment> mContents;

	public HomeAdapter(FragmentManager fm) {
		super(fm);
	}

	public HomeAdapter(FragmentManager fm, List<String> titles, List<Fragment> contents) {
		super(fm);
		mTitles = titles;
		mContents = contents;
	}

	public List<String> getTitles() {
		return mTitles;
	}

	public void setTitles(List<String> titles) {
		mTitles = titles;
	}

	public List<Fragment> getContents() {
		return mContents;
	}

	public void setContents(List<Fragment> contents) {
		mContents = contents;
	}

	@Override
	public Fragment getItem(int position) {
		return mContents.get(position);
	}

	@Override
	public int getCount() {
		return mTitles.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mTitles.get(position);
	}
}
