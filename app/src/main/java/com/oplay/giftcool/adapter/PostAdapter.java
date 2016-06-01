package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.adapter.base.FooterHolder;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.TypeStatusCode;
import com.oplay.giftcool.config.util.PostTypeUtil;
import com.oplay.giftcool.listener.CallbackListener;
import com.oplay.giftcool.listener.FooterListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.manager.StatisticsManager;
import com.oplay.giftcool.model.data.resp.IndexPostNew;
import com.oplay.giftcool.ui.widget.ToggleButton;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.oplay.giftcool.util.ViewUtil;
import com.socks.library.KLog;

/**
 * 首页活动页面的适配器
 * <p/>
 * Created by zsigui on 16-4-5.
 */
public class PostAdapter extends BaseRVAdapter<IndexPostNew> implements View.OnClickListener, FooterListener {

    private final float HEADER_RIGHT_WH_RATE = 0.62f;
    /**
     * 左右间隔的大小
     */
    private final int GAP_SIZE;
    private final int SCREEN_WIDTH;

    // 重复使用的文字类型
    private final String TEXT_STATE_DOING;
    private final String TEXT_STATE_FINISHED;
    private final String TEXT_STATE_WAIT;
    private final String TEXT_OFFICIAL;
    private final String TEXT_NOTIFY;
    private final String TEXT_READ_ATTENTION;

    private ToggleButton tbReadAttention;
    private LayoutInflater mInflater;
    private CallbackListener<Boolean> mCallbackListener;
    private boolean mHasFooter = false;

    public PostAdapter(Context context) {
        super(context);
        GAP_SIZE = context.getResources().getDimensionPixelSize(R.dimen.di_index_post_gap_vertical);
        SCREEN_WIDTH = context.getResources().getDisplayMetrics().widthPixels;
        TEXT_STATE_DOING = context.getResources().getString(R.string.st_index_post_text_working);
        TEXT_STATE_FINISHED = context.getResources().getString(R.string.st_index_post_text_finished);
        TEXT_STATE_WAIT = context.getResources().getString(R.string.st_index_post_text_wait);
        TEXT_OFFICIAL = context.getResources().getString(R.string.st_index_post_official);
        TEXT_NOTIFY = context.getResources().getString(R.string.st_index_post_notify);
        TEXT_READ_ATTENTION = context.getResources().getString(R.string.st_index_post_read_attention);

        mInflater = LayoutInflater.from(mContext);
    }

    public void setCallbackListener(CallbackListener<Boolean> callbackListener) {
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
                return new FooterHolder(LayoutInflater.from(mContext).inflate(R.layout.view_item_footer, parent,
                        false));
            case PostTypeUtil.TYPE_TITLE_OFFICIAL:
                final ItemTitleVH titleOneVH = new ItemTitleVH(
                        mInflater.inflate(R.layout.view_index_item_title_1, parent, false));
                titleOneVH.tvTitle.setText(TEXT_OFFICIAL);
                return titleOneVH;
            case PostTypeUtil.TYPE_TITLE_GAME:
                final ItemTitleVH titleTwoVH = new ItemTitleVH(
                        mInflater.inflate(R.layout.view_index_item_title_2, parent, false));
                titleTwoVH.tvTitle.setText(TEXT_NOTIFY);
                titleTwoVH.tvNote.setText(TEXT_READ_ATTENTION);
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
        LinearLayout.LayoutParams lpSignIn = (LinearLayout.LayoutParams) headerHolder.flSignIn.getLayoutParams();
        lpSignIn.width = width;
        lpSignIn.height = height;
        lpSignIn.rightMargin = GAP_SIZE;
        headerHolder.flSignIn.setLayoutParams(lpSignIn);
        LinearLayout.LayoutParams lpLottery = (LinearLayout.LayoutParams) headerHolder.flLottery.getLayoutParams();
        lpLottery.width = width;
        lpLottery.height = height;
        lpLottery.rightMargin = GAP_SIZE;
        headerHolder.flLottery.setLayoutParams(lpLottery);
        LinearLayout.LayoutParams lpTask = (LinearLayout.LayoutParams) headerHolder.flTask.getLayoutParams();
        lpTask.width = width;
        lpTask.height = height;
        headerHolder.flTask.setLayoutParams(lpTask);
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
                KLog.d(AppDebugConfig.TAG_WARN, "onBindViewHolder update Header");
                if (AccountManager.getInstance().isLogin()
                        && (ScoreManager.getInstance().isSignInTaskFinished() || Global.sHasShowedSignInHint)) {
                    headerHolder.ivSignInHint.setVisibility(View.GONE);
                } else {
                    headerHolder.ivSignInHint.setVisibility(View.VISIBLE);
                }
                break;
            case PostTypeUtil.TYPE_FOOTER:
                break;
            case PostTypeUtil.TYPE_TITLE_OFFICIAL:
                ItemTitleVH titleOneVH = (ItemTitleVH) holder;
                titleOneVH.rlItem.setOnClickListener(this);
                // 无处理
                break;
            case PostTypeUtil.TYPE_TITLE_GAME:
                ItemTitleVH titleTwoVH = (ItemTitleVH) holder;
                titleTwoVH.tbAttention.setOnClickListener(this);
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
        if (item.showType == 1) {
            ViewUtil.showImage(holder.ivIcon, item.banner);
        } else {
            ViewUtil.showImage(holder.ivIcon, item.img);
        }
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
        if (item.showType == 1) {
            ViewUtil.showImage(holder.ivIcon, item.banner);
        } else {
            ViewUtil.showImage(holder.ivIcon, item.img);
        }
        switch (item.state) {
            case TypeStatusCode.POST_FINISHED:
                holder.tvState.setText(TEXT_STATE_FINISHED);
                holder.tvState.setBackgroundResource(R.drawable.ic_post_disabled);
                break;
            case TypeStatusCode.POST_WAIT:
                holder.tvState.setText(TEXT_STATE_WAIT);
                holder.tvState.setBackgroundResource(R.drawable.ic_post_disabled);
                break;
            case TypeStatusCode.POST_BEING:
                holder.tvState.setText(TEXT_STATE_DOING);
                holder.tvState.setBackgroundResource(R.drawable.ic_post_enabled);
                break;
        }
        holder.itemView.setOnClickListener(this);
        holder.itemView.setTag(TAG_POSITION, position);
        holder.tvContent.setText(Html.fromHtml(item.content));
    }

