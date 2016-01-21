package com.oplay.giftcool.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.socks.library.KLog;

import java.util.List;

import cn.bingoogolapple.androidcommon.adapter.BGAAdapterViewHolder;
import cn.bingoogolapple.androidcommon.adapter.BGAViewHolderHelper;

/**
 * Created by zsigui on 15-12-23.
 */
public class SearchHistoryAdapter extends BaseAdapter {

	private static final int TYPE_NORMAL = 0;
	private static final int TYPE_CLEAR = 1;

	private boolean mIsHistory;
	private List<String> mKeywords;
	private OnClearListener mClearListener;

	public SearchHistoryAdapter(List<String> keywords, boolean isHistory) {
		mIsHistory = isHistory;
		mKeywords = keywords;
	}

	@Override
	public int getCount() {
		if (mKeywords == null || mKeywords.size() == 0) {
			return 0;
		} else if (mIsHistory) {
            return mKeywords.size() + 1;
        } else {
            return mKeywords.size();
        }
	}

	@Override
	public Object getItem(int position) {
		if (getCount() == 0) {
			return null;
		}
        if (position == mKeywords.size()) {
            return "clear placeholder";
        }
        return mKeywords.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

    @Override
    public int getItemViewType(int position) {
        if (mIsHistory && position == getCount() - 1) {
            // the final item to show the clear icon
            return TYPE_CLEAR;
        }
        return TYPE_NORMAL;
    }

    public void setClearListener(OnClearListener clearListener) {
		mClearListener = clearListener;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (getItem(position) == null) {
			return null;
		}


		int type = getItemViewType(position);
		if (AppDebugConfig.IS_FRAG_DEBUG) {
			KLog.v(getItem(position));
		}
		BGAAdapterViewHolder viewHolder;
		if (type == TYPE_NORMAL) {
			viewHolder = BGAAdapterViewHolder.dequeueReusableAdapterViewHolder(convertView, parent,
					R.layout.item_list_history);
			BGAViewHolderHelper helper = viewHolder.getViewHolderHelper();
			helper.setText(R.id.tv_search_text, mKeywords.get(position));
			helper.getView(R.id.rl_search_history).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mClearListener != null) {
						mClearListener.onSearchPerform(mKeywords.get(position));
					}
				}
			});
		} else {
			viewHolder = BGAAdapterViewHolder.dequeueReusableAdapterViewHolder(convertView, parent,
					R.layout.item_list_history_clear);
			BGAViewHolderHelper helper = viewHolder.getViewHolderHelper();
			helper.getView(R.id.ll_history_clear).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mClearListener != null) {
						mClearListener.onClearPerform();
					}
					updateData(null, true);
				}
			});
		}
		return viewHolder.getViewHolderHelper().getConvertView();
	}

	@Override
	public int getViewTypeCount() {
		if (mIsHistory) {
			return 2;
		} else {
			return 1;
		}
	}

	public void updateData(List<String> data, boolean isHistory) {
		mKeywords = data;
		mIsHistory = isHistory;
		notifyDataSetChanged();
	}

	public interface OnClearListener{
		public void onClearPerform();

		public void onSearchPerform(String keyword);
	}
}
