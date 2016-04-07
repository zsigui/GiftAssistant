package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.IndexTypeUtil;
import com.oplay.giftcool.model.data.resp.IndexPostNew;
import com.oplay.giftcool.util.IntentUtil;
import com.socks.library.KLog;

/**
 * 首页活动页面的适配器
 * <p/>
 * Created by zsigui on 16-4-5.
 */
public class PostAdapter extends BaseRVAdapter<IndexPostNew> implements View.OnClickListener {

	private final float HEADER_RIGHT_WH_RATE = 0.65f;
	/**
	 * 左右间隔的大小
	 */
	private final int GAP_SIZE;
	private final int SCREEN_WIDTH;



	public PostAdapter(Context context) {
		super(context);
		GAP_SIZE = context.getResources().getDimensionPixelSize(R.dimen.di_index_post_gap_vertical);
		SCREEN_WIDTH = context.getResources().getDisplayMetrics().widthPixels;

	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
			case IndexTypeUtil.ITEM_POST_HEADER:
				HeaderHolder headerHolder = new HeaderHolder(LayoutInflater.from(mContext)
						.inflate(R.layout.item_index_post_header, parent, false));
				initHeaderLayoutParams(headerHolder);
				return headerHolder;
			case IndexTypeUtil.ITEM_POST_OFFICIAL:
				return new ContentHolder(LayoutInflater.from(mContext)
						.inflate(R.layout.view_gift_index_like, parent, false));
			case IndexTypeUtil.ITEM_POST_NOTIFY:
				break;
		}
		return null;
	}

	/**
	 * 初始化标题头的配置
	 */
	private void initHeaderLayoutParams(HeaderHolder headerHolder) {
		KLog.d(AppDebugConfig.TAG_WARN, "screen = " + SCREEN_WIDTH + ", gap = " + GAP_SIZE + ", padding = " + headerHolder.itemView.getPaddingLeft());
		final int width = (SCREEN_WIDTH - 2 * GAP_SIZE - 2 * headerHolder.itemView.getPaddingLeft()) / 3;
		final int height = (int) (width * HEADER_RIGHT_WH_RATE);
		LinearLayout.LayoutParams lpSignIn = (LinearLayout.LayoutParams) headerHolder.ivSignIn.getLayoutParams();
		lpSignIn.width = width;
		lpSignIn.height = height;
		lpSignIn.rightMargin = GAP_SIZE;
		headerHolder.ivSignIn.setLayoutParams(lpSignIn);
		LinearLayout.LayoutParams lpLottery = (LinearLayout.LayoutParams) headerHolder.ivLottery.getLayoutParams();
		lpLottery.width = width;
		lpLottery.height = height;
		lpLottery.rightMargin = GAP_SIZE;
		headerHolder.ivLottery.setLayoutParams(lpLottery);
		LinearLayout.LayoutParams lpTask = (LinearLayout.LayoutParams) headerHolder.ivTask.getLayoutParams();
		lpTask.width = width;
		lpTask.height = height;
		headerHolder.ivTask.setLayoutParams(lpTask);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		switch (getItemViewType(position)) {
			case IndexTypeUtil.ITEM_POST_HEADER:
				HeaderHolder headerHolder = (HeaderHolder) holder;
				headerHolder.ivSignIn.setOnClickListener(this);
				headerHolder.ivLottery.setOnClickListener(this);
				headerHolder.ivTask.setOnClickListener(this);
				break;
			case IndexTypeUtil.ITEM_POST_OFFICIAL:
				break;
			case IndexTypeUtil.ITEM_POST_NOTIFY:
			default:
				break;
		}
	}

	private int getHeaderCount() {
		return IndexTypeUtil.ITEM_POST_HEADER_COUNT;
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

	private static class OfficialHolder extends BaseRVHolder {
		RelativeLayout rlTitle;
		RecyclerView rvContainer;
		PostOfficialAdapter rvAdapter;

		public OfficialHolder(View itemView) {
			super(itemView);
			rlTitle = getViewById(R.id.rl_like_all);
			rvContainer = getViewById(R.id.rv_like_content);
		}
	}

	private static class ItemTitleVH extends BaseRVHolder {

		public ItemTitleVH(View itemView) {
			super(itemView);
		}
	}

	private static class ContentHolder extends BaseRVHolder {

		ImageView ivBanner;
		TextView tvContent;
		TextView tvTitle;

		public ContentHolder(View itemView) {
			super(itemView);
			ivBanner = getViewById(R.id.iv_icon);
			tvTitle = getViewById(R.id.tv_title);
			tvContent = getViewById(R.id.tv_content);
		}
	}
}
