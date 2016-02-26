package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.adapter.other.DividerItemDecoration;
import com.oplay.giftcool.adapter.other.GameSuperLinearLayoutManager;
import com.oplay.giftcool.adapter.other.HeaderFooterDividerItemDecoration;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.BannerTypeUtil;
import com.oplay.giftcool.config.GameTypeUtil;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.IndexTypeUtil;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.download.listener.OnDownloadStatusChangeListener;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.model.AppStatus;
import com.oplay.giftcool.model.NetworkImageHolderView;
import com.oplay.giftcool.model.data.resp.GameDownloadInfo;
import com.oplay.giftcool.model.data.resp.IndexBanner;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.model.data.resp.IndexGameSuper;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ViewUtil;
import com.socks.library.KLog;

import net.youmi.android.libs.common.util.Util_System_Runtime;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by zsigui on 16-1-15.
 */
public class GameSuperAdapter extends RecyclerView.Adapter implements OnDownloadStatusChangeListener, com.bigkoo
		.convenientbanner.listener.OnItemClickListener {

	private IndexGameSuper mData;
	private Context mContext;
	private LayoutInflater mInflater;

	private WeakReference<BannerVH> mBannerWR;
	private GameHotVH mGameHotVH;
	private RecommendVH mRecommendVH;
	private GameNewVH mGameNewVH;

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
					if (!AppStatus.DISABLE.equals(mData.recommend.appStatus)) {
						mData.recommend.handleOnClick(((FragmentActivity) mContext).getSupportFragmentManager());
					}
					break;
			}
		}
	};

	private OnItemClickListener<IndexGameNew> mItemClickListener = new OnItemClickListener<IndexGameNew>
			() {
		@Override
		public void onItemClick(IndexGameNew item, View view, int position) {
			try {
				// !!! 此处由于点击事件被应用于多处，故 position 不准备，勿用
				if (view.getId() == R.id.tv_download && !AppStatus.DISABLE.equals(item.appStatus)) {
					item.handleOnClick(((FragmentActivity) mContext).getSupportFragmentManager());
				} else {
					IntentUtil.jumpGameDetail(mContext, item.id, GameTypeUtil.JUMP_STATUS_DETAIL);
				}
			} catch (Throwable e) {
				if (AppDebugConfig.IS_DEBUG) {
					KLog.e(e);
				}
			}
		}
	};
	private View.OnTouchListener mTouchListener;

	public GameSuperAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		ApkDownloadManager.getInstance(mContext).addDownloadStatusListener(this);
	}

	public void setData(IndexGameSuper data) {
		mData = data;
	}

	public void setTouchListener(View.OnTouchListener touchListener) {
		mTouchListener = touchListener;
	}

	private void updateBanners(BannerVH bannerVH, ArrayList<IndexBanner> banners) {
		if (banners == null || bannerVH == null || mData == null) {
			return;
		}
		if (banners.size() == 0) {
			IndexBanner banner = new IndexBanner();
			banner.url = "drawable://" + R.drawable.ic_banner_empty_default;
			banner.type = BannerTypeUtil.ACTION_SCORE_TASK;
			banners.add(banner);
		}
		mData.banner = banners;
		ArrayList<String> data = new ArrayList<>();
		for (IndexBanner banner : banners) {
			data.add(banner.url);
		}
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				Global.getBannerHeight(mContext));
		bannerVH.mBanner.setLayoutParams(lp);
		bannerVH.mBanner.setPages(new CBViewHolderCreator<NetworkImageHolderView>() {

			@Override
			public NetworkImageHolderView createHolder() {
				return new NetworkImageHolderView();
			}
		}, data)
				.setPageIndicator(new int[]{R.drawable.ic_banner_point_normal, R.drawable.ic_banner_point_selected})
				.setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL)
				.setOnItemClickListener(this);
		if (data.size() == 1) {
			bannerVH.mBanner.setCanLoop(false);
			bannerVH.mBanner.stopTurning();
		} else {
			bannerVH.mBanner.setCanLoop(true);
			//mBannerVH.mBanner.getViewPager().setPageTransformer(true, new CubePageTransformer());
			bannerVH.mBanner.startTurning(AppConfig.BANNER_LOOP_TIME);
		}
	}

	public void updateHotData(ArrayList<IndexGameNew> data) {
		if (data == null || mGameHotVH == null || mData == null) {
			return;
		}
		mData.hot = data;
		mGameHotVH.adapter.updateData(mData.hot);
	}

	public void updateRecommendData(IndexGameNew data) {
		if (data == null || mRecommendVH == null || mData == null) {
			return;
		}
		mData.recommend = data;
		data.initAppInfoStatus(mContext);
		ViewUtil.showBannerImage(mRecommendVH.ivBanner, data.banner);
		ViewUtil.showImage(mRecommendVH.ivIcon, data.img);
		mRecommendVH.tvName.setText(data.name);
		mRecommendVH.tvSize.setText(data.size);
		ViewUtil.initDownloadBtnStatus(mRecommendVH.btnDownload, data.appStatus);
		mRecommendVH.itemView.setOnClickListener(mRecItemClickListener);
		mRecommendVH.btnDownload.setOnClickListener(mRecItemClickListener);
	}

	public void updateNewData(ArrayList<IndexGameNew> data) {
		if (data == null || mGameNewVH == null || mData == null) {
			return;
		}
		mData.news = data;
		mGameNewVH.adapter.updateData(mData.news);
	}

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
			case IndexTypeUtil.ITEM_BANNER:
				if (mBannerWR == null || mBannerWR.get() == null) {
					mBannerWR =  new WeakReference<BannerVH>(new BannerVH(mInflater.inflate(R.layout.view_banner, parent, false)));
				}
				return mBannerWR.get();
			case IndexTypeUtil.ITEM_HOT:
				if (mGameHotVH == null) {
					mGameHotVH = new GameHotVH(mInflater.inflate(R.layout.view_game_hot, parent, false));
					mGameHotVH.rvContainer.setLayoutManager(new GridLayoutManager(mContext, 4));
					mGameHotVH.rvContainer.addItemDecoration(new HeaderFooterDividerItemDecoration(mContext, LinearLayoutManager
							.VERTICAL));
					mGameHotVH.adapter = new IndexGameHotWithTitleAdapter(mContext);
					mGameHotVH.rvContainer.setAdapter(mGameHotVH.adapter);
				}
				return mGameHotVH;
			case IndexTypeUtil.ITEM_RECOMMEND:
				if (mRecommendVH == null) {
					mRecommendVH = new RecommendVH(mInflater.inflate(R.layout.view_game_recommend, parent, false));
				}
				return mRecommendVH;
			case IndexTypeUtil.ITEM_NEW:
				if (mGameNewVH == null) {
					mGameNewVH = new GameNewVH(mInflater.inflate(R.layout.view_game_new, parent, false));
					mGameNewVH.rvContainer.setLayoutManager(new GameSuperLinearLayoutManager(mGameNewVH.rvContainer));
					mGameNewVH.rvContainer.addItemDecoration(new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL));

					mGameNewVH.adapter = new IndexGameNewWithTitleAdapter(mContext);
					mGameNewVH.rvContainer.setAdapter(mGameNewVH.adapter);
				}
				return mGameNewVH;
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
			case IndexTypeUtil.ITEM_BANNER:

				BannerVH bannerVH = (BannerVH) holder;
				if (mTouchListener != null)
				bannerVH.mBanner.setOnTouchListener(mTouchListener);
				updateBanners(bannerVH, mData.banner);
				break;
			case IndexTypeUtil.ITEM_HOT:
				mGameHotVH = (GameHotVH) holder;
				mGameHotVH.adapter.setListener(mItemClickListener);
				updateHotData(mData.hot);
				break;
			case IndexTypeUtil.ITEM_RECOMMEND:
				mRecommendVH = (RecommendVH) holder;
				updateRecommendData(mData.recommend);
				break;
			case IndexTypeUtil.ITEM_NEW:
				mGameNewVH = (GameNewVH) holder;
				mGameNewVH.adapter.setListener(mItemClickListener);
				updateNewData(mData.news);
				break;
		}
	}

	@Override
	public int getItemCount() {
		return mData == null ? 0 : IndexTypeUtil.ITEM_INDEX_COUNT;
	}


	@Override
	public int getItemViewType(int position) {
		switch (position) {
			case 0:
				return IndexTypeUtil.ITEM_BANNER;
			case 1:
				return IndexTypeUtil.ITEM_HOT;
			case 2:
				return IndexTypeUtil.ITEM_RECOMMEND;
			case 3:
				return IndexTypeUtil.ITEM_NEW;
			default:
				throw new IllegalArgumentException("bad position in View : " + position);
		}
	}

	@Override
	public void onDownloadStatusChanged(final GameDownloadInfo appInfo) {
		if (mData == null || mData.recommend == null) {
			return;
		}
		if (appInfo.packageName.equals(mData.recommend.packageName)) {
			mData.recommend.downloadStatus = appInfo.downloadStatus;
			mData.recommend.initAppInfoStatus(mContext);
			if (mRecommendVH != null) {
				Util_System_Runtime.getInstance().runInUiThread(new Runnable() {
					@Override
					public void run() {
						ViewUtil.initDownloadBtnStatus(mRecommendVH.btnDownload, appInfo.appStatus);
					}
				});
			}
		}
	}

	@Override
	public void onItemClick(int position) {
		if (mData == null || mData.banner == null || mData.banner.size() <= position) {
			return;
		}
		IndexBanner banner = mData.banner.get(position);
		AppDebugConfig.trace(mContext, "游戏首页推荐位", String.format("第%d推广位，标题：%s", position, banner.title));
		BannerTypeUtil.handleBanner(mContext, banner);
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

}
