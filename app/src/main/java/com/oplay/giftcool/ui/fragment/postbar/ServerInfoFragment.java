package com.oplay.giftcool.ui.fragment.postbar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;

/**
 * Created by zsigui on 16-8-24.
 */
public class ServerInfoFragment extends BaseFragment implements ViewPager.OnPageChangeListener{

        private ViewPager mPager;
        private SmartTabLayout mTabLayout;

        private String[] mTabTitle = new String[]{"开服表", "开测表"};
        private int mCurrentPosition = 0;

        public static ServerInfoFragment newInstance() {
            return new ServerInfoFragment();
        }

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
            mPager.setAdapter(new IndexGamePagerAdapter(getChildFragmentManager()));
            mPager.setOffscreenPageLimit(1);
            mTabLayout.setViewPager(mPager);
            mPager.setCurrentItem(0);
            mPager.addOnPageChangeListener(this);
            mPager.setCurrentItem(mCurrentPosition, true);
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
            } else {
                mCurrentPosition = gamePosition;
            }
        }



        public class IndexGamePagerAdapter extends FragmentPagerAdapter {

            public IndexGamePagerAdapter(FragmentManager fm) {
                super(fm);
            }

            @Override
            public Fragment getItem(int position) {
                if (position == 0) {
                    return OpenServerFragment.newInstance(KeyConfig.TYPE_ID_OPEN_SERVER);
                }
                return OpenServerFragment.newInstance(KeyConfig.TYPE_ID_OPEN_TEST);
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
