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
import com.oplay.giftcool.util.ViewUtil;
import com.socks.library.KLog;

import java.util.List;

/**
 * Created by zsigui on 15-12-23.
 */
public class SearchHistoryAdapter extends BaseListAdapter<String> implements OnFinishListener, View.OnClickListener {

	private static final int TYPE_NORMAL = 0;
	private static final int TYPE_CLEAR = 1;
    private static final int TAG_TYPE = 0x12344444;

    private boolean mIsHistory;
	private List<String> mKeywords;
	private OnClearListener mClearListener;

	public SearchHistoryAdapter(Context context, List<String> keywords, boolean isHistory) {
        super(context, keywords);
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
	public String getItem(int position) {
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
		if (getCount() == 0) {
			return null;
		}


		int type = getItemViewType(position);
		if (AppDebugConfig.IS_FRAG_DEBUG) {
			KLog.d(AppDebugConfig.TAG_ADAPTER, getItem(position) + ", type = " + type);
		}
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
                holder.tvKey.setText(mKeywords.get(position));
                break;
            case TYPE_CLEAR:
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list_history_clear, parent, false);
                }
                break;
        }
        convertView.setTag(TAG_POSITION, position);
        convertView.setTag(TAG_TYPE, type);
        convertView.setOnClickListener(this);
		return convertView;
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

	@Override
	public void release() {
		mClearListener = null;
		if (mKeywords != null) {
			mKeywords.clear();
			mKeywords = null;
		}
	}

    @Override
    public void onClick(View v) {
        if (mListData == null || v.getTag(TAG_TYPE) == null) {
            return;
        }
        switch ((Integer)v.getTag(TAG_TYPE)) {
            case TYPE_NORMAL:
                if (mClearListener != null) {
                    mClearListener.onSearchPerform(mKeywords.get((Integer) v.getTag(TAG_POSITION)));
                }
                break;
            case TYPE_CLEAR:
                if (mClearListener != null) {
                    mClearListener.onClearPerform();
                }
                updateData(null, true);
                break;
        }
    }

    public interface OnClearListener{
        void onClearPerform();

        void onSearchPerform(String keyword);
	}

    static class NormalHolder {
        TextView tvKey;
    }
}
