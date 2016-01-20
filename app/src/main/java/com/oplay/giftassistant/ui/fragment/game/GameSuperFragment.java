package com.oplay.giftassistant.ui.fragment.game;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.GameSuperAdapter;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.config.StatusCode;
import com.oplay.giftassistant.model.data.resp.IndexBanner;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;
import com.oplay.giftassistant.model.data.resp.IndexGameSuper;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftassistant.util.NetworkUtil;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 15-12-30.
 */
public class GameSuperFragment extends BaseFragment_Refresh implements View.OnClickListener {

	private RecyclerView mRecyclerView;
	private GameSuperAdapter mAdapter;

	public static GameSuperFragment newInstance() {
		return new GameSuperFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_refresh_rv_container);
		mRecyclerView = getViewById(R.id.rv_content);
	}

	@Override
	protected void setListener() {
		mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
					ImageLoader.getInstance().resume();
				} else if (newState == RecyclerView.SCROLL_STATE_SETTLING
						|| newState == RecyclerView.SCROLL_STATE_DRAGGING) {
					ImageLoader.getInstance().pause();
				}
			}


		});
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void processLogic(Bundle savedInstanceState) {

		// 设置RecyclerView的LayoutManager
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		mAdapter = new GameSuperAdapter(getActivity());
		mRecyclerView.setAdapter(mAdapter);

		mIsPrepared = mNoMoreLoad = true;
	}

	public void updateData(IndexGameSuper data) {
		if (data == null || mAdapter == null) {
			return;
		}
		mAdapter.updateData(data);
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
	                                updateData(initStashData());
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

	private IndexGameSuper initStashData() {
		IndexGameSuper data = new IndexGameSuper();
		mHasData = true;
		// 先暂时使用缓存数据假定
		ArrayList<IndexBanner> bannerData = new ArrayList<IndexBanner>();
		ArrayList<IndexGameNew> hotData = new ArrayList<IndexGameNew>();
		ArrayList<IndexGameNew> newData = new ArrayList<IndexGameNew>();

		IndexBanner banner1 = new IndexBanner();
		banner1.url = "http://owan-avatar.ymapp.com/avatar/upload/www/2015/12/28/1451266522_48a10badcdbd.jpg";
		bannerData.add(banner1);
		IndexBanner banner2 = new IndexBanner();
		banner2.url = "http://owan-avatar.ymapp.com/avatar/upload/www/2015/12/22/1450752589_814869b92f05.jpg";
		bannerData.add(banner2);
		IndexBanner banner3 = new IndexBanner();
		banner3.url = "http://owan-avatar.ymapp.com/avatar/upload/www/2015/12/23/1450833623_8e099a40a742.jpg";
		bannerData.add(banner3);
		IndexBanner banner4 = new IndexBanner();
		/*banner4.url = "http://owan-avatar.ymapp.com/avatar/upload/www/2015/12/22/1450752589_814869b92f05.jpg";
		bannerData.add(banner4);
		IndexBanner banner5 = new IndexBanner();
		banner5.url = "http://owan-avatar.ymapp.com/avatar/upload/www/2015/12/28/1451266522_48a10badcdbd.jpg";
		bannerData.add(banner5);*/

		data.banner = bannerData;

		for (int i = 0; i < 8; i++) {
			IndexGameNew game = new IndexGameNew();
			game.id = i + 1;
			game.name = "全民神将-攻城战";
			game.newCount = 2;
			game.playCount = 53143;
			game.totalCount = 12;
			game.giftName = "至尊礼包";
			game.img = "http://owan-avatar.ymapp.com/app/10986/icon/icon_1449227350.png_140_140_100.png";
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
		rec.img = "http://owan-avatar.ymapp.com/app/10986/icon/icon_1449227350.png_140_140_100.png";
		rec.banner = "http://owan-avatar.ymapp.com/avatar/upload/www/2015/12/22/1450752589_814869b92f05.jpg";
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
		game1.img = String.format("http://owan-avatar.ymapp.com/app/%d/icon/%s", id, "icon_1450685681.png");
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
		game2.img = String.format("http://owan-avatar.ymapp.com/app/%d/icon/%s", id, "icon_1451034535.png");
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
		game3.img = String.format("http://owan-avatar.ymapp.com/app/%d/icon/%s", id, "icon_1451031060.png");
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
//			game.avatar = "http://owan-avatar.ymapp.com/app/10986/icon/icon_1449227350.png_140_140_100.png";
//			game.size = "" + (0.8 * i + 10 * i);
//			newData.add(game);
//		}
		data.news = newData;

		return data;
	}
}
