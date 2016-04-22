package com.oplay.giftcool.ui.fragment.base;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.config.WebViewUrl;
import com.oplay.giftcool.listener.OnBackPressListener;
import com.oplay.giftcool.listener.SetTitleListner;
import com.oplay.giftcool.listener.WebViewInterface;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.SystemUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

	private List<String> mTitles = new ArrayList<>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContentView = super.onCreateView(inflater, container, savedInstanceState);
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
				if (!mSettings.getLoadsImagesAutomatically()) {
					mSettings.setLoadsImagesAutomatically(true);
				}
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
                if (mUri == null) {
                    return false;
                }
				final String host = mUri.getHost();
                if (TextUtils.isEmpty(host)) {
                    return false;
                }
				//首先域名匹配
                // 对于部分机型 getBaseUrl 可能为null
				if (WebViewUrl.getBaseUrl().contains(host)
                        || NetUrl.getBaseUrl().contains(host)) {
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

			@Override
			public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
				return super.onJsBeforeUnload(view, url, message, result);
			}

			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
				if (getContext() != null && getContext() instanceof SetTitleListner) {
					mTitles.add(title);
					((SetTitleListner) getContext()).setBarTitle(title);
				}
			}

			@Override
			public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
				ToastUtil.showShort(message);
				return super.onJsAlert(view, url, message, result);
			}
		});
		// 开启硬件加速，否则部分手机（如vivo）WebView会很卡
		if (Build.VERSION.SDK_INT >= 11) {
			mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_WEBVIEW, "isHardwareAccelerated = " + mWebView.isHardwareAccelerated());
			}
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
		mSettings.setUseWideViewPort(true);
		mSettings.setLoadWithOverviewMode(true);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			WebView.setWebContentsDebuggingEnabled(true);
			mSettings.setLoadsImagesAutomatically(true);
		} else {
			mSettings.setLoadsImagesAutomatically(false);
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
				if (!NetworkUtil.isAvailable(AssistantApp.getInstance().getApplicationContext())) {
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

	/**
	 * 重写该方法执行异步网络请求
	 * @param view
	 * @param request
	 * @return
	 */
	protected WebResourceResponse interceptRequest(WebView view, WebResourceRequest request) {

		return null;
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
			} else {
				if (mViewManager != null) {
					mViewManager.showContent();
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
			if (mViewManager != null) {
				mViewManager.showErrorRetry();
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
			if (mTitles.size() > 1) {
				mTitles.remove(mTitles.size() - 1);
				if (getContext() != null && getContext() instanceof SetTitleListner) {
					((SetTitleListner) getContext()).setBarTitle(mTitles.get(mTitles.size() - 1));
				}
			}
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
            KLog.d(AppDebugConfig.TAG_WEBVIEW, "request_url = " + url);
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
            KLog.d(AppDebugConfig.TAG_WEBVIEW, "post_url = " + url);
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
//				mWebView.stopLoading();
//				((ViewGroup)mContentView).removeView(mWebView);
//				mWebView.removeAllViews();
//				mWebView.destroy();
			}
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
		return false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			if (mWebView != null) {
				mWebView.stopLoading();
				mWebView.removeAllViews();
				mWebView.destroy();
			}
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
	}
}
