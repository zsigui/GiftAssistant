package com.oplay.giftcool.ui.widget.button;

/**
 * Created by zsigui on 16-1-5.
 */

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.model.AppStatus;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.util.ViewUtil;


public class DownloadButtonView extends RelativeLayout {

    private TextView mDownloadTextView;
    private ProgressBar mProgressBar;
    private AppStatus mAppStatus;
    private String mCurStateText = "下载";
    private int mCurStateBackground = R.drawable.selector_btn_download_green;
    private int mCurDownloadProgress;
    private String mProgressText;
    private String mDisableText = "无效";

    public DownloadButtonView(Context context) {
        this(context, null);
    }

    public DownloadButtonView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DownloadButtonView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View v = LayoutInflater.from(getContext()).inflate(R.layout.view_download_btn, this, true);
        mProgressBar = ViewUtil.getViewById(v, R.id.pb_download);
        mDownloadTextView = ViewUtil.getViewById(v, R.id.btn_download_text);
        if (mProgressBar != null) {
            mProgressBar.setMax(100);
        }
        mProgressText = "%d%%";
        setProgressVisible(true);
    }

    public void setDisableText(String disableText) {
        mDisableText = disableText;
    }

    public void setText(CharSequence charSequence) {
        mDownloadTextView.setText(charSequence);
        mProgressBar.setVisibility(GONE);
    }

    public void setText(int stringId) {
        final String s = getResources().getString(stringId);
        setText(s);
    }

    public AppStatus handleOnClick() {
        try {
            switch (mAppStatus) {
                case DOWNLOADABLE:
                case RETRYABLE:
                case RESUMABLE:
                    return setStatus(AppStatus.PAUSABLE, "");
                case PAUSABLE:
                    return setStatus(AppStatus.RESUMABLE, "");
                case UPDATABLE:
                case INSTALLABLE:
                case OPENABLE:
                case DISABLE:
                default:
                    break;
            }
        } catch (Exception e) {
            AppDebugConfig.w(AppDebugConfig.TAG_DEBUG_INFO, e);
        }
        return mAppStatus;
    }

    public AppStatus setStatus(AppStatus status, String apkSize) {
        if (status != null && !status.equals(mAppStatus)) {
            mAppStatus = status;
            switch (mAppStatus) {
                case PAUSABLE:
                    setProgressVisible(true);
//					mCurStateText = String.format("%d%%", mCurDownloadProgress);
                    mCurStateBackground = R.color.co_transparent;
                    break;
                case RETRYABLE:
                    setProgressVisible(false);
                    mCurStateText = "重试";
                    mCurStateBackground = R.drawable.selector_btn_green;
                    break;
                case RESUMABLE:
                    setProgressVisible(true);
                    mCurStateText = "继续";
                    mCurStateBackground = R.color.co_transparent;
                    break;
                case INSTALLABLE:
                    setProgressVisible(false);
                    mCurStateText = "安装";
                    mCurStateBackground = R.drawable.selector_btn_download_blue;
                    break;
                case OPENABLE:
                    setProgressVisible(false);
                    mCurStateText = "打开";
                    mCurStateBackground = R.drawable.selector_btn_download_blue;
                    break;
                case DISABLE:
                    setProgressVisible(false);
                    mCurStateBackground = R.drawable.selector_btn_download_blue;
                    break;
                case UPDATABLE:
                case DOWNLOADABLE:
                    setProgressVisible(false);
                    mCurStateText = String.format("下载 %s", apkSize);
                    mCurStateBackground = R.drawable.selector_btn_download_green;
                    break;
            }
            setEnabled(mAppStatus != AppStatus.DISABLE);
            if (AppStatus.DISABLE.equals(mAppStatus) && !TextUtils.isEmpty(mDisableText)) {
                mDownloadTextView.setText(mDisableText);
            } else {
                mDownloadTextView.setText(mCurStateText);
            }
            mDownloadTextView.setBackgroundResource(mCurStateBackground);
            setProgress(mCurDownloadProgress);
        }
        return mAppStatus;
    }

    public void setProgress(int progress) {
        if (mAppStatus != AppStatus.PAUSABLE && mAppStatus != AppStatus.RESUMABLE) {
            return;
        }
        if (progress == 0) {
            progress = mCurDownloadProgress == 0 ? IndexGameNew.FAKE_INIT_PROGRESS : mCurDownloadProgress;
        }
        mCurDownloadProgress = progress;
        mProgressBar.setProgress(progress);
        if (mAppStatus.equals(AppStatus.PAUSABLE) && mDownloadTextView != null) {
            mDownloadTextView.setText(String.format(mProgressText, progress));
        }
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void setProgressVisible(boolean isVisible) {
        if (mProgressBar != null) {
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

}

