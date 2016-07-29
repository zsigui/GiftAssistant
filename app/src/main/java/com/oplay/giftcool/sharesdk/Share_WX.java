package com.oplay.giftcool.sharesdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.sharesdk.base.IShare;
import com.oplay.giftcool.util.BitmapUtil;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXMusicObject;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXVideoObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;

/**
 * 分享到微信
 * Created by yxf on 14-11-7.
 */
public abstract class Share_WX extends IShare {

    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
    protected IWXAPI mIWXAPI;
    protected ImageLoader mImageLoader;

    public Share_WX(Context context, IWXAPI iwxapi, int iconId, String description) {
        super(context, iconId, description);
        mIWXAPI = iwxapi;
        mImageLoader = ImageLoader.getInstance();
    }

    @Override
    public void share(final String title, final String description, final String url, String iconUrl,
                      Bitmap Iconbitmap, final int type) {
        AppDebugConfig.v(AppDebugConfig.TAG_DOWNLOAD, "\ntitle;" + title, "\ndescription" + description +
                "\nurl:" + url, "\nimg:" + iconUrl, "\nIconBitmap:" + Iconbitmap);
        //如果有位图，优先考虑位图
        if (Iconbitmap != null) {
            shareAction(title, description, url, Iconbitmap, type);
            return;
        }
        //没有位图，再考虑URL
        mImageLoader.loadImage(iconUrl, new ImageSize(ShareSDKConfig.THUMB_SIZE, ShareSDKConfig.THUMB_SIZE),
                new ImageLoadingListener() {

                    @Override
                    public void onLoadingStarted(String s, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {
                        shareAction(title, description, url, null, type);
                    }

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                        if (bitmap != null) {
                            final int Width = bitmap.getWidth();
                            final int Height = bitmap.getHeight();
                            final int Length = Width < Height ? Width : Height;
                            final int x = (Width - Length) / 2;
                            final int y = (Height - Length) / 2;
                            final Bitmap b = Bitmap.createBitmap(bitmap, x, y, Length, Length);
//						bitmap.recycle();
                            shareAction(title, description, url, b, type);
                        } else {
                            shareAction(title, description, url, null, type);
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {

                    }
                });
    }

    abstract int getShareScene();

    @Override
    public boolean isSupport() {
        final boolean isInstalled = mIWXAPI.isWXAppInstalled();
        final int wxSdkVersion = mIWXAPI.getWXAppSupportAPI();
        return getShareScene() == SendMessageToWX.Req.WXSceneTimeline ?
                (isInstalled && wxSdkVersion > TIMELINE_SUPPORTED_VERSION) : isInstalled;
    }

    /**
     * 执行分享图片
     */
    public void sharePic(final String url, Bitmap picData) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (picData != null) {
            SendMessageToWX.Req req = new SendMessageToWX.Req();
            WXMediaMessage msg = new WXMediaMessage();
            req.transaction = buildTransaction(TYPE.STR_IMG);
            msg.mediaObject = new WXImageObject(picData);
            msg.setThumbImage(getThumb(picData));
            picData.recycle();
            req.scene = getShareScene();
            req.message = msg;
            mIWXAPI.sendReq(req);
        } else {
            mImageLoader.loadImage(url, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    sharePic(url, loadedImage);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });
        }
    }

    public void shareAction(String title, String description, String url, Bitmap iconBm, int dataType) {
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        WXMediaMessage msg = new WXMediaMessage();
        switch (dataType) {
            case TYPE.IMG:
                sharePic(url, null);
                return;
            case TYPE.MUSIC:
                req.transaction = buildTransaction(TYPE.STR_MUSIC);
                WXMusicObject musicObj = new WXMusicObject();
                musicObj.musicUrl = url;
                msg.mediaObject = musicObj;
                break;
            case TYPE.VIDEO:
                req.transaction = buildTransaction(TYPE.STR_VIDEO);
                WXVideoObject videoObj = new WXVideoObject();
                videoObj.videoUrl = url;
                msg.mediaObject = videoObj;
                break;
            case TYPE.TEXT:
                WXTextObject textObj = new WXTextObject();
                textObj.text = description;
                msg.mediaObject = textObj;
                req.transaction = buildTransaction(TYPE.STR_TEXT);
                break;
            case TYPE.WEB:
            default:
                req.transaction = buildTransaction(TYPE.STR_WEB);
                WXWebpageObject webObj = new WXWebpageObject();
                webObj.webpageUrl = url;
                msg.mediaObject = webObj;
                break;
        }
        msg.title = (getShareScene() == SendMessageToWX.Req.WXSceneTimeline ? title + "\n" + description: title);
        msg.description = description;
        msg.setThumbImage(getThumb(iconBm));
        if (iconBm != null) {
            iconBm.recycle();
        }
        req.scene = getShareScene();
        req.message = msg;
        mIWXAPI.sendReq(req);
    }

    private Bitmap getThumb(Bitmap iconBm) {
        if (iconBm == null) {
            iconBm = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
        }
        return BitmapUtil.compressResize(iconBm, ShareSDKConfig.THUMB_SIZE,
                ShareSDKConfig.THUMB_SIZE);
    }
}
