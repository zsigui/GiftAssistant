package com.oplay.giftcool.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.oplay.giftcool.R;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.model.data.resp.GameTypeMain;
import com.oplay.giftcool.ui.widget.button.TagButton;

import java.util.ArrayList;

import cn.bingoogolapple.androidcommon.adapter.BGARecyclerViewAdapter;
import cn.bingoogolapple.androidcommon.adapter.BGAViewHolderHelper;

/**
 * Created by zsigui on 16-1-10.
 */
public class GameTagAdapter extends BGARecyclerViewAdapter<GameTypeMain> {

	private OnItemClickListener<GameTypeMain> mItemClickListener;

	public GameTagAdapter(RecyclerView recyclerView) {
		super(recyclerView, R.layout.item_grid_game_type_tag);
	}

	public GameTagAdapter(RecyclerView recyclerView, ArrayList<GameTypeMain> data) {
		this(recyclerView);
		this.mDatas = data;
	}

	public void setItemClickListener(OnItemClickListener<GameTypeMain> itemClickListener) {
		mItemClickListener = itemClickListener;
	}

	@Override
	protected void fillData(BGAViewHolderHelper bgaViewHolderHelper, final int i, final GameTypeMain o) {
		int state = TagButton.STATE_NONE;
		if (i >= 1 && i == 1) {
			state = TagButton.STATE_RED;
		} else if (i >= 3 && i == 3) {
			state = TagButton.STATE_ORANGE;
		} else if (i >= 8 && i == 8) {
			state = TagButton.STATE_BLUE;
		} else if (i >= 9 && i == 9) {
			state = TagButton.STATE_PURPLE;
		} else if (i >= 13 && i == 13) {
			state = TagButton.STATE_LIGHT_GREEN;
		}
		bgaViewHolderHelper.setText(R.id.tv_tag, o.name);
		((TagButton) bgaViewHolderHelper.getView(R.id.tv_tag)).setState(state);
		bgaViewHolderHelper.getView(R.id.ll_item).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mItemClickListener != null) {
					mItemClickListener.onItemClick(o, null, i);
				}
			}
		});
	}

	public void updateData(ArrayList<GameTypeMain> data) {
		if (data == null)
			return;
		this.mDatas = data;
		notifyDataSetChanged();
	}
}
