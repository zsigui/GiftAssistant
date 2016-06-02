package com.oplay.giftcool.ui.fragment.setting;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ToastUtil;

/**
 * Created by zsigui on 16-1-6.
 */
public class MyGiftFragment extends BaseFragment {

    private final static String PAGE_NAME = "我的礼包";
    private ViewPager mPager;
    private SmartTabLayout mTabLayout;

    private String[] mTitles;
    private Fragment[] mFragments;
    private int[] mFragmentType;

    @Override
    protected void initView(Bundle savedInstanceState) {
        if (!AccountManager.getInstance().isLogin()) {
            ToastUtil.showShort(mApp.getResources().getString(R.string.st_hint_un_login));
            IntentUtil.jumpLogin(getContext());
            if (getActivity() != null) {
                getActivity().finish();
            }
            return;
        }
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
        mFragmentType = new int[]{KeyConfig.TYPE_KEY_SEIZED, KeyConfig.TYPE_KEY_SEARCH, KeyConfig.TYPE_KEY_OVERTIME};
        mTitles = new String[3];
        mTitles[0] = "已抢";
        mTitles[1] = "已淘";
        mTitles[2] = "已过期";
        mPager.setOffscreenPageLimit(1);
        mPager.setAdapter(new MyGiftPagerAdapter(getChildFragmentManager()));
        mTabLayout.setViewPager(mPager);
    }

    @Override
    protected void lazyLoad() {
    }

    public static MyGiftFragment newInstance() {
        return new MyGiftFragment();
    }

    @Override
    public String getPageName() {
        return PAGE_NAME;
    }

    public class MyGiftPagerAdapter extends FragmentPagerAdapter {

        public MyGiftPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (mFragments[position] == null) {
                mFragments[position] = MyGiftListFragment.newInstance(mFragmentType[position]);
            }
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
