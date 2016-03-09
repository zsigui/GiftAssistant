package com.oplay.giftcool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseListAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.listener.OnFinishListener;
import com.oplay.giftcool.listener.OnSearchListener;
import com.oplay.giftcool.util.ViewUtil;
import com.socks.library.KLog;

import java.util.List;

/**
 * Created by zsigui on 15-12-23.
 */
public class SearchHistoryAdapterOld extends BaseListAdapter<String> implements OnFinishListener, View.OnClickListener {

	private final int TYPE_COUNT = 2;
	private final int TYPE_NORMAL = 0;
	private final int TYPE_CLEAR = 1;

	private OnSearchListener mSearchListener;

	public SearchHistoryAdapterOld(Context context, List<String> keywords) {
		super(context, keywords);
		mData = keywords;
	}

	@Override
	public int getCount() {
		if (mData == null || mData.size() == 0) {
			return 0;
		}
		return mData.size() + 1;
	}

	@Override
	public String getItem(int position) {
		if (getCount() == 0) {
			return null;
		}
		if (position == mData.size()) {
			return "clear placeholder";
		}
		return mData.get(position);
	}

	public void setSearchListener(OnSearchListener searchListener) {
		mSearchListener = searchListener;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == getCount() - 1) {
			// the final item to show the clear icon
			return TYPE_CLEAR;
		}
		return TYPE_NORMAL;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (getCount() == 0) {
			return null;
		}


		int type = getItemViewType(position);
		switch (type) {
			case TYPE_NORMAL:
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
				holder.tvKey.setText(mData.get(position));
				break;
			case TYPE_CLEAR:
				if (convertView == null) {
					convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list_history_clear, parent,
							false);
				}
				break;
		}
		convertView.setTag(TAG_POSITION, position);
		convertView.setOnClickListener(this);
		return convertView;
	}

	@Override
	public int getViewTypeCount() {
		return TYPE_COUNT;
	}

	public void updateData(List<String> data) {
		mData = data;
		notifyDataSetChanged();
	}

	@Override
	public void release() {
		if (mData != null) {
			mData.clear();
			mData = null;
		}
	}

	@Override
	public void onClick(View v) {
		if (mData == null) {
			return;
		}
		if (mSearchListener != null) {
			switch (v.getId()) {
				case R.id.rl_search_history:
					if (v.getTag(TAG_POSITION) != null) {
						mSearchListener.sendSearchRequest(mData.get((Integer) v.getTag(TAG_POSITION)), 0);
					} else {
						if (AppDebugConfig.IS_DEBUG) {
							KLog.d(AppDebugConfig.TAG_SEARCH, "error_position : " + v.getTag(TAG_POSITION));
						}
					}
					break;
				case R.id.ll_history_clear:
					mSearchListener.clearHistory();
					updateData(null);
					break;
			}
		}
	}

	static class NormalHolder {
		TextView tvKey;
	}
}
