package com.oplay.giftassistant.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckedTextView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.HomePagerAdapter;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.GiftFragment;
import com.oplay.giftassistant.ui.fragment.MoocRecyclerViewFragment;
import com.oplay.giftassistant.ui.widget.search.SearchLayout;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class MainActivity extends BaseAppCompatActivity {

	private ViewPager mContainerPager;
	private long mLastClickTime = 0;
	private CheckedTextView[] mCtvs;
	private int mCurIndex = 0;
	private Fragment mGiftFragment;
	private Fragment mMoocRecyclerViewFragment;
	private SearchLayout mSearchLayout;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		processLogic();
	}

	protected void initView() {
		setContentView(R.layout.activity_main);
		mContainerPager = getViewById(R.id.vpContainer);
		CheckedTextView ctvGame = getViewById(R.id.ctv_game);
		CheckedTextView ctvGift = getViewById(R.id.ctv_gift);
		mCtvs = new CheckedTextView[2];
		mCtvs[0] = ctvGift;
		mCtvs[1] = ctvGame;
	}

	@Override
	protected void initMenu(@NonNull Toolbar toolbar) {
		super.initMenu(toolbar);
		mSearchLayout = getViewById(toolbar, R.id.sl_search);
		if (mSearchLayout != null) {
			mSearchLayout.clearFocus();
			mSearchLayout.setOnClickListener(this);
		}
	}

	protected void processLogic() {
		List<Fragment> fragments = new ArrayList<>(2);
		fragments.add(new GiftFragment());
		fragments.add(new MoocRecyclerViewFragment());
		for (CheckedTextView ctv : mCtvs) {
			ctv.setOnClickListener(this);
		}
		mContainerPager.setAdapter(new HomePagerAdapter(getSupportFragmentManager(), fragments));
		mContainerPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				setCurSelected(position);
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});
		if (mGiftFragment == null) {
			mGiftFragment = GiftFragment.newInstance();
		}
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.fl_container, mGiftFragment);
		ft.commit();
		setCurSelected(0);
	}

	private void setCurSelected(int position) {
		mCurIndex = position;
		for (CheckedTextView ctv : mCtvs) {
			ctv.setChecked(false);
			ctv.setTextColor(getResources().getColor(R.color.co_tabs_index_text_unchecked));
		}
		mCtvs[mCurIndex].setChecked(true);
		mCtvs[mCurIndex].setTextColor(getResources().getColor(R.color.co_common_green_normal));
		mContainerPager.setCurrentItem(mCurIndex);
		if (mCurIndex == 0) {
			if (mGiftFragment == null) {
				mGiftFragment = GiftFragment.newInstance();
			}
			replaceFragment(mGiftFragment);
		} else {
			if (mMoocRecyclerViewFragment == null) {
				mMoocRecyclerViewFragment = MoocRecyclerViewFragment.newInstance();
			}
			replaceFragment(mMoocRecyclerViewFragment);
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		if (AppDebugConfig.IS_DEBUG) {
			KLog.i(v);
		}
		for (int i = 0; i < mCtvs.length; i++) {
			if (v.getId() == mCtvs[i].getId()) {
				setCurSelected(i);
				return;
			}
		}
		switch (v.getId()) {
			case R.id.ctv_gift:
				setCurSelected(0);
				break;
			case R.id.ctv_game:
				setCurSelected(1);
				break;
			case R.id.sl_search:
				Intent intent = new Intent();
				break;
		}
	}

	private void replaceFragment(Fragment newFrag) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.fl_container, newFrag);
		ft.commit();
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

}
