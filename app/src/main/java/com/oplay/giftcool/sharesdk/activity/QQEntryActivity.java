package com.oplay.giftcool.sharesdk.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.sharesdk.DefaultShareIconUrlLoader;
import com.oplay.giftcool.sharesdk.ShareSDKConfig;
import com.oplay.giftcool.util.ToastUtil;
import com.oplay.giftcool.util.URLUtil;
import com.socks.library.KLog;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.util.ArrayList;
import java.util.Map;

/**
 * QQ分享的发出/结果处理类
 * Created by yxf on 14-12-18.
 * update zsg on 16-1-21
 */
public class QQEntryActivity extends Activity implements DefaultShareIconUrlLoader.LoaderListener {
	private Tencent mTencent;
	private static final String KEY_RESULT = "result";
	private static final String SUCCESS = "complete";
	private IUiListener mUiListener = new IUiListener() {
		@Override
		public void onComplete(Object o) {
			ToastUtil.showShort(getString(R.string.st_share_result_success));
			// 通知发放金币
			ScoreManager.getInstance().reward(null, true);
			finish();
		}

		@Override
		public void onError(UiError uiError) {
			ToastUtil.showShort(getString(R.string.st_share_result_failed));
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_UTIL, "onError: QQ分享失败");
			}
			finish();
		}

		@Override
		public void onCancel() {
			ToastUtil.showShort(getString(R.string.st_share_result_quick));
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_UTIL, "onCancel: QQ分享取消");
			}
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTencent = Tencent.createInstance(ShareSDKConfig.SHARE_QQ_APP_ID, this);
		Intent intent = getIntent();
		if (intent != null) {
			final String data = intent.getDataString();

			if (TextUtils.isEmpty(data)) {
				final int shareType = intent.getIntExtra(ShareSDKConfig.ARGS_SHARE_TYPE, -1);
				final String title = intent.getStringExtra(ShareSDKConfig.ARGS_TITLE);
				final String description = intent.getStringExtra(ShareSDKConfig.ARGS_DESCRIPTION);
				final String url = intent.getStringExtra(ShareSDKConfig.ARGS_URL);
				final String iconUrl = intent.getStringExtra(ShareSDKConfig.ARGS_ICON_URL);
				switch (shareType) {
					case ShareSDKConfig.QQFRIENDS:
						shareQQFriends(title, description, url, iconUrl, shareType);
						break;
					case ShareSDKConfig.QZONE:
						shareQZone(title, description, url, iconUrl, shareType);
						break;
					default:
				}
//		    由于回调也走这里，我们添加了自己的回调处理
			} else {
				Map<String, String> params = URLUtil.getParams(data);
				final String result = params.get(KEY_RESULT);
				if (result.equals(SUCCESS)) {
					ToastUtil.showShort(getString(R.string.st_share_result_success));
//					新手任务过来的分享需要上报服务器
					ScoreManager.getInstance().reward(null, true);
				}
				finish();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (AppDebugConfig.IS_DEBUG) {
			KLog.d(AppDebugConfig.TAG_UTIL, "onResume: QQ分享");
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Tencent.onActivityResultData(requestCode, resultCode, data, mUiListener);
		if (AppDebugConfig.IS_DEBUG) {
			KLog.d(AppDebugConfig.TAG_UTIL, "qq分享-onActivityResult: requestCode = " + requestCode + ", " +
					"resultCode = " + resultCode
					+ ", data = " + data);
		}
	}

	private void shareQQFriends(String title, String description, String url, String iconUrl, int shareType) {
		final Bundle params = new Bundle();
		params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
		params.putString(QQShare.SHARE_TO_QQ_SUMMARY, description);
		params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, url);
		if (!TextUtils.isEmpty(iconUrl)) {
			params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, iconUrl);
			mTencent.shareToQQ(this, params, mUiListener);
		} else {
			DefaultShareIconUrlLoader.getInstance().setListener(this);
			DefaultShareIconUrlLoader.getInstance().getDefaultShareIcon(this, title, description, url, shareType);
		}
	}

	private void shareQZone(String title, String description, String url, String iconUrl, int shareType) {
		final Bundle params = new Bundle();
		params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
		params.putString(QzoneShare.SHARE_TO_QQ_TITLE, title);
		params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, description);
		params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, url);
		if (!TextUtils.isEmpty(iconUrl)) {
			final ArrayList<String> imgList = new ArrayList<String>();
			imgList.add(iconUrl);
			params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imgList);
			Global.THREAD_POOL.execute(new Runnable() {
				@Override
				public void run() {
					mTencent.shareToQzone(QQEntryActivity.this, params, mUiListener);
				}
			});
		} else {
			DefaultShareIconUrlLoader.getInstance().setListener(this);
			DefaultShareIconUrlLoader.getInstance().getDefaultShareIcon(this, title, description, url, shareType);
		}
	}

	@Override
	public void onFetch(String title, String description, String url, String iconUrl, int shareType) {
		switch (shareType) {
			case ShareSDKConfig.QQFRIENDS:
				shareQQFriends(title, description, url, iconUrl, shareType);
				break;
			case ShareSDKConfig.QZONE:
				shareQZone(title, description, url, iconUrl, shareType);
		}
		DefaultShareIconUrlLoader.getInstance().setListener(null);
	}

}
