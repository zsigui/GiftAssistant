package com.oplay.giftcool.ui.activity;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.setting.DownloadFragment;
import com.oplay.giftcool.ui.fragment.setting.FeedBackFragment;
import com.oplay.giftcool.ui.fragment.setting.MyGiftFragment;
import com.oplay.giftcool.ui.fragment.setting.SettingFragment;
import com.oplay.giftcool.ui.fragment.setting.TaskFragment;
import com.oplay.giftcool.ui.fragment.setting.WalletFragment;
import com.oplay.giftcool.ui.fragment.user.SetNickFragment;
import com.oplay.giftcool.ui.fragment.user.UploadAvatarFragment;
import com.oplay.giftcool.ui.fragment.user.UserInfoFragment;
import com.oplay.giftcool.util.ToastUtil;
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
			case KeyConfig.TYPE_ID_FEEDBACK:
				replaceFragWithTitle(R.id.fl_container, FeedBackFragment.newInstance(),
						getResources().getString(R.string.st_feedback_title));
				break;
			case KeyConfig.TYPE_ID_USERINFO:
				replaceFragWithTitle(R.id.fl_container, UserInfoFragment.newInstance(),
						getResources().getString(R.string.st_user_info_title));
				break;
			case KeyConfig.TYPE_ID_USER_SET_NICK:
				replaceFragWithTitle(R.id.fl_container, UploadAvatarFragment.newInstance(),
						getResources().getString(R.string.st_user_set_nick_title));
				break;
			case KeyConfig.TYPE_ID_USER_SET_AVATAR:
				replaceFragWithTitle(R.id.fl_container, SetNickFragment.newInstance(),
						getResources().getString(R.string.st_user_set_avatar_title));
				break;
			default:
				if (AppDebugConfig.IS_FRAG_DEBUG) {
					KLog.d(AppDebugConfig.TAG_FRAG, "type = " + mType);
				}
				ToastUtil.showShort("跳转出错");
		}
	}


	@Override
	public void handleBackPressed() {
		super.handleBackPressed();
	}
}