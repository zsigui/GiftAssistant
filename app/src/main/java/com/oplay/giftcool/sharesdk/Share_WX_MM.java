package com.oplay.giftcool.sharesdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;

/**
 * 分享到微信朋友圈
 * Created by yxf on 14-11-7.
 */
public class Share_WX_MM extends IShare {

    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
    private IWXAPI mIWXAPI;
    private ImageLoader mImageLoader;

    public Share_WX_MM(Context context, IWXAPI api) {
        super(context, R.drawable.ic_share_mm_moments, context.getString(R.string.st_share_mm_moments));
        mIWXAPI = api;
        mImageLoader = ImageLoader.getInstance();
    }

    @Override
    public void share(final String title, final String description, final String url, String iconUrl,
                      Bitmap Iconbitmap) {
        AppDebugConfig.v(AppDebugConfig.TAG_DOWNLOAD, "\ntitle;" + title, "\ndescription" + description +
                "\nurl:" + url, "\nimg:" + iconUrl, "\nIconBitmap:" + Iconbitmap);
        //如果有位图，优先考虑位图
        if (Iconbitmap != null) {
            shareWithBitmap(title, description, url, Iconbitmap);
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
                        shareWithBitmap(title, description, url, null);
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
                            shareWithBitmap(title, description, url, b);
                        } else {
                            shareWithBitmap(title, description, url, null);
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {

                    }
                });
    }

    @Override
    public boolean isSupport() {
        final boolean isInstalled = mIWXAPI.isWXAppInstalled();
        final int wxSdkVersion = mIWXAPI.getWXAppSupportAPI();
        return isInstalled && wxSdkVersion > TIMELINE_SUPPORTED_VERSION;
    }

    @Override
    public ShareType getShareType() {
        return ShareType.WX_MM;
    }

    public void shareWithBitmap(String title, String description, String url, Bitmap iconBm) {
        try {
            AppDebugConfig.v(AppDebugConfig.TAG_MANAGER, "\ntitle;" + title, "\ndescription" + description +
                    "\nurl:" + url, "\nIconBitmap:" + iconBm);
            if (isSupport()) {
                final Bitmap imgBitmap;
                if (iconBm == null) {
                    imgBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
                } else {
                    imgBitmap = iconBm;
                }
                WXWebpageObject webpage = new WXWebpageObject();
                webpage.webpageUrl = url;
                WXMediaMessage msg = new WXMediaMessage(webpage);
                // 此处由于是直接显示在图片右边的内容，所以title部分应该是description，
                // 会导致分享对话框的title部分就是这个description
                msg.title = title + "\n" + description;
                msg.description = description;
                Bitmap thumb = BitmapUtil.compressResize(imgBitmap, ShareSDKConfig.THUMB_SIZE,
                        ShareSDKConfig.THUMB_SIZE);
                msg.setThumbImage(thumb);
                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = buildTransaction("text");
                req.message = msg;
                req.scene = SendMessageToWX.Req.WXSceneTimeline;
                mIWXAPI.sendReq(req);
            }
        } catch (Exception e) {
            AppDebugConfig.w(AppDebugConfig.TAG_SHARE, e);
        }
    }
}
