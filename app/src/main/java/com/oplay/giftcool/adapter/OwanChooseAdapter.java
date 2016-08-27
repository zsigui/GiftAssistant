package com.oplay.giftcool.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseListAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.model.data.resp.BindAccount;

import java.util.List;

/**
 * Created by zsigui on 16-8-27.
 */
public class OwanChooseAdapter extends BaseListAdapter<BindAccount> implements View.OnClickListener {

    private int mCurChecked = 0;
    private boolean mIsFirst = true;

    public OwanChooseAdapter(Context context, List<BindAccount> objects) {
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
        BindAccount o = getItem(position);
        holder.tvName.setText(Html.fromHtml(String.format(mContext.getString(R.string.st_lbind_choose_user_name),
                o.username, o.regAppName)));
        if (position == mCurChecked) {
            holder.ctvCheck.setText(mIsFirst ? "默认选择" : "");
            holder.ctvCheck.setVisibility(View.VISIBLE);
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
            mIsFirst = false;
            notifyDataSetChanged();
        }
    }

    public BindAccount getCheckedItem() {
        return getCount() == 0 ? null : getItem(mCurChecked);
    }

    private static class ViewHolder extends BaseRVHolder {

        TextView tvName;
        CheckedTextView ctvCheck;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = getViewById(R.id.tv_name);
            ctvCheck = getViewById(R.id.ctv_checked);
        }
    }
}