    @Override
    public IndexPostNew getItem(int position) {
        return mHasFooter && position == getItemCount() - 1 ? null : super.getItem(position);
    }

    @Override
    public int getItemViewType(int position) {
        return mHasFooter && position == getItemCount() - 1 ? PostTypeUtil.TYPE_FOOTER : getItem(position).showType;
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
                if (AccountManager.getInstance().isLogin()) {
                    Global.sHasShowedSignInHint = true;
                    notifyItemChanged(0);
                }
                StatisticsManager.getInstance().trace(mContext,
                        StatisticsManager.ID.SIGN_IN_FROM_ACTIVITY,
                        StatisticsManager.ID.STR_SIGN_IN_FROM_ACTIVITY);
                break;
            case R.id.iv_lottery_everyday:
                // 跳转每日抽奖页面
                IntentUtil.jumpLottery(mContext);
                if (AccountManager.getInstance().isLogin()) {
                    Global.sHasShowedLotteryHint = true;
                    notifyItemChanged(0);
                }
                StatisticsManager.getInstance().trace(mContext,
                        StatisticsManager.ID.LOTTERY_FROM_ACTIVITY,
                        StatisticsManager.ID.STR_LOTTERY_FROM_ACTIVITY);
                break;
            case R.id.iv_task_everyday:
                // 跳转每日任务列表页面
                IntentUtil.jumpEarnScore(mContext);
                StatisticsManager.getInstance().trace(mContext,
                        StatisticsManager.ID.TASK_FROM_ACTIVITY,
                        StatisticsManager.ID.STR_TASK_FROM_ACTIVITY);
                break;
            case R.id.tb_read_attention:
                // 只看我关注的游戏资讯
                toggleButton(true, true);
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

    public void toggleButton(boolean toggleState, boolean needCallBack) {
        boolean isRead = toggleState && !AssistantApp.getInstance().isReadAttention();
        if (tbReadAttention != null) {
            if (isRead) {
                tbReadAttention.toggleOn();
            } else {
                tbReadAttention.toggleOff();
            }
        }
        AssistantApp.getInstance().setIsReadAttention(isRead);
        if (needCallBack && mCallbackListener != null) {
            mCallbackListener.doCallBack(isRead);
        }
    }

    private static class HeaderHolder extends BaseRVHolder {

        FrameLayout flSignIn;
        FrameLayout flLottery;
        FrameLayout flTask;
        ImageView ivSignIn;
        ImageView ivLottery;
        ImageView ivTask;
        ImageView ivSignInHint;
        ImageView ivLotteryHint;
        ImageView ivTaskHint;

        public HeaderHolder(View itemView) {
            super(itemView);
            flSignIn = getViewById(R.id.fl_sign_in);
            flLottery = getViewById(R.id.fl_lottery);
            flTask = getViewById(R.id.fl_task);
            ivSignIn = getViewById(R.id.iv_sign_in_everyday);
            ivLottery = getViewById(R.id.iv_lottery_everyday);
            ivTask = getViewById(R.id.iv_task_everyday);
            ivSignInHint = getViewById(R.id.iv_sign_in_hint);
            ivLotteryHint = getViewById(R.id.iv_lottery_hint);
            ivTaskHint = getViewById(R.id.iv_task_hint);
        }
    }

    private static class ItemTitleVH extends BaseRVHolder {

        private TextView tvTitle;
        private TextView tvNote;
        private RelativeLayout rlItem;
        private ToggleButton tbAttention;

        public ItemTitleVH(View itemView) {
            super(itemView);
            tvTitle = getViewById(R.id.tv_title);
            tbAttention = getViewById(R.id.tb_read_attention);
            tvNote = getViewById(R.id.tv_note);
            rlItem = getViewById(R.id.rl_header_item);
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
