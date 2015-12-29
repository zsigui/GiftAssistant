package com.oplay.giftassistant.config;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.oplay.giftassistant.AssistantApp;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.engine.NetEngine;

/**
 * Created by zsigui on 15-12-16.
 */
public class Global {
	private static NetEngine sNetEngine;
	public static final String BASE_URL = "http://172.16.3.59:8987/";

	public static final DisplayImageOptions IMAGE_OPTIONS = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.ic_img_empty)
			.showImageOnFail(R.drawable.ic_img_fail)
			.showImageOnLoading(R.drawable.ic_img_loading)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.delayBeforeLoading(100)
			.build();

	public static NetEngine getNetEngine() {
		if (sNetEngine == null) {
			sNetEngine =AssistantApp.getInstance().getRetrofit().create(NetEngine.class);
		}
		return sNetEngine;
	}
}
