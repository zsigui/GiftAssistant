package com.oplay.giftcool.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.manager.StatisticsManager;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.model.data.resp.UpdateInfo;
import com.oplay.giftcool.model.data.resp.UserInfo;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.DrawerFragment;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.ui.fragment.dialog.AllViewDialog;
import com.oplay.giftcool.ui.fragment.dialog.WelcomeDialog;
import com.oplay.giftcool.ui.fragment.postbar.PostFragment;
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

	final String TAG_GIFT = GiftFragment.class.getSimpleName();
	final String TAG_GAME = GameFragment.class.getSimpleName();
	final String TAG_POST = PostFragment.class.getSimpleName();
	final String TAG_DRAWER = DrawerFragment.class.getSimpleName();

	final int INDEX_COUNT = 3;
	final int INDEX_DEFAULT = -1;
	// 需要按顺序 0...n
	final int INDEX_GIFT = 0;
	final int INDEX_GAME = 1;
	final int INDEX_ESSAY = 2;

	// 保持一个Activity的全局对象
	public static MainActivity sGlobalHolder;
	// 判断是否今日首次打开APP
	public static boolean sIsTodayFirstOpen = false;
	public static boolean sIsTodayFirstOpenForBroadcast = false;

	private long mLastClickTime = 0;
	private int mCurSelectedItem = INDEX_DEFAULT;

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
	private PostFragment mPostFragment;
	// 当前选项卡下标
	private DrawerLayout mDrawerLayout;
	private DrawerFragment mDrawerFragment;

	private void initHandler() {
		if (mHandler == null) {
			mHandler = new Handler(Looper.getMainLooper());
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (!AssistantApp.getInstance().isGlobalInit()) {
			AssistantApp.getInstance().appInit();
		}
		super.onCreate(savedInstanceState);

		sGlobalHolder = MainActivity.this;
		updateToolBar();
		updateHintState(KeyConfig.TYPE_ID_DOWNLOAD, ApkDownloadManager.getInstance(this).getEndOfPaused());
	}

	private void createDrawer() {
		mDrawerLayout = getViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerListener(new ActionBarDrawerToggle(this, mDrawerLayout,
				R.string.st_drawer_text_open, R.string.st_drawer_text_close));
		// 考虑onSaveInstanceState造成的叠加问题
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (mDrawerFragment == null) {
			Fragment f = getSupportFragmentManager().findFragmentByTag(DrawerFragment.class.getSimpleName());
			if (f != null) {
				mDrawerFragment = (DrawerFragment) f;
			} else {
				mDrawerFragment = DrawerFragment.newInstance(mDrawerLayout);
			}
		}
		ft.replace(R.id.drawer_container, mDrawerFragment, TAG_DRAWER);
		ft.commit();
		updateToolBar();
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		ObserverManager.getInstance().removeUserUpdateListener(this);
	}

	protected void initView() {
		setContentView(R.layout.activity_main);
		llTab = getViewById(R.id.ll_tabs);
		CheckedTextView ctvGame = getViewById(R.id.ctv_game);
		CheckedTextView ctvGift = getViewById(R.id.ctv_gift);
		CheckedTextView ctvEssay = getViewById(R.id.ctv_post);
		mCtvs = new CheckedTextView[INDEX_COUNT];
		mCtvs[INDEX_GIFT] = ctvGift;
		mCtvs[INDEX_GAME] = ctvGame;
		mCtvs[INDEX_ESSAY] = ctvEssay;
		if (!AssistantApp.getInstance().isAllowDownload()) {
			llTab.setVisibility(View.GONE);
		}
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
		initHandler();
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (AccountManager.getInstance().isLogin()) {
					if (AccountManager.getInstance().getUserInfo().isCompleteTodayMission) {
						updateHintState(KeyConfig.TYPE_ID_SIGN_IN_EVERYDAY, 1);
					} else {
						updateHintState(KeyConfig.TYPE_ID_SIGN_IN_EVERYDAY, 0);
					}
					UserInfo user = AccountManager.getInstance().getUserInfo();
					tvGiftCount.setText(String.valueOf(user.giftCount));
					ViewUtil.showAvatarImage(user.avatar, ivProfile, true);
				} else {
					updateHintState(KeyConfig.TYPE_ID_SIGN_IN_EVERYDAY, 0);
					ivProfile.setImageResource(R.drawable.ic_avator_unlogin);
					ivProfile.setTag("");
					tvGiftCount.setText("0");
				}
			}
		});
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
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void processLogic() {
		ObserverManager.getInstance().addUserUpdateListener(this);

		for (CheckedTextView ctv : mCtvs) {
			ctv.setOnClickListener(this);
		}

		// 加载数据在父类进行，初始先显示加载页面，同时起到占位作用
		setCurSelected(mCurSelectedItem);
	}

	public void setCurSelected(int position) {
		if (mCurSelectedItem == position
				&& position != INDEX_DEFAULT) {
			return;
		}
		if (position == INDEX_DEFAULT) {
			position = INDEX_GIFT;
		}
		for (CheckedTextView ctv : mCtvs) {
			ctv.setChecked(false);
			ctv.setTextColor(getResources().getColor(R.color.co_tab_index_text_normal));
		}
		mCtvs[position].setChecked(true);
		mCtvs[position].setTextColor(getResources().getColor(R.color.co_tab_index_text_selected));
		switch (position) {
			case INDEX_ESSAY:
				displayEssayUI();
				break;
			case INDEX_GAME:
				displayGameUI();
				break;
			case INDEX_GIFT:
			default:
				displayGiftUI();
				break;
		}
	}

	/**
	 * 隐藏所有的Fragment
	 */
	private void hideAllFragment(FragmentTransaction ft) {
		if (mGiftFragment != null) {
			ft.hide(mGiftFragment);
		}
		if (mGameFragment != null) {
			ft.hide(mGameFragment);
		}
		if (mPostFragment != null) {
			ft.hide(mPostFragment);
		}
	}

	/**
	 * 显示游戏界面，采用 show/hide 进行显示
	 */
	private void displayGameUI() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		hideAllFragment(ft);
		if (mGameFragment == null) {
			// Activity被回收重建后查找
			Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_GAME);
			if (f != null) {
				mGameFragment = (GameFragment) f;
				ft.show(mGameFragment);
			} else {
				// 正常新建
				mGameFragment = GameFragment.newInstance();
				ft.add(R.id.fl_container, mGameFragment, TAG_GAME);
			}
		} else {
			ft.show(mGameFragment);
		}
		ft.commit();
		mCurSelectedItem = INDEX_GAME;
	}

	/**
	 * 显示礼包界面，采用 show/hide 进行显示
	 */
	private void displayGiftUI() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		hideAllFragment(ft);
		if (mGiftFragment == null) {
			Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_GIFT);
			if (f != null) {
				mGiftFragment = (GiftFragment) f;
				ft.show(mGiftFragment);
			} else {
				mGiftFragment = GiftFragment.newInstance();
				ft.add(R.id.fl_container, mGiftFragment, TAG_GIFT);
			}
		} else {
			ft.show(mGiftFragment);
		}
		ft.commit();
		mCurSelectedItem = INDEX_GIFT;
	}

	/**
	 * 显示活动模块
	 */
	private void displayEssayUI() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		hideAllFragment(ft);
		if (mPostFragment == null) {
			// Activity被回收重建后查找
			Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_POST);
			if (f != null) {
				mPostFragment = (PostFragment) f;
				ft.show(mPostFragment);
			} else {
				// 正常新建
				mPostFragment = PostFragment.newInstance();
				ft.add(R.id.fl_container, mPostFragment, TAG_POST);
			}
		} else {
			ft.show(mPostFragment);
		}
		ft.commit();
		mCurSelectedItem = INDEX_ESSAY;
	}

	/**
	 * 该方法能保证在完成 Fragment 的唤醒之后再调用，防止在其他 Activity 调用本类 commit 之后出现 IllegalStateException
	 */
	@Override
	protected void onPostResume() {
		super.onPostResume();
		// 为了避免 commit state loss 错误，在此处执行对 Fragment 的操作
		if (mJumpGiftPos != -1) {
			setCurSelected(INDEX_GIFT);
			if (mGiftFragment != null) {
				mGiftFragment.scrollToPos(mJumpGiftPos);
				mJumpGiftPos = -1;
			}
		} else if (mJumpGamePos != -1) {
			setCurSelected(INDEX_GAME);
			if (mGameFragment != null) {
				mGameFragment.setPagePosition(mJumpGamePos);
			}
			mJumpGamePos = -1;
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		for (int pos = 0; pos < mCtvs.length; pos++) {
			if (v.getId() == mCtvs[pos].getId()) {
				setCurSelected(pos);
				return;
			}
		}
		switch (v.getId()) {
			case R.id.ctv_gift:
				setCurSelected(INDEX_GIFT);
				break;
			case R.id.ctv_game:
				setCurSelected(INDEX_GAME);
				break;
			case R.id.sl_search:
				IntentUtil.jumpSearch(MainActivity.this);
				break;
			case R.id.iv_profile:
				if (mDrawerLayout == null) {
					createDrawer();
				}
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
			mApp.appExit();
			// 发送退出指令
			finish();
//			System.exit(0);
		} else {
			mLastClickTime = System.currentTimeMillis();
			ToastUtil.showShort("再次点击退出应用");
		}
	}


	// 定义跳转定位的位置
	private int mJumpGamePos = -1;
	private int mJumpGiftPos = -1;

	public void jumpToIndexGame(int gamePosition) {
		mJumpGamePos = gamePosition;
		mJumpGiftPos = -1;
	}

	public void jumpToIndexGift(final int giftPosition) {
		mJumpGiftPos = giftPosition;
		mJumpGamePos = -1;
	}

	@Override
	public void onUserUpdate(int action) {
		switch (action) {
			case ObserverManager.STATUS.USER_UPDATE_ALL:
				updateToolBar();
				break;
			case ObserverManager.STATUS.USER_UPDATE_PUSH_MESSAGE:
				int msgCount = AccountManager.getInstance().getUnreadMessageCount();
				updateHintState(KeyConfig.TYPE_ID_MSG, msgCount);
				break;
		}
	}


	/**
	 * 侧边栏提示数组
	 */
	private SparseIntArray mHintCount = new SparseIntArray();

	/**
	 * 更新侧边栏和顶部导航栏信息
	 */
	public void updateHintState(final int key, final int count) {
		if (mHandler == null) {
			return;
		}
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				int val = mHintCount.get(key, 0);
				if (val == 0) {
					// 原先不存在该键值
					if (count != 0) {
						mHintCount.put(key, count);
						ivHint.setVisibility(View.VISIBLE);
						if (mDrawerFragment != null) {
							mDrawerFragment.updateCount(key, count);
						}
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
					if (mDrawerFragment != null) {
						mDrawerFragment.updateCount(key, count);
					}
				}
			}
		}, 300);

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

		} else if (sIsTodayFirstOpen && mHandler != null) {
			sIsTodayFirstOpen = false;
			// 防止在调用onSaveInstanceState时触发导致崩溃，延迟触发
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (AccountManager.getInstance().isLogin()) {
						ScoreManager.getInstance().showWelComeDialog(getSupportFragmentManager(), MainActivity.this,
								AccountManager.getInstance().getUser());
					}
				}
			}, 1000);
		}
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
	 *
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
					BaseFragment_Dialog confirmDialog = getUpdateDialog(appInfo, updateInfo.content,
							updateInfo.updatePercent);
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

	private WelcomeDialog getUpdateDialog(final IndexGameNew appInfo, final String content, int updatePercent) {
		final WelcomeDialog confirmDialog = WelcomeDialog.newInstance(R.layout.dialog_welcome_update);
		confirmDialog.setTitle(content);
		confirmDialog.setPositiveBtnText(getResources().getString(R.string.st_welcome_update_confirm));
		confirmDialog.setNegativeBtnText(getResources().getString(R.string.st_welcome_update_cancel));
		confirmDialog.setPercent(updatePercent);
		confirmDialog.setListener(new BaseFragment_Dialog.OnDialogClickListener() {
			@Override
			public void onCancel() {
				confirmDialog.dismiss();
				handleFirstOpen();
			}

			@Override
			public void onConfirm() {
				StatisticsManager.getInstance().trace(mApp, StatisticsManager.ID.APP_UPDATE, "点击更新");
				appInfo.startDownload();
				confirmDialog.dismiss();
				handleFirstOpen();
			}
		});
		return confirmDialog;
	}
}
