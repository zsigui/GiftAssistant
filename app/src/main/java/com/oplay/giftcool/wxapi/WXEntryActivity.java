package com.oplay.giftcool.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.sharesdk.ShareSDKManager;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.ShowMessageFromWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;

/**
 * Created by zsigui on 16-1-22.
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		IWXAPI api = ShareSDKManager.getInstance(this).getWXApi();
		api.handleIntent(getIntent(), this);
		finish();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	/*
		 * 处理微信分享内容点击响应后的请求回调
		 */
	@Override
	public void onReq(BaseReq baseReq) {
		if (AppDebugConfig.IS_DEBUG) {
			KLog.d(AppDebugConfig.TAG_SERVICE, "req from wx share : " + baseReq);
		}
		goToShowShare((ShowMessageFromWX.Req) baseReq);
		finish();
	}

	/*
	 * 处理分享到微信后的回调
	 */
	@Override
	public void onResp(BaseResp baseResp) {
		if (AppDebugConfig.IS_DEBUG) {
			KLog.d(AppDebugConfig.TAG_SERVICE, "resp from wx share : " + baseResp);
		}
		switch (baseResp.errCode) {
			case BaseResp.ErrCode.ERR_OK:
				// 分享成功
				ScoreManager.getInstance().reward();
				break;
			case BaseResp.ErrCode.ERR_USER_CANCEL:
				// 分享取消
				break;
			case BaseResp.ErrCode.ERR_AUTH_DENIED:
				// 分享拒绝
				ToastUtil.showShort("分享失败-被拒绝了");
				break;
		}
		finish();
	}

	/**
	 * 处理微信分享内容，跳转到礼包详情界面
	 */
	private void goToShowShare(ShowMessageFromWX.Req baseReq) {
		WXMediaMessage msg = baseReq.message;
		if (!(msg.mediaObject instanceof WXWebpageObject)) {
			return;
		}
		WXWebpageObject webObj = (WXWebpageObject) msg.mediaObject;
		String index = webObj.webpageUrl.substring(webObj.webpageUrl.indexOf("plan_id=") + 8, webObj.webpageUrl.length());
		KLog.e("share from: detail index = " + index);
		try {
			int id = Integer.parseInt(index);
			IntentUtil.jumpGiftDetail(this, id);
		} catch (Exception e) {
			ToastUtil.showShort("跳转页面错误，该分享已不存在");
		}
	}
}
