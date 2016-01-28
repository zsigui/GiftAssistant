package com.oplay.giftcool.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.config.GiftTypeUtil;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ViewUtil;

/**
 * Created by zsigui on 15-12-24.
 */
public class IndexGiftZeroAdapter extends BaseRVAdapter<IndexGiftNew> implements View.OnClickListener {

	private static final int TAG_POSITION = 0xFF01F112;
	private boolean mIsNotifyUpdate = false;

	public IndexGiftZeroAdapter(Context context) {
		super(context);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ContentHolder(LayoutInflater.from(mContext).inflate(R.layout.item_index_gift_zero, parent, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		IndexGiftNew data = getItem(position);
		if (data == null) return;
		final ContentHolder contentHolder = (ContentHolder) holder;
		ViewUtil.showImage(contentHolder.ivIcon, data.img);
		contentHolder.itemView.setOnClickListener(this);
		contentHolder.itemView.setTag(TAG_POSITION, position);
		contentHolder.tvSrc.setText(String.format("原:¥%d", data.orginPrice));
		contentHolder.tvSrc.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
		contentHolder.tvName.setText(data.name);
		contentHolder.tvGameName.setText(data.gameName);
		contentHolder.tvTagSeize.setEnabled(false);
		if (data.seizeStatus == GiftTypeUtil.SEIZE_TYPE_SEIZED){
			contentHolder.tvTagSeize.setText("已抢");
		}else {
			switch (data.status) {
				case GiftTypeUtil.STATUS_WAIT_SEIZE:
					contentHolder.tvTagSeize.setText(mContext.getResources().getString(R.string.st_0_gift_wait_seize));
					long seizeTime = DateUtil.getTime(data.seizeTime);
					CountDownTimer timer = new CountDownTimer(seizeTime - System.currentTimeMillis(), 1000) {

						@Override
						public void onTick(long millisUntilFinished) {
							contentHolder.tvTagSeize.setText(DateUtil.formatTime(millisUntilFinished, "HH:mm:ss"));
						}

						@Override
						public void onFinish() {
							contentHolder.tvTagSeize.setText(mContext.getResources().getString(R.string.st_0_gift_seize));
							if (mIsNotifyUpdate) {
								ObserverManager.getInstance().notifyGiftUpdate();
								setNotifyUpdate(false);
							}
						}
					};
					timer.start();
					break;
				case GiftTypeUtil.STATUS_SEIZE:
					contentHolder.tvTagSeize.setText(mContext.getResources().getString(R.string.st_0_gift_seize));
					contentHolder.tvTagSeize.setEnabled(true);
					break;
				case GiftTypeUtil.STATUS_WAIT_SEARCH:
					contentHolder.tvTagSeize.setText(mContext.getResources().getString(R.string.st_gift_seized));
					break;
				case GiftTypeUtil.STATUS_FINISHED:
					contentHolder.tvTagSeize.setText(mContext.getResources().getString(R.string.st_gift_finished));
					break;
			}
		}

	}

	public void setNotifyUpdate(boolean isNotifyUpdate) {
		this.mIsNotifyUpdate = isNotifyUpdate;
	}

	@Override
	public void onClick(View v) {
		Object object = v.getTag();
		if (object != null) {
			int pos = (int) object;
			IntentUtil.jumpGiftDetail(mContext, getItem(pos).id);
		}
	}

	class ContentHolder extends BaseRVHolder {
		ImageView ivIcon;
		TextView tvSrc;
		TextView tvGameName;
		TextView tvName;
		TextView tvTagSeize;


		public ContentHolder(View itemView) {
			super(itemView);
			ivIcon = getViewById(R.id.iv_icon);
			tvSrc = getViewById(R.id.tv_src);
			tvGameName = getViewById(R.id.tv_game_name);
			tvName = getViewById(R.id.tv_name);
			tvTagSeize = getViewById(R.id.tv_tag_seize);
		}
	}
}
