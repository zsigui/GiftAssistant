package com.oplay.giftcool.ui.activity;

import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.config.util.PostTypeUtil;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.postbar.PostListFragment;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

/**
 * Created by zsigui on 16-4-13.
 */
public class PostListActivity extends BaseAppCompatActivity {

	private int mType = 0;

	@Override
	protected void processLogic() {
		KLog.d(AppDebugConfig.TAG_WARN, "processLogic");
		if (getIntent() == null) {
			ToastUtil.showShort(ConstString.TEXT_ENTER_ERROR);
			finish();
			return;
		}
		mType = getIntent().getIntExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_DEFAULT);
		switch (mType) {
			case KeyConfig.TYPE_ID_POST_OFFICIAL:
				KLog.d(AppDebugConfig.TAG_WARN, "官方活动页面");
				replaceFragWithTitle(R.id.fl_container,
						PostListFragment.newInstance(PostTypeUtil.TYPE_CONTENT_OFFICIAL, NetUrl.POST_GET_LIST),
						"官方活动");
				break;
		}
	}

	@Override
	protected void initView() {
		setContentView(R.layout.activity_common_with_back);
	}

	@Override
	protected void initMenu(@NonNull Toolbar toolbar) {
		super.initMenu(toolbar);
		switch (mType) {
			case KeyConfig.TYPE_ID_POST_OFFICIAL:
				setBarTitle("官方活动");
				break;
		}

	}
}
