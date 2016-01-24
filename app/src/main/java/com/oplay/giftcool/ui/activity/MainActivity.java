package com.oplay.giftcool.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.model.data.resp.UpdateInfo;
import com.oplay.giftcool.model.data.resp.UserInfo;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.DrawerFragment;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.ui.fragment.dialog.ConfirmDialog;
import com.oplay.giftcool.ui.fragment.game.GameFragment;
import com.oplay.giftcool.ui.fragment.gift.GiftFragment;
import com.oplay.giftcool.ui.widget.search.SearchLayout;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.oplay.giftcool.util.ViewUtil;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class MainActivity extends BaseAppCompatActivity implements ObserverManager.UserUpdateListener {

	// 保持一个Activity的全局对象
	public static MainActivity sGlobalHolder;
	// 判断是否今日首次打开APP
	public static boolean sIsTodayFirstOpen = false;
	private long mLastClickTime = 0;

	//是否已经显示过更新提示
	private boolean mHasShowUpdate = false;
	// 底部Tabs
	private CheckedTextView[] mCtvs;
	private ImageView ivProfile;
	private TextView tvGiftCount;
	// 礼物Fragment
	private GiftFragment mGiftFragment;
	private GameFragment mGameFragment;
	// 当前选项卡下标
	private int mCurrentIndex = 0;

	private UserInfo mUser;
	// 侧边栏
	/*private Drawer mDrawer;
	private AccountHeader mDrawerHeader;
	private IProfile mProfile;*/
	private DrawerLayout mDrawerLayout;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sGlobalHolder = MainActivity.this;
		createDrawer();
	}

	private void createDrawer() {
		mDrawerLayout = getViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerListener(new ActionBarDrawerToggle(this, mDrawerLayout,
				R.string.st_drawer_open, R.string.st_drawer_close));
		replaceFrag(R.id.drawer_container, DrawerFragment.newInstance(mDrawerLayout), false);
	}



	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	protected void initView() {
		setContentView(R.layout.activity_main);
		CheckedTextView ctvGame = getViewById(R.id.ctv_game);
		CheckedTextView ctvGift = getViewById(R.id.ctv_gift);
		mCtvs = new CheckedTextView[2];
		mCtvs[0] = ctvGift;
		mCtvs[1] = ctvGame;
		if (!AssistantApp.getInstance().isAllowDownload()) {
			ctvGame.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}


	@Override
	protected void initMenu(@NonNull Toolbar toolbar) {
		super.initMenu(toolbar);
		SearchLayout searchLayout = getViewById(toolbar, R.id.sl_search);
		searchLayout.setCanGetFocus(false);
		searchLayout.setOnClickListener(this);
		ivProfile = getViewById(toolbar, R.id.iv_profile);
		ivProfile.setOnClickListener(this);
		tvGiftCount = getViewById(toolbar, R.id.tv_gift_count);
		getViewById(R.id.ll_gift_count).setOnClickListener(this);
		updateToolBar();
	}

	private void updateToolBar() {
		if (AccountManager.getInstance().isLogin()) {
			mUser = AccountManager.getInstance().getUserInfo();
			tvGiftCount.setText(String.valueOf(mUser.giftCount));
			if (TextUtils.isEmpty(mUser.avatar)) {
				ivProfile.setImageResource(R.drawable.ic_avatar_default);
			} else {
				ViewUtil.showAvatarImage(mUser.avatar, ivProfile, AccountManager.getInstance().isLogin());
			}
		} else {
			ivProfile.setImageResource(R.drawable.ic_avator_unlogin);
			tvGiftCount.setText("0");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		//没更新才显示欢迎
		if (!handleUpdateApp()) {
			handleFirstOpen();
		}
	}

	@Override
	protected void processLogic() {
		ObserverManager.getInstance().addUserUpdateListener(this);

		for (CheckedTextView ctv : mCtvs) {
			ctv.setOnClickListener(this);
		}

		// 加载数据在父类进行，初始先显示加载页面，同时起到占位作用
		setCurSelected(mCurrentIndex);

	}

	public void setCurSelected(int position) {
		for (CheckedTextView ctv : mCtvs) {
			ctv.setChecked(false);
			ctv.setTextColor(getResources().getColor(R.color.co_tab_index_text_normal));
		}
		mCurrentIndex = position;
		mCtvs[position].setChecked(true);
		mCtvs[position].setTextColor(getResources().getColor(R.color.co_tab_index_text_selected));
		if (position == 0) {
			displayGiftUI();
		} else {
			displayGameUI();
		}
	}

	private void displayGameUI() {
		if (mGameFragment == null) {
			mGameFragment = GameFragment.newInstance();
		}
		reshowFrag(R.id.fl_container, mGameFragment, mGameFragment.getClass().getSimpleName(),
				GiftFragment.class.getSimpleName());
	}

	private void displayGiftUI() {
		if (mGiftFragment == null) {
			mGiftFragment = GiftFragment.newInstance();
		}
		reshowFrag(R.id.fl_container, mGiftFragment, mGiftFragment.getClass().getSimpleName(),
				GameFragment.class.getSimpleName());
	}


	@Override
	public void onClick(View v) {
		super.onClick(v);
		for (int i = 0; i < mCtvs.length; i++) {
			if (v.getId() == mCtvs[i].getId()) {
				setCurSelected(i);
				return;
			}
		}
		switch (v.getId()) {
			case R.id.ctv_gift:
				setCurSelected(0);
				break;
			case R.id.ctv_game:
				setCurSelected(1);
				break;
			case R.id.sl_search:
				IntentUtil.jumpSearch(MainActivity.this);
				break;
			case R.id.iv_profile:
				if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
					mDrawerLayout.closeDrawer(GravityCompat.START);
				} else {
					mDrawerLayout.openDrawer(GravityCompat.START);
				}
				break;
			case R.id.ll_gift_count:
				if (AccountManager.getInstance().isLogin()) {
					IntentUtil.jumpMyGift(MainActivity.this);
				} else {
					IntentUtil.jumpLogin(MainActivity.this);
				}
				break;
		}
	}

	@Override
	public void onBackPressed() {

		if (System.currentTimeMillis() - mLastClickTime <= 1000) {
			mApp.exit();
			finish();
			System.exit(0);
		} else {
			mLastClickTime = System.currentTimeMillis();
			ToastUtil.showShort("再次点击退出应用");
		}
	}

	@Override
	public void onUserUpdate() {
		mUser = AccountManager.getInstance().getUserInfo();
		updateToolBar();
		handleFirstOpen();
	}

	private void handleFirstOpen() {
		if (sIsTodayFirstOpen) {
			// 防止在调用onSaveInstanceState时触发导致崩溃，延迟触发
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					if (AccountManager.getInstance().isLogin()) {
						ScoreManager.getInstance().showWelComeDialog(getSupportFragmentManager(), MainActivity.this,
								AccountManager.getInstance().getUser());
					} else {
						ScoreManager.getInstance().showWelComeDialog(getSupportFragmentManager(), MainActivity.this, null);
					}
					sIsTodayFirstOpen = false;
				}
			}, 1000);
		}
	}

	/**
	 * 处理更新逻辑
	 * @return　是否有更新
	 */
	private boolean handleUpdateApp() {

		final UpdateInfo updateInfo = mApp.getUpdateInfo();
		if (updateInfo != null && updateInfo.checkoutUpdateInfo(this)) {
			mHasShowUpdate = true;
			final IndexGameNew appInfo = new IndexGameNew();
			appInfo.id = Global.GIFTCOOL_GAME_ID;
			appInfo.name = getString(R.string.app_name);
			appInfo.apkFileSize = updateInfo.apkFileSize;
			//没icon地址，随便填个
			appInfo.img = updateInfo.downloadUrl;
			appInfo.downloadUrl = updateInfo.downloadUrl;
			appInfo.destUrl = updateInfo.downloadUrl;
			appInfo.packageName = updateInfo.packageName;
			appInfo.versionName = updateInfo.versionName;
			appInfo.size = appInfo.getApkFileSizeStr();
			appInfo.initAppInfoStatus(this);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					ConfirmDialog confirmDialog = getUpdateDialog(appInfo, updateInfo.content);
					confirmDialog.show(getSupportFragmentManager(), "update");
				}
			}, 1000);
			return true;
		}
		return false;
	}

	private ConfirmDialog getUpdateDialog(final IndexGameNew appInfo, final String content) {
		final ConfirmDialog confirmDialog = ConfirmDialog.newInstance();
		confirmDialog.setTitle("更新提示");
		confirmDialog.setContent(content);
		confirmDialog.setPositiveBtnText("马上更新");
		confirmDialog.setNegativeBtnText("暂不更新");
		confirmDialog.setListener(new BaseFragment_Dialog.OnDialogClickListener() {
			@Override
			public void onCancel() {
				confirmDialog.dismiss();
				handleFirstOpen();
			}

			@Override
			public void onConfirm() {
				appInfo.startDownload();
				confirmDialog.dismiss();
				handleFirstOpen();
			}
		});
		return confirmDialog;
	}
}
