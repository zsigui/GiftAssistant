package com.oplay.giftcool.util;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.holder.StyleBaseHolder;
import com.oplay.giftcool.adapter.holder.StyleCouponHolder;
import com.oplay.giftcool.adapter.holder.StyleCouponReserveHolder;
import com.oplay.giftcool.adapter.holder.StyleLabelBaseHolder;
import com.oplay.giftcool.adapter.holder.StyleFreeHolder;
import com.oplay.giftcool.adapter.holder.StyleFreeReserveHolder;
import com.oplay.giftcool.adapter.holder.StyleNormalHolder;
import com.oplay.giftcool.adapter.holder.StylePreciousHolder;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.util.GiftTypeUtil;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.ui.widget.ArrowAnimView;
import com.oplay.giftcool.ui.widget.ClockAnimView;

import java.util.Locale;

/**
 * 专门用于绑定礼包样式的工具类
 * <p/>
 * Created by zsigui on 16-8-9.
 */
public class UiStyleUtil {

    private static Context mContext;

    private static View inflateView(Context context, ViewGroup parent, @LayoutRes int id) {
        return LayoutInflater.from(context).inflate(id, parent, false);
    }

    /**
     * 为按钮和视图绑定基本的监听事件
     */
    public static void bindListener(StyleBaseHolder holder, int tag, int pos, View.OnClickListener listener) {
        holder.itemView.setTag(tag, pos);
        holder.itemView.setOnClickListener(listener);
        if (holder.btnSend != null) {
            holder.btnSend.setTag(tag, pos);
            holder.btnSend.setOnClickListener(listener);
        }
    }

    /**
     * 根据布局类型生成对应的holder
     *
     * @param convertView 如果为null，则会新创建holder，否则直接使用getTag()从View中获取
     */
    public static StyleBaseHolder onCreateHolder(Context context, View convertView, ViewGroup parent,
                                                 int uiStyle, boolean withLine) {
        StyleBaseHolder baseHolder;
        if (convertView == null) {
            switch (uiStyle) {
                case GiftTypeUtil.UI_TYPE_NORMAL_SEARCH:
                case GiftTypeUtil.UI_TYPE_NORMAL_SEIZE:
                case GiftTypeUtil.UI_TYPE_NORMAL_OTHER:
                case GiftTypeUtil.UI_TYPE_NORMAL_WAIT_SEARCH:
                case GiftTypeUtil.UI_TYPE_NORMAL_WAIT_SEIZE:
                    convertView = inflateView(context, parent,
                            withLine ? R.layout.item_ui_style_1_to_5_line : R.layout.item_ui_style_1_to_5);
                    baseHolder = new StyleNormalHolder(convertView);
                    break;
                case GiftTypeUtil.UI_TYPE_FREE_RESERVE:
                    convertView = inflateView(context, parent,
                            withLine ? R.layout.item_ui_style_12_line : R.layout.item_ui_style_12);
                    baseHolder = new StyleFreeReserveHolder(convertView);
                    break;
                case GiftTypeUtil.UI_TYPE_FREE_SEIZE:
                case GiftTypeUtil.UI_TYPE_FREE_WAIT_SEARCH:
                case GiftTypeUtil.UI_TYPE_FREE_SEARCH:
                case GiftTypeUtil.UI_TYPE_FREE_OTHER:
                    convertView = inflateView(context, parent,
                            withLine ? R.layout.item_ui_style_11_13_to_15_line : R.layout.item_ui_style_11_13_to_15);
                    baseHolder = new StyleFreeHolder(convertView);
                    break;
                case GiftTypeUtil.UI_TYPE_COUPON_RESERVE:
                    convertView = inflateView(context, parent,
                            withLine ? R.layout.item_ui_style_17_line : R.layout.item_ui_style_17);
                    baseHolder = new StyleCouponReserveHolder(convertView);
                    break;
                case GiftTypeUtil.UI_TYPE_COUPON_SEIZE:
                case GiftTypeUtil.UI_TYPE_COUPON_OTHER:
                    convertView = inflateView(context, parent,
                            withLine ? R.layout.item_ui_style_16_18_line : R.layout.item_ui_style_16_18);
                    baseHolder = new StyleCouponHolder(convertView);
                    break;
                case GiftTypeUtil.UI_TYPE_PRECIOUS_SEIZE:
                case GiftTypeUtil.UI_TYPE_PRECIOUS_WAIT_SEIZE:
                case GiftTypeUtil.UI_TYPE_PRECIOUS_WAIT_SEARCH:
                case GiftTypeUtil.UI_TYPE_PRECIOUS_SEARCH:
                case GiftTypeUtil.UI_TYPE_PRECIOUS_OTHER:
                default:
                    convertView = inflateView(context, parent,
                            withLine ? R.layout.item_ui_style_6_to_10_line : R.layout.item_ui_style_6_to_10);
                    baseHolder = new StylePreciousHolder(convertView);

            }
            convertView.setTag(baseHolder);
        } else {
            baseHolder = (StyleBaseHolder) convertView.getTag();
        }
        return baseHolder;
    }

