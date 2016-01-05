package com.oplay.giftassistant.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.manager.AccountManager;
import com.oplay.giftassistant.model.UserModel;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.game.GameFragment;
import com.oplay.giftassistant.ui.fragment.gift.GiftFragment;
import com.oplay.giftassistant.ui.widget.search.SearchLayout;
import com.oplay.giftassistant.util.ToastUtil;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class MainActivity extends BaseAppCompatActivity {

	private static final int ID_SETTING = 0;
	private static final int ID_TASK = 1;
	private static final int ID_WALLET = 2;
	private static final int ID_GIFTS = 3;
	private static final int ID_DOWNLOAD = 4;
	private static final int ID_ABOUT = 5;
	private static final int ID_FEEDBACK = 6;


	private long mLastClickTime = 0;
	// 底部Tabs
	private CheckedTextView[] mCtvs;
	// 礼物Fragment
	private GiftFragment mGiftFragment;
	private GameFragment mGameFragment;
	// 当前选项卡下标
	private int mCurrentIndex = 0;

	private UserModel mUser;
	// 侧边栏
	private Drawer mDrawer;
	private AccountHeader mDrawerHeader;
	private boolean mIsOpen = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUser = AccountManager.getInstance().getUser();
		createDrawer(savedInstanceState);
	}

	protected void initView() {
		setContentView(R.layout.activity_main);
		CheckedTextView ctvGame = getViewById(R.id.ctv_game);
		CheckedTextView ctvGift = getViewById(R.id.ctv_gift);
		mCtvs = new CheckedTextView[2];
		mCtvs[0] = ctvGift;
		mCtvs[1] = ctvGame;


	}

	private void createDrawer(Bundle savedInstanceState) {
		final IProfile profile;
		// 可以根据需要添加当前和候选
		// 比如历史登录过账号
		if (!AccountManager.getInstance().isLogin()) {
			profile = new ProfileDrawerItem()
					.withName("未知")
					.withIcon(R.drawable.test_code)
					.withIdentifier(100);
		} else {
			UserModel user = AccountManager.getInstance().getUser();
			profile = new ProfileDrawerItem()
					.withName(TextUtils.isEmpty(user.username) ? user.phone : user.username)
					.withEmail(user.phone)
					.withIcon(user.img)
					.withIdentifier(100);
		}
		mDrawerHeader = new AccountHeaderBuilder()
				.withActivity(this)
				.withHeaderBackground(R.color.co_common_app_main_bg)
				.addProfiles(profile)
				.withProfileImagesClickable(true)
				.withOnAccountHeaderProfileImageListener(new AccountHeader.OnAccountHeaderProfileImageListener() {
					@Override
					public boolean onProfileImageClick(View view, IProfile iProfile, boolean b) {
						if (AccountManager.getInstance().isLogin()) {
							ToastUtil.showShort("已经登录，跳转个人信息页面");
						} else {
							ToastUtil.showShort("未登录，跳转登录页面");
						}
						return true;
					}

					@Override
					public boolean onProfileImageLongClick(View view, IProfile iProfile, boolean b) {
						return false;
					}
				})
				.withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
					@Override
					public boolean onProfileChanged(View view, IProfile iProfile, boolean b) {

						return true;
					}
				})
				.withSavedInstance(savedInstanceState)
				.build();


		mDrawer = new DrawerBuilder()
				.withActivity(this)
				.withHasStableIds(true)
				.withAccountHeader(mDrawerHeader)
				.addDrawerItems(
						new PrimaryDrawerItem().withName("我的礼包").withSelectable(true).withIdentifier(ID_GIFTS).withIcon
								(GoogleMaterial.Icon.gmd_ac_unit),
						new PrimaryDrawerItem().withName("个人设置").withSelectable(true).withIdentifier(ID_SETTING).withIcon
								(GoogleMaterial.Icon.gmd_account_balance),
						new DividerDrawerItem(),
						new PrimaryDrawerItem().withName("意见反馈").withSelectable(true).withIdentifier(ID_FEEDBACK).withIcon
								(GoogleMaterial.Icon.gmd_feedback),
						new PrimaryDrawerItem().withName("我的钱包").withSelectable(true).withIdentifier(ID_WALLET).withIcon
								(GoogleMaterial.Icon.gmd_settings_input_svideo),
						new PrimaryDrawerItem().withName("下载管理").withSelectable(true).withIdentifier(ID_DOWNLOAD).withIcon
								(GoogleMaterial.Icon.gmd_settings_input_svideo),
						new PrimaryDrawerItem().withName("积分任务").withSelectable(true).withIdentifier(ID_TASK).withIcon
								(GoogleMaterial.Icon.gmd_settings_input_svideo),
						new PrimaryDrawerItem().withName("关于礼包酷").withSelectable(true).withIdentifier(ID_ABOUT).withIcon
								(GoogleMaterial.Icon.gmd_settings_input_svideo))
				.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
					@Override
					public boolean onItemClick(View view, int pos, IDrawerItem drawerItem) {
						if (drawerItem != null) {
							if (drawerItem.getIdentifier() != 3 && !AccountManager.getInstance().isLogin()) {
								ToastUtil.showShort("跳转登录界面");
								// close drawer
								return false;
							}
							switch (drawerItem.getIdentifier()) {
								case ID_GIFTS:
									ToastUtil.showShort("跳转 我的礼包 界面");
									break;
								case ID_SETTING:
									ToastUtil.showShort("跳转 个人设置 界面");
									break;
								case ID_FEEDBACK:
									ToastUtil.showShort("跳转 意见反馈 界面");
									break;
								case ID_ABOUT:
									ToastUtil.showShort("跳转 关于 界面");
									break;
								case ID_TASK:
									ToastUtil.showShort("跳转 积分任务 界面");
									break;
								case ID_WALLET:
									ToastUtil.showShort("跳转 我的钱包 界面");
									break;
								case ID_DOWNLOAD:
									ToastUtil.showShort("跳转 下载管理 界面");
									break;
							}
							return true;
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

		mDrawerHeader.setActiveProfile(100);
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
		ImageView ivProfile = getViewById(toolbar, R.id.iv_profile);
		ivProfile.setOnClickListener(this);
		getViewById(toolbar, R.id.ll_gift_count).setOnClickListener(this);
		((TextView) getViewById(toolbar, R.id.tv_gift_count)).setText(mUser == null ? "?" : String.valueOf(mUser
				.giftCount));
	}

	@Override
	protected void processLogic() {
		for (CheckedTextView ctv : mCtvs) {
			ctv.setOnClickListener(this);
		}

		// 加载数据在父类进行，初始先显示加载页面，同时起到占位作用
		setCurSelected(mCurrentIndex);
	}

	private void setCurSelected(int position) {
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
				Intent intent = new Intent(MainActivity.this, SearchActivity.class);
				startActivity(intent);
				break;
			case R.id.iv_profile:
				if (mDrawer.isDrawerOpen()) {
					mDrawer.closeDrawer();
				} else {
					mDrawer.openDrawer();
				}
				break;
			case R.id.ll_gift_count:
				if (!AccountManager.getInstance().isLogin()) {
					ToastUtil.showShort("跳转 登录 页面");
				} else {
					ToastUtil.showShort("跳转 我的礼包 页面");
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

}
