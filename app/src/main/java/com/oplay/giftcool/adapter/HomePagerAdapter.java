package com.oplay.giftcool.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by zsigui on 15-12-22.
 */
public class HomePagerAdapter extends FragmentPagerAdapter {

	private List<Fragment> mFragments;


	public HomePagerAdapter(FragmentManager fm, List<Fragment> fragments) {
		super(fm);
		if (fragments == null || fragments.size() == 0) {
			throw new IllegalArgumentException("Can't add a null or empty fragment list here");
		}
		this.mFragments = fragments;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		super.destroyItem(container, position, object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		return super.instantiateItem(container, position);
	}

	@Override
	public Fragment getItem(int position) {
		return this.mFragments.get(position);
	}

	@Override
	public int getCount() {
		return this.mFragments.size();
	}
}
