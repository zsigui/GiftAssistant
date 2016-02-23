package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.BannerTypeUtil;
import com.oplay.giftcool.config.GiftTypeUtil;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.model.NetworkImageHolderView;
import com.oplay.giftcool.model.data.resp.IndexBanner;
import com.oplay.giftcool.model.data.resp.IndexGift;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.ui.widget.button.GiftButton;

import java.util.ArrayList;

/**
 * Created by zsigui on 16-2-23.
 */
public class GiftAdapter extends RecyclerView.Adapter implements com.bigkoo.convenientbanner.listener.OnItemClickListener, View.OnClickListener {

	public static final int TYPE_DEFAULT = 110;
	public static final int TYPE_BANNER = TYPE_DEFAULT + 1;
	public static final int TYPE_ZERO = TYPE_DEFAULT + 2;
	public static final int TYPE_LIKE = TYPE_DEFAULT + 3;
	public static final int TYPE_LIMIT = TYPE_DEFAULT + 4;
	public static final int TYPE_NEW_HEAD = TYPE_DEFAULT + 5;

	private IndexGift mData;
	private OnItemClickListener<IndexGiftNew> mListener;
	private Context mContext;
	private LayoutInflater mInflater;

	public GiftAdapter(Context context) {
		mContext = (context == null ? AssistantApp.getInstance().getApplicationContext() : context
				.getApplicationContext());
		mInflater = LayoutInflater.from(mContext);
	}

	public void setData(IndexGift data) {
		mData = data;
	}

	public void setListener(OnItemClickListener<IndexGiftNew> listener) {
		mListener = listener;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
			case TYPE_BANNER:
				return new BannerVH(mInflater.inflate(R.layout.view_banner, parent, false));
			case TYPE_ZERO:
				ZeroVH zeroVH = new ZeroVH(mInflater.inflate(R.layout.view_gift_index_zero, parent, false));
				LinearLayoutManager llmZero = new LinearLayoutManager(mContext);
				llmZero.setOrientation(LinearLayoutManager.HORIZONTAL);
				zeroVH.rvContainer.setLayoutManager(llmZero);
				zeroVH.rvAdapter = new IndexGiftZeroAdapter(mContext);
				// 加载数据
				zeroVH.rvContainer.setAdapter(zeroVH.rvAdapter);
				return zeroVH;
			case TYPE_LIKE:
				LikeVH likeVH = new LikeVH(mInflater.inflate(R.layout.view_gift_index_like, parent, false));
				LinearLayoutManager llmLike = new LinearLayoutManager(mContext);
				llmLike.setOrientation(LinearLayoutManager.HORIZONTAL);
				likeVH.rvContainer.setLayoutManager(llmLike);
				likeVH.rvAdapter = new IndexGiftLikeAdapter(likeVH.rvContainer);
				// 加载数据
				likeVH.rvContainer.setAdapter(likeVH.rvAdapter);
				return likeVH;
			case TYPE_LIMIT:
				LimitVH limitVH = new LimitVH(mInflater.inflate(R.layout.view_gift_index_limit, parent, false));
				LinearLayoutManager llmLimit = new LinearLayoutManager(mContext);
				llmLimit.setOrientation(LinearLayoutManager.HORIZONTAL);
				limitVH.rvContainer.setLayoutManager(llmLimit);
				limitVH.rvAdapter = new IndexGiftLimitAdapter(limitVH.rvContainer);
				// 加载数据
				limitVH.rvContainer.setAdapter(limitVH.rvAdapter);
				return limitVH;
			case TYPE_NEW_HEAD:
				ItemTitleVH titleVH = new ItemTitleVH(mInflater.inflate(R.layout.view_gift_index_item_title, parent,
						false));
				return titleVH;
			case GiftTypeUtil.TYPE_NORMAL_SEIZE:
				ItemHolder normalItemVH = new ItemHolder(mInflater.inflate(R.layout.item_index_gift_new_normal,
						parent, false));
				return normalItemVH;
			case GiftTypeUtil.TYPE_LIMIT_SEIZE:
			case GiftTypeUtil.TYPE_ZERO_SEIZE:
				ItemHolder limitItemVH = new ItemHolder(mInflater.inflate(R.layout.item_index_gift_new_limit, parent,
						false));
				return limitItemVH;
			default:
				ItemHolder disabledItemVH = new ItemHolder(mInflater.inflate(R.layout.item_index_gift_new_disabled,
						parent, false));
				return disabledItemVH;
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		int type = getItemViewType(position);
		switch (type) {
			case TYPE_BANNER:
				updateBanners((BannerVH) holder, mData.banner);
				break;
			case TYPE_ZERO:
				ZeroVH zeroVH = ((ZeroVH) holder);
				zeroVH.rvAdapter.updateData(mData.zero);
				zeroVH.tvSubTitle.setText(Html.fromHtml("(每天<font " +
						"color='#f85454'>20:00</font>开抢3款)"));
				break;
			case TYPE_LIKE:
				LikeVH likeVH = ((LikeVH) holder);
				likeVH.rlTitle.setOnClickListener(this);
				likeVH.rvAdapter.updateData(mData.like);
				break;
			case TYPE_LIMIT:
				LimitVH limitVH = ((LimitVH) holder);
				limitVH.rvAdapter.updateData(mData.limit);
				limitVH.tvSubTitle.setText(Html.fromHtml("(每天<font " +
						"color='#f85454'>20:00</font>开抢10款)"));
				limitVH.rlTitle.setOnClickListener(this);
				break;
			case TYPE_NEW_HEAD:
				break;
			default:
		}
	}

