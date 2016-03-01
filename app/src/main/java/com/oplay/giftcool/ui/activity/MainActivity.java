package com.oplay.giftcool.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.model.data.resp.UpdateInfo;
import com.oplay.giftcool.model.data.resp.UserInfo;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.DrawerFragment;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.ui.fragment.dialog.AllViewDialog;
import com.oplay.giftcool.ui.fragment.dialog.WelcomeDialog;
import com.oplay.giftcool.ui.fragment.game.GameFragment;
import com.oplay.giftcool.ui.fragment.gift.GiftFragment;
import com.oplay.giftcool.ui.widget.search.SearchLayout;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.oplay.giftcool.util.ViewUtil;

import java.io.File;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class MainActivity extends BaseAppCompatActivity implements ObserverManager.UserUpdateListener {

	private static final String TAG_EXIT = "com.oplay.giftcool.MainActivity.exit";
	// 保持一个Activity的全局对象
	public static MainActivity sGlobalHolder;
	// 判断是否今日首次打开APP
	public static boolean sIsTodayFirstOpen = false;
	public static boolean sIsTodayFirstOpenForBroadcast = false;
	private long mLastClickTime = 0;

	//是否已经显示过更新提示
	private boolean mHasShowUpdate = false;
	// 底部Tabs
	private LinearLayout llTab;
	private CheckedTextView[] mCtvs;

	private ImageView ivProfile;
	private ImageView ivHint;
	private TextView tvGiftCount;
	// 礼物Fragment
	private GiftFragment mGiftFragment;
	private GameFragment mGameFragment;
	// 当前选项卡下标
	private int mCurrentIndex = 0;
	private DrawerLayout mDrawerLayout;
	private DrawerFragment mDrawerFragment;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sGlobalHolder = MainActivity.this;
		createDrawer();
		updateToolBar();
	}

	private void createDrawer() {
		mDrawerLayout = getViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerListener(new ActionBarDrawerToggle(this, mDrawerLayout,
				R.string.st_drawer_open, R.string.st_drawer_close));
		mDrawerFragment = DrawerFragment.newInstance(mDrawerLayout);
		replaceFrag(R.id.drawer_container, mDrawerFragment, false);
	}



	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	protected void initView() {
		setContentView(R.layout.activity_main);
		llTab = getViewById(R.id.ll_tabs);
		CheckedTextView ctvGame = getViewById(R.id.ctv_game);
		CheckedTextView ctvGift = getViewById(R.id.ctv_gift);
		mCtvs = new CheckedTextView[2];
		mCtvs[0] = ctvGift;
		mCtvs[1] = ctvGame;
		if (!AssistantApp.getInstance().isAllowDownload()) {
			llTab.setVisibility(View.GONE);
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
		ivHint = getViewById(R.id.iv_hint);
		getViewById(R.id.ll_gift_count).setOnClickListener(this);
	}

	private void updateToolBar() {
		if (AccountManager.getInstance().isLogin()) {
			if (AccountManager.getInstance().getUserInfo().isCompleteTodayMission) {
				updateHintState(KeyConfig.TYPE_ID_SCORE_TASK, 0);
			} else {
				updateHintState(KeyConfig.TYPE_ID_SCORE_TASK, -1);
			}
			UserInfo user = AccountManager.getInstance().getUserInfo();
			tvGiftCount.setText(String.valueOf(user.giftCount));
			ViewUtil.showAvatarImage(user.avatar, ivProfile, true);
		} else {
			updateHintState(KeyConfig.TYPE_ID_SCORE_TASK, 0);
			ivProfile.setImageResource(R.drawable.ic_avator_unlogin);
			ivProfile.setTag("");
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
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent != null) {
			boolean isExit = intent.getBooleanExtra(TAG_EXIT, false);
			if (isExit) {
				finish();
				System.exit(0);
			}
		}
	}

	@Override
	public void onBackPressed() {

		if (System.currentTimeMillis() - mLastClickTime <= 1000) {
			ObserverManager.getInstance().removeUserUpdateListener(this);
			mApp.exit();
			// 发送退出指令
			Intent intent = new Intent(this, MainActivity.class);
			intent.putExtra(TAG_EXIT, true);
			startActivity(intent);
		} else {
			mLastClickTime = System.currentTimeMillis();
			ToastUtil.showShort("再次点击退出应用");
		}
	}

	public void jumpToIndexGame(int gamePosition) {
		setCurSelected(1);
		if(mGameFragment != null) {
			mGameFragment.setPagePosition(gamePosition);
		}
	}

	public void jumpToIndexGift(final int giftPosition) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {

				setCurSelected(0);
				if (mGiftFragment != null) {
					mGiftFragment.scrollToPos(giftPosition);
				}
			}
		});
	}

	@Override
	public void onUserUpdate(int action) {
		if (action == ObserverManager.STATUS.USER_UPDATE_ALL) {
			updateToolBar();
		}
	}


	private SparseIntArray mHintCount = new SparseIntArray();

	/**
	 * 更新侧边栏和顶部导航栏信息
	 */
	public void updateHintState(int key, int count) {
		if (count < -1 || mDrawerFragment == null) {
			return;
		}
		int val = mHintCount.get(key);
		if (val == 0) {
			// 原先不存在该键值
			if (count != 0) {
				mHintCount.put(key, count);
				mDrawerFragment.updateCount(key, count);
				ivHint.setVisibility(View.VISIBLE);
				mDrawerFragment.updateCount(key, count);
			}
		} else {
			// 原先已经存在该键值
			if (count == 0) {
				// 移除该键值
				mHintCount.delete(key);
				if (mHintCount.size() > 0) {
					ivHint.setVisibility(View.VISIBLE);
				} else {
					ivHint.setVisibility(View.GONE);
				}
			} else {
				mHintCount.put(key, count);
				ivHint.setVisibility(View.VISIBLE);
			}
			mDrawerFragment.updateCount(key, count);
		}
	}

	private void handleFirstOpen() {
		if (sIsTodayFirstOpenForBroadcast && AssistantApp.getInstance().getBroadcastBanner() != null
				&& hasLoadPic() && mHandler != null) {
			sIsTodayFirstOpenForBroadcast = false;
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					AllViewDialog dialog = AllViewDialog.newInstance(AssistantApp.getInstance().getBroadcastBanner());
					dialog.show(getSupportFragmentManager(), "broadcast");
				}
			}, 1000);

		}
