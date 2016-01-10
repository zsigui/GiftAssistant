package com.oplay.giftassistant.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.listener.OnItemClickListener;
import com.oplay.giftassistant.model.data.resp.GameTypeMain;

import java.util.ArrayList;

import cn.bingoogolapple.androidcommon.adapter.BGARecyclerViewAdapter;
import cn.bingoogolapple.androidcommon.adapter.BGAViewHolderHelper;

/**
 * Created by zsigui on 16-1-10.
 */
public class GameTypeMainAdapter extends BGARecyclerViewAdapter<GameTypeMain> {

	private OnItemClickListener<GameTypeMain> mItemClickListener;

	public GameTypeMainAdapter(RecyclerView recyclerView) {
		super(recyclerView, R.layout.item_grid_game_type_main);
	}

	public GameTypeMainAdapter(RecyclerView recyclerView, ArrayList<GameTypeMain> data) {
		this(recyclerView);
		this.mDatas = data;
	}

	public void setItemClickListener(OnItemClickListener<GameTypeMain> itemClickListener) {
		mItemClickListener = itemClickListener;
	}

	@Override
	protected void fillData(BGAViewHolderHelper bgaViewHolderHelper, final int i, final GameTypeMain o) {
		bgaViewHolderHelper.setText(R.id.tv_name, o.name);
		ImageLoader.getInstance().displayImage("drawable://" + o.icon, (ImageView) bgaViewHolderHelper.getView(R.id.iv_icon));
		bgaViewHolderHelper.getView(R.id.ll_item).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mItemClickListener != null) {
					mItemClickListener.onItemClick(o, i);
				}
			}
		});
	}

	public void updateData(ArrayList<GameTypeMain> data) {
		if (this.mDatas == null)
			return;
		this.mDatas = data;
		notifyDataSetChanged();
	}

}
