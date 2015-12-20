package com.oplay.giftassistant.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.GiftFragment;
import com.oplay.giftassistant.ui.fragment.MoocRecyclerViewFragment;

import cn.bingoogolapple.bgaindicator.BGAFixedIndicator;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class MainActivity extends BaseAppCompatActivity {

	private ViewPager mContainerPager;
	private BGAFixedIndicator mIndicator;
	private Fragment[] mFragments;
	private String[] mTitles;
    private long mLastClickTime = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		processLogic(savedInstanceState);
	}

	protected void initView() {
		setContentView(R.layout.activity_main);
	    mIndicator = getViewById(R.id.fixIndicator);
	    mContainerPager = getViewById(R.id.vpContainer);

    }

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (System.currentTimeMillis() - mLastClickTime <= 1000) {
            mLastClickTime = System.currentTimeMillis();
            mApp.exit();
            finish();
        }
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
