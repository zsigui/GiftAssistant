package com.oplay.giftcool.model;

import android.view.View;

/**
 * Created by zsigui on 16-1-21.
 */
public class DrawerModel {

	public View.OnClickListener listener;
	public int icon;
	public String name;

	public DrawerModel(int icon, String name, View.OnClickListener listener) {
		this.icon = icon;
		this.name = name;
		this.listener = listener;
	}
}
