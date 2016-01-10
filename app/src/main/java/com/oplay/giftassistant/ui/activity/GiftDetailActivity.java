package com.oplay.giftassistant.ui.activity;

import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.KeyConfig;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.gift.GiftDetailFragment;

/**
 * Created by zsigui on 15-12-31.
 */
public class GiftDetailActivity extends BaseAppCompatActivity {


    private int detailId;
    private String detailName;


    @Override
    protected void processLogic() {
        displayLoadingUI(R.id.fl_container);

        loadData();
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_common_with_back);

        if (getIntent() != null) {
            detailId = getIntent().getIntExtra(KeyConfig.KEY_DATA, KeyConfig.TYPE_ID_DEFAULT);
            detailName = getIntent().getStringExtra(KeyConfig.KEY_NAME);
        }
    }

    @Override
    protected void initMenu(@NonNull Toolbar toolbar) {
        super.initMenu(toolbar);
        setBarTitle(detailName);
    }

    public void loadData() {
	    displayGiftDetailUI();
    }

    private void displayGiftDetailUI() {
        replaceFrag(R.id.fl_container, GiftDetailFragment.newInstance(detailId),
                GiftDetailFragment.class.getSimpleName(), false);
    }
}
