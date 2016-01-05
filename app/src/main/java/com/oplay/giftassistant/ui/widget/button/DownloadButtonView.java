package com.oplay.giftassistant.ui.widget.button;

/**
 * Created by zsigui on 16-1-5.
 */

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.socks.library.KLog;


public class DownloadButtonView extends RelativeLayout {

	public static final int DOWNLOADABLE = 0;
	public static final int PAUSABLE = 1;
	public static final int INSTALLABLE = 2;
	public static final int UPDATABLE = 3;
	public static final int OPENABLE = 4;
	public static final int RESUMABLE = 5;
	public static final int RETRYABLE = 6;
	public static final int DISABLE = 7;

	private TextView mDownloadTextView;
	private ProgressBar mProgressBar;
	private int mCurState = DOWNLOADABLE;
	private int mCurStateColor = R.color.co_white;
	private int mCurStateText = R.string.st_game_download;
	private int mCurStateBackground = R.drawable.selector_btn_green;
	private int mCurDownloadProgress;
	private String mProgressText;
	private String mDisableText = "";

	public DownloadButtonView(Context context) {
		super(context);
	}

	public DownloadButtonView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DownloadButtonView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mProgressBar = (ProgressBar) findViewById(R.id.pb_download);
		mDownloadTextView = (TextView) findViewById(R.id.btn_download_text);
		mProgressText = "";
	}

	public void setDisableText(String disableText) {
		mDisableText = disableText;
	}

	public int handleOnClick() {
		try {
			switch (mCurState) {
				case DOWNLOADABLE:
				case RETRYABLE:
				case RESUMABLE:
					return setStatus(PAUSABLE);
				case PAUSABLE:
					return setStatus(RESUMABLE);
				case UPDATABLE:
				case INSTALLABLE:
				case OPENABLE:
				case DISABLE:
				default:
					break;
			}
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
		return mCurState;
	}

	public int setStatus(int status) {
		if (status != mCurState) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_UTIL, "DownloadButtonView set AppStatus to " + status);
			}
			mCurState = status;
			switch (mCurState) {
				case PAUSABLE:
					setProgressVisible(true);
					mCurStateText = R.string.st_game_pause;
					mCurStateBackground = R.drawable.selector_btn_green;
					break;
				case RETRYABLE:
					setProgressVisible(false);
					mCurStateText = R.string.st_game_retry;
					mCurStateBackground = R.drawable.selector_btn_red;
					break;
				case RESUMABLE:
					setProgressVisible(true);
					mCurStateText = R.string.st_game_continue;
					mCurStateBackground = R.drawable.selector_btn_green;
					break;
				case INSTALLABLE:
					setProgressVisible(false);
					mCurStateText = R.string.st_game_install;
					mCurStateBackground = R.drawable.selector_btn_blue;
					break;
				case OPENABLE:
					setProgressVisible(false);
					mCurStateText = R.string.st_game_open;
					mCurStateBackground = R.drawable.selector_btn_blue;
					break;
				case UPDATABLE:
					setProgressVisible(false);
					mCurStateText = R.string.st_game_update;
					mCurStateBackground = R.drawable.selector_btn_blue;
					break;
				case DISABLE:
					setProgressVisible(false);
					mCurStateBackground = R.drawable.shape_rect_btn_grey;
					break;
				case DOWNLOADABLE:
					setProgressVisible(false);
					mCurStateText = R.string.st_game_download;
					mCurStateBackground = R.drawable.selector_btn_green;
					break;
			}
		}
		setEnabled(mCurState != DISABLE);
		if (mCurState == DISABLE && !TextUtils.isEmpty(mDisableText)) {
			mDownloadTextView.setText(mDisableText);
		} else {
			mDownloadTextView.setText(mCurStateText);
		}
		mDownloadTextView.setTextColor(getResources().getColorStateList(mCurStateColor));
		setBackgroundResource(mCurStateBackground);
		setProgress(mCurDownloadProgress);
		return mCurState;
	}

	public void setProgress(int progress) {
		if (mCurState != PAUSABLE) {
			return;
		}
		if (progress == 0) {
			progress = 0;
		}
		if (progress >= mCurDownloadProgress) {
			mCurDownloadProgress = progress;
			mProgressBar.setProgress(progress);
			mDownloadTextView.setText(String.format(mProgressText, mCurDownloadProgress));
		}
		mProgressBar.setVisibility(View.VISIBLE);
	}

	private void setProgressVisible(boolean isVisible) {
		final int visibilityToSet = isVisible ? View.VISIBLE : View.GONE;
		if (visibilityToSet == mProgressBar.getVisibility()) {
			return;
		}
		if (isVisible) {
			mProgressBar.setProgress(mCurDownloadProgress);
			mProgressBar.setVisibility(View.VISIBLE);
		} else {
			mProgressBar.setVisibility(View.INVISIBLE);
		}
	}

}

