package com.oplay.giftcool.assist;

import android.app.Activity;
import android.graphics.drawable.Drawable;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
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
//		if (path.startsWith("drawable://")) {
//			// 对于资源图，直接切割然后显示
//			final String temp = path.substring(11, path.length()).trim();
//			if (!TextUtils.isEmpty(temp)) {
//				final int resId = Integer.parseInt(temp);
//				ViewGroup.LayoutParams lp = imageView.getLayoutParams();
//				lp.width = width;
//				lp.height = height;
//				imageView.setLayoutParams(lp);
//				imageView.setImageResource(resId);
//			}
//		} else {
		if (!path.startsWith("http") && !path.startsWith("file://")
				&& !path.startsWith("drawable://")) {
			path = "file://" + path;
		}
		ImageSize imageSize = new ImageSize(width, height);
		ImageLoader.getInstance().displayImage(path, new ImageViewAware(imageView),
				Global.getGalleryImgOptions(), imageSize, null, null);
//		}
	}

	@Override
	public void clearMemoryCache() {

	}
}
