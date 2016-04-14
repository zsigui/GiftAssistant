package com.oplay.giftcool.ui.fragment.postbar;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.PostOfficialListAdapter;
import com.oplay.giftcool.adapter.itemdecoration.DividerItemDecoration;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.model.data.req.ReqIndexPost;
import com.oplay.giftcool.model.data.resp.IndexPostNew;
import com.oplay.giftcool.model.data.resp.OneTypeDataList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-4-13.
 */
public class PostListFragment extends BaseFragment_Refresh<IndexPostNew> {

	private RecyclerView rvData;
	private PostOfficialListAdapter mAdapter;
	private String mUrl;

	public static PostListFragment newInstance(String url) {
		PostListFragment fragment = new PostListFragment();
		Bundle bundle = new Bundle();
		bundle.putString(KeyConfig.KEY_URL, url);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_refresh_rv_container);
		rvData = getViewById(R.id.lv_content);
	}

	@Override
	protected void setListener() {

	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		if (getArguments() == null) {
			ToastUtil.showShort(ConstString.TEXT_ENTER_ERROR);
			return;
		}
		mUrl = getArguments().getString(KeyConfig.KEY_URL);

		mData = new ArrayList<>();
		mAdapter = new PostOfficialListAdapter(getContext(), mData);
		LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
		DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), llm.getOrientation());
		rvData.setLayoutManager(llm);
		rvData.addItemDecoration(itemDecoration);
		rvData.setAdapter(mAdapter);
	}

	/**
	 * 刷新首页数据的网络请求声明
	 */
	private Call<JsonRespBase<OneTypeDataList<IndexPostNew>>> mCallRefresh;
	private JsonReqBase<ReqIndexPost> mReqPageObj;


	@Override
	protected void lazyLoad() {
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				refreshInitConfig();
				// 判断网络情况
//        if (!NetworkUtil.isConnected(getContext())) {
//            refreshFailEnd();
//            return;
//        }
				if (mCallRefresh != null) {
					mCallRefresh.cancel();
				}

				// 设置请求对象的值
				if (mReqPageObj == null) {
					ReqIndexPost data = new ReqIndexPost();
					data.pageSize = 20;
					mReqPageObj = new JsonReqBase<ReqIndexPost>(data);
				}
				mReqPageObj.data.page = PAGE_FIRST;

				mCallRefresh = Global.getNetEngine().obtainPostList(mUrl, mReqPageObj);
				mCallRefresh.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexPostNew>>>() {
					@Override
					public void onResponse(Call<JsonRespBase<OneTypeDataList<IndexPostNew>>> call,
					                       Response<JsonRespBase<OneTypeDataList<IndexPostNew>>> response) {
						if (!mCanShowUI || call.isCanceled()) {
							return;
						}
						if (response != null && response.isSuccessful()) {
							Global.sServerTimeDiffLocal = System.currentTimeMillis() - response.headers().getDate
									("Date").getTime();
							if (response.body() != null && response.body().getCode() == NetStatusCode.SUCCESS) {
								// 获取数据成功
								refreshSuccessEnd();
								updateDate(mData);
								return;
							}
						}
						if (AppDebugConfig.IS_DEBUG) {
							KLog.d(AppDebugConfig.TAG_FRAG, (response == null ?
									"返回出错" : response.code() + ", " + response.message()));
						}
						refreshFailEnd();
					}

					@Override
					public void onFailure(Call<JsonRespBase<OneTypeDataList<IndexPostNew>>> call, Throwable t) {
						if (!mCanShowUI || call.isCanceled()) {
							return;
						}
						if (AppDebugConfig.IS_DEBUG) {
							KLog.d(AppDebugConfig.TAG_FRAG, t);
						}
						refreshFailEnd();
					}
				});
			}
		});
	}

	private void updateDate(ArrayList<IndexPostNew> data) {
		if (data == null) {
			return;
		}
		if (data.isEmpty()) {
			mViewManager.showEmpty();
			return;
		}
		mAdapter.updateData(data);
		mLastPage = PAGE_FIRST;
		mViewManager.showContent();
	}

	/**
	 * 首页加载更多数据的网络请求声明
	 */
	private Call<JsonRespBase<OneTypeDataList<IndexPostNew>>> mCallLoad;

	/**
	 * 加载更多数据
	 */
	@Override
	protected void loadMoreData() {
		if (!mNoMoreLoad && !mIsLoadMore) {
//            if (!NetworkUtil.isConnected(getContext())) {
//                moreLoadFailEnd();
//                return;
//            }
			mIsLoadMore = true;
			mReqPageObj.data.page = mLastPage + 1;
			if (mCallLoad != null) {
				mCallLoad.cancel();
			}
			mCallLoad = Global.getNetEngine().obtainPostList(mUrl, mReqPageObj);
			mCallLoad.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexPostNew>>>() {
				@Override
				public void onResponse(Call<JsonRespBase<OneTypeDataList<IndexPostNew>>> call,
				                       Response<JsonRespBase<OneTypeDataList<IndexPostNew>>> response) {
					if (!mCanShowUI || call.isCanceled()) {
						return;
					}
					if (response != null && response.isSuccessful()) {
						if (response.body() != null && response.body().isSuccess()) {
							moreLoadSuccessEnd();
							OneTypeDataList<IndexPostNew> backObj = response.body().getData();
							setLoadState(backObj.data, backObj.isEndPage);
							addMoreData(backObj.data);
							return;
						}
						if (AppDebugConfig.IS_DEBUG) {
							KLog.d(AppDebugConfig.TAG_FRAG, (response.body() == null ?
									"解析错误" : response.body().error()));
						}
					}
					moreLoadFailEnd();
				}

				@Override
				public void onFailure(Call<JsonRespBase<OneTypeDataList<IndexPostNew>>> call, Throwable t) {
					if (!mCanShowUI || call.isCanceled()) {
						return;
					}
					moreLoadFailEnd();
				}
			});
		}
	}

	private void addMoreData(ArrayList<IndexPostNew> moreData) {
		if (moreData == null) {
			return;
		}
		mAdapter.addMoreData(moreData);
		mLastPage += 1;
	}

	@Override
	public String getPageName() {
		return "活动列表";
	}
}