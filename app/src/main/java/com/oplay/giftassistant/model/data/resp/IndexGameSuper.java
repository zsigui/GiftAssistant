package com.oplay.giftassistant.model.data.resp;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zsigui on 15-12-31.
 */
public class IndexGameSuper implements Serializable {

	public ArrayList<IndexGameBanner> banner;

	public ArrayList<IndexGameNew> hot;

	public IndexGameNew recommend;

	public ArrayList<IndexGameNew> news;
}
