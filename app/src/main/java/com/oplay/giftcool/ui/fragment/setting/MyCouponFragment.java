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
 * Created by zsigui on 16-5-31.
 */
public class MyCouponFragment extends BaseFragment {

    private final static String PAGE_NAME = "我的首充券";
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
        mTitles = new String[3];
        mTitles[0] = "已抢";
        mTitles[1] = "我的预约";
        mTitles[2] = "已过期";
        mFragmentType = new int[]{KeyConfig.TYPE_KEY_COUPON_SEIZED,
                KeyConfig.TYPE_KEY_COUPON_RESERVED, KeyConfig.TYPE_KEY_COUPON_OVERTIME};
        mPager.setAdapter(new MyCouponPagerAdapter(getChildFragmentManager()));
        mTabLayout.setViewPager(mPager);
    }

    @Override
    protected void lazyLoad() {
    }

    public static MyCouponFragment newInstance() {
        return new MyCouponFragment();
    }

    @Override
    public String getPageName() {
        return PAGE_NAME;
    }

    public class MyCouponPagerAdapter extends FragmentPagerAdapter {

        public MyCouponPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (mFragments[position] == null) {
                if (mFragmentType[position] == KeyConfig.TYPE_KEY_COUPON_RESERVED) {
                    mFragments[position] = MyCouponReservedFragment.newInstance();
                } else {
                    mFragments[position] = MyCouponListFragment.newInstance(mFragmentType[position]);
                }
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
