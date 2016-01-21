package com.oplay.giftcool.ui.activity;

import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.listener.OnShareListener;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.gift.GiftDetailFragment;
import com.oplay.giftcool.util.ToastUtil;

/**
 * Created by zsigui on 15-12-31.
 */
public class GiftDetailActivity extends BaseAppCompatActivity {

	private ImageView ivLimitTag;
	private ImageView ivShare;
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
		ivShare = getViewById(toolbar, R.id.iv_bar_share);
		ivShare.setOnClickListener(this);
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
}