	public void updateBanners(BannerVH bannerVH, ArrayList<IndexBanner> banners) {
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
		} else {
			bannerVH.mBanner.setCanLoop(true);
			bannerVH.mBanner.setScrollDuration(500);
			//mBannerVH.mBanner.getViewPager().setPageTransformer(true, new CubePageTransformer());
			bannerVH.mBanner.startTurning(5000);
		}

	}

	@Override
	public int getItemCount() {
		if (mData == null) {
			return 0;
		} else if (mData.news == null) {
			return 5;
		} else {
			return 5 + mData.news.size();
		}
	}

	@Override
	public int getItemViewType(int position) {
		if (position > 5) {
			return GiftTypeUtil.getItemViewType(mData.zero.get(position - 5));
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
		AppDebugConfig.trace(mContext, "礼包首页推荐位", String.format("第%d推广位，标题：%s", position, banner.title));
		BannerTypeUtil.handleBanner(mContext, banner);
	}

	@Override
	public void onClick(View v) {

	}

	static class BannerVH extends BaseRVHolder {

		ConvenientBanner mBanner;

		public BannerVH(View itemView) {
			super(itemView);
			mBanner = getViewById(R.id.banner);
		}
	}

	static class ZeroVH extends BaseRVHolder {

		TextView tvSubTitle;
		RecyclerView rvContainer;
		IndexGiftZeroAdapter rvAdapter;

		public ZeroVH(View itemView) {
			super(itemView);
			tvSubTitle = getViewById(R.id.tv_zero_limit);
			rvContainer = getViewById(R.id.rv_zero_content);
		}
	}

	static class LikeVH extends BaseRVHolder {

		LinearLayout rlTitle;
		RecyclerView rvContainer;
		IndexGiftLikeAdapter rvAdapter;

		public LikeVH(View itemView) {
			super(itemView);
			rlTitle = getViewById(R.id.rl_hot_all);
			rvContainer = getViewById(R.id.rv_like_content);
		}
	}

	static class LimitVH extends BaseRVHolder {

		LinearLayout rlTitle;
		TextView tvSubTitle;
		RecyclerView rvContainer;
		IndexGiftLimitAdapter rvAdapter;

		public LimitVH(View itemView) {
			super(itemView);
			rlTitle = getViewById(R.id.rl_limit_all);
			tvSubTitle = getViewById(R.id.tv_limit_hint);
			rvContainer = getViewById(R.id.rv_like_content);

		}
	}

	static class ItemTitleVH extends BaseRVHolder {

		public ItemTitleVH(View itemView) {
			super(itemView);
		}
	}

	static class ItemHolder extends BaseRVHolder {
		ImageView ivIcon;
		ImageView ivLimit;
		TextView tvTitle;
		TextView tvContent;
		GiftButton btnSend;
		TextView tvScore;
		TextView tvOr;
		TextView tvBean;
		TextView tvRemain;
		TextView tvCount;
		TextView tvPercent;
		ProgressBar pbPercent;

		public ItemHolder(View itemView) {
			super(itemView);
			ivIcon = getViewById(R.id.iv_icon);
			ivLimit = getViewById(R.id.iv_limit);
			tvTitle = getViewById(R.id.tv_title);
			tvContent = getViewById(R.id.tv_content);
			btnSend = getViewById(R.id.btn_send);
			tvScore = getViewById(R.id.tv_score);
			tvOr = getViewById(R.id.tv_or);
			tvBean = getViewById(R.id.tv_bean);
			tvRemain = getViewById(R.id.tv_remain);
			tvCount = getViewById(R.id.tv_count);
			tvPercent = getViewById(R.id.tv_percent);
			pbPercent = getViewById(R.id.pb_percent);
		}
	}
}
