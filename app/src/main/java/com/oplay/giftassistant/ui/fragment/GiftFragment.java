package com.oplay.giftassistant.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.IndexGiftLikeAdapter;
import com.oplay.giftassistant.adapter.IndexGiftLimitAdapter;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.engine.NetEngine;
import com.oplay.giftassistant.model.data.resp.IndexLikeGame;
import com.oplay.giftassistant.model.data.resp.IndexLimitGift;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.bgabanner.BGABanner;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class GiftFragment extends BaseFragment {

	private List<View> views;
    private NetEngine mEngine;

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
	private RecyclerView mNewView;

	private IndexGiftLikeAdapter mLikeAdpater;
	private IndexGiftLimitAdapter mLimitAdpater;
	private IndexGiftLimitAdapter mNewAdpater;
	private ArrayList<IndexLikeGame> mGameDatas;
	private ArrayList<IndexLimitGift> mGiftDatas;
	private ArrayList<IndexLimitGift> mNewDatas;

	public Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 1) {
				mLimitAdpater.updateData(mGiftDatas);
				mLikeAdpater.updateData(mGameDatas);
				mNewAdpater.updateData(mNewDatas);
			}
		}
	};


	public static Fragment newInstance() {
		return new GiftFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_gifts);

		LinearLayoutManager llmLike = new LinearLayoutManager(getContext());
		llmLike.setOrientation(LinearLayoutManager.HORIZONTAL);
		LinearLayoutManager llmLimit = new LinearLayoutManager(getContext());
		llmLimit.setOrientation(LinearLayoutManager.HORIZONTAL);
		LinearLayoutManager llmNew = new LinearLayoutManager(getContext());
		llmNew.setOrientation(LinearLayoutManager.VERTICAL);

		mBanner = getViewById(R.id.banner);
		mLikeBar = getViewById(R.id.rl_like_all);
		mLikeView = getViewById(R.id.rv_like_content);
		mLimitBar = getViewById(R.id.rl_limit_all);
		mLimitView = getViewById(R.id.rv_limit_content);
		mNewBar = getViewById(R.id.rl_new_all);
		mNewView = getViewById(R.id.rv_new_content);
		mLikeView.setLayoutManager(llmLike);
		mLimitView.setLayoutManager(llmLimit);
		mNewView.setLayoutManager(llmNew);
		mLikeAdpater = new IndexGiftLikeAdapter(mLikeView);
		mLimitAdpater = new IndexGiftLimitAdapter(mLimitView);
		mNewAdpater = new IndexGiftLimitAdapter(mNewView);
	}

	@Override
	protected void setListener() {

	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
        mEngine = mApp.getRetrofit().create(NetEngine.class);
		views = new ArrayList<>(3);
		for (int i = 0; i < 3; i++) {
			View v = View.inflate(mActivity, R.layout.view_banner_img, null);
			views.add(v);

		}
		mBanner.setViews(views);
		mLikeView.setAdapter(mLikeAdpater);
		mLimitView.setAdapter(mLimitAdpater);
		mNewView.setAdapter(mNewAdpater);
		mIsPrepared = true;
	}

	public void initData() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				mHasData = true;
				mGiftDatas = new ArrayList<IndexLimitGift>();
				mGameDatas = new ArrayList<IndexLikeGame>();
				for (int i =0 ; i< 10; i++) {
					IndexLikeGame game = new IndexLikeGame();
					game.name = "游戏名" + i;
					game.hasGiftCount = 10 - i;
					game.newGiftCount = i;
					game.img = "http://m.ouwan.com/api/extDownloadApp?app_id=4210&chn=200";
					mGameDatas.add(game);
					IndexLimitGift gift = new IndexLimitGift();
					gift.gameName = "游戏名" + i;
					gift.name = "传奇礼包";
					gift.img = "http://owan-img.ymapp.com/app/4726/icon/icon_1427963355.png_140_140_100.png";
					gift.remainCount = i * 10 + i;
					mGiftDatas.add(gift);
				}
				mNewDatas = (ArrayList<IndexLimitGift>) mGiftDatas.clone();
				mHandler.sendEmptyMessage(1);

			}
		}).start();
	}

	@Override
	protected void lazyLoad() {
		KLog.d(AppDebugConfig.TAG_APP, "load");
		mIsLoading = true;
		initData();
		/*DataModel<String> d = new DataModel<>();
		d.status = 1;
		d.data = "dadfadf";

		mEngine.postJson(d).enqueue(new Callback<DataModel<Object>>() {
			@Override
			public void onResponse(Response<DataModel<Object>> response, Retrofit retrofit) {

			}

			@Override
			public void onFailure(Throwable t) {

			}
		});
		mEngine.getHomeData("definednone").enqueue(new Callback<DataModel<HomeModel>>() {
			@Override
			public void onResponse(final Response<DataModel<HomeModel>> response, Retrofit retrofit) {
				try {
					KLog.e(response.message());
					KLog.e(response.body());
					KLog.e(response.body().data.banners);
					final List<String> arr = response.body().data.banners;
					mHasData = true;
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							for (int i = 0; i< arr.size(); i++) {
								Glide.with(mActivity).load(arr.get(i)).thumbnail(0.1f).into((android.widget.ImageView) views.get(i));
							}
						}
					});

				} catch (Exception e) {
					KLog.e(e);
				}finally {
					mIsLoading = false;
				}
			}

			@Override
			public void onFailure(Throwable t) {
				KLog.e(t);
				mIsLoading = false;
			}
		});*/
	}
}
