package com.oplay.giftcool.ui.fragment.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.model.DecryptDataModel;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;

/**
 * Created by zsigui on 16-1-6.
 */
public class AboutFragment extends BaseFragment {

	private final static String PAGE_NAME = "关于";
	private RelativeLayout rlUpdate;
	private RelativeLayout rlQQ;
	private TextView tvUpdate;
	private TextView tvQQ;
	private TextView tvVersion;

	public static AboutFragment newInstance() {
		return new AboutFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_about);
		rlUpdate = getViewById(R.id.rl_update);
		rlQQ = getViewById(R.id.rl_qq);
		tvUpdate = getViewById(R.id.tv_update);
		tvQQ = getViewById(R.id.tv_qq);
		tvVersion = getViewById(R.id.tv_version);
	}

	@Override
	protected void setListener() {
		rlUpdate.setOnClickListener(this);
		rlQQ.setOnClickListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		tvUpdate.setText(String.format(getResources().getString(R.string.st_about_wait_update_text), "V2.0"));
		tvVersion.setText("礼包酷 " + DecryptDataModel.SDK_VER_NAME);
		tvQQ.setText("515318514");
	}

	@Override
	protected void lazyLoad() {

	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.rl_update:
				break;
			case R.id.rl_qq:
				break;
		}
	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}
}
