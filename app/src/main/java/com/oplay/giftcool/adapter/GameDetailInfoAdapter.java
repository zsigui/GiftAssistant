package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.model.data.resp.GameDetail;
import com.oplay.giftcool.ui.widget.layout.flowlayout.TagFlowLayout;

/**
 * Created by zsigui on 16-5-5.
 */
public class GameDetailInfoAdapter extends BaseRVAdapter {


	private final int TYPE_PICS = 0;
	private final int TYPE_TITLE_DESC = 1;
	private final int TYPE_DESC = 2;
	private final int TYPE_TITLE_POST = 3;
	private final int TYPE_POST = 4;

	private GameDetail mGameData;

	public GameDetailInfoAdapter(Context context) {
		super(context);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
			case TYPE_PICS:
				return new ThumbHolder(LayoutInflater.from(mContext)
						.inflate(R.layout.item_game_detail_info_tags, parent, false));
			case TYPE_TITLE_DESC:
				return new TitleHolder(LayoutInflater.from(mContext)
						.inflate(R.layout.item_game_detail_info_title, parent, false));
			case TYPE_DESC:
				return new DescriptionHolder(LayoutInflater.from(mContext)
						.inflate(R.layout.item_game_detail_info_introduction, parent, false));
			case TYPE_TITLE_POST:
				return new TitleHolder(LayoutInflater.from(mContext)
						.inflate(R.layout.item_game_detail_info_title, parent, false));
		}
		return new PostMessageHolder(LayoutInflater.from(mContext)
				.inflate(R.layout.item_game_detail_info_post, parent, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		switch (getItemViewType(position)) {

		}
	}

	@Override
	public int getItemViewType(int position) {
		switch (position) {
			case 0:
				return TYPE_PICS;
			case 1:
				return TYPE_TITLE_DESC;
			case 2:
				return TYPE_DESC;
			case 3:
				return TYPE_TITLE_POST;
		}
		return TYPE_POST;
	}

	private void updateData(GameDetail data) {
		mGameData = data;
		notifyDataSetChanged();
	}

	@Override
	public void release() {
		super.release();
		mGameData = null;
	}

	private static class ThumbHolder extends BaseRVHolder {

		private RecyclerView rvData;

		private TagFlowLayout tflTags;
		private GameDetailTagAdapter mTagAdapter;

		public ThumbHolder(View itemView) {
			super(itemView);
			rvData = getViewById(R.id.rv_pics);
			tflTags = getViewById(R.id.tf_tags);
		}
	}

	private static class TitleHolder extends BaseRVHolder {

		private TextView tvTitle;

		public TitleHolder(View itemView) {
			super(itemView);
			tvTitle = getViewById(R.id.tv_title);
		}
	}

	private static class DescriptionHolder extends BaseRVHolder {

		private TextView tvDesc;
		private TextView tvExpand;

		public DescriptionHolder(View itemView) {
			super(itemView);
			tvDesc = getViewById(R.id.tv_desc);
			tvExpand = getViewById(R.id.tv_expand);
		}
	}

	private static class PostMessageHolder extends BaseRVHolder {

		private TextView tvTitle;

		public PostMessageHolder(View itemView) {
			super(itemView);
			tvTitle = getViewById(R.id.tv_title);
		}
	}
}
