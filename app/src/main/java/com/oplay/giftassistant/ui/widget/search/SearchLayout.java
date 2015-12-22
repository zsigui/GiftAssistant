package com.oplay.giftassistant.ui.widget.search;

import android.content.Context;
import android.os.Build;
import android.support.annotation.IdRes;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.model.data.resp.SearchPromptResult;
import com.oplay.giftassistant.util.InputMethodUtil;
import com.socks.library.KLog;

import java.util.List;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/20
 */
public class SearchLayout extends RelativeLayout implements AdapterView.OnItemClickListener, TextView.OnEditorActionListener, TextWatcher, View.OnClickListener {

    protected SearchDataAdapter<SearchPromptResult> mBaseAdapter;
    protected AutoCompleteTextView mEdtSearch;
    protected TextView mIconSearch;
    protected TextView mIconClear;
    protected TextView mDivider;
    protected String mCurKeyWord;
    protected boolean mIsAutoPopupPromt;
    private List<String> mKeyWordList;
    private OnSearchActionListener mSearchActionListener;
    /**
     * defined whether need to auto send request to get prompt list
     */
    private boolean mAutoSendRequest;
    /**
     * this list view is used to display the input history<br />
     * <b>Note : null means that you don't need display history<b/>
     */
    private ListView mHistoryView;
    /**
     * this list view is used to display the auto complete hint<br />
     * <b>Note : null means that you don't need display the auto complete hint<b/>
     */
    private ListView mPromptView;
    /**
     *
     */
    private View T;

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
        mIconSearch = getViewById(R.id.tv_search_icon);
        mDivider = getViewById(R.id.tv_search_divider);
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
        mIconSearch.setOnClickListener(this);
    }

    private <V extends View> V getViewById(@IdRes int id) {
        View child = findViewById(id);
        return (child != null ? (V) child : null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_search_icon:
                sendSearchRequest();
                break;
            case R.id.tv_search_clear:
                doClearAction();
                break;
        }
    }

	public OnSearchActionListener getSearchActionListener() {
		return mSearchActionListener;
	}

	public void setSearchActionListener(OnSearchActionListener searchActionListener) {
		mSearchActionListener = searchActionListener;
	}

	private void sendSearchRequest() {
        mCurKeyWord = mEdtSearch.getText().toString().trim();
        if (TextUtils.isEmpty(mCurKeyWord)) {
            return;
        }
        if (mSearchActionListener != null) {
            mEdtSearch.dismissDropDown();
            InputMethodUtil.hideSoftInput(mEdtSearch);
            mSearchActionListener.onSearchPerform(mCurKeyWord);
        }
    }

    /**
     * clear the content of the ACTextView, and change some icons state and call onSearchCleared()
     */
    private void doClearAction() {
        mCurKeyWord = "";
        mEdtSearch.setText(mCurKeyWord);
        mEdtSearch.requestFocus();
        mIconSearch.setEnabled(false);
        mIconClear.setVisibility(View.GONE);
        if (mDivider != null) {
            mDivider.setVisibility(View.GONE);
        }
        if (mPromptView != null) {
            mPromptView.setVisibility(View.GONE);
        }
        if (mHistoryView != null) {
            mHistoryView.setVisibility(View.VISIBLE);
        }
        if (mSearchActionListener != null) {
            mSearchActionListener.onSearchCleared();
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position < mKeyWordList.size()) {
            final String keyword = mKeyWordList.get(position);
            if (mSearchActionListener != null) {
                mSearchActionListener.onSearchPerform(keyword);
            }
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            sendSearchRequest();
            return true;
        }
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mCurKeyWord = s.toString().trim();
        if (TextUtils.isEmpty(mCurKeyWord)) {
            doClearAction();
            return;
        }
        if (mIsAutoPopupPromt && mPromptView != null) {
            sendPromptRequest(mCurKeyWord);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    public void sendPromptRequest(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            KLog.w("the keyword is null or empty");
            return;
        }
        if (mSearchActionListener != null) {
            mSearchActionListener.onSearchPromptPerform(keyword);
        }
    }

	public String getKeyword() {
		mCurKeyWord = mEdtSearch.getText().toString().trim();
		return mCurKeyWord;
	}

    /**
     *
     */
    public interface OnSearchActionListener {

        /**
         * pass search
         *
         * @param keyword used to be searched
         */
        void onSearchPerform(String keyword);

        void onSearchCleared();

        void onSearchPromptPerform(String keyword);
    }
}
