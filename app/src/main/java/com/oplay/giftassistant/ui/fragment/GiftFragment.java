package com.oplay.giftassistant.ui.fragment;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.bumptech.glide.Glide;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.engine.NetEngine;
import com.oplay.giftassistant.model.DataModel;
import com.oplay.giftassistant.model.HomeModel;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.bgabanner.BGABanner;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class GiftFragment extends BaseFragment {

	private BGABanner mBanner;
	private ViewPager mvpRecommend;
	private ViewPager mvpHot;
	private List<View> views;
    private NetEngine mEngine;


	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_gifts);
		showLoadingDialog();
		mBanner = getViewById(R.id.banner);
		views = new ArrayList<>(3);
		for (int i = 0; i < 3; i++) {
			View v = View.inflate(mActivity, R.layout.view_banner_img, null);
			views.add(v);

		}
		mBanner.setViews(views);
		mvpRecommend = getViewById(R.id.vpRecommend);
		mIsPrepared = true;
		lazyLoad();
	}

	@Override
	protected void setListener() {

	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
        mEngine = mApp.getRetrofit().create(NetEngine.class);
	}

	@Override
	protected void lazyLoad() {
		mIsLoading = true;
		DataModel<String> d = new DataModel<>();
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
					dismissLoadingDialog();
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
				dismissLoadingDialog();
				mIsLoading = false;
			}
		});
	}
}
