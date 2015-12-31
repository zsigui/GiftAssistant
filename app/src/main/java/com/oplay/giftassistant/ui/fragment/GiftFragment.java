package com.oplay.giftassistant.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.IndexGiftLikeAdapter;
import com.oplay.giftassistant.adapter.IndexGiftLimitAdapter;
import com.oplay.giftassistant.adapter.IndexGiftNewAdapter;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.model.data.resp.IndexGiftBanner;
import com.oplay.giftassistant.model.data.resp.IndexGiftLike;
import com.oplay.giftassistant.model.data.resp.IndexGiftLimit;
import com.oplay.giftassistant.model.data.resp.IndexGiftNew;
import com.oplay.giftassistant.ui.activity.GiftListActivity;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;
import com.oplay.giftassistant.ui.widget.NestedListView;
import com.socks.library.KLog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.bgabanner.BGABanner;

/**
 * 主页-礼包页面主要内容页，不承担网络请求任务
 *
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class GiftFragment extends BaseFragment implements View.OnClickListener {

	private static final String KEY_BANNER = "key_banner";
	private static final String KEY_LIKE = "key_like";
	private static final String KEY_LIMIT = "key_limit";
	private static final String KEY_NEW = "key_new";

	private List<View> views;

	private ScrollView mScrollView;
	// 活动视图, 3张
	private BGABanner mBanner;
	// 猜你喜欢
	private RelativeLayout mLikeBar;
	private RecyclerView mLikeView;
	// 今日限量
	private RelativeLayout mLimitBar;
	private RecyclerView mLimitView;
	// 今日出炉
	private RelativeLayout mNewBar;
	private NestedListView mNewView;


	private IndexGiftLikeAdapter mLikeAdapter;
	private IndexGiftLimitAdapter mLimitAdapter;
	private IndexGiftNewAdapter mNewAdapter;


	public static GiftFragment newInstance() {
		return new GiftFragment();
	}

	public static GiftFragment newInstance(ArrayList<IndexGiftBanner> banners,
	                                       ArrayList<IndexGiftLike> likeGames,
	                                       ArrayList<IndexGiftLimit> limitGifts,
	                                       ArrayList<IndexGiftNew> newGifts) {
		GiftFragment fragment = new GiftFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(KEY_BANNER, banners);
		bundle.putSerializable(KEY_LIKE, likeGames);
		bundle.putSerializable(KEY_LIMIT, limitGifts);
		bundle.putSerializable(KEY_NEW, newGifts);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_gifts);

		mScrollView = getViewById(R.id.sv_container);
		mBanner = getViewById(R.id.banner);
		mLikeBar = getViewById(R.id.rl_hot_all);
		mLikeView = getViewById(R.id.rv_like_content);
		mLimitBar = getViewById(R.id.rl_limit_all);
		mLimitView = getViewById(R.id.rv_limit_content);
		mNewBar = getViewById(R.id.rl_new_all);
		mNewView = getViewById(R.id.rv_new_content);
		((TextView) getViewById(R.id.tv_limit_hint)).setText(Html.fromHtml("(每天<font " +
				"color='#F86060'>20:00</font>更新10款)"));
	}

	@Override
	protected void setListener() {
		mLikeBar.setOnClickListener(this);
		mLimitBar.setOnClickListener(this);
		mNewBar.setOnClickListener(this);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void processLogic(Bundle savedInstanceState) {
		// 设置Banner
		views = new ArrayList<>(3);
		for (int i = 0; i < 3; i++) {
			View v = View.inflate(mActivity, R.layout.view_banner_img, null);
			views.add(v);
		}
		mBanner.setViews(views);

		// 设置RecyclerView的LayoutManager
		LinearLayoutManager llmLike = new LinearLayoutManager(getContext());
		llmLike.setOrientation(LinearLayoutManager.HORIZONTAL);
		LinearLayoutManager llmLimit = new LinearLayoutManager(getContext());
		llmLimit.setOrientation(LinearLayoutManager.HORIZONTAL);

		mLikeView.setLayoutManager(llmLike);
		mLimitView.setLayoutManager(llmLimit);
		mLikeAdapter = new IndexGiftLikeAdapter(mLikeView);
		mLimitAdapter = new IndexGiftLimitAdapter(mLimitView);
		mNewAdapter = new IndexGiftNewAdapter(getContext());

		// 加载数据

		if (getArguments() != null) {

			Serializable s;
			s = getArguments().getSerializable(KEY_BANNER);
			if (s != null) {
				updateBanners((ArrayList<IndexGiftBanner>) s);
			}
			s = getArguments().getSerializable(KEY_LIKE);
			if (s != null) {
				mLikeAdapter.setDatas((ArrayList<IndexGiftLike>) s);
			}
			s = getArguments().getSerializable(KEY_LIMIT);
			if (s != null) {
				mLimitAdapter.setDatas((ArrayList<IndexGiftLimit>) s);
			}
			s = getArguments().getSerializable(KEY_NEW);
			if (s != null) {
				mNewAdapter.setData((ArrayList<IndexGiftNew>) s);
			}
		}

		mLikeView.setAdapter(mLikeAdapter);
		mLimitView.setAdapter(mLimitAdapter);
		mNewView.setAdapter(mNewAdapter);

		mIsPrepared = true;
		mScrollView.smoothScrollTo(0, 0);
	}

	private void loadBanner(ArrayList<IndexGiftBanner> banners) {
		if (banners == null || banners.size() != views.size()) {
			if (AppDebugConfig.IS_FRAG_DEBUG) {
				KLog.d(AppDebugConfig.TAG_FRAG, "bannerUrls is not to be null and the size need to be 3 : " +
						banners);
			}
			return;
		}
		for (int i = 0; i < views.size(); i++) {
			ImageLoader.getInstance().displayImage(banners.get(i).url, (ImageView) getViewById(views.get(i), R.id
					.iv_image_view));
		}
	}

	public void updateBanners(ArrayList<IndexGiftBanner> banners) {
		loadBanner(banners);
	}

	public void updateLikeData(ArrayList<IndexGiftLike> likeData) {
		if (likeData == null) {
			return;
		}
		mLikeAdapter.updateData(likeData);
	}

	public void updateLimitData(ArrayList<IndexGiftLimit> limitData) {
		if (limitData == null) {
			return;
		}
		mLimitAdapter.updateData(limitData);
	}

	public void updateNewData(ArrayList<IndexGiftNew> newData) {
		if (newData == null) {
			return;
		}
		mNewAdapter.updateData(newData);
	}

	@Override
	protected void lazyLoad() {

	}

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
			case R.id.rl_hot_all:
	            intent = new Intent(getActivity(), GiftListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(GiftListActivity.KEY_TYPE, 3);
                intent.putExtras(bundle);
                startActivity(intent);
				break;
			case R.id.rl_limit_all:
				intent = new Intent(getContext(), GiftListActivity.class);
				intent.putExtra(GiftListActivity.KEY_TYPE, 1);
				getContext().startActivity(intent);
				break;
			case R.id.rl_new_all:
				intent = new Intent(getContext(), GiftListActivity.class);
				intent.putExtra(GiftListActivity.KEY_TYPE, 2);
				getContext().startActivity(intent);
				break;
		}
	}
}
