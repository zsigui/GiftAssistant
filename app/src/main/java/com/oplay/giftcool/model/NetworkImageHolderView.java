package com.oplay.giftcool.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bigkoo.convenientbanner.holder.Holder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.util.ThreadUtil;
import com.socks.library.KLog;

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
		if (data.startsWith("drawable://")) {
			imageView.setImageResource(R.drawable.ic_banner_empty_default);
		} else {
			try {
				if (!ImageLoader.getInstance().isInited()) {
					ThreadUtil.runInUIThread(new Runnable() {
						@Override
						public void run() {
							if (ImageLoader.getInstance().isInited()) {
								ImageLoader.getInstance().displayImage(data, imageView, Global.BANNER_IMAGE_LOADER);
							} else {
								ThreadUtil.runInUIThread(this, 2000);
							}

						}
					}, 2000);
				} else {
					ImageLoader.getInstance().displayImage(data, imageView, Global.BANNER_IMAGE_LOADER);
				}
			} catch (Exception e) {
				if (AppDebugConfig.IS_DEBUG) {
					KLog.d(e);
				}
			}
		}
	}
}