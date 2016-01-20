package com.oplay.giftassistant.ui.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.AssistantApp;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.config.KeyConfig;
import com.oplay.giftassistant.config.UserTypeUtil;
import com.oplay.giftassistant.manager.AccountManager;
import com.oplay.giftassistant.manager.ObserverManager;
import com.oplay.giftassistant.model.data.resp.UserInfo;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.dialog.ConfirmDialog;
import com.oplay.giftassistant.ui.fragment.game.GameFragment;
import com.oplay.giftassistant.ui.fragment.gift.GiftFragment;
import com.oplay.giftassistant.ui.widget.search.SearchLayout;
import com.oplay.giftassistant.util.IntentUtil;
import com.oplay.giftassistant.util.StringUtil;
import com.oplay.giftassistant.util.ToastUtil;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class MainActivity extends BaseAppCompatActivity implements ObserverManager.UserUpdateListener {

	// 保持一个Activity的全局对象
	public static MainActivity sGlobalHolder;
	private long mLastClickTime = 0;
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
	private Drawer mDrawer;
	private AccountHeader mDrawerHeader;
	private IProfile mProfile;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sGlobalHolder = MainActivity.this;
		createDrawer(savedInstanceState);
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

	private void createDrawer(Bundle savedInstanceState) {
		// 可以根据需要添加当前和候选
		// 比如历史登录过账号
		updateProfile();
		mDrawerHeader = new AccountHeaderBuilder()
				.withActivity(this)
				.withHeaderBackground(R.color.co_common_app_main_bg)
				.addProfiles(mProfile)
				.withProfileImagesClickable(true)
				.withOnAccountHeaderProfileImageListener(new AccountHeader.OnAccountHeaderProfileImageListener() {
					@Override
					public boolean onProfileImageClick(View view, IProfile iProfile, boolean b) {
						if (AccountManager.getInstance().isLogin()) {
							IntentUtil.jumpUserInfo(MainActivity.this);
						} else {
							IntentUtil.jumpLogin(MainActivity.this);
						}
						return false;
					}

					@Override
					public boolean onProfileImageLongClick(View view, IProfile iProfile, boolean b) {
						return false;
					}
				})
				.withSavedInstance(savedInstanceState)
				.build();


		mDrawer = new DrawerBuilder()
				.withActivity(this)
				.withHasStableIds(true)
				.withAccountHeader(mDrawerHeader)
				.addDrawerItems(
						new PrimaryDrawerItem().withName("我的礼包").withSelectable(false).withIdentifier(KeyConfig
								.TYPE_ID_MY_GIFT_CODE).withIcon(R.drawable.ic_toolbar_gift),
						new PrimaryDrawerItem().withName("我的钱包").withSelectable(false).withIdentifier(KeyConfig
								.TYPE_ID_WALLET).withIcon(R.drawable.ic_drawer_wallet),
						new PrimaryDrawerItem().withName("每日任务").withSelectable(false).withIdentifier(KeyConfig
								.TYPE_ID_SCORE_TASK).withIcon(R.drawable.ic_drawer_score_task),
						new PrimaryDrawerItem().withName("消息中心").withSelectable(false).withIdentifier(KeyConfig
								.TYPE_ID_MSG).withIcon(R.drawable.ic_drawer_message))
				.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
					@Override
					public boolean onItemClick(View view, int pos, IDrawerItem drawerItem) {
						if (drawerItem != null) {
							if (drawerItem.getIdentifier() != KeyConfig.TYPE_ID_SETTING
									&& drawerItem.getIdentifier() != KeyConfig.TYPE_ID_DOWNLOAD
									&& !AccountManager.getInstance().isLogin()) {
								IntentUtil.jumpLogin(MainActivity.this);
								return false;
							}
							switch (drawerItem.getIdentifier()) {
								case KeyConfig.TYPE_ID_MY_GIFT_CODE:
									IntentUtil.jumpMyGift(MainActivity.this);
									break;
								case KeyConfig.TYPE_ID_SETTING:
									IntentUtil.jumpSetting(MainActivity.this);
									break;
								case KeyConfig.TYPE_ID_SCORE_TASK:
									IntentUtil.jumpEarnScore(MainActivity.this);
									break;
								case KeyConfig.TYPE_ID_WALLET:
									IntentUtil.jumpMyWallet(MainActivity.this);
									break;
								case KeyConfig.TYPE_ID_DOWNLOAD:
									IntentUtil.jumpDownloadManager(MainActivity.this);
									break;
								case KeyConfig.TYPE_ID_MSG:
									ToastUtil.showShort("敬请期待");
									break;
							}
							return false;
						}
						// don't close drawer
						return true;
					}
				})
				.withSavedInstance(savedInstanceState)
				.withShowDrawerOnFirstLaunch(false)
				.build();
		//当侧边栏项太多时可通过以下代码缓存，以保证滑动时顺畅
		//RecyclerViewCacheUtil.getInstance().withCacheSize(2).init(mDrawer);

		if (AssistantApp.getInstance().isAllowDownload()) {
			mDrawer.addItem(new PrimaryDrawerItem().withName("下载管理").withSelectable(false).withIdentifier(KeyConfig
					.TYPE_ID_DOWNLOAD).withIcon(R.drawable.ic_drawer_download));
		}
		mDrawer.addItem(new PrimaryDrawerItem().withName("设置").withSelectable(false).withIdentifier(KeyConfig
				.TYPE_ID_SETTING).withIcon(R.drawable.ic_drawer_setting));
		mDrawerHeader.setActiveProfile(KeyConfig.TYPE_ID_PROFILE);
	}

	private void updateProfile() {
		if (!AccountManager.getInstance().isLogin()) {
			mProfile = new ProfileDrawerItem()
					.withName(getResources().getString(R.string.st_login_user))
					.withIcon("http://default_icon")
					.withIdentifier(KeyConfig.TYPE_ID_PROFILE);
		} else {
			String name;
			String email;
			if (mUser.loginType == UserTypeUtil.TYPE_POHNE
					|| (mUser.loginType != UserTypeUtil.TYPE_OUWAN && mUser.bindOuwanStatus == 0)) {
				name = (TextUtils.isEmpty(mUser.nick) ?  StringUtil.transePhone(mUser.phone) : mUser.nick);
				email = "登陆手机：" + StringUtil.transePhone(mUser.phone);
			} else {
				name = (TextUtils.isEmpty(mUser.nick) ? mUser.username : mUser.nick);
				email = "偶玩账号：" + mUser.username;
			}
			mProfile = new ProfileDrawerItem()
					.withName(name)
					.withEmail(email)
					.withIdentifier(KeyConfig.TYPE_ID_PROFILE);
			if (TextUtils.isEmpty(mUser.avatar)) {
				mProfile.withIcon("http://default_icon");
			} else {
				mProfile.withIcon(mUser.avatar);
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState = mDrawer.saveInstanceState(outState);
		outState = mDrawerHeader.saveInstanceState(outState);
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
				ivProfile.setImageResource(R.drawable.ic_avator_default);
			} else {
				ImageLoader.getInstance().displayImage(mUser.avatar, ivProfile, Global.AVATOR_IMAGE_LOADER);
			}
		} else {
			ivProfile.setImageResource(R.drawable.ic_avator_unlogin);
			tvGiftCount.setText("?");
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
				if (mDrawer.isDrawerOpen()) {
					mDrawer.closeDrawer();
				} else {
					mDrawer.openDrawer();
				}
				break;
			case R.id.ll_gift_count:
				if (AccountManager.getInstance().isLogin()) {
					IntentUtil.jumpMyGift(MainActivity.this);
				} else {
					final ConfirmDialog dialog = ConfirmDialog.newInstance();
					dialog.setContent("还没登录，是否跳转登录页面");
					dialog.setListener(new ConfirmDialog.OnDialogClickListener() {
						@Override
						public void onCancel() {
							dialog.dismiss();
						}

						@Override
						public void onConfirm() {
							IntentUtil.jumpLogin(MainActivity.this);
							dialog.dismiss();
						}
					});
					dialog.show(getSupportFragmentManager(), ConfirmDialog.class.getSimpleName());
				}
				break;
		}
	}

	@Override
	public void onBackPressed() {
		if (mDrawer != null && mDrawer.isDrawerOpen()) {
			mDrawer.closeDrawer();
			return;
		}

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
		updateProfile();
		mDrawerHeader.updateProfile(mProfile);
	}
}
