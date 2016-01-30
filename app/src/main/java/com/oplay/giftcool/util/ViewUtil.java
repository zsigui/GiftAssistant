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
import com.oplay.giftcool.config.IndexTypeUtil;
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
			if (iv.getTag(IndexTypeUtil.TAG_VIEW) == null || !(iv.getTag(IndexTypeUtil.TAG_VIEW)).equals(img)) {
				ImageViewAware viewAware = new ImageViewAware(iv, false);
				ImageLoader.getInstance().displayImage(img, viewAware, Global.IMAGE_OPTIONS);
				iv.setTag(IndexTypeUtil.TAG_VIEW, img);
			}
		} catch (Exception e) {
			// 通常ImageLoader未初始化完成调用报错，先设置默认图片
            iv.setTag(IndexTypeUtil.TAG_VIEW, "error");
			iv.setImageResource(R.drawable.ic_img_default);
		}
	}

	public static void showImage(ImageView iv, int img) {
        iv.setTag(IndexTypeUtil.TAG_VIEW, img);
		if (img == 0) {
			iv.setImageResource(R.drawable.ic_img_default);
		} else {
			iv.setImageResource(img);
		}
	}

    public static void showBannerImage(ImageView ivIcon, String img) {
        if (TextUtils.isEmpty(img)) {
            ivIcon.setTag(IndexTypeUtil.TAG_VIEW, "error");
            ivIcon.setImageResource(R.drawable.ic_banner_default);
        } else if (img.startsWith("drawable://")) {
            ivIcon.setTag(IndexTypeUtil.TAG_VIEW, "empty");
            ivIcon.setImageResource(R.drawable.ic_banner_empty_default);
        } else {
            try {
                if (ivIcon.getTag() == null || !(ivIcon.getTag()).equals(img)) {
                    ImageViewAware viewAware = new ImageViewAware(ivIcon, false);
                    ImageLoader.getInstance().displayImage(img, viewAware, Global.BANNER_IMAGE_LOADER);
                    ivIcon.setTag(IndexTypeUtil.TAG_VIEW, img);
                }
            } catch (Exception e) {
                // 通常ImageLoader未初始化完成调用报错，先设置默认图片
                ivIcon.setTag(IndexTypeUtil.TAG_VIEW, "error");
                ivIcon.setImageResource(R.drawable.ic_avatar_default);
            }
        }
    }

	public static void showAvatarImage(String avatar, ImageView ivIcon, boolean isLogin) {
		if (TextUtils.isEmpty(avatar)) {
			ivIcon.setTag(IndexTypeUtil.TAG_VIEW, avatar);
			if (isLogin) {
				ivIcon.setImageResource(R.drawable.ic_avatar_default);
			} else {
				ivIcon.setImageResource(R.drawable.ic_avator_unlogin);
			}
		} else {
			try {
				if (ivIcon.getTag() == null || !(ivIcon.getTag()).equals(avatar)) {
					ImageViewAware viewAware = new ImageViewAware(ivIcon, false);
					ImageLoader.getInstance().displayImage(avatar, viewAware, Global.AVATAR_IMAGE_LOADER);
					ivIcon.setTag(IndexTypeUtil.TAG_VIEW, avatar);
				}
			} catch (Exception e) {
				// 通常ImageLoader未初始化完成调用报错，先设置默认图片
                ivIcon.setTag(IndexTypeUtil.TAG_VIEW, "error");
				ivIcon.setImageResource(R.drawable.ic_avatar_default);
			}
		}
	}
}
