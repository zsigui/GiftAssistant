package com.oplay.giftassistant.adapter;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.model.data.resp.IndexGameBanner;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;
import com.oplay.giftassistant.model.data.resp.IndexGameSuper;
import com.oplay.giftassistant.ui.widget.NestedListView;

import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.bgabanner.BGABanner;

/**
 * Created by zsigui on 16-1-15.
 */
public class GameSuperAdapter extends RecyclerView.Adapter{

	private IndexGameSuper mData;
	private Context mContext;

	private List<View> views;
	private IndexGameNew mRecommendData;

	private NestedScrollView mScrollView;
	// 活动图，5张
	private BGABanner mBanner;

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


	public void updateBanners(ArrayList<IndexGameBanner> banners) {
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
		mBanner.setViews(vs);
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
		updateBanners(data.banner);
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
		return null;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

	}

	@Override
	public int getItemCount() {
		return 0;
	}

	class BannerVH extends RecyclerView.ViewHolder {

		public BannerVH(View itemView) {
			super(itemView);
		}
	}
}
