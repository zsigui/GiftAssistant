package com.oplay.giftcool.adapter;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.adapter.base.FooterHolder;
import com.oplay.giftcool.adapter.holder.StyleBaseHolder;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
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
import com.oplay.giftcool.util.UiStyleUtil;

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
    private static final int TYPE_FOOTER = TYPE_DEFAULT + 4;
    private static final int TYPE_NEW_ITEM_BASE = TYPE_DEFAULT + 5;
    private static final int COUNT_HEADER = 4;
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
                return new FooterHolder(inflateView(parent, R.layout.view_item_footer));
            case TYPE_BANNER:
                if (mBannerWR == null || mBannerWR.get() == null) {
                    mBannerWR = new WeakReference<BannerVH>(new BannerVH(inflateView(parent, R.layout.view_banner)));
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
                LikeVH likeVH = new LikeVH(inflateView(parent, R.layout.view_gift_index_like));
                LinearLayoutManager llmLike = new LinearLayoutManager(mContext);
                llmLike.setOrientation(LinearLayoutManager.HORIZONTAL);
                likeVH.rvContainer.setLayoutManager(llmLike);
                likeVH.rvAdapter = new IndexGiftLikeAdapter(mContext);
                // 加载数据
                likeVH.rvContainer.setAdapter(likeVH.rvAdapter);
                return likeVH;
            case TYPE_LIMIT:
                LimitVH limitVH = new LimitVH(inflateView(parent, R.layout.view_gift_index_limit));
                LinearLayoutManager llmLimit = new LinearLayoutManager(mContext);
                llmLimit.setOrientation(LinearLayoutManager.HORIZONTAL);
                limitVH.rvContainer.setLayoutManager(llmLimit);
                limitVH.rvAdapter = new IndexGiftLimitAdapter(mContext);
                // 加载数据
                limitVH.rvContainer.setAdapter(limitVH.rvAdapter);
                return limitVH;
            case TYPE_NEW_HEAD:
                return new ItemTitleVH(inflateView(parent, R.layout.view_gift_index_item_title));
            default: {
                int uiStyle = viewType - TYPE_NEW_ITEM_BASE;
                return UiStyleUtil.onCreateHolder(mContext, null, parent, uiStyle, false);
            } // default case finished
        }
    }

    protected View inflateView(ViewGroup parent, int id) {
        return mInflater.inflate(id, parent, false);
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

    /*
     * 获取头部数量，默认至少有‘轮播’、‘限量’、‘新增头部’；‘猜你喜欢’延迟加载可能变动
     */
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
                StyleBaseHolder baseHolder = (StyleBaseHolder) holder;
                UiStyleUtil.bindListener(baseHolder, TAG_POS, position, this);
                UiStyleUtil.bindHolderData(mContext, baseHolder, o);
        }
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

    public IndexGiftNew getItem(int position) {
        return position >= 0 && mData != null && mData.news != null && position < mData.news.size() ?
                mData.news.get(position) : null;
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= getHeaderCount()) {
            if (mShowFooter && position == getItemCount() - 1) {
                return TYPE_FOOTER;
            } else {
                IndexGiftNew o = getItem(position - getHeaderCount());
                if (o == null) {
                    return GiftTypeUtil.UI_TYPE_NORMAL_SEIZE + TYPE_NEW_ITEM_BASE;
                }
                o.uiStyle = (o.uiStyle == GiftTypeUtil.UI_TYPE_DEFAULT ? GiftTypeUtil.UI_TYPE_NORMAL_SEIZE : o.uiStyle);
                return o.uiStyle + TYPE_NEW_ITEM_BASE;
            }
        } else {
            return TYPE_DEFAULT + position;
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
                    AppDebugConfig.d(AppDebugConfig.TAG_WARN, "uiStyle = " + gift.uiStyle + ", state = " + gift
                            .buttonState
                            + ", status = " + gift.status + ", giftType = " + gift.giftType + ", totalType = " + gift
                            .totalType);
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
}
