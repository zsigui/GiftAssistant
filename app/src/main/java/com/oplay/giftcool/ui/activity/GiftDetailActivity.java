package com.oplay.giftcool.ui.activity;

import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.listener.OnBackPressListener;
import com.oplay.giftcool.listener.OnShareListener;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.ui.fragment.gift.GiftDetailFragment;
import com.oplay.giftcool.util.InputMethodUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ToastUtil;

/**
 * Created by zsigui on 15-12-31.
 */
public class GiftDetailActivity extends BaseAppCompatActivity {

	private ImageView ivLimitTag;
	private OnShareListener mOnShareListener;

	@Override
	protected void processLogic() {
		loadData();
	}

	@Override
	protected void initView() {
		setContentView(R.layout.activity_gift_detail);
	}

	@Override
	protected void initMenu(@NonNull Toolbar toolbar) {
		super.initMenu(toolbar);
		ivLimitTag = getViewById(toolbar, R.id.iv_tool_limit);
		getViewById(toolbar, R.id.iv_bar_share).setOnClickListener(this);
		showLimitTag(false);
	}

	public void loadData() {
		if (getIntent() == null) {
			ToastUtil.showShort("跳转失败，获取不到礼包ID");
			return;
		}
		int detailId = getIntent().getIntExtra(KeyConfig.KEY_DATA, KeyConfig.TYPE_ID_DEFAULT);
		replaceFrag(R.id.fl_container, GiftDetailFragment.newInstance(detailId), true);
	}

	public void setOnShareListener(OnShareListener shareClickListener) {
		mOnShareListener = shareClickListener;
	}

	public void showLimitTag(boolean isShow){
		if (ivLimitTag != null) {
			if (isShow) {
				ivLimitTag.setVisibility(View.VISIBLE);
			} else {
				ivLimitTag.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		if (v.getId() == R.id.iv_bar_share) {
			if (mOnShareListener != null) {
				mOnShareListener.share();
			} else {
				ToastUtil.showShort("该礼包无分享设置");
			}
		}
	}

	@Override
	public void onBackPressed() {
		InputMethodUtil.hideSoftInput(this);
		if (getTopFragment() != null && getTopFragment() instanceof OnBackPressListener
				&& ((OnBackPressListener) getTopFragment()).onBack()) {
			// back事件被处理
			return;
		}
		if (!popFrag() && !isFinishing()) {
			mNeedWorkCallback = false;
			IntentUtil.jumpHome(GiftDetailActivity.this);
			finish();
		} else {
			if (getTopFragment() instanceof BaseFragment) {
				setBarTitle(((BaseFragment) getTopFragment()).getTitleName());
			}
		}
	}
}