    /**
     * 为Holder绑定对应的数据
     */
    public static void bindHolderData(Context context, StyleBaseHolder baseHolder, IndexGiftNew o) {
        mContext = context;
        o.buttonState = (o.buttonState == 0 ? GiftTypeUtil.getButtonState(o) : o.buttonState);
        o.platform = (TextUtils.isEmpty(o.platform) ? "偶玩版" : o.platform);
        bindBaseData(baseHolder, o);
        switch (o.uiStyle) {
            case GiftTypeUtil.UI_TYPE_NORMAL_SEARCH:
            case GiftTypeUtil.UI_TYPE_NORMAL_SEIZE:
            case GiftTypeUtil.UI_TYPE_NORMAL_OTHER:
            case GiftTypeUtil.UI_TYPE_NORMAL_WAIT_SEARCH:
            case GiftTypeUtil.UI_TYPE_NORMAL_WAIT_SEIZE:
                bindNormalHolder((StyleNormalHolder) baseHolder, o);
                break;
            case GiftTypeUtil.UI_TYPE_FREE_RESERVE:
                bindFreeReserveHolder((StyleFreeReserveHolder) baseHolder, o);
                break;
            case GiftTypeUtil.UI_TYPE_FREE_SEIZE:
            case GiftTypeUtil.UI_TYPE_FREE_WAIT_SEARCH:
            case GiftTypeUtil.UI_TYPE_FREE_SEARCH:
            case GiftTypeUtil.UI_TYPE_FREE_OTHER:
                bindFreeHolder((StyleFreeHolder) baseHolder, o);
                break;
            case GiftTypeUtil.UI_TYPE_COUPON_RESERVE:
                bindCouponReserveHolder((StyleCouponReserveHolder) baseHolder, o);
                break;
            case GiftTypeUtil.UI_TYPE_COUPON_SEIZE:
            case GiftTypeUtil.UI_TYPE_COUPON_OTHER:
                bindCouponHolder((StyleCouponHolder) baseHolder, o);
                break;
            case GiftTypeUtil.UI_TYPE_PRECIOUS_SEIZE:
            case GiftTypeUtil.UI_TYPE_PRECIOUS_WAIT_SEIZE:
            case GiftTypeUtil.UI_TYPE_PRECIOUS_WAIT_SEARCH:
            case GiftTypeUtil.UI_TYPE_PRECIOUS_SEARCH:
            case GiftTypeUtil.UI_TYPE_PRECIOUS_OTHER:
            default:
                bindPreciousHolder((StylePreciousHolder) baseHolder, o);
        }
        mContext = null;
    }

    private static void bindCouponReserveHolder(StyleCouponReserveHolder holder, IndexGiftNew o) {
        holder.tvPlatform.setText(o.platform);
        ViewUtil.siteSpendUI(holder.tvSpend, o.score, o.bean, o.priceType);
        ViewUtil.siteValueUI(holder.tvPrice, o.originPrice, true);
    }


    public static void bindCouponHolder(StyleCouponHolder holder, IndexGiftNew o) {
        holder.tvPlatform.setText(o.platform);
        ViewUtil.siteSpendUI(holder.tvSpend, o.score, o.bean, o.priceType);
        ViewUtil.siteValueUI(holder.tvPrice, o.originPrice, true);
        setFreeLabelSeize(holder, o);
        switch (o.uiStyle) {
            case GiftTypeUtil.UI_TYPE_COUPON_SEIZE:
                setProgressBarData(o, holder);
                break;
            case GiftTypeUtil.UI_TYPE_COUPON_OTHER:
            default:
                holder.tvPercent.setVisibility(View.GONE);
                holder.pbPercent.setVisibility(View.GONE);
                break;
        }
    }

