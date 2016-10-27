package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;

import com.oplay.giftcool.adapter.base.BaseListAdapter;
import com.oplay.giftcool.adapter.holder.StyleBaseHolder;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.util.GiftTypeUtil;
import com.oplay.giftcool.listener.OnFinishListener;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.util.UiStyleUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mink on 16-03-04.
 */
public class GiftListAdapter extends BaseListAdapter<IndexGiftNew> implements View.OnClickListener,
        OnFinishListener {

    private OnItemClickListener<IndexGiftNew> mListener;
    private ArrayMap<String, String> mCalendar;

    public GiftListAdapter(Context context, List<IndexGiftNew> objects) {
        super(context, objects);
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
        return GiftTypeUtil.UI_TYPE_COUNT;
    }

    /**
     * 获取ListItem类型<br/>
     * 注意: 返回的 int 需要范围为 0 ~ getViewTypeCount() - 1, 否则会出现ArrayIndexOutOfBoundsException
     */
    @Override
    public int getItemViewType(int position) {
        IndexGiftNew o = getItem(position);
        if (o == null) {
            return GiftTypeUtil.UI_TYPE_PRECIOUS_SEIZE;
        }
        o.uiStyle = o.uiStyle == GiftTypeUtil.UI_TYPE_DEFAULT ?
                GiftTypeUtil.getUiStyle(o) : o.uiStyle;
        return o.uiStyle;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        getItemViewType(position);
        IndexGiftNew o = getItem(position);
        StyleBaseHolder baseHolder = UiStyleUtil.onCreateHolder(mContext, convertView, parent, o.uiStyle, false);
        UiStyleUtil.bindListener(baseHolder, TAG_POSITION, position, this);
        UiStyleUtil.bindHolderData(mContext, baseHolder, o);

        return baseHolder.itemView;
    }

    @Override
    public void updateData(List<IndexGiftNew> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public void addMoreData(ArrayList<IndexGiftNew> data) {
        if (data == null || mData == null) {
            return;
        }
        mData.addAll(data);
        notifyDataSetChanged();
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
}
