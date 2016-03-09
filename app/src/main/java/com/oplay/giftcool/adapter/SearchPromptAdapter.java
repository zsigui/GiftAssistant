package com.oplay.giftcool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseListAdapter;
import com.oplay.giftcool.listener.OnFinishListener;
import com.oplay.giftcool.listener.OnSearchListener;
import com.oplay.giftcool.model.data.resp.PromptData;
import com.oplay.giftcool.util.ViewUtil;

import java.util.List;

/**
 * Created by zsigui on 15-12-23.
 */
public class SearchPromptAdapter extends BaseListAdapter<PromptData> implements OnFinishListener, View.OnClickListener {


    private OnSearchListener mSearchListener;

    public SearchPromptAdapter(Context context, List<PromptData> data) {
        super(context, data);
        mData = data;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setSearchListener(OnSearchListener searchListener) {
        mSearchListener = searchListener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (getCount() == 0) {
            return null;
        }

        NormalHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list_history, parent,
                    false);
            holder = new NormalHolder();
            holder.tvKey = ViewUtil.getViewById(convertView, R.id.tv_search_text);
            convertView.setTag(holder);
        } else {
            holder = (NormalHolder) convertView.getTag();
        }
        holder.tvKey.setText(mData.get(position).appName);

        convertView.setTag(TAG_POSITION, position);
        convertView.setOnClickListener(this);
        return convertView;
    }

    @Override
    public void release() {
        mSearchListener = null;
        if (mData != null) {
            mData.clear();
            mData = null;
        }
    }

    @Override
    public void updateData(List<PromptData> newData) {
        mData = newData;
        notifyDataChanged();
    }

    @Override
    public void onClick(View v) {
        if (mData == null) {
            return;
        }
        if (mSearchListener != null) {
            PromptData data = mData.get((Integer) v.getTag(TAG_POSITION));
            mSearchListener.sendSearchRequest(data.appName, data.appId);
        }

    }

    static class NormalHolder {
        TextView tvKey;
    }
}
