package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.model.data.resp.GameTypeMain;
import com.oplay.giftcool.ui.widget.button.TagButton;
import com.oplay.giftcool.util.IntentUtil;

/**
 * Created by zsigui on 16-1-10.
 */
public class GameTagAdapter extends BaseRVAdapter<GameTypeMain> implements View.OnClickListener{


	private static final int TAG_POSITION = 0xFFFF1444;

	public GameTagAdapter(Context context) {
		super(context);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new TagHolder(LayoutInflater.from(mContext).inflate(R.layout.item_grid_game_type_tag, parent, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		TagHolder tagHolder =(TagHolder) holder;
		GameTypeMain o = getItem(position);
		tagHolder.tvTag.setText(o.name);
		tagHolder.tvTag.setState(getState(position));
		tagHolder.itemView.setOnClickListener(this);
		tagHolder.itemView.setTag(TAG_POSITION, position);
	}

	/**
	 * 获取要显示的背景状态
	 */
	private int getState(int pos) {
		int state = TagButton.STATE_NONE;
		int k = pos + pos/15;
		if (pos >= 15 && k % 3== 2) {
			k = k % 15 - 2;
		}
		switch (k) {
			case 1:
				state = TagButton.STATE_RED;
				break;
			case 3:
				state = TagButton.STATE_ORANGE;
				break;
			case 8:
				state = TagButton.STATE_BLUE;
				break;
			case 9:
				state = TagButton.STATE_PURPLE;
				break;
			case 13:
				state = TagButton.STATE_LIGHT_GREEN;
				break;
		}
		return state;
	}

	@Override
	public void onClick(View v) {
		if (mData == null || v.getTag(TAG_POSITION) == null) {
			return;
		}
		GameTypeMain o = getItem((Integer)v.getTag(TAG_POSITION));
		IntentUtil.jumpGameTagList(mContext, o.id, o.name);
	}

	static class TagHolder extends BaseRVHolder {
		TagButton tvTag;

		public TagHolder(View itemView) {
			super(itemView);
			tvTag = getViewById(R.id.tv_tag);
		}
	}
}
