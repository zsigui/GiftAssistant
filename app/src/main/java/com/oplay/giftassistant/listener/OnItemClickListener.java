package com.oplay.giftassistant.listener;

/**
 *
 * 自定义实现的列表项点击监听接口，实现对项内部按钮的判断 <br/>
 * Created by zsigui on 16-1-7.
 */
public interface OnItemClickListener<T> {

	void onItemClick(T item, int position);
}
