package com.oplay.giftassistant.ui.activity;

import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

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
		handleRedirect();
	}

	@Override
	protected void processLogic() {
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

		setContentView(R.layout.activity_fullscreen);
		switch (mType){
			case KeyConfig.TYPE_ID_SETTING:
				replaceFrag(R.id.fl_container, SettingFragment.newInstance());
				break;
			case KeyConfig.TYPE_ID_WALLET:
				replaceFrag(R.id.fl_container, WalletFragment.newInstance());
				break;
			case KeyConfig.TYPE_ID_DOWNLOAD:
				replaceFrag(R.id.fl_container, DownloadFragment.newInstance());
				break;
			case KeyConfig.TYPE_ID_SCORE_TASK:
				replaceFrag(R.id.fl_container, TaskFragment.newInstance());
				break;
			case KeyConfig.TYPE_ID_MY_GIFT_CODE:
				setContentView(R.layout.activity_common_with_back);
				replaceFrag(R.id.fl_container, MyGiftFragment.newInstance());
				break;
			default:
				if (AppDebugConfig.IS_FRAG_DEBUG) {
					KLog.d(AppDebugConfig.TAG_FRAG, "type = " + mType);
				}
				ToastUtil.showShort("跳转出错");
		}
	}


	@Override
	protected void initMenu(@NonNull Toolbar toolbar) {
		super.initMenu(toolbar);
		if (mType == KeyConfig.TYPE_ID_MY_GIFT_CODE) {
			setBarTitle(R.string.st_my_gift_title);
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