    private static void bindFreeHolder(StyleFreeHolder holder, IndexGiftNew o) {
        holder.tvContent.setText(o.content);
        holder.pbPercent.setVisibility(View.GONE);
        holder.tvPercent.setVisibility(View.GONE);
        holder.tvCount.setVisibility(View.GONE);
        holder.tvSpend.setVisibility(View.GONE);
        holder.tvPrice.setVisibility(View.GONE);
        setFreeLabelSeize(holder, o);
        switch (o.uiStyle) {
            case GiftTypeUtil.UI_TYPE_FREE_SEIZE:
                setProgressBarData(o, holder);
                ViewUtil.siteSpendUI(holder.tvSpend, o.score, o.bean, o.priceType);
                ViewUtil.siteValueUI(holder.tvPrice, o.originPrice, true);
                break;
            case GiftTypeUtil.UI_TYPE_FREE_WAIT_SEARCH:
                bindSearchTime(holder.tvCount, o.searchTime);
                break;
            case GiftTypeUtil.UI_TYPE_FREE_SEARCH:
                bindSearchCount(holder.tvCount, String.valueOf(o.searchCount));
                break;
            case GiftTypeUtil.UI_TYPE_FREE_OTHER:
            default:
                ViewUtil.siteSpendUI(holder.tvSpend, o.score, o.bean, o.priceType);
                ViewUtil.siteValueUI(holder.tvPrice, o.originPrice, true);
                break;
        }
    }

    private static void bindFreeReserveHolder(StyleFreeReserveHolder holder, IndexGiftNew o) {
        holder.tvContent.setText(o.content);
        ViewUtil.siteSpendUI(holder.tvSpend, o.score, o.bean, o.priceType);
        ViewUtil.siteValueUI(holder.tvPrice, o.originPrice, true);
    }

    private static void bindPreciousHolder(StylePreciousHolder holder, IndexGiftNew o) {
        holder.pbPercent.setVisibility(View.GONE);
        holder.tvPercent.setVisibility(View.GONE);
        holder.tvCount.setVisibility(View.GONE);
        ViewUtil.siteValueUI(holder.tvPrice, o.originPrice, true);
        switch (o.uiStyle) {
            case GiftTypeUtil.UI_TYPE_PRECIOUS_WAIT_SEIZE:
            case GiftTypeUtil.UI_TYPE_PRECIOUS_SEARCH:
            case GiftTypeUtil.UI_TYPE_PRECIOUS_WAIT_SEARCH:
                holder.tvSpend.setVisibility(View.INVISIBLE);
                switch (o.uiStyle) {
                    case GiftTypeUtil.UI_TYPE_PRECIOUS_SEARCH:
                        bindSearchCount(holder.tvCount, String.valueOf(o.searchCount));
                        break;
                    case GiftTypeUtil.UI_TYPE_PRECIOUS_WAIT_SEARCH:
                        bindSearchTime(holder.tvCount, o.searchTime);
                        break;
                    case GiftTypeUtil.UI_TYPE_PRECIOUS_WAIT_SEIZE:
                        bindSeizeTime(holder.tvCount, o.seizeTime);
                        break;
                }
                break;
            case GiftTypeUtil.UI_TYPE_PRECIOUS_SEIZE:
                setProgressBarData(o, holder);
                ViewUtil.siteSpendUI(holder.tvSpend, o.score, o.bean, o.priceType);
                break;
            case GiftTypeUtil.UI_TYPE_PRECIOUS_OTHER:
            default:
                ViewUtil.siteSpendUI(holder.tvSpend, o.score, o.bean, o.priceType);
                break;
        }
    }

    public static void bindNormalHolder(StyleNormalHolder holder, IndexGiftNew o) {
        holder.tvPercent.setVisibility(View.GONE);
        holder.pbPercent.setVisibility(View.GONE);
        holder.tvContent.setText(o.content);
        holder.tvCount.setVisibility(View.GONE);
        switch (o.uiStyle) {
            case GiftTypeUtil.UI_TYPE_NORMAL_SEARCH:
            case GiftTypeUtil.UI_TYPE_NORMAL_WAIT_SEARCH:
            case GiftTypeUtil.UI_TYPE_NORMAL_WAIT_SEIZE:
                holder.tvSpend.setVisibility(View.GONE);
                switch (o.uiStyle) {
                    case GiftTypeUtil.UI_TYPE_NORMAL_SEARCH:
                        bindSearchCount(holder.tvCount, String.valueOf(o.searchCount));
                        break;
                    case GiftTypeUtil.UI_TYPE_NORMAL_WAIT_SEARCH:
                        bindSearchTime(holder.tvCount, o.searchTime);
                        break;
                    case GiftTypeUtil.UI_TYPE_NORMAL_WAIT_SEIZE:
                        bindSeizeTime(holder.tvCount, o.seizeTime);
                        break;
                }
                break;
            case GiftTypeUtil.UI_TYPE_NORMAL_SEIZE:
                setProgressBarData(o, holder);
                ViewUtil.siteSpendUI(holder.tvSpend, o.score, o.bean, o.priceType);
                break;
            case GiftTypeUtil.UI_TYPE_NORMAL_OTHER:
            default:
                ViewUtil.siteSpendUI(holder.tvSpend, o.score, o.bean, o.priceType);
                break;
        }
    }

