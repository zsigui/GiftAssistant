package com.oplay.giftcool.ui.fragment.postbar;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.PostAdapter;
import com.oplay.giftcool.adapter.itemdecoration.DividerItemDecoration;
import com.oplay.giftcool.adapter.layoutmanager.SnapLinearLayoutManager;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.config.util.PostTypeUtil;
import com.oplay.giftcool.listener.CallbackListener;
import com.oplay.giftcool.model.data.req.ReqIndexPost;
import com.oplay.giftcool.model.data.resp.IndexPost;
import com.oplay.giftcool.model.data.resp.IndexPostNew;
import com.oplay.giftcool.model.data.resp.OneTypeDataList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.util.ThreadUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 活动Fragment
 * <p/>
 * Created by zsigui on 16-4-5.
 */
public class PostFragment extends BaseFragment_Refresh<IndexPostNew> implements CallbackListener {

	private final String TAG_NAME = "首页活动";
	private final String PREFIX_POST = "获取数据";

	public static final int INDEX_HEADER = 0;
	public static final int INDEX_OFFICIAL = 1;
	public static final int INDEX_NOTIFY = 2;

	private int mIndexOfficialHeader = 1;
	private int mIndexNotifyHeader = 2;

	// 页面控件
	private RecyclerView rvData;
	private PostAdapter mAdapter;

	private IndexPost mInitData;

	public static PostFragment newInstance() {
		return new PostFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_refresh_rv_container);
		rvData = getViewById(R.id.lv_content);
	}

	@Override
	protected void setListener() {
		mAdapter.setCallbackListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		LinearLayoutManager llp = new SnapLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
		DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), llp.getOrientation());
		rvData.setLayoutManager(llp);
		rvData.addItemDecoration(itemDecoration);
		mAdapter = new PostAdapter(getContext());
		rvData.setAdapter(mAdapter);

	}

	/**
	 * 刷新首页数据的网络请求声明
	 */
	private Call<JsonRespBase<IndexPost>> mCallRefresh;

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
				final boolean isRead = false;
				if (mReqPageObj == null) {
					ReqIndexPost data = new ReqIndexPost();
					data.pageSize = 20;
					data.isAttention = (isRead ? 1 : 0);
					mReqPageObj = new JsonReqBase<ReqIndexPost>(data);
				}
				if (isRead) {
					mReqPageObj.data.appNames = Global.getInstalledAppNames();
				} else {
					mReqPageObj.data.appNames = null;
				}

				mCallRefresh = Global.getNetEngine().obtainIndexPost(mReqPageObj);
				mCallRefresh.enqueue(new Callback<JsonRespBase<IndexPost>>() {
					@Override
					public void onResponse(Call<JsonRespBase<IndexPost>> call, Response<JsonRespBase<IndexPost>>
							response) {
						if (!mCanShowUI || call.isCanceled()) {
							return;
						}
						if (response != null && response.isSuccessful()) {
							Global.sServerTimeDiffLocal = System.currentTimeMillis() - response.headers().getDate
									("Date").getTime();
							if (response.body() != null && response.body().getCode() == NetStatusCode.SUCCESS) {
								// 获取数据成功
								refreshSuccessEnd();
								transferIndexPostToArray(response.body().getData());
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
					public void onFailure(Call<JsonRespBase<IndexPost>> call, Throwable t) {
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

	/**
	 * 将异步返回的首页活动数据转换为列表形式
	 *
	 * @param data
	 */
	private void transferIndexPostToArray(IndexPost data) {
		if (data == null) {
			mData = null;
			return;
		}
		mInitData = data;
		if (mData == null) {
			mData = new ArrayList<>();
		} else {
			mData.clear();
		}
		// 添加固定头
		IndexPostNew header = new IndexPostNew();
		header.showType = PostTypeUtil.TYPE_HEADER;
		mData.add(header);

		mIndexOfficialHeader = mData.size();
		// 添加官方活动部分
		IndexPostNew titleOne = new IndexPostNew();
		titleOne.showType = PostTypeUtil.TYPE_TITLE_OFFICIAL;
		mData.add(titleOne);
		if (data.officialData != null && !data.officialData.isEmpty()) {
			mData.addAll(data.officialData);
		}

		mIndexNotifyHeader = mData.size();
		IndexPostNew titleTwo = new IndexPostNew();
		titleTwo.showType = PostTypeUtil.TYPE_TITLE_GAME;
		mData.add(titleTwo);
		// 添加游戏快讯部分
		if (data.notifyData != null && !data.notifyData.isEmpty()) {
			mData.addAll(data.notifyData);
		}
	}

	public void updateDate(ArrayList<IndexPostNew> data) {
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

	private void addMoreData(ArrayList<IndexPostNew> moreData) {
		if (moreData == null) {
			return;
		}
		mAdapter.addMoreData(moreData);
		mLastPage += 1;
	}

	/**
	 * 首页加载更多数据的网络请求声明
	 */
	private Call<JsonRespBase<OneTypeDataList<IndexPostNew>>> mCallLoad;
	private JsonReqBase<ReqIndexPost> mReqPageObj;

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
			mCallLoad = Global.getNetEngine().obtainPostList(NetUrl.POST_GET_LIST, mReqPageObj);
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

	/**
	 *
	 */
	private Call<JsonRespBase<OneTypeDataList<IndexPostNew>>> mCallChange;

	/**
	 * 刷新游戏资讯列表数据
	 */
	private void refreshNotifyData() {
		Global.THREAD_POOL.execute(new Runnable() {

			@Override
			public void run() {
				mLastPage = PAGE_FIRST;
				final boolean isRead = AssistantApp.getInstance().isReadAttention();
				if (isRead) {
					mReqPageObj.data.appNames = Global.getInstalledAppNames();
				} else {
					mReqPageObj.data.appNames = null;
				}
				mReqPageObj.data.isAttention = (isRead ? 1 : 0);
				mReqPageObj.data.page = mLastPage;
				mIsSwipeRefresh = true;
				mCallChange = Global.getNetEngine().obtainPostList(NetUrl.POST_GET_LIST, mReqPageObj);
				mCallChange.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexPostNew>>>() {
					@Override
					public void onResponse(Call<JsonRespBase<OneTypeDataList<IndexPostNew>>> call,
					                       Response<JsonRespBase<OneTypeDataList<IndexPostNew>>> response) {
						if (!mCanShowUI || call.isCanceled()) {
							return;
						}
						// 解除刷新和加载的状态锁定
						mIsSwipeRefresh = mIsNotifyRefresh = mIsLoading = mIsLoadMore = false;
						if (mRefreshLayout != null) {
							mRefreshLayout.setRefreshing(false);
							mRefreshLayout.setEnabled(true);
							mRefreshLayout.setLoading(false);
						}
						if (response != null && response.isSuccessful()) {
							if (response.body() != null && response.body().isSuccess()) {
								final ArrayList<IndexPostNew> data = response.body().getData().data;
								if (data != null && !data.isEmpty()) {

									// 切换成功，刷新修改
									if (mRefreshLayout != null) {
										mNoMoreLoad = false;
										mRefreshLayout.setCanShowLoad(true);
									}

									mInitData.notifyData = data;
									transferIndexPostToArray(mInitData);
									updateDate(mData);
								}
								ToastUtil.showShort("获取出错-返回为空");
								return;
							}
							if (AppDebugConfig.IS_DEBUG) {
								KLog.d(AppDebugConfig.TAG_FRAG, response.body() != null ?
										response.body().error() : "解析失败");
							}
							ToastUtil.blurErrorMsg(PREFIX_POST, response.body());
							return;
						}
						if (AppDebugConfig.IS_DEBUG) {
							KLog.d(AppDebugConfig.TAG_FRAG, response != null ?
									response.code() + ", " + response.message() : "返回失败");
						}
					}

					@Override
					public void onFailure(Call<JsonRespBase<OneTypeDataList<IndexPostNew>>> call, Throwable t) {
						if (!mCanShowUI || call.isCanceled()) {
							return;
						}
						if (AppDebugConfig.IS_DEBUG) {
							KLog.d(AppDebugConfig.TAG_FRAG, t);
						}
						ToastUtil.blurThrow(PREFIX_POST);
						mIsSwipeRefresh = mIsNotifyRefresh = mIsLoading = mIsLoadMore = false;
						if (mRefreshLayout != null) {
							mRefreshLayout.setRefreshing(false);
							mRefreshLayout.setEnabled(true);
							mRefreshLayout.setLoading(false);
						}
					}
				});
			}
		});
	}

	@Override
	public void doCallBack(Object data) {
		if (data != null && data instanceof Boolean) {
			if (!mIsSwipeRefresh) {
				if (mCallRefresh != null) {
					mCallRefresh.cancel();
				}
				if (mCallLoad != null) {
					mCallLoad.cancel();
				}
				if (mCallChange != null) {
					mCallChange.cancel();
				}
				if (mRefreshLayout != null) {
					mRefreshLayout.setRefreshing(true);
					mRefreshLayout.setEnabled(false);
					mRefreshLayout.setCanShowLoad(false);
				}
				refreshNotifyData();
			}
		}
	}

	public void setPagePosition(final int type) {
		ThreadUtil.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (rvData != null) {
					switch (type) {
						case INDEX_HEADER:
							rvData.smoothScrollToPosition(0);
							break;
						case INDEX_OFFICIAL:
							rvData.smoothScrollToPosition(mIndexOfficialHeader < mAdapter.getItemCount() ?
									mIndexOfficialHeader : mAdapter.getItemCount() - 1);
							break;
						case INDEX_NOTIFY:
							rvData.smoothScrollToPosition(mIndexNotifyHeader < mAdapter.getItemCount() ?
									mIndexNotifyHeader : mAdapter.getItemCount() - 1);
							break;
					}

				}
			}
		});
	}

	@Override
	public String getPageName() {
		return TAG_NAME;
	}

	/* 测试数据 */

	private IndexPost initTestData() {
		IndexPost post = new IndexPost();
		post.officialData = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			IndexPostNew data = new IndexPostNew();
			data.id = i * (int) (Math.random() * 103);
			data.title = "[天剑传说]盖楼拿代金卷了" + data.id;
			data.content = "洗刷刷西三环阿迪开房间阿里开始的加法考虑到将很快垃圾sdk了好久阿喀琉斯大家伙可垃圾sdk你就爱看市领导和金卡的就好了";
			data.state = (int) (Math.random() * 2);
			data.img = "http://owan-img.ymapp.com/app/76/d4/7705/icon/icon_1440398275.png_128_128_70.png";
			data.showType = PostTypeUtil.TYPE_CONTENT_OFFICIAL;
			post.officialData.add(data);
		}
		post.notifyData = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			IndexPostNew data = new IndexPostNew();
			data.id = i * (int) (Math.random() * 1002 + 1002);
			data.title = "[神三国]清明充值送大礼" + data.id;
			data.content = "洗刷刷西三环阿迪开房间阿里开始的加法考虑到将很快垃圾sdk了好久阿喀琉斯大家伙可垃圾sdk你就爱看市领导和金卡的就好了";
			data.state = (int) (Math.random() * 2);
			data.img = "http://owan-img.ymapp.com/app/76/d4/7705/icon/icon_1440398275.png_128_128_70.png";
			data.startTime = "4月31日";
			data.showType = PostTypeUtil.TYPE_CONTENT_GAME;
			post.notifyData.add(data);
		}
		return post;
	}

	private OneTypeDataList<IndexPostNew> getLoadData() {
		OneTypeDataList<IndexPostNew> data = new OneTypeDataList<>();
		data.data = getRefreshData();
		data.pageSize = 20;
		data.isEndPage = (int) (Math.random() * 2) == 0 ? false : true;
		return data;
	}

	private ArrayList<IndexPostNew> getRefreshData() {
		ArrayList<IndexPostNew> datas = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			IndexPostNew data = new IndexPostNew();
			data.id = i * (int) (Math.random() * 1002 + 1002);
			data.title = "[战国时代]刷新数据" + data.id;
			data.content = "从赶紧来开始好几个开始了就发给你考试后点击开公司好可能根据首付款工具框架飞黄金矿工";
			data.state = (int) (Math.random() * 2);
			data.img = "http://owan-img.ymapp.com/app/10946/icon/icon_1439432439.png_128_128_70.png";
			data.startTime = "3月12日";
			data.showType = PostTypeUtil.TYPE_CONTENT_GAME;
			datas.add(data);
		}
		return datas;
	}
}
