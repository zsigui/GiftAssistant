package com.oplay.giftcool.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ViewUtil;

import java.util.List;

import cn.bingoogolapple.androidcommon.adapter.BGARecyclerViewAdapter;
import cn.bingoogolapple.androidcommon.adapter.BGAViewHolderHelper;

/**
 * Created by zsigui on 15-12-24.
 */
public class IndexGiftLimitAdapter extends BGARecyclerViewAdapter<IndexGiftNew> {

	public IndexGiftLimitAdapter(RecyclerView recyclerView) {
		super(recyclerView, R.layout.item_index_gift_limit);
	}

	public IndexGiftLimitAdapter(RecyclerView recyclerView, List<IndexGiftNew> data) {
		super(recyclerView, R.layout.item_index_gift_limit);
		this.mDatas = data;
	}

	@Override
	protected void fillData(BGAViewHolderHelper bgaViewHolderHelper, int i, final IndexGiftNew o) {
		bgaViewHolderHelper.setText(R.id.tv_game_name, o.gameName);
		bgaViewHolderHelper.setText(R.id.tv_name, o.name);
		bgaViewHolderHelper.setText(R.id.tv_remain, String.valueOf(o.remainCount));
		ViewUtil.showImage(bgaViewHolderHelper.<ImageView>getView(R.id.iv_icon), o.img);
		bgaViewHolderHelper.getView(R.id.rl_recommend).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				IntentUtil.jumpGiftDetail(mRecyclerView.getContext(), o.id);
			}
		});

	}

	public void updateData(List<IndexGiftNew> data) {
		this.mDatas = data;
		notifyDataSetChanged();
	}
}
