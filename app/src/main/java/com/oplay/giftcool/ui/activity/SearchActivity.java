package com.oplay.giftcool.ui.activity;

import android.text.TextUtils;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.model.data.req.ReqSearchKey;
import com.oplay.giftcool.model.data.resp.SearchDataResult;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.NetErrorFragment;
import com.oplay.giftcool.ui.fragment.search.EmptySearchFragment;
import com.oplay.giftcool.ui.fragment.search.HistoryFragment;
import com.oplay.giftcool.ui.fragment.search.ResultFragment;
import com.oplay.giftcool.ui.widget.search.SearchLayout;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.SPUtil;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.Collections;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 15-12-22.
 */
public class SearchActivity extends BaseAppCompatActivity {

	private SearchLayout mSearchLayout;
	/*private SearchEngine mEngine;*/
	private ArrayList<String> mHistoryData;
	private ResultFragment mResultFragment;
	private EmptySearchFragment mEmptySearchFragment;
	private HistoryFragment mHistoryFragment;
	private NetErrorFragment mNetErrorFragment;

    private String mLastSearchKey = "";

	@Override
	protected void initView() {
		setContentView(R.layout.activity_search);
		mSearchLayout = getViewById(R.id.sl_search);
	}

	@Override
	protected void processLogic() {
		obtainHistoryData();
		/*mEngine = mApp.getRetrofit().create(SearchEngine.class);*/
		mSearchLayout.setCanGetFocus(true);
		mSearchLayout.setIsAutoSendRequest(true);
		mSearchLayout.setIsAutoSendRequest(false);
		mSearchLayout.setSearchActionListener(new SearchActionListener());
		displayHistoryUI(mHistoryData, true);
	}


	/**
	 * 获取输入的历史数据
	 */
	private void obtainHistoryData() {
		mHistoryData = new ArrayList<>();
		String s = SPUtil.getString(getApplicationContext(),
				SPConfig.SP_SEARCH_FILE,
				SPConfig.KEY_SEARCH_INDEX,
				null);
		if (TextUtils.isEmpty(s)) {
			return;
		}
		if (AppDebugConfig.IS_DEBUG) {
			KLog.d("get saveHistory = " + s);
		}

		String[] keys = s.split("\t");
		Collections.addAll(mHistoryData, keys);
	}

	/**
	 * 将每次的搜索记录写入提示表中，需要每次完成搜索执行一次
	 */
	private void saveHistoryData(String newKey) {
		if (TextUtils.isEmpty(newKey)) {
			return;
		}

        if (mHistoryData.contains(newKey)) {
            mHistoryData.remove(newKey);
        }

		// 添加搜索记录，精简个数到10个以内
		mHistoryData.add(0, newKey);
		while (mHistoryData.size() > 10) {
			mHistoryData.remove(mHistoryData.size() - 1);
		}

		StringBuilder builder = new StringBuilder();
		for (String s : mHistoryData) {
			builder.append(s).append("\t");
		}
		if (AppDebugConfig.IS_DEBUG) {
			KLog.d("put saveHistory = " + builder.toString());
		}
		SPUtil.putString(getApplicationContext(),
                SPConfig.SP_SEARCH_FILE,
                SPConfig.KEY_SEARCH_INDEX,
                builder.toString());
	}

	public void clearHistory() {
		mHistoryData.clear();
		SPUtil.putString(getApplicationContext(),
                SPConfig.SP_SEARCH_FILE,
                SPConfig.KEY_SEARCH_INDEX,
                "");
	}

	/**
	 * 显示历史记录或者提示信息
	 */
	private void displayHistoryUI(ArrayList<String> data, boolean isHistory) {
		if (mHistoryFragment == null) {
			mHistoryFragment = HistoryFragment.newInstance(data, isHistory);
		}
		mHistoryFragment.updateData(data, isHistory);
		reattachFrag(R.id.fl_container, mHistoryFragment, mHistoryFragment.getClass().getSimpleName());
	}

	/**
	 * 显示搜索结果界面
	 *
	 * @param data
	 */
	private void displayDataUI(SearchDataResult data) {
		if (mResultFragment == null) {
			mResultFragment = ResultFragment.newInstance(data);
		}
		mResultFragment.updateData(data);
		reattachFrag(R.id.fl_container, mResultFragment, mResultFragment.getClass().getSimpleName());
	}

	/**
	 * 显示搜索结果为空界面
	 */
	private void displayEmptyUI() {
		if (mEmptySearchFragment == null) {
			mEmptySearchFragment = EmptySearchFragment.newInstance();
		}
		reattachFrag(R.id.fl_container, mEmptySearchFragment, mEmptySearchFragment.getClass().getSimpleName());
	}

	/**
	 * 显示网络错误提示
	 */
	private void displayNetworkErrUI() {
        if ("".equals(mLastSearchKey) ||
                !mLastSearchKey.equals(mSearchLayout.getKeyword())) {
            return;
        }
		if (mNetErrorFragment == null) {
			mNetErrorFragment = NetErrorFragment.newInstance();
		}
        reattachFrag(R.id.fl_container, mNetErrorFragment, mNetErrorFragment.getClass().getSimpleName());
	}

	public void sendSearchRequest(String keyword) {
		mSearchLayout.setText(keyword);
		mSearchLayout.sendSearchRequest(keyword);
	}

	private class SearchActionListener implements SearchLayout.OnSearchActionListener {
		@Override
		public void onSearchPerform(String keyword) {
			ScoreManager.getInstance().reward(ScoreManager.RewardType.SEARCH);
			saveHistoryData(keyword);
            mLastSearchKey = keyword;
			if (NetworkUtil.isConnected(SearchActivity.this)) {
				displayLoadingUI(R.id.fl_container);
				ReqSearchKey data = new ReqSearchKey();
				data.searchKey = keyword;
				Global.getNetEngine().obtainSearchResult(new JsonReqBase<ReqSearchKey>(data))
						.enqueue(new Callback<JsonRespBase<SearchDataResult>>() {
							@Override
							public void onResponse(Response<JsonRespBase<SearchDataResult>> response, Retrofit retrofit) {
								if (!mNeedWorkCallback) {
									return;
								}
								if (response != null && response.code() == 200) {
									if (response.body() != null && response.body().isSuccess()) {
										SearchDataResult data = response.body().getData();
										// 检验Key返回数据是否是当前需要的
										if (!mLastSearchKey.equals(mSearchLayout.getKeyword())) {
											// 丢弃这次搜索结果
											// 不更新
											return;
										}
										if (data.games == null && data.gifts == null) {
											displayEmptyUI();
											return;
										}
										// display list h  ere
										displayDataUI(data);
										return;
									}
									if (AppDebugConfig.IS_DEBUG) {
										KLog.e(response.body());
									}
								}
								displayNetworkErrUI();
							}

							@Override
							public void onFailure(Throwable t) {
								if (!mNeedWorkCallback) {
									return;
								}
								if (AppDebugConfig.IS_DEBUG) {
									KLog.e(AppDebugConfig.TAG_SEARCH, t);
								}
								// 提示网络错误
								displayNetworkErrUI();
							}
						});
			} else {
				displayNetworkErrUI();
			}
		}

		@Override
		public void onSearchCleared() {
            // 取消上一轮搜索
            //mEngine.getSearchPrompt(mLastSearchKey).cancel();
			displayHistoryUI(mHistoryData, true);
		}

		@Override
		public void onSearchPromptPerform(String keyword) {
			/*if (NetworkUtil.isConnected(SearchActivity.this)) {
				mEngine.getSearchPrompt(keyword).enqueue(new Callback<JsonRespSearchPrompt>() {
					@Override
					public void onResponse(Response<JsonRespSearchPrompt> response, Retrofit retrofit) {
						if (response.code() == 200) {
							if (response.body().getCode() == StatusCode.SUCCESS) {
								SearchPromptResult data = response.body().getData();
								// 检验Key返回数据是否是当前需要的
								if (!data.keyword.trim().equals(mSearchLayout.getKeyword())) {
									// 丢弃这次搜索结果
									// 不更新
									return;
								}
								// display list here
								displayHistoryUI(data.promptList, false);
								return;
							}
							if (AppDebugConfig.IS_DEBUG) {
								KLog.e(response.body());
							}
						}
					}

					@Override
					public void onFailure(Throwable t) {
						if (AppDebugConfig.IS_FRAG_DEBUG) {
							KLog.e(t);
						}
					}
				});
			} // end if*/

		}
	} // end internal class
}
