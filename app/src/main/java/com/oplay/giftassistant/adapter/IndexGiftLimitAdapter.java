package com.oplay.giftassistant.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.model.data.resp.IndexGiftNew;
import com.oplay.giftassistant.util.IntentUtil;

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
		ImageLoader.getInstance().displayImage(o.img, bgaViewHolderHelper.<ImageView>getView(R.id
				.iv_icon), Global.IMAGE_OPTIONS);
		bgaViewHolderHelper.getView(R.id.rl_recommend).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				IntentUtil.jumpGiftDetail(mRecyclerView.getContext(), o.id,
						String.format("[%s]%s", o.gameName, o.name));
			}
		});

	}

	public void updateData(List<IndexGiftNew> data) {
		this.mDatas = data;
		notifyDataSetChanged();
	}
}
