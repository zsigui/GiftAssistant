package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.text.Html;
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
import com.oplay.giftcool.model.data.resp.TimeData;
import com.oplay.giftcool.ui.widget.DeletedTextView;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.MixUtil;
import com.oplay.giftcool.util.ViewUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by mink on 16-03-04.
 */
public class LimitGiftListNewAdapter extends BaseListAdapter<TimeData<IndexGiftNew>> implements View.OnClickListener,
        OnFinishListener {

    final ImageSpan DRAWER_GOLD;
    final ImageSpan DRAWER_BEAN;
    final int W_DIVIDER;

    private OnItemClickListener<IndexGiftNew> mListener;
    private ArrayMap<String, String> mCalendar;

    public LimitGiftListNewAdapter(Context context, List<TimeData<IndexGiftNew>> objects) {
        super(context, objects);
        W_DIVIDER = context.getResources().getDimensionPixelSize(R.dimen.di_divider_height);
        DRAWER_GOLD = new ImageSpan(context, R.drawable.ic_score);
        DRAWER_BEAN = new ImageSpan(context, R.drawable.ic_bean);
    }


    public OnItemClickListener<IndexGiftNew> getListener() {
        return mListener;
    }

    public void setListener(OnItemClickListener<IndexGiftNew> listener) {
        mListener = listener;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
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
        return GiftTypeUtil.getItemViewType(mData.get(position).data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getCount() == 0) {
            return null;
        }

        int type = getItemViewType(position);

        IndexGiftNew o = getItem(position).data;

        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflateView(parent, holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        convertView.setTag(TAG_POSITION, position);
        convertView.setOnClickListener(this);
        holder.btnSend.setTag(TAG_POSITION, position);
        holder.btnSend.setOnClickListener(this);
        handleGiftLimit(type, o, holder);
        return convertView;
    }

    /**
     * 处理免费礼包样式设置逻辑
     */
    private void handleGiftLimit(int type, IndexGiftNew o, ViewHolder holder) {
        ViewUtil.showImage(holder.ivIcon, o.img);
        holder.tvName.setText(String.format("[%s]%s", o.gameName, o.name));
        ViewUtil.siteValueUI(holder.tvPrice, o.originPrice, true);
        holder.btnSend.setState(GiftTypeUtil.getButtonState(o));
        ViewUtil.siteSpendUI(holder.tvMoney, o.score, o.bean, o.priceType);
        switch (type) {
            case GiftTypeUtil.TYPE_LIMIT_FREE_SEIZE:
                holder.tvSeizeHint.setVisibility(View.VISIBLE);
                holder.tvSeizeHint.setText("正在免费抢");
                setProgressBarData(o, holder);
                break;
            case GiftTypeUtil.TYPE_LIMIT_WAIT_SEIZE:
            case GiftTypeUtil.TYPE_LIMIT_FREE_WAIT_SEIZE:
                holder.tvMoney.setText(Html.fromHtml(String.format("开抢时间：<font color='#ffaa17'>%s</font>",
                        DateUtil.formatTime(o.seizeTime, "yyyy-MM-dd HH:mm"))));
            case GiftTypeUtil.TYPE_LIMIT_FREE_SEIZED:
            case GiftTypeUtil.TYPE_LIMIT_SEIZED:
            case GiftTypeUtil.TYPE_LIMIT_FREE_EMPTY:
            case GiftTypeUtil.TYPE_LIMIT_EMPTY:
            case GiftTypeUtil.TYPE_LIMIT_FINISHED:
                holder.pbPercent.setVisibility(View.GONE);
                holder.tvPercent.setVisibility(View.GONE);
                holder.tvSeizeHint.setVisibility(View.GONE);
                break;
            case GiftTypeUtil.TYPE_LIMIT_SEIZE:
            default:
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
        }
    }

    private void setProgressBarData(IndexGiftNew o, ViewHolder gHolder) {
        gHolder.tvPercent.setVisibility(View.VISIBLE);
        gHolder.pbPercent.setVisibility(View.VISIBLE);
        final int percent = MixUtil.calculatePercent(o.remainCount, o.totalCount);
        gHolder.tvPercent.setText(String.format(Locale.CHINA, "剩余%d%%", percent));
        gHolder.pbPercent.setProgress(percent);
        gHolder.pbPercent.setMax(100);
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

    /**
     * 根据XML填充convertView
     */
    private View inflateView(ViewGroup parent, ViewHolder viewHolder) {
        View convertView;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(R.layout.item_gift_limit_list_new, parent, false);
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

    @Override
    public void onClick(View v) {
        try {
            if (v.getTag(TAG_POSITION) != null) {
                Integer pos = (Integer) v.getTag(TAG_POSITION);
                if (mData != null && pos < mData.size()) {
                    IndexGiftNew gift = mData.get(pos).data;
                    if (mListener != null) {
                        mListener.onItemClick(gift, v, pos);
                    }
                }
            }
        } catch (Exception e) {
            AppDebugConfig.w(AppDebugConfig.TAG_ADAPTER, e);
        }
    }

    @Override
    public void release() {
        mContext = null;
        mListener = null;
        if (mData != null) {
            mData.clear();
            mData = null;
        }
        if (mCalendar != null) {
            mCalendar.clear();
            mCalendar = null;
        }
    }

    /**
     * ViewHolder缓存，由于大体结构相似，统一一个ViewHolder类型
     */
    static class ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvPrice;
        GiftButton btnSend;
        DeletedTextView tvMoney;
        TextView tvPercent;
        ProgressBar pbPercent;
        TextView tvSeizeHint;
    }
}
