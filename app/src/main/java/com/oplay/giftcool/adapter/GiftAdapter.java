package com.oplay.giftcool.adapter;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.adapter.base.FooterHolder;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.util.BannerTypeUtil;
import com.oplay.giftcool.config.util.GiftTypeUtil;
import com.oplay.giftcool.ext.holder.BannerHolderCreator;
import com.oplay.giftcool.listener.FooterListener;
import com.oplay.giftcool.manager.PayManager;
import com.oplay.giftcool.manager.StatisticsManager;
import com.oplay.giftcool.model.data.resp.IndexBanner;
import com.oplay.giftcool.model.data.resp.IndexGift;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ViewUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

/**
 * 礼包首页的 Adapter, 实现多种布局配置
 * <p/>
 * Created by zsigui on 16-2-23.
 */
public class GiftAdapter extends RecyclerView.Adapter implements com.bigkoo.convenientbanner.listener
        .OnItemClickListener, View.OnClickListener, FooterListener {

    private static final int TAG_POS = 0x1234FFFF;
    private static final int TYPE_DEFAULT = 110;
    private static final int TYPE_BANNER = TYPE_DEFAULT;
    private static final int TYPE_LIKE = TYPE_DEFAULT + 1;
    private static final int TYPE_LIMIT = TYPE_DEFAULT + 2;
    private static final int TYPE_NEW_HEAD = TYPE_DEFAULT + 3;
    private static final int TYPE_NEW_ITEM = TYPE_DEFAULT + 4;
    private static final int TYPE_FOOTER = TYPE_DEFAULT + 11;
    private static final int COUNT_HEADER = 4;

    final ImageSpan DRAWER_GOLD;

    private IndexGift mData;
    private FragmentActivity mContext;
    private LayoutInflater mInflater;
    private boolean mShowFooter = false;

    // banner
    private WeakReference<BannerVH> mBannerWR;
    private IndexBanner mDefaultBanner;
    private BannerHolderCreator mBannerHolderCreator;
    private int[] mBannerIndicatorDrawable;
    private ArrayList<String> mBannerData;


    public GiftAdapter(FragmentActivity context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        initDefaultBannerConfig();
        DRAWER_GOLD = new ImageSpan(context, R.drawable.ic_score);
    }

    private void initDefaultBannerConfig() {
        mBannerHolderCreator = new BannerHolderCreator();
        mBannerIndicatorDrawable = new int[]{R.drawable.ic_banner_point_normal, R.drawable.ic_banner_point_selected};
        mDefaultBanner = new IndexBanner();
        mDefaultBanner.url = "drawable://" + R.drawable.ic_banner_empty_default;
        mDefaultBanner.type = BannerTypeUtil.ACTION_SCORE_TASK;
        mBannerData = new ArrayList<>();
    }

    public void setData(IndexGift data) {
        mData = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_FOOTER:
                return new FooterHolder(LayoutInflater.from(mContext).inflate(R.layout.view_item_footer, parent,
                        false));
            case TYPE_BANNER:
                if (mBannerWR == null || mBannerWR.get() == null) {
                    mBannerWR = new WeakReference<BannerVH>(new BannerVH(mInflater.inflate(R.layout.view_banner,
                            parent, false)));
                }
                BannerVH bannerVH = mBannerWR.get();
                ConvenientBanner banner = bannerVH.mBanner;
                banner.setPageIndicator(mBannerIndicatorDrawable);
                banner.setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL);
                banner.setOnItemClickListener(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        Global.getBannerHeight(mContext));
                banner.setLayoutParams(lp);
                return bannerVH;
//			case TYPE_ZERO:
//				ZeroVH zeroVH = new ZeroVH(mInflater.inflate(R.layout.view_gift_index_zero, parent, false));
//				LinearLayoutManager llmZero = new LinearLayoutManager(mContext);
//				llmZero.setOrientation(LinearLayoutManager.HORIZONTAL);
//				zeroVH.rvContainer.setLayoutManager(llmZero);
//				zeroVH.rvAdapter = new IndexGiftZeroAdapter(mContext);
//				// 加载数据
//				zeroVH.rvContainer.setAdapter(zeroVH.rvAdapter);
//				return zeroVH;
            case TYPE_LIKE:
                LikeVH likeVH = new LikeVH(mInflater.inflate(R.layout.view_gift_index_like, parent, false));
                LinearLayoutManager llmLike = new LinearLayoutManager(mContext);
                llmLike.setOrientation(LinearLayoutManager.HORIZONTAL);
                likeVH.rvContainer.setLayoutManager(llmLike);
                likeVH.rvAdapter = new IndexGiftLikeAdapter(mContext);
                // 加载数据
                likeVH.rvContainer.setAdapter(likeVH.rvAdapter);
                return likeVH;
            case TYPE_LIMIT:
                LimitVH limitVH = new LimitVH(mInflater.inflate(R.layout.view_gift_index_limit, parent, false));
                LinearLayoutManager llmLimit = new LinearLayoutManager(mContext);
                llmLimit.setOrientation(LinearLayoutManager.HORIZONTAL);
                limitVH.rvContainer.setLayoutManager(llmLimit);
                limitVH.rvAdapter = new IndexGiftLimitAdapter(mContext);
                // 加载数据
                limitVH.rvContainer.setAdapter(limitVH.rvAdapter);
                return limitVH;
            case TYPE_NEW_HEAD:
                return new ItemTitleVH(mInflater.inflate(R.layout.view_gift_index_item_title, parent, false));
            case TYPE_NEW_ITEM:
                return new ItemHolder(mInflater.inflate(R.layout.item_index_gift_new_list, parent, false));
            default:
                return null;
        }
    }

    /**
     * 更新轮播图内容
     */
    private void updateBanners(BannerVH bannerVH) {
        if (bannerVH == null || mData == null) {
            return;
        }
        if (mData.banner == null) {
            mData.banner = new ArrayList<>();
        }
        ArrayList<IndexBanner> banners = mData.banner;
        if (banners.size() == 0) {
            mData.banner.add(mDefaultBanner);
        }
        mData.banner = banners;
        mBannerData.clear();
        for (IndexBanner banner : banners) {
            mBannerData.add(banner.url);
        }
        ConvenientBanner banner = bannerVH.mBanner;
        banner.setPages(mBannerHolderCreator, mBannerData);
        if (mBannerData.size() == 1) {
            banner.setCanLoop(false);
            banner.stopTurning();
        } else {
            banner.setCanLoop(true);
            banner.startTurning(AppConfig.BANNER_LOOP_TIME);
        }

    }

    private int getHeaderCount() {
        return mData == null ? 0 : (mData.like == null || mData.like.isEmpty() ? COUNT_HEADER - 1 : COUNT_HEADER);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemCount() == 0 || holder == null) {
            return;
        }
        int type = getItemViewType(position);
        switch (type) {
            case TYPE_FOOTER:
                return;
            case TYPE_BANNER:
                updateBanners((BannerVH) holder);
                break;
//			case TYPE_ZERO:
//				ZeroVH zeroVH = ((ZeroVH) holder);
//				zeroVH.rvAdapter.updateData(mData.zero);
//				zeroVH.tvSubTitle.setText(TITLE_ZERO);
//				break;
            case TYPE_LIKE:
                LikeVH likeVH = ((LikeVH) holder);
                likeVH.rlTitle.setOnClickListener(this);
                likeVH.rvAdapter.updateData(mData.like);
                break;
            case TYPE_LIMIT:
                LimitVH limitVH = ((LimitVH) holder);
                limitVH.rvAdapter.updateData(mData.limit);
//				limitVH.tvSubTitle.setText(TITLE_LIMIT);
                limitVH.rlTitle.setOnClickListener(this);
                break;
            case TYPE_NEW_HEAD:
                break;
            default:
                if (mData.news == null || mData.news.size() == 0
                        || mData.news.size() <= position - getHeaderCount()) {
                    return;
                }
                final IndexGiftNew o = mData.news.get(position - getHeaderCount());
                type = GiftTypeUtil.getItemViewType(o);
                ItemHolder viewHolder = (ItemHolder) holder;
                viewHolder.btnSend.setTag(TAG_POS, position);
                viewHolder.btnSend.setOnClickListener(this);
                viewHolder.itemView.setTag(TAG_POS, position);
                viewHolder.itemView.setOnClickListener(this);
                handleGiftNormalCharge(type, o, viewHolder);
        }
    }

    private void handleGiftNormalCharge(int type, IndexGiftNew o, ItemHolder holder) {
        ViewUtil.showImage(holder.ivIcon, o.img);
        holder.tvName.setText(String.format("[%s]%s", o.gameName, o.name));
        holder.btnSend.setState(GiftTypeUtil.getButtonState(o));
        holder.tvContent.setText(o.content);
        if (type != GiftTypeUtil.TYPE_NORMAL_SEIZE) {
            holder.tvMoney.setVisibility(View.GONE);
            holder.tvPercent.setVisibility(View.GONE);
            holder.pbPercent.setVisibility(View.GONE);
        }
        switch (type) {
            case GiftTypeUtil.TYPE_NORMAL_SEIZED:
                ViewUtil.siteSpendUI(holder.tvMoney, o.score, o.bean, o.priceType);
                holder.tvCount.setVisibility(View.GONE);
                break;
            case GiftTypeUtil.TYPE_NORMAL_SEIZE:
                ViewUtil.siteSpendUI(holder.tvMoney, o.score, o.bean, o.priceType);
                setProgressBarData(o, holder);
                holder.tvCount.setVisibility(View.GONE);
                break;
            case GiftTypeUtil.TYPE_NORMAL_FINISHED:
                holder.tvCount.setVisibility(View.GONE);
                break;
            case GiftTypeUtil.TYPE_NORMAL_WAIT_SEARCH:
                setDisabledText(holder.tvCount, Html.fromHtml(String.format("开淘时间：<font color='#ffaa17'>%s</font>",
                        o.searchTime)));
                break;
            case GiftTypeUtil.TYPE_NORMAL_WAIT_SEIZE:
                setDisabledText(holder.tvCount, Html.fromHtml(String.format("开抢时间：<font color='#ffaa17'>%s</font>",
                        o.seizeTime)));
                break;
            case GiftTypeUtil.TYPE_NORMAL_SEARCH:
            case GiftTypeUtil.TYPE_NORMAL_SEARCHED:
                setDisabledText(holder.tvCount, Html.fromHtml(String.format("已淘数：<font color='#ffaa17'>%s</font>",
                        o.searchCount)));
                break;
        }
    }

    private void setDisabledText(TextView tv, Spanned text) {
        tv.setVisibility(View.VISIBLE);
        tv.setText(text);
    }

    private void setProgressBarData(IndexGiftNew o, ItemHolder holder) {
        holder.tvPercent.setVisibility(View.VISIBLE);
        holder.pbPercent.setVisibility(View.VISIBLE);
        final int percent = (int) (Math.ceil(o.remainCount * 100.0 / o.totalCount));
        holder.tvPercent.setText(String.format(Locale.CHINA, "剩余%d%%", percent));
        holder.pbPercent.setProgress(percent);
        holder.pbPercent.setMax(100);
    }


    public void startBanner() {
        if (mBannerWR != null && mBannerWR.get() != null) {
            mBannerWR.get().mBanner.startTurning(AppConfig.BANNER_LOOP_TIME);
        }
    }

    public void stopBanner() {
        if (mBannerWR != null && mBannerWR.get() != null) {
            mBannerWR.get().mBanner.stopTurning();
        }
    }

    public boolean updateData(IndexGift data, int start, int end) {
        if (data == null) {
            return false;
        }
//		if (start < 0) {
//			start = -1;
//		}
//		if (end < 0) {
//			end = getItemCount();
//		}
        // 由于线程间会造成 RV 的 Inconsistency detected 问题，忽略
//		notifyItemRangeChanged(start, end - start);
        mData = data;
        notifyDataSetChanged();
        return true;
    }

    public boolean addData(IndexGift data, int start, int end) {
        if (data == null) {
            return false;
        }
        if (start < 0) {
            start = -1;
        }
        mData = data;
        if (end < 0) {
            end = getItemCount();
        }
        notifyItemRangeInserted(start, end - start);
        return true;
    }

    @Override
    public int getItemCount() {
        int count;
        if (mData == null) {
            count = 0;
        } else if (mData.news == null) {
            count = getHeaderCount();
        } else {
            count = getHeaderCount() + mData.news.size();
        }
        return mShowFooter && count != 0 ? count + 1 : count;
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= getHeaderCount()) {
            if (mShowFooter && position == getItemCount() - 1) {
                return TYPE_FOOTER;
            } else {
                return TYPE_NEW_ITEM;
            }
        } else {
            if (position == 0) {
                return TYPE_BANNER;
            } else if (getHeaderCount() == COUNT_HEADER) {
                return TYPE_DEFAULT + position;
            } else {
                return TYPE_DEFAULT + position + 1;
            }
        }
    }

    @Override
    public void onItemClick(int position) {
        if (mData == null || mData.banner == null || mData.banner.size() <= position) {
            return;
        }
        IndexBanner banner = mData.banner.get(position);
        StatisticsManager.getInstance().trace(mContext,
                StatisticsManager.ID.GIFT_BANNER,
                StatisticsManager.ID.STR_GIFT_BANNER,
                String.format(Locale.CHINA, "第%d推广位，标题：%s", position, banner.title));
        BannerTypeUtil.handleBanner(mContext, banner);
    }

    @Override
    public void onClick(View v) {
        IndexGiftNew gift = null;
        if (v.getId() == R.id.rl_recommend
                || v.getId() == R.id.btn_send) {
            Integer pos = (Integer) v.getTag(TAG_POS);
            if (pos == null || pos < getHeaderCount() || pos - getHeaderCount() >= mData.news.size()) {
                return;
            }
            gift = mData.news.get(pos - getHeaderCount());
        }
        switch (v.getId()) {
            case R.id.rl_like_all:
                IntentUtil.jumpGiftHotList(mContext, null);
                break;
            case R.id.rl_limit_all:
                IntentUtil.jumpGiftLimitList(mContext, false);
                break;
            case R.id.rl_recommend:
                if (gift != null) {
                    IntentUtil.jumpGiftDetail(mContext, gift.id);
                }
                break;
            case R.id.btn_send:
                if (gift == null)
                    return;
                if (gift.giftType == GiftTypeUtil.GIFT_TYPE_LIMIT_FREE) {
                    // 对于0元抢，先跳转到游戏详情
                    IntentUtil.jumpGiftDetail(mContext, gift.id);
                } else {
                    PayManager.getInstance().seizeGift(mContext, gift, (GiftButton) v);
                }
                break;
        }
    }

    @Override
    public void showFooter(boolean isShow) {
        mShowFooter = isShow;
        if (mShowFooter) {
            notifyItemInserted(getItemCount() - 1);
        } else {
            notifyItemRemoved(getItemCount());
        }
    }

    static class BannerVH extends BaseRVHolder {

        ConvenientBanner mBanner;

        public BannerVH(View itemView) {
            super(itemView);
            mBanner = getViewById(R.id.banner);
        }
    }

