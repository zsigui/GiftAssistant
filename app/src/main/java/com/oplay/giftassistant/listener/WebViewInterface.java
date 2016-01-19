package com.oplay.giftassistant.listener;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.google.gson.JsonSyntaxException;
import com.oplay.giftassistant.AssistantApp;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.GiftTypeUtil;
import com.oplay.giftassistant.manager.PayManager;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;
import com.oplay.giftassistant.model.data.resp.IndexGiftNew;
import com.oplay.giftassistant.ui.fragment.game.GameDetailFragment;
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
	public void setHeaderColor(int color) {

	}

	@JavascriptInterface
	public void seizeGiftCode(String giftJson) {
		try {
			IndexGiftNew gift = AssistantApp.getInstance().getGson().fromJson(giftJson, IndexGiftNew.class);
			if (gift != null) {
				switch (GiftTypeUtil.getItemViewType(gift)) {
					case GiftTypeUtil.TYPE_NORMAL_SEIZE:
					case GiftTypeUtil.TYPE_LIMIT_SEIZE:
						PayManager.getInstance().chargeGift(mHostActivity, gift, null);
						break;
					case GiftTypeUtil.TYPE_NORMAL_SEARCH:
						PayManager.getInstance().searchGift(mHostActivity, gift, null);
						break;
				}
			}
		} catch (JsonSyntaxException e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(AppDebugConfig.TAG_WEBVIEW, e);
			}
		}
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
			((GameDetailFragment) mHostFragment).setDownloadBtn(isShow, appInfo);
			return RET_SUCCESS;
		}catch (Throwable e) {
			if(AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
			return RET_INTERAL_ERR;
		}
	}
}
