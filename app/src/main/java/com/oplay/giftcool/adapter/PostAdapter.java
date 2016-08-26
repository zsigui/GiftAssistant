package com.oplay.giftcool.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.adapter.base.FooterHolder;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.TypeStatusCode;
import com.oplay.giftcool.config.util.PostTypeUtil;
import com.oplay.giftcool.listener.FooterListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.manager.StatisticsManager;
import com.oplay.giftcool.model.data.resp.IndexPostNew;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.oplay.giftcool.util.ViewUtil;

/**
 * 首页活动页面的适配器
 * <p/>
 * Created by zsigui on 16-4-5.
 */
public class PostAdapter extends BaseRVAdapter<IndexPostNew> implements View.OnClickListener, FooterListener {

    private final int TAG_TYPE = 0x3333FF11;
    private final int TYPE_SIGN_IN = 0;
    private final int TYPE_LOTTERY = 1;
    private final int TYPE_SERVER_INFO = 2;
    private final int TYPE_TASK = 3;

    // 重复使用的文字类型
    private final String TEXT_STATE_DOING;
    private final String TEXT_STATE_FINISHED;
    private final String TEXT_STATE_WAIT;

    private LayoutInflater mInflater;
    private boolean mHasFooter = false;

    public PostAdapter(Context context) {
        super(context);
        TEXT_STATE_DOING = context.getResources().getString(R.string.st_index_post_text_working);
        TEXT_STATE_FINISHED = context.getResources().getString(R.string.st_index_post_text_finished);
        TEXT_STATE_WAIT = context.getResources().getString(R.string.st_index_post_text_wait);

        mInflater = LayoutInflater.from(mContext);
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case PostTypeUtil.TYPE_HEADER:
                return new HeaderHolder(
                        mInflater.inflate(R.layout.item_index_post_header, parent, false));
            case PostTypeUtil.TYPE_FOOTER:
                return new FooterHolder(LayoutInflater.from(mContext).inflate(R.layout.view_item_footer, parent,
                        false));
            case PostTypeUtil.TYPE_CONTENT_OFFICIAL:
                return new ContentOfficialHolder(LayoutInflater.from(mContext)
                        .inflate(R.layout.item_index_post_offical_content, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final IndexPostNew item = getItem(position);
        switch (getItemViewType(position)) {
            case PostTypeUtil.TYPE_HEADER:
                HeaderHolder headerHolder = (HeaderHolder) holder;
                setHeaderLayout(position, headerHolder);
                bindHeaderInfo(position, headerHolder);
                break;
            case PostTypeUtil.TYPE_FOOTER:
                break;
            case PostTypeUtil.TYPE_CONTENT_OFFICIAL:
                setContentOneData((ContentOfficialHolder) holder, position, item);
                break;
        }
    }

    private void setHeaderLayout(int position, HeaderHolder holder) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(mContext.getResources(), R.drawable.pic_sign_in_everyday, options);
        float rate = (float) options.outHeight / options.outWidth;
//        final int width = options.outWidth;
        final int SCREEN_WIDTH = mContext.getResources().getDisplayMetrics().widthPixels;
        final int RIGHT_PADDING = mContext.getResources().getDimensionPixelSize(R.dimen.di_list_item_gap_very_small);
        final int TOP_PADDING = mContext.getResources().getDimensionPixelSize(R.dimen.di_list_item_gap_very_small);
        final int LEFT_SIZE = mContext.getResources().getDimensionPixelSize(R.dimen.di_list_item_gap_s_normal);
        final int TOP_SIZE = mContext.getResources().getDimensionPixelSize(R.dimen.di_list_item_gap_s_normal);
        final int GAP_SIZE = mContext.getResources().getDimensionPixelSize(R.dimen.di_line_space_extra_normal);
        // (pic_width + right_padding) * 2 + left_size + (left_size - right_padding) + gap_size = screen_width
        // height = pic_width * rate + top_padding
        final int width = (SCREEN_WIDTH - LEFT_SIZE * 2  + RIGHT_PADDING - GAP_SIZE) / 2;
        final int height = (int) ((width  - RIGHT_PADDING) * rate) + TOP_PADDING;
        LinearLayout.LayoutParams lpOne = (LinearLayout.LayoutParams) holder.flTapOne.getLayoutParams();
        LinearLayout.LayoutParams lpTwo = (LinearLayout.LayoutParams) holder.flTapTwo.getLayoutParams();
        lpOne.width = width;
        lpOne.height = height;
        lpTwo.width = width;
        lpTwo.height = height;
        lpOne.leftMargin = LEFT_SIZE;
        lpTwo.rightMargin = LEFT_SIZE - RIGHT_PADDING;
        lpOne.rightMargin = GAP_SIZE;
        lpTwo.leftMargin = 0;
        if (position == 0) {
            lpOne.topMargin = lpTwo.topMargin = TOP_SIZE - TOP_PADDING;
            lpOne.bottomMargin = lpTwo.bottomMargin = 0;
        } else {
            lpOne.topMargin = lpTwo.topMargin = GAP_SIZE;
            // 此处正常是 BOTTOM_SHADOW，然后由于图片阴影对比没下面明显，显示看起来会宽点，所以减少一点
            lpOne.bottomMargin = lpTwo.bottomMargin = TOP_SIZE;
        }
        holder.flTapOne.setLayoutParams(lpOne);
        holder.flTapTwo.setLayoutParams(lpTwo);
    }

    private void bindHeaderInfo(int position, HeaderHolder headerHolder) {
        if (position == 0) {
            headerHolder.ivTapOne.setImageResource(R.drawable.pic_sign_in_everyday);
            headerHolder.ivTapOne.setOnClickListener(this);
            headerHolder.ivTapOne.setTag(TAG_TYPE, TYPE_SIGN_IN);
            headerHolder.ivTapTwo.setImageResource(R.drawable.pic_lottery_everyday);
            headerHolder.ivTapTwo.setOnClickListener(this);
            headerHolder.ivTapTwo.setTag(TAG_TYPE, TYPE_LOTTERY);
            if (AccountManager.getInstance().isLogin()
                    && (ScoreManager.getInstance().isSignInTaskFinished() || Global.sHasShowedSignInHint)) {
                headerHolder.ivTapOneHint.setVisibility(View.GONE);
            } else {
                headerHolder.ivTapOneHint.setVisibility(View.VISIBLE);
            }
            if (AccountManager.getInstance().isLogin()
                    && (ScoreManager.getInstance().isFreeLotteryEmpty() || Global.sHasShowedLotteryHint)) {
                headerHolder.ivTapTwoHint.setVisibility(View.GONE);
            } else {
                headerHolder.ivTapTwoHint.setVisibility(View.VISIBLE);
            }
        } else {
            headerHolder.ivTapOne.setImageResource(R.drawable.pic_server_info);
            headerHolder.ivTapOne.setOnClickListener(this);
            headerHolder.ivTapOne.setTag(TAG_TYPE, TYPE_SERVER_INFO);
            headerHolder.ivTapTwo.setImageResource(R.drawable.pic_task_everyday);
            headerHolder.ivTapTwo.setOnClickListener(this);
            headerHolder.ivTapTwo.setTag(TAG_TYPE, TYPE_TASK);
            headerHolder.ivTapOneHint.setVisibility(View.GONE);
            headerHolder.ivTapTwoHint.setVisibility(View.GONE);
        }
    }

    /**
     * 设置类型一的内容
     */
    private void setContentOneData(final ContentOfficialHolder holder, final int position, final IndexPostNew item) {
        holder.tvTitle.setText(item.title);
        ViewUtil.showImage(holder.ivBanner, item.banner);
        switch (item.state) {
            case TypeStatusCode.POST_FINISHED:
                holder.tvState.setText(TEXT_STATE_FINISHED);
                holder.tvState.setBackgroundResource(R.drawable.ic_post_disabled);
                break;
            case TypeStatusCode.POST_WAIT:
                holder.tvState.setText(TEXT_STATE_WAIT);
                holder.tvState.setBackgroundResource(R.drawable.ic_post_wait);
                break;
            case TypeStatusCode.POST_BEING:
                holder.tvState.setText(TEXT_STATE_DOING);
                holder.tvState.setBackgroundResource(R.drawable.ic_post_enabled);
                break;
        }
        holder.itemView.setOnClickListener(this);
        holder.itemView.setTag(TAG_POSITION, position);
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


    private void handleHeaderClick(int type) {
        switch (type) {
            case TYPE_SIGN_IN:
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
            case TYPE_LOTTERY:
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
            case TYPE_SERVER_INFO:
                IntentUtil.jumpServerInfo(mContext);
                StatisticsManager.getInstance().trace(mContext,
                        StatisticsManager.ID.SERVER_INFO_FROM_ACTIVITY,
                        StatisticsManager.ID.STR_SERVER_INFO_FROM_ACTIVITY);
                break;
            case TYPE_TASK:
                // 跳转每日任务列表页面
                IntentUtil.jumpEarnScore(mContext);
                StatisticsManager.getInstance().trace(mContext,
                        StatisticsManager.ID.TASK_FROM_ACTIVITY,
                        StatisticsManager.ID.STR_TASK_FROM_ACTIVITY);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (mContext == null) {
            ToastUtil.showShort(ConstString.TOAST_COPY_CODE);
            return;
        }
        switch (v.getId()) {
            case R.id.iv_tap_one:
                handleHeaderClick((Integer) v.getTag(TAG_TYPE));
                break;
            case R.id.iv_tap_two:
                handleHeaderClick((Integer) v.getTag(TAG_TYPE));
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

        ImageView ivTapOne;
        ImageView ivTapTwo;
        ImageView ivTapOneHint;
        ImageView ivTapTwoHint;
        FrameLayout flTapOne;
        FrameLayout flTapTwo;

        public HeaderHolder(View itemView) {
            super(itemView);
            ivTapOne = getViewById(R.id.iv_tap_one);
            ivTapTwo = getViewById(R.id.iv_tap_two);
            ivTapOneHint = getViewById(R.id.iv_tap_one_hint);
            ivTapTwoHint = getViewById(R.id.iv_tap_two_hint);
            flTapOne = getViewById(R.id.fl_tap_one);
            flTapTwo = getViewById(R.id.fl_tap_two);
        }
    }

    private static class ContentOfficialHolder extends BaseRVHolder {

        ImageView ivBanner;
        TextView tvState;
        TextView tvTitle;

        public ContentOfficialHolder(View itemView) {
            super(itemView);
            ivBanner = getViewById(R.id.iv_banner);
            tvState = getViewById(R.id.tv_post_state);
            tvTitle = getViewById(R.id.tv_title);
        }
    }

}