    private static void bindSearchTime(TextView tv, String time) {
        tv.setVisibility(View.VISIBLE);
        tv.setText(Html.fromHtml(String.format(Locale.CHINA, ConstString.TEXT_SEARCH,
                DateUtil.formatTime(time, "yyyy-MM-dd HH:mm"))));
    }

    private static void bindSeizeTime(TextView tv, String time) {
        tv.setVisibility(View.VISIBLE);
        tv.setText(Html.fromHtml(String.format(Locale.CHINA, ConstString.TEXT_SEIZE,
                DateUtil.formatTime(time, "yyyy-MM-dd HH:mm"))));
    }

    private static void bindSearchCount(TextView tv, String count) {
        tv.setVisibility(View.VISIBLE);
        tv.setText(Html.fromHtml(String.format(Locale.CHINA, ConstString.TEXT_SEARCHED, count)));
    }

    private static void bindSeizeHint(TextView tv, IndexGiftNew o) {
        if ((o.freeStartTime != 0 && o.freeStartTime * 1000 > System.currentTimeMillis())
                || (o.giftType == GiftTypeUtil.GIFT_TYPE_LIMIT_FREE && o.status == GiftTypeUtil.STATUS_SEIZE)) {
            tv.setText(String.format(Locale.CHINA, "%s免费抢",
                    DateUtil.formatUserReadDate(o.freeStartTime)));
        } else {
            tv.setText("");
        }
    }

    private static void setProgressBarData(IndexGiftNew o, StyleBaseHolder holder) {
        holder.tvPercent.setVisibility(View.VISIBLE);
        holder.pbPercent.setVisibility(View.VISIBLE);
        final int percent = MixUtil.calculatePercent(o.remainCount, o.totalCount);
        holder.tvPercent.setText(String.format(Locale.CHINA, "剩%d%%", percent));
        holder.pbPercent.setProgress(percent);
        holder.pbPercent.setMax(100);
    }

    private static void bindBaseData(StyleBaseHolder holder, IndexGiftNew o) {
        ViewUtil.showImage(holder.ivIcon, o.img);
        holder.tvName.setText(String.format("[%s]%s", o.gameName, o.name));
        if (o.nature == GiftTypeUtil.NATURE_ACTIVITY) {
            bindTitleTag(holder.tvName, R.drawable.ic_tag_activity, R.dimen.di_line_space_extra_big);
        } else if (o.totalType == GiftTypeUtil.TOTAL_TYPE_COUPON) {
            bindTitleTag(holder.tvName, R.drawable.ic_tag_coupon, R.dimen.di_line_space_extra_big);
        } else if (o.exclusive == 1) {
            bindTitleTag(holder.tvName, R.drawable.ic_tag_exclusive, R.dimen.di_line_space_extra_big);
        } else {
            bindTitleTag(holder.tvName, 0, 0);
        }
        if (holder.btnSend != null) {
            if (o.buttonState == GiftTypeUtil.BUTTON_TYPE_ACTIVITY_JOIN) {
                holder.btnSend.setText(mContext.getString(R.string.st_gift_activity_join_s));
            } else {
                holder.btnSend.setState(o.buttonState);
            }
        }
        holder.tvSeizeHint.setVisibility(View.VISIBLE);
        if (o.buttonState == GiftTypeUtil.BUTTON_TYPE_RESERVE_TAKE) {
            holder.tvSeizeHint.setText(mContext.getString(R.string.st_gift_reserve_take_hint));
        } else {
            bindSeizeHint(holder.tvSeizeHint, o);
        }
    }

    private static void bindTitleTag(TextView tv, @DrawableRes int leftTag, @DimenRes int padding) {
        int pad = (padding != 0 ? mContext.getResources().getDimensionPixelSize(padding) : 0);
        tv.setCompoundDrawablePadding(pad);
        tv.setCompoundDrawablesWithIntrinsicBounds(leftTag, 0, 0, 0);
    }

