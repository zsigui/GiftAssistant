package com.oplay.giftassistant.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.IndexGameHotAdapter;
import com.oplay.giftassistant.adapter.IndexGameNewAdapter;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.model.data.resp.IndexGameBanner;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;
import com.oplay.giftassistant.model.data.resp.IndexGameSuper;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;
import com.oplay.giftassistant.ui.widget.NestedListView;
import com.oplay.giftassistant.util.ToastUtil;
import com.socks.library.KLog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.bgabanner.BGABanner;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 15-12-30.
 */
public class GameSuperFragment extends BaseFragment implements View.OnClickListener {

	private static final String KEY_BANNER = "key_banner";
	private static final String KEY_HOT = "key_hot";
	private static final String KEY_RECOMMEND = "key_recommend";
	private static final String KEY_NEW = "key_new";

	private List<View> views;
	private IndexGameNew mRecommendData;

	private ScrollView mScrollView;
	private ImageView mLoading;
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


	public static GameSuperFragment newInstance() {
		return new GameSuperFragment();
	}

	public static GameSuperFragment newInstance(ArrayList<IndexGameBanner> banners,
	                                            ArrayList<IndexGameNew> hotGames,
	                                            IndexGameNew recommendGame,
	                                            ArrayList<IndexGameNew> newGames) {
		GameSuperFragment fragment = new GameSuperFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(KEY_BANNER, banners);
		bundle.putSerializable(KEY_HOT, hotGames);
		bundle.putSerializable(KEY_RECOMMEND, recommendGame);
		bundle.putSerializable(KEY_NEW, newGames);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_game_super);
		mScrollView = getViewById(R.id.sv_container);
		mBanner = getViewById(R.id.banner);
		mHotBar = getViewById(R.id.rl_hot_all);
		mHotView = getViewById(R.id.rv_hot_content);
		mRecommendItem = getViewById(R.id.rl_recommend);
		mRecommendView = getViewById(R.id.iv_big_pic);
		mRecommendIcon = getViewById(R.id.iv_icon);
		mRecommendName = getViewById(R.id.tv_name);
		mRecommendSize = getViewById(R.id.tv_size);
		mRecommendDownload = getViewById(R.id.tv_download);
		mNewBar = getViewById(R.id.rl_new_all);
		mNewView = getViewById(R.id.rv_new_content);

	}

	@Override
	protected void setListener() {
		mRecommendItem.setOnClickListener(this);
		mHotBar.setOnClickListener(this);
		mNewBar.setOnClickListener(this);
		mRecommendDownload.setOnClickListener(this);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void processLogic(Bundle savedInstanceState) {
// 设置Banner
		views = new ArrayList<>(5);
		for (int i = 0; i < 5; i++) {
			View v = View.inflate(getContext(), R.layout.view_banner_img, null);
			views.add(v);
		}
		mBanner.setViews(views);

		// 设置RecyclerView的LayoutManager
		GridLayoutManager glm = new GridLayoutManager(getContext(), 4);
		mHotView.setLayoutManager(glm);
		mHotAdapter = new IndexGameHotAdapter(mHotView);
		mNewAdapter = new IndexGameNewAdapter(getContext());

		// 加载数据

		if (getArguments() != null) {

			Serializable s;
			s = getArguments().getSerializable(KEY_BANNER);
			if (s != null) {
				updateBanners((ArrayList<IndexGameBanner>) s);
			}
			s = getArguments().getSerializable(KEY_HOT);
			if (s != null) {
				mHotAdapter.setDatas((ArrayList<IndexGameNew>) s);
			}
			s = getArguments().getSerializable(KEY_RECOMMEND);
			if (s != null) {
				updateRecommendData((IndexGameNew) s);
			}
			s = getArguments().getSerializable(KEY_NEW);
			if (s != null) {
				mNewAdapter.setDatas((ArrayList<IndexGameNew>) s);
			}
		}

		mHotView.setAdapter(mHotAdapter);
		mNewView.setAdapter(mNewAdapter);

		mIsPrepared = true;
		mScrollView.smoothScrollTo(0, 0);
	}

	private void loadBanner(ArrayList<IndexGameBanner> banners) {
		if (banners == null || banners.size() != views.size()) {
			if (AppDebugConfig.IS_FRAG_DEBUG) {
				KLog.d(AppDebugConfig.TAG_FRAG, "bannerUrls is not to be null and the size need to be 5 : " +
						banners);
			}
			return;
		}
		for (int i = 0; i < views.size(); i++) {
			ImageLoader.getInstance().displayImage(banners.get(i).url, (ImageView) getViewById(views.get(i), R.id
					.iv_image_view));
		}
	}

	public void updateBanners(ArrayList<IndexGameBanner> banners) {
		loadBanner(banners);
	}

	public void updateHotData(ArrayList<IndexGameNew> data) {
		if (data == null) {
			return;
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
		mNewAdapter.updateData(data);
	}

	public void updateData(IndexGameSuper data) {
		updateBanners(data.banner);
		updateHotData(data.hot);
		updateRecommendData(data.recommend);
		updateNewData(data.news);
	}

	@Override
	protected void lazyLoad() {
		mIsLoading = true;
		mHasData = false;
		showLoadingDialog();
		new Thread(new Runnable() {
			@Override
			public void run() {
				Global.getNetEngine().obtainIndexGameSuper(new JsonReqBase<String>(null))
						.enqueue(new Callback<JsonRespBase<IndexGameSuper>>() {

							@Override
							public void onResponse(Response<JsonRespBase<IndexGameSuper>> response,
							                       Retrofit retrofit) {
								mIsLoading = false;
								dismissLoadingDialog();
								if (response != null && response.isSuccess()) {
									mHasData = true;
									updateData(response.body().getData());
									return;
								}
								// 出错
								displayNetErrorUI();
							}

							@Override
							public void onFailure(Throwable t) {
								mIsLoading = false;
								dismissLoadingDialog();
								//displayNetErrorUI();
								IndexGameSuper data = initStashData();
								updateData(data);
							}
						});
			}
		}).start();
	}

	private void displayNetErrorUI() {
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.rl_hot_all:
				ToastUtil.showShort("热门游戏-全部 被点击");
				break;
			case R.id.rl_recommend:
				ToastUtil.showShort("主推游戏 被点击" + ((mRecommendData != null) ? mRecommendData.name : null));
				break;
			case R.id.rl_new_all:
				ToastUtil.showShort("新游推荐-全部 被点击");
				break;
			case R.id.tv_download:
				ToastUtil.showShort("游戏 开始下载" + ((mRecommendData != null) ? mRecommendData.name : null));
				break;
		}
	}

	private IndexGameSuper initStashData() {
		IndexGameSuper data = new IndexGameSuper();
		mHasData = true;
		// 先暂时使用缓存数据假定
		ArrayList<IndexGameBanner> bannerData = new ArrayList<IndexGameBanner>();
		ArrayList<IndexGameNew> hotData = new ArrayList<IndexGameNew>();
		ArrayList<IndexGameNew> newData = new ArrayList<IndexGameNew>();

		IndexGameBanner banner1 = new IndexGameBanner();
		banner1.url = "http://owan-img.ymapp.com/img/upload/www/2015/12/28/1451266522_48a10badcdbd.jpg";
		bannerData.add(banner1);
		IndexGameBanner banner2 = new IndexGameBanner();
		banner2.url = "http://owan-img.ymapp.com/img/upload/www/2015/12/22/1450752589_814869b92f05.jpg";
		bannerData.add(banner2);
		IndexGameBanner banner3 = new IndexGameBanner();
		banner3.url = "http://owan-img.ymapp.com/img/upload/www/2015/12/23/1450833623_8e099a40a742.jpg";
		bannerData.add(banner3);
		IndexGameBanner banner4 = new IndexGameBanner();
		banner4.url = "http://owan-img.ymapp.com/img/upload/www/2015/12/22/1450752589_814869b92f05.jpg";
		bannerData.add(banner4);
		IndexGameBanner banner5 = new IndexGameBanner();
		banner5.url = "http://owan-img.ymapp.com/img/upload/www/2015/12/28/1451266522_48a10badcdbd.jpg";
		bannerData.add(banner5);

		data.banner = bannerData;

		for (int i= 0; i<8; i++) {
			IndexGameNew game = new IndexGameNew();
			game.id = i + 1;
			game.name = "全民神将-攻城战";
			game.newCount = 2;
			game.playCount = 53143;
			game.totalCount = 12;
			game.giftName = "至尊礼包";
			game.img = "http://owan-img.ymapp.com/app/10986/icon/icon_1449227350.png_140_140_100.png";
			game.size = "" + (0.8 * i + 10 * i);
			hotData.add(game);
		}
		data.hot = hotData;

		IndexGameNew rec = new IndexGameNew();
		rec.id = 773;
		rec.name = "全民神将-攻城战";
		rec.newCount = 2;
		rec.playCount = 53143;
		rec.totalCount = 12;
		rec.giftName = "至尊礼包";
		rec.img = "http://owan-img.ymapp.com/app/10986/icon/icon_1449227350.png_140_140_100.png";
		rec.banner = "http://owan-img.ymapp.com/img/upload/www/2015/12/22/1450752589_814869b92f05.jpg";
		rec.size = "153M";
		data.recommend = rec;

		for (int i = 0; i<10; i++) {
			IndexGameNew game = new IndexGameNew();
			game.id = i + 1;
			game.name = "全民神将-攻城战";
			game.newCount = 2;
			game.playCount = 53143;
			game.totalCount = 12;
			game.giftName = "至尊礼包";
			game.img = "http://owan-img.ymapp.com/app/10986/icon/icon_1449227350.png_140_140_100.png";
			game.size = "" + (0.8 * i + 10 * i);
			newData.add(game);
		}
		data.news = newData;

		return data;
	}

}
