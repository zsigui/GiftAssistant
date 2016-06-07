package com.oplay.giftcool.adapter;

import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseListAdapter;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.util.GiftTypeUtil;
import com.oplay.giftcool.manager.PayManager;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.TimeData;
import com.oplay.giftcool.ui.widget.DeletedTextView;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.ui.widget.stickylistheaders.StickyListHeadersAdapter;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ViewUtil;

import net.ouwan.umipay.android.debug.Debug_Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 限量免费模块Adapter
 * <p/>
 * Created by zsigui on 16-5-23.
 */
public class FreeAdapter extends BaseListAdapter<TimeData<IndexGiftNew>> implements View.OnClickListener,
        StickyListHeadersAdapter {

    final int COLOR_GREY;
    final int COLOR_RED;
    final ImageSpan DRAWER_GOLD;
    final ImageSpan DRAWER_BEAN;
    final int W_DIVIDER;
    private FragmentActivity mActivity;

    public FreeAdapter(FragmentActivity context, List<TimeData<IndexGiftNew>> objects) {
        super(context, objects);
        mActivity = context;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getCount() == 0) {
            return null;
        }

        int type = getItemViewType(position);

        IndexGiftNew o = getItem(position).data;

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
                convertView.setTag(TAG_POSITION, position);
                convertView.setOnClickListener(this);
                cHolder.btnSend.setTag(TAG_POSITION, position);
                cHolder.btnSend.setOnClickListener(this);
                handleFirstCharge(type, o, cHolder);
                break;
            // 首充券一类结束
            default:
                GiftHolder gHolder;
                if (convertView == null) {
                    gHolder = new GiftHolder();
                    convertView = inflateGiftHolder(parent, gHolder);
                } else {
                    gHolder = (GiftHolder) convertView.getTag();
                }
                convertView.setTag(TAG_POSITION, position);
                convertView.setOnClickListener(this);
                handleGiftFree(type, o, gHolder);
                break;
        }

        return convertView;
    }

    /**
     * 处理免费礼包样式设置逻辑
     */
    private void handleGiftFree(int type, IndexGiftNew o, GiftHolder gHolder) {
        ViewUtil.showImage(gHolder.ivIcon, o.img);
        gHolder.tvName.setText(String.format("[%s]%s", o.gameName, o.name));
        gHolder.tvContent.setText(o.content);
//        SpannableString ss = new SpannableString(String.format(Locale.CHINA, "[gold] %d 或 [bean] %d", o.score, o.bean));
//        final int startPos = String.valueOf(o.score).length() + 10;
//        ss.setSpan(DRAWER_GOLD, 0, 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//        ss.setSpan(DRAWER_BEAN, startPos, startPos + 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        switch (type) {
            case GiftTypeUtil.TYPE_LIMIT_EMPTY:
            case GiftTypeUtil.TYPE_LIMIT_WAIT_SEIZE:
            case GiftTypeUtil.TYPE_LIMIT_FINISHED:
            case GiftTypeUtil.TYPE_LIMIT_FREE_WAIT_SEIZE:
            case GiftTypeUtil.TYPE_LIMIT_FREE_EMPTY:
                finishState(o, gHolder);
                break;
            case GiftTypeUtil.TYPE_LIMIT_FREE_SEIZE:
//                gHolder.tvMoney.setPaint(COLOR_GREY, W_DIVIDER);
                gHolder.pbPercent.setVisibility(View.VISIBLE);
                gHolder.tvPercent.setVisibility(View.VISIBLE);
                final int percent = (int) ((float) o.remainCount * 100 / o.totalCount);
                gHolder.tvPercent.setText(String.format(Locale.CHINA, "剩余%d%%", percent));
                gHolder.pbPercent.setProgress(percent);
                gHolder.pbPercent.setMax(100);
                gHolder.tvSeizeHint.setVisibility(View.GONE);
                setSeizeTextUI(gHolder.tvSeize, 0);
                break;
            case GiftTypeUtil.TYPE_LIMIT_FREE_SEIZED:
//                gHolder.tvMoney.setPaint(COLOR_GREY, W_DIVIDER);
                seizedState(gHolder);
                break;
            case GiftTypeUtil.TYPE_LIMIT_SEIZE:
                seizeLimitState(o, gHolder);
                break;
            case GiftTypeUtil.TYPE_LIMIT_SEIZED:
            default:
                if (o.seizeStatus == GiftTypeUtil.SEIZE_TYPE_SEIZED) {
                    // 效果同 TYPE_LIMIT_FREE_SEIZED , 不过没有删除线
                    seizedState(gHolder);
                } else if (o.status == GiftTypeUtil.STATUS_FINISHED
                        || o.status == GiftTypeUtil.STATUS_WAIT_SEIZE
                        || o.status == GiftTypeUtil.STATUS_WAIT_SEARCH) {
                    finishState(o, gHolder);
                } else {
                    seizeLimitState(o, gHolder);
                }
                break;

        }
