package com.oplay.giftcool.ui.activity;

import android.content.Intent;
import android.view.ViewStub;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.listener.OnShareListener;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.ActivityFragment;
import com.oplay.giftcool.util.ToastUtil;

/**
 * Created by zsigui on 16-1-29.
 */
public class WebActivity extends BaseAppCompatActivity {

	private OnShareListener mSaveListener;
	private TextView btnToolRight;
	private ActivityFragment mContentFragment;

	@Override
	protected void initView() {
		setContentView(R.layout.activity_common_with_back);
	}

	@Override
	protected void processLogic() {
		if (getIntent() == null) {
			ToastUtil.showShort("参数获取失败，请重新进入");
			finish();
			return;
		}

		handleRedirect(getIntent());
	}

	private void handleRedirect(Intent intent) {
		String url = getIntent().getStringExtra(KeyConfig.KEY_URL);
		String title = getIntent().getStringExtra(KeyConfig.KEY_DATA);
		int type = getIntent().getIntExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_DEFAULT);
	}

	public void reload(String url) {
		if (mContentFragment == null) {
			mContentFragment = ActivityFragment.newInstance(url);
			replaceFragWithTitle(R.id.fl_container, ActivityFragment.newInstance(url), "");
		}
	}

	public void showRightBtn(int visibility, String text) {
		if (mToolbar == null)
			return;
		if (btnToolRight == null) {
			ViewStub v = getViewById(mToolbar, R.id.vs_save);
			if (v != null) {
				v.inflate();
				btnToolRight = getViewById(R.id.btn_bar_save);
				btnToolRight.setOnClickListener(this);
				btnToolRight.setText(text);
			}
		}
		if (btnToolRight != null) {
			btnToolRight.setVisibility(visibility);
		}
	}

	public void setRightBtnEnabled(boolean enabled) {
		if (mToolbar == null)
			return;
		if (btnToolRight == null) {
			ViewStub v = getViewById(mToolbar, R.id.vs_save);
			if (v != null) {
				v.inflate();
				btnToolRight = getViewById(R.id.btn_bar_save);
				btnToolRight.setOnClickListener(this);
			}
		}
		if (btnToolRight != null) {
			btnToolRight.setEnabled(enabled);
		}
	}

	public void setRightBtnListener(OnShareListener saveListener) {
		mSaveListener = saveListener;
	}
}
