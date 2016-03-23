package com.oplay.giftcool.ui.fragment.dialog;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.listener.WebViewInterface;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog_NoButton;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.SystemUtil;
import com.socks.library.KLog;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.io.File;

/**
 * Created by zsigui on 16-1-29.
 */
public class WebViewDialog extends BaseFragment_Dialog_NoButton {

    private final static String KEY_TITLE = "title";
    private final static String KEY_URL = "url";
    String mTitle;
    String mUrl;
    WebView wv;
    protected ProgressBar mProgressBar;

    protected int mScrollX;
    protected int mScrollY;
    private boolean mInit = false;
    private boolean mIsLoadingFailed;


    public static WebViewDialog newInstance(String title, String url) {
        WebViewDialog dialog = new WebViewDialog();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TITLE, title);
        bundle.putString(KEY_URL, url);
        dialog.setArguments(bundle);
        return dialog;
    }

    @SuppressLint("AddJavascriptInterface")
    @Override
    protected void bindViewWithData(View contentView, @Nullable Bundle savedInstanceState) {
        mProgressBar = (ProgressBar) contentView.findViewById(R.id.pb_percent);
        wv = (WebView) contentView.findViewById(R.id.wv_container);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                onWebPageStarted();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                onWebPageFinished();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                onWebReceivedError();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        wv.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                onWebProgressChangedMethod(newProgress);
            }
        });
        WebSettings mSettings = wv.getSettings();

        //JS监听
        mSettings.setJavaScriptEnabled(true);
        WebViewInterface mJsInterfaceObject = new WebViewInterface(getActivity(), this, wv);
        if (wv != null) {
            wv.addJavascriptInterface(mJsInterfaceObject, "GiftCool");
        }
        mSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        // 缩放
        mSettings.setSupportZoom(false);
        mSettings.setBuiltInZoomControls(false);
        mSettings.setUseWideViewPort(true);
        mSettings.setLoadWithOverviewMode(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        try {
            File cacheDir = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                // enabled flash plugin or other
                mSettings.setPluginState(WebSettings.PluginState.ON);
                cacheDir = getActivity().getExternalCacheDir();
            }
            if (cacheDir == null) {
                cacheDir = getActivity().getCacheDir();
            }
            if (cacheDir != null) {
                if (!NetworkUtil.isAvailable(getContext())) {
                    if (AppDebugConfig.IS_DEBUG) {
                        KLog.d(AppDebugConfig.TAG_WEBVIEW, "cache_mode : load cache else network");
                    }
                    mSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                } else {
                    if (AppDebugConfig.IS_DEBUG) {
                        KLog.d(AppDebugConfig.TAG_WEBVIEW, "cache_mode : load default");
                    }
                    mSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
                }
                mSettings.setAppCachePath(cacheDir.getAbsolutePath());
                mSettings.setAppCacheEnabled(true);
            }
            mSettings.setAllowFileAccess(true);
            mSettings.setDomStorageEnabled(true);
        } catch (Exception e) {
            if (AppDebugConfig.IS_DEBUG) {
                Debug_SDK.e(e);
            }
        }
        final int vc = SystemUtil.getVerCode(getContext());
        final String ua = mSettings.getUserAgentString() + " GIFT_COOL_APP/" + vc;
        mSettings.setUserAgentString(ua);
        wv.loadUrl(mUrl);
    }

    protected void onWebProgressChangedMethod(int i) {
        if (mProgressBar != null) {
            mProgressBar.setProgress(i);
        }
    }

    protected void onWebPageStarted() {
        if (AppDebugConfig.IS_DEBUG) {
            AppDebugConfig.logMethodWithParams(this);
        }
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    protected void onWebReceivedError() {
        if (AppDebugConfig.IS_DEBUG) {
            AppDebugConfig.logMethodWithParams(this);
        }
        mIsLoadingFailed = true;
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    protected void onWebPageFinished() {
        if (AppDebugConfig.IS_DEBUG) {
            AppDebugConfig.logMethodWithParams(this);
        }
        try {
            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.GONE);
            }
            if (mIsLoadingFailed) {
                if (wv != null) {
                    wv.setVisibility(View.GONE);
                }
            } else {
                if (wv != null) {
                    wv.setVisibility(View.VISIBLE);
                }
            }
            // 必须page load finish 才能重新setScrollY
            if (wv != null && mInit) {
                wv.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (wv != null && mScrollX > 0 && mScrollY > 0) {
                            wv.scrollTo(mScrollX, mScrollY);
                        }
                    }
                }, 55);
            }
            mInit = true;
//			mSettings.setBlockNetworkImage(false);
        } catch (Exception e) {
            if (AppDebugConfig.IS_DEBUG) {
                Debug_SDK.e(e);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            if (wv != null) {
                if (wv.canGoBack()) {
                    if ( wv.canGoBack()) {
                        wv.goBack();
                    }
                }
                wv.stopLoading();
                ((ViewGroup)mContentView).removeView(wv);
                wv.removeAllViews();
                wv.destroy();
            }
        } catch (Exception e) {
            if (AppDebugConfig.IS_DEBUG) {
                KLog.e(e);
            }
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            mTitle = args.getString(KEY_TITLE);
            mUrl = args.getString(KEY_URL);
        }
        super.onCreate(savedInstanceState);
    }



    @Override
    protected String getTitle() {
        return mTitle;
    }

    @Override
    protected View getContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(R.layout.dialog_webview, container, false);
    }
}