//	static class ZeroVH extends BaseRVHolder {
//
//		TextView tvSubTitle;
//		RecyclerView rvContainer;
//		IndexGiftZeroAdapter rvAdapter;
//
//		public ZeroVH(View itemView) {
//			super(itemView);
//			tvSubTitle = getViewById(R.id.tv_zero_limit);
//			rvContainer = getViewById(R.id.rv_zero_content);
//		}
//	}

    static class LikeVH extends BaseRVHolder {

        RelativeLayout rlTitle;
        RecyclerView rvContainer;
        IndexGiftLikeAdapter rvAdapter;

        public LikeVH(View itemView) {
            super(itemView);
            rlTitle = getViewById(R.id.rl_like_all);
            rvContainer = getViewById(R.id.rv_like_content);
        }
    }

    static class LimitVH extends BaseRVHolder {

        RelativeLayout rlTitle;
        //		TextView tvSubTitle;
        RecyclerView rvContainer;
        IndexGiftLimitAdapter rvAdapter;

        public LimitVH(View itemView) {
            super(itemView);
            rlTitle = getViewById(R.id.rl_limit_all);
//			tvSubTitle = getViewById(R.id.tv_limit_hint);
            rvContainer = getViewById(R.id.rv_limit_content);

        }
    }

    static class ItemTitleVH extends BaseRVHolder {

        public ItemTitleVH(View itemView) {
            super(itemView);
        }
    }

    static class ItemHolder extends BaseRVHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvContent;
        GiftButton btnSend;
        TextView tvMoney;
        TextView tvCount;
        TextView tvPercent;
        ProgressBar pbPercent;

        public ItemHolder(View itemView) {
            super(itemView);
            ivIcon = getViewById(R.id.iv_icon);
            tvName = getViewById(R.id.tv_name);
            tvContent = getViewById(R.id.tv_content);
            btnSend = getViewById(R.id.btn_send);
            tvMoney = getViewById(R.id.tv_money);
            tvCount = getViewById(R.id.tv_count);
            tvPercent = getViewById(R.id.tv_percent);
            pbPercent = getViewById(R.id.pb_percent);
        }
    }
}
