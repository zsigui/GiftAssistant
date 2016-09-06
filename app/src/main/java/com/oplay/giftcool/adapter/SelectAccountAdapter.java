package com.oplay.giftcool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseListAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;

import net.ouwan.umipay.android.entry.UmipayCommonAccount;

import java.util.List;

/**
 * Created by zsigui on 16-9-6.
 */
public class SelectAccountAdapter extends BaseListAdapter<UmipayCommonAccount> implements View.OnClickListener{
    private int mCurChecked = 0;
    public SelectAccountAdapter(Context context, List<UmipayCommonAccount> objects) {
        super(context, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_lbind_choose, parent, false));
            convertView = holder.itemView;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        UmipayCommonAccount o = getItem(position);
        holder.tvName.setText(o.getUserName());
        holder.tvGameName.setText(o.getOriginApkName());
        if (position == mCurChecked) {
            holder.ctvCheck.setVisibility(View.VISIBLE);
            holder.ctvCheck.setText("");
        } else {
            holder.ctvCheck.setVisibility(View.INVISIBLE);
        }
        holder.itemView.setOnClickListener(this);
        holder.itemView.setTag(TAG_POSITION, position);
        return convertView;
    }

    @Override
    public void onClick(View v) {
        if (v.getTag(TAG_POSITION) == null) {
            return;
        }
        int pos = (int) v.getTag(TAG_POSITION);
        if (pos != mCurChecked) {
            mCurChecked = pos;
            notifyDataSetChanged();
        }
    }

    public UmipayCommonAccount getCheckedItem() {
        return getCount() == 0 ? null : getItem(mCurChecked);
    }

    private static class ViewHolder extends BaseRVHolder {

        TextView tvName;
        TextView tvGameName;
        CheckedTextView ctvCheck;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = getViewById(R.id.tv_name);
            tvGameName = getViewById(R.id.tv_game_name);
            ctvCheck = getViewById(R.id.ctv_checked);
        }
    }
}
