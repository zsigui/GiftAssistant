package com.oplay.giftcool.ui.activity;

import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.gift.GiftFreeFragment;
import com.oplay.giftcool.ui.fragment.gift.GiftLikeFragment;
import com.oplay.giftcool.ui.fragment.gift.GiftLimitFragment;
import com.oplay.giftcool.ui.fragment.gift.GiftNewFragment;
import com.oplay.giftcool.util.IntentUtil;

/**
 * Created by zsigui on 15-12-29.
 */
public class GiftListActivity extends BaseAppCompatActivity {

    private int type = 0;

    @Override
    @SuppressWarnings("unchecked")
    protected void initView() {
        setContentView(R.layout.activity_common_with_back_white_bg);

        if (getIntent() != null) {
            type = getIntent().getIntExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_DEFAULT);
        }
    }

    @Override
    protected void initMenu(@NonNull Toolbar toolbar) {
        super.initMenu(toolbar);
        switch (type) {
            case KeyConfig.TYPE_ID_GIFT_LIMIT:
                setBarTitle("珍贵限量礼包");
                break;
            case KeyConfig.TYPE_ID_GIFT_NEW:
                setBarTitle("新鲜出炉礼包");
                break;
            case KeyConfig.TYPE_ID_GIFT_LIKE:
                setBarTitle("猜你喜欢");
                break;
            case KeyConfig.TYPE_ID_GIFT_FREE:
                setBarTitle("限时免费礼包");
                break;
        }

    }

    @Override
    protected void processLogic() {
        mNeedWorkCallback = true;
        loadData();
    }

    public void loadData() {
        if (mIsLoading) {
            return;
        }
        mIsLoading = true;
        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                switch (type) {
                    case KeyConfig.TYPE_ID_GIFT_FREE:
                        displayGiftFreeUI();
                        break;
                    case KeyConfig.TYPE_ID_GIFT_LIMIT:
                        displayGiftLimitUI();
                        break;
                    case KeyConfig.TYPE_ID_GIFT_NEW:
                        displayGiftNewUI();
                        break;
                    case KeyConfig.TYPE_ID_GIFT_LIKE:
                        displayGiftLikeUI();
                        break;
                }
            }
        });
    }

    private void displayGiftFreeUI() {
        replaceFrag(R.id.fl_container, GiftFreeFragment.newInstance(),
                GiftFreeFragment.class.getSimpleName(), false);
    }

    private void displayGiftNewUI() {
        replaceFrag(R.id.fl_container, GiftNewFragment.newInstance(),
                GiftNewFragment.class.getSimpleName(), false);
    }


    private void displayGiftLikeUI() {
        replaceFrag(R.id.fl_container, GiftLikeFragment.newInstance(),
                GiftLikeFragment.class.getSimpleName(), false);
    }

    private void displayGiftLimitUI() {
        replaceFrag(R.id.fl_container, GiftLimitFragment.newInstance(),
                GiftLimitFragment.class.getSimpleName(), false);
    }

    @Override
    protected void doBeforeFinish() {
        super.doBeforeFinish();
        if (MainActivity.sGlobalHolder == null) {
            IntentUtil.jumpHome(this, false);
        }
    }

    @Override
    public void release() {
        super.release();
    }
}
