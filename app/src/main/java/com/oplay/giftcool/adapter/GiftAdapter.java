package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.BannerTypeUtil;
import com.oplay.giftcool.config.GiftTypeUtil;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.manager.PayManager;
import com.oplay.giftcool.model.NetworkImageHolderView;
import com.oplay.giftcool.model.data.resp.IndexBanner;
import com.oplay.giftcool.model.data.resp.IndexGift;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ViewUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by zsigui on 16-2-23.
 */
public class GiftAdapter extends RecyclerView.Adapter implements com.bigkoo.convenientbanner.listener
        .OnItemClickListener, View.OnClickListener {

	private static final int TAG_POS = 0x1234FFFF;
	public static final int TYPE_DEFAULT = 110;
	public static final int TYPE_BANNER = TYPE_DEFAULT;
	public static final int TYPE_ZERO = TYPE_DEFAULT + 1;
	public static final int TYPE_LIKE = TYPE_DEFAULT + 2;
	public static final int TYPE_LIMIT = TYPE_DEFAULT + 3;
	public static final int TYPE_NEW_HEAD = TYPE_DEFAULT + 4;
	public static final int TYPE_NEW_ITEM = TYPE_DEFAULT + 5;

	private IndexGift mData;
	private Context mContext;
	private LayoutInflater mInflater;
	private WeakReference<BannerVH> mBannerWR;
	private View.OnTouchListener mTouchListener;

	public GiftAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
	}

	public void setData(IndexGift data) {
		mData = data;
	}

	public void setTouchListener(View.OnTouchListener touchListener) {
		mTouchListener = touchListener;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
			case TYPE_BANNER:
				if (mBannerWR == null || mBannerWR.get() == null) {
					mBannerWR =  new WeakReference<BannerVH>(new BannerVH(mInflater.inflate(R.layout.view_banner, parent, false)));
				}
				return mBannerWR.get();
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
				ItemTitleVH titleVH = new ItemTitleVH(mInflater.inflate(R.layout.view_gift_index_item_title, parent,
						false));
				return titleVH;
			case TYPE_NEW_ITEM:
				ItemHolder limitItemVH = new ItemHolder(mInflater.inflate(R.layout.item_index_gift_new_list, parent,
						false));
				return limitItemVH;
			default:
				return null;
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
				BannerVH bannerVH = (BannerVH) holder;
				if (mTouchListener != null)
					bannerVH.mBanner.setOnTouchListener(mTouchListener);
				updateBanners(bannerVH, mData.banner);
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
				if (mData.news == null || mData.news.size() == 0
						|| mData.news.size() <= position - 5) {
					return;
				}
				final IndexGiftNew gift = mData.news.get(position - 5);
				type = GiftTypeUtil.getItemViewType(gift);
				ItemHolder viewHolder = (ItemHolder) holder;
				viewHolder.itemView.setBackgroundResource(R.drawable.selector_white_module);
				setData(position, type, gift, viewHolder);
		}
	}

	private void setData(int position, int type, IndexGiftNew gift, ItemHolder viewHolder) {
		setCommonField(viewHolder, gift);
		viewHolder.btnSend.setTag(TAG_POS, position);
		viewHolder.itemView.setTag(TAG_POS, position);
		viewHolder.btnSend.setState(type);
		viewHolder.btnSend.setOnClickListener(this);
		viewHolder.itemView.setOnClickListener(this);
		// 设置数据和按键状态
		if (type == GiftTypeUtil.TYPE_NORMAL_SEIZE
				|| type == GiftTypeUtil.TYPE_LIMIT_SEIZE
				|| type == GiftTypeUtil.TYPE_ZERO_SEIZE) {
			viewHolder.llMoney.setVisibility(View.VISIBLE);
			viewHolder.tvCount.setVisibility(View.GONE);
			viewHolder.tvPercent.setVisibility(View.VISIBLE);
			viewHolder.pbPercent.setVisibility(View.VISIBLE);
			viewHolder.tvScore.setText(String.valueOf(gift.score));
			int percent = gift.remainCount * 100 / gift.totalCount;
			viewHolder.tvPercent.setText(String.format("剩%d%%", percent));
			viewHolder.pbPercent.setProgress(percent);
			if (gift.priceType == GiftTypeUtil.PAY_TYPE_SCORE
					&& gift.giftType != GiftTypeUtil.GIFT_TYPE_ZERO_SEIZE) {
				// 只用积分
				viewHolder.tvScore.setText(String.valueOf(gift.score));
				viewHolder.tvScore.setVisibility(View.VISIBLE);
				viewHolder.tvOr.setVisibility(View.GONE);
				viewHolder.tvBean.setVisibility(View.GONE);
			} else if (gift.priceType == GiftTypeUtil.PAY_TYPE_BEAN) {
				// 只用偶玩豆
				viewHolder.tvBean.setText(String.valueOf(gift.bean));
				viewHolder.tvBean.setVisibility(View.VISIBLE);
				viewHolder.tvOr.setVisibility(View.GONE);
				viewHolder.tvScore.setVisibility(View.GONE);
			} else {
				// 积分 或 偶玩豆
				viewHolder.tvScore.setText(String.valueOf(gift.score));
				viewHolder.tvBean.setText(String.valueOf(gift.bean));
				viewHolder.tvScore.setVisibility(View.VISIBLE);
				viewHolder.tvOr.setVisibility(View.VISIBLE);
				viewHolder.tvBean.setVisibility(View.VISIBLE);
			}
		} else {
			switch (type) {
				case GiftTypeUtil.TYPE_LIMIT_WAIT_SEIZE:
					setDisabledField(viewHolder, View.VISIBLE,
							Html.fromHtml(String.format("开抢时间：<font color='#ffaa17'>%s</font>",
									gift.seizeTime)));
					break;
				case GiftTypeUtil.TYPE_LIMIT_FINISHED:
				case GiftTypeUtil.TYPE_NORMAL_FINISHED:
				case GiftTypeUtil.TYPE_LIMIT_EMPTY:
				case GiftTypeUtil.TYPE_LIMIT_SEIZED:
				case GiftTypeUtil.TYPE_NORMAL_SEIZED:
					setDisabledField(viewHolder, View.INVISIBLE, null);
					break;
				case GiftTypeUtil.TYPE_NORMAL_WAIT_SEIZE:
					setDisabledField(viewHolder, View.VISIBLE,
							Html.fromHtml(String.format("开抢时间：<font color='#ffaa17'>%s</font>",
									gift.seizeTime)));
					break;
				case GiftTypeUtil.TYPE_NORMAL_WAIT_SEARCH:
					setDisabledField(viewHolder, View.VISIBLE,
							Html.fromHtml(String.format("开淘时间：<font color='#ffaa17'>%s</font>",
									gift.searchTime)));
					break;
				case GiftTypeUtil.TYPE_NORMAL_SEARCH:
				case GiftTypeUtil.TYPE_NORMAL_SEARCHED:
					setDisabledField(viewHolder, View.VISIBLE,
							Html.fromHtml(String.format("已淘号：<font color='#ffaa17'>%d</font>次",
									gift.searchCount)));
					break;
			}
		}
	}

	/**
	 * 设置几个类型下的通用配置
	 */
	private void setCommonField(final ItemHolder itemHolder, final IndexGiftNew gift) {
		ViewUtil.showImage(itemHolder.ivIcon, gift.img);
		itemHolder.tvName.setText(String.format("[%s]%s", gift.gameName, gift.name));
		if (gift.giftType == GiftTypeUtil.GIFT_TYPE_LIMIT) {
			itemHolder.ivLimit.setVisibility(View.VISIBLE);
		} else {
			itemHolder.ivLimit.setVisibility(View.GONE);
		}
		if (gift.exclusive == 1) {
			itemHolder.tvName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_exclusive, 0, 0, 0);
		} else {
			itemHolder.tvName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
		itemHolder.tvContent.setText(String.format("%s", gift.content));
	}

	public void setDisabledField(ItemHolder itemHolder, int tvVisibility, Spanned tvText) {
		itemHolder.llMoney.setVisibility(View.GONE);
		itemHolder.pbPercent.setVisibility(View.GONE);
		itemHolder.tvPercent.setVisibility(View.GONE);
		itemHolder.tvCount.setVisibility(tvVisibility);
		itemHolder.tvCount.setText(tvText);
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

	public boolean updateData(IndexGift data) {
		if (data == null) {
			return false;
		}
		mData = data;
		notifyDataSetChanged();
		return true;
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
		if (position >= 5) {
			return TYPE_NEW_ITEM;
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
		IndexGiftNew gift = null;
		if (v.getId() == R.id.rl_recommend
				|| v.getId() == R.id.btn_send) {
			Integer pos = (Integer) v.getTag(TAG_POS);
			if (pos == null || pos < 5 || pos - 5 >= mData.news.size()) {
				return;
			}
			gift = mData.news.get(pos - 5);
		}
		switch (v.getId()) {
			case R.id.rl_hot_all:
				IntentUtil.jumpGiftHotList(mContext, null);
				break;
			case R.id.rl_limit_all:
				IntentUtil.jumpGiftLimitList(mContext);
				break;
			case R.id.rl_recommend:
				if (gift != null) {
					IntentUtil.jumpGiftDetail(mContext, gift.id);
				}
				break;
			case R.id.btn_send:
				if (gift == null)
					return;
				if (gift.giftType == GiftTypeUtil.GIFT_TYPE_ZERO_SEIZE) {
					// 对于0元抢，先跳转到游戏详情
					IntentUtil.jumpGiftDetail(mContext, gift.id);
				} else {
					PayManager.getInstance().seizeGift(mContext, gift, (GiftButton) v);
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

		RelativeLayout rlTitle;
		RecyclerView rvContainer;
		IndexGiftLikeAdapter rvAdapter;

		public LikeVH(View itemView) {
			super(itemView);
			rlTitle = getViewById(R.id.rl_hot_all);
			rvContainer = getViewById(R.id.rv_like_content);
		}
	}

	static class LimitVH extends BaseRVHolder {

		RelativeLayout rlTitle;
		TextView tvSubTitle;
		RecyclerView rvContainer;
		IndexGiftLimitAdapter rvAdapter;

		public LimitVH(View itemView) {
			super(itemView);
			rlTitle = getViewById(R.id.rl_limit_all);
			tvSubTitle = getViewById(R.id.tv_limit_hint);
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
		ImageView ivLimit;
		TextView tvName;
		TextView tvContent;
		GiftButton btnSend;
		TextView tvScore;
		TextView tvOr;
		TextView tvBean;
		TextView tvCount;
		TextView tvPercent;
		LinearLayout llMoney;
		ProgressBar pbPercent;

		public ItemHolder(View itemView) {
			super(itemView);
			ivIcon = getViewById(R.id.iv_icon);
			ivLimit = getViewById(R.id.iv_limit);
			tvName = getViewById(R.id.tv_name);
			tvContent = getViewById(R.id.tv_content);
			btnSend = getViewById(R.id.btn_send);
			tvScore = getViewById(R.id.tv_score);
			tvOr = getViewById(R.id.tv_or);
			tvBean = getViewById(R.id.tv_bean);
			tvCount = getViewById(R.id.tv_count);
			tvPercent = getViewById(R.id.tv_percent);
			pbPercent = getViewById(R.id.pb_percent);
			llMoney = getViewById(R.id.ll_money);
		}
	}
}
