package com.oplay.giftcool.assist;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.oplay.giftcool.R;

import cn.finalteam.galleryfinal.widget.GFImageView;

/**
 * Created by zsigui on 16-4-1.
 */
public class UILImageLoader implements cn.finalteam.galleryfinal.ImageLoader {
	private Bitmap.Config mImageConfig;

	public UILImageLoader() {
		this(Bitmap.Config.RGB_565);
	}

	public UILImageLoader(Bitmap.Config config) {
		this.mImageConfig = config;
	}

	@Override
	public void displayImage(Activity activity, String path, GFImageView imageView, Drawable defaultDrawable, int
			width, int height) {
		DisplayImageOptions options = new DisplayImageOptions.Builder()
				.cacheOnDisk(false)
				.cacheInMemory(false)
				.bitmapConfig(mImageConfig)
				.showImageOnLoading(R.drawable.ic_img_default)
				.showImageOnFail(R.drawable.ic_img_default)
				.showImageForEmptyUri(R.drawable.ic_img_default)
				.build();
		ImageSize imageSize = new ImageSize(width, height);
		ImageLoader.getInstance().displayImage("file://" + path, new ImageViewAware(imageView), options, imageSize,
				null, null);
	}

	@Override
	public void clearMemoryCache() {

	}
}
