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
import com.oplay.giftcool.ui.fragment.gift.GiftFragment;
import com.oplay.giftcool.util.IntentUtil;
import com.socks.library.KLog;

import java.util.Observable;

import cn.finalteam.galleryfinal.GalleryFinal;

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
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
			return RET_INTERNAL_ERR;
		}
	}

	@JavascriptInterface
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

	@JavascriptInterface
	public int shareGift(String giftJson) {
		try {
			if (mHostActivity == null || mHostFragment == null) {
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
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
			return RET_INTERNAL_ERR;
		}
	}

	@JavascriptInterface
	public int shareGCool() {
		try {
			if (mHostActivity == null || mHostFragment == null) {
				return RET_INTERNAL_ERR;
			}
			ShareSDKManager.getInstance(mHostActivity).shareGCool(mHostActivity, mHostFragment.getChildFragmentManager
					());
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
	@JavascriptInterface
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
					IntentUtil.jumpGiftLimitList(mHostActivity, false);
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
						MainActivity.sGlobalHolder.jumpToIndexGift(GiftFragment.POS_NEW);
						mHostActivity.finish();
					}
					break;
			}
			return RET_SUCCESS;
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_WEBVIEW, e);
			}
			return RET_INTERNAL_ERR;
		}
	}

	/**
	 * 显示多张预览图片
	 * @param selectedIndex 选择最初显示图片的下标，从0开始
	 * @param picsPath 传入图片地址的字符串数组
	 */
	@JavascriptInterface
	public int showMultiPic(int selectedIndex, String... picsPath) {
		int ret = GalleryFinal.openMultiPhoto(selectedIndex, picsPath);
		switch (ret) {
			case GalleryFinal.Error.RET_INIT_FAIL:
				return RET_INTERNAL_ERR;
			case GalleryFinal.Error.RET_NO_SELECTED_PHOTO:
				return RET_PARAM_ERR;
			case GalleryFinal.Error.SUCCESS:
				return RET_SUCCESS;
			default:
				return RET_OTHER_ERR;
		}
	}

}
