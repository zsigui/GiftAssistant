package com.oplay.giftassistant.adapter;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.base.BaseRVHolder;
import com.oplay.giftassistant.adapter.other.AutoMeasureGridLayoutManager;
import com.oplay.giftassistant.adapter.other.AutoMeasureLinearLayoutManager;
import com.oplay.giftassistant.adapter.other.DividerItemDecoration;
import com.oplay.giftassistant.adapter.other.HeaderFooterDividerItemDecoration;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.IndexTypeUtil;
import com.oplay.giftassistant.download.ApkDownloadManager;
import com.oplay.giftassistant.download.listener.OnDownloadStatusChangeListener;
import com.oplay.giftassistant.listener.OnItemClickListener;
import com.oplay.giftassistant.model.AppStatus;
import com.oplay.giftassistant.model.data.resp.IndexGameBanner;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;
import com.oplay.giftassistant.model.data.resp.IndexGameSuper;
import com.oplay.giftassistant.util.IntentUtil;
import com.oplay.giftassistant.util.ViewUtil;
import com.socks.library.KLog;

import java.util.ArrayList;

import cn.bingoogolapple.bgabanner.BGABanner;

/**
 * Created by zsigui on 16-1-15.
 */
public class GameSuperAdapter extends RecyclerView.Adapter implements OnDownloadStatusChangeListener {

	private IndexGameSuper mData;
	private Context mContext;
	private LayoutInflater mInflater;

	private BannerVH mBannerVH;
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
					IntentUtil.jumpGameDetail(mContext, mData.recommend.id, mData.recommend.name);
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
					IntentUtil.jumpGameDetail(mContext, item.id, item.name);
				}
			} catch (Throwable e) {
				if (AppDebugConfig.IS_DEBUG) {
					KLog.e(e);
				}
			}
		}
	};

	public GameSuperAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		ApkDownloadManager.getInstance(mContext).addDownloadStatusListener(this);
	}

	public void setData(IndexGameSuper data) {
		mData = data;
	}

	public void updateBanners(ArrayList<IndexGameBanner> banners) {
		if (banners == null || mBannerVH == null || mData == null) {
			return;
		}
		mData.banner = banners;
		ArrayList<View> vs = new ArrayList<>(banners.size());
		for (int i = 0; i < banners.size(); i++) {
			View v = LayoutInflater.from(mContext).inflate(R.layout.view_banner_img, null);
			vs.add(v);
			final IndexGameBanner banner = banners.get(i);
			ImageLoader.getInstance().displayImage(banner.url, (ImageView) v);
			v.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					handleBannerJump(banner);
				}
			});
		}
		mBannerVH.mBanner.setViews(vs);
	}

	/**
	 * 判断处理 Banner 跳转事件
	 *
	 * @param banner
	 */
	private void handleBannerJump(IndexGameBanner banner) {

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
		ImageLoader.getInstance().displayImage(data.banner, mRecommendVH.ivBanner);
		ImageLoader.getInstance().displayImage(data.img, mRecommendVH.ivIcon);
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
				if (mBannerVH != null) {
					return mBannerVH;
				}
				return new BannerVH(mInflater.inflate(R.layout.view_banner, parent, false));
			case IndexTypeUtil.ITEM_HOT:
				if (mGameHotVH != null) {
					return mGameHotVH;
				}
				return new GameHotVH(mInflater.inflate(R.layout.view_game_hot, parent, false));
			case IndexTypeUtil.ITEM_RECOMMEND:
				if (mRecommendVH != null) {
					return mRecommendVH;
				}
				return new RecommendVH(mInflater.inflate(R.layout.view_game_recommend, parent, false));
			case IndexTypeUtil.ITEM_NEW:
				if (mGameNewVH != null) {
					return mGameNewVH;
				}
				return new GameNewVH(mInflater.inflate(R.layout.view_game_hot, parent, false));
			default:
				throw new IllegalArgumentException("bad viewType : " + viewType);
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (getItemCount() == 0)
			return;
		switch (getItemViewType(position)) {
			case IndexTypeUtil.ITEM_BANNER:
				mBannerVH = (BannerVH) holder;
				updateBanners(mData.banner);
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
	public void onDownloadStatusChanged(IndexGameNew appInfo) {
		if (mData == null || mData.recommend == null) {
			return;
		}
		if (appInfo.packageName.equals(mData.recommend.packageName)) {
			mData.recommend.downloadStatus = appInfo.downloadStatus;
			mData.recommend.initAppInfoStatus(mContext);
			if (mRecommendVH != null) {
				ViewUtil.initDownloadBtnStatus(mRecommendVH.btnDownload, appInfo.appStatus);
			}
		}
	}

	class BannerVH extends BaseRVHolder {

		BGABanner mBanner;

		public BannerVH(View itemView) {
			super(itemView);
			mBanner = getViewById(R.id.banner);
		}
	}

	class GameHotVH extends BaseRVHolder {

		RecyclerView rvContainer;
		IndexGameHotWithTitleAdapter adapter;

		public GameHotVH(View itemView) {
			super(itemView);
			rvContainer = getViewById(R.id.rv_content);
			rvContainer.setLayoutManager(new AutoMeasureGridLayoutManager(mContext, 4));
			rvContainer.addItemDecoration(new HeaderFooterDividerItemDecoration(mContext, LinearLayoutManager.VERTICAL));
			adapter = new IndexGameHotWithTitleAdapter(mContext);
			rvContainer.setAdapter(adapter);
		}
	}

	class RecommendVH extends BaseRVHolder {

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

	class GameNewVH extends BaseRVHolder {

		RecyclerView rvContainer;
		IndexGameNewWithTitleAdapter adapter;

		public GameNewVH(View itemView) {
			super(itemView);
			rvContainer = getViewById(R.id.rv_content);
			rvContainer.setLayoutManager(new AutoMeasureLinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
			rvContainer.addItemDecoration(new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL));
			adapter = new IndexGameNewWithTitleAdapter(mContext);
			rvContainer.setAdapter(adapter);
		}
	}
}
