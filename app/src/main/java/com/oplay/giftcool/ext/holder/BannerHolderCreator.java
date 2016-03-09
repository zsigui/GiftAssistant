package com.oplay.giftcool.ext.holder;

import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;

/**
 * Created by zsigui on 16-3-2.
 */
public class BannerHolderCreator implements CBViewHolderCreator<NetworkImageHolderView> {

	@Override
	public NetworkImageHolderView createHolder() {
		return new NetworkImageHolderView();
	}
}