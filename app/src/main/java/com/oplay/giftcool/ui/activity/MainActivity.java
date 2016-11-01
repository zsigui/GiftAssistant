package com.oplay.giftcool.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.reflect.TypeToken;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.DialogManager;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.manager.OuwanSDKManager;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.model.data.resp.message.AwardNotify;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.MyFragment;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.ui.fragment.dialog.AwardDialog;
import com.oplay.giftcool.ui.fragment.dialog.ConfirmDialog;
import com.oplay.giftcool.ui.fragment.dialog.ImageViewDialog;
import com.oplay.giftcool.ui.fragment.dialog.SplashFragmentDialog;
import com.oplay.giftcool.ui.fragment.game.GameFragment;
import com.oplay.giftcool.ui.fragment.gift.GiftFragment;
import com.oplay.giftcool.ui.fragment.postbar.PostFragment;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.MixUtil;
import com.oplay.giftcool.util.PermissionUtil;
import com.oplay.giftcool.util.SPUtil;
import com.oplay.giftcool.util.ThreadUtil;
import com.oplay.giftcool.util.ToastUtil;

import java.util.ArrayList;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class MainActivity extends BaseAppCompatActivity implements ObserverManager.UserUpdateListener {

    final String TAG_GIFT = GiftFragment.class.getSimpleName();
    final String TAG_MY = MyFragment.class.getSimpleName();
    final String TAG_GAME = GameFragment.class.getSimpleName();
    final String TAG_POST = PostFragment.class.getSimpleName();

    final int INDEX_COUNT = 4;
    final int INDEX_DEFAULT = -1;
    // 需要按顺序 0...n
    final int INDEX_GIFT = 0;
    final int INDEX_GAME = 1;
    final int INDEX_POST = 2;
    final int INDEX_MY = 3;

    // 保持一个Activity的全局对象
    public static MainActivity sGlobalHolder;
    // 判断是否今天首次打开APP
    public static boolean sIsTodayFirstOpen = false;
    public static boolean sIsTodayFirstOpenForBroadcast = false;
    // 判断并显示弹窗
    public static boolean sIsLoginStateUnavailableShow = false;
    private boolean mHasJudgeBind = false;
    private boolean mHasShowSelectedUser = false;

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
    private LinearLayout llSearch;
    private FrameLayout flMsg;
    private ImageView ivMsgHint;
    // 礼物Fragment
    private GiftFragment mGiftFragment;
    private MyFragment mMyFragment;
    private GameFragment mGameFragment;
    private PostFragment mPostFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//		AssistantApp.getInstance().appInit();
        findFragmentByTag(savedInstanceState);
        super.onCreate(savedInstanceState);

        PermissionUtil.judgePermission(this);
        sGlobalHolder = MainActivity.this;
        handleIntent(getIntent());
        updateToolBarHint();
    }

    private void findFragmentByTag(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mGiftFragment = (GiftFragment) getSupportFragmentManager().findFragmentByTag(TAG_GIFT);
            mMyFragment = (MyFragment) getSupportFragmentManager().findFragmentByTag(TAG_MY);
            mGameFragment = (GameFragment) getSupportFragmentManager().findFragmentByTag(TAG_GAME);
            mPostFragment = (PostFragment) getSupportFragmentManager().findFragmentByTag(TAG_POST);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ObserverManager.getInstance().removeUserUpdateListener(this);
        sGlobalHolder = null;
    }

    protected void initView() {
        setContentView(R.layout.activity_main);
        llSearch = getViewById(R.id.ll_search);
        flMsg = getViewById(R.id.fl_msg);
        ivMsgHint = getViewById(R.id.iv_msg_hint);

        ivTabHints = new ImageView[INDEX_COUNT];
        ivTabHints[INDEX_GIFT] = getViewById(R.id.iv_gift_hint);
        ivTabHints[INDEX_POST] = getViewById(R.id.iv_post_hint);
        ivTabHints[INDEX_MY] = getViewById(R.id.iv_my_hint);
        CheckedTextView ctvGame = getViewById(R.id.ctv_game);
        CheckedTextView ctvGift = getViewById(R.id.ctv_gift);
        CheckedTextView ctvEssay = getViewById(R.id.ctv_post);
        CheckedTextView ctvMy = getViewById(R.id.ctv_my);
        mCtvs = new CheckedTextView[INDEX_COUNT];
        mCtvs[INDEX_GIFT] = ctvGift;
        mCtvs[INDEX_MY] = ctvMy;
        mCtvs[INDEX_GAME] = ctvGame;
        mCtvs[INDEX_POST] = ctvEssay;
        if (!AssistantApp.getInstance().isAllowDownload()) {
            ctvGame.setVisibility(View.GONE);
        }
    }

    @Override
    protected void initMenu(@NonNull Toolbar toolbar) {
        super.initMenu(toolbar);
        llSearch = getViewById(R.id.ll_search);
        flMsg = getViewById(R.id.fl_msg);
        ivMsgHint = getViewById(R.id.iv_msg_hint);
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
        if (!mNotifyAward) {
            judgeAwardShow();
        }
        judgeBindOrSelectUser();
    }

    public void judgeBindOrSelectUser() {
        if (!mHasJudgeBind && SplashFragmentDialog.sHasShow
                && AssistantApp.getInstance().getSetupOuwanAccount() != KeyConfig.KEY_LOGIN_NOT_BIND
                && AccountManager.getInstance().isLogin()
                && AccountManager.getInstance().getUserInfo().bindOuwanStatus != 1) {
            IntentUtil.jumpBindOwan(this, AccountManager.getInstance().getUser());
            mHasJudgeBind = true;
        } else if (!mHasShowSelectedUser && SplashFragmentDialog.sHasShow
                && !OuwanSDKManager.sIsWakeChangeAccountAction
                && !AccountManager.getInstance().isLogin()) {
//                OuwanSDKManager.getInstance().showSelectAccountView();
            MixUtil.sendSelectAccountBroadcast(this);
            mHasShowSelectedUser = true;
        }
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
            if (intent != null && intent.getAction() != null) {
                String action = intent.getAction();
                if (action.equals(AppConfig.PACKAGE_NAME() + ".action.MAIN")) {
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
                        case KeyConfig.TYPE_ID_INDEX_MY:
                            jumpToIndexMy(data);
                            break;
                        case KeyConfig.TYPE_ID_INDEX_UPGRADE:
                            mHasShowUpdate = false;
                            break;
                    }
                } else if (action.equals(Intent.ACTION_VIEW)) {
                    Uri uri = intent.getData();
                    MixUtil.handleViewUri(getApplicationContext(), uri);
                }
            }

        } catch (Exception e) {
            AppDebugConfig.w(AppDebugConfig.TAG_ACTIVITY, e);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KeyConfig.KEY_DATA, mCurSelectedItem);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            int index = savedInstanceState.getInt(KeyConfig.KEY_DATA, INDEX_DEFAULT);
            setCurSelected(index);
        }
    }

    @Override
    protected void processLogic() {

        ObserverManager.getInstance().addUserUpdateListener(this);
        llSearch.setOnClickListener(this);
        flMsg.setOnClickListener(this);

        for (CheckedTextView ctv : mCtvs) {
            ctv.setOnClickListener(this);
        }

        // 加载数据在父类进行，初始先显示加载页面，同时起到占位作用
        setCurSelected(mCurSelectedItem);
        ScoreManager.getInstance().initTaskState();
        showTabMyHint();
        showTabPostHint();
    }


    public void setCurSelected(int position) {
        if (mCurSelectedItem == position
                && position != INDEX_DEFAULT) {
            if (position == INDEX_GIFT && mGiftFragment != null) {
                showTabGiftHint(View.GONE);
            }
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
            case INDEX_MY:
                displayMyUI();
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
        if (mMyFragment == null) {
            Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_MY);
            if (f != null) {
                mMyFragment = (MyFragment) f;
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
        if (mMyFragment != null && mCurSelectedItem != INDEX_MY) {
            ft.hide(mMyFragment);
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
     * 显示我的界面
     */
    private void displayMyUI() {
        mCurSelectedItem = INDEX_MY;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        hideAllFragment(ft);
        if (mMyFragment == null) {
            Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_MY);
            if (f != null) {
                mMyFragment = (MyFragment) f;
                ft.show(mMyFragment);
            } else {
                mMyFragment = MyFragment.newInstance();
                ft.add(R.id.fl_container, mMyFragment, TAG_MY);
            }
        } else {
            ft.show(mMyFragment);
        }
        mMyFragment.setRetainInstance(true);
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


    public void showTabGiftHint(int view) {
        showTabHint(INDEX_GIFT, view);
    }

    /*
     * 该方法能保证在完成 Fragment 的唤醒之后再调用，防止在其他 Activity 调用本类 commit 之后出现 IllegalStateException
     */
    @Override
    protected void onPostResume() {
        super.onPostResume();
        jumpFragment();

        //没更新才显示欢迎
        if (sIsLoginStateUnavailableShow) {
            handleLoginUnavailable();
        } else if (!handleUpdateApp()) {
            handleFirstOpen();
        }
        if (!SplashFragmentDialog.sHasShow) {
            new SplashFragmentDialog().show(getSupportFragmentManager(), "splash");
        }
    }

    /**
     * 处理显示登录失效的弹窗
     */
    private void handleLoginUnavailable() {
        final ConfirmDialog dialog = ConfirmDialog.newInstance();
        dialog.setTitle("登录失效");
        dialog.setContent(ConstString.TOAST_SESSION_UNAVAILABLE);
        dialog.setNegativeVisibility(View.GONE);
        dialog.setPositiveVisibility(View.VISIBLE);
        dialog.setPositiveBtnText("我知道了");
        dialog.setListener(new BaseFragment_Dialog.OnDialogClickListener() {
            @Override
            public void onCancel() {
                dialog.dismissAllowingStateLoss();
            }

            @Override
            public void onConfirm() {
                dialog.dismissAllowingStateLoss();
                IntentUtil.jumpLoginNoToast(MainActivity.this);
            }
        });
        dialog.show(getSupportFragmentManager(), "session_unavailable");
        sIsLoginStateUnavailableShow = false;
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
            mJumpPostPos = -1;
        } else if (mJumpMyPos != -1) {
            setCurSelected(INDEX_MY);
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
            case R.id.ll_search:
                IntentUtil.jumpSearch(this);
                break;
            case R.id.fl_msg:
                if (AccountManager.getInstance().isLogin()) {
                    AccountManager.getInstance().setUnreadMessageCount(0);
                    updateToolBarHint();
                    IntentUtil.jumpMessageCentral(this);
                } else {
                    IntentUtil.jumpLogin(this);
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
            SplashFragmentDialog.sHasShow = false;
//            System.exit(0);
        } else {
            mLastClickTime = System.currentTimeMillis();
            ToastUtil.showShort(ConstString.TOAST_EXIT_APP);
        }
    }


    // 定义跳转定位的位置
    private int mJumpGamePos = -1;
    private int mJumpGiftPos = -1;
    private int mJumpPostPos = -1;
    private int mJumpMyPos = -1;

    public void jumpToIndexGame(int gamePosition) {
        mJumpGamePos = gamePosition;
        mJumpMyPos = mJumpPostPos = mJumpGiftPos = -1;
        if (mActive) {
            jumpFragment();
        }
    }

    public void jumpToIndexGift(final int giftPosition) {
        mJumpGiftPos = giftPosition;
        mJumpMyPos = mJumpPostPos = mJumpGamePos = -1;
        if (mActive) {
            jumpFragment();
        }
    }

    public void jumpToIndexPost(final int postPosition) {
        mJumpPostPos = postPosition;
        mJumpMyPos = mJumpGiftPos = mJumpGamePos = -1;
        if (mActive) {
            jumpFragment();
        }
    }

    public void jumpToIndexMy(final int myPosition) {
        mJumpMyPos = myPosition;
        mJumpPostPos = mJumpGiftPos = mJumpGamePos = -1;
        if (mActive) {
            jumpFragment();
        }
    }

    @Override
    public void onUserUpdate(final int action) {
        ThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (action) {
                    case ObserverManager.STATUS.USER_UPDATE_ALL:
                        showTabMyHint();
                        showTabPostHint();
                        updateToolBarHint();
                        break;
                    case ObserverManager.STATUS.USER_UPDATE_PUSH_MESSAGE:
                        updateToolBarHint();
                        mNotifyAward = false;
                        judgeAwardShow();
                        break;
                }
            }
        });
    }

    private void showTabPostHint() {
        if (AccountManager.getInstance().isLogin()
                && ScoreManager.getInstance().isSignInTaskFinished()
                && ScoreManager.getInstance().isFreeLotteryEmpty()) {
            showTabHint(INDEX_POST, View.GONE);
        } else {
            showTabHint(INDEX_POST, View.VISIBLE);
        }
    }

    private void showTabMyHint() {
        if ((!AccountManager.getInstance().isLogin()
                || AccountManager.getInstance().getUnreadMessageCount() == 0)
                && ApkDownloadManager.getInstance(getApplicationContext()).getEndOfPaused() == 0) {
            showTabHint(INDEX_MY, View.GONE);
        } else {
            showTabHint(INDEX_MY, View.VISIBLE);
        }
    }

    /**
     * 更新工具栏小红点提示 <br />
     */
    private void updateToolBarHint() {
        ThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showTabMyHint();
                if (ivMsgHint != null) {
                    if (AccountManager.getInstance().getUnreadMessageCount() > 0) {
                        ivMsgHint.setVisibility(View.VISIBLE);
                    } else {
                        ivMsgHint.setVisibility(View.GONE);
                    }
                }
                if (mMyFragment != null) {
                    mMyFragment.updateMsgHint(AccountManager.getInstance().getUnreadMessageCount());
                }
            }
        });
    }

    private void judgeAwardShow() {
        if (!AccountManager.getInstance().isLogin()) {
            // 未登录，无须领取
            AppDebugConfig.v(AppDebugConfig.TAG_DEBUG_INFO, "登录后才需要判断领取状态");
            return;
        }
        String s = SPUtil.getString(this, SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_USER_AWARD, null);
        if (!TextUtils.isEmpty(s)) {
            final ArrayList<AwardNotify> data = AssistantApp.getInstance().getGson()
                    .fromJson(s, new TypeToken<ArrayList<AwardNotify>>() {
                    }.getType());
            SPUtil.putString(this, SPConfig.SP_USER_INFO_FILE, SPConfig.KEY_USER_AWARD, "");
            mNotifyAward = true;
            ThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (int i = data.size() - 1; i >= 0; i--) {
                        AwardNotify notify = data.get(i);
                        showAwardDialog(getSupportFragmentManager(), notify, String.valueOf(i));
                    }
                }
            });
        }
    }

    private boolean mNotifyAward = false;

    public void showAwardDialog(FragmentManager fm, AwardNotify data, String tag) {
        AwardDialog dialog = AwardDialog.newInstance(data);
        dialog.setFm(getSupportFragmentManager());
        dialog.show(fm, tag);
    }

    private void handleFirstOpen() {
        if (AccountManager.getInstance().isLogin()
                && AssistantApp.getInstance().getBroadcastBanner() != null
                && AccountManager.getInstance().getUserInfo().isFirstLogin
                && mHandler != null) {
            // 首次登录显示弹窗
            ImageViewDialog dialog = ImageViewDialog.newInstance(AssistantApp.getInstance().getBroadcastBanner());
            dialog.show(getSupportFragmentManager(), "broadcast");
            AccountManager.getInstance().getUserInfo().isFirstLogin = false;
        }
    }

    public void updateHintState(int key, final int count) {
        switch (key) {
            case KeyConfig.TYPE_ID_DOWNLOAD:
                ThreadUtil.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showTabMyHint();
                        if (mMyFragment != null)
                            mMyFragment.updateDownloadHint(count);
                    }
                });
                break;
            case KeyConfig.TYPE_ID_MSG:
                updateToolBarHint();
                break;
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
        // 礼物Fragment
        mGiftFragment = null;
        mGameFragment = null;
    }

}
