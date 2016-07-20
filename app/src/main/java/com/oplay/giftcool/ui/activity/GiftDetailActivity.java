package com.oplay.giftcool.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.listener.OnHandleListener;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.sharesdk.ShareSDKConfig;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.gift.GiftDetailFragment;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ToastUtil;

/**
 * Created by zsigui on 15-12-31.
 */
public class GiftDetailActivity extends BaseAppCompatActivity {

    private TextView tvTag;
    private ImageView ivShare;
    private OnHandleListener mOnHandleListener;
    private int mId;
    private GiftDetailFragment mDetailFragment;

    @Override
    protected void processLogic() {
        loadData(getIntent());
    }

    @Override
    protected void initView() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_toolbar_with_share);
    }

    @Override
    protected void initMenu(@NonNull Toolbar toolbar) {
        super.initMenu(toolbar);
        tvTag = getViewById(toolbar, R.id.tv_tool_tag);
        ivShare = getViewById(toolbar, R.id.iv_bar_share);
        if (ivShare != null) {
            ivShare.setOnClickListener(this);
        }
        showLimitTag(false, 0);
    }

    public void loadData(Intent intent) {
        if (intent == null) {
            ToastUtil.showShort(ConstString.TOAST_WRONG_PARAM);
            return;
        }

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // 来自浏览器的URI请求
//            Uri uri = intent.getData();
//            if (uri != null) {
//                try {
//                    mId = Integer.parseInt(uri.getQueryParameter("plan_id"));
//                } catch (Exception e) {
//                    mId = 0;
//                    ToastUtil.showShort(ConstString.TOAST_WRONG_PARAM);
//                    onBackPressed();
//                    return;
//                }
//            }
        } else {
            mId = intent.getIntExtra(KeyConfig.KEY_DATA, KeyConfig.TYPE_ID_DEFAULT);
        }
        if (mDetailFragment == null) {
            mDetailFragment = GiftDetailFragment.newInstance(mId);
            replaceFrag(R.id.fl_container, mDetailFragment, false);
        } else {
            mDetailFragment.updateData(mId);
        }
    }

    public void setOnHandleListener(OnHandleListener shareClickListener) {
        mOnHandleListener = shareClickListener;
    }

    public void showLimitTag(boolean isShow, @StringRes int textId) {
        if (tvTag != null) {
            if (isShow) {
                tvTag.setVisibility(View.VISIBLE);
                tvTag.setText(textId);
            } else {
                tvTag.setVisibility(View.GONE);
            }
        }
    }

    public void showShareButton(boolean isShow) {
        if (ivShare != null) {
            if (isShow) {
                ivShare.setVisibility(View.VISIBLE);
            } else {
                ivShare.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        loadData(intent);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.iv_bar_share) {
            if (mOnHandleListener != null) {
                mOnHandleListener.deal();
            } else {
                ToastUtil.showShort(ConstString.TOAST_SHARE_NO_OFFER);
            }
        }
    }

    @Override
    public void release() {
        super.release();
        mOnHandleListener = null;
        if (tvTag != null && tvTag.getBackground() != null) {
            tvTag.getBackground().setCallback(null);
        }
        tvTag = null;
        ivShare = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AppDebugConfig.d(AppDebugConfig.TAG_ACTIVITY, "requestCode = " + requestCode + ", resultCode = "
                + resultCode + ", data = " + data);
        if (requestCode == ShareSDKConfig.SHARE_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    ScoreManager.getInstance().setTaskFinished(true);
                    ScoreManager.getInstance().reward(null, true);
                    break;
                case Activity.RESULT_CANCELED:
                    break;
                default:
                    ToastUtil.showShort(ConstString.TOAST_SHARE_FAILED);
            }
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
