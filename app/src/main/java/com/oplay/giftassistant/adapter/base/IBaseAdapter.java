package com.oplay.giftassistant.adapter.base;

import java.util.List;

/**
 * IBaseAdapter
 *
 * @author zacklpx
 *         date 16-1-12
 *         description
 */
public interface IBaseAdapter<T> {
	void onDestroy();

	int getCount();

	T getItem(int position);

	void updateData(List<T> newData);

	void notifyDataChanged();
}
