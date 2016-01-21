package com.oplay.giftcool.adapter.base;

import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.oplay.giftcool.util.ViewUtil;

/**
 * Created by zsigui on 16-1-16.
 */
public abstract class BaseRVHolder extends RecyclerView.ViewHolder {

	public View itemView;

	public BaseRVHolder(View itemView) {
		super(itemView);
		this.itemView = itemView;
	}

	public <VT extends View> VT getViewById(@IdRes int id) {
		return ViewUtil.getViewById(itemView, id);
	}
}
