package com.oplay.giftcool.util;

import android.content.Context;
import android.os.Build;
import android.support.annotation.IdRes;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.util.IndexTypeUtil;
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


	/**
	 * 设置礼包'价值:￥5.00'的显示方式
	 */
	public static void siteValueUI(TextView tv, int value, boolean delete) {
		Context context = tv.getContext();
		final int originSize = 7;
		final String s = String.format("价值:￥%d.00", value);
		final int moneyLength = s.length() - originSize;
		SpannableString ss = new SpannableString(s);
		if (delete) {
			ss.setSpan(new TextAppearanceSpan(context, R.style.DefaultTextView_ItemSubTitle_S1),
					0, 4, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
			ss.setSpan(new TextAppearanceSpan(context, R.style.DefaultTextView_ItemSubTitle_S2),
					3, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
			ss.setSpan(new TextAppearanceSpan(context, R.style.DefaultTextView_ItemSubTitle_S2_S3),
					4, 5 + moneyLength, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
			ss.setSpan(new TextAppearanceSpan(context, R.style.DefaultTextView_ItemSubTitle_S2_S4),
					5 + moneyLength, s.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		} else {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
				ss.setSpan(new ForegroundColorSpan(
								context.getResources().getColor(R.color.co_common_app_second_bg_orange)),
						3, s.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
			} else {
				ss.setSpan(new ForegroundColorSpan(
								context.getResources().getColor(R.color.co_common_app_second_bg_orange, null)),
						3, s.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
			}
			ss.setSpan(new StrikethroughSpan(), 0, s.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		}
		tv.setText(ss, TextView.BufferType.SPANNABLE);
	}

	public static void showImage(ImageView iv, String img) {
		try {
			if (iv.getTag(IndexTypeUtil.TAG_VIEW) == null || !(iv.getTag(IndexTypeUtil.TAG_VIEW)).equals(img)) {
				ImageViewAware viewAware = new ImageViewAware(iv, false);
				ImageLoader.getInstance().displayImage(img, viewAware, Global.getDefaultImgOptions());
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
					ImageLoader.getInstance().displayImage(img, viewAware, Global.getBannerImgOptions());
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
					ImageLoader.getInstance().displayImage(avatar, viewAware, Global.getAvatarImgOptions());
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
