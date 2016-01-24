package com.oplay.giftcool.listener;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.google.gson.JsonSyntaxException;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.manager.PayManager;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.ui.fragment.game.GameDetailFragment;
import com.oplay.giftcool.util.IntentUtil;
import com.socks.library.KLog;

import java.util.Observable;

/**
 * Created by zsigui on 16-1-14.
 */
public class WebViewInterface extends Observable {

	private static final int RET_SUCCESS = 0;
	private static final int RET_INTERAL_ERR = 1;
	private static final int RET_PARAM_ERR = 2;

	private FragmentActivity mHostActivity;
	private Fragment mHostFragment;
	private WebView mWebView;

	public WebViewInterface(FragmentActivity hostActivity, Fragment hostFragment, WebView webView) {
		mHostActivity = hostActivity;
		mHostFragment = hostFragment;
		mWebView = webView;
	}

	@JavascriptInterface
	public int jumpToGift(int id) {
		if (id <= 0) {
			return RET_PARAM_ERR;
		}
		IntentUtil.jumpGiftDetail(mHostActivity, id);
		return RET_SUCCESS;
	}

	@JavascriptInterface
	public int seizeGiftCode(String giftJson) {
		if (TextUtils.isEmpty(giftJson)) {
			return RET_PARAM_ERR;
		}
		try {
			IndexGiftNew gift = AssistantApp.getInstance().getGson().fromJson(giftJson, IndexGiftNew.class);
			PayManager.getInstance().seizeGift(mHostActivity, gift, null);
			return RET_SUCCESS;
		} catch (JsonSyntaxException e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(AppDebugConfig.TAG_WEBVIEW, e);
			}
		}
		return RET_INTERAL_ERR;
	}

	@JavascriptInterface
	public int setDownloadBtn(boolean isShow, String params) {
		try {
			if (mHostActivity == null || mHostFragment == null || !(mHostFragment instanceof GameDetailFragment)) {
				return RET_INTERAL_ERR;
			}
			IndexGameNew appInfo = null;
			if (isShow) {
				try {
					appInfo = AssistantApp.getInstance().getGson().fromJson(params, IndexGameNew.class);
				} catch (Throwable e) {
					if (AppDebugConfig.IS_DEBUG) {
						KLog.e(e);
					}
				}
				if (appInfo == null || !appInfo.isValid()) {
					return RET_PARAM_ERR;
				}
			}
			((GameDetailFragment) mHostFragment).setDownloadBtn(isShow, mHostActivity, appInfo);
			return RET_SUCCESS;
		}catch (Throwable e) {
			if(AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
			return RET_INTERAL_ERR;
		}
	}
}