package com.oplay.giftcool.ui.fragment.gift;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ListView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.GiftLikeListAdapter;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.model.data.req.ReqIndexGift;
import com.oplay.giftcool.model.data.resp.IndexGiftLike;
import com.oplay.giftcool.model.data.resp.OneTypeDataList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.util.NetworkUtil;

import java.util.ArrayList;
import java.util.HashSet;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 15-12-30.
 */
public class GiftLikeListFragment extends BaseFragment_Refresh<IndexGiftLike> {

	private final static String PAGE_NAME = "猜你喜欢";
	private static final String KEY_DATA = "key_like_game";

	private ListView mDataView;
	private GiftLikeListAdapter mAdapter;
	private String mGameKey;
	private JsonReqBase<ReqIndexGift> mReqPageObj;

	public static GiftLikeListFragment newInstance() {
		return new GiftLikeListFragment();
	}

	public static GiftLikeListFragment newInstance(String gameKey) {
		GiftLikeListFragment fragment = new GiftLikeListFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(KEY_DATA, gameKey);
		fragment.setArguments(bundle);
		return fragment;
	}


	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_refresh_lv_container);
		mDataView = getViewById(R.id.lv_content);
	}

	@Override
	protected void setListener() {

	}

	@Override
	@SuppressWarnings("unchecked")
	protected void processLogic(Bundle savedInstanceState) {
		ReqIndexGift data = new ReqIndexGift();
		mReqPageObj = new JsonReqBase<ReqIndexGift>(data);

		if (getArguments() != null) {
			mGameKey = getArguments().getString(KEY_DATA);
		}
		mLastPage = 1;

		if (TextUtils.isEmpty(mGameKey)) {
			mReqPageObj.data.appNames = Global.getInstalledAppNames();
		} else {
			HashSet<String> s = new HashSet<>();
			s.add(mGameKey);
			mReqPageObj.data.appNames = s;
		}
		mAdapter = new GiftLikeListAdapter(getContext());
		mDataView.setAdapter(mAdapter);

	}

	@Override
	protected void lazyLoad() {
		refreshInitConfig();

		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (NetworkUtil.isConnected(getContext())) {
					mReqPageObj.data.page = 1;
					Global.getNetEngine().obtainGiftLike(mReqPageObj)
							.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGiftLike>>>() {
								@Override
								public void onResponse(Response<JsonRespBase<OneTypeDataList<IndexGiftLike>>>
										                       response, Retrofit
										retrofit) {
									if (response != null && response.isSuccess()) {
										refreshSuccessEnd();
										OneTypeDataList<IndexGiftLike> backObj = response.body().getData();
										refreshLoadState(backObj.data, backObj.isEndPage);
										updateData(backObj.data);
										return;
									}
									refreshFailEnd();
								}

								@Override
								public void onFailure(Throwable t) {
									refreshFailEnd();
								}
							});
				} else {
					mViewManager.showErrorRetry();
				}
			}
		});

	}

	/**
	 * 加载更多数据
	 */
	@Override
	protected void loadMoreData() {
		if (!mNoMoreLoad && !mIsLoadMore) {
			mIsLoadMore = true;
			mReqPageObj.data.page = mLastPage + 1;
			new Thread(new Runnable() {
				@Override
				public void run() {
					if (NetworkUtil.isConnected(getContext())) {
						Global.getNetEngine().obtainGiftLike(mReqPageObj)
								.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGiftLike>>>() {
									@Override
									public void onResponse(Response<JsonRespBase<OneTypeDataList<IndexGiftLike>>>
											                       response, Retrofit
											retrofit) {
										if (response != null && response.isSuccess()) {
											moreLoadSuccessEnd();
											OneTypeDataList<IndexGiftLike> backObj = response.body().getData();
											setLoadState(backObj.data, backObj.isEndPage);
											addMoreData(backObj.data);
											return;
										}
										moreLoadFailEnd();
									}

									@Override
									public void onFailure(Throwable t) {
										moreLoadFailEnd();
									}
								});
					} else {
						mViewManager.showErrorRetry();
					}
				}
			}).start();
		}
	}

	public void updateData(ArrayList<IndexGiftLike> data) {
		if (data.size() == 0) {
			mViewManager.showEmpty();
		} else {
			mViewManager.showContent();
		}
		mHasData = true;
		mData = data;
		mAdapter.updateData(mData);
		mLastPage = 1;
	}

	private void addMoreData(ArrayList<IndexGiftLike> moreData) {
		if (moreData == null) {
			return;
		}
		mData.addAll(moreData);
		mAdapter.updateData(mData);
		mLastPage += 1;
	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}
}
