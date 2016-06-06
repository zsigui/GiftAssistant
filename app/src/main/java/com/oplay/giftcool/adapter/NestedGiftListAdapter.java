package com.oplay.giftcool.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseListAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.util.GiftTypeUtil;
import com.oplay.giftcool.listener.OnFinishListener;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.ui.widget.DeletedTextView;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.ViewUtil;
import com.socks.library.KLog;

import java.util.List;
import java.util.Locale;

/**
 * Created by zsigui on 15-12-24.
 */
public class NestedGiftListAdapter extends BaseListAdapter<IndexGiftNew> implements View.OnClickListener,
        OnFinishListener {

    final int COLOR_GREY;
    final int COLOR_RED;
    final ImageSpan DRAWER_GOLD;
    final ImageSpan DRAWER_BEAN;
    final int W_DIVIDER;

    private OnItemClickListener<IndexGiftNew> mListener;


    public NestedGiftListAdapter(Context context) {
        this(context, null);
    }

    public NestedGiftListAdapter(Context context, List<IndexGiftNew> objects) {
        super(context, objects);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            COLOR_GREY = context.getColor(R.color.co_common_text_second);
            COLOR_RED = context.getColor(R.color.co_common_app_main_bg);
        } else {
            COLOR_GREY = context.getResources().getColor(R.color.co_common_text_second);
            COLOR_RED = context.getResources().getColor(R.color.co_common_app_main_bg);
        }
        W_DIVIDER = context.getResources().getDimensionPixelSize(R.dimen.di_divider_height);
        DRAWER_GOLD = new ImageSpan(context, R.drawable.ic_score);
        DRAWER_BEAN = new ImageSpan(context, R.drawable.ic_bean);
    }

    public void updateData(List<IndexGiftNew> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    public OnItemClickListener<IndexGiftNew> getListener() {
        return mListener;
    }

    public void setListener(OnItemClickListener<IndexGiftNew> listener) {
        mListener = listener;
    }

    public List<IndexGiftNew> getData() {
        return mData;
    }

    public void setData(List<IndexGiftNew> data) {
        mData = data;
    }

    @Override
    public int getViewTypeCount() {
        return GiftTypeUtil.TYPE_COUNT;
    }

    /**
     * 获取ListItem类型<br/>
     * 注意: 返回的 int 需要范围为 0 ~ getViewTypeCount() - 1, 否则会出现ArrayIndexOutOfBoundsException
     */
    @Override
    public int getItemViewType(int position) {
        return GiftTypeUtil.getItemViewType(mData.get(position));
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getCount() == 0) {
            return null;
        }

        int type = getItemViewType(position);

        IndexGiftNew o = getItem(position);

        switch (type) {
            case GiftTypeUtil.TYPE_ERROR:
                return LayoutInflater.from(mContext).inflate(R.layout.xml_null, parent, false);
            case GiftTypeUtil.TYPE_CHARGE_UN_RESERVE:
            case GiftTypeUtil.TYPE_CHARGE_DISABLE_RESERVE:
            case GiftTypeUtil.TYPE_CHARGE_SEIZE:
            case GiftTypeUtil.TYPE_CHARGE_RESERVED:
            case GiftTypeUtil.TYPE_CHARGE_TAKE:
            case GiftTypeUtil.TYPE_CHARGE_SEIZED:
            case GiftTypeUtil.TYPE_CHARGE_EMPTY:
            case GiftTypeUtil.TYPE_CHARGE_RESERVE_EMPTY:
                ChargeHolder cHolder;
                if (convertView == null) {
                    cHolder = new ChargeHolder();
                    convertView = inflateChargeHolder(parent, cHolder);
                } else {
                    cHolder = (ChargeHolder) convertView.getTag();
                }
                cHolder.btnSend.setTag(TAG_POSITION, position);
                cHolder.btnSend.setOnClickListener(this);
                handleFirstCharge(type, o, cHolder);
                break;
            // 首充券一类结束
            case GiftTypeUtil.TYPE_NORMAL_WAIT_SEIZE:
            case GiftTypeUtil.TYPE_NORMAL_SEIZE:
            case GiftTypeUtil.TYPE_NORMAL_WAIT_SEARCH:
            case GiftTypeUtil.TYPE_NORMAL_SEARCH:
            case GiftTypeUtil.TYPE_NORMAL_FINISHED:
            case GiftTypeUtil.TYPE_NORMAL_SEARCHED:
            case GiftTypeUtil.TYPE_NORMAL_SEIZED:
                GiftNormalHolder nHolder;
                if (convertView == null) {
                    nHolder = new GiftNormalHolder();
                    convertView = inflateGiftNormalHolder(parent, nHolder);
                } else {
                    nHolder = (GiftNormalHolder) convertView.getTag();
                }
                nHolder.btnSend.setTag(TAG_POSITION, position);
                nHolder.btnSend.setOnClickListener(this);
                handleGiftNormalCharge(type, o, nHolder);
                break;
            // 普通礼包结束
            default:
                GiftLimitFeeHolder gHolder;
                if (convertView == null) {
                    gHolder = new GiftLimitFeeHolder();
                    convertView = inflateGiftFeeHolder(parent, gHolder);
                } else {
                    gHolder = (GiftLimitFeeHolder) convertView.getTag();
                }
                handleGiftFee(type, o, gHolder);
                break;
        }
        convertView.setTag(TAG_POSITION, position);
        convertView.setOnClickListener(this);

        return convertView;
    }

    /**
     * 处理普通礼包样式设置逻辑
     */
    @NonNull
    private View inflateGiftNormalHolder(ViewGroup parent, GiftNormalHolder holder) {
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_index_gift_new_list, parent, false);
        holder.ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
        holder.tvName = ViewUtil.getViewById(convertView, R.id.tv_name);
        holder.tvCount = ViewUtil.getViewById(convertView, R.id.tv_count);
        holder.btnSend = ViewUtil.getViewById(convertView, R.id.btn_send);
        holder.tvPercent = ViewUtil.getViewById(convertView, R.id.tv_percent);
        holder.pbPercent = ViewUtil.getViewById(convertView, R.id.pb_percent);
        holder.tvMoney = ViewUtil.getViewById(convertView, R.id.tv_money);
        holder.tvContent = ViewUtil.getViewById(convertView, R.id.tv_content);
        convertView.setTag(holder);
        return convertView;
    }

    /**
     * 处理免费礼包样式设置逻辑
     */
    private void handleGiftFee(int type, IndexGiftNew o, GiftLimitFeeHolder holder) {
        ViewUtil.showImage(holder.ivIcon, o.img);
        holder.tvName.setText(String.format("[%s]%s", o.gameName, o.name));
        SpannableString ss = new SpannableString(String.format(Locale.CHINA, "[gold] %d 或 [bean] %d", o.score, o.bean));
        final int startPos = String.valueOf(o.score).length() + 10;
        ss.setSpan(DRAWER_GOLD, 0, 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        ss.setSpan(DRAWER_BEAN, startPos, startPos + 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        holder.tvPrice.setText(o.content);
        holder.tvPrice.setPadding(0, 6, 0, 0);
        holder.btnSend.setState(type);
        switch (type) {
            case GiftTypeUtil.TYPE_LIMIT_SEIZE:
                if (o.freeStartTime != 0 && System.currentTimeMillis() < o.freeStartTime * 1000) {
                    // 限量抢状态,表示当前不处于免费抢
                    holder.tvSeizeHint.setVisibility(View.VISIBLE);
                    holder.tvSeizeHint.setText(String.format(Locale.CHINA,
                            ConstString.TEXT_GIFT_FREE_SEIZE, DateUtil.formatUserReadDate(o.freeStartTime)));
                } else {
                    // 无免费
                    holder.tvSeizeHint.setVisibility(View.GONE);
                }
                setProgressBarData(o, holder);
                break;
            case GiftTypeUtil.TYPE_LIMIT_FREE_SEIZE:
                holder.tvMoney.setPaint(COLOR_GREY, W_DIVIDER);
                holder.tvSeizeHint.setVisibility(View.VISIBLE);
                holder.tvSeizeHint.setText("正在免费抢");
                setProgressBarData(o, holder);
                break;
            case GiftTypeUtil.TYPE_LIMIT_FREE_SEIZED:
            case GiftTypeUtil.TYPE_LIMIT_SEIZED:
            case GiftTypeUtil.TYPE_LIMIT_WAIT_SEIZE:
            case GiftTypeUtil.TYPE_LIMIT_FREE_EMPTY:
            case GiftTypeUtil.TYPE_LIMIT_FREE_WAIT_SEIZE:
            case GiftTypeUtil.TYPE_LIMIT_EMPTY:
            case GiftTypeUtil.TYPE_LIMIT_FINISHED:
                holder.pbPercent.setVisibility(View.GONE);
                holder.tvPercent.setVisibility(View.GONE);
                holder.tvSeizeHint.setVisibility(View.GONE);
                break;
        }
        holder.tvMoney.setText(ss, TextView.BufferType.SPANNABLE);
    }

    private void setProgressBarData(IndexGiftNew o, GiftLimitFeeHolder gHolder) {
        gHolder.tvPercent.setVisibility(View.VISIBLE);
        gHolder.pbPercent.setVisibility(View.VISIBLE);
        final int percent = (int) ((float) o.remainCount * 100 / o.totalCount);
        gHolder.tvPercent.setText(String.format(Locale.CHINA, "剩余%d%%", percent));
        gHolder.pbPercent.setProgress(percent);
        gHolder.pbPercent.setMax(100);
    }


    private void handleGiftNormalCharge(int type, IndexGiftNew o, GiftNormalHolder holder) {
        ViewUtil.showImage(holder.ivIcon, o.img);
        holder.tvName.setText(String.format("[%s]%s", o.gameName, o.name));
        holder.btnSend.setState(type);
        holder.tvContent.setText(o.content);
        if (type != GiftTypeUtil.TYPE_NORMAL_SEIZE) {
            holder.tvMoney.setVisibility(View.GONE);
            holder.tvPercent.setVisibility(View.GONE);
            holder.pbPercent.setVisibility(View.GONE);
        }
        switch (type) {
            case GiftTypeUtil.TYPE_NORMAL_SEIZED:
                setMoneyData(o, holder);
                holder.tvCount.setVisibility(View.GONE);
                break;
            case GiftTypeUtil.TYPE_NORMAL_SEIZE:
                setMoneyData(o, holder);
                setProgressBarData(o, holder);
                holder.tvCount.setVisibility(View.GONE);
                break;
            case GiftTypeUtil.TYPE_NORMAL_FINISHED:
                holder.tvCount.setVisibility(View.GONE);
                break;
            case GiftTypeUtil.TYPE_NORMAL_WAIT_SEARCH:
                setDisabledText(holder.tvCount, Html.fromHtml(String.format("开淘时间：<font color='#ffaa17'>%s</font>",
                        o.searchTime)));
                break;
            case GiftTypeUtil.TYPE_NORMAL_WAIT_SEIZE:
                setDisabledText(holder.tvCount, Html.fromHtml(String.format("开抢时间：<font color='#ffaa17'>%s</font>",
                        o.seizeTime)));
                break;
            case GiftTypeUtil.TYPE_NORMAL_SEARCH:
            case GiftTypeUtil.TYPE_NORMAL_SEARCHED:
                setDisabledText(holder.tvCount, Html.fromHtml(String.format("已淘数：<font color='#ffaa17'>%s</font>",
                        o.searchCount)));
                break;
        }
    }

    private void setMoneyData(IndexGiftNew o, GiftNormalHolder holder) {
        SpannableString ss = new SpannableString(String.format(Locale.CHINA, "[gold] %d", o.score));
        ss.setSpan(DRAWER_GOLD, 0, 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        holder.tvMoney.setText(ss, TextView.BufferType.SPANNABLE);
        holder.tvMoney.setVisibility(View.VISIBLE);
    }

    private void setDisabledText(TextView tv, Spanned text) {
        tv.setVisibility(View.VISIBLE);
        tv.setText(text);
    }

    private void setProgressBarData(IndexGiftNew o, GiftNormalHolder holder) {
        holder.tvPercent.setVisibility(View.VISIBLE);
        holder.pbPercent.setVisibility(View.VISIBLE);
        final int percent = (int) ((float) o.remainCount * 100 / o.totalCount);
        holder.tvPercent.setText(String.format(Locale.CHINA, "剩余%d%%", percent));
        holder.pbPercent.setProgress(percent);
        holder.pbPercent.setMax(100);
    }

    /**
     * 处理首充券样式设置逻辑
     */
    private void handleFirstCharge(int type, IndexGiftNew o, ChargeHolder cHolder) {
        ViewUtil.showImage(cHolder.ivIcon, o.img);
        cHolder.tvName.setText(o.gameName);
        cHolder.tvPlatform.setText(o.platform);
        cHolder.btnSend.setState(type);
        switch (type) {
            case GiftTypeUtil.TYPE_CHARGE_UN_RESERVE:
            case GiftTypeUtil.TYPE_CHARGE_DISABLE_RESERVE:
            case GiftTypeUtil.TYPE_CHARGE_RESERVED:
            case GiftTypeUtil.TYPE_CHARGE_RESERVE_EMPTY:
                cHolder.tvSeizeHint.setText(String.format(Locale.CHINA,
                        ConstString.TEXT_GIFT_FREE_SEIZE, DateUtil.formatUserReadDate(o.freeStartTime)));
                cHolder.tvSeizeHint.setVisibility(View.VISIBLE);
                cHolder.btnSend.setVisibility(View.VISIBLE);
                cHolder.tvSeize.setVisibility(View.GONE);
                cHolder.pbPercent.setVisibility(View.GONE);
                cHolder.tvPercent.setVisibility(View.GONE);
                cHolder.tvReserveDeadline.setVisibility(View.GONE);
                break;
            case GiftTypeUtil.TYPE_CHARGE_SEIZE:
                cHolder.tvSeizeHint.setVisibility(View.GONE);
                cHolder.btnSend.setVisibility(View.GONE);
                cHolder.tvReserveDeadline.setVisibility(View.GONE);
                cHolder.pbPercent.setVisibility(View.VISIBLE);
                cHolder.tvPercent.setVisibility(View.VISIBLE);
                setSeizeTextUI(cHolder.tvSeize, 0);
                final int percent = (int) ((float) o.remainCount * 100 / o.totalCount);
                cHolder.tvPercent.setText(String.format(Locale.CHINA, "剩余%d%%", percent));
                cHolder.pbPercent.setProgress(percent);
                cHolder.pbPercent.setMax(100);
                break;
            case GiftTypeUtil.TYPE_CHARGE_TAKE:
                cHolder.tvSeizeHint.setVisibility(View.GONE);
                cHolder.btnSend.setVisibility(View.GONE);
                cHolder.pbPercent.setVisibility(View.GONE);
                cHolder.tvPercent.setVisibility(View.GONE);
                cHolder.tvSeize.setVisibility(View.VISIBLE);
                setSeizeTextUI(cHolder.tvSeize, 0);
                cHolder.tvReserveDeadline.setVisibility(View.VISIBLE);
                cHolder.tvReserveDeadline.setText(
                        String.format("已预留一张首充券到%s", o.reserveDeadline));
                break;
            case GiftTypeUtil.TYPE_CHARGE_SEIZED:
                cHolder.tvSeizeHint.setVisibility(View.GONE);
                cHolder.btnSend.setVisibility(View.GONE);
                cHolder.pbPercent.setVisibility(View.GONE);
                cHolder.tvPercent.setVisibility(View.GONE);
                cHolder.tvReserveDeadline.setVisibility(View.GONE);
                cHolder.tvSeize.setVisibility(View.VISIBLE);
                setSeizeTextUI(cHolder.tvSeize, 2);
                break;
            case GiftTypeUtil.TYPE_CHARGE_EMPTY:
                cHolder.tvSeizeHint.setVisibility(View.GONE);
                cHolder.btnSend.setVisibility(View.GONE);
                cHolder.pbPercent.setVisibility(View.GONE);
                cHolder.tvPercent.setVisibility(View.GONE);
                cHolder.tvReserveDeadline.setVisibility(View.GONE);
                cHolder.tvSeize.setVisibility(View.VISIBLE);
                setSeizeTextUI(cHolder.tvSeize, 1);
                break;
        }
        ViewUtil.siteValueUI(cHolder.tvPrice, o.originPrice, true);
    }

    /**
     * 根据XML填充ConvertView并设置礼包Holder内容
     */
    @NonNull
    private View inflateGiftFeeHolder(ViewGroup parent, GiftLimitFeeHolder viewHolder) {
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list_gift_with_free, parent, false);
        viewHolder.ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
        viewHolder.tvName = ViewUtil.getViewById(convertView, R.id.tv_name);
        viewHolder.tvPrice = ViewUtil.getViewById(convertView, R.id.tv_price);
        viewHolder.btnSend = ViewUtil.getViewById(convertView, R.id.btn_send);
        viewHolder.tvPercent = ViewUtil.getViewById(convertView, R.id.tv_percent);
        viewHolder.pbPercent = ViewUtil.getViewById(convertView, R.id.pb_percent);
        viewHolder.tvMoney = ViewUtil.getViewById(convertView, R.id.tv_money);
        viewHolder.tvSeizeHint = ViewUtil.getViewById(convertView, R.id.tv_seize_hint);
        convertView.setTag(viewHolder);
        return convertView;
    }

    /**
     * 根据XML填充ConvertView并设置首充券Holder内容
     */
    @NonNull
    private View inflateChargeHolder(ViewGroup parent, ChargeHolder cHolder) {
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list_coupon_with_free,
                parent, false);
        cHolder.ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
        cHolder.tvName = ViewUtil.getViewById(convertView, R.id.tv_name);
        cHolder.tvPlatform = ViewUtil.getViewById(convertView, R.id.tv_platform);
        cHolder.tvPrice = ViewUtil.getViewById(convertView, R.id.tv_price);
        cHolder.tvPercent = ViewUtil.getViewById(convertView, R.id.tv_percent);
        cHolder.pbPercent = ViewUtil.getViewById(convertView, R.id.pb_percent);
        cHolder.tvSeize = ViewUtil.getViewById(convertView, R.id.tv_seize);
        cHolder.tvSeizeHint = ViewUtil.getViewById(convertView, R.id.tv_seize_hint);
        cHolder.tvReserveDeadline = ViewUtil.getViewById(convertView, R.id.tv_reserve_deadline);
        cHolder.btnSend = ViewUtil.getViewById(convertView, R.id.btn_send);
        convertView.setTag(cHolder);
        return convertView;
    }

    /**
     * 设置抢号Text的样式
     *
     * @param state 0 正在疯抢 1 已抢完 2 已抢号 3 免费已抢完 4 灰色不填
     */
    private void setSeizeTextUI(TextView tv, int state) {
        tv.setVisibility(View.VISIBLE);
        if (state == 0) {
            tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_red_right, 0);
            tv.setTextColor(COLOR_RED);
            tv.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            tv.setText("正在疯抢");
        } else {
            tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            tv.setTextColor(COLOR_GREY);
            tv.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
            switch (state) {
                case 1:
                    tv.setText("已抢完");
                    break;
                case 2:
                    tv.setText("已抢号");
                    break;
                case 3:
                    tv.setText("免费已抢完");
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        try {
            if (v.getTag(TAG_POSITION) != null) {
                Integer pos = (Integer) v.getTag(TAG_POSITION);
                if (mData != null && pos < mData.size()) {
                    IndexGiftNew gift = mData.get(pos);
                    if (mListener != null) {
                        mListener.onItemClick(gift, v, pos);
                    }
                }
            }
        } catch (Exception e) {
            if (AppDebugConfig.IS_DEBUG) {
                KLog.d(AppDebugConfig.TAG_ADAPTER, e);
            }
        }
    }

    @Override
    public void release() {
        mContext = null;
        mListener = null;
        mData = null;
    }

    /**
     * 普通礼包Holder
     */
    private static class GiftNormalHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvContent;
        GiftButton btnSend;
        TextView tvMoney;
        TextView tvCount;
        TextView tvPercent;
        ProgressBar pbPercent;
    }

    /**
     * 限量礼包Holder
     */
    private static class GiftLimitFeeHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvPrice;
        GiftButton btnSend;
        DeletedTextView tvMoney;
        TextView tvPercent;
        ProgressBar pbPercent;
        TextView tvSeizeHint;
    }

    /**
     * 首充券Holder
     */
    private static class ChargeHolder {
        ImageView ivIcon;
        TextView tvName;
        DeletedTextView tvPrice;
        TextView tvPlatform;
        GiftButton btnSend;
        TextView tvSeize;
        TextView tvPercent;
        ProgressBar pbPercent;
        TextView tvReserveDeadline;
        TextView tvSeizeHint;
    }
}
