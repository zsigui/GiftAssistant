package com.oplay.giftcool.listener;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.google.gson.JsonSyntaxException;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.GiftTypeUtil;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.manager.PayManager;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.sharesdk.ShareSDKManager;
import com.oplay.giftcool.ui.activity.MainActivity;
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
			if (gift.giftType == GiftTypeUtil.GIFT_TYPE_ZERO_SEIZE) {
				IntentUtil.jumpGiftDetail(mHostActivity, gift.id);
				return RET_SUCCESS;
			}
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

	public int shareGift(String giftJson) {
		try {
			if (mHostActivity == null || mHostFragment == null ) {
				return RET_INTERNAL_ERR;
			}
			try {
				IndexGiftNew gift = AssistantApp.getInstance().getGson().fromJson(giftJson, IndexGiftNew.class);
				if (gift.status == GiftTypeUtil.STATUS_FINISHED) {
					return RET_PARAM_ERR;
				}
				ShareSDKManager.getInstance(mHostActivity).shareGift(mHostActivity,
						mHostFragment.getChildFragmentManager(), gift);
			} catch (Throwable e) {
				return RET_PARAM_ERR;
			}
			return RET_SUCCESS;
		}catch (Throwable e) {
			if(AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
			return RET_INTERNAL_ERR;
		}
	}

	public int shareGCool() {
		try {
			if (mHostActivity == null || mHostFragment == null) {
				return RET_INTERNAL_ERR;
			}
			ShareSDKManager.getInstance(mHostActivity).shareGCool(mHostActivity, mHostFragment.getChildFragmentManager());
			return RET_SUCCESS;
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
			return RET_INTERNAL_ERR;
		}
	}

	/**
	 * 根据类型跳转对应列表界面
	 */
	public int jumpByType(int type) {
		if (type > 10 || type < 0) {
			return RET_PARAM_ERR;
		}
		try {
			switch (type) {
				case 0:
					IntentUtil.jumpGiftNewList(mHostActivity);
					break;
				case 1:
					IntentUtil.jumpGiftLimitList(mHostActivity);
					break;
				case 2:
					IntentUtil.jumpGiftHotList(mHostActivity, "");
					break;
				case 3:
					IntentUtil.jumpGameHotList(mHostActivity);
					break;
				case 4:
					IntentUtil.jumpGameNewList(mHostActivity);
					break;
				case 5:
					IntentUtil.jumpLogin(mHostActivity);
					break;
				// 以下几个需要登录
				case 6:
					IntentUtil.jumpMyGift(mHostActivity);
					break;
				case 7:
					IntentUtil.jumpEarnScore(mHostActivity);
					break;
				case 8:
					IntentUtil.jumpMyWallet(mHostActivity);
					break;
				case 9:
					IntentUtil.jumpFeedBack(mHostActivity);
					break;
				case 10:
					// 分享普通礼包
					if (MainActivity.sGlobalHolder == null) {
						IntentUtil.jumpGiftNewList(mHostActivity);
					} else {
						MainActivity.sGlobalHolder.jumpToIndexGift(4);
						mHostActivity.finish();
					}
					break;
			}
			return RET_SUCCESS;
		} catch (Throwable e) {
			return RET_INTERNAL_ERR;
		}
	}
}
