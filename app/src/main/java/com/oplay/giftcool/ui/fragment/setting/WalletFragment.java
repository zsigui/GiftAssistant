package com.oplay.giftcool.ui.fragment.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.OuwanSDKManager;
import com.oplay.giftcool.model.data.resp.UserInfo;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

/**
 * Created by zsigui on 16-1-6.
 */
public class WalletFragment extends BaseFragment {

	private TextView tvScore;
	private TextView tvBean;
	private TextView btnGetScore;
	private TextView btnGetBean;
	private RelativeLayout rlBeanDetail;
	private RelativeLayout rlScoreDetail;


	public static WalletFragment newInstance(){
		return new WalletFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		if (!AccountManager.getInstance().isLogin()) {
			ToastUtil.showShort("跳转异常,请先返回登录");
			if (AppDebugConfig.IS_FRAG_DEBUG) {
				KLog.e(AppDebugConfig.TAG_FRAG, "no login to into WalletFragment!");
			}
			return;
		}
		setContentView(R.layout.fragment_wallet);
		tvScore = getViewById(R.id.tv_score);
		tvBean = getViewById(R.id.tv_bean);
		btnGetScore = getViewById(R.id.btn_get_score);
		btnGetBean = getViewById(R.id.btn_get_bean);
		rlBeanDetail = getViewById(R.id.rl_bean_detail);
		rlScoreDetail = getViewById(R.id.rl_score_detail);
	}

	@Override
	protected void setListener() {
		tvScore.setOnClickListener(this);
		tvBean.setOnClickListener(this);
		btnGetScore.setOnClickListener(this);
		btnGetBean.setOnClickListener(this);
		rlBeanDetail.setOnClickListener(this);
		rlScoreDetail.setOnClickListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		UserInfo user = AccountManager.getInstance().getUserInfo();
		tvScore.setText(String.valueOf(user.score));
		tvBean.setText(String.valueOf(user.bean));
	}

	@Override
	protected void lazyLoad() {
		AccountManager.getInstance().updateUserInfo();
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.rl_bean_detail:
				((BaseAppCompatActivity)getActivity()).replaceFragWithTitle(R.id.fl_container,
						MoneyDetailFragment.newInstance(KeyConfig.TYPE_ID_DETAIL_BEAN),
						getResources().getString(R.string.st_bean_detail));
				break;
			case R.id.rl_score_detail:
				((BaseAppCompatActivity)getActivity()).replaceFragWithTitle(R.id.fl_container,
						MoneyDetailFragment.newInstance(KeyConfig.TYPE_ID_DETAIL_SCORE),
						getResources().getString(R.string.st_score_detail));
				break;
			case R.id.btn_get_bean:
				OuwanSDKManager.getInstance().recharge();
				break;
			case R.id.btn_get_score:
				((BaseAppCompatActivity)getActivity()).replaceFragWithTitle(R.id.fl_container,
						TaskFragment.newInstance(), getResources().getString(R.string.st_task_title));
				break;
		}
	}

	@Override
	public void onUserUpdate() {
		super.onUserUpdate();
		try {
			if (AccountManager.getInstance().isLogin()) {
				if (tvBean != null) {
					tvBean.setText(AccountManager.getInstance().getUserInfo().bean);
				}
				if (tvScore != null) {
					tvScore.setText(AccountManager.getInstance().getUserInfo().score);
				}
			}
		}catch (Throwable e) {
			if(AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
	}
}
