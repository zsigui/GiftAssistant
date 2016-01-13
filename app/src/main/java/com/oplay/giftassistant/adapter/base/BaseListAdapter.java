package com.oplay.giftassistant.adapter.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.oplay.giftassistant.config.AppDebugConfig;
import com.socks.library.KLog;

import java.util.Arrays;
import java.util.List;

/**
 * BaseListAdapter
 *
 * @author zacklpx
 *         date 16-1-12
 *         description
 */
public abstract class BaseListAdapter<T> extends BaseAdapter implements IBaseAdapter<T>{

	protected static final int TAG_POSITION = 0xFFF11133;
	protected static final int TAG_URL = 0xffff1111;

	protected  List<T> mListData;
	protected Context mContext;
	protected LayoutInflater mLayoutInflater;

	public BaseListAdapter(Context context, List<T> objects) {
		mContext = context;
		mListData = objects;
		mLayoutInflater = LayoutInflater.from(context);
	}

	public BaseListAdapter(Context context, T[] list) {
		this(context, Arrays.asList(list));
	}

	protected void bindImageViewWithUrl(ImageView imageView, String imgUrl, int stubResource) {
		try {
			final Object iconTag = imageView.getTag(TAG_URL);
			if (iconTag != null && !iconTag.equals(imgUrl)) {
				imageView.setImageResource(stubResource);
			}
			imageView.setTag(TAG_URL, imgUrl);
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(BaseListAdapter.class.getSimpleName(), e);
			}
		}
	}

	public void onDestroy() {
		if (mListData != null) {
			mListData.clear();
		}
	}

	public void updateData(List<T> newData) {
		mListData.clear();
		mListData.addAll(newData);
	}

	@Override
	public void notifyDataChanged() {
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mListData.size();
	}

	@Override
	public T getItem(int position) {
		return mListData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
