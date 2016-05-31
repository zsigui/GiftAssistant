package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter_Download;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.adapter.itemdecoration.HeaderFooterDividerItemDecoration;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.util.BannerTypeUtil;
import com.oplay.giftcool.config.util.GameTypeUtil;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.util.IndexTypeUtil;
import com.oplay.giftcool.download.listener.OnDownloadStatusChangeListener;
import com.oplay.giftcool.ext.holder.BannerHolderCreator;
import com.oplay.giftcool.manager.StatisticsManager;
import com.oplay.giftcool.model.AppStatus;
import com.oplay.giftcool.model.DownloadStatus;
import com.oplay.giftcool.model.data.resp.GameDownloadInfo;
import com.oplay.giftcool.model.data.resp.IndexBanner;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.model.data.resp.IndexGameSuper;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ThreadUtil;
import com.oplay.giftcool.util.ViewUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by zsigui on 16-1-15.
 */
public class GameSuperAdapter extends BaseRVAdapter_Download implements OnDownloadStatusChangeListener,
        OnItemClickListener {

    private IndexGameSuper mData;
    private LayoutInflater mInflater;


    // banner
    private WeakReference<BannerVH> mBannerWR;
    private IndexBanner mDefaultBanner;
    private BannerHolderCreator mBannerHolderCreator;
    private int[] mBannerIndicatorDrawable;
    private ArrayList<String> mBannerData;
    private RecommendVH mRecommendVH;


    public GameSuperAdapter(Context context) {
        super(context);
        mInflater = LayoutInflater.from(mContext);
        initDefaultBannerConfig();
    }

    public void setData(IndexGameSuper data) {
        mData = data;
    }

    private void initDefaultBannerConfig() {
        mBannerHolderCreator = new BannerHolderCreator();
        mBannerIndicatorDrawable = new int[]{R.drawable.ic_banner_point_normal, R.drawable.ic_banner_point_selected};
        mDefaultBanner = new IndexBanner();
        mDefaultBanner.url = "drawable://" + R.drawable.ic_banner_empty_default;
        mDefaultBanner.type = BannerTypeUtil.ACTION_SCORE_TASK;
        mBannerData = new ArrayList<>();
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

    public void updateHotData(GameHotVH gameHotVH, ArrayList<IndexGameNew> data) {
        if (data == null || gameHotVH == null || mData == null) {
            return;
        }
        mData.hot = data;
        gameHotVH.adapter.updateData(mData.hot);
    }

    private View.OnClickListener mRecItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mData == null || mData.recommend == null) {
                return;
            }
            switch (v.getId()) {
                case R.id.rl_recommend:
                    IntentUtil.jumpGameDetail(mContext, mData.recommend.id, GameTypeUtil.JUMP_STATUS_DETAIL);
                    break;
                case R.id.tv_download:
                    if (mContext != null && mContext instanceof FragmentActivity
                            && !AppStatus.DISABLE.equals(mData.recommend.appStatus)) {
                        mData.recommend.handleOnClick(((FragmentActivity) mContext).getSupportFragmentManager());
                    }
                    break;
            }
        }
    };

    public void updateRecommendData(RecommendVH recommendVH, IndexGameNew data) {
        if (data == null || recommendVH == null || mData == null) {
            return;
        }
        mData.recommend = data;
        data.initAppInfoStatus(mContext);
        ViewUtil.showBannerImage(recommendVH.ivBanner, data.banner);
        ViewUtil.showImage(recommendVH.ivIcon, data.img);
        recommendVH.tvName.setText(data.name);
        recommendVH.tvSize.setText(data.size);
        ViewUtil.initDownloadBtnStatus(recommendVH.btnDownload, data.appStatus);
        recommendVH.itemView.setOnClickListener(mRecItemClickListener);
        recommendVH.btnDownload.setOnClickListener(mRecItemClickListener);
    }

	/*public void updateNewData(ArrayList<IndexGameNew> data) {
        if (data == null || mGameNewVH == null || mData == null) {
			return;
		}
		mData.news = data;
		mGameNewVH.adapter.updateData(mData.news);
	}*/

    public void updateData(IndexGameSuper data) {
        if (data == null) {
            return;
        }
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case IndexTypeUtil.ITEM_GAME_BANNER:
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
            case IndexTypeUtil.ITEM_GAME_HOT:
                GameHotVH gameHotVH = new GameHotVH(mInflater.inflate(R.layout.view_game_hot, parent, false));
                gameHotVH.rvContainer.setLayoutManager(new GridLayoutManager(mContext, 4));
                gameHotVH.rvContainer.addItemDecoration(new HeaderFooterDividerItemDecoration(mContext,
                        LinearLayoutManager
                                .VERTICAL));
                gameHotVH.adapter = new IndexGameHotWithTitleAdapter(mContext);
                gameHotVH.rvContainer.setAdapter(gameHotVH.adapter);
                return gameHotVH;
            case IndexTypeUtil.ITEM_GAME_RECOMMEND:
                if (mRecommendVH == null) {
                    mRecommendVH = new RecommendVH(mInflater.inflate(R.layout.view_game_recommend, parent, false));
                }
                return mRecommendVH;
            case IndexTypeUtil.ITEM_GAME_TITLE:
                return new HeaderVH(mInflater.inflate(R.layout.item_header_index, parent, false));
            case IndexTypeUtil.ITEM_GAME_NEW_NORMAL:
                NormalVH normalVH = new NormalVH(mInflater.inflate(R.layout.item_index_game_new, parent, false));
                normalVH.itemView.setBackgroundResource(R.drawable.selector_white_module);
                return normalVH;
            default:
                throw new IllegalArgumentException("bad viewType : " + viewType);
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

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemCount() == 0 || holder == null)
            return;
        switch (getItemViewType(position)) {
            case IndexTypeUtil.ITEM_GAME_BANNER:
                updateBanners((BannerVH) holder);
                break;
            case IndexTypeUtil.ITEM_GAME_HOT:
                updateHotData((GameHotVH) holder, mData.hot);
                break;
            case IndexTypeUtil.ITEM_GAME_RECOMMEND:
                updateRecommendData(mRecommendVH, mData.recommend);
                break;
            case IndexTypeUtil.ITEM_GAME_TITLE:
                HeaderVH headerVH = (HeaderVH) holder;
                headerVH.tvTitle.setText("新游推荐");
                headerVH.itemView.setTag(IndexTypeUtil.TAG_POSITION, position);
                headerVH.itemView.setOnClickListener(this);
                break;
            case IndexTypeUtil.ITEM_GAME_NEW_NORMAL:
                setItemValue(position, (NormalVH) holder);
                break;
        }
    }

    /**
     * 设置基本项的内容
     */
    private void setItemValue(int position, NormalVH normalVH) {
        final IndexGameNew o = getItem(position);
        o.initAppInfoStatus(mContext);
        normalVH.tvName.setText(o.name);
        if (o.playCount < 10000) {
            normalVH.tvPlay.setText(Html.fromHtml(String.format("<font color='#ffaa17'>%d人</font>在玩",
                    o.playCount)));
        } else {
            normalVH.tvPlay.setText(Html.fromHtml(String.format("<font color='#ffaa17'>%.1f万人</font>在玩",
                    (float) o.playCount / 10000)));
        }
        if (o.totalCount > 0) {
            normalVH.ivGift.setVisibility(View.VISIBLE);
            normalVH.tvGift.setText(Html.fromHtml(
                    String.format("<font color='#ffaa17'>%s</font> 等共<font color='#ffaa17'>%d</font>款礼包",
                            o.giftName, o.totalCount)));
        } else {
            normalVH.ivGift.setVisibility(View.GONE);
            normalVH.tvGift.setText("暂时还木有礼包");
        }
        normalVH.tvSize.setText(o.size);
        ViewUtil.showImage(normalVH.ivIcon, o.img);
        ViewUtil.initDownloadBtnStatus(normalVH.btnDownload, o.appStatus);
        normalVH.itemView.setOnClickListener(this);
        normalVH.itemView.setTag(IndexTypeUtil.TAG_POSITION, position);
        normalVH.btnDownload.setTag(IndexTypeUtil.TAG_POSITION, position);
        normalVH.btnDownload.setTag(IndexTypeUtil.TAG_URL, o.downloadUrl);
        normalVH.btnDownload.setOnClickListener(this);

        mPackageNameMap.put(o.packageName, o);
        mUrlDownloadBtn.put(o.downloadUrl, normalVH.btnDownload);
    }

    @Override
    protected int getItemHeaderCount() {
        return IndexTypeUtil.ITEM_GAME_SUPER_HEADER_COUNT;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : IndexTypeUtil.ITEM_GAME_SUPER_HEADER_COUNT + mData.news.size();
    }


    @Override
    protected int getItemTypeCount() {
        return IndexTypeUtil.ITEM_GAME_SUPER_TOTAL_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return IndexTypeUtil.ITEM_GAME_BANNER;
            case 1:
                return IndexTypeUtil.ITEM_GAME_HOT;
            case 2:
                return IndexTypeUtil.ITEM_GAME_RECOMMEND;
            case 3:
                return IndexTypeUtil.ITEM_GAME_TITLE;
            default:
                return IndexTypeUtil.ITEM_GAME_NEW_NORMAL;
        }
    }

    @Override
    public IndexGameNew getItem(int position) {
        return getItemCount() > IndexTypeUtil.ITEM_GAME_SUPER_HEADER_COUNT ?
                mData.news.get(position - IndexTypeUtil.ITEM_GAME_SUPER_HEADER_COUNT) : null;
    }

    @Override
    public void onDownloadStatusChanged(final GameDownloadInfo appInfo) {
        if (appInfo == null || mData == null || mData.recommend == null) {
            return;
        }

        ThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appInfo.packageName.equals(mData.recommend.packageName)) {
                    mData.recommend.downloadStatus = appInfo.downloadStatus;
                    mData.recommend.initAppInfoStatus(mContext);
                    if (mRecommendVH != null) {
                        ViewUtil.initDownloadBtnStatus(mRecommendVH.btnDownload, appInfo.appStatus);
                    }
                } else {
                    final String packageName = appInfo.packageName;
                    final DownloadStatus status = appInfo.downloadStatus;
                    updateViewByPackageName(packageName, status);
                }
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        if (mData == null || mData.banner == null || mData.banner.size() <= position) {
            return;
        }
        IndexBanner banner = mData.banner.get(position);
        StatisticsManager.getInstance().trace(mContext,
                StatisticsManager.ID.GAME_BANNER,
                StatisticsManager.ID.STR_GAME_BANNER,
                String.format("第%d推广位，标题：%s", position, banner.title));
        BannerTypeUtil.handleBanner(mContext, banner);
    }

    @Override
    public void onClick(View v) {
        if (v.getTag(IndexTypeUtil.TAG_POSITION) == null) {
            return;
        }
        Integer pos = (Integer) v.getTag(IndexTypeUtil.TAG_POSITION);
        switch (v.getId()) {
            case R.id.rl_like_all:
                IntentUtil.jumpGameNewList(mContext);
                break;
            case R.id.rl_recommend:
                IntentUtil.jumpGameDetail(mContext, getItem(pos).id, GameTypeUtil.JUMP_STATUS_DETAIL);
                break;
            case R.id.tv_download:
                final IndexGameNew appInfo = getItem(pos);
                if (!AppStatus.DISABLE.equals(appInfo.appStatus)) {
                    appInfo.handleOnClick(((FragmentActivity) mContext).getSupportFragmentManager());
                }
                break;
        }
    }

    static class BannerVH extends BaseRVHolder {

        ConvenientBanner mBanner;

        public BannerVH(View itemView) {
            super(itemView);
            mBanner = getViewById(R.id.banner);
        }
    }

    static class GameHotVH extends BaseRVHolder {

        RecyclerView rvContainer;
        IndexGameHotWithTitleAdapter adapter;

        public GameHotVH(View itemView) {
            super(itemView);
            rvContainer = getViewById(R.id.lv_content);
        }
    }

    static class RecommendVH extends BaseRVHolder {

        ImageView ivBanner;
        ImageView ivIcon;
        TextView tvName;
        TextView tvSize;
        TextView btnDownload;

        public RecommendVH(View itemView) {
            super(itemView);

            ivBanner = getViewById(R.id.iv_big_pic);
            ivIcon = getViewById(R.id.iv_icon);
            tvName = getViewById(R.id.tv_name);
            tvSize = getViewById(R.id.tv_size);
            btnDownload = getViewById(R.id.tv_download);
        }
    }

    static class GameNewVH extends BaseRVHolder {

        RecyclerView rvContainer;
        IndexGameNewWithTitleAdapter adapter;

        public GameNewVH(View itemView) {
            super(itemView);
            rvContainer = getViewById(R.id.lv_content);
        }
    }

    static class HeaderVH extends BaseRVHolder {

        TextView tvTitle;

        public HeaderVH(View itemView) {
            super(itemView);
            tvTitle = getViewById(R.id.tv_title);
        }
    }

    static class NormalVH extends BaseRVHolder {

        TextView tvName;
        ImageView ivGift;
        ImageView ivIcon;
        TextView tvPlay;
        TextView tvSize;
        TextView tvGift;
        TextView btnDownload;

        public NormalVH(View itemView) {
            super(itemView);
            tvName = getViewById(R.id.tv_name);
            ivGift = getViewById(R.id.iv_gift_hint);
            ivIcon = getViewById(R.id.iv_icon);
            tvPlay = getViewById(R.id.tv_content);
            tvSize = getViewById(R.id.tv_size);
            tvGift = getViewById(R.id.tv_gift);
            btnDownload = getViewById(R.id.tv_download);
        }
    }

}
