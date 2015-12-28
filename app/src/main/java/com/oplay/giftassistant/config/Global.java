package com.oplay.giftassistant.config;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.oplay.giftassistant.R;

/**
 * Created by zsigui on 15-12-16.
 */
public class Global {
	public static final String BASE_URL = "http://172.16.3.59:8987/";

	public static final DisplayImageOptions IMAGE_OPTIONS = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.ic_img_empty)
			.showImageOnFail(R.drawable.ic_img_fail)
			.showImageOnLoading(R.drawable.ic_img_loading)
			.build();
}
