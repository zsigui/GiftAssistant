package com.oplay.giftcool.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.listener.OnBackPressListener;
import com.oplay.giftcool.listener.OnShareListener;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.sharesdk.ShareSDKConfig;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.ui.fragment.gift.GiftDetailFragment;
import com.oplay.giftcool.util.InputMethodUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

/**
 * Created by zsigui on 15-12-31.
 */
public class GiftDetailActivity extends BaseAppCompatActivity {

	private ImageView ivLimitTag;
	private ImageView ivShare;
	private OnShareListener mOnShareListener;
	private int mId;

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
		if (ivShare != null) {
			ivShare.setOnClickListener(this);
		}
		showLimitTag(false, 0);
	}

	public void loadData() {
		if (getIntent() == null) {
			ToastUtil.showShort("跳转失败，获取不到礼包ID");
			return;
		}

		handleIntent(getIntent());
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			// 来自浏览器的URI请求
			Uri uri = getIntent().getData();
			if (uri != null) {
				try {
					mId = Integer.parseInt(uri.getQueryParameter("plan_id"));
				} catch (Exception e) {
					mId = 0;
					ToastUtil.showShort("跳转链接出错");
					onBackPressed();
					return;
				}
			}
		} else {
			mId = getIntent().getIntExtra(KeyConfig.KEY_DATA, KeyConfig.TYPE_ID_DEFAULT);
		}
		replaceFrag(R.id.fl_container, GiftDetailFragment.newInstance(mId), true);
	}

	public void setOnShareListener(OnShareListener shareClickListener) {
		mOnShareListener = shareClickListener;
	}

	public void showLimitTag(boolean isShow, @DrawableRes int bgId){
		if (ivLimitTag != null) {
			ivLimitTag.setBackgroundResource(bgId);
			if (isShow) {
				ivLimitTag.setVisibility(View.VISIBLE);
			} else {
				ivLimitTag.setVisibility(View.GONE);
			}
		}
	}
	public void showShareButton(boolean isShow) {
		if (ivShare != null) {
			if (isShow) {
				ivShare.setVisibility(View.VISIBLE);
			} else {
				ivShare.setVisibility(View.GONE);
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (AppDebugConfig.IS_DEBUG) {
			KLog.d("requestCode = " + requestCode + ", resultCode = " + resultCode + ", data = " + data);
		}
		if (requestCode == ShareSDKConfig.SHARE_REQUEST_CODE) {
			switch (resultCode) {
				case Activity.RESULT_OK:
					ScoreManager.getInstance().reward(ScoreManager.RewardType.NOTHING);
					break;
				case Activity.RESULT_CANCELED:
					break;
				default:
					ToastUtil.showShort("分享失败");
			}
		}
	}

	@Override
	public void handleBackPressed() {
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
