package com.oplay.giftcool.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.util.GameTypeUtil;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.game.GameDetailFragment;
import com.oplay.giftcool.util.IntentUtil;

/**
 * Created by zsigui on 15-12-31.
 */
public class GameDetailActivity extends BaseAppCompatActivity {

    private int mDetailId;
    private int mStatus;
    private int mStatusBarColorIndex;
    private int[] mThemeColor = {R.color.co_rainbow_color_1, R.color.co_rainbow_color_2,
            R.color.co_rainbow_color_3, R.color.co_rainbow_color_4, R.color.co_rainbow_color_5};
    private int[] mThemeColorStr = {R.string.st_rainbow_color_1, R.string.st_rainbow_color_2,
            R.string.st_rainbow_color_3, R.string.st_rainbow_color_4, R.string.st_rainbow_color_5};

    private GameDetailFragment mFragment;

    @Override
    protected void processLogic() {
        handleIntent(getIntent());
    }

    @Override
    protected int getStatusBarColor() {
        mStatusBarColorIndex = (int) (Math.random() * 4);
        int color;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            color = getResources().getColor(mThemeColor[mStatusBarColorIndex]);
        } else {
            color = getResources().getColor(mThemeColor[mStatusBarColorIndex], null);
        }
        return color;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_common_with_back);
    }

    @Override
    protected void initMenu(@NonNull Toolbar toolbar) {
        super.initMenu(toolbar);
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            mDetailId = intent.getIntExtra(KeyConfig.KEY_DATA, KeyConfig.TYPE_ID_DEFAULT);
            mStatus = intent.getIntExtra(KeyConfig.KEY_STATUS, GameTypeUtil.JUMP_STATUS_DETAIL);
        }
        if (mFragment == null) {
            Fragment f = getSupportFragmentManager().findFragmentById(R.id.fl_container);
            if (f != null) {
                mFragment = (GameDetailFragment) f;
                getSupportFragmentManager().beginTransaction().show(mFragment).commit();
                mFragment.updateUrl(mDetailId, mStatus, getResources().getString(mThemeColorStr[mStatusBarColorIndex]));
            } else {
                mFragment = GameDetailFragment.newInstance(mDetailId, mStatus,
                        getResources().getString(mThemeColorStr[mStatusBarColorIndex]));
                replaceFragWithTitle(R.id.fl_container, mFragment,
                        getResources().getString(R.string.st_game_continue), true);
            }
        } else {
            mFragment.updateUrl(mDetailId, mStatus, getResources().getString(mThemeColorStr[mStatusBarColorIndex]));
        }
        if (mToolbar != null) {
            mToolbar.setBackgroundResource(mThemeColor[mStatusBarColorIndex]);
        }

    }

    @Override
    protected void doBeforeFinish() {
        super.doBeforeFinish();
        if (MainActivity.sGlobalHolder == null) {
            IntentUtil.jumpHome(this, false);
        }
    }
}
