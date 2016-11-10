package com.oplay.giftcool.ui.activity;

import android.content.Intent;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.listener.OnHandleListener;
import com.oplay.giftcool.listener.ToolbarListener;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.postbar.CommentDetailFragment;
import com.oplay.giftcool.util.InputMethodUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ToastUtil;

/**
 * Created by zsigui on 16-11-7.
 */

public class CommentDetailActivity extends BaseAppCompatActivity implements ToolbarListener {

    private int mPostId;

    @Override
    protected void processLogic() {
        handleRedirect(getIntent());
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_common_with_back);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleRedirect(intent);
    }

    private void handleRedirect(Intent intent) {
        if (intent == null) {
            ToastUtil.showShort(ConstString.TOAST_WRONG_PARAM);
            AppDebugConfig.d(AppDebugConfig.TAG_ACTIVITY, "no intent");
            return;
        }
        int commentId = intent.getIntExtra(KeyConfig.KEY_DATA_O, 0);
        mPostId = intent.getIntExtra(KeyConfig.KEY_DATA, 0);
        replaceFragWithTitle(R.id.fl_container, CommentDetailFragment.newInstance(mPostId, commentId),
                "评论详情");
    }

    private TextView btnToolRight;

    public void setPostId(int postId) {
        mPostId = postId;
    }

    @Override
    public void showRightBtn(int visibility, String text) {
        if (mToolbar == null)
            return;
        iniToolRight(text);
        if (btnToolRight != null) {
            btnToolRight.setVisibility(visibility);
        }
    }

    @Override
    public void setRightBtnEnabled(boolean enabled) {
        if (mToolbar == null)
            return;
        iniToolRight("");
        if (btnToolRight != null) {
            btnToolRight.setEnabled(enabled);
        }
    }

    private void iniToolRight(String text) {
        if (btnToolRight == null) {
            ViewStub v = getViewById(mToolbar, R.id.vs_bar_right);
            if (v != null) {
                v.inflate();
                btnToolRight = getViewById(R.id.btn_bar_right);
                btnToolRight.setOnClickListener(this);
                btnToolRight.setText(text);
            }
        }
    }

    @Override
    public void setHandleListener(OnHandleListener handleListener) {}

    @Override
    public void onBackPressed() {
        InputMethodUtil.hideSoftInput(this);
        finish();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_bar_right:
                IntentUtil.jumpPostDetail(this, mPostId);
                finish();
                break;
        }
    }
}
