package com.oplay.giftcool.adapter;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.adapter.holder.StyleBaseHolder;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.util.BannerTypeUtil;
import com.oplay.giftcool.config.util.GameTypeUtil;
import com.oplay.giftcool.ext.holder.BannerHolderCreator;
import com.oplay.giftcool.manager.PayManager;
import com.oplay.giftcool.model.data.resp.IndexBanner;
import com.oplay.giftcool.model.data.resp.IndexGiftLike;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.ui.activity.MainActivity;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.UiStyleUtil;
import com.oplay.giftcool.util.ViewUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

/**
 * 礼包首页的 Adapter, 实现多种布局配置
 * <p/>
 * Created by zsigui on 16-2-23.
 */
public class GiftAdapter extends BaseRVAdapter<Object> implements View.OnClickListener {

    private static final int TAG_TYPE = 0x1234FFFE;

    public static final int TYPE_BANNER = 10001;
    public static final int TYPE_ICON_BAR = 10002;
    public static final int TYPE_HEADER_LIKE = 10003;
    public static final int TYPE_HEADER_LIMIT = 10004;
    // 限时免费即为之前的零元抢
    public static final int TYPE_HEADER_FREE = 10005;
    public static final int TYPE_HEADER_NEW = 10006;
    // 由于 like 的数据类型不为礼包类型，也即是无 UI Style 设置，故单独出来
    public static final int TYPE_ITEM_LIKE = 10007;

    private LayoutInflater mInflater;

    // banner
    private WeakReference<BannerVH> mBannerWR;
    private IndexBanner mDefaultBanner;
    private BannerHolderCreator mBannerHolderCreator;
    private int[] mBannerIndicatorDrawable;
    private ArrayList<String> mBannerData;
    private OnItemClickListener mOnBannerItemClickListener;

    private boolean mNeedShowTabHint = false;

