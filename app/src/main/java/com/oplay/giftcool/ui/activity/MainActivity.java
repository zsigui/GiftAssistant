package com.oplay.giftcool.ui.activity;

import android.content.Intent;
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
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.DialogManager;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.manager.SocketIOManager;
import com.oplay.giftcool.model.data.resp.UserInfo;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.DrawerFragment;
import com.oplay.giftcool.ui.fragment.dialog.AllViewDialog;
import com.oplay.giftcool.ui.fragment.game.GameFragment;
import com.oplay.giftcool.ui.fragment.gift.GiftFragment;
import com.oplay.giftcool.ui.fragment.gift.GiftFreeFragment;
import com.oplay.giftcool.ui.fragment.postbar.PostFragment;
import com.oplay.giftcool.ui.widget.search.SearchLayout;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.PermissionUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.oplay.giftcool.util.ViewUtil;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class MainActivity extends BaseAppCompatActivity implements ObserverManager.UserUpdateListener {

    final String TAG_GIFT = GiftFragment.class.getSimpleName();
    final String TAG_FREE = GiftFreeFragment.class.getSimpleName();
    final String TAG_GAME = GameFragment.class.getSimpleName();
    final String TAG_POST = PostFragment.class.getSimpleName();
    final String TAG_DRAWER = DrawerFragment.class.getSimpleName();

    final int INDEX_COUNT = 4;
    final int INDEX_DEFAULT = -1;
    // 需要按顺序 0...n
    final int INDEX_GIFT = 0;
    final int INDEX_FREE = 1;
    final int INDEX_GAME = 2;
    final int INDEX_POST = 3;

    // 保持一个Activity的全局对象
    public static MainActivity sGlobalHolder;
    // 判断是否今日首次打开APP
    public static boolean sIsTodayFirstOpen = false;
    public static boolean sIsTodayFirstOpenForBroadcast = false;

    private long mLastClickTime = 0;
    private int mCurSelectedItem = INDEX_DEFAULT;

    //是否已经显示过更新提示
    private boolean mHasShowUpdate = false;
    private boolean mActive = false;
    // 底部Tabs
//	private LinearLayout llTab;
    private CheckedTextView[] mCtvs;
    private ImageView[] ivTabHints;

    // 顶部导航栏
    private SearchLayout mSearchLayout;
    private LinearLayout llGiftCount;
    private ImageView ivProfile;
    private ImageView ivHint;
    private TextView tvGiftCount;
    private TextView tvTitle;
    private ImageView ivTitle;
    // 礼物Fragment
    private GiftFragment mGiftFragment;
    private GiftFreeFragment mFreeFragment;
    private GameFragment mGameFragment;
    private PostFragment mPostFragment;
    // 侧边栏
    private DrawerLayout mDrawerLayout;
    private DrawerFragment mDrawerFragment;

    private Handler mHandler = new Handler(Looper.myLooper());

    private void initHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.myLooper());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//		AssistantApp.getInstance().appInit();
        findFragmentByTag(savedInstanceState);
        super.onCreate(savedInstanceState);
        PermissionUtil.judgePermission(this);
        sGlobalHolder = MainActivity.this;
        updateToolBar();
        handleIntent(getIntent());
        updateHintState(KeyConfig.TYPE_ID_DOWNLOAD, ApkDownloadManager.getInstance(this).getEndOfPaused());
        SocketIOManager.getInstance().connectOrReConnect(false);
    }

    private void findFragmentByTag(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mGiftFragment = (GiftFragment) getSupportFragmentManager().findFragmentByTag(TAG_GIFT);
            mFreeFragment = (GiftFreeFragment) getSupportFragmentManager().findFragmentByTag(TAG_FREE);
            mGameFragment = (GameFragment) getSupportFragmentManager().findFragmentByTag(TAG_GAME);
            mPostFragment = (PostFragment) getSupportFragmentManager().findFragmentByTag(TAG_POST);
            mDrawerFragment = (DrawerFragment) getSupportFragmentManager().findFragmentByTag(TAG_DRAWER);
        }
    }

    private void createDrawer() {
        mDrawerLayout = getViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.st_drawer_text_open, R.string.st_drawer_text_close));
        // 考虑onSaveInstanceState造成的叠加问题
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (mDrawerFragment == null) {
            Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_DRAWER);
            if (f != null) {
                mDrawerFragment = (DrawerFragment) f;
            } else {
                mDrawerFragment = DrawerFragment.newInstance(mDrawerLayout);
            }
            mDrawerFragment.setRetainInstance(true);
        }
        ft.replace(R.id.drawer_container, mDrawerFragment, TAG_DRAWER);
        ft.commit();
        updateToolBar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ObserverManager.getInstance().removeUserUpdateListener(this);
        sGlobalHolder = null;
    }

    protected void initView() {
        setContentView(R.layout.activity_main);
//		llTab = getViewById(R.id.ll_tabs);
        ivTabHints = new ImageView[INDEX_COUNT];
        ivTabHints[INDEX_GIFT] = getViewById(R.id.iv_gift_hint);
        ivTabHints[INDEX_POST] = getViewById(R.id.iv_post_hint);
        CheckedTextView ctvGame = getViewById(R.id.ctv_game);
        CheckedTextView ctvGift = getViewById(R.id.ctv_gift);
        CheckedTextView ctvEssay = getViewById(R.id.ctv_post);
        CheckedTextView ctvFree = getViewById(R.id.ctv_free);
        mCtvs = new CheckedTextView[INDEX_COUNT];
        mCtvs[INDEX_GIFT] = ctvGift;
        mCtvs[INDEX_FREE] = ctvFree;
        mCtvs[INDEX_GAME] = ctvGame;
        mCtvs[INDEX_POST] = ctvEssay;
        if (!AssistantApp.getInstance().isAllowDownload()) {
            ctvGame.setVisibility(View.GONE);
        }
    }

    @Override
    protected void initMenu(@NonNull Toolbar toolbar) {
        super.initMenu(toolbar);
        mSearchLayout = getViewById(toolbar, R.id.sl_search);
        mSearchLayout.setCanGetFocus(false);
        mSearchLayout.setOnClickListener(this);
        ivProfile = getViewById(toolbar, R.id.iv_profile);
        ivProfile.setOnClickListener(this);
        tvGiftCount = getViewById(toolbar, R.id.tv_gift_count);
        ivHint = getViewById(R.id.iv_hint);
        tvTitle = getViewById(R.id.tv_title);
        ivTitle = getViewById(R.id.iv_title);
        llGiftCount = getViewById(R.id.ll_gift_count);
        llGiftCount.setOnClickListener(this);
    }

    private void updateToolBar() {
        initHandler();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (AccountManager.getInstance().isLogin()) {
                    if (ScoreManager.getInstance().isSignInTaskFinished()) {
                        updateHintState(KeyConfig.TYPE_SIGN_IN_EVERY_DAY, 0);
                    } else {
                        updateHintState(KeyConfig.TYPE_SIGN_IN_EVERY_DAY, 1);
                    }
                    if (mCurSelectedItem == INDEX_POST
                            || (ScoreManager.getInstance().isSignInTaskFinished()
                            && ScoreManager.getInstance().isFreeLotteryEmpty())) {
                        showTabHint(INDEX_POST, View.GONE);
                    } else {
                        showTabHint(INDEX_POST, View.VISIBLE);
                    }
                    updateHintState(KeyConfig.TYPE_ID_MSG, AccountManager.getInstance().getUnreadMessageCount());
                    UserInfo user = AccountManager.getInstance().getUserInfo();
                    tvGiftCount.setText(String.valueOf(user.giftCount));
                    ViewUtil.showAvatarImage(user.avatar, ivProfile, true);
                } else {
                    updateHintState(KeyConfig.TYPE_SIGN_IN_EVERY_DAY, 1);
                    updateHintState(KeyConfig.TYPE_ID_MSG, 0);
                    showTabHint(INDEX_POST, View.VISIBLE);
                    ivProfile.setImageResource(R.drawable.ic_avator_unlogin);
                    ivProfile.setTag("");
                    tvGiftCount.setText("0");
                }
            }
        });
    }

    /**
     * 显示下端标签栏红点提示
     */
    @SuppressWarnings("ResourceType")
    public void showTabHint(int pos, int visibility) {
        ivTabHints[pos].setVisibility(visibility);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActive = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mActive = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        try {
            if (intent != null && intent.getAction() != null
                    && intent.getAction().equals(AppConfig.PACKAGE_NAME + ".action.Main")) {
                int type = intent.getIntExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_DEFAULT);
                String dStr = intent.getStringExtra(KeyConfig.KEY_DATA);
                int data = Integer.parseInt(TextUtils.isEmpty(dStr) ? "0" : dStr);
                switch (type) {
                    case KeyConfig.TYPE_ID_INDEX_GIFT:
                        jumpToIndexGift(data);
                        break;
                    case KeyConfig.TYPE_ID_INDEX_GAME:
                        jumpToIndexGame(data);
                        break;
                    case KeyConfig.TYPE_ID_INDEX_POST:
                        jumpToIndexPost(data);
                        break;
                    case KeyConfig.TYPE_ID_INDEX_FREE:
                        jumpToIndexFree(data);
                        break;
                    case KeyConfig.TYPE_ID_INDEX_UPGRADE:
                        mHasShowUpdate = false;
                        break;
                }
            }
        } catch (Exception e) {
            AppDebugConfig.w(AppDebugConfig.TAG_ACTIVITY, e);
        }
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
            case INDEX_POST:
                displayEssayUI();
                break;
            case INDEX_GAME:
                displayGameUI();
                break;
            case INDEX_FREE:
                displayFreeUI();
                break;
            case INDEX_GIFT:
            default:
                displayGiftUI();
                break;
        }
        showToolbarStyle(position);
    }

    private void showToolbarStyle(int index) {
        if (tvTitle != null) {
            tvTitle.setVisibility(index == INDEX_POST ? View.VISIBLE : View.GONE);
        }
        final boolean isShowSearch = (index == INDEX_GIFT || index == INDEX_GAME || index == INDEX_DEFAULT);
        if (mSearchLayout != null) {
            mSearchLayout.setVisibility(isShowSearch ? View.VISIBLE : View.GONE);
        }
        if (llGiftCount != null) {
            llGiftCount.setVisibility(isShowSearch ? View.VISIBLE : View.GONE);
        }
        if (ivTitle != null) {
            ivTitle.setVisibility(index == INDEX_FREE ? View.VISIBLE : View.GONE);
        }

    }

    /**
     * 隐藏所有的Fragment
     */
    private void hideAllFragment(FragmentTransaction ft) {
        if (mGameFragment == null) {
            // Activity被回收重建后查找
            Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_GAME);
            if (f != null) {
                mGameFragment = (GameFragment) f;
            }
        }
        if (mGiftFragment == null) {
            Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_GIFT);
            if (f != null) {
                mGiftFragment = (GiftFragment) f;
            }
        }
        if (mPostFragment == null) {
            Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_POST);
            if (f != null) {
                mPostFragment = (PostFragment) f;
            }
        }
        if (mFreeFragment == null) {
            Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_FREE);
            if (f != null) {
                mFreeFragment = (GiftFreeFragment) f;
            }
        }
        if (mGiftFragment != null && mCurSelectedItem != INDEX_GIFT) {
            ft.hide(mGiftFragment);
        }
        if (mGameFragment != null && mCurSelectedItem != INDEX_GAME) {
            ft.hide(mGameFragment);
        }
        if (mPostFragment != null && mCurSelectedItem != INDEX_POST) {
            ft.hide(mPostFragment);
        }
        if (mFreeFragment != null && mCurSelectedItem != INDEX_FREE) {
            ft.hide(mFreeFragment);
        }
    }

    /**
     * 显示游戏界面，采用 show/hide 进行显示
     */
    private void displayGameUI() {
        mCurSelectedItem = INDEX_GAME;
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
        mGameFragment.setRetainInstance(true);
        ft.commit();
    }

    /**
     * 显示礼包界面，采用 show/hide 进行显示
     */
    private void displayGiftUI() {
        mCurSelectedItem = INDEX_GIFT;
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
        mGiftFragment.setRetainInstance(true);
        ft.commit();
    }

    /**
     * 显示限时免费界面
     */
    private void displayFreeUI() {
        mCurSelectedItem = INDEX_FREE;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        hideAllFragment(ft);
        if (mFreeFragment == null) {
            Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_FREE);
            if (f != null) {
                mFreeFragment = (GiftFreeFragment) f;
                ft.show(mFreeFragment);
            } else {
                mFreeFragment = GiftFreeFragment.newInstance();
                ft.add(R.id.fl_container, mFreeFragment, TAG_FREE);
            }
        } else {
            ft.show(mFreeFragment);
        }
        mFreeFragment.setRetainInstance(true);
        ft.commit();
    }

    /**
     * 显示活动模块
     */
    private void displayEssayUI() {
        mCurSelectedItem = INDEX_POST;
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
        mPostFragment.setRetainInstance(true);
        ft.commit();
        if (AccountManager.getInstance().isLogin()) {
            showTabHint(INDEX_POST, View.GONE);
        }
    }

    /*
     * 该方法能保证在完成 Fragment 的唤醒之后再调用，防止在其他 Activity 调用本类 commit 之后出现 IllegalStateException
     */
    @Override
    protected void onPostResume() {
        super.onPostResume();
        jumpFragment();


        //没更新才显示欢迎
        if (!handleUpdateApp()) {
            handleFirstOpen();
        }
    }

    private void jumpFragment() {
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
        } else if (mJumpPostPos != -1) {
            setCurSelected(INDEX_POST);
            if (mPostFragment != null) {
                mPostFragment.setPagePosition(mJumpPostPos);
            }
            mJumpPostPos = -1;
        } else if (mJumpFreePos != -1) {
            setCurSelected(INDEX_FREE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.doAfterRequest(this, requestCode, grantResults);
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

        if (System.currentTimeMillis() - mLastClickTime <= 3000) {
            mApp.appExit();
            // 发送退出指令
            finish();
            System.exit(0);
        } else {
            mLastClickTime = System.currentTimeMillis();
            ToastUtil.showShort(ConstString.TEXT_EXIT_APP);
        }
    }


    // 定义跳转定位的位置
    private int mJumpGamePos = -1;
    private int mJumpGiftPos = -1;
    private int mJumpPostPos = -1;
    private int mJumpFreePos = -1;

    public void jumpToIndexGame(int gamePosition) {
        mJumpGamePos = gamePosition;
        mJumpFreePos = mJumpPostPos = mJumpGiftPos = -1;
        if (mActive) {
            jumpFragment();
        }
    }

    public void jumpToIndexGift(final int giftPosition) {
        mJumpGiftPos = giftPosition;
        mJumpFreePos = mJumpPostPos = mJumpGamePos = -1;
        if (mActive) {
            jumpFragment();
        }
    }

    public void jumpToIndexPost(final int postPosition) {
        mJumpPostPos = postPosition;
        mJumpFreePos = mJumpGiftPos = mJumpGamePos = -1;
        if (mActive) {
            jumpFragment();
        }
    }

    public void jumpToIndexFree(final int freePosition) {
        mJumpFreePos = freePosition;
        mJumpPostPos = mJumpGiftPos = mJumpGamePos = -1;
        if (mActive) {
            jumpFragment();
        }
    }

    @Override
    public void onUserUpdate(int action) {
        switch (action) {
            case ObserverManager.STATUS.USER_UPDATE_ALL:
            case ObserverManager.STATUS.USER_UPDATE_TASK:
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
    private SparseIntArray mHintCount;

    /**
     * 更新侧边栏和顶部导航栏信息
     */
    public void updateHintState(final int key, final int count) {
        if (mHandler == null) {
            return;
        }
        if (mHintCount == null) {
            mHintCount = new SparseIntArray();
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
                }
                if (mDrawerFragment != null) {
                    mDrawerFragment.updateCount(key, count);
                }
            }
        }, 300);

    }

    private void handleFirstOpen() {
        if (AccountManager.getInstance().isLogin()
                && AssistantApp.getInstance().getBroadcastBanner() != null
                && AccountManager.getInstance().getUserInfo().isFirstLogin
                && mHandler != null) {
            // 首次登录显示弹窗
            AllViewDialog dialog = AllViewDialog.newInstance(AssistantApp.getInstance().getBroadcastBanner());
            dialog.show(getSupportFragmentManager(), "broadcast");
            AccountManager.getInstance().getUserInfo().isFirstLogin = false;
        }
    }

    /**
     * 处理更新逻辑
     *
     * @return　是否有更新
     */
    private boolean handleUpdateApp() {
        if (!mHasShowUpdate
                && DialogManager.getInstance().showUpdateDialog(this, getSupportFragmentManager(), false)) {
            mHasShowUpdate = true;
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
//		llTab = null;
        mCtvs = null;
        ivProfile = null;
        tvGiftCount = null;
        // 礼物Fragment
        mGiftFragment = null;
        mGameFragment = null;
        mDrawerLayout = null;
        mDrawerFragment = null;
    }

}
