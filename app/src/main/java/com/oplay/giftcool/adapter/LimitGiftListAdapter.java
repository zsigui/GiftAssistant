package com.oplay.giftcool.adapter;

import android.content.Context;
import android.os.Build;
import android.support.v4.util.ArrayMap;
import android.text.SpannableString;
import android.text.Spanned;
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
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.util.GiftTypeUtil;
import com.oplay.giftcool.listener.OnFinishListener;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.TimeData;
import com.oplay.giftcool.ui.widget.DeletedTextView;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.ui.widget.stickylistheaders.StickyListHeadersAdapter;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.ViewUtil;
import com.socks.library.KLog;

import net.ouwan.umipay.android.debug.Debug_Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by mink on 16-03-04.
 */
public class LimitGiftListAdapter extends BaseListAdapter<TimeData<IndexGiftNew>> implements View.OnClickListener,
        OnFinishListener,
        StickyListHeadersAdapter {

    final int COLOR_GREY;
    final ImageSpan DRAWER_GOLD;
    final ImageSpan DRAWER_BEAN;
    final int W_DIVIDER;

    private OnItemClickListener<IndexGiftNew> mListener;
    private ArrayMap<String, String> mCalendar;

    public LimitGiftListAdapter(Context context, List<TimeData<IndexGiftNew>> objects) {
        super(context, objects);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            COLOR_GREY = context.getColor(R.color.co_common_text_second);
        } else {
            COLOR_GREY = context.getResources().getColor(R.color.co_common_text_second);
        }
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
        SpannableString ss = new SpannableString(String.format(Locale.CHINA, "[gold] %d 或 [bean] %d", o.score, o.bean));
        final int startPos = String.valueOf(o.score).length() + 10;
        ss.setSpan(DRAWER_GOLD, 0, 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        ss.setSpan(DRAWER_BEAN, startPos, startPos + 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        ViewUtil.siteValueUI(holder.tvPrice, o.originPrice, false);
        holder.btnSend.setState(type);
        switch (type) {
            case GiftTypeUtil.TYPE_LIMIT_SEIZE:
                if (o.freeStartTime != 0 && System.currentTimeMillis() < o.freeStartTime * 1000) {
                    // 限量抢状态,表示当前不处于免费抢
                    holder.tvSeizeHint.setVisibility(View.VISIBLE);
                    holder.tvSeizeHint.setText(String.format("%s%s免费抢",
                            DateUtil.formatUserReadDate(o.freeStartTime),
                            DateUtil.formatTime(o.freeStartTime * 1000, "HH:mm")));
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
            case GiftTypeUtil.TYPE_LIMIT_EMPTY:
            case GiftTypeUtil.TYPE_LIMIT_FINISHED:
                holder.pbPercent.setVisibility(View.GONE);
                holder.tvPercent.setVisibility(View.GONE);
                holder.tvSeizeHint.setVisibility(View.GONE);
                break;
        }
        holder.tvMoney.setText(ss, TextView.BufferType.SPANNABLE);
    }

    private void setProgressBarData(IndexGiftNew o, ViewHolder gHolder) {
        gHolder.tvPercent.setVisibility(View.VISIBLE);
        gHolder.pbPercent.setVisibility(View.VISIBLE);
        final int percent = (int) ((float) o.remainCount * 100 / o.totalCount);
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
        convertView = inflater.inflate(R.layout.item_index_gift_limit_list, parent, false);
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
            if (AppDebugConfig.IS_DEBUG) {
                KLog.d(AppDebugConfig.TAG_ADAPTER, e);
            }
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

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        if (getCount() == 0) {
            return null;
        }
        HeaderViewHolder headerViewHolder = null;

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

    private static class HeaderViewHolder {
        public TextView tv_date;

    }
//
//	private static ArrayList<IndexGiftNew> praseDateFromTimeDataList(ArrayList<TimeDataList<IndexGiftNew>> data) {
//		ArrayList list = new ArrayList<IndexGiftNew>();
//		try {
//			Collections.sort(data, new Comparator<TimeDataList<IndexGiftNew>>() {
//				@Override
//				public int compare(TimeDataList<IndexGiftNew> lhs, TimeDataList<IndexGiftNew> rhs) {
//
//					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//					//将字符串形式的时间转化为Date类型的时间
//					try {
//						Date a = sdf.parse(lhs.date);
//						Date b = sdf.parse(rhs.date);
//
//						//Date类的一个方法，如果a早于b返回true，否则返回false
//						if (a.before(b))
//							return 1;
//						else
//							return -1;
//					} catch (Throwable e) {
//						if (AppDebugConfig.IS_DEBUG) {
//							KLog.e(AppDebugConfig.TAG_FRAG, e);
//						}
//					}
//					return -1;
//				}
//			});
//			for (TimeDataList<IndexGiftNew> d : data) {
//				list.addAll(d.data);
//			}
//		} catch (Throwable e) {
//			Debug_Log.e(e);
//		}
//		return list;
//	}

    public String formatDateTime(String time) {
        String date = "";
        try {
            if (mCalendar == null) {
                mCalendar = new ArrayMap<String, String>();
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
        SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
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
            today.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH));
            //  Calendar.HOUR——12小时制的小时数 Calendar.HOUR_OF_DAY——24小时制的小时数
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);

            Calendar yesterday = Calendar.getInstance();    //昨天

            yesterday.set(Calendar.YEAR, current.get(Calendar.YEAR));
            yesterday.set(Calendar.MONTH, current.get(Calendar.MONTH));
            yesterday.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH) - 1);
            yesterday.set(Calendar.HOUR_OF_DAY, 0);
            yesterday.set(Calendar.MINUTE, 0);
            yesterday.set(Calendar.SECOND, 0);

            current.setTime(date);
            current.set(Calendar.HOUR_OF_DAY, 0);
            current.set(Calendar.MINUTE, 0);
            current.set(Calendar.SECOND, 30);

            if (current.after(today)) {
                return "今天";
            } else if (current.before(today) && current.after(yesterday)) {
                return "昨天";
            } else {
                return "以前";
            }
        } catch (ParseException e) {
            Debug_Log.e(e);
        }
        return time;
    }

}
