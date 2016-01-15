package com.oplay.giftassistant.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.listener.OnItemClickListener;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;
import com.oplay.giftassistant.util.IntentUtil;
import com.oplay.giftassistant.util.ToastUtil;
import com.oplay.giftassistant.util.ViewUtil;

import java.util.ArrayList;

import cn.bingoogolapple.androidcommon.adapter.BGAViewHolderHelper;

/**
 * Created by zsigui on 15-12-31.
 */
public class IndexGameHotWithTitleAdapter extends RecyclerView.Adapter {

	public static final int ITEM_HEADER = 0;
	public static final int ITEM_NORMAL = 1;

	private ArrayList<IndexGameNew> mData;
	private OnItemClickListener<IndexGameNew> mListener;

	private RecyclerView mDataView;
	private Context mContext;

	public IndexGameHotWithTitleAdapter(RecyclerView recyclerView) {
		mDataView = recyclerView;
		mContext = recyclerView.getContext();
	}

	public void setListener(OnItemClickListener<IndexGameNew> listener) {
		mListener = listener;
	}

	@Override
	public int getItemCount() {
		return mData == null? 0 : mData.size();
	}

	public void setData(ArrayList<IndexGameNew> data) {
		mData = data;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
			case ITEM_HEADER:
				return new HeaderVH(LayoutInflater.from(mContext).inflate(R.layout.view_banner, parent, false));
			case ITEM_NORMAL:
				return new HeaderVH(LayoutInflater.from(mContext).inflate(R.layout.view_banner, parent, false));
		}
		return null;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0) {
			return ITEM_HEADER;
		}
		return ITEM_NORMAL;
	}


	protected void fillData(BGAViewHolderHelper bgaViewHolderHelper, int i, final IndexGameNew o) {
		bgaViewHolderHelper.setText(R.id.tv_name, o.name);
		if (o.newCount > 0) {
			bgaViewHolderHelper.setVisibility(R.id.iv_gift, View.VISIBLE);
		} else {
			bgaViewHolderHelper.setVisibility(R.id.iv_gift, View.GONE);
		}
		ImageLoader.getInstance().displayImage(o.img, (ImageView) bgaViewHolderHelper.getView(R.id.iv_icon),
				Global.IMAGE_OPTIONS);
		bgaViewHolderHelper.getView(R.id.rl_recommend).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				IntentUtil.jumpGameDetail(mContext, o.id, o.name);
			}
		});
		bgaViewHolderHelper.getView(R.id.tv_download).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastUtil.showShort(o.name + " 开始下载");
			}
		});
	}

	public void updateData(ArrayList<IndexGameNew> data) {
		this.mData = data;
		notifyDataSetChanged();
	}

	class HeaderVH extends RecyclerView.ViewHolder {

		TextView tvTitle;
		public HeaderVH(View itemView) {
			super(itemView);
			tvTitle = ViewUtil.getViewById(itemView, R.id.tv_title);
		}
	}

	class NormalVH extends RecyclerView.ViewHolder {

		TextView tvName;
		ImageView ivGift;
		ImageView ivIcon;
		TextView btnDownload;

		public NormalVH(View itemView) {
			super(itemView);
			tvName = ViewUtil.getViewById(itemView, R.id.tv_name);
			ivGift = ViewUtil.getViewById(itemView, R.id.iv_gift);
			ivIcon = ViewUtil.getViewById(itemView, R.id.iv_icon);
			btnDownload = ViewUtil.getViewById(itemView, R.id.btn_download);
		}
	}
}