    /**
     * 判断按钮为文字标签时对应的显示
     */
    private static void setFreeLabelSeize(StyleLabelBaseHolder holder, IndexGiftNew o) {
        if (holder.btnSend != null) {
            holder.btnSend.setVisibility(View.INVISIBLE);
        }
        setSeizeTextUI(holder.aavView, holder.cavView, holder.tvSeize, holder.tvSeizeHint, o);
        switch (o.uiStyle) {
            case GiftTypeUtil.UI_TYPE_FREE_SEIZE:
            case GiftTypeUtil.UI_TYPE_COUPON_SEIZE:
                // 显示百分比，隐藏免费抢提示，防止混淆
                holder.tvSeizeHint.setText("");
                break;
            default:
        }
    }

    /**
     * 设置抢号Text的样式
     */
    private static void setSeizeTextUI(ArrowAnimView aav, ClockAnimView cav, TextView tv, TextView hint, IndexGiftNew
            o) {
        tv.setVisibility(View.VISIBLE);
        if ((o.buttonState == GiftTypeUtil.BUTTON_TYPE_SEIZE
                || o.buttonState == GiftTypeUtil.BUTTON_TYPE_RESERVE_TAKE)
                && o.giftType == GiftTypeUtil.GIFT_TYPE_LIMIT_FREE) {
            tv.setTextColor(Global.getRedColor(mContext));
            tv.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            tv.setText(mContext.getString(R.string.st_gift_free_seize));
            tv.setPadding(0, 0, 0, 0);
            aav.setViewVisibility(true);
            cav.setVisibility(View.VISIBLE);
            hint.setText("");
        } else {
            tv.setTextColor(Global.getGreyColor(mContext));
            tv.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
            tv.setPadding(0, 0, mContext.getResources().getDimensionPixelSize(R.dimen.di_line_space_extra_big), 0);
            aav.setViewVisibility(false);
            cav.setVisibility(View.GONE);
            if (o.giftType == GiftTypeUtil.GIFT_TYPE_LIMIT_FREE) {
                hint.setText("");
            }
            switch (o.buttonState) {
                case GiftTypeUtil.BUTTON_TYPE_SEARCH:
                    tv.setText(mContext.getString(R.string.st_gift_search));
                    break;
                case GiftTypeUtil.BUTTON_TYPE_WAIT_SEARCH:
                    tv.setText(mContext.getString(R.string.st_gift_wait_search));
                    break;
                case GiftTypeUtil.BUTTON_TYPE_RESERVE:
                case GiftTypeUtil.BUTTON_TYPE_RESERVE_EMPTY:
                    tv.setText(mContext.getString(R.string.st_gift_reserve));
                    break;
                case GiftTypeUtil.BUTTON_TYPE_RESERVED:
                    tv.setText(mContext.getString(R.string.st_gift_reserved));
                    break;
                case GiftTypeUtil.BUTTON_TYPE_RESERVE_TAKE:
                    tv.setText(mContext.getString(R.string.st_gift_take));
                    break;
                case GiftTypeUtil.BUTTON_TYPE_FINISH:
                case GiftTypeUtil.BUTTON_TYPE_TAKE_OFF:
                case GiftTypeUtil.BUTTON_TYPE_EMPTY:
                    tv.setText(mContext.getString(R.string.st_gift_empty));
                    break;
                case GiftTypeUtil.BUTTON_TYPE_SEIZED:
                    tv.setText(mContext.getString(R.string.st_gift_seized));
                    break;
                case GiftTypeUtil.BUTTON_TYPE_WAIT_SEIZE:
                    if (o.freeStartTime * 1000 > System.currentTimeMillis()) {
                        tv.setText(String.format(Locale.CHINA, "%s免费抢",
                                DateUtil.formatUserReadDate(o.freeStartTime)));
                        hint.setText("");
                    } else {
                        tv.setText(String.format(Locale.CHINA, "%s抢",
                                DateUtil.formatUserReadDate(DateUtil.getTime(o.seizeTime) / 1000)));
                    }
                    break;
            }
        }
        if (o.status == GiftTypeUtil.STATUS_SEIZE && o.giftType != GiftTypeUtil.GIFT_TYPE_LIMIT_FREE) {
            hint.setText(mContext.getString(R.string.st_gift_normal_seize_hint));
            hint.setTextColor(Global.getRedColor(mContext));
        } else {
            hint.setTextColor(Global.getGreyColor(mContext));
        }
    }


}
