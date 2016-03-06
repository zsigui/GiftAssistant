package com.oplay.giftcool.ui.fragment.search;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.DialogManager;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.IntentUtil;

/**
 * Created by zsigui on 15-12-22.
 */
public class EmptySearchFragment extends BaseFragment {

	private final String PAGE_NAME = "空搜索页";

	private String mName;
	private int mId;
	private ImageView btnHopeGift;


	public static EmptySearchFragment newInstance() {
		return new EmptySearchFragment();
	}

	public static EmptySearchFragment newInstance(String name, int id) {
		EmptySearchFragment fragment = new EmptySearchFragment();
		Bundle bundle = new Bundle();
		bundle.putString(KeyConfig.KEY_NAME, name);
		bundle.putInt(KeyConfig.KEY_DATA, id);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_empty_search);
		btnHopeGift = getViewById(R.id.btn_hope_gift);
	}

	@Override
	protected void setListener() {
		btnHopeGift.setOnClickListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		if (getArguments() != null) {
			Bundle bundle = getArguments();
			mName = bundle.getString(KeyConfig.KEY_NAME);
			mId = bundle.getInt(KeyConfig.KEY_DATA, 0);
		}
	}

	@Override
	protected void lazyLoad() {

	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.btn_hope_gift:
				// 弹出提示窗
				if (!AccountManager.getInstance().isLogin()) {
					IntentUtil.jumpLogin(getContext());
					return;
				}
				DialogManager.getInstance().showHopeGift(getChildFragmentManager(), mId, mName, mId == 0);
				break;
		}
	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}
}
