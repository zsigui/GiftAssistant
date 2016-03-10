package com.oplay.giftcool.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
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
import com.oplay.giftcool.config.GiftTypeUtil;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.IndexTypeUtil;
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
	private long mSeizeTime = 0;
	private ZeroCountTimer mCountTimer;

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
		if (data.status == GiftTypeUtil.STATUS_WAIT_SEIZE
				&& (mSeizeTime < 0 || System.currentTimeMillis() > mSeizeTime)) {
			mSeizeTime = DateUtil.getTime(data.seizeTime);
			if (mCountTimer == null) {
				mCountTimer = new ZeroCountTimer(mSeizeTime - System.currentTimeMillis() + Global.sServerTimeDiffLocal,
						Global.COUNTDOWN_INTERVAL);
				mCountTimer.start();
			} else {
				mCountTimer.restart(System.currentTimeMillis() - mSeizeTime + Global.sServerTimeDiffLocal);
			}
		}
		if (mCountTimer != null) {
			mCountTimer.removeTextView(position);
		}
		if (data.seizeStatus != GiftTypeUtil.SEIZE_TYPE_NEVER) {
			contentHolder.tvTagSeize.setText("已抢号");
		} else {
			switch (data.status) {
				case GiftTypeUtil.STATUS_WAIT_SEIZE:
					contentHolder.tvTagSeize.setText(mDefaultTime);
					contentHolder.tvTagSeize.setBackgroundResource(R.drawable.ic_0_seize_btn_count);
					if (mCountTimer != null) {
						mCountTimer.addTextView(position, contentHolder.tvTagSeize);
					}
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

		private SparseArray<TextView> tvs;
		private boolean mIsInAddOrRemove = false;

		public ZeroCountTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			tvs = new SparseArray<>(6);
		}

		public synchronized void addTextView(int key, TextView tv) {
			mIsInAddOrRemove = true;
			if (tvs.get(key) == null)
				tvs.append(key, tv);
			mIsInAddOrRemove = false;
		}

		public synchronized void removeTextView(int key) {
			mIsInAddOrRemove = true;
			tvs.remove(key);
			mIsInAddOrRemove = false;
		}

		public void setText(String s) {
			for (int i = tvs.size() - 1; i >= 0; i--) {
				TextView t = tvs.valueAt(i);
				if (t == null) {
					tvs.removeAt(i);
					continue;
				}
				t.setText(s);
			}
		}

		@Override
		public void onTick(long millisUntilFinished) {
			if (mIsInAddOrRemove) {
				return;
			}
			String s = DateUtil.formatRemain(millisUntilFinished, "HH:mm:ss");
			setText(s);
		}

		@Override
		public void onFinish() {
			setText("刷新抢");
			ThreadUtil.runInUIThread(new Runnable() {
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
