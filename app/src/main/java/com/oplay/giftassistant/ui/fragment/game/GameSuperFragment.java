package com.oplay.giftassistant.ui.fragment.game;

import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.IndexGameHotAdapter;
import com.oplay.giftassistant.adapter.IndexGameNewAdapter;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.config.StatusCode;
import com.oplay.giftassistant.download.ApkDownloadManager;
import com.oplay.giftassistant.download.listener.OnDownloadStatusChangeListener;
import com.oplay.giftassistant.listener.OnItemClickListener;
import com.oplay.giftassistant.model.AppStatus;
import com.oplay.giftassistant.model.DownloadStatus;
import com.oplay.giftassistant.model.data.resp.IndexGameBanner;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;
import com.oplay.giftassistant.model.data.resp.IndexGameSuper;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_Refresh_2;
import com.oplay.giftassistant.ui.widget.NestedListView;
import com.oplay.giftassistant.util.IntentUtil;
import com.oplay.giftassistant.util.NetworkUtil;
import com.oplay.giftassistant.util.ToastUtil;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.bgabanner.BGABanner;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 15-12-30.
 */
public class GameSuperFragment extends BaseFragment_Refresh_2 implements View.OnClickListener,
        OnItemClickListener<IndexGameNew>, OnDownloadStatusChangeListener {

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


	public static GameSuperFragment newInstance() {
		return new GameSuperFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_game_super);

		mRefreshLayout = getViewById(R.id.srl_layout);
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
		views = new ArrayList<>();
	}

	@Override
	protected void setListener() {
		mRecommendItem.setOnClickListener(this);
		mHotBar.setOnClickListener(this);
		mNewBar.setOnClickListener(this);
		mRecommendDownload.setOnClickListener(this);
		mRefreshLayout.setOnRefreshListener(this);
		ApkDownloadManager.getInstance(getActivity()).addDownloadStatusListener(this);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void processLogic(Bundle savedInstanceState) {

		// 设置RecyclerView的LayoutManager
		GridLayoutManager glm = new GridLayoutManager(getContext(), 4);
		mHotView.setLayoutManager(glm);

		mIsPrepared = mNoMoreLoad = true;
	}

	public void updateBanners(ArrayList<IndexGameBanner> banners) {
		if (banners == null || banners.size() == 0) {
			mBanner.setVisibility(View.GONE);
			return;
		}

		for (IndexGameBanner banner : banners) {
			View v = View.inflate(getContext(), R.layout.view_banner_img, null);
			ImageLoader.getInstance().displayImage(banner.url, (ImageView) getViewById(v, R.id.iv_image_view));
			views.add(v);
		}

		mBanner.setViews(views);
		mBanner.setVisibility(View.VISIBLE);
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
			mNewAdapter = new IndexGameNewAdapter(getContext(), this);
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
		mViewManager.showContent();
	}

	@Override
    protected void lazyLoad() {
	    refreshInitConfig();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (NetworkUtil.isConnected(getContext())) {
                    Global.getNetEngine().obtainIndexGameSuper(new JsonReqBase<String>(null))
                            .enqueue(new Callback<JsonRespBase<IndexGameSuper>>() {

                                @Override
                                public void onResponse(Response<JsonRespBase<IndexGameSuper>> response,
                                                       Retrofit retrofit) {
                                    if (response != null && response.isSuccess()) {
	                                    if (response.body() != null && response.body().getCode() == StatusCode.SUCCESS) {
		                                    refreshSuccessEnd();
		                                    updateData(response.body().getData());
		                                    return;
	                                    }
                                    }
                                    // 出错
	                                refreshFailEnd();
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    refreshFailEnd();
                                    updateData(initStashData());
                                }
                            });
                } else {
	                refreshFailEnd();
                }
            }
        }).start();
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.rl_hot_all:
				IntentUtil.jumpGameHotList(getContext());
				break;
			case R.id.rl_recommend:
				if (mRecommendData != null) {
					IntentUtil.jumpGameDetail(getContext(), mRecommendData.id, mRecommendData.name);
				}
				break;
			case R.id.rl_new_all:
				IntentUtil.jumpGameNewList(getContext());
				break;
			case R.id.tv_download:
				ToastUtil.showShort("游戏 开始下载" + ((mRecommendData != null) ? mRecommendData.name : null));
				break;
		}
	}

	@Override
	public void onItemClick(IndexGameNew item, View view, int position) {
		try {
			if (view.getId() == R.id.tv_download && !AppStatus.DISABLE.equals(item.appStatus)) {
				item.handleOnClick(getChildFragmentManager());
			} else {
				//TODO 跳转游戏详情页
				IntentUtil.jumpGameDetail(getContext(), item.id, item.name);
			}
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
	}

	@Override
	public void onDownloadStatusChanged(final IndexGameNew appInfo) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mNewAdapter != null && appInfo != null) {
					final String packageName = appInfo.packageName;
					final DownloadStatus status = appInfo.downloadStatus;
					mNewAdapter.updateViewByPackageName(packageName, status);
				}
			}
		});
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

		for (int i = 0; i < 8; i++) {
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

		int id;
		IndexGameNew game1 = new IndexGameNew();
		id = 11064;
		game1.id = id;
		game1.name = "永恒之源";
		game1.newCount = 2;
		game1.playCount = 53143;
		game1.totalCount = 12;
		game1.giftName = "至尊礼包";
		game1.img = String.format("http://owan-img.ymapp.com/app/%d/icon/%s", id, "icon_1450685681.png");
		game1.downloadUrl = String.format("http://m.ouwan.com/api/quick_download/?app_id=%d&chn=200&pack_chn=1", id);
		game1.packageName = "com.aifeng.quanminsifu.ouwan";
		game1.size = "120.04 MB";
		game1.apkFileSize = 125870780;
		game1.versionName = "1.0.8";
		game1.initAppInfoStatus(getActivity());
		newData.add(game1);

		IndexGameNew game2 = new IndexGameNew();
		id = 11068;
		game2.id = id;
		game2.name = "有杀气童话";
		game2.newCount = 2;
		game2.playCount = 53143;
		game2.totalCount = 12;
		game2.giftName = "至尊礼包";
		game2.img = String.format("http://owan-img.ymapp.com/app/%d/icon/%s", id, "icon_1451034535.png");
		game2.downloadUrl = String.format("http://m.ouwan.com/api/quick_download/?app_id=%d&chn=200&pack_chn=1", id);
		game2.packageName = "com.netease.sq.ewan.ouwan";
		game2.size = "169.34 MB";
		game2.apkFileSize = 177563688;
		game2.versionName = "1.2.0";
		game2.initAppInfoStatus(getActivity());
		newData.add(game2);

		IndexGameNew game3 = new IndexGameNew();
		id = 11069;
		game3.id = id;
		game3.name = "一刀流";
		game3.newCount = 2;
		game3.playCount = 53143;
		game3.totalCount = 12;
		game3.giftName = "至尊礼包";
		game3.img = String.format("http://owan-img.ymapp.com/app/%d/icon/%s", id, "icon_1451031060.png");
		game3.downloadUrl = String.format("http://m.ouwan.com/api/quick_download/?app_id=%d&chn=200&pack_chn=1", id);
		game3.packageName = "com.ceapon.fire.ouwan";
		game3.size = "146.53 MB";
		game3.apkFileSize = 153647051;
		game3.versionName = "17.48000.180";
		game3.initAppInfoStatus(getActivity());
		newData.add(game3);

//		for (int i = 0; i < 10; i++) {
//			IndexGameNew game = new IndexGameNew();
//			game.id = i + 1;
//			game.name = "全民神将-攻城战";
//			game.newCount = 2;
//			game.playCount = 53143;
//			game.totalCount = 12;
//			game.giftName = "至尊礼包";
//			game.img = "http://owan-img.ymapp.com/app/10986/icon/icon_1449227350.png_140_140_100.png";
//			game.size = "" + (0.8 * i + 10 * i);
//			newData.add(game);
//		}
		data.news = newData;

		return data;
	}
}
