package com.oplay.giftcool.model;

import android.view.View;

/**
 * Created by zsigui on 16-1-21.
 */
public class DrawerModel {

	public int id;
	public View.OnClickListener listener;
	public int icon;
	public String name;
	public int count;

	public DrawerModel(int id, int icon, String name, View.OnClickListener listener) {
		this(id, icon, name, listener, 0);
	}

	public DrawerModel(int id, int icon, String name, View.OnClickListener listener, int count) {
		this.id = id;
		this.icon = icon;
		this.name = name;
		this.listener = listener;
		this.count = count;
	}
}