//        if (sIsTodayFirstOpen && mHandler != null) {
//				sIsTodayFirstOpen = false;
//			// 防止在调用onSaveInstanceState时触发导致崩溃，延迟触发
//			mHandler.postDelayed(new Runnable() {
//				@Override
//				public void run() {
//					if (AccountManager.getInstance().isLogin()) {
//						ScoreManager.getInstance().showWelComeDialog(getSupportFragmentManager(), MainActivity.this,
//								AccountManager.getInstance().getUser());
//					} else {
//						ScoreManager.getInstance().showWelComeDialog(getSupportFragmentManager(), MainActivity.this,
//								null);
//					}
//				}
//			}, 3000);
//		}
	}

	private boolean hasLoadPic() {
		boolean hasLoad = false;
		String imageUrl = AssistantApp.getInstance().getBroadcastBanner().url;
		if (imageUrl != null) {
			File file = ImageLoader.getInstance().getDiskCache().get(imageUrl);
			if (file != null && file.exists()) {
				hasLoad = true;
			} else {
				ImageLoader.getInstance().loadImage(imageUrl, null);
			}
		}
		return hasLoad;
	}

	/**
	 * 处理更新逻辑
	 * @return　是否有更新
	 */
	private boolean handleUpdateApp() {

		final UpdateInfo updateInfo = mApp.getUpdateInfo();
		if (!mHasShowUpdate && updateInfo != null && updateInfo.checkoutUpdateInfo(this)
				&& mHandler != null) {
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
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					BaseFragment_Dialog confirmDialog = getUpdateDialog(appInfo, updateInfo.content);
					confirmDialog.show(getSupportFragmentManager(), "update");
				}
			}, 1000);
			return true;
		}
		return false;
	}

	@Override
	public void release() {
		super.release();
		// 保持一个Activity的全局对象
		sGlobalHolder = null;
		// 底部Tabs
		 llTab = null;
		mCtvs = null;
		ivProfile = null;
		tvGiftCount = null;
		// 礼物Fragment
		mGiftFragment = null;
		mGameFragment = null;
		mDrawerLayout = null;
		mDrawerFragment = null;
	}

	private WelcomeDialog getUpdateDialog(final IndexGameNew appInfo, final String content) {
		final WelcomeDialog confirmDialog = WelcomeDialog.newInstance(R.layout.dialog_welcome_update);
		confirmDialog.setTitle(content);
		confirmDialog.setPositiveBtnText(getResources().getString(R.string.st_welcome_update_confirm));
		confirmDialog.setNegativeBtnText(getResources().getString(R.string.st_welcome_update_cancel));
		confirmDialog.setListener(new BaseFragment_Dialog.OnDialogClickListener() {
			@Override
			public void onCancel() {
				AppDebugConfig.trace(mApp, "更新弹窗", "取消更新");
				confirmDialog.dismiss();
				handleFirstOpen();
			}

			@Override
			public void onConfirm() {
				AppDebugConfig.trace(mApp, "更新弹窗", "点击更新");
				appInfo.startDownload();
				confirmDialog.dismiss();
				handleFirstOpen();
			}
		});
		return confirmDialog;
	}
}
