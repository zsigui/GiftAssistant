package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.adapter.base.FooterHolder;
import com.oplay.giftcool.config.util.PostTypeUtil;
import com.oplay.giftcool.listener.CallbackListener;
import com.oplay.giftcool.listener.FooterListener;
import com.oplay.giftcool.model.data.resp.IndexPostNew;
import com.oplay.giftcool.ui.widget.ToggleButton;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.oplay.giftcool.util.ViewUtil;

/**
 * 首页活动页面的适配器
 * <p/>
 * Created by zsigui on 16-4-5.
 */
public class PostAdapter extends BaseRVAdapter<IndexPostNew> implements View.OnClickListener, FooterListener {

	private final float HEADER_RIGHT_WH_RATE = 0.65f;
	/**
	 * 左右间隔的大小
	 */
	private final int GAP_SIZE;
	private final int SCREEN_WIDTH;

	// 重复使用的文字类型
	private final String TEXT_STATE_DOING;
	private final String TEXT_STATE_FINISHED;
	private final String TEXT_OFFICIAL;
	private final String TEXT_NOTIFY;
	private final String TEXT_READ_ATTENTION;

	private ToggleButton tbReadAttention;
	private LayoutInflater mInflater;
	private CallbackListener mCallbackListener;
	private boolean mHasFooter = false;

	public PostAdapter(Context context) {
		super(context);
		GAP_SIZE = context.getResources().getDimensionPixelSize(R.dimen.di_index_post_gap_vertical);
		SCREEN_WIDTH = context.getResources().getDisplayMetrics().widthPixels;
		TEXT_STATE_DOING = context.getResources().getString(R.string.st_index_post_text_working);
		TEXT_STATE_FINISHED = context.getResources().getString(R.string.st_index_post_text_finished);
		TEXT_OFFICIAL = context.getResources().getString(R.string.st_index_post_official);
		TEXT_NOTIFY = context.getResources().getString(R.string.st_index_post_notify);
		TEXT_READ_ATTENTION = context.getResources().getString(R.string.st_index_post_read_attention);

		mInflater = LayoutInflater.from(mContext);
	}

	public void setCallbackListener(CallbackListener callbackListener) {
		mCallbackListener = callbackListener;
	}

