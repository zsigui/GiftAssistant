package com.oplay.giftcool.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.engine.NoEncryptEngine;
import com.oplay.giftcool.ext.retrofit2.DefaultGsonConverterFactory;
import com.oplay.giftcool.listener.OnBackPressListener;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_WebView;
import com.oplay.giftcool.ui.fragment.postbar.PostCommentFragment;
import com.oplay.giftcool.ui.fragment.postbar.PostDetailFragment;
import com.oplay.giftcool.util.InputMethodUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;

/**
 * Created by zsigui on 16-4-11.
 */
public class PostDetailActivity extends BaseAppCompatActivity {

    private NoEncryptEngine mEngine;

    private List<Integer> mTypeHierarchy;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_web);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void processLogic() {
        mTypeHierarchy = new ArrayList<>(5);
        mEngine = new Retrofit.Builder()
                .baseUrl(NetUrl.getBaseUrl())
                .client(AssistantApp.getInstance().getHttpClient())
                .addConverterFactory(DefaultGsonConverterFactory.create(AssistantApp.getInstance().getGson()))
                .build()
                .create(NoEncryptEngine.class);
        handleRedirect(getIntent());
    }

    @Override
    protected void initMenu(@NonNull Toolbar toolbar) {
        super.initMenu(toolbar);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_bar_back:
                finish();
                break;
        }
    }

    private void handleRedirect(Intent intent) {

        if (intent == null) {
            ToastUtil.showShort("跳转出错");
            AppDebugConfig.d(AppDebugConfig.TAG_ACTIVITY, "no intent");
            return;
        }
        int type = intent.getIntExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_DEFAULT);
        if (type == KeyConfig.TYPE_ID_DEFAULT) {
            AppDebugConfig.d(AppDebugConfig.TAG_ACTIVITY, "no type");
            ToastUtil.showShort("跳转出错");
            return;
        }
        mTypeHierarchy.add(type);
        final int postId = intent.getIntExtra(KeyConfig.KEY_DATA, 0);
        switch (type) {
            case KeyConfig.TYPE_ID_POST_REPLY_DETAIL:
                replaceFragWithTitle(R.id.fl_container, PostDetailFragment.newInstance(postId), "", true);
                break;
            case KeyConfig.TYPE_ID_POST_COMMENT_DETAIL:
                final int commentId = intent.getIntExtra(KeyConfig.KEY_DATA_O, 0);
                replaceFragWithTitle(R.id.fl_container, PostCommentFragment.newInstance(postId, commentId), "", true);
                break;
            default:
                mTypeHierarchy.remove(mTypeHierarchy.size() - 1);
                AppDebugConfig.d(AppDebugConfig.TAG_ACTIVITY, "type = " + type);
                ToastUtil.showShort("跳转出错");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (mTypeHierarchy == null) {
            mTypeHierarchy = new ArrayList<>();
        }
        int type = mTypeHierarchy.size() == 0 ?
                KeyConfig.TYPE_ID_DEFAULT : mTypeHierarchy.get(mTypeHierarchy.size() - 1);
        if (intent != null
                && type != KeyConfig.TYPE_ID_DEFAULT
                && type != intent.getIntExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_DEFAULT)) {
            handleRedirect(intent);
        }
    }

    /**
     * 重新复写处理后退事件
     */
    @Override
    public boolean onBack() {
        InputMethodUtil.hideSoftInput(this);
        if (getTopFragment() != null && getTopFragment() instanceof OnBackPressListener
                && ((OnBackPressListener) getTopFragment()).onBack()) {
            // back事件被处理
            return false;
        }
        if (!popFrag() && !isFinishing()) {
            mNeedWorkCallback = false;
            if (MainActivity.sGlobalHolder == null) {
                IntentUtil.jumpHome(this, false);
            }
            if (BaseFragment_WebView.sScrollMap != null) {
                BaseFragment_WebView.sScrollMap.clear();
            }
            finish();
        } else {
            if (getTopFragment() instanceof BaseFragment) {
                setBarTitle(((BaseFragment) getTopFragment()).getTitleName());
            }
            if (mTypeHierarchy != null && mTypeHierarchy.size() > 0) {
                mTypeHierarchy.remove(mTypeHierarchy.size() - 1);
            }
        }
        return true;
    }

    public NoEncryptEngine getEngine() {
        return mEngine;
    }

}
