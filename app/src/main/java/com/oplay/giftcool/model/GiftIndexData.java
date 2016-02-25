package com.oplay.giftcool.model;

import java.io.Serializable;

/**
 * Created by zsigui on 16-2-23.
 */
public class GiftIndexData implements Serializable {

	private int mType;

	private Serializable mData;

	public int getType() {
		return mType;
	}

	public void setType(int type) {
		mType = type;
	}

	public Serializable getData() {
		return mData;
	}

	public void setData(Serializable data) {
		mData = data;
	}
}
