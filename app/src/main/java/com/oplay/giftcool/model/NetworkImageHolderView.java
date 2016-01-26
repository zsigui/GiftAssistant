package com.oplay.giftcool.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bigkoo.convenientbanner.holder.Holder;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftcool.R;

/**
 * Created by zsigui on 16-1-19.
 */
public class NetworkImageHolderView implements Holder<String> {
	private ImageView imageView;

	@Override
	public View createView(Context context) {
		//你可以通过layout文件来创建，也可以像我一样用代码创建，不一定是Image，任何控件都可以进行翻页
		imageView = (ImageView) LayoutInflater.from(context).inflate(R.layout.view_banner_img, null);
		return imageView;
	}

	@Override
	public void UpdateUI(Context context, int position, String data) {
		if (data.startsWith("drawable://")) {
			imageView.setImageResource(R.drawable.ic_banner_empty_default);
		} else {
			DisplayImageOptions imgOptions = new DisplayImageOptions.Builder()
					.showImageForEmptyUri(R.drawable.ic_banner_default)
					.showImageOnFail(R.drawable.ic_banner_default)
					.showImageOnLoading(R.drawable.ic_banner_default)
					.bitmapConfig(Bitmap.Config.RGB_565)
					.cacheInMemory(true)
					.cacheOnDisk(true)
					.build();
			ImageLoader.getInstance().displayImage(data, imageView, imgOptions);
		}
	}
}