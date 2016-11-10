package com.oplay.giftcool.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.listener.OnBackPressListener;
import com.oplay.giftcool.listener.OnHandleListener;
import com.oplay.giftcool.listener.ToolbarListener;
import com.oplay.giftcool.model.data.resp.IndexPostNew;
import com.oplay.giftcool.model.data.resp.PostDetail;
import com.oplay.giftcool.sharesdk.ShareSDKManager;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.postbar.PostDetailNewFragment;
import com.oplay.giftcool.util.InputMethodUtil;
import com.oplay.giftcool.util.ToastUtil;

import java.util.regex.Pattern;

/**
 * Created by zsigui on 16-11-2.
 */
public class PostDetailNewActivity extends BaseAppCompatActivity implements ToolbarListener {
    private int mPostId;
    private IndexPostNew postData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        super.onCreate(savedInstanceState);
    }

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
        mPostId = intent.getIntExtra(KeyConfig.KEY_DATA, 0);
        replaceFragWithTitle(R.id.fl_container, PostDetailNewFragment.newInstance(mPostId),
                "官方活动详情");
    }

    private ImageView ivShare;

    public void setPostId(int postId) {
        mPostId = postId;
    }

    private void iniToolRight() {
        if (ivShare == null) {
            ViewStub v = getViewById(mToolbar, R.id.vs_bar_right);
            if (v != null) {
                v.inflate();
                getViewById(R.id.btn_bar_right).setVisibility(View.GONE);
                ivShare = getViewById(R.id.iv_bar_share);
                ivShare.setOnClickListener(this);
            }
        }
    }

    public void showShareBtn(int visibility, PostDetail data) {
        if (mToolbar == null)
            return;
        iniToolRight();
        if (ivShare != null) {
            ivShare.setVisibility(visibility);
            if (data != null) {
                postData = new IndexPostNew();
                postData.id = data.postInfo.id;
                String content =  data.postInfo.content;
                Pattern p = Pattern.compile("<[^>]+>");
                content = p.matcher(content).replaceAll("").trim();
                postData.content = content;
                postData.img = data.postInfo.img;
                postData.title = data.postInfo.title;
            }
        }
    }

    @Override
    public void showRightBtn(int visibility, String text) {
        if (mToolbar == null)
            return;
        iniToolRight();
    }

    @Override
    public void setRightBtnEnabled(boolean enabled) {
    }

    @Override
    public void setHandleListener(OnHandleListener handleListener) {
    }

    @Override
    public void onBackPressed() {
        if (getTopFragment() != null && getTopFragment() instanceof OnBackPressListener
                && ((OnBackPressListener) getTopFragment()).onBack()) {
            // back事件被处理
            return;
        }
        InputMethodUtil.hideSoftInput(this);
        finish();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_bar_share:
                ShareSDKManager.getInstance(this)
                        .shareActivity(getApplicationContext(), getSupportFragmentManager(), postData);
                break;
        }
    }
}