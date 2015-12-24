package com.oplay.giftassistant.adapter;

import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.model.data.resp.IndexLikeGame;

import java.util.List;

import cn.bingoogolapple.androidcommon.adapter.BGARecyclerViewAdapter;
import cn.bingoogolapple.androidcommon.adapter.BGAViewHolderHelper;

/**
 * Created by zsigui on 15-12-24.
 */
public class IndexGiftLikeAdapter extends BGARecyclerViewAdapter<IndexLikeGame> {

	public IndexGiftLikeAdapter(RecyclerView recyclerView) {
		super(recyclerView, R.layout.item_index_gift_like);
	}

	public IndexGiftLikeAdapter(RecyclerView recyclerView, List<IndexLikeGame> data) {
		super(recyclerView, R.layout.item_index_gift_like);
		this.mDatas = data;
	}


	@Override
	protected void fillData(BGAViewHolderHelper bgaViewHolderHelper, int i, IndexLikeGame o) {
		bgaViewHolderHelper.setText(R.id.tv_game_name, o.name);
		bgaViewHolderHelper.setText(R.id.tv_remain, o.newGiftCount);
		// n款礼包
		ImageLoader.getInstance().displayImage(o.img, bgaViewHolderHelper.<ImageView>getView(R.id.iv_icon));
	}

	public void updateData(List<IndexLikeGame> data) {
		this.mDatas = data;
		notifyDataSetChanged();
	}

}
