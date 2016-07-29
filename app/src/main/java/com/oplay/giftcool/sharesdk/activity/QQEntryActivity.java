package com.oplay.giftcool.sharesdk.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.WebViewUrl;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.sharesdk.DefaultShareIconUrlLoader;
import com.oplay.giftcool.sharesdk.ShareSDKConfig;
import com.oplay.giftcool.sharesdk.base.IShare;
import com.oplay.giftcool.util.ToastUtil;
import com.oplay.giftcool.util.URLUtil;
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
//            ToastUtil.showShort(getString(R.string.st_share_result_success));
            ToastUtil.showShort(ConstString.TOAST_SHARE_SUCCESS);
            // 通知发放金币
            ScoreManager.getInstance().setTaskFinished(true);
            ScoreManager.getInstance().reward(null, true);
            finish();
        }

        @Override
        public void onError(UiError uiError) {
//            ToastUtil.showShort(getString(R.string.st_share_result_failed));
            ToastUtil.showShort(ConstString.TOAST_SHARE_FAILED);
            AppDebugConfig.d(AppDebugConfig.TAG_SHARE, "onError: QQ分享失败");
            finish();
        }

        @Override
        public void onCancel() {
//            ToastUtil.showShort(getString(R.string.st_share_result_quick));
            ToastUtil.showShort(ConstString.TOAST_SHARE_QUICKED);
            AppDebugConfig.d(AppDebugConfig.TAG_SHARE, "onCancel: QQ分享取消");
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
                final int type = intent.getIntExtra(ShareSDKConfig.ARGS_CONTENT_TYPE, IShare.TYPE.WEB);
                switch (shareType) {
                    case ShareSDKConfig.QQFRIENDS:
                        shareQQFriends(title, description, url, iconUrl, shareType, type);
                        break;
                    case ShareSDKConfig.QZONE:
                        shareQZone(title, description, url, iconUrl, shareType, type);
                        break;
                    default:
                }
//		    由于回调也走这里，我们添加了自己的回调处理
            } else {
                Map<String, String> params = URLUtil.getParams(data);
                final String result = params.get(KEY_RESULT);
                if (result.equals(SUCCESS)) {
//                    ToastUtil.showShort(getString(R.string.st_share_result_success));
                    ToastUtil.showShort(ConstString.TOAST_SHARE_SUCCESS);
//					新手任务过来的分享需要上报服务器
                    ScoreManager.getInstance().setTaskFinished(true);
                    ScoreManager.getInstance().reward(null, true);
                }
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppDebugConfig.d(AppDebugConfig.TAG_SHARE, "onResume: QQ分享");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Tencent.onActivityResultData(requestCode, resultCode, data, mUiListener);
        AppDebugConfig.d(AppDebugConfig.TAG_SHARE, "qq分享-onActivityResult: requestCode = " + requestCode + ", " +
                "resultCode = " + resultCode + ", data = " + data);
    }

    private void shareQQFriends(String title, String description, String url, String iconUrl, int shareType, int type) {
        final Bundle params = new Bundle();
        params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, description);
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, url);
        switch (type) {
            case IShare.TYPE.IMG:
                // 纯图片分享只针对本地图片
                // 对于分享图片，url则为图片地址
                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
                if (TextUtils.isEmpty(url)) {
                    // 本地图片地址为空，则采用默认图文方式
                    shareQQFriends(title, description, url, iconUrl, shareType, IShare.TYPE.TEXT);
                } else if (url.startsWith("http")) {
                    // 先加载执行到本地
                    DefaultShareIconUrlLoader.getInstance().setListener(this);
                    DefaultShareIconUrlLoader.getInstance()
                            .getDefaultShareIcon(this, title, description, url, iconUrl, shareType, type);
                } else {
                    // 设置地址并进行分享
                    params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, url);
                    mTencent.shareToQQ(this, params, mUiListener);
                }
                return;
            case IShare.TYPE.MUSIC:
            case IShare.TYPE.VIDEO:
                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_AUDIO);
                break;
            case IShare.TYPE.TEXT:
            case IShare.TYPE.WEB:
            default:
                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);

        }
        if (TextUtils.isEmpty(iconUrl)) {
            iconUrl = WebViewUrl.ICON_GCOOL;
        }
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, iconUrl);
        mTencent.shareToQQ(this, params, mUiListener);
    }

    private void shareQZone(String title, String description, String url, String iconUrl, int shareType, int type) {
        final Bundle params = new Bundle();
        // QQ空间只支持图文分享
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, title);
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, description);
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, url);
        if (TextUtils.isEmpty(iconUrl)) {
            iconUrl = WebViewUrl.ICON_GCOOL;
        }
        final ArrayList<String> imgList = new ArrayList<String>();
        if (type == IShare.TYPE.IMG && !TextUtils.isEmpty(url)) {
            imgList.add(url);
        } else {
            imgList.add(iconUrl);
        }
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imgList);
        mTencent.shareToQzone(this, params, mUiListener);
    }

    @Override
    public void onFetch(String title, String description, String url, String iconUrl, int shareType, int type) {
        switch (shareType) {
            case ShareSDKConfig.QQFRIENDS:
                shareQQFriends(title, description, url, iconUrl, shareType, type);
                break;
            case ShareSDKConfig.QZONE:
                shareQZone(title, description, url, iconUrl, shareType, type);
        }
        DefaultShareIconUrlLoader.getInstance().setListener(null);
    }

}
