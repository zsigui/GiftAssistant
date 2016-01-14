package com.oplay.giftassistant.listener;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import java.util.Observable;

/**
 * Created by zsigui on 16-1-14.
 */
public class WebViewInterface extends Observable {

	private FragmentActivity mHostActivity;
	private Fragment mHostFragment;
	private WebView mWebView;

	public WebViewInterface(FragmentActivity hostActivity, Fragment hostFragment, WebView webView) {
		mHostActivity = hostActivity;
		mHostFragment = hostFragment;
		mWebView = webView;
	}

	@JavascriptInterface
	public void setHeaderColor(int color) {

	}
}
