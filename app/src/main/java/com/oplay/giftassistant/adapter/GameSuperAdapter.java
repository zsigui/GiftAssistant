package com.oplay.giftassistant.adapter;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.other.DividerItemDecoration;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.listener.OnItemClickListener;
import com.oplay.giftassistant.model.AppStatus;
import com.oplay.giftassistant.model.data.resp.IndexGameBanner;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;
import com.oplay.giftassistant.model.data.resp.IndexGameSuper;
import com.oplay.giftassistant.ui.widget.NestedListView;
import com.oplay.giftassistant.util.IntentUtil;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.bgabanner.BGABanner;

/**
 * Created by zsigui on 16-1-15.
 */
public class GameSuperAdapter extends RecyclerView.Adapter{

	private IndexGameSuper mData;

	public static  final int ITEM_COUNT = 4;

	public static  final int ITEM_BANNER = 0;
	public static final int ITEM_HOT = 1;
	public static final int ITEM_RECOMMEND = 2;
	public static final int ITEM_NEW = 3;


	private Context mContext;

	private List<View> views;
	private IndexGameNew mRecommendData;

	private NestedScrollView mScrollView;

	// 热门手游
	private RelativeLayout mHotBar;
	private RecyclerView mHotView;

	// 主推游戏
	private RelativeLayout mRecommendItem;
	private ImageView mRecommendView;
	private ImageView mRecommendIcon;
	private TextView mRecommendName;
	private TextView mRecommendSize;
	private TextView mRecommendDownload;

	// 新游推荐
	private RelativeLayout mNewBar;
	private NestedListView mNewView;


	private IndexGameHotAdapter mHotAdapter;
	private IndexGameNewAdapter mNewAdapter;

	private LayoutInflater mInflater;

