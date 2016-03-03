package com.oplay.giftcool.ui.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.listener.OnShareListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.setting.DownloadFragment;
import com.oplay.giftcool.ui.fragment.setting.FeedBackFragment;
import com.oplay.giftcool.ui.fragment.setting.MyGiftFragment;
import com.oplay.giftcool.ui.fragment.setting.SetNickFragment;
import com.oplay.giftcool.ui.fragment.setting.SettingFragment;
import com.oplay.giftcool.ui.fragment.setting.TaskFragment;
import com.oplay.giftcool.ui.fragment.setting.UploadAvatarFragment;
import com.oplay.giftcool.ui.fragment.setting.UserInfoFragment;
import com.oplay.giftcool.ui.fragment.setting.WalletFragment;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

/**
 * Created by zsigui on 16-1-5.
 */
public class SettingActivity extends BaseAppCompatActivity implements ObserverManager.UserUpdateListener {

	private int mType;
	private boolean mInSetting = false;
	private OnShareListener mSaveListener;
	private TextView btnToolRight;

	@Override
	protected void initView() {
		setContentView(R.layout.activity_common_with_back);
	}

	@Override
	protected void processLogic() {
		handleRedirect(getIntent());
		ObserverManager.getInstance().addUserUpdateListener(this);
	}

	@Override
	protected void initMenu(@NonNull Toolbar toolbar) {
		super.initMenu(toolbar);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ObserverManager.getInstance().removeUserUpdateListener(this);
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

	private void handleRedirect(Intent intent) {

		if (intent == null) {
			ToastUtil.showShort("跳转出错");
			if (AppDebugConfig.IS_FRAG_DEBUG) {
				KLog.d(AppDebugConfig.TAG_FRAG, "no intent");
			}
			return;
		}
		mType = intent.getIntExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_DEFAULT);
		if (mType == KeyConfig.TYPE_ID_DEFAULT) {
			if (AppDebugConfig.IS_FRAG_DEBUG) {
				KLog.d(AppDebugConfig.TAG_FRAG, "no type");
			}
			ToastUtil.showShort("跳转出错");
			return;
		}
		mInSetting = false;
		switch (mType) {
			case KeyConfig.TYPE_ID_SETTING:
				mInSetting = true;
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
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.btn_bar_save:
				if (mSaveListener != null) {
					mSaveListener.share();
				}
				break;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		KLog.e("setting", "onNewIntent is call");
		super.onNewIntent(intent);
		handleRedirect(intent);
	}

	@Override
	public void onUserUpdate(int action) {
		if (mInSetting) {
			return;
		}
		if (!AccountManager.getInstance().isLogin()) {
			ToastUtil.showShort(getResources().getString(R.string.st_hint_un_login));
			IntentUtil.jumpLogin(this);
			finish();
		}
	}
}
