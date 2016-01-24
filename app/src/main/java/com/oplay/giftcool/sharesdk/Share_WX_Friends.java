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
import com.oplay.giftcool.sharesdk.base.IShare;
import com.oplay.giftcool.util.BitmapUtil;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;

import net.youmi.android.libs.common.debug.Debug_SDK;

/**
 * 分享到微信朋友
 * Created by yxf on 14-11-7.
 * update by zsg on 16-1-21
 */
public class Share_WX_Friends extends IShare {

	private IWXAPI mWXApi;
	private ImageLoader mImageLoader;

	public Share_WX_Friends(Context context, IWXAPI WXApi) {
		super(context, R.drawable.ic_share_mm_friends, context.getString(R.string.st_share_mm_friends));
		mWXApi = WXApi;
		mImageLoader = ImageLoader.getInstance();
	}

	@Override
	public void share(final String title, final String description, final String url, String iconUrl,
	                  Bitmap Iconbitmap) {
		//如果有位图，优先考虑位图
		if (Iconbitmap != null) {
			shareWithBitmap(title, description, url, Iconbitmap);
			return;
		}
		//有url，再考虑URL
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
		return mWXApi.isWXAppSupportAPI();
	}

	@Override
	public ShareType getShareType() {
		return ShareType.WX_FRIENDS;
	}

	public void shareWithBitmap(String title, String description, String url, Bitmap iconBm) {
		try {
			final Bitmap imgBitmap;
			if (iconBm == null) {
				imgBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
			} else {
				imgBitmap = iconBm;
			}
			if (isSupport()) {
				WXWebpageObject webpage = new WXWebpageObject();
				webpage.webpageUrl = url;
				WXMediaMessage msg = new WXMediaMessage(webpage);
				msg.title = title;
				msg.description = description;
				Bitmap thumb = BitmapUtil.compressResize(imgBitmap, ShareSDKConfig.THUMB_SIZE,
						ShareSDKConfig.THUMB_SIZE);
				msg.setThumbImage(thumb);
				SendMessageToWX.Req req = new SendMessageToWX.Req();
				req.transaction = buildTransaction("text");
				req.message = msg;
				mWXApi.sendReq(req);
			}
		} catch (Exception e) {
			Debug_SDK.e(e);
		}
	}
}
