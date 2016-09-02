package com.oplay.giftcool.adapter;

import android.content.Context;
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

import java.util.List;

/**
 * Created by zsigui on 15-12-24.
 */
public class NestedGiftListAdapter extends BaseListAdapter<IndexGiftNew> implements View.OnClickListener,
        OnFinishListener {


    private OnItemClickListener<IndexGiftNew> mListener;


    public NestedGiftListAdapter(Context context) {
        this(context, null);
    }

    public NestedGiftListAdapter(Context context, List<IndexGiftNew> objects) {
        super(context, objects);
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
            return GiftTypeUtil.UI_TYPE_NORMAL_SEIZE;
        } else if (o.uiStyle == GiftTypeUtil.UI_TYPE_DEFAULT) {
            return GiftTypeUtil.getUiStyle(o);
        }
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
        mData = null;
    }
}