//        gHolder.tvMoney.setText(ss, TextView.BufferType.SPANNABLE);
        ViewUtil.siteValueUI(gHolder.tvMoney, o.originPrice, true);
    }

    private void seizeLimitState(IndexGiftNew o, GiftHolder gHolder) {
        gHolder.pbPercent.setVisibility(View.GONE);
        gHolder.tvPercent.setVisibility(View.GONE);
        gHolder.tvSeizeHint.setVisibility(View.VISIBLE);
        if (o.freeStartTime != 0 && System.currentTimeMillis() < o.freeStartTime * 1000) {
            setSeizeTextUI(gHolder.tvSeize, 4);
            gHolder.tvSeize.setText(String.format(Locale.CHINA,
                    ConstString.TEXT_GIFT_FREE_SEIZE, DateUtil.formatUserReadDate(o.freeStartTime)));
        } else {
            setSeizeTextUI(gHolder.tvSeize, 3);
        }
    }

    private void seizedState(GiftHolder gHolder) {
        gHolder.pbPercent.setVisibility(View.GONE);
        gHolder.tvPercent.setVisibility(View.GONE);
        gHolder.tvSeizeHint.setVisibility(View.GONE);
        setSeizeTextUI(gHolder.tvSeize, 2);
    }

    private void finishState(IndexGiftNew o, GiftHolder gHolder) {
        gHolder.pbPercent.setVisibility(View.GONE);
        gHolder.tvPercent.setVisibility(View.GONE);
        gHolder.tvSeizeHint.setVisibility(View.GONE);
        if (o.status == GiftTypeUtil.STATUS_WAIT_SEIZE) {
            setSeizeTextUI(gHolder.tvSeize, 4);
            gHolder.tvSeize.setText(String.format(Locale.CHINA,
                    ConstString.TEXT_GIFT_FREE_SEIZE, DateUtil.formatUserReadDate(o.freeStartTime)));
        } else {
            setSeizeTextUI(gHolder.tvSeize, 3);
        }
    }

    /**
     * 处理首充券样式设置逻辑
     */
    private void handleFirstCharge(int type, IndexGiftNew o, ChargeHolder cHolder) {
        ViewUtil.showImage(cHolder.ivIcon, o.img);
        cHolder.tvName.setText(o.gameName);
        cHolder.tvPlatform.setText(o.platform);
        cHolder.btnSend.setState(GiftTypeUtil.getButtonState(o));
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
                final int percent = (int) (Math.ceil(o.remainCount * 100.0 / o.totalCount));
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
                        String.format("已预留一张首充券到%s", DateUtil.optDate(o.reserveDeadline)));
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
    private View inflateGiftHolder(ViewGroup parent, GiftHolder gHolder) {
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_index_free_gift_seize,
                parent, false);
        gHolder.ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
        gHolder.tvName = ViewUtil.getViewById(convertView, R.id.tv_name);
        gHolder.tvContent = ViewUtil.getViewById(convertView, R.id.tv_content);
        gHolder.tvMoney = ViewUtil.getViewById(convertView, R.id.tv_money);
        gHolder.tvPercent = ViewUtil.getViewById(convertView, R.id.tv_percent);
        gHolder.pbPercent = ViewUtil.getViewById(convertView, R.id.pb_percent);
        gHolder.tvSeize = ViewUtil.getViewById(convertView, R.id.tv_seize);
        gHolder.tvSeizeHint = ViewUtil.getViewById(convertView, R.id.tv_seize_hint);
        convertView.setTag(gHolder);
        return convertView;
    }

    /**
     * 根据XML填充ConvertView并设置首充券Holder内容
     */
    @NonNull
    private View inflateChargeHolder(ViewGroup parent, ChargeHolder cHolder) {
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_index_free_gift_first_charge,
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
     * @param tv
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
    public void updateData(List<TimeData<IndexGiftNew>> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public void addMoreData(ArrayList<TimeData<IndexGiftNew>> data) {
        if (data == null || mData == null) {
            return;
        }
        mData.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (v.getTag(TAG_POSITION) == null) {
            return;
        }
        IndexGiftNew o = getItem((Integer) v.getTag(TAG_POSITION)).data;
        switch (v.getId()) {
            case R.id.rl_recommend:
                IntentUtil.jumpGiftDetail(mContext, o.id);
                break;
            case R.id.btn_send:
                // 进行预约
                PayManager.getInstance().seizeGift(mActivity, o, (GiftButton) v);
                break;
        }
    }

    @Override
    public int getViewTypeCount() {
        return GiftTypeUtil.TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return GiftTypeUtil.getItemViewType(mData.get(position).data);
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        if (getCount() == 0) {
            return null;
        }
        HeaderViewHolder headerViewHolder;

        if (convertView == null) {
            headerViewHolder = new HeaderViewHolder();
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.item_header_gift_limmit, parent, false);
            headerViewHolder.tv_date = ViewUtil.getViewById(convertView, R.id.item_header_tv_gift_limmit_tv);
            convertView.setTag(headerViewHolder);
        } else {
            headerViewHolder = (HeaderViewHolder) convertView.getTag();
        }
        headerViewHolder.tv_date.setText(formatDateTime(mData.get(position).date));
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        //用item数据的日期首次在列表中出现位置作为id
        String date = formatDateTime(mData.get(position).date);
        for (int i = 0; i < mData.size(); i++) {
            String d = formatDateTime(mData.get(i).date);
            if (d.equals(date)) {
                return i;
            }
        }
        return -1;
    }


    @Override
    public void release() {
        mActivity = null;
        if (mData != null) {
            mData.clear();
            mData = null;
        }
        if (mCalendar != null) {
            mCalendar.clear();
            mCalendar = null;
        }
    }

    public ArrayMap<String, String> mCalendar;

    public String formatDateTime(String time) {
        String date = "";
        try {
            if (mCalendar == null) {
                mCalendar = new ArrayMap<>();
            }
            date = mCalendar.get(time);
            if (date == null) {
                date = formatDateTimeHelper(time);
                mCalendar.put(time, date);
            }
        } catch (Throwable e) {
            Debug_Log.e(e);
        }
        return date;
    }

    /**
     * 格式化时间
     *
     * @param time
     * @return
     */
    private static String formatDateTimeHelper(String time) {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        if (TextUtils.isEmpty(time)) {
            return "";
        }
        Date date = null;
        try {

            date = format.parse(time);
            Calendar current = Calendar.getInstance();

            Calendar today = Calendar.getInstance();    //今天
            today.set(Calendar.YEAR, current.get(Calendar.YEAR));
            today.set(Calendar.MONTH, current.get(Calendar.MONTH));
            today.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH) - 1);
            //  Calendar.HOUR——12小时制的小时数 Calendar.HOUR_OF_DAY——24小时制的小时数
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);

            Calendar tomorrow = Calendar.getInstance();    //明天
            today.set(Calendar.YEAR, current.get(Calendar.YEAR));
            today.set(Calendar.MONTH, current.get(Calendar.MONTH));
            today.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH));
            //  Calendar.HOUR——12小时制的小时数 Calendar.HOUR_OF_DAY——24小时制的小时数
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);

            current.setTime(date);
            current.set(Calendar.HOUR_OF_DAY, 0);
            current.set(Calendar.MINUTE, 0);
            current.set(Calendar.SECOND, 30);

            if (current.before(today)) {
                return "昨天/以前";
            } else if (current.after(today) && current.before(tomorrow)) {
                return "今天";
            } else {
                return "明天/以后";
            }
        } catch (ParseException e) {
            Debug_Log.e(e);
        }
        return time;
    }

    private static class HeaderViewHolder {
        public TextView tv_date;

    }

    private static class GiftHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvContent;
        DeletedTextView tvMoney;
        TextView tvPercent;
        ProgressBar pbPercent;
        TextView tvSeizeHint;
        TextView tvSeize;
    }

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
