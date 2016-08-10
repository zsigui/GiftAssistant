package com.oplay.giftcool.util;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.LayoutRes;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.holder.StyleBaseHolder;
import com.oplay.giftcool.adapter.holder.StyleCouponHolder;
import com.oplay.giftcool.adapter.holder.StyleFreeBaseHolder;
import com.oplay.giftcool.adapter.holder.StyleFreeCouponHolder;
import com.oplay.giftcool.adapter.holder.StyleFreeGiftHolder;
import com.oplay.giftcool.adapter.holder.StyleLimitHolder;
import com.oplay.giftcool.adapter.holder.StyleNormalHolder;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.util.GiftTypeUtil;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;

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
        holder.btnSend.setTag(tag, pos);
        holder.itemView.setOnClickListener(listener);
        holder.btnSend.setOnClickListener(listener);
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
                case GiftTypeUtil.UI_TYPE_NORMAL_SEIZED:
                case GiftTypeUtil.UI_TYPE_NORMAL_WAIT_SEARCH:
                case GiftTypeUtil.UI_TYPE_NORMAL_WAITE_SEIZE:
                    convertView = inflateView(context, parent,
                            withLine ? R.layout.item_ui_style_normal_with_line : R.layout.item_ui_style_normal);
                    baseHolder = new StyleNormalHolder(convertView);
                    break;
                case GiftTypeUtil.UI_TYPE_COUPON:
                    convertView = inflateView(context, parent,
                            withLine ? R.layout.item_ui_style_coupon_with_line : R.layout.item_ui_style_coupon);
                    baseHolder = new StyleCouponHolder(convertView);
                    break;
                case GiftTypeUtil.UI_TYPE_FREE_COUPON:
                    convertView = inflateView(context, parent,
                            withLine ? R.layout.item_ui_style_coupon_free_with_line : R.layout.item_ui_style_coupon_free);
                    baseHolder = new StyleFreeCouponHolder(convertView);
                    break;
                case GiftTypeUtil.UI_TYPE_FREE_GIFT:
                    convertView = inflateView(context, parent,
                            withLine ? R.layout.item_ui_style_limit_free_with_line : R.layout.item_ui_style_limit_free);
                    baseHolder = new StyleFreeGiftHolder(convertView);
                    break;
                case GiftTypeUtil.UI_TYPE_LIMIT:
                default:
                    convertView = inflateView(context, parent,
                            withLine ? R.layout.item_ui_style_normal_with_line : R.layout.item_ui_style_normal);
                    baseHolder = new StyleLimitHolder(convertView);

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
        switch (o.uiStyle) {
            case GiftTypeUtil.UI_TYPE_NORMAL_SEARCH:
            case GiftTypeUtil.UI_TYPE_NORMAL_SEIZE:
            case GiftTypeUtil.UI_TYPE_NORMAL_SEIZED:
            case GiftTypeUtil.UI_TYPE_NORMAL_WAIT_SEARCH:
            case GiftTypeUtil.UI_TYPE_NORMAL_WAITE_SEIZE:
                bindNormalHolder((StyleNormalHolder) baseHolder, o);
                break;
            case GiftTypeUtil.UI_TYPE_COUPON:
                bindCouponHolder((StyleCouponHolder) baseHolder, o);
                break;
            case GiftTypeUtil.UI_TYPE_FREE_COUPON:
                bindFreeCouponHolder((StyleFreeCouponHolder) baseHolder, o);
                break;
            case GiftTypeUtil.UI_TYPE_FREE_GIFT:
                bindFreeGiftHolder((StyleFreeGiftHolder) baseHolder, o);
                break;
            case GiftTypeUtil.UI_TYPE_LIMIT:
            default:
                bindLimitHolder((StyleLimitHolder) baseHolder, o);
        }
        mContext = null;
    }

    private static void bindLimitHolder(StyleLimitHolder holder, IndexGiftNew o) {
        bindBaseData(holder, o);
        holder.pbPercent.setVisibility(View.GONE);
        holder.tvPercent.setVisibility(View.GONE);
        holder.tvCount.setVisibility(View.GONE);
        holder.tvPrice.setVisibility(View.VISIBLE);
        holder.tvContent.setText(o.content);
        ViewUtil.siteSpendUI(holder.tvSpend, o.score, o.bean, o.priceType);
        ViewUtil.siteValueUI(holder.tvPrice, o.originPrice, true);
    }

    public static void bindFreeGiftHolder(StyleFreeGiftHolder holder, IndexGiftNew o) {
        bindBaseData(holder, o);
        holder.tvPercent.setVisibility(View.GONE);
        holder.pbPercent.setVisibility(View.GONE);
        ViewUtil.siteSpendUI(holder.tvSpend, o.score, o.bean, o.priceType);
        ViewUtil.siteValueUI(holder.tvPrice, o.originPrice, true);
        holder.tvContent.setText(o.content);
        setFreeSeize(holder, o);
    }

    public static void bindFreeCouponHolder(StyleFreeCouponHolder holder, IndexGiftNew o) {
        bindBaseData(holder, o);
        holder.tvPlatform.setText(o.platform);
        ViewUtil.siteSpendUI(holder.tvSpend, o.score, o.bean, o.priceType);
        ViewUtil.siteValueUI(holder.tvPrice, o.originPrice, true);
        holder.tvPercent.setVisibility(View.GONE);
        holder.pbPercent.setVisibility(View.GONE);
        setFreeSeize(holder, o);
    }

    public static void bindCouponHolder(StyleCouponHolder holder, IndexGiftNew o) {
        bindBaseData(holder, o);
        holder.tvPlatform.setText(o.platform);
        ViewUtil.siteSpendUI(holder.tvSpend, o.score, o.bean, o.priceType);
        ViewUtil.siteValueUI(holder.tvPrice, o.originPrice, true);
    }

    public static void bindNormalHolder(StyleNormalHolder holder, IndexGiftNew o) {
        holder.tvContent.setText(o.content);
        switch (o.uiStyle) {
            case GiftTypeUtil.UI_TYPE_NORMAL_SEARCH:
            case GiftTypeUtil.UI_TYPE_NORMAL_WAIT_SEARCH:
            case GiftTypeUtil.UI_TYPE_NORMAL_WAITE_SEIZE:
                setNormalDisabled(o, holder);
                switch (o.uiStyle) {

                    case GiftTypeUtil.UI_TYPE_NORMAL_SEARCH:
                        setDisabledText(holder.tvCount, Html.fromHtml(String.format("已淘数：<font " +
                                        "color='#ffaa17'>%s</font>",
                                o.searchCount)));
                        break;
                    case GiftTypeUtil.UI_TYPE_NORMAL_WAIT_SEARCH:
                        holder.tvCount.setText(Html.fromHtml(String.format("开淘时间：<font " +
                                        "color='#ffaa17'>%s</font>",
                                DateUtil.formatTime(o.searchTime, "yyyy-MM-dd HH:mm"))));
                        break;
                    case GiftTypeUtil.UI_TYPE_NORMAL_WAITE_SEIZE:
                        holder.tvCount.setText(Html.fromHtml(String.format("开抢时间：<font " +
                                        "color='#ffaa17'>%s</font>",
                                DateUtil.formatTime(o.seizeTime, "yyyy-MM-dd HH:mm"))));
                        break;
                }
                break;
            case GiftTypeUtil.UI_TYPE_NORMAL_SEIZE:
                bindBaseData(holder, o);
                holder.tvSpend.setVisibility(View.VISIBLE);
                holder.tvCount.setVisibility(View.GONE);
                setProgressBarData(o, holder);
                ViewUtil.siteSpendUI(holder.tvSpend, o.score, o.bean, o.priceType);
                break;
            case GiftTypeUtil.UI_TYPE_NORMAL_SEIZED:
                bindBaseData(holder, o);
                holder.tvPercent.setVisibility(View.GONE);
                holder.pbPercent.setVisibility(View.GONE);
                holder.tvSpend.setVisibility(View.VISIBLE);
                holder.tvCount.setVisibility(View.GONE);
                ViewUtil.siteSpendUI(holder.tvSpend, o.score, o.bean, o.priceType);
                break;
        }
    }

    private static void setFreeSeize(StyleFreeBaseHolder holder, IndexGiftNew o) {
        switch (o.buttonState) {
            case GiftTypeUtil.BUTTON_TYPE_RESERVE:
            case GiftTypeUtil.BUTTON_TYPE_RESERVE_EMPTY:
            case GiftTypeUtil.BUTTON_TYPE_RESERVE_TAKE:
            case GiftTypeUtil.BUTTON_TYPE_RESERVED:
                holder.btnSend.setVisibility(View.VISIBLE);
                holder.tvSeize.setVisibility(View.GONE);
                break;
            case GiftTypeUtil.BUTTON_TYPE_SEIZE:
                setProgressBarData(o, holder);
                holder.btnSend.setVisibility(View.INVISIBLE);
                setSeizeTextUI(holder.tvSeize, o);
                holder.tvSeizeHint.setText("");
                break;
            default:
                holder.btnSend.setVisibility(View.INVISIBLE);
                setSeizeTextUI(holder.tvSeize, o);

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

    private static void setNormalDisabled(IndexGiftNew o, StyleNormalHolder holder) {
        bindBaseData(holder, o);
        holder.tvContent.setText(o.content);
        holder.tvSpend.setVisibility(View.GONE);
        holder.tvPercent.setVisibility(View.GONE);
        holder.pbPercent.setVisibility(View.GONE);
        holder.tvCount.setVisibility(View.VISIBLE);
    }

    private static void bindBaseData(StyleBaseHolder holder, IndexGiftNew o) {
        ViewUtil.showImage(holder.ivIcon, o.img);
        holder.tvName.setText(String.format("[%s]%s", o.gameName, o.name));
//        holder.btnSend.setState(GiftTypeUtil.getButtonState(o));
        holder.btnSend.setState(o.buttonState);
        holder.tvSeizeHint.setVisibility(View.VISIBLE);
        if (o.buttonState == GiftTypeUtil.BUTTON_TYPE_RESERVE_TAKE) {
            holder.tvSeizeHint.setText(mContext.getString(R.string.st_gift_reserve_take_hint));
        } else if (o.freeStartTime * 1000 > System.currentTimeMillis()) {
            holder.tvSeizeHint.setText(String.format(Locale.CHINA,
                    ConstString.TEXT_GIFT_FREE_SEIZE, DateUtil.formatUserReadDate(o.freeStartTime)));
        } else {
            holder.tvSeizeHint.setText("");
        }
    }

    private static void setDisabledText(TextView tv, Spanned text) {
        tv.setVisibility(View.VISIBLE);
        tv.setText(text);
    }

    /**
     * 设置抢号Text的样式
     */
    private static void setSeizeTextUI(TextView tv, IndexGiftNew o) {
        tv.setVisibility(View.VISIBLE);
        if (o.buttonState == GiftTypeUtil.BUTTON_TYPE_SEIZE
                || o.buttonState == GiftTypeUtil.BUTTON_TYPE_RESERVE_TAKE) {
            tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_red_right, 0);
            tv.setTextColor(Global.getRedColor(mContext));
            tv.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            tv.setText(mContext.getString(R.string.st_gift_free_seize));
        } else {
            tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            tv.setTextColor(Global.getGreyColor(mContext));
            tv.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
            switch (o.buttonState) {
                case GiftTypeUtil.BUTTON_TYPE_SEARCH:
                    tv.setText(mContext.getString(R.string.st_gift_search));
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
                    tv.setText(String.format(Locale.CHINA, "%s抢",
                            DateUtil.formatUserReadDate(DateUtil.getTime(o.seizeTime))));
                    break;
            }
        }
    }
}
