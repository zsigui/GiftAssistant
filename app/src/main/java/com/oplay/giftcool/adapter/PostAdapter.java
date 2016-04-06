package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.config.IndexTypeUtil;
import com.oplay.giftcool.model.data.resp.IndexPostNew;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ViewUtil;

/**
 * 首页活动页面的适配器
 * <p/>
 * Created by zsigui on 16-4-5.
 */
public class PostAdapter extends BaseRVAdapter<IndexPostNew> implements View.OnClickListener {

	private final float HEADER_RIGHT_WH_RATE = 2.2f;
	/**
	 * 左右间隔的大小
	 */
	private final int GAP_SIZE;
	private final int SCREEN_WIDTH;

	private final String TEXT_STATE_DOING;
	private final String TEXT_STATE_FINISHED;

	public PostAdapter(Context context) {
		super(context);
		GAP_SIZE = context.getResources().getDimensionPixelSize(R.dimen.di_index_post_gap_vertical);
		SCREEN_WIDTH = context.getResources().getDisplayMetrics().widthPixels;
		TEXT_STATE_DOING = context.getResources().getString(R.string.st_index_post_text_working);
		TEXT_STATE_FINISHED = context.getResources().getString(R.string.st_index_post_text_finished);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
			case IndexTypeUtil.ITEM_HEADER:
				HeaderHolder headerHolder = new HeaderHolder(LayoutInflater.from(mContext)
						.inflate(R.layout.item_index_post_header, parent, false));
				initHeaderLayoutParams(headerHolder);
				return headerHolder;
			case IndexTypeUtil.ITEM_NORMAL:
				return new ContentHolder(LayoutInflater.from(mContext)
						.inflate(R.layout.item_index_post_content, parent, false));
		}
		return null;
	}

	/**
	 * 初始化标题头的配置
	 */
	private void initHeaderLayoutParams(HeaderHolder headerHolder) {
		final int width = (SCREEN_WIDTH - 3 * GAP_SIZE) / 2;
		final int height = width;
		final int rightHeight = (int) (width / HEADER_RIGHT_WH_RATE);
		final int topGap = width - 2 * rightHeight;
		RelativeLayout.LayoutParams lpSignIn = (RelativeLayout.LayoutParams) headerHolder.ivSignIn.getLayoutParams();
		lpSignIn.width = width;
		lpSignIn.height = height;
//				lpSignIn.bottomMargin = lpSignIn.topMargin = lpSignIn.leftMargin = 0;
		lpSignIn.rightMargin = GAP_SIZE;
		headerHolder.ivSignIn.setLayoutParams(lpSignIn);
		RelativeLayout.LayoutParams lpLottery = (RelativeLayout.LayoutParams) headerHolder.ivLottery.getLayoutParams();
		lpLottery.width = width;
		lpLottery.height = rightHeight;
//				lpLottery.leftMargin = lpLottery.rightMargin = lpLottery.topMargin = lpLottery.bottomMargin = 0;
		headerHolder.ivLottery.setLayoutParams(lpLottery);
		RelativeLayout.LayoutParams lpTask = (RelativeLayout.LayoutParams) headerHolder.ivTask.getLayoutParams();
		lpTask.width = width;
		lpTask.height = rightHeight;
		lpTask.topMargin = topGap;
		headerHolder.ivTask.setLayoutParams(lpTask);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		switch (getItemViewType(position)) {
			case IndexTypeUtil.ITEM_HEADER:
				HeaderHolder headerHolder = (HeaderHolder) holder;
				headerHolder.ivSignIn.setOnClickListener(this);
				headerHolder.ivLottery.setOnClickListener(this);
				headerHolder.ivTask.setOnClickListener(this);
				break;
			case IndexTypeUtil.ITEM_NORMAL:
			default:
				IndexPostNew item = getItem(position);
				ContentHolder contentHolder = (ContentHolder) holder;
				contentHolder.tvTitle.setText(item.title);
				ViewUtil.showImage(contentHolder.ivBanner, item.img);
				if (item.isNew) {
					contentHolder.tvTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_task_new, 0, 0, 0);
				} else {
					contentHolder.tvTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
				}
				switch (item.state) {
					case 0:
						contentHolder.tvState.setText(TEXT_STATE_FINISHED);
						contentHolder.tvState.setBackgroundResource(R.drawable.ic_post_disabled);
						break;
					case 1:
						contentHolder.tvState.setText(TEXT_STATE_DOING);
						contentHolder.tvState.setBackgroundResource(R.drawable.ic_post_enabled);
						break;
				}
				contentHolder.itemView.setOnClickListener(this);
				contentHolder.itemView.setTag(TAG_POSITION, position);
				break;
		}
	}

	private int getHeaderCount() {
		return 1;
	}

	@Override
	public int getItemCount() {
		return super.getItemCount() + getHeaderCount();
	}

	@Override
	public IndexPostNew getItem(int position) {
		return super.getItem(position - 1);
	}

	@Override
	public int getItemViewType(int position) {
		return position < getHeaderCount() ? IndexTypeUtil.ITEM_HEADER : IndexTypeUtil.ITEM_NORMAL;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.iv_sign_in_everyday:
				// 跳转签到页面
				IntentUtil.jumpSignIn(mContext);
				break;
			case R.id.iv_lottery_everyday:
				// 跳转每日抽奖页面
				IntentUtil.jumpLottery(mContext);
				break;
			case R.id.iv_task_everyday:
				// 跳转每日任务列表页面
				IntentUtil.jumpEarnScore(mContext);
				break;
			case R.id.rl_item:
				if (v.getTag(TAG_POSITION) == null) {
					return;
				}
				final IndexPostNew item = getItem((Integer) v.getTag(TAG_POSITION));
				break;
		}
	}

	@Override
	public void release() {
		super.release();
	}

	private static class ContentHolder extends BaseRVHolder {

		ImageView ivBanner;
		TextView tvState;
		TextView tvTitle;

		public ContentHolder(View itemView) {
			super(itemView);
			ivBanner = getViewById(R.id.iv_icon);
			tvState = getViewById(R.id.tv_post_state);
			tvTitle = getViewById(R.id.tv_title);
		}
	}

	private static class HeaderHolder extends BaseRVHolder {

		ImageView ivSignIn;
		ImageView ivLottery;
		ImageView ivTask;

		public HeaderHolder(View itemView) {
			super(itemView);
			ivSignIn = getViewById(R.id.iv_sign_in_everyday);
			ivLottery = getViewById(R.id.iv_lottery_everyday);
			ivTask = getViewById(R.id.iv_task_everyday);
		}
	}
}