	protected int getItemFooterCount() {
		return mHasFooter ? 1 : 0;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
			case PostTypeUtil.TYPE_HEADER:
				final HeaderHolder headerHolder = new HeaderHolder(
						mInflater.inflate(R.layout.item_index_post_header, parent, false));
				initHeaderLayoutParams(headerHolder);
				return headerHolder;
			case PostTypeUtil.TYPE_FOOTER:
				return new FooterHolder(LayoutInflater.from(mContext).inflate(R.layout.view_item_footer, parent, false));
			case PostTypeUtil.TYPE_TITLE_OFFICIAL:
				final ItemTitleVH titleOneVH = new ItemTitleVH(
						mInflater.inflate(R.layout.view_index_item_title_1, parent, false));
				titleOneVH.tvTitle.setText(TEXT_OFFICIAL);
				titleOneVH.itemView.setOnClickListener(this);
				return titleOneVH;
			case PostTypeUtil.TYPE_TITLE_GAME:
				final ItemTitleVH titleTwoVH = new ItemTitleVH(
						mInflater.inflate(R.layout.view_index_item_title_2, parent, false));
				titleTwoVH.tvTitle.setText(TEXT_NOTIFY);
				titleTwoVH.tvNote.setText(TEXT_READ_ATTENTION);
				titleTwoVH.itemView.setOnClickListener(this);
				titleTwoVH.tbAttention.setOnClickListener(this);
				tbReadAttention = titleTwoVH.tbAttention;
				return titleTwoVH;
			case PostTypeUtil.TYPE_CONTENT_OFFICIAL:
				return new ContentOneHolder(LayoutInflater.from(mContext)
						.inflate(R.layout.item_index_post_content_one, parent, false));
			case PostTypeUtil.TYPE_CONTENT_GAME:
				return new ContentTwoHolder(LayoutInflater.from(mContext)
						.inflate(R.layout.item_index_post_content_two, parent, false));
		}
		return null;
	}

	/**
	 * 初始化标题头的配置
	 */
	private void initHeaderLayoutParams(HeaderHolder headerHolder) {
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
		final IndexPostNew item = getItem(position);
		switch (getItemViewType(position)) {
			case PostTypeUtil.TYPE_HEADER:
				HeaderHolder headerHolder = (HeaderHolder) holder;
				headerHolder.ivSignIn.setOnClickListener(this);
				headerHolder.ivLottery.setOnClickListener(this);
				headerHolder.ivTask.setOnClickListener(this);
				break;
			case PostTypeUtil.TYPE_FOOTER:
			case PostTypeUtil.TYPE_TITLE_OFFICIAL:
				// 无处理
				break;
			case PostTypeUtil.TYPE_TITLE_GAME:
				ItemTitleVH titleTwoVH = (ItemTitleVH) holder;
				if (AssistantApp.getInstance().isReadAttention()) {
					titleTwoVH.tbAttention.setToggleOn();
				} else {
					titleTwoVH.tbAttention.setToggleOff();
				}
				break;
			case PostTypeUtil.TYPE_CONTENT_OFFICIAL:
				setContentOneData((ContentOneHolder) holder, position, item);
				break;
			case PostTypeUtil.TYPE_CONTENT_GAME:
			default:
				setContentTwoData((ContentTwoHolder) holder, position, item);
				break;
		}
	}

	/**
	 * 设置类型二的内容
	 */
	private void setContentTwoData(final ContentTwoHolder holder, final int position, final IndexPostNew item) {
		ViewUtil.showImage(holder.ivIcon, item.img);
		holder.tvPubTime.setText(item.startTime);
		holder.tvTitle.setText(item.title);
		holder.tvContent.setText(item.content);
		holder.itemView.setOnClickListener(this);
		holder.itemView.setTag(TAG_POSITION, position);
	}

	/**
	 * 设置类型一的内容
	 */
	private void setContentOneData(final ContentOneHolder holder, final int position, final IndexPostNew item) {
		holder.tvTitle.setText(item.title);
		ViewUtil.showImage(holder.ivIcon, item.img);
//				if (item.isNew) {
//					holder.tvTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_task_new, 0, 0, 0);
//				} else {
//					holder.tvTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
//				}
		switch (item.state) {
			case 0:
				holder.tvState.setText(TEXT_STATE_FINISHED);
				holder.tvState.setBackgroundResource(R.drawable.ic_post_disabled);
				break;
			case 1:
				holder.tvState.setText(TEXT_STATE_DOING);
				holder.tvState.setBackgroundResource(R.drawable.ic_post_enabled);
				break;
		}
		holder.itemView.setOnClickListener(this);
		holder.itemView.setTag(TAG_POSITION, position);
		holder.tvContent.setText(item.content);
	}

	@Override
	public IndexPostNew getItem(int position) {
		return mHasFooter && position == getItemCount() - 1 ? null : super.getItem(position);
	}

	@Override
	public int getItemViewType(int position) {
		return mHasFooter && position == getItemCount() - 1 ? PostTypeUtil.TYPE_FOOTER: getItem(position).showType;
	}

	@Override
	public int getItemCount() {
		return mHasFooter ? super.getItemCount() + 1 : super.getItemCount();
	}

	@Override
	public void onClick(View v) {
		if (mContext == null) {
			ToastUtil.showShort("页面失效，请重新打开应用");
			return;
		}
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
			case R.id.tb_read_attention:
				// 只看我关注的游戏资讯
				final boolean isRead = !AssistantApp.getInstance().isReadAttention();
				if (tbReadAttention != null) {
					if (isRead) {
						tbReadAttention.toggleOn();
					} else {
						tbReadAttention.toggleOff();
					}
				}
				AssistantApp.getInstance().setIsReadAttention(isRead);
				if (mCallbackListener != null) {
					mCallbackListener.doCallBack(isRead);
				}
				break;
			case R.id.rl_header_item:
				// 跳转官方活动列表页面
				IntentUtil.jumpPostOfficialList(mContext);
				break;
			case R.id.rl_item:
				// 内容项被点击
				if (v.getTag(TAG_POSITION) == null) {
					return;
				}
				final IndexPostNew item = getItem((Integer) v.getTag(TAG_POSITION));
				IntentUtil.jumpPostDetail(mContext, item.id);
				break;
		}
	}

	@Override
	public void release() {
		super.release();
		mInflater = null;
	}

	@Override
	public void showFooter(boolean isShow) {
		mHasFooter = isShow;
		if (mHasFooter) {
			notifyItemInserted(getItemCount() - 1);
		} else {
			notifyItemRemoved(getItemCount());
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

	private static class ItemTitleVH extends BaseRVHolder {

		private TextView tvTitle;
		private TextView tvNote;
		private ToggleButton tbAttention;

		public ItemTitleVH(View itemView) {
			super(itemView);
			tvTitle = getViewById(R.id.tv_title);
			tbAttention = getViewById(R.id.tb_read_attention);
			tvNote = getViewById(R.id.tv_note);
		}
	}

	private static class ContentOneHolder extends BaseRVHolder {

		ImageView ivIcon;
		TextView tvState;
		TextView tvTitle;
		TextView tvContent;

		public ContentOneHolder(View itemView) {
			super(itemView);
			ivIcon = getViewById(R.id.iv_icon);
			tvState = getViewById(R.id.tv_post_state);
			tvTitle = getViewById(R.id.tv_title);
			tvContent = getViewById(R.id.tv_content);
		}
	}

	private static class ContentTwoHolder extends BaseRVHolder {

		ImageView ivIcon;
		TextView tvContent;
		TextView tvTitle;
		TextView tvPubTime;

		public ContentTwoHolder(View itemView) {
			super(itemView);
			ivIcon = getViewById(R.id.iv_icon);
			tvTitle = getViewById(R.id.tv_title);
			tvContent = getViewById(R.id.tv_content);
			tvPubTime = getViewById(R.id.tv_pub_time);
		}
	}


}
