package com.oplay.giftcool.ui.fragment.base;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.config.WebViewUrl;
import com.oplay.giftcool.listener.OnBackPressListener;
import com.oplay.giftcool.listener.WebViewInterface;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.SystemUtil;
import com.socks.library.KLog;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.io.File;

/**
 * Created by zsigui on 16-1-14.
 */
public abstract class BaseFragment_WebView extends BaseFragment implements DownloadListener, OnBackPressListener {

	protected WebView mWebView;
	private WebSettings mSettings;
	protected WebViewInterface mJsInterfaceObject;
	protected ProgressBar mProgressBar;

	protected int mScrollX;
	protected int mScrollY;
	private boolean mInit = false;
	private boolean mIsLoadingFailed;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		initWebView();
		return mContentView;
	}

	private void initWebView() {
		mProgressBar = getViewById(R.id.pb_percent);
		mWebView = getViewById(R.id.wv_container);
		mWebView.setWebViewClient(new WebViewClient() {
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
				boolean hasFind;
				Uri mUri = Uri.parse(url);
				final String host = mUri.getHost();
				//首先域名匹配
				if (WebViewUrl.URL_BASE.contains(host) || NetUrl.URL_BASE.contains(host)) {
					//其次路径匹配
					hasFind = false;
				} else {
					Intent in = new Intent (Intent.ACTION_VIEW , Uri.parse(url));
					startActivity(in);
					hasFind = true;
				}
				return hasFind || super.shouldOverrideUrlLoading(view, url);
			}
		});
		mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				onWebProgressChangedMethod(newProgress);
			}
		});
		// 关闭硬件加速，否则部分手机（如vivo）WebView会很卡
		if (Build.VERSION.SDK_INT >= 11) {
			mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			AppDebugConfig.logMethodWithParams(this, mWebView.isHardwareAccelerated());
		}
		// 下载监听
		mWebView.setDownloadListener(this);
		//滚动条样式
		mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

		// initWithView mSettings
		mSettings = mWebView.getSettings();

		//JS监听
		mSettings.setJavaScriptEnabled(true);
		mJsInterfaceObject = new WebViewInterface(getActivity(), this, mWebView);
		if (mWebView != null) {
			mWebView.addJavascriptInterface(mJsInterfaceObject, "GiftCool");
		}
		mSettings.setJavaScriptCanOpenWindowsAutomatically(true);

		// 缩放
		mSettings.setSupportZoom(false);
		mSettings.setBuiltInZoomControls(false);
		mSettings.setUseWideViewPort(false);
		mSettings.setDisplayZoomControls(false);
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
	}

	protected void onWebPageStarted() {
		if (AppDebugConfig.IS_DEBUG) {
			AppDebugConfig.logMethodWithParams(this);
		}
		if (mProgressBar != null) {
			mProgressBar.setVisibility(View.VISIBLE);
		}
		if (mViewManager != null) {
			mViewManager.showContent();
		}
	}

	protected void onWebReceivedError() {
		if (AppDebugConfig.IS_DEBUG) {
			AppDebugConfig.logMethodWithParams(this);
		}
		mIsLoadingFailed = true;
		if (mViewManager != null) {
			mViewManager.showErrorRetry();
		}
		if (mProgressBar != null) {
			mProgressBar.setVisibility(View.GONE);
		}
	}

	protected void onWebPageFinished() {
		if (AppDebugConfig.IS_DEBUG) {
			AppDebugConfig.logMethodWithParams(this);
		}
		try {
			mIsLoading = false;
			if (mProgressBar != null) {
				mProgressBar.setVisibility(View.GONE);
			}
			if (mIsLoadingFailed) {
				if (mViewManager != null) {
					mViewManager.showErrorRetry();
				}
				if (mWebView != null) {
					mWebView.setVisibility(View.GONE);
				}
			} else {
				if (mViewManager != null) {
					mViewManager.showContent();
				}
				if (mWebView != null) {
					mWebView.setVisibility(View.VISIBLE);
				}
			}
			// 必须page load finish 才能重新setScrollY
			if (mWebView != null && mInit) {
				mWebView.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (mWebView != null && mScrollX > 0 && mScrollY > 0) {
							mWebView.scrollTo(mScrollX, mScrollY);
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
	public void onResume() {
		super.onResume();
		try {
			// 部分手机在调用 onPause 还是会出现mWeiView == null
			if (mWebView != null) {
				mWebView.onResume();
			}
		} catch (Exception ignored) {}
	}

	@Override
	public void onPause() {
		super.onPause();
		try {
			// 部分手机在调用 onPause 还是会出现mWeiView == null
			if (mWebView != null) {
				mWebView.onPause();
			}
		} catch (Exception ignored){}
	}

	public void reloadPage() {
		mIsLoading = false;
		mIsLoadingFailed = false;
		if (mWebView != null) {
			mWebView.reload();
			mScrollX = mWebView.getScrollX();
			mScrollY = mWebView.getScrollY();
		}
	}

	public void goBack() {
		if (mWebView != null && mWebView.canGoBack()) {
			mWebView.goBack();
		}
	}

	public void goForward() {
		if (mWebView != null && mWebView.canGoForward()) {
			mWebView.goForward();
		}
	}

	public void loadUrl(String url) {
		if (AppDebugConfig.IS_DEBUG) {
			AppDebugConfig.logMethodWithParams(this, url);
		}
		if (mWebView != null) {
			mWebView.loadUrl(url);
		}
	}

	/**
	 *
	 * 需要在setting设置完之后设置，方法可在processLogic中调用
 	 */
	public void postUrl(String url, byte[] postData) {
		if (AppDebugConfig.IS_DEBUG) {
			AppDebugConfig.logMethodWithParams(this, url);
		}
		if (mWebView != null) {
			mWebView.postUrl(url, postData);
		}
	}



	protected void onWebProgressChangedMethod(int i) {
		if (mProgressBar != null) {
			mProgressBar.setProgress(i);
		}
	}

	@Override
	public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
		try {
			if (AppDebugConfig.IS_DEBUG) {
				AppDebugConfig.logMethodWithParams(this, url, userAgent, contentDisposition, mimetype, contentLength);
			}
			Uri uri = Uri.parse(url);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
	}



	@Override
	public boolean onBack() {
		try {
			if (mWebView != null) {
				if (mWebView.canGoBack()) {
					goBack();
					return true;
				}
				mWebView.stopLoading();
				((ViewGroup)mContentView).removeView(mWebView);
				mWebView.removeAllViews();
				mWebView.destroy();
			}
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
		return false;
	}
}
