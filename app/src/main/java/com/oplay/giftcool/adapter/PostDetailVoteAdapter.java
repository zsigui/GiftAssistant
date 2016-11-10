package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.model.data.resp.PostVoteInfo;
import com.oplay.giftcool.model.data.resp.PostVoteItem;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.dialog.ConfirmDialog;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.oplay.giftcool.util.ViewUtil;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-11-8.
 */

public class PostDetailVoteAdapter extends BaseRVAdapter<PostVoteItem> implements View.OnClickListener {

    final int TYPE_HEADER = 0x01;
    final int TYPE_CONTENT = 0x02;
    final int TYPE_CONTENT_VOTED = 0x03;
    final int TYPE_FOOTER = 0x04;

    private PostVoteInfo mInfo;
    private SparseIntArray mMultiCheckedPos;
    private View.OnClickListener mOnClickListener;

    public PostDetailVoteAdapter(Context context) {
        super(context);
    }

    public void setInfo(PostVoteInfo info) {
        mInfo = info;
        if (mInfo != null && mInfo.voteItems != null) {
            mMultiCheckedPos = new SparseIntArray(mInfo.type == 0 ? 1 : mInfo.voteItems.size());
        }
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                return new HeaderVH(inflateView(mContext, R.layout.item_post_detail_vote_top, parent));
            case TYPE_CONTENT:
                return new ContentVH(inflateView(mContext, R.layout.item_post_detail_vote_content, parent));
            case TYPE_CONTENT_VOTED:
                return new ContentVotedVH(inflateView(mContext, R.layout.item_post_detail_vote_content_voted, parent));
            case TYPE_FOOTER:
                return new FooterVH(inflateView(mContext, R.layout.item_post_detail_vote_bottom, parent));
        }
        return null;
    }

    public View inflateView(Context context, @LayoutRes int layoutId, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(layoutId, parent, false);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        switch (type) {
            case TYPE_HEADER:
                ((HeaderVH) holder).tvTitle.setText(String.format(Locale.CHINA, "%s (%s)", mInfo.title,
                        mInfo.type == 0 ? "单选" : "多选"));
                break;
            case TYPE_CONTENT:
                ContentVH contentVH = (ContentVH) holder;
                contentVH.ctvTitle.setText(getItem(position).name);
                contentVH.ctvTitle.setChecked(mMultiCheckedPos.indexOfKey(position) >= 0);
                contentVH.ctvTitle.setOnClickListener(this);
                contentVH.ctvTitle.setTag(TAG_POSITION, position);
                break;
            case TYPE_CONTENT_VOTED:
                ContentVotedVH contentVotedVH = (ContentVotedVH) holder;
                PostVoteItem voteItem = getItem(position);
                int pb = Math.round((float) voteItem.amount * 100 / mInfo.voteCount);
                contentVotedVH.pbPercent.setProgress(pb);
                contentVotedVH.tvVotePercent.setText(String.format(Locale.CHINA, "%d%%", pb));
                contentVotedVH.tvVoteCount.setText(String.format(Locale.CHINA, "%d票", voteItem.amount));
                if (voteItem.isVote) {
                    contentVotedVH.tvTitle.setText(
                            Html.fromHtml(String.format("%s <font color='#ffaa17'> (你投的选项) </font>", voteItem.name))
                    );
                } else {
                    contentVotedVH.tvTitle.setText(voteItem.name);
                }
                break;
            case TYPE_FOOTER:
                TextView tv = ((FooterVH) holder).btnVote;
                if (mInfo.isVote) {
                    tv.setEnabled(false);
                    tv.setText("你已投票");
                    tv.setTextColor(ViewUtil.getColor(mContext, R.color.co_btn_grey));
                    tv.setOnClickListener(null);
                } else {
                    tv.setEnabled(true);
                    tv.setTextColor(ViewUtil.getColor(mContext, R.color.co_btn_green));
                    tv.setText("投票");
                    tv.setTag(TAG_POSITION, position);
                    tv.setOnClickListener(this);
                }
                break;
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else if (position == getItemCount() - 1) {
            return TYPE_FOOTER;
        } else {
            return mInfo.isVote ? TYPE_CONTENT_VOTED : TYPE_CONTENT;
        }
    }

    @Override
    public int getHeaderCount() {
        return 1;
    }

    private int getFooterCount() {
        return 1;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + getFooterCount();
    }

    @Override
    public void release() {
        super.release();
        if (mPutVoteCall != null) {
            mPutVoteCall.cancel();
            mPutVoteCall = null;
        }
    }

    Call<JsonRespBase<PostVoteInfo>> mPutVoteCall;
    private long mLastClickTime = 0;

    @Override
    public void onClick(View v) {
        if (v.getTag(TAG_POSITION) == null) return;

        int pos = (int) v.getTag(TAG_POSITION);
        switch (v.getId()) {
            case R.id.tv_title:
                if (mInfo.type == 0) {
                    // 单选
                    if (mMultiCheckedPos.size() > 0) {
                        int oldChecked = mMultiCheckedPos.keyAt(0);
                        notifyItemChanged(oldChecked);
                        mMultiCheckedPos.clear();
                    }
                    mMultiCheckedPos.put(pos, pos);
                } else {
                    int index = mMultiCheckedPos.indexOfKey(pos);
                    if (index >= 0) {
                        mMultiCheckedPos.removeAt(index);
                    } else {
                        mMultiCheckedPos.put(pos, pos);
                    }
                }
                notifyItemChanged(pos);
                break;
            case R.id.btn_vote:
                long curTime = System.currentTimeMillis();
                if (curTime - mLastClickTime < Global.CLICK_TIME_INTERVAL) {
                    return;
                }
                mLastClickTime = curTime;
                if (mMultiCheckedPos.size() == 0) {
                    if (mContext != null && mContext instanceof FragmentActivity) {
                        ConfirmDialog dialog = ConfirmDialog.newInstance();
                        dialog.setContent(mContext.getString(R.string.st_post_vote_no_choice));
                        dialog.setNegativeVisibility(View.GONE);
                        dialog.setPositiveBtnText(mContext.getString(R.string.st_dialog_btn_ok));
                        dialog.show(((FragmentActivity) mContext).getSupportFragmentManager(), "vote");
                    } else {
                        ToastUtil.showShort("请先选择想投票的选项");
                    }
                    return;
                }
                if (!AccountManager.getInstance().isLogin()) {
                    IntentUtil.jumpLogin(mContext);
                    return;
                }
                mInfo.isVote = true;
                // 进行预更新
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mMultiCheckedPos.size(); i++) {
                    int p = mMultiCheckedPos.keyAt(i);
                    PostVoteItem item = getItem(p);
                    item.isVote = true;
                    item.amount++;
                    sb.append(item.id).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                mInfo.voteCount += mMultiCheckedPos.size();
                notifyDataSetChanged();
                // 进行网络请求
                mPutVoteCall = Global.getNoEncryptEngine().putVote(mInfo.id, sb.toString());
                mPutVoteCall.enqueue(new Callback<JsonRespBase<PostVoteInfo>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<PostVoteInfo>> call,
                                           Response<JsonRespBase<PostVoteInfo>> response) {
                        if (call.isCanceled())
                            return;
                        if (response != null && response.isSuccessful()
                                && response.body() != null && response.body().isSuccess()) {
                            PostVoteInfo info = response.body().getData();
                            setInfo(info);
                            setData(info.voteItems);
                            notifyDataSetChanged();
                            // 确认
                            if (mContext != null && mContext instanceof FragmentActivity) {
                                ConfirmDialog dialog = ConfirmDialog.newInstance();
                                dialog.setTitle(mContext.getString(R.string.st_post_vote_success_title));
                                dialog.setContent(mContext.getString(R.string.st_post_vote_success_content));
                                dialog.setNegativeVisibility(View.GONE);
                                dialog.setPositiveBtnText(mContext.getString(R.string.st_dialog_btn_ok));
                                dialog.show(((FragmentActivity) mContext).getSupportFragmentManager(), "success");
                            }
                            return;
                        }
                        ToastUtil.blurErrorResp(response);
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<PostVoteInfo>> call, Throwable t) {
                        if (call.isCanceled())
                            return;
                        ToastUtil.blurThrow(t);
                    }
                });
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(v);
                }
                break;
        }
    }


    static class HeaderVH extends BaseRVHolder {

        TextView tvTitle;

        public HeaderVH(View itemView) {
            super(itemView);
            tvTitle = getViewById(R.id.tv_title);
        }
    }

    static class ContentVH extends BaseRVHolder {

        CheckedTextView ctvTitle;

        public ContentVH(View itemView) {
            super(itemView);
            ctvTitle = getViewById(R.id.tv_title);
        }
    }

    static class ContentVotedVH extends BaseRVHolder {

        TextView tvTitle;
        TextView tvVoteCount;
        TextView tvVotePercent;
        ProgressBar pbPercent;

        public ContentVotedVH(View itemView) {
            super(itemView);
            tvTitle = getViewById(R.id.tv_title);
            tvVoteCount = getViewById(R.id.tv_vote_count);
            tvVotePercent = getViewById(R.id.tv_vote_percent);
            pbPercent = getViewById(R.id.pb_percent);
        }
    }

    static class FooterVH extends BaseRVHolder {

        TextView btnVote;

        public FooterVH(View itemView) {
            super(itemView);
            btnVote = getViewById(R.id.btn_vote);
        }
    }
}
