package com.oplay.giftcool.ui.fragment.setting;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.WebViewUrl;
import com.oplay.giftcool.listener.OnBackPressListener;
import com.oplay.giftcool.listener.OnHandleListener;
import com.oplay.giftcool.listener.ToolbarListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.ui.fragment.dialog.WebViewDialog;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ToastUtil;

/**
 * Created by zsigui on 16-5-31.
 */
public class MyCouponFragment extends BaseFragment implements OnBackPressListener{

    private final static String PAGE_NAME = "我的首充券";
    private ViewPager mPager;
    private SmartTabLayout mTabLayout;

    private String[] mTitles;
    private Fragment[] mFragments;
    private int[] mFragmentType;

    @Override
    protected void initView(Bundle savedInstanceState) {
        if (!AccountManager.getInstance().isLogin()) {
//            ToastUtil.showShort(mApp.getResources().getString(R.string.st_hint_un_login));
            ToastUtil.showShort(ConstString.TOAST_SESSION_UNAVAILABLE);
            IntentUtil.jumpLoginNoToast(getContext());
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
        mFragmentType = new int[]{KeyConfig.TYPE_KEY_SEIZED,
                KeyConfig.TYPE_KEY_RESERVED, KeyConfig.TYPE_KEY_OVERTIME};
        mPager.setOffscreenPageLimit(1);
        mPager.setAdapter(new MyCouponPagerAdapter(getChildFragmentManager()));
        mTabLayout.setViewPager(mPager);

        setToolbarDescription();
    }

    private long mLastClickTime = 0;

    private void setToolbarDescription() {
        if (getContext() != null && getContext() instanceof ToolbarListener) {
            ToolbarListener activity = ((ToolbarListener) getContext());
            activity.showRightBtn(View.VISIBLE, mApp.getResources().getString(R.string.st_wallet_money_note));
            activity.setRightBtnEnabled(true);
            activity.setHandleListener(new OnHandleListener() {
                @Override
                public void deal() {
                    long time = System.currentTimeMillis();
                    if (time - mLastClickTime < 500) {
                        mLastClickTime = time;
                        return;
                    }
                    WebViewDialog dialog;
                    dialog = WebViewDialog.newInstance(
                            mApp.getResources().getString(R.string.st_my_coupon_note), WebViewUrl.getWebUrl(WebViewUrl.COUPON_DETAIL_NOTE));
                    dialog.show(getChildFragmentManager(), WebViewDialog.class.getSimpleName());
                }
            });
        }
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

    @Override
    public boolean onBack() {
        // 隐藏输入框
        if (getContext() != null && getContext() instanceof ToolbarListener) {
            ((ToolbarListener) getContext()).showRightBtn(View.GONE, "");
        }
        return false;
    }

    public class MyCouponPagerAdapter extends FragmentPagerAdapter {

        public MyCouponPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (mFragments[position] == null) {
                if (mFragmentType[position] == KeyConfig.TYPE_KEY_RESERVED) {
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
