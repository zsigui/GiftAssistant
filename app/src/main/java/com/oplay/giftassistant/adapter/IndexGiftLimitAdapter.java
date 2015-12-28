package com.oplay.giftassistant.adapter;

import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.model.data.resp.IndexGiftLimit;

import java.util.List;

import cn.bingoogolapple.androidcommon.adapter.BGARecyclerViewAdapter;
import cn.bingoogolapple.androidcommon.adapter.BGAViewHolderHelper;

/**
 * Created by zsigui on 15-12-24.
 */
public class IndexGiftLimitAdapter extends BGARecyclerViewAdapter<IndexGiftLimit> {

	public IndexGiftLimitAdapter(RecyclerView recyclerView) {
		super(recyclerView, R.layout.item_index_gift_limit);
	}

	public IndexGiftLimitAdapter(RecyclerView recyclerView, List<IndexGiftLimit> data) {
		super(recyclerView, R.layout.item_index_gift_limit);
		this.mDatas = data;
	}

	@Override
	protected void fillData(BGAViewHolderHelper bgaViewHolderHelper, int i, IndexGiftLimit giftLimitGame) {
		bgaViewHolderHelper.setText(R.id.tv_game_name, giftLimitGame.gameName);
		bgaViewHolderHelper.setText(R.id.tv_name, giftLimitGame.name);
		bgaViewHolderHelper.setText(R.id.tv_remain, String.valueOf(giftLimitGame.remainCount));
		ImageLoader.getInstance().displayImage(giftLimitGame.img, bgaViewHolderHelper.<ImageView>getView(R.id
				.iv_icon), Global.IMAGE_OPTIONS);
	}

	public void updateData(List<IndexGiftLimit> data) {
		this.mDatas = data;
		notifyDataSetChanged();
	}
}
