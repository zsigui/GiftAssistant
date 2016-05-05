package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;

import java.util.ArrayList;

import cn.finalteam.galleryfinal.GalleryFinal;

/**
 * Created by zsigui on 16-5-5.
 */
public class GameDetailPicsAdapter extends BaseRVAdapter<String> implements View.OnClickListener {

	private String[] mPics;

	public GameDetailPicsAdapter(Context context) {
		super(context);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		ImageHolder holder = new ImageHolder(LayoutInflater.from(mContext).inflate(R.layout.view_banner, parent, false));
		ViewGroup.LayoutParams lp = holder.ivBanner.getLayoutParams();
		lp.height = mContext.getResources().getDimensionPixelSize(R.dimen.di_banner_height);
		lp.width = (int)(lp.height * 0.45);
		holder.ivBanner.setLayoutParams(lp);
		return holder;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		ImageHolder imageHolder = (ImageHolder) holder;
		imageHolder.ivBanner.setTag(TAG_POSITION, position);
		imageHolder.ivBanner.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getTag(TAG_POSITION) == null) {
			return;
		}
		Integer pos = (Integer) v.getTag(TAG_POSITION);
		GalleryFinal.openMultiPhoto(pos, mPics);
	}

	@Override
	public void updateData(ArrayList<String> data) {
		super.updateData(data);
		if (data != null) {
			if (mPics == null || data.size() != mPics.length) {
				mPics = new String[data.size()];
			}
			int i = 0;
			for (String s : data) {
				mPics[i++] = s;
			}
		}
	}

	@Override
	public void release() {
		super.release();
		mPics = null;
	}

	private static class ImageHolder extends BaseRVHolder {

		private ImageView ivBanner;

		public ImageHolder(View itemView) {
			super(itemView);
			ivBanner = getViewById(R.id.iv_banner);
		}
	}
}
