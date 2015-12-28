package com.oplay.giftassistant.ui.widget.search;

import android.content.Context;
import android.support.annotation.IdRes;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.util.InputMethodUtil;
import com.socks.library.KLog;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/20
 */
public class SearchLayout extends LinearLayout implements TextView.OnEditorActionListener, TextWatcher, View.OnClickListener {

	protected AutoCompleteTextView mEdtSearch;
	protected TextView mIconSearch;
	protected TextView mIconClear;
	protected TextView mDivider;
	protected String mCurKeyWord;
	protected boolean mCanGetFocus = false;
    private boolean mShowClear = false;
	private OnSearchActionListener mSearchActionListener;
	/**
	 * defined whether need to auto send search request to get prompt list
	 */
	protected boolean mIsAutoPopupPrompt;
	/**
	 * defined whether need to auto send search request to get result list <br/>
	 * Note: it will disable {@code mIsAutoPopupPrompt}
	 */
	private boolean mIsAutoSendRequest;

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
        if (mShowClear) {
            mIconClear = getViewById(R.id.tv_search_clear);
            mDivider = getViewById(R.id.tv_search_divider);
        }
		mEdtSearch.setOnEditorActionListener(this);
		mEdtSearch.addTextChangedListener(this);
		mEdtSearch.setOnClickListener(this);
		mIconSearch.setOnClickListener(this);
		if (mIconClear != null) {
            mIconClear.setVisibility(View.GONE);
			mIconClear.setOnClickListener(this);
		}
        if (mDivider != null) {
            mDivider.setVisibility(View.GONE);
        }
	}

	private <V extends View> V getViewById(@IdRes int id) {
		View child = findViewById(id);
		return (child != null ? (V) child : null);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.actv_search_input:
				if (mCanGetFocus) {
					mEdtSearch.requestFocus();
					InputMethodUtil.showSoftInput(this);
				}
				break;
			case R.id.tv_search_icon:
				sendSearchRequest(mCurKeyWord);
				break;
			case R.id.tv_search_clear:
				mEdtSearch.setText("");
				break;
		}
	}

	public OnSearchActionListener getSearchActionListener() {
		return mSearchActionListener;
	}

	public void setSearchActionListener(OnSearchActionListener searchActionListener) {
		mSearchActionListener = searchActionListener;
	}

	public void sendSearchRequest(String keyword) {
		if (TextUtils.isEmpty(mCurKeyWord)) {
			KLog.w("the keyword is null or empty");
			return;
		}
		if (mSearchActionListener != null) {
			InputMethodUtil.hideSoftInput(mEdtSearch);
			mSearchActionListener.onSearchPerform(mCurKeyWord);
		}
	}

	/**
	 * clear the content of the ACTextView, and change some icons state and call onSearchCleared()
	 */
	private void doClearAction() {
		mEdtSearch.requestFocus();
		mIconSearch.setEnabled(false);
		if (mIconClear != null) {
			mIconClear.setVisibility(View.GONE);
		}
		if (mDivider != null) {
			mDivider.setVisibility(View.GONE);
		}
		if (mSearchActionListener != null) {
			mSearchActionListener.onSearchCleared();
		}
	}

	private void enableAction() {
		mIconSearch.setEnabled(true);
		if (mIconClear != null) {
			mIconClear.setVisibility(View.VISIBLE);
		}
		if (mDivider != null) {
			mDivider.setVisibility(View.VISIBLE);
		}
		if (mIsAutoSendRequest) {
			sendSearchRequest(mCurKeyWord);
			return;
		}
		if (mIsAutoPopupPrompt) {
			sendPromptRequest(mCurKeyWord);
		}
	}

	@Override
	public void clearFocus() {
		super.clearFocus();
		mEdtSearch.clearFocus();
	}

	public boolean isCanGetFocus() {
		return mCanGetFocus;
	}

	public void setCanGetFocus(boolean canGetFocus) {
		mCanGetFocus = canGetFocus;
		if (!mCanGetFocus) {
			mEdtSearch.clearFocus();
			mEdtSearch.setFocusable(false);
			mEdtSearch.setFocusableInTouchMode(false);
			mEdtSearch.setClickable(false);
			mEdtSearch.setOnClickListener(null);
		} else {
			mEdtSearch.setEnabled(true);
			mEdtSearch.setFocusable(true);
			mEdtSearch.setClickable(true);
			mEdtSearch.setOnClickListener(this);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return !mCanGetFocus || super.onInterceptTouchEvent(ev);
	}


	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (AppDebugConfig.IS_DEBUG) {
			KLog.v();
		}
		if (actionId == EditorInfo.IME_ACTION_SEARCH) {
			sendSearchRequest(mCurKeyWord);
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
		enableAction();

	}

	@Override
	public void afterTextChanged(Editable s) {
	}

	public void sendPromptRequest(String keyword) {
		if (TextUtils.isEmpty(keyword)) {
			return;
		}
		if (mSearchActionListener != null) {
			mSearchActionListener.onSearchPromptPerform(keyword);
		}
	}

	public String getKeyword() {
		return mCurKeyWord;
	}

	public boolean isAutoSendRequest() {
		return mIsAutoSendRequest;
	}

	public void setIsAutoSendRequest(boolean isAutoSendRequest) {
		mIsAutoSendRequest = isAutoSendRequest;
	}

	public void setText(String keyword) {
		mEdtSearch.setText(keyword);
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
