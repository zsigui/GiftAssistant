package com.oplay.giftassistant.adapter.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.oplay.giftassistant.listener.OnItemClickListener;

import java.util.ArrayList;

/**
 * Created by zsigui on 16-1-16.
 */
public abstract class BaseRVAdapter<T> extends RecyclerView.Adapter {

	protected ArrayList<T> mData;
	protected OnItemClickListener<T> mListener;
	protected Context mContext;

	protected BaseRVAdapter(Context context) {
		this(context, null);
	}

	protected BaseRVAdapter(Context context, ArrayList<T> data) {
		this(context, data, null);
	}

	protected BaseRVAdapter(Context context, ArrayList<T> data, OnItemClickListener<T> listener) {
		mData = data;
		mListener = listener;
		mContext = context;
	}

	@Override
	public int getItemCount() {
		return mData == null? 0 : mData.size();
	}

	public void setData(ArrayList<T> data) {
		mData = data;
	}

	public void setListener(OnItemClickListener<T> listener) {
		mListener = listener;
	}

	/**
	 * 更新数据并通知界面刷新，需要传入数据非null和非空
	 */
	public void updateData(ArrayList<T> data) {
		if (data == null || data.size() == 0) {
			return;
		}
		this.mData = data;
		notifyDataSetChanged();
	}

}
