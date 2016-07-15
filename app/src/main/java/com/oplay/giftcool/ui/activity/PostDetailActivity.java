package com.oplay.giftcool.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.config.WebViewUrl;
import com.oplay.giftcool.engine.NoEncryptEngine;
import com.oplay.giftcool.ext.retrofit2.DefaultGsonConverterFactory;
import com.oplay.giftcool.listener.OnBackPressListener;
import com.oplay.giftcool.listener.OnHandleListener;
import com.oplay.giftcool.listener.ToolbarListener;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.WebFragment;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_WebView;
import com.oplay.giftcool.ui.fragment.postbar.PostCommentFragment;
import com.oplay.giftcool.ui.fragment.postbar.PostDetailFragment;
import com.oplay.giftcool.util.InputMethodUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.MixUtil;
import com.oplay.giftcool.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;

/**
 * Created by zsigui on 16-4-11.
 */
public class PostDetailActivity extends BaseAppCompatActivity implements ToolbarListener {

    private NoEncryptEngine mEngine;

    private List<Integer> mTypeHierarchy;
    private int mPostId;
    private int identifierId = 100000;

    public int getIdentifierId() {
        return identifierId++;
    }

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

    private OnHandleListener mHandleListener;
    private TextView btnToolRight;

    public void showRightBtn(int visibility, String text) {
        if (mToolbar == null)
            return;
        iniToolRight(text);
        if (btnToolRight != null) {
            btnToolRight.setVisibility(visibility);
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
    public void setRightBtnEnabled(boolean enabled) {
        if (mToolbar == null)
            return;
        iniToolRight("");
        if (btnToolRight != null) {
            btnToolRight.setEnabled(enabled);
        }
    }

    public void setHandleListener(OnHandleListener handleListener) {
        mHandleListener = handleListener;
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
            case R.id.btn_bar_right:
                if (mHandleListener != null) {
                    mHandleListener.deal();
                } else {
                    IntentUtil.jumpPostDetail(this, mPostId);
                }
                break;
        }
    }

    private void handleRedirect(Intent intent) {
        if (intent == null) {
            ToastUtil.showShort(ConstString.TOAST_WRONG_PARAM);
            AppDebugConfig.d(AppDebugConfig.TAG_ACTIVITY, "no intent");
            return;
        }

        int type = 0;
        int commentId = 0;
        String url = WebViewUrl.URL_BASE;
        AppDebugConfig.d(AppDebugConfig.TAG_APP, "action = " + intent.getAction());
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // 来自浏览器的URI请求
            Uri uri = intent.getData();
            AppDebugConfig.d(AppDebugConfig.TAG_APP, "data = " + intent.getData());

            if (uri != null) {
                url = uri.toString();
                final String path = uri.getPath();
                final String detailS = "article/detail";
                if (path.contains(detailS)) {
                    type = KeyConfig.TYPE_ID_POST_REPLY_DETAIL;
                    mPostId = Integer.parseInt(path.substring(detailS.length() + 2, path.length() - 1));
                } else if (path.contains("comment")) {
                    type = KeyConfig.TYPE_ID_POST_COMMENT_DETAIL;
                    mPostId = Integer.parseInt(uri.getQueryParameter("activity_id"));
                    commentId = Integer.parseInt(uri.getQueryParameter("comment_id"));
                } else {
                    if (!url.startsWith("http")) {
                        int index = url.indexOf("://");
                        if (index != -1) {
                            url = "http" + url.substring(index);
                        } else {
                            url = "http://" + url;
                        }
                    }
                    type = KeyConfig.TYPE_ID_DEFAULT;
                }
            }
        } else {
            type = intent.getIntExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_DEFAULT);
            if (type == KeyConfig.TYPE_ID_DEFAULT) {
                AppDebugConfig.d(AppDebugConfig.TAG_ACTIVITY, "no type");
                ToastUtil.showShort(ConstString.TOAST_WRONG_PARAM);
                return;
            }
            commentId = intent.getIntExtra(KeyConfig.KEY_DATA_O, 0);
            mPostId = intent.getIntExtra(KeyConfig.KEY_DATA, 0);
        }
        mTypeHierarchy.add(type);
        switch (type) {
            case KeyConfig.TYPE_ID_POST_REPLY_DETAIL:
                replaceFragWithTitle(R.id.fl_container, PostDetailFragment.newInstance(mPostId),
                        String.valueOf(getIdentifierId()), "");
                break;
            case KeyConfig.TYPE_ID_POST_COMMENT_DETAIL:
                replaceFragWithTitle(R.id.fl_container, PostCommentFragment.newInstance(mPostId, commentId),
                        String.valueOf(getIdentifierId()), "");
                break;
            default:
                try {
                    String host = url.substring(7, url.indexOf("/", 8));
                    if (MixUtil.isAppHost(host)) {
                        replaceFragWithTitle(R.id.fl_container, WebFragment.newInstance(url), "Web");
                    } else {
                        IntentUtil.startBrowser(this, url);
                        onBackPressed();
                    }
                } catch (Throwable t) {
                    AppDebugConfig.w(AppDebugConfig.TAG_APP, t);
                }
//                ToastUtil.showShort(ConstString.TOAST_WRONG_PARAM);
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

    public void setPostId(int postId) {
        mPostId = postId;
    }
}