	public void updateBanners(BGABanner bannerView, ArrayList<IndexGameBanner> banners) {
		if (banners == null) {
			return;
		}
		ArrayList<View> vs = new ArrayList<>(banners.size());
		for (int i = 0; i < banners.size(); i++) {
			View v = LayoutInflater.from(mContext).inflate(R.layout.view_banner_img, null);
			vs.add(v);
			final IndexGameBanner banner = banners.get(i);
			ImageLoader.getInstance().displayImage(banner.url, (ImageView) v);
			v.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					judgeBanner(banner);
				}
			});
		}
		bannerView.setViews(vs);
	}

	/**
	 * 判断处理 Banner 跳转事件
	 * @param banner
	 */
	private void judgeBanner(IndexGameBanner banner) {

	}

	public void updateHotData(ArrayList<IndexGameNew> data) {
		if (data == null) {
			return;
		}
		if (mHotAdapter == null) {
			mHotAdapter = new IndexGameHotAdapter(mHotView);
			mHotView.setAdapter(mHotAdapter);
		}
		mHotAdapter.updateData(data);
	}

	public void updateRecommendData(IndexGameNew data) {
		if (data == null) {
			return;
		}
		mRecommendData = data;
		ImageLoader.getInstance().displayImage(data.banner, mRecommendView, Global.IMAGE_OPTIONS);
		ImageLoader.getInstance().displayImage(data.img, mRecommendIcon, Global.IMAGE_OPTIONS);
		mRecommendName.setText(data.name);
		mRecommendSize.setText(data.size);
	}

	public void updateNewData(ArrayList<IndexGameNew> data) {
		if (data == null) {
			return;
		}
		if (mNewAdapter == null) {
			//mNewAdapter = new IndexGameNewAdapter(getContext(), this);
			mNewView.setAdapter(mNewAdapter);
		}
		mNewAdapter.updateData(data);
	}

	public void updateData(IndexGameSuper data) {
		updateHotData(data.hot);
		updateRecommendData(data.recommend);
		updateNewData(data.news);
		if (mScrollView != null) {
			mScrollView.smoothScrollTo(0, 0);
		}
		//mViewManager.showContent();
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
			case ITEM_BANNER:
				return new BannerVH(mInflater.inflate(R.layout.view_banner, parent, false));
			case ITEM_HOT:
				return new GameHotVH(mInflater.inflate(R.layout.view_recycle_view, parent, false));
			case ITEM_RECOMMEND:
				return new RecommendVH(mInflater.inflate(R.layout.view_game_recommend, parent, false));
			case ITEM_NEW:
				return new GameNewVH(mInflater.inflate(R.layout.view_recycle_view, parent, false));
			default:
				throw new IllegalArgumentException("bad viewType : " + viewType);
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (getItemCount() == 0)
			return;
		switch (getItemViewType(position)) {
			case ITEM_BANNER:
				BannerVH holderVH = (BannerVH) holder;
				updateBanners(holderVH.mBanner, mData.banner);
				break;
			case ITEM_HOT:
				GameHotVH hotVH = (GameHotVH) holder;

				break;
			case ITEM_RECOMMEND:
				break;
			case ITEM_NEW:

				break;
		}
	}

	@Override
	public int getItemCount() {
		return mData == null? 0 : ITEM_COUNT;
	}


	@Override
	public int getItemViewType(int position) {
		switch (position) {
			case 0:
				return ITEM_BANNER;
			case 1:
				return ITEM_HOT;
			case 2:
				return ITEM_RECOMMEND;
			case 3:
				return ITEM_NEW;
			default:
				throw new IllegalArgumentException("bad position in View : " + position);
		}
	}

	public static  <V extends View> V getViewById(View v, @IdRes int id) {
		View child = v.findViewById(id);
		return (child != null ? (V)child : null);
	}

	class BannerVH extends RecyclerView.ViewHolder {

		BGABanner mBanner;

		public BannerVH(View itemView) {
			super(itemView);
			mBanner = getViewById(itemView, R.id.banner);
		}
	}

	class GameHotVH extends RecyclerView.ViewHolder {

		RecyclerView rvContainer;
		IndexGameHotWithTitleAdapter apapter;

		public GameHotVH(View itemView) {
			super(itemView);
			rvContainer = getViewById(itemView, R.id.rv_container);
			apapter = new IndexGameHotWithTitleAdapter(rvContainer);
			rvContainer.setAdapter(apapter);
		}
	}

	class RecommendVH extends RecyclerView.ViewHolder {

		ImageView ivBanner;
		ImageView ivIcon;
		TextView tvName;
		TextView tvSize;
		TextView tvDownload;

		public RecommendVH(View itemView) {
			super(itemView);

			ivBanner = getViewById(itemView, R.id.iv_big_pic);
			ivIcon = getViewById(itemView, R.id.iv_icon);
			tvName = getViewById(itemView, R.id.tv_name);
			tvSize = getViewById(itemView, R.id.tv_size);
			tvDownload = getViewById(itemView, R.id.tv_download);
		}
	}

	class GameNewVH extends RecyclerView.ViewHolder {

		RecyclerView rvContainer;
		IndexGameNewWithTitleAdapter adpter;

		public GameNewVH(View itemView) {
			super(itemView);
			rvContainer = getViewById(itemView, R.id.rv_container);
			rvContainer.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
			rvContainer.addItemDecoration(new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL));
			adpter = new IndexGameNewWithTitleAdapter(rvContainer, new OnItemClickListener<IndexGameNew>() {
				@Override
				public void onItemClick(IndexGameNew item, View view, int position) {
					try {
						if (view.getId() == R.id.tv_download && !AppStatus.DISABLE.equals(item.appStatus)) {
							item.handleOnClick(((FragmentActivity)mContext).getSupportFragmentManager());
						} else {
							//TODO 跳转游戏详情页
							IntentUtil.jumpGameDetail(mContext, item.id, item.name);
						}
					} catch (Throwable e) {
						if (AppDebugConfig.IS_DEBUG) {
							KLog.e(e);
						}
					}
				}
			});
			rvContainer.setAdapter(adpter);
		}
	}
}
