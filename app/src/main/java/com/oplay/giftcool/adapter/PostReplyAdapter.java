package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.ui.fragment.postbar.PostDetailFragment;
import com.oplay.giftcool.util.ViewUtil;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.model.PhotoInfo;

/**
 * 发帖回复图片的预览适配器
 *
 * Created by zsigui on 16-4-11.
 */
public class PostReplyAdapter extends BaseRVAdapter<PhotoInfo> implements View.OnClickListener,
		GalleryFinal.OnHandlerResultCallback {

	private String TEXT_PICK_HINT = "已选%d张，还可以选择%d张";
	public final int REQ_ID_IMG_ADD = 0x12343;

	private TextView tvPickHint;
	private PostDetailFragment mFragment;

	public PostReplyAdapter(Context context) {
		this(context, null);
	}

	public PostReplyAdapter(Context context, ArrayList<PhotoInfo> data) {
		super(context, data);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ItemHolder(LayoutInflater.from(mContext).inflate(R.layout.item_list_post_reply, parent, false));
	}

	public void setTvPickHint(TextView tvPickHint) {
		this.tvPickHint = tvPickHint;
		setPicTextVal();
	}

	public void setFragment(PostDetailFragment fragment) {
		mFragment = fragment;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		KLog.d(AppDebugConfig.TAG_WARN, "itemCount = " + getItemCount() + ", position = " + position);
		ItemHolder itemHolder = (ItemHolder) holder;
		itemHolder.ivThumb.setTag(TAG_POSITION, itemHolder);
		itemHolder.ivThumb.setOnClickListener(this);
		itemHolder.ivDel.setTag(TAG_POSITION, itemHolder);
		itemHolder.ivDel.setOnClickListener(this);
		if (position == getItemCount() - 1) {
			itemHolder.ivDel.setVisibility(View.GONE);
			itemHolder.ivThumb.setBackgroundResource(R.drawable.shape_dash_border);
			itemHolder.ivThumb.setImageResource(R.drawable.ic_photo_add);
			itemHolder.ivThumb.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

			return;
		}
		PhotoInfo info = getItem(position);
		itemHolder.ivDel.setVisibility(View.VISIBLE);
		itemHolder.ivThumb.setBackgroundResource(0);
		KLog.d(AppDebugConfig.TAG_WARN, "photoPath = " + info.getPhotoPath());
		ViewUtil.showImage(itemHolder.ivThumb, "file://" + info.getPhotoPath());
		itemHolder.ivThumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
	}

	@Override
	public PhotoInfo getItem(int position) {
		return position == mData.size() ? null : mData.get(position);
	}

	@Override
	public int getItemCount() {
		return (mData == null ? 0 : mData.size()) + 1;
	}

	@Override
	public void onClick(View v) {
		if (v.getTag(TAG_POSITION) == null) {
			KLog.d(AppDebugConfig.TAG_WARN, "is pos null");
			return;
		}
		int pos = ((ItemHolder)v.getTag(TAG_POSITION)).getAdapterPosition();
		if (pos == getItemCount() - 1) {
			if (v.getId() == R.id.iv_thumb) {
				GalleryFinal.openGalleryMulti(REQ_ID_IMG_ADD, mData, Global.REPLY_IMG_COUNT, this);
			}
			return;
		}
		switch (v.getId()) {
			case R.id.iv_thumb:
				GalleryFinal.openMultiPhoto(pos, mData);
				break;
			case R.id.iv_delete:
				mData.remove(pos);
				notifyItemRemoved(pos);
				setPicTextVal();
				if (mData.size() == 0 && mFragment != null) {
					mFragment.pickFailed();
				}
				break;
		}
	}

	private void setPicTextVal() {
		if (tvPickHint != null) {
			tvPickHint.setText(String.format(TEXT_PICK_HINT, mData.size(),
					Global.REPLY_IMG_COUNT - mData.size()));
		}
	}

	@Override
	public void onHandlerSuccess(int requestCode, List<PhotoInfo> resultList) {
		if (requestCode == REQ_ID_IMG_ADD) {
			mData.clear();
			mData.addAll(resultList);
			notifyItemRangeChanged(0, getItemCount());
			setPicTextVal();
			if (mFragment != null) {
				mFragment.pickSuccess();
			}
		}
	}

	@Override
	public void onHandlerFailure(int requestCode, String errorMsg) {
		// 取消不做处理
		KLog.d(AppDebugConfig.TAG_WARN, "errorMsg = " + errorMsg);
	}

	private static class ItemHolder extends BaseRVHolder{

		private ImageView ivThumb;
		private ImageView ivDel;

		public ItemHolder(View itemView) {
			super(itemView);
			ivThumb = getViewById(R.id.iv_thumb);
			ivDel = getViewById(R.id.iv_delete);
		}
	}
}
