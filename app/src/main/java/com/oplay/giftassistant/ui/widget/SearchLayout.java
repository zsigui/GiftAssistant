package com.oplay.giftassistant.ui.widget;

import android.content.Context;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.SearchDataAdapter;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.engine.SearchEngine;
import com.oplay.giftassistant.model.data.SearchPromptResult;
import com.oplay.giftassistant.model.json.base.JsonBaseImpl;
import com.socks.library.KLog;

import java.util.List;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/20
 */
public class SearchLayout extends RelativeLayout implements AdapterView.OnItemClickListener, TextView.OnEditorActionListener, TextWatcher {

    public class ItemType {
        public static final int GIFT = 0;
        public static final int GIFT_PROMPT = 1;
        public static final int GAME = 10;
        public static final int GAME_PROMPT = 11;
        public static final int MIX = 20;
        public static final int MIX_PROMPT = 21;
    }
    protected SearchDataAdapter<SearchPromptResult> mBaseAdapter;
    protected AutoCompleteTextView mEdtSearch;
    protected ImageView mIconSearch;
    protected View mDivider;
    protected String mCurKeyWord;
    protected boolean mIsAutoPopupPromt;
    private boolean mIsAutoSendRequest;
    private List<String> mKeyWordList;
    private ItemType mItemType;
    private SearchEngine mEngine;

    public SearchLayout(Context context) {
        this(context, null);
    }

    public SearchLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mEdtSearch = getViewById(R.id.actv_search_input);
        mIconSearch = getViewById(R.id.iv_search_icon);
        mDivider = getViewById(R.id.iv_search_divider);
        if (Build.VERSION.SDK_INT >= 17) {
            mEdtSearch.setOnDismissListener(new AutoCompleteTextView.OnDismissListener() {
                @Override
                public void onDismiss() {
                    if (mKeyWordList != null) {
                        mKeyWordList.clear();
                    }
                }
            });
        }
        mEdtSearch.setOnItemClickListener(this);
        mEdtSearch.setOnEditorActionListener(this);
        mEdtSearch.addTextChangedListener(this);
    }

    private <V extends View> V getViewById(@IdRes int id) {
        View child = findViewById(id);
        return (child != null ? (V)child : null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public void sendRequest(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            KLog.w("the keyword is null or empty");
            return;
        }
        if (mEngine == null) {
            KLog.e("mEngine is null");
            throw new RuntimeException("You need to set a engine before call this, please use setEngine() first");
        }
        mEngine.getSearchPromt(keyword).enqueue(new Callback<JsonBaseImpl<SearchPromptResult>>() {
            @Override
            public void onResponse(Response<JsonBaseImpl<SearchPromptResult>> response, Retrofit retrofit) {
                if (response != null && response.code() == 200) {
                    mBaseAdapter.doWithPrompt(response.body());
                } else {
                    if (AppDebugConfig.IS_DEBUG) {
                        KLog.d(AppDebugConfig.TAG_SEARCH, response);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (AppDebugConfig.IS_DEBUG) {
                    KLog.d(AppDebugConfig.TAG_SEARCH, t);
                }
            }
        });
    }

    public SearchEngine getEngine() {
        return mEngine;
    }

    public void setEngine(@NonNull SearchEngine engine) {
        mEngine = engine;
    }
}
