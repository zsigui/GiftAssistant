package com.oplay.giftcool.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.assist.CountTimer;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.util.GiftTypeUtil;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.util.IndexTypeUtil;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.manager.StatisticsManager;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ThreadUtil;
import com.oplay.giftcool.util.ViewUtil;
import com.socks.library.KLog;

/**
 * Created by zsigui on 15-12-24.
 */
public class IndexGiftZeroAdapter extends BaseRVAdapter<IndexGiftNew> implements View.OnClickListener {

	private final String mDefaultTime = "00:00:00";
//	private long mSeizeTime = 0;
	private ZeroCountTimer mCountTimer;
	private final int TAG_COUNTER = 0x1233aaaa;

	public IndexGiftZeroAdapter(Context context) {
		super(context.getApplicationContext());
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
		contentHolder.itemView.setTag(IndexTypeUtil.TAG_POSITION, position);
		contentHolder.tvSrc.setText(String.format("值:¥%d", data.originPrice));
		contentHolder.tvSrc.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
		contentHolder.tvName.setText(data.name);
		contentHolder.tvGameName.setText(data.gameName);
		contentHolder.tvTagSeize.setEnabled(false);
		contentHolder.tvTagSeize.setBackgroundResource(R.drawable.selector_btn_zero_seize);
		ZeroCountTimer countTimer = null;
		if (contentHolder.itemView.getTag(TAG_COUNTER) != null) {
			countTimer = (ZeroCountTimer) contentHolder.itemView.getTag(TAG_COUNTER);
			countTimer.cancel();
		}
		if (data.seizeStatus != GiftTypeUtil.SEIZE_TYPE_NEVER) {
			contentHolder.tvTagSeize.setText("已抢号");
		} else {
			switch (data.status) {
				case GiftTypeUtil.STATUS_WAIT_SEIZE:
					contentHolder.tvTagSeize.setText(mDefaultTime);
					contentHolder.tvTagSeize.setBackgroundResource(R.drawable.ic_0_seize_btn_count);
					final long remainTime = DateUtil.getTime(data.seizeTime)
							- System.currentTimeMillis() + Global.sServerTimeDiffLocal;
					if (countTimer == null) {
						countTimer = new ZeroCountTimer(remainTime, Global.COUNTDOWN_INTERVAL);
						countTimer.start();
					} else {
						countTimer.restart(remainTime);
					}
					countTimer.setTextView(contentHolder.tvTagSeize);
					contentHolder.itemView.setTag(TAG_COUNTER, countTimer);
					break;
				case GiftTypeUtil.STATUS_SEIZE:
					contentHolder.tvTagSeize.setEnabled(true);
					contentHolder.tvTagSeize.setText(mContext.getResources().getString(R.string.st_0_gift_seize));
					break;
				case GiftTypeUtil.STATUS_WAIT_SEARCH:
					contentHolder.tvTagSeize.setText(mContext.getResources().getString(R.string.st_gift_empty));
					break;
				case GiftTypeUtil.STATUS_FINISHED:
					contentHolder.tvTagSeize.setText(mContext.getResources().getString(R.string.st_gift_finished));
					break;
			}
		}

	}

	@Override
	public void onClick(View v) {
		try {
			final Object tag = v.getTag(IndexTypeUtil.TAG_POSITION);
			int position = (Integer) tag;
			if (position < 0 || mData == null || position >= mData.size()) {
				if (AppDebugConfig.IS_DEBUG) {
					KLog.e(AppDebugConfig.TAG_ADAPTER, "position = " + position + ", data = " + mData);
				}
				return;
			}
			IndexGiftNew data = mData.get(position);
			if (data != null) {
				IntentUtil.jumpGiftDetail(mContext, data.id);
				if (AppDebugConfig.IS_STATISTICS_SHOW) {
					StatisticsManager.getInstance().trace(mContext, StatisticsManager.ID.GIFT_ZERO_ITEM,
							String.format("第%s项点击:[%s]%s", position, data.gameName, data.name));
				}
			}
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(AppDebugConfig.TAG_ADAPTER, e);
			}
		}
	}

	@Override
	public void release() {
		super.release();
		if (mCountTimer != null) {
			mCountTimer.cancel();
			mCountTimer = null;
		}
	}

	static class ZeroCountTimer extends CountTimer {

		private TextView mTv;

		public ZeroCountTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		public void setTextView(TextView tv) {
			mTv = tv;
		}

		public void setText(String s) {
			if (mTv != null)
				mTv.setText(s);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			String s = DateUtil.formatTime(millisUntilFinished, "HH:mm:ss");
			setText(s);
		}

		@Override
		public void onFinish() {
			setText("刷新抢");
			ThreadUtil.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					ObserverManager.getInstance().notifyGiftUpdate(ObserverManager.STATUS
							.GIFT_UPDATE_ALL);
				}
			}, 2500);
		}
	}

	static class ContentHolder extends BaseRVHolder {
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
