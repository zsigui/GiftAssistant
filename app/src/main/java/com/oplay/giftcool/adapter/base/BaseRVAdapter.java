package com.oplay.giftcool.adapter.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.oplay.giftcool.listener.OnFinishListener;
import com.oplay.giftcool.listener.OnItemClickListener;

import java.util.ArrayList;

/**
 * Created by zsigui on 16-1-16.
 */
public abstract class BaseRVAdapter<T> extends RecyclerView.Adapter implements OnFinishListener {

    protected static final int TAG_POSITION = 0x1234478f;

	protected ArrayList<T> mData;
	protected OnItemClickListener<T> mListener;
	protected Context mContext;

	public BaseRVAdapter(Context context) {
		this(context, null);
	}

	public BaseRVAdapter(Context context, ArrayList<T> data) {
		this(context, data, null);
	}

	public BaseRVAdapter(Context context, ArrayList<T> data, OnItemClickListener<T> listener) {
		mData = data;
		mListener = listener;
		mContext = context;
	}

	public T getItem(int position) {
		return getItemCount() <= position ? null : mData.get(position);
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

	@Override
	public void release() {

	}
}
