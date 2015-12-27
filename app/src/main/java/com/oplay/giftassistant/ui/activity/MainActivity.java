package com.oplay.giftassistant.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckedTextView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.GiftFragment;
import com.oplay.giftassistant.ui.fragment.MoocRecyclerViewFragment;
import com.oplay.giftassistant.ui.widget.search.SearchLayout;
import com.oplay.giftassistant.util.ToastUtil;

import java.util.ArrayList;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class MainActivity extends BaseAppCompatActivity {

	private long mLastClickTime = 0;
    // 底部Tabs
	private CheckedTextView[] mCtvs;
    // 礼物Fragment
	private GiftFragment mGiftFragment;
	private Fragment mMoocRecyclerViewFragment;
    // 顶部toolbar搜索框
	private SearchLayout mSearchLayout;
    // 当前选项卡下标
    private int mCurrentIndex = 0;
    // 判断礼物界面是否初始化
    private boolean mHasGiftData = false;
    // 判断礼物界面数据是否在加载中，以防止重复调用
    private boolean mIsGiftDataLoading = false;

    private static MainActivity sInstance;
    private static Handler mHandler = new Handler() {
        @Override
        @SuppressWarnings("unchecked")
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                if (sInstance == null) {
                    return;
                }
                if (sInstance.mCurrentIndex == 0) {
                    sInstance.mGiftFragment.updateBanners((ArrayList<String>) msg.obj);
                } else {

                }
            }
        }
    };


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        sInstance = this;
		processLogic();
	}

	protected void initView() {
		setContentView(R.layout.activity_main);
		CheckedTextView ctvGame = getViewById(R.id.ctv_game);
		CheckedTextView ctvGift = getViewById(R.id.ctv_gift);
		mCtvs = new CheckedTextView[2];
		mCtvs[0] = ctvGift;
		mCtvs[1] = ctvGame;
	}

	@Override
	protected void initMenu(@NonNull Toolbar toolbar) {
		super.initMenu(toolbar);
		mSearchLayout = getViewById(toolbar, R.id.sl_search);
		if (mSearchLayout != null) {
			mSearchLayout.setCanGetFocus(false);
			mSearchLayout.setOnClickListener(this);
		}
	}

	protected void processLogic() {
		for (CheckedTextView ctv : mCtvs) {
			ctv.setOnClickListener(this);
		}
		if (mGiftFragment == null) {
			mGiftFragment = GiftFragment.newInstance();
		}

        // 加载数据在父类进行，初始先显示加载页面
        displayLoadingUI(R.id.fl_main_container);
        initGiftData();
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
            if (mHasGiftData) {
                displayGiftUI();
            } else {
                if (!mIsGiftDataLoading) {
                    initGiftData();
                }
                displayLoadingUI(R.id.fl_main_container);
            }
		} else {
            displayGameUI();
		}
	}

    private void displayGameUI() {
        if (mMoocRecyclerViewFragment == null) {
            mMoocRecyclerViewFragment = MoocRecyclerViewFragment.newInstance();
        }
        reattachFrag(R.id.fl_main_container, mMoocRecyclerViewFragment,
                mMoocRecyclerViewFragment.getClass().getSimpleName());
    }

    private void displayGiftUI() {
        if (mGiftFragment == null) {
            mGiftFragment = GiftFragment.newInstance();
        }
        reattachFrag(R.id.fl_main_container, mGiftFragment, mGiftFragment.getClass().getSimpleName());
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
		}
	}

    public void initGiftData() {
        if (mHasGiftData) {
            return;
        }
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep((int) (Math.random() * 2000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHasData = true;
                mLimitDatas = new ArrayList<IndexLimitGift>();
                mLikeDatas = new ArrayList<IndexLikeGame>();
                mNewDatas = new ArrayList<IndexNewGift>();
                for (int i = 0; i < 10; i++) {
                    IndexLikeGame game = new IndexLikeGame();
                    game.name = "游戏名" + i;
                    game.hasGiftCount = 10 - i;
                    game.newGiftCount = i;
                    game.img = "http://m.ouwan.com/api/extDownloadApp?app_id=4210&chn=200";
                    mLikeDatas.add(game);
                    IndexLimitGift gift = new IndexLimitGift();
                    gift.gameName = "游戏名" + i;
                    gift.name = "传奇礼包";
                    gift.img = "http://owan-img.ymapp.com/app/4726/icon/icon_1427963355.png_140_140_100.png";
                    gift.remainCount = i * 10 + i;
                    mLimitDatas.add(gift);
                    IndexNewGift ngift = new IndexNewGift();
                    ngift.gameName = "游戏名" + i;
                    mNewDatas.add(ngift);
                }
                mHandler.sendEmptyMessage(1);

            }
        }).start();*/
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

}
