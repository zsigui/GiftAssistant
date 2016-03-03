package com.oplay.giftcool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseListAdapter;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.util.ViewUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zsigui on 16-2-16.
 */
public class AccountAdapter extends BaseListAdapter<String> implements Filterable, View.OnClickListener {

	private InputFilter mFilter;
	private OnItemClickListener<String> mListener;
	private boolean mIsOuwan = false;

	public AccountAdapter(Context context, List<String> objects, boolean isOuwan) {
		super(context, objects);
		mIsOuwan = isOuwan;
	}

	public void setListener(OnItemClickListener<String> listener) {
		mListener = listener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder v;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_account, parent, false);
			v = new ViewHolder();
			v.tvName = ViewUtil.getViewById(convertView, R.id.tv_account_list_text);
			v.ivDel = ViewUtil.getViewById(convertView, R.id.iv_account_list_delete);
			convertView.setTag(v);
		} else {
			v = (ViewHolder) convertView.getTag();
		}
		String s = getItem(position);
		v.tvName.setText((mIsOuwan ? s.split(",")[0] : s));
		v.ivDel.setOnClickListener(this);
		v.ivDel.setTag(TAG_POSITION, position);
		convertView.setTag(TAG_POSITION, position);
		convertView.setOnClickListener(this);
		return convertView;
	}

	@Override
	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new InputFilter();
		}
		return mFilter;
	}

	@Override
	public void onClick(View v) {
		if (mListener != null && v.getTag(TAG_POSITION) != null) {
			Integer pos = (Integer) v.getTag(TAG_POSITION);
			mListener.onItemClick(getItem(pos), v, pos);
		}
	}

	static final class ViewHolder {
		public TextView tvName;
		public ImageView ivDel;
	}

	private class InputFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			if (mData == null) {
				return null;
			}
			FilterResults results = new FilterResults();
			ArrayList<String> newValues = new ArrayList<>();
			String prefixStr = prefix.toString().toLowerCase();
			for (String s : mData) {
				if (s.toLowerCase().startsWith(prefixStr)) {
					newValues.add(s);
				}
			}
			results.values = newValues;
			results.count = newValues.size();
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			if (results == null || results.count == 0) {
				notifyDataSetInvalidated();
			} else {
				mData = (ArrayList<String>) results.values;
				notifyDataSetChanged();
			}
		}
	}
}
