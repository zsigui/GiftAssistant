package com.oplay.giftcool.assist;

import android.app.Activity;
import android.graphics.drawable.Drawable;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.oplay.giftcool.config.Global;

import cn.finalteam.galleryfinal.widget.GFImageView;

/**
 * Created by zsigui on 16-4-1.
 */
public class UILImageLoader implements cn.finalteam.galleryfinal.ImageLoader {

	private final int TAG_IMG = 0x124433ff;

	public UILImageLoader() {
	}

	@Override
	public void displayImage(Activity activity, String path, GFImageView imageView, Drawable defaultDrawable, int
			width, int height) {
		if (imageView == null) {
			return;
		}
		if (imageView.getTag(TAG_IMG) != null && imageView.getTag(TAG_IMG).equals(path)) {
			// 已设置，不重新刷新
			return;
		}
		imageView.setTag(TAG_IMG, path);
		if (!path.startsWith("http") && !path.startsWith("file://")
				&& !path.startsWith("drawable://")) {
			path = "file://" + path;
		}
		ImageLoader.getInstance().displayImage(path, new ImageViewAware(imageView),
				Global.getGalleryImgOptions(), null, null, null);
	}

	@Override
	public void displayImage(Activity activity, String path, GFImageView imageView, Drawable defaultDrawable, int
			width, int height, ImageLoadingListener listener) {
		if (imageView == null) {
			return;
		}
		if (imageView.getTag(TAG_IMG) != null && imageView.getTag(TAG_IMG).equals(path)) {
			// 已设置，不重新刷新
			return;
		}
		imageView.setTag(TAG_IMG, path);
		if (!path.startsWith("http") && !path.startsWith("file://")
				&& !path.startsWith("drawable://")) {
			path = "file://" + path;
		}
		ImageLoader.getInstance().displayImage(path, new ImageViewAware(imageView),
				Global.getGalleryImgOptions(), null, listener, null);
	}

	@Override
	public void clearMemoryCache() {

	}
}
