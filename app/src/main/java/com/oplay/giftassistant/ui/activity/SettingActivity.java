package com.oplay.giftassistant.ui.activity;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.KeyConfig;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.setting.DownloadFragment;
import com.oplay.giftassistant.ui.fragment.setting.FeedBackFragment;
import com.oplay.giftassistant.ui.fragment.setting.MyGiftFragment;
import com.oplay.giftassistant.ui.fragment.setting.SettingFragment;
import com.oplay.giftassistant.ui.fragment.setting.TaskFragment;
import com.oplay.giftassistant.ui.fragment.setting.WalletFragment;
import com.oplay.giftassistant.util.ToastUtil;
import com.socks.library.KLog;

/**
 * Created by zsigui on 16-1-5.
 */
public class SettingActivity extends BaseAppCompatActivity {

	private int mType;

	@Override
	protected void initView() {
		setContentView(R.layout.activity_common_with_back);
	}

	@Override
	protected void processLogic() {
		handleRedirect();
	}

	private void handleRedirect() {

		if (getIntent() == null) {
			ToastUtil.showShort("跳转出错");
			if (AppDebugConfig.IS_FRAG_DEBUG) {
				KLog.d(AppDebugConfig.TAG_FRAG, "no intent");
			}
			return;
		}
		mType = getIntent().getIntExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_DEFAULT);
		if (mType == KeyConfig.TYPE_ID_DEFAULT) {
			if (AppDebugConfig.IS_FRAG_DEBUG) {
				KLog.d(AppDebugConfig.TAG_FRAG, "no type");
			}
			ToastUtil.showShort("跳转出错");
			return;
		}

		switch (mType) {
			case KeyConfig.TYPE_ID_SETTING:
				replaceFragWithTitle(R.id.fl_container, SettingFragment.newInstance(), getResources().getString(R
						.string.st_setting_title));
				break;
			case KeyConfig.TYPE_ID_WALLET:
				replaceFragWithTitle(R.id.fl_container, WalletFragment.newInstance(), getResources().getString(R
						.string.st_wallet_title));
				break;
			case KeyConfig.TYPE_ID_DOWNLOAD:
				replaceFragWithTitle(R.id.fl_container, DownloadFragment.newInstance(),
						getResources().getString(R.string.st_download_title));
				break;
			case KeyConfig.TYPE_ID_SCORE_TASK:
				replaceFragWithTitle(R.id.fl_container, TaskFragment.newInstance(), getResources().getString(R.string
						.st_task_title));
				break;
			case KeyConfig.TYPE_ID_MY_GIFT_CODE:
				replaceFragWithTitle(R.id.fl_container, MyGiftFragment.newInstance(),
						getResources().getString(R.string.st_my_gift_title));
				break;
			default:
				if (AppDebugConfig.IS_FRAG_DEBUG) {
					KLog.d(AppDebugConfig.TAG_FRAG, "type = " + mType);
				}
				ToastUtil.showShort("跳转出错");
		}
	}


	@Override
	public void popOrExit() {
		if (isTopFragment(FeedBackFragment.class.getSimpleName())) {
			ToastUtil.showShort("反馈返回，执行保存处理");
			if (getTopFragment() instanceof FeedBackFragment) {
				KLog.d("is FeedBack");
			}
		}
		super.popOrExit();
	}
}
