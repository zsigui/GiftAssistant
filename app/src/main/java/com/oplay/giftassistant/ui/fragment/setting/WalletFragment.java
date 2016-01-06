package com.oplay.giftassistant.ui.fragment.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.KeyConfig;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_FullScreen;
import com.oplay.giftassistant.util.ToastUtil;

/**
 * Created by zsigui on 16-1-6.
 */
public class WalletFragment extends BaseFragment_FullScreen {

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
		/* // 测试先忽略登录判断
		if (AccountManager.getInstance().isLogin()) {
			ToastUtil.showShort("跳转异常,请先返回登录");
			if (AppDebugConfig.IS_FRAG_DEBUG) {
				KLog.e(AppDebugConfig.TAG_FRAG, "no login to into WalletFragment!");
			}
			return;
		}*/
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
		setTitleBar(R.string.st_wallet_title);
		/*UserModel user = AccountManager.getInstance().getUser();
		tvScore.setText(user.score);
		tvBean.setText(user.bean);*/
	}

	@Override
	protected void lazyLoad() {

	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.rl_bean_detail:
				((BaseAppCompatActivity)getActivity()).replaceFrag(R.id.fl_container,
						MoneyDetailFragment.newInstance(KeyConfig.TYPE_ID_DETAIL_BEAN));
				break;
			case R.id.rl_score_detail:
				((BaseAppCompatActivity)getActivity()).replaceFrag(R.id.fl_container,
						MoneyDetailFragment.newInstance(KeyConfig.TYPE_ID_DETAIL_SCORE));
				break;
			case R.id.btn_get_bean:
				ToastUtil.showShort("充值偶玩豆");
				break;
			case R.id.btn_get_score:
				((BaseAppCompatActivity)getActivity()).replaceFrag(R.id.fl_container,
						TaskFragment.newInstance());
				break;
		}
	}
}
