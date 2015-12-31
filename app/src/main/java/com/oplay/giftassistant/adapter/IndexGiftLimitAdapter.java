package com.oplay.giftassistant.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.model.data.resp.IndexGiftLimit;
import com.oplay.giftassistant.ui.activity.DetailActivity;

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
	protected void fillData(BGAViewHolderHelper bgaViewHolderHelper, int i, final IndexGiftLimit o) {
		bgaViewHolderHelper.setText(R.id.tv_game_name, o.gameName);
		bgaViewHolderHelper.setText(R.id.tv_name, o.name);
		bgaViewHolderHelper.setText(R.id.tv_new, String.valueOf(o.remainCount));
		ImageLoader.getInstance().displayImage(o.img, bgaViewHolderHelper.<ImageView>getView(R.id
				.iv_icon), Global.IMAGE_OPTIONS);
		bgaViewHolderHelper.getView(R.id.rl_recommend).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mRecyclerView.getContext(), DetailActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra(DetailActivity.KEY_DETAIL_ID, o.id);
				intent.putExtra(DetailActivity.KEY_DETAIL_NAME, String.format("[%s]%s", o.gameName, o.name));
				mRecyclerView.getContext().startActivity(intent);
			}
		});

	}

	public void updateData(List<IndexGiftLimit> data) {
		this.mDatas = data;
		notifyDataSetChanged();
	}
}
