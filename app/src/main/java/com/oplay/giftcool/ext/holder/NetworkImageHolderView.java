package com.oplay.giftcool.ext.holder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bigkoo.convenientbanner.holder.Holder;
import com.oplay.giftcool.R;
import com.oplay.giftcool.util.ViewUtil;

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
	public void UpdateUI(Context context, int position, final String data) {
        ViewUtil.showBannerImage(imageView, data);
	}
}