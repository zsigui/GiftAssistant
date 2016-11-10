package com.oplay.giftcool.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.databinding.ItemPostDetailCommentNoPicBinding;
import com.oplay.giftcool.databinding.ItemPostDetailCommentOnePicBinding;
import com.oplay.giftcool.databinding.ItemPostDetailCommentSixPicBinding;
import com.oplay.giftcool.databinding.ItemPostDetailCommentThreePicBinding;
import com.oplay.giftcool.databinding.ItemPostDetailContentBinding;
import com.oplay.giftcool.model.data.resp.CommentDetail;
import com.oplay.giftcool.model.data.resp.CommentDetailInfo;
import com.oplay.giftcool.model.data.resp.CommentDetailListItem;
import com.oplay.giftcool.model.data.resp.PostDetail;
import com.oplay.giftcool.model.data.resp.PostDetailInfo;
import com.oplay.giftcool.util.ViewUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Locale;

import static java.lang.String.format;

/**
 * Created by zsigui on 16-11-3.
 */

public class PostDetailAdapter extends BaseRVAdapter<CommentDetail> implements View.OnClickListener, View
        .OnTouchListener {

    static final int COUNT_SUB_COMMENT = 3;

    static final int TYPE_HEADER = 0x1;
    static final int TYPE_CMT_NO_PIC = 0x11;
    static final int TYPE_CMT_ONE_PIC = 0x21;
    static final int TYPE_CMT_THREE_PIC = 0x31;
    static final int TYPE_CMT_SIX_PIC = 0x41;

    private PostDetail mPostData;
    private HeaderHolder mHeaderHolder;
    private LayoutInflater mInflater;
    private View.OnTouchListener mTouchListener;


    public PostDetailAdapter(Context context) {
        super(context);
        mInflater = LayoutInflater.from(context);
    }

    public void setPostData(PostDetail postData) {
        mPostData = postData;
    }

    public void setTouchListener(View.OnTouchListener touchListener) {
        mTouchListener = touchListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                if (mHeaderHolder == null) {
                    ItemPostDetailContentBinding binding = DataBindingUtil.inflate(mInflater,
                            R.layout.item_post_detail_content, parent, false);
                    mHeaderHolder = new HeaderHolder(binding.getRoot());
                    mHeaderHolder.setBinding(binding);
                }
                return mHeaderHolder;
            case TYPE_CMT_NO_PIC:
                ItemPostDetailCommentNoPicBinding noPicBinding = DataBindingUtil.inflate(mInflater,
                        R.layout.item_post_detail_comment_no_pic, parent, false);
                NoPicHolder noPicHolder = new NoPicHolder(noPicBinding.getRoot());
                noPicHolder.setBinding(noPicBinding);
                return noPicHolder;
            case TYPE_CMT_ONE_PIC:
                ItemPostDetailCommentOnePicBinding onePicBinding = DataBindingUtil.inflate(mInflater,
                        R.layout.item_post_detail_comment_one_pic, parent, false);
                OnePicHolder onePicHolder = new OnePicHolder(onePicBinding.getRoot());
                onePicHolder.setBinding(onePicBinding);
                return onePicHolder;
            case TYPE_CMT_THREE_PIC:
                ItemPostDetailCommentThreePicBinding threePicBinding = DataBindingUtil.inflate(mInflater,
                        R.layout.item_post_detail_comment_three_pic, parent, false);
                ThreePicHolder threePicHolder = new ThreePicHolder(threePicBinding.getRoot());
                threePicHolder.setBinding(threePicBinding);
                return threePicHolder;
            case TYPE_CMT_SIX_PIC:
                ItemPostDetailCommentSixPicBinding sixPicBinding = DataBindingUtil.inflate(mInflater,
                        R.layout.item_post_detail_comment_six_pic, parent, false);
                SixPicHolder sixPicHolder = new SixPicHolder(sixPicBinding.getRoot());
                sixPicHolder.setBinding(sixPicBinding);
                return sixPicHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        BaseRVH rvh = (BaseRVH) holder;
        if (type == TYPE_HEADER) {
            rvh.bindData(mPostData, this, position);
        } else {
            rvh.bindData(getItem(position), this, position);
        }
    }

    @Override
    public int getHeaderCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        }
        CommentDetail detail = getItem(position);
        int count = (detail.commentInfo.thumbImgs == null ? 0 : detail.commentInfo.thumbImgs.size());
        // 有评论的
        switch (count) {
            case 0:
                return TYPE_CMT_NO_PIC;
            case 1:
                return TYPE_CMT_ONE_PIC;
            case 2:
            case 3:
                return TYPE_CMT_THREE_PIC;
            case 4:
            default:
                return TYPE_CMT_SIX_PIC;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_vote) {
            notifyItemChanged(0);
        } else {
            if (v.getTag(TAG_POSITION) == null) {
                return;
            }
            int pos = (int) v.getTag(TAG_POSITION);
            CommentDetail detail = getItem(pos);
            if (mListener != null) {
                mListener.onItemClick(detail, v, pos);
            }
        }
    }

    @Override
    public void release() {
        super.release();
        mInflater = null;
        if (mHeaderHolder != null) {
            mHeaderHolder.destroy();
        }
        mHeaderHolder = null;
        mPostData = null;
        mTouchListener = null;
    }

    /**
     * 显示评论底下的子评论列表
     */
    static void showPostSubComment(CommentDetail detail, Context context, View divider,
                                   ViewGroup parent, View.OnClickListener listener, int pos) {
        if (detail.commentInfo.commentCount > 0) {
            // 附带子评论内容
            parent.removeAllViews();
            parent.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);

            int count = detail.commentInfo.commentCount > COUNT_SUB_COMMENT ?
                    COUNT_SUB_COMMENT : detail.commentInfo.commentCount;
            try {
                TextView tv;
                String s;
                for (int i = 0; i < count; i++) {
                    // 循环添加评论
                    tv = (TextView) LayoutInflater.from(context)
                            .inflate(R.layout.view_post_comment_sub_content, parent, false);
                    CommentDetailListItem detailItem = detail.listInfo.get(i);

                    // 设置样式
                    int contentStartIndex = 0;
                    if (!TextUtils.isEmpty(detailItem.atNick)) {
                        s = String.format(Locale.CHINA, "%s: 回复: %s %s", detailItem.nick,
                                detailItem.atNick, URLDecoder.decode(detailItem.content, "UTF-8"));
                        contentStartIndex = detailItem.nick.length() + 2;
                    } else {
                        s = String.format(Locale.CHINA, "%s: %s", detailItem.nick,
                                URLDecoder.decode(detailItem.content, "UTF-8"));
                    }
                    SpannableString ss = new SpannableString(s);
                    ss.setSpan(new TextAppearanceSpan(context, R.style.DefaultTextView_N1),
                            0, detailItem.nick.length() + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    if (contentStartIndex != 0) {
                        ss.setSpan(new TextAppearanceSpan(context, R.style.DefaultTextView_N2),
                                contentStartIndex, contentStartIndex + detailItem.atNick.length() + 4,
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                    tv.setText(ss);

//                if (i != 0) {
//                    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) tv.getLayoutParams();
//                    lp.topMargin = TOP_MARGIN;
//                    tv.setLayoutParams(lp);
//                }
                    parent.addView(tv);
                }
            } catch (UnsupportedEncodingException e) {
                AppDebugConfig.w(AppDebugConfig.TAG_ADAPTER, e);
            }
            if (detail.commentInfo.commentCount > COUNT_SUB_COMMENT) {
                // 添加更多回复
                TextView tv = (TextView) LayoutInflater.from(context)
                        .inflate(R.layout.view_post_comment_sub_more, parent, false);
                tv.setText(String.format(Locale.CHINA, "更多%d条回复", detail.commentInfo.commentCount - 3));
                tv.setOnClickListener(listener);
                tv.setTag(TAG_POSITION, pos);
                parent.addView(tv);
            }
        } else {
            divider.setVisibility(View.GONE);
            parent.setVisibility(View.GONE);
        }
    }

    static void showPostCommentNoPic(CommentDetailInfo info, ImageView ivIcon, TextView tvName, TextView tvContent,
                                     TextView tvFloor, TextView tvPubTime, TextView tvAdmire, TextView tvComment,
                                     View.OnClickListener listener, int pos) {
        try {
            ViewUtil.showAvatarImage(info.avatar, ivIcon, true);
            tvName.setText(info.nick);
            tvContent.setText(URLDecoder.decode(info.content, "UTF-8"));
            tvFloor.setText(format(Locale.CHINA, "%d楼", info.floor));
            tvPubTime.setText(info.pubTime);
            tvAdmire.setText(String.valueOf(info.likeCount));
            tvComment.setText(String.valueOf(info.commentCount));
            tvComment.setOnClickListener(listener);
            tvComment.setTag(TAG_POSITION, pos);
            tvAdmire.setOnClickListener(listener);
            tvAdmire.setTag(TAG_POSITION, pos);
            tvAdmire.setSelected(info.isLike);
        } catch (UnsupportedEncodingException e) {
            AppDebugConfig.w(AppDebugConfig.TAG_ADAPTER, e);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mTouchListener != null) {
            mTouchListener.onTouch(v, event);
        }
        return false;
    }

    static class BaseRVH<T extends ViewDataBinding> extends BaseRVHolder {

        T mBinding;

        BaseRVH(View itemView) {
            super(itemView);
        }

        void setBinding(T binding) {
            mBinding = binding;
        }

        void bindData(Object data, PostDetailAdapter adapter, int pos) {
        }
    }

    static class HeaderHolder extends BaseRVH<ItemPostDetailContentBinding> {

        boolean mIsFirst = true;
        PostDetailVoteAdapter mVoteAdapter;

        HeaderHolder(View itemView) {
            super(itemView);
        }

        @Override
        void setBinding(ItemPostDetailContentBinding binding) {
            super.setBinding(binding);
            initWebView();
        }

        private void initWebView() {
            WebSettings settings = mBinding.wvContent.getSettings();
            settings.setJavaScriptEnabled(false);
            settings.setDisplayZoomControls(false);
            settings.setSupportMultipleWindows(false);
            settings.setAppCacheEnabled(false);
            settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
//            settings.setUseWideViewPort(true);
            settings.setLoadWithOverviewMode(true);
        }

        @Override
        void bindData(Object data, PostDetailAdapter adapter, int pos) {
            if (data == null || !(data instanceof PostDetail)) return;
            PostDetail detail = (PostDetail) data;
            if (mIsFirst) {
                // 设置固定数据
                PostDetailInfo info = detail.postInfo;
                // 找到所有 <img />，插入 style='width:100%;height:auto;'
                String content = "<style type=\"text/css\">body{font: 14px/1.8 arial;}</style>"
                        + info.content.replaceAll("<img", "<img style='width:100%;height:auto;'");
                mBinding.wvContent.loadDataWithBaseURL(null, content, "text/html", "UTF-8", null);
                mBinding.wvContent.setOnTouchListener(adapter);
                mBinding.wvContent.setTag(TAG_POSITION, pos);
                ViewUtil.showAvatarImage(info.icon, mBinding.ivIcon, true);
                mBinding.tvHot.setText(String.valueOf(info.skipCount));
                mBinding.tvName.setText(info.nick);
                if (info.userType == 1) {
                    // 管理员
                    mBinding.tvName.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.ic_icon_vip, 0
                    );
                    mBinding.ivManager.setVisibility(View.VISIBLE);
                } else {
                    mBinding.tvName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    mBinding.ivManager.setVisibility(View.GONE);
                }
                mBinding.tvDesc.setText(info.signature);
                mBinding.tvName.setText(info.nick);
                mBinding.tvTitle.setText(info.title);
                mBinding.tvPubTime.setText(String.format(Locale.CHINA, "发布时间: %s", info.pubTime));
                mBinding.tvComment.setOnClickListener(adapter);
                mBinding.tvComment.setTag(TAG_POSITION, pos);
                mIsFirst = false;
            }
            mBinding.tvComment.setText(String.valueOf(detail.postInfo.commentCount));
            if (detail.voteInfo != null) {
                // 添加投票内容
                if (mVoteAdapter == null) {
                    mVoteAdapter = new PostDetailVoteAdapter(itemView.getContext());
                    mBinding.rvVote.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                    mVoteAdapter.setOnClickListener(adapter);
                }
                mBinding.rvVote.setVisibility(View.VISIBLE);
                mVoteAdapter.setInfo(detail.voteInfo);
                mVoteAdapter.setData(detail.voteInfo.voteItems);
                mBinding.rvVote.setAdapter(mVoteAdapter);
                mVoteAdapter.notifyDataSetChanged();
            }
        }

        void destroy() {
            if (mBinding != null) {
                mBinding.wvContent.removeAllViews();
                mBinding.wvContent.destroy();
            }
            if (mVoteAdapter != null) {
                mVoteAdapter.release();
                mVoteAdapter = null;
            }
        }
    }

    static class NoPicHolder extends BaseRVH<ItemPostDetailCommentNoPicBinding> {

        NoPicHolder(View itemView) {
            super(itemView);
        }

        @Override
        void bindData(Object data, PostDetailAdapter adapter, int pos) {
            if (data == null || !(data instanceof CommentDetail)) return;
            CommentDetail detail = (CommentDetail) data;
            showPostCommentNoPic(detail.commentInfo, mBinding.ivIcon, mBinding.tvName, mBinding.tvContent,
                    mBinding.tvFloor, mBinding.tvPubTime, mBinding.tvAdmire, mBinding.tvComment, adapter, pos);
            showPostSubComment(detail, itemView.getContext(), mBinding.vDivider, mBinding.llComment, adapter, pos);
        }

    }

    static class OnePicHolder extends BaseRVH<ItemPostDetailCommentOnePicBinding> {

        OnePicHolder(View itemView) {
            super(itemView);
        }

        @Override
        void bindData(Object data, PostDetailAdapter adapter, int pos) {
            if (data == null || !(data instanceof CommentDetail)) return;
            CommentDetail detail = (CommentDetail) data;
            showPostCommentNoPic(detail.commentInfo, mBinding.ivIcon, mBinding.tvName, mBinding.tvContent,
                    mBinding.tvFloor, mBinding.tvPubTime, mBinding.tvAdmire, mBinding.tvComment, adapter, pos);
            showPostSubComment(detail, itemView.getContext(), mBinding.vDivider, mBinding.llComment, adapter, pos);

            // 显示 1 张图片
            ViewUtil.showImage(mBinding.ivPic1, detail.commentInfo.thumbImgs.get(0));
            mBinding.ivPic1.setOnClickListener(adapter);
            mBinding.ivPic1.setTag(TAG_POSITION, pos);
        }
    }

    static class ThreePicHolder extends BaseRVH<ItemPostDetailCommentThreePicBinding> {

        ThreePicHolder(View itemView) {
            super(itemView);
        }

        @Override
        void bindData(Object data, PostDetailAdapter adapter, int pos) {
            if (data == null || !(data instanceof CommentDetail)) return;
            CommentDetail detail = (CommentDetail) data;
            showPostCommentNoPic(detail.commentInfo, mBinding.ivIcon, mBinding.tvName, mBinding.tvContent,
                    mBinding.tvFloor, mBinding.tvPubTime, mBinding.tvAdmire, mBinding.tvComment, adapter, pos);
            showPostSubComment(detail, itemView.getContext(), mBinding.vDivider, mBinding.llComment, adapter, pos);

            // 显示 2 ~ 3 张图片
            ViewUtil.showImage(mBinding.ivListPic1, detail.commentInfo.thumbImgs.get(0));
            mBinding.ivListPic1.setOnClickListener(adapter);
            mBinding.ivListPic1.setTag(TAG_POSITION, pos);
            ViewUtil.showImage(mBinding.ivListPic2, detail.commentInfo.thumbImgs.get(1));
            mBinding.ivListPic2.setOnClickListener(adapter);
            mBinding.ivListPic2.setTag(TAG_POSITION, pos);
            if (detail.commentInfo.thumbImgs.size() == 3) {
                ViewUtil.showImage(mBinding.ivListPic3, detail.commentInfo.thumbImgs.get(2));
                mBinding.ivListPic3.setOnClickListener(adapter);
                mBinding.ivListPic3.setTag(TAG_POSITION, pos);
                mBinding.ivListPic3.setVisibility(View.VISIBLE);
            } else {
                mBinding.ivListPic3.setVisibility(View.GONE);
            }
        }
    }

    static class SixPicHolder extends BaseRVH<ItemPostDetailCommentSixPicBinding> {

        SixPicHolder(View itemView) {
            super(itemView);
        }

        @Override
        void bindData(Object data, PostDetailAdapter adapter, int pos) {
            if (data == null || !(data instanceof CommentDetail)) return;
            CommentDetail detail = (CommentDetail) data;
            showPostCommentNoPic(detail.commentInfo, mBinding.ivIcon, mBinding.tvName, mBinding.tvContent,
                    mBinding.tvFloor, mBinding.tvPubTime, mBinding.tvAdmire, mBinding.tvComment, adapter, pos);
            showPostSubComment(detail, itemView.getContext(), mBinding.vDivider, mBinding.llComment, adapter, pos);

            ArrayList<String> thumbImgs = detail.commentInfo.thumbImgs;
            // 显示 4 ~ 6 张图片
            ViewUtil.showImage(mBinding.ivListPic1, thumbImgs.get(0));
            mBinding.ivListPic1.setOnClickListener(adapter);
            mBinding.ivListPic1.setTag(TAG_POSITION, pos);
            ViewUtil.showImage(mBinding.ivListPic2, thumbImgs.get(1));
            mBinding.ivListPic2.setOnClickListener(adapter);
            mBinding.ivListPic2.setTag(TAG_POSITION, pos);
            ViewUtil.showImage(mBinding.ivListPic3, thumbImgs.get(2));
            mBinding.ivListPic3.setOnClickListener(adapter);
            mBinding.ivListPic3.setTag(TAG_POSITION, pos);
            ViewUtil.showImage(mBinding.ivListPic4, thumbImgs.get(3));
            mBinding.ivListPic4.setOnClickListener(adapter);
            mBinding.ivListPic4.setTag(TAG_POSITION, pos);
            switch (thumbImgs.size()) {
                case 6:
                    ViewUtil.showImage(mBinding.ivListPic6, thumbImgs.get(5));
                    mBinding.ivListPic6.setOnClickListener(adapter);
                    mBinding.ivListPic6.setTag(TAG_POSITION, pos);
                    mBinding.ivListPic6.setVisibility(View.VISIBLE);
                case 5:
                    mBinding.ivListPic5.setVisibility(View.VISIBLE);
                    ViewUtil.showImage(mBinding.ivListPic5, thumbImgs.get(4));
                    mBinding.ivListPic5.setOnClickListener(adapter);
                    mBinding.ivListPic5.setTag(TAG_POSITION, pos);
                case 4:
                    if (thumbImgs.size() < 5)
                        mBinding.ivListPic5.setVisibility(View.GONE);
                    if (thumbImgs.size() < 6)
                        mBinding.ivListPic6.setVisibility(View.GONE);
                    break;

            }
        }
    }
}