    public GiftAdapter(FragmentActivity context) {
        super(context);
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

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_BANNER:
                BannerVH bVH;
                if (mBannerWR == null || mBannerWR.get() == null) {
                    bVH = new BannerVH(inflateView(parent, R.layout.view_banner));
                    mBannerWR = new WeakReference<BannerVH>(bVH);
                    ConvenientBanner banner = bVH.mBanner;
                    banner.setPageIndicator(mBannerIndicatorDrawable);
                    banner.setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL);
                    banner.setOnItemClickListener(mOnBannerItemClickListener);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            Global.getBannerHeight(mContext));
                    banner.setLayoutParams(lp);
                } else {
                    bVH = mBannerWR.get();
                }
                return bVH;
            case TYPE_ICON_BAR:
                return new IconBarVh(inflateView(parent, R.layout.item_index_gift_icon_bar));
            case TYPE_HEADER_FREE:
            case TYPE_HEADER_NEW:
            case TYPE_HEADER_LIMIT:
            case TYPE_HEADER_LIKE:
                return new HeaderVH(inflateView(parent, R.layout.view_index_item_title_1));
            case TYPE_ITEM_LIKE:
                return new ItemLikeVH(inflateView(parent, R.layout.item_list_gift_like));
            default: {
                return UiStyleUtil.onCreateHolder(mContext, null, parent, viewType, false);
            } // default case finished
        }
    }

    public void setOnBannerItemClickListener(OnItemClickListener onBannerItemClickListener) {
        mOnBannerItemClickListener = onBannerItemClickListener;
    }

    private View inflateView(ViewGroup parent, int id) {
        return mInflater.inflate(id, parent, false);
    }

    /**
     * 更新轮播图内容
     */
    @SuppressWarnings("unchecked")
    private void updateBanners(BannerVH bannerVH) {
        if (bannerVH == null || bannerVH.mBanner == null) {
            return;
        }
        ArrayList<IndexBanner> banners = (ArrayList<IndexBanner>) getItem(0);
        if (banners.size() == 0) {
            banners.add(mDefaultBanner);
        }
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
            if (!banner.isTurning()) {
                banner.startTurning(AppConfig.BANNER_LOOP_TIME);
            }
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemCount() == 0 || holder == null) {
            return;
        }
        int type = getItemViewType(position);
        switch (type) {
            case TYPE_BANNER:
                updateBanners((BannerVH) holder);
                break;
            case TYPE_ICON_BAR:
                updateIconBar((IconBarVh) holder, position);
                break;
            case TYPE_HEADER_FREE:
            case TYPE_HEADER_LIKE:
            case TYPE_HEADER_NEW:
            case TYPE_HEADER_LIMIT:
                updateHeaderData((HeaderVH) holder, position, type);
                break;
            case TYPE_ITEM_LIKE:
                updateLikeData((ItemLikeVH) holder, position, type);
                break;
            default:
                IndexGiftNew o = (IndexGiftNew) getItem(position);
                StyleBaseHolder baseHolder = (StyleBaseHolder) holder;
                UiStyleUtil.bindListener(baseHolder, TAG_POSITION, position, this);
                UiStyleUtil.bindHolderData(mContext, baseHolder, o);
        }
    }

    private void updateIconBar(IconBarVh holder, int position) {
        holder.llSign.setTag(TAG_POSITION, position);
        holder.llLimit.setTag(TAG_POSITION, position);
        holder.llNew.setTag(TAG_POSITION, position);
        holder.llFree.setTag(TAG_POSITION, position);
        holder.llSign.setTag(TAG_TYPE, TYPE_ICON_BAR);
        holder.llLimit.setTag(TAG_TYPE, TYPE_ICON_BAR);
        holder.llNew.setTag(TAG_TYPE, TYPE_ICON_BAR);
        holder.llFree.setTag(TAG_TYPE, TYPE_ICON_BAR);
        holder.llSign.setOnClickListener(this);
        holder.llLimit.setOnClickListener(this);
        holder.llNew.setOnClickListener(this);
        holder.llFree.setOnClickListener(this);
    }

    private void updateHeaderData(HeaderVH holder, int position, int type) {
        holder.rlItem.setOnClickListener(this);
        holder.rlItem.setTag(TAG_POSITION, position);
        holder.rlItem.setTag(TAG_TYPE, type);
//        holder.tvMore.setVisibility(type == TYPE_HEADER_NEW ? View.GONE : View.VISIBLE);

        CharSequence s;
        switch (type) {
            case TYPE_HEADER_FREE:
                s = mContext.getText(R.string.st_index_gift_free);
                break;
            case TYPE_HEADER_LIKE:
                s = mContext.getText(R.string.st_index_gift_like);
                break;
            case TYPE_HEADER_NEW:
                s = mContext.getText(R.string.st_index_gift_new);
                break;
            default:
                s = mContext.getText(R.string.st_index_gift_limit);
        }
        holder.tvTitle.setText(s);
    }

    /**
     * 设置猜你喜欢列表数据的显示
     */
    private void updateLikeData(ItemLikeVH holder, int position, int type) {
        IndexGiftLike o = (IndexGiftLike) getItem(position);
        holder.tvName.setText(o.name);
        if (o.newestCreateTime > Global.getLikeNewTimeArray().get(o.id) &&
                o.newestCreateTime > AssistantApp.getInstance().getLastLaunchTime() / 1000) {
            holder.ivHint.setVisibility(View.VISIBLE);
            mNeedShowTabHint = true;
        } else {
            holder.ivHint.setVisibility(View.GONE);
            if (position == 0) {

            }
        }
        if (mNeedShowTabHint && MainActivity.sGlobalHolder != null) {
            MainActivity.sGlobalHolder.showTabGiftHint(View.VISIBLE);
        }
        holder.tvSize.setText(o.size);
        holder.tvCount.setText(Html.fromHtml(String.format(Locale.CHINA, "<font color='#ffaa17'>%d</font>款礼包", o
                .totalCount)));
        holder.tvNewAdd.setText(Html.fromHtml(String.format(Locale.CHINA, "<font color='#ffaa17'>%s</font>", o
                .giftName)));
        ViewUtil.showImage(holder.ivIcon, o.img);
        holder.btnCheckout.setOnClickListener(this);
        holder.btnCheckout.setTag(TAG_POSITION, position);
        holder.itemView.setOnClickListener(this);
        holder.itemView.setTag(TAG_POSITION, position);
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

    @Override
    public int getItemViewType(int position) {
        Object obj = getItem(position);
        int type;
        if (obj == null) {
            type = 0;
        } else if (obj instanceof ArrayList) {
            type = position == 0 ? TYPE_BANNER : TYPE_ICON_BAR;
        } else if (obj instanceof Integer) {
            type = (int) obj;
        } else if (obj instanceof  IndexGiftLike) {
            type = TYPE_ITEM_LIKE;
        } else {
            type = ((IndexGiftNew) obj).uiStyle;
        }
        return type;
    }


    @Override
    public void onClick(View v) {
        Integer pos = (Integer) v.getTag(TAG_POSITION);
        Integer type = (Integer) v.getTag(TAG_TYPE);
        if (type != null) {
            switch (type) {
                case TYPE_HEADER_FREE:
                    IntentUtil.jumpGiftFreeList(mContext);
                    break;
                case TYPE_HEADER_LIKE:
                    IntentUtil.jumpGiftHotList(mContext);
                    break;
                case TYPE_HEADER_NEW:
                    IntentUtil.jumpGiftNewList(mContext);
                    break;
                case TYPE_HEADER_LIMIT:
                    IntentUtil.jumpGiftLimitList(mContext);
                    break;
                case TYPE_ICON_BAR:
                    switch (v.getId()) {
                        case R.id.ll_limit:
                            IntentUtil.jumpGiftLimitList(mContext);
                            break;
                        case R.id.ll_new:
                            IntentUtil.jumpGiftNewList(mContext);
                            break;
                        case R.id.ll_free:
                            IntentUtil.jumpGiftFreeList(mContext);
                            break;
                        case R.id.ll_sign:
                            IntentUtil.jumpSignIn(mContext);
                            break;
                    }
                    break;
            }
        } else {
            switch (v.getId()) {
                case R.id.btn_checkout:
                case R.id.rl_like:
                    IndexGiftLike o = (IndexGiftLike) getItem(pos);
                    IntentUtil.jumpGameDetail(mContext, o.id, GameTypeUtil.JUMP_STATUS_GIFT);
                    Global.getLikeNewTimeArray().put(o.id, o.newestCreateTime);
                    notifyItemChanged(pos);
                    break;
                case R.id.rl_recommend:
                    IndexGiftNew g1 = (IndexGiftNew) getItem(pos);
                    IntentUtil.jumpGiftDetail(mContext, g1.id);
                    break;
                case R.id.btn_send:
                    IndexGiftNew g2 = (IndexGiftNew) getItem(pos);
                    PayManager.getInstance().seizeGift((FragmentActivity) mContext, g2, (GiftButton) v);
                    break;
            }
        }
    }

    @Override
    public void release() {
        mInflater = null;
        super.release();
        mData = null;
        mBannerData = null;
        mBannerHolderCreator = null;
        mBannerWR = null;
    }

    static class BannerVH extends BaseRVHolder {

        ConvenientBanner mBanner;

        public BannerVH(View itemView) {
            super(itemView);
            mBanner = getViewById(R.id.banner);
        }
    }

    static class HeaderVH extends BaseRVHolder {

        TextView tvTitle;
        TextView tvMore;
        RelativeLayout rlItem;

        public HeaderVH(View itemView) {
            super(itemView);
            tvTitle = getViewById(R.id.tv_title);
            tvMore = getViewById(R.id.tv_more);
            rlItem = getViewById(R.id.rl_header_item);
        }
    }

    static class IconBarVh extends BaseRVHolder {

        LinearLayout llLimit;
        ImageView ivLimit;
        LinearLayout llFree;
        ImageView ivFree;
        LinearLayout llNew;
        ImageView ivNew;
        LinearLayout llSign;
        ImageView ivSign;

        public IconBarVh(View itemView) {
            super(itemView);
            llLimit = getViewById(R.id.ll_limit);
            ivLimit = getViewById(R.id.iv_limit);
            llFree = getViewById(R.id.ll_free);
            ivFree = getViewById(R.id.iv_free);
            llNew = getViewById(R.id.ll_new);
            ivNew = getViewById(R.id.iv_new);
            llSign = getViewById(R.id.ll_sign);
            ivSign = getViewById(R.id.iv_sign);
        }
    }

    static class ItemLikeVH extends BaseRVHolder {
        ImageView ivHint;
        TextView tvName;
        TextView tvSize;
        TextView tvCount;
        TextView tvNewAdd;
        ImageView ivIcon;
        TextView btnCheckout;

        public ItemLikeVH(View itemView) {
            super(itemView);
            ivHint = getViewById(R.id.iv_hint);
            tvName = getViewById(R.id.tv_name);
            tvSize = getViewById(R.id.tv_size);
            tvCount = getViewById(R.id.tv_count);
            tvNewAdd = getViewById(R.id.tv_new_add);
            ivIcon = getViewById(R.id.iv_icon);
            btnCheckout = getViewById(R.id.btn_checkout);
        }
    }
}
