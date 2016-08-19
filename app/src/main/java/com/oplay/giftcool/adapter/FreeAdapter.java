package com.oplay.giftcool.adapter;

import android.support.v4.app.FragmentActivity;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseListAdapter;
import com.oplay.giftcool.adapter.holder.StyleBaseHolder;
import com.oplay.giftcool.config.util.GiftTypeUtil;
import com.oplay.giftcool.manager.PayManager;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.TimeData;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.ui.widget.stickylistheaders.StickyListHeadersAdapter;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.UiStyleUtil;
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

    private FragmentActivity mActivity;

    public FreeAdapter(FragmentActivity context, List<TimeData<IndexGiftNew>> objects) {
        super(context, objects);
        mActivity = context;
    }

    @Override
    public int getViewTypeCount() {
        return GiftTypeUtil.UI_TYPE_COUNT;
    }

    /**
     * 获取ListItem类型<br/>
     * 注意: 返回的 int 需要范围为 0 ~ getViewTypeCount() - 1, 否则会出现ArrayIndexOutOfBoundsException
     */
    @Override
    public int getItemViewType(int position) {
        IndexGiftNew o = getDataItem(position);
        if (o == null) {
            return GiftTypeUtil.UI_TYPE_FREE_OTHER;
        } else if (o.uiStyle == GiftTypeUtil.UI_TYPE_DEFAULT) {
            switch (o.totalType) {
                case GiftTypeUtil.TOTAL_TYPE_COUPON:
                    o.uiStyle = GiftTypeUtil.UI_TYPE_COUPON_OTHER;
                    break;
                case GiftTypeUtil.TOTAL_TYPE_GIFT_LIMIT:
                case GiftTypeUtil.TOTAL_TYPE_GIFT:
                    o.uiStyle = GiftTypeUtil.UI_TYPE_PRECIOUS_SEIZE;
                    break;
                default:
                    o.uiStyle = GiftTypeUtil.UI_TYPE_FREE_OTHER;
            }
        }
        return o.uiStyle;
    }


    public IndexGiftNew getDataItem(int position) {
        return getCount() == 0 ? null : getItem(position).data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        getItemViewType(position);
        IndexGiftNew o = getDataItem(position);
        StyleBaseHolder baseHolder = UiStyleUtil.onCreateHolder(mContext, convertView, parent, o.uiStyle, true);
        UiStyleUtil.bindListener(baseHolder, TAG_POSITION, position, this);
        UiStyleUtil.bindHolderData(mContext, baseHolder, o);

        return baseHolder.itemView;
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
}
