package com.oplay.giftcool.util;

import android.support.annotation.IdRes;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.model.AppStatus;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2015/12/28
 */
public class ViewUtil {
	public static <V extends View> V getViewById(View v, @IdRes int id) {
		View child = v.findViewById(id);
		return (child != null ? (V) child : null);
	}

	public static void initDownloadBtnStatus(TextView view, AppStatus status) {
		switch (status) {
			case OPENABLE:
				view.setText("打开");
				view.setBackgroundResource(R.drawable.selector_btn_blue);
				break;
			case INSTALLABLE:
				view.setText("安装");
				view.setBackgroundResource(R.drawable.selector_btn_blue);
				break;
			case PAUSABLE:
				view.setText("暂停");
				view.setBackgroundResource(R.drawable.selector_btn_grey);
				break;
			case RESUMABLE:
				view.setText("继续");
				view.setBackgroundResource(R.drawable.selector_btn_green);
				break;
			case DOWNLOADABLE:
				view.setText("下载");
				view.setBackgroundResource(R.drawable.selector_btn_green);
				break;
			case RETRYABLE:
				view.setText("重试");
				view.setBackgroundResource(R.drawable.selector_btn_green);
				break;
			default:
				view.setText("失效");
				view.setBackgroundResource(R.drawable.selector_btn_grey);
				break;
		}
	}

	public static void showImage(ImageView iv, String img) {
		try {
			if (iv.getTag() == null || !(iv.getTag()).equals(img)) {
				ImageViewAware viewAware = new ImageViewAware(iv, false);
				ImageLoader.getInstance().displayImage(img, viewAware, Global.IMAGE_OPTIONS);
				iv.setTag(img);
			}
		} catch (Exception e) {
			// 通常ImageLoader未初始化完成调用报错，先设置默认图片
			iv.setImageResource(R.drawable.ic_img_default);
		}
	}

	public static void showImage(ImageView iv, int img) {
		if (img == 0) {
			iv.setImageResource(R.drawable.ic_img_default);
		} else {
			iv.setImageResource(img);
		}
	}

	public static void showAvatarImage(String avatar, ImageView ivIcon, boolean isLogin) {
		if (TextUtils.isEmpty(avatar)) {
			if (isLogin) {
				ivIcon.setImageResource(R.drawable.ic_avatar_default);
			} else {
				ivIcon.setImageResource(R.drawable.ic_avator_unlogin);
			}
		} else {
			try {
				ImageLoader.getInstance().displayImage(avatar, ivIcon, Global.AVATOR_IMAGE_LOADER);
			} catch (Exception e) {
				// 通常ImageLoader未初始化完成调用报错，先设置默认图片
				ivIcon.setImageResource(R.drawable.ic_img_default);
			}
		}
	}
}
