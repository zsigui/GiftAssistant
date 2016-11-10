package com.oplay.giftcool.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.databinding.ItemCommentDetailContentBinding;
import com.oplay.giftcool.databinding.ItemCommentDetailHeaderBinding;
import com.oplay.giftcool.model.data.resp.CommentDetail;
import com.oplay.giftcool.model.data.resp.CommentDetailInfo;
import com.oplay.giftcool.model.data.resp.CommentDetailListItem;
import com.oplay.giftcool.util.ViewUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by zsigui on 16-11-7.
 */

public class CommentDetailAdapter extends BaseRVAdapter<CommentDetailListItem> implements View.OnClickListener {

    final int TYPE_HEADER = 0x12222222;
    final int TYPE_CONTENT = 0x12222223;

    private CommentDetail mDetailData;
    private LayoutInflater mInflater;

    public CommentDetailAdapter(Context context) {
        super(context);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                ItemCommentDetailHeaderBinding headerBinding = DataBindingUtil.inflate(mInflater,
                        R.layout.item_comment_detail_header, parent, false);
                HeaderVH headerVH = new HeaderVH(headerBinding.getRoot());
                headerVH.setBinding(headerBinding);
                return headerVH;
            case TYPE_CONTENT:
                ItemCommentDetailContentBinding contentBinding = DataBindingUtil.inflate(mInflater,
                        R.layout.item_comment_detail_content, parent, false);
                ContentVH contentVH = new ContentVH(contentBinding.getRoot());
                contentVH.setBinding(contentBinding);
                return contentVH;
        }
        return null;
    }

    public void setDetailData(CommentDetail detailData) {
        mDetailData = detailData;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        BaseRVH rvh = (BaseRVH) holder;
        if (type == TYPE_HEADER) {
            rvh.bindData(mDetailData, this, position);
        } else {
            rvh.bindData(getItem(position), this, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_HEADER : TYPE_CONTENT;
    }

    @Override
    public int getHeaderCount() {
        return mDetailData != null && mDetailData.commentInfo != null ? 1 : 0;
    }

    @Override
    public void onClick(View v) {
        if (v.getTag(TAG_POSITION) == null) return;

        int pos = (int) v.getTag(TAG_POSITION);
        if (mListener != null) {
            mListener.onItemClick(getItem(pos), v, pos);
        }
    }

    @Override
    public void release() {
        super.release();
        mInflater = null;
        mDetailData = null;
    }

    static void showPostCommentNoPic(CommentDetailInfo info, ImageView ivIcon, TextView tvName, TextView tvContent,
                                     TextView tvPubTime, TextView tvAdmire, TextView tvComment,
                                     View.OnClickListener listener, int pos) {
        try {
            ViewUtil.showAvatarImage(info.avatar, ivIcon, true);
            tvName.setText(info.nick);
            tvContent.setText(URLDecoder.decode(info.content, "UTF-8"));
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

    static abstract class BaseRVH<T extends ViewDataBinding> extends BaseRVHolder {

        T mBinding;

        BaseRVH(View itemView) {
            super(itemView);
        }

        void setBinding(T binding) {
            mBinding = binding;
        }

        void bindData(Object data, CommentDetailAdapter adapter, int pos) {
        }
    }

    static class HeaderVH extends BaseRVH<ItemCommentDetailHeaderBinding> {

        HeaderVH(View itemView) {
            super(itemView);
        }

        @Override
        void bindData(Object data, CommentDetailAdapter adapter, int pos) {
            if (data == null || !(data instanceof CommentDetail)) return;
            CommentDetail detail = (CommentDetail) data;
            showPostCommentNoPic(detail.commentInfo, mBinding.ivIcon, mBinding.tvName, mBinding.tvContent,
                    mBinding.tvPubTime, mBinding.tvAdmire, mBinding.tvComment, adapter, pos);
            itemView.setOnClickListener(adapter);
            itemView.setTag(TAG_POSITION, pos);

            ArrayList<String> thumbImgs = detail.commentInfo.thumbImgs;
            if (thumbImgs == null || thumbImgs.isEmpty()) {
                // do nothing here
            } else if (thumbImgs.size() == 1) {
                mBinding.ivPic1.setVisibility(View.VISIBLE);
                // 显示 1 张图片
                ViewUtil.showImage(mBinding.ivPic1, detail.commentInfo.thumbImgs.get(0));
                mBinding.ivPic1.setOnClickListener(adapter);
                mBinding.ivPic1.setTag(TAG_POSITION, pos);
            } else if (thumbImgs.size() < 4) {
                mBinding.llPic1.setVisibility(View.VISIBLE);
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
            } else {
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

    static class ContentVH extends BaseRVH<ItemCommentDetailContentBinding> {

        ContentVH(View itemView) {
            super(itemView);
        }

        @Override
        void bindData(Object data, CommentDetailAdapter adapter, int pos) {
            if (data == null || !(data instanceof CommentDetailListItem)) return;
            CommentDetailListItem detailItem = (CommentDetailListItem) data;
            ViewUtil.showAvatarImage(detailItem.avatar, mBinding.ivIcon, true);
            mBinding.tvName.setText(detailItem.nick);
            try {
                // 设置样式
                String s;
                if (!TextUtils.isEmpty(detailItem.atNick)) {
                    s = String.format(Locale.CHINA, "回复: %s %s", detailItem.atNick, URLDecoder.decode(detailItem
                            .content, "UTF-8"));
                    SpannableString ss = new SpannableString(s);
                    ss.setSpan(new TextAppearanceSpan(itemView.getContext(), R.style.DefaultTextView_N2),
                            0, detailItem.atNick.length() + 4, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    mBinding.tvContent.setText(ss);
                } else {

                    mBinding.tvContent.setText(URLDecoder.decode(detailItem.content, "UTF-8"));
                }
            } catch (UnsupportedEncodingException e) {
                AppDebugConfig.w(AppDebugConfig.TAG_ADAPTER, e);
            }
            mBinding.tvPubTime.setText(detailItem.pubTime);
            itemView.setOnClickListener(adapter);
            itemView.setTag(TAG_POSITION, pos);
        }
    }
}
