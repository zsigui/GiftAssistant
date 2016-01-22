package com.oplay.giftcool.sharesdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * Created by yxf on 14-12-16.
 * 加载默认图片的URL
 */
public class DefaultShareIconUrlLoader {
	private static DefaultShareIconUrlLoader instance;
	public String defaulturl = "";
	private LoaderListener mListener;

	public static synchronized DefaultShareIconUrlLoader getInstance() {
		if (instance == null) {
			instance = new DefaultShareIconUrlLoader();
		}
		return instance;
	}

	public void getDefaultShareIcon(Context context, final String title, final String description, final String url,
	                                final int shareType) {
		if (!TextUtils.isEmpty(defaulturl)) {
			if (mListener != null) {
				mListener.onFetch(title, description, url, defaulturl, shareType);
			}
			return;
		}
		ImageLoader.getInstance().loadImage(url, new ImageLoadingListener() {
			@Override
			public void onLoadingStarted(String s, View view) {

			}

			@Override
			public void onLoadingFailed(String s, View view, FailReason failReason) {

			}

			@Override
			public void onLoadingComplete(String s, View view, Bitmap bitmap) {
				if (mListener != null && !TextUtils.isEmpty(defaulturl)) {
					mListener.onFetch(title, description, url, defaulturl, shareType);
				}
			}

			@Override
			public void onLoadingCancelled(String s, View view) {

			}
		});
	}

	public String getDefaulturl() {
		return defaulturl;
	}

	public void setListener(LoaderListener listener) {
		mListener = listener;
	}

	public static interface LoaderListener {
		public void onFetch(String title, String description, String url, String iconUrl, int shareType);
	}
}

