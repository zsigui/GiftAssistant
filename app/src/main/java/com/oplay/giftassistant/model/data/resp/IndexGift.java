package com.oplay.giftassistant.model.data.resp;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zsigui on 15-12-28.
 */
public class IndexGift implements Serializable {

	public ArrayList<IndexGiftBanner> banner;

	public ArrayList<IndexGiftLike> like;

	public ArrayList<IndexGiftLimit> limit;

	public ArrayList<IndexGiftNew> news;
}
