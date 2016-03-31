package com.oplay.giftcool.util;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by zsigui on 16-3-30.
 */
public class ImageLoaderUtil {

	public static synchronized void resume() {
		if (ImageLoader.getInstance().isInited()) {
			ImageLoader.getInstance().resume();
		}
	}

	public static synchronized void stop() {
		if (ImageLoader.getInstance().isInited()) {
			ImageLoader.getInstance().stop();
		}
	}
}
