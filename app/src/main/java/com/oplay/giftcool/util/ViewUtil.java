package com.oplay.giftcool.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.text.util.Linkify;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.util.GiftTypeUtil;
import com.oplay.giftcool.config.util.IndexTypeUtil;
import com.oplay.giftcool.model.AppStatus;
import com.oplay.giftcool.model.data.resp.GameDownloadInfo;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2015/12/28
 */
public class ViewUtil {
    @SuppressWarnings("unchecked")
    public static <V extends View> V getViewById(View v, @IdRes int id) {
        View child = v.findViewById(id);
        return (child != null ? (V) child : null);
    }

    public static void initDownloadBtnStatus(TextView view, AppStatus status) {
        view.setTextColor(getColorState(view.getContext(), R.color.color_btn_blue_border));
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
                view.setTextColor(getColorState(view.getContext(), R.color.color_btn_grey_border));
                view.setBackgroundResource(R.drawable.selector_btn_grey);
                break;
            case RESUMABLE:
                view.setText("继续");
                view.setTextColor(getColorState(view.getContext(), R.color.color_btn_green_border));
                view.setBackgroundResource(R.drawable.selector_btn_green);
                break;
            case DOWNLOADABLE:
                view.setText("下载");
                view.setTextColor(getColorState(view.getContext(), R.color.color_btn_green_border));
                view.setBackgroundResource(R.drawable.selector_btn_green);
                break;
            case RETRYABLE:
                view.setText("重试");
                view.setTextColor(getColorState(view.getContext(), R.color.color_btn_green_border));
                view.setBackgroundResource(R.drawable.selector_btn_green);
                break;
            default:
                view.setText("失效");
                view.setBackgroundResource(R.drawable.selector_btn_grey);
                view.setTextColor(getColorState(view.getContext(), R.color.color_btn_grey_border));
                break;
        }
    }

    public static void enableDownload(TextView tv, GameDownloadInfo o) {
        if (o.downloadState == 0 || TextUtils.isEmpty(o.downloadUrl)) {
            // 没有下载
            tv.setEnabled(false);
            tv.setText("下载");
        } else {
            tv.setEnabled(true);
            ViewUtil.initDownloadBtnStatus(tv, o.appStatus);
        }
    }


    /**
     * 设置礼包'￥5.00'的显示方式
     */
    @SuppressLint("NewApi")
    public static void siteValueUI(TextView tv, float value, boolean delete) {
        if (tv == null) {
            return;
        }
//        Context context = tv.getContext();
//        final int originSize = 4;
//        final String s = String.format(Locale.CHINA, "￥%.2f", value);
//        final int moneyLength = s.length() - originSize;
//        SpannableString ss = new SpannableString(s);
//        ss.setSpan(new TextAppearanceSpan(context, R.style.DefaultTextView_ItemSubTitle_S1_S2),
//                0, 2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//        ss.setSpan(new TextAppearanceSpan(context, R.style.DefaultTextView_ItemSubTitle_S1_S2_S3),
//                1, 2 + moneyLength, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//        ss.setSpan(new TextAppearanceSpan(context, R.style.DefaultTextView_ItemSubTitle_S1_S2_S4),
//                2 + moneyLength, s.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//        tv.setText(ss, TextView.BufferType.NORMAL);
//        if (delete && tv instanceof DeletedTextView) {
//            ((DeletedTextView) tv).setPaint(getColor(context, R.color.co_btn_grey_pressed), 3);
//        }
//        tv.setVisibility(View.VISIBLE);
        final String s = String.format(Locale.CHINA, "原价值: [gold] %.0f", value * 100);
        SpannableString ss = new SpannableString(s);
        ss.setSpan(Global.getScoreSpan(tv.getContext()), 5, 11, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        ss.setSpan(Global.getStrikeSpan(), 12, s.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        tv.setText(ss, TextView.BufferType.SPANNABLE);
        tv.setVisibility(View.VISIBLE);
    }

    /**
     * 设置花费的金币或偶玩豆样式
     */
    public static void siteSpendUI(TextView tv, int score, int bean, int type) {
        if (tv == null) {
            return;
        }
        final ImageSpan DRAWER_GOLD = Global.getScoreSpan(tv.getContext());
        final ImageSpan DRAWER_BEAN = Global.getBeanSpan(tv.getContext());
        tv.setVisibility(View.VISIBLE);
        switch (type) {
            case GiftTypeUtil.PAY_TYPE_BEAN: {
                SpannableString ss = new SpannableString(String.format(Locale.CHINA, "[bean] %d", bean));
                ss.setSpan(DRAWER_BEAN, 0, 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                tv.setText(ss, TextView.BufferType.SPANNABLE);
                break;
            }
            case GiftTypeUtil.PAY_TYPE_SCORE: {
                SpannableString ss = new SpannableString(String.format(Locale.CHINA, "[gold] %d", score));
                ss.setSpan(DRAWER_GOLD, 0, 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                tv.setText(ss, TextView.BufferType.SPANNABLE);
                break;
            }
            default: {
                SpannableString ss = new SpannableString(String.format(Locale.CHINA, "[gold] %d  [bean] %d",
                        score, bean));
                final int startPos = String.valueOf(score).length() + 9;
                ss.setSpan(DRAWER_GOLD, 0, 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                ss.setSpan(DRAWER_BEAN, startPos, startPos + 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                tv.setText(ss, TextView.BufferType.SPANNABLE);
            }

        }
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
                ivIcon.setImageResource(R.drawable.ic_avatar_un_login);
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

    public static int getColor(Context context, @ColorRes int colorId) {
        int colorInt;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            colorInt = context.getColor(colorId);
        } else {
            colorInt = context.getResources().getColor(colorId);
        }
        return colorInt;
    }

    public static ColorStateList getColorState(Context context, @ColorRes int colorId) {
        ColorStateList colorInt;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            colorInt = context.getResources().getColorStateList(colorId, null);
        } else {
            colorInt = context.getResources().getColorStateList(colorId);
        }
        return colorInt;
    }

    public static void handleLink(TextView tv, String text, String protocol) {
        if (tv == null) {
            return;
        }
        text = text.toLowerCase();
        if (TextUtils.isEmpty(protocol) || !text.contains(protocol)) {
            tv.setAutoLinkMask(Linkify.ALL);
            tv.setText(text);
        } else {
            tv.setText(text);
            Linkify.addLinks(tv, Pattern.compile(protocol + "://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"),
                    protocol);
        }
    }

}
