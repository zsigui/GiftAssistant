package com.oplay.giftcool.listener;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.google.gson.JsonSyntaxException;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.KeyConfig;
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

	public static final int RET_SUCCESS = 0;
	public static final int RET_INTERNAL_ERR = 1;
	public static final int RET_PARAM_ERR = 2;
	public static final int RET_OTHER_ERR = 3;

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
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_WEBVIEW, "json = " + giftJson);
			}
			IndexGiftNew gift = AssistantApp.getInstance().getGson().fromJson(giftJson, IndexGiftNew.class);
			return PayManager.getInstance().seizeGift(mHostActivity, gift, null);
		} catch (JsonSyntaxException e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(AppDebugConfig.TAG_WEBVIEW, e);
			}
		}
		return RET_INTERNAL_ERR;
	}

	@JavascriptInterface
	public int setDownloadBtn(boolean isShow, String params) {
		try {
			if (mHostActivity == null || mHostFragment == null || !(mHostFragment instanceof GameDetailFragment)) {
				return RET_INTERNAL_ERR;
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
			return RET_INTERNAL_ERR;
		}
	}

	public int login(int loginType) {
		if (loginType != KeyConfig.TYPE_ID_OUWAN_LOGIN && loginType != KeyConfig.TYPE_ID_PHONE_LOGIN) {
			loginType = KeyConfig.TYPE_ID_PHONE_LOGIN;
		}
		try {
			IntentUtil.jumpLogin(mHostActivity, loginType);
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_WEBVIEW, e);
			}
			return RET_INTERNAL_ERR;
		}
		return RET_SUCCESS;
	}
}
