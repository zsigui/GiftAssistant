package com.oplay.giftcool.sharesdk.base;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * 分享SDK的基类
 * Created by yxf on 14-11-7.
 */
public abstract class IShare {
	protected Context mContext;
	private int mIconId;
	private String mDescription;

	public IShare(Context context, int iconId, String description) {
		mContext = context;
		mIconId = iconId;
		mDescription = description;
	}

	/**
	 * @param title
	 * @param description 朋友圈是使用title作为右边展示部分
	 * @param url
	 * @param iconUrl
	 * @param Iconbitmap
	 */
	public abstract void share(
			final String title,
			final String description,
			final String url,
			final String iconUrl,
			final Bitmap Iconbitmap
	);

	public abstract boolean isSupport();

	public int getIconId() {
		return mIconId;
	}

	public String getDescription() {
		return mDescription;
	}

	protected String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}

	public abstract ShareType getShareType();

	public static enum ShareType {
		WX_MM,
		WX_FRIENDS,
		QQ_ZONE,
		QQ_FRIENDS,
		MORE
	}
}
