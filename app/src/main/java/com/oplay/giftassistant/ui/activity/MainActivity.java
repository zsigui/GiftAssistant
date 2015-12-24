package com.oplay.giftassistant.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckedTextView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.GiftFragment;
import com.oplay.giftassistant.ui.fragment.MoocRecyclerViewFragment;
import com.oplay.giftassistant.ui.widget.search.SearchLayout;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class MainActivity extends BaseAppCompatActivity {

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
			mSearchLayout.setCanGetFocus(false);
			mSearchLayout.setOnClickListener(this);
		}
	}

	protected void processLogic() {
		for (CheckedTextView ctv : mCtvs) {
			ctv.setOnClickListener(this);
		}
		if (mGiftFragment == null) {
			mGiftFragment = GiftFragment.newInstance();
		}
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
		if (mCurIndex == 0) {
			if (mGiftFragment == null) {
				mGiftFragment = GiftFragment.newInstance();
			}
			reattachFrag(R.id.fl_main_container, mGiftFragment, mGiftFragment.getClass().getSimpleName());
		} else {
			if (mMoocRecyclerViewFragment == null) {
				mMoocRecyclerViewFragment = MoocRecyclerViewFragment.newInstance();
			}
			reattachFrag(R.id.fl_main_container, mMoocRecyclerViewFragment,
					mMoocRecyclerViewFragment.getClass().getSimpleName());
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
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
				Intent intent = new Intent(MainActivity.this, SearchActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				break;
		}
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
