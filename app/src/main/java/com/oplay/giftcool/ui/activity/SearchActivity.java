package com.oplay.giftcool.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.listener.OnSearchListener;
import com.oplay.giftcool.manager.StatisticsManager;
import com.oplay.giftcool.model.data.req.ReqSearchKey;
import com.oplay.giftcool.model.data.resp.PromptData;
import com.oplay.giftcool.model.data.resp.SearchDataResult;
import com.oplay.giftcool.model.data.resp.SearchPromptResult;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.LoadingFragment;
import com.oplay.giftcool.ui.fragment.NetErrorFragment;
import com.oplay.giftcool.ui.fragment.search.EmptySearchFragment;
import com.oplay.giftcool.ui.fragment.search.HistoryFragment;
import com.oplay.giftcool.ui.fragment.search.PromptFragment;
import com.oplay.giftcool.ui.fragment.search.ResultFragment;
import com.oplay.giftcool.ui.widget.search.SearchLayout;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.SPUtil;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 15-12-22.
 */
public class SearchActivity extends BaseAppCompatActivity implements OnSearchListener {

	private SearchLayout mSearchLayout;
	/*private SearchEngine mEngine;*/
	private ArrayList<String> mHistoryData;
	private ResultFragment mResultFragment;
	private EmptySearchFragment mEmptySearchFragment;
	private HistoryFragment mHistoryFragment;
	private PromptFragment mPromptFragment;
	private NetErrorFragment mNetErrorFragment;
	private LoadingFragment mLoadingFragment;

	private String mLastSearchKey = "";
	private String mLastInputKey = "";

	/* Fragment页面定位 */
	private final int PAGE_CONTENT = 1;
	private final int PAGE_EMPTY = 2;
	private final int PAGE_HISTORY = 3;
	private final int PAGE_PROMPT = 4;
	private final int PAGE_ERROR = 5;
	private final int PAGE_LOADING = 6;

	/* Fragment页面标签 */
	private final String TAG_CONTENT = ResultFragment.class.getSimpleName();
	private final String TAG_EMPTY = EmptySearchFragment.class.getSimpleName();
	private final String TAG_HISTORY = HistoryFragment.class.getSimpleName();
	private final String TAG_PROMPT = PromptFragment.class.getSimpleName();
	private final String TAG_ERROR = NetErrorFragment.class.getSimpleName();
	private final String TAG_LOADING = LoadingFragment.class.getSimpleName();

	@Override
	protected void initView() {
		setContentView(R.layout.activity_search);
		mSearchLayout = getViewById(R.id.sl_search);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mResultFragment = (ResultFragment) getSupportFragmentManager().findFragmentByTag(TAG_CONTENT);
			mEmptySearchFragment = (EmptySearchFragment) getSupportFragmentManager().findFragmentByTag(TAG_EMPTY);
			mHistoryFragment = (HistoryFragment) getSupportFragmentManager().findFragmentByTag(TAG_HISTORY);
			mPromptFragment = (PromptFragment) getSupportFragmentManager().findFragmentByTag(TAG_PROMPT);
			mNetErrorFragment = (NetErrorFragment) getSupportFragmentManager().findFragmentByTag(TAG_ERROR);
			mLoadingFragment = (LoadingFragment) getSupportFragmentManager().findFragmentByTag(TAG_LOADING);
		}
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void processLogic() {
		obtainHistoryData();
		mSearchLayout.setCanGetFocus(true);
		mSearchLayout.setIsAutoSendRequest(false);
		mSearchLayout.setAutoPopupPrompt(true);
		mSearchLayout.setSearchActionListener(new SearchActionListener());

		displayHistoryUI(mHistoryData);
	}

	@Override
	protected void onResume() {
		super.onResume();
		AssistantApp.getInstance().getSoftInputHeight(this);
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
	 * 先判断隐藏所有可见的Fragment
	 */
	private void hideAllFragment(FragmentTransaction ft, int id) {
		if (mResultFragment != null && id != PAGE_CONTENT) {
			ft.hide(mResultFragment);
		}
		if (mLoadingFragment != null && id != PAGE_LOADING) {
			ft.hide(mLoadingFragment);
		}
		if (mEmptySearchFragment != null && id != PAGE_EMPTY) {
			ft.hide(mEmptySearchFragment);
		}
		if (mHistoryFragment != null && id != PAGE_HISTORY) {
			ft.hide(mHistoryFragment);
		}
		if (mPromptFragment != null && id != PAGE_PROMPT) {
			ft.hide(mPromptFragment);
		}
		if (mNetErrorFragment != null && id != PAGE_ERROR) {
			ft.hide(mNetErrorFragment);
		}
	}

	/**
	 * 显示历史记录信息
	 */
	private void displayHistoryUI(ArrayList<String> data) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		hideAllFragment(ft, PAGE_HISTORY);
		if (mHistoryFragment == null) {
			Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_HISTORY);
			if (f != null) {
				mHistoryFragment = (HistoryFragment) f;
				ft.show(f);
			} else {
				mHistoryFragment = HistoryFragment.newInstance(data);
				ft.add(R.id.fl_container, mHistoryFragment, TAG_HISTORY);
			}
		} else {
			ft.show(mHistoryFragment);
		}
		mHistoryFragment.updateHistoryData(data);
		ft.commit();
	}


	/**
	 * 显示提示信息
	 *
	 * @param data
	 */
	private void displayPromptUI(ArrayList<PromptData> data) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		hideAllFragment(ft, PAGE_PROMPT);
		if (mPromptFragment == null) {
			Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_PROMPT);
			if (f != null) {
				mPromptFragment = (PromptFragment) f;
				ft.show(f);
			} else {
				mPromptFragment = PromptFragment.newInstance(data);
				ft.add(R.id.fl_container, mPromptFragment, TAG_PROMPT);
			}
		} else {
			ft.show(mPromptFragment);
		}
		mPromptFragment.updateData(data);
		ft.commit();
	}

	/**
	 * 显示搜索结果界面
	 *
	 * @param data
	 */
	private void displayDataUI(SearchDataResult data, String name, int id) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		hideAllFragment(ft, PAGE_CONTENT);
		if (mResultFragment == null) {
			Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT);
			if (f != null) {
				mResultFragment = (ResultFragment) f;
				ft.show(f);
			} else {
				mResultFragment = ResultFragment.newInstance(data);
				ft.add(R.id.fl_container, mResultFragment, TAG_CONTENT);
			}
		} else {
			ft.show(mResultFragment);
		}
		mResultFragment.updateData(data, name, id);
		ft.commit();
	}

	/**
	 * 显示搜索结果为空界面
	 */
	private void displayEmptyUI() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		hideAllFragment(ft, PAGE_EMPTY);
		if (mEmptySearchFragment == null) {
			Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_EMPTY);
			if (f != null) {
				mEmptySearchFragment = (EmptySearchFragment) f;
				ft.show(f);
			} else {
				mEmptySearchFragment = EmptySearchFragment.newInstance(mLastSearchKey, 0);
				ft.add(R.id.fl_container, mEmptySearchFragment, TAG_EMPTY);
			}
		} else {
			ft.show(mEmptySearchFragment);
		}
		ft.commit();
	}

	/**
	 * 显示网络错误提示
	 */
	private void displayNetworkErrUI() {
		if ("".equals(mLastSearchKey)
				|| (mSearchLayout != null && !mLastSearchKey.equals(mSearchLayout.getKeyword()))) {
			return;
		}
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		hideAllFragment(ft, PAGE_ERROR);
		if (mNetErrorFragment == null) {
			Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_ERROR);
			if (f != null) {
				mNetErrorFragment = (NetErrorFragment) f;
				ft.show(f);
			} else {
				mNetErrorFragment = NetErrorFragment.newInstance();
				ft.add(R.id.fl_container, mNetErrorFragment, TAG_ERROR);
			}
		} else {
			ft.show(mNetErrorFragment);
		}
		ft.commit();
	}


	/**
	 * 显示加载中页面
	 */
	private void displayLoadingUI() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		hideAllFragment(ft, PAGE_LOADING);
		if (mLoadingFragment == null) {
			Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_LOADING);
			if (f != null) {
				mLoadingFragment = (LoadingFragment) f;
				ft.show(f);
			} else {
				mLoadingFragment = LoadingFragment.newInstance();
				ft.add(R.id.fl_container, mLoadingFragment, TAG_LOADING);
			}
		} else {
			ft.show(mLoadingFragment);
		}
		ft.commit();
	}

	public void sendSearchRequest(String keyword, int id) {
		mSearchLayout.setAutoPopupPrompt(false);
		mSearchLayout.setText(keyword);
		mLastInputKey = keyword;
		mSearchLayout.setAutoPopupPrompt(true);
		mSearchLayout.sendSearchRequest(keyword, id);
	}

	@Override
	public void release() {
		super.release();
		mSearchLayout = null;
		mHistoryData = null;
		mResultFragment = null;
		mEmptySearchFragment = null;
		mHistoryFragment = null;
		mNetErrorFragment = null;
		if (mCallPrompt != null) {
			mCallPrompt.cancel();
		}
		if (mCallResult != null) {
			mCallResult.cancel();
		}
	}

	private Call<JsonRespBase<SearchDataResult>> mCallResult;
	private Call<JsonRespBase<SearchPromptResult>> mCallPrompt;
	private long mLastCommitTime = System.currentTimeMillis();

	private class SearchActionListener implements SearchLayout.OnSearchActionListener {

		@Override
		public void onSearchPerform(String keyword, final int id) {
			mLastSearchKey = keyword;
			if (!NetworkUtil.isConnected(SearchActivity.this)) {
				displayNetworkErrUI();
				return;
			}
			long curTime = System.currentTimeMillis();
			if (curTime - mLastCommitTime < Global.CLICK_TIME_INTERVAL) {
				// 防止重复提交
				mLastCommitTime = curTime;
				return;
			}
			saveHistoryData(keyword);
			displayLoadingUI();
			if (mCallResult != null) {
				mCallResult.cancel();
			}
			StatisticsManager.getInstance().trace(getApplicationContext(),
					StatisticsManager.ID.USER_SEARCH,
					StatisticsManager.ID.STR_USER_SEARCH,
					"关键字: " + keyword);
			ReqSearchKey data = new ReqSearchKey();
			data.searchKey = keyword;
			mCallResult = Global.getNetEngine().obtainSearchResult(new JsonReqBase<ReqSearchKey>(data));
			mCallResult.enqueue(new Callback<JsonRespBase<SearchDataResult>>() {
				@Override
				public void onResponse(Call<JsonRespBase<SearchDataResult>> call, Response<JsonRespBase
						<SearchDataResult>> response) {
					if (!mNeedWorkCallback || call.isCanceled()) {
						return;
					}
					if (response != null && response.code() == 200) {
						if (response.body() != null && response.body().isSuccess()) {
							SearchDataResult data = response.body().getData();
							// 检验Key返回数据是否是当前需要的
							if (mSearchLayout != null && !mLastSearchKey.equals(mSearchLayout.getKeyword())) {
								// 丢弃这次搜索结果
								// 不更新
								return;
							}
							if (data.gifts == null || data.gifts.size() == 0) {
								if ((data.games == null || data.games.size() == 0) ||
										!AssistantApp.getInstance().isAllowDownload()) {
									displayEmptyUI();
									return;
								}
							}
							// display list here
							displayDataUI(data, mLastSearchKey, id);
							return;
						}
						if (AppDebugConfig.IS_DEBUG) {
							KLog.e(response.body());
						}
					}
					displayNetworkErrUI();
				}

				@Override
				public void onFailure(Call<JsonRespBase<SearchDataResult>> call, Throwable t) {
					if (!mNeedWorkCallback || call.isCanceled()) {
						return;
					}
					if (AppDebugConfig.IS_DEBUG) {
						KLog.e(AppDebugConfig.TAG_SEARCH, t);
					}
					// 提示网络错误
					displayNetworkErrUI();
				}
			});
		}

		@Override
		public void onSearchCleared() {
			// 取消上一轮搜索
			displayHistoryUI(mHistoryData);
			if (mCallPrompt != null) {
				mCallPrompt.cancel();
			}
			if (mCallResult != null) {
				mCallResult.cancel();
			}
		}


		@Override
		public void onSearchPromptPerform(String keyword) {
			if (mLastInputKey.equals("")) {
				displayPromptUI(null);
			}
			if (!NetworkUtil.isConnected(SearchActivity.this)) {
				if (!mLastInputKey.equals(keyword)) {
					displayPromptUI(null);
				}
				mLastInputKey = keyword;
				return;
			}
			mLastInputKey = keyword;
			if (mCallPrompt != null) {
				mCallPrompt.cancel();
			}
			ReqSearchKey reqData = new ReqSearchKey();
			reqData.searchKey = keyword;
			mCallPrompt = Global.getNetEngine().obtainSearchPrompt(new JsonReqBase<ReqSearchKey>(reqData));
			mCallPrompt.enqueue(new Callback<JsonRespBase<SearchPromptResult>>() {
				@Override
				public void onResponse(Call<JsonRespBase<SearchPromptResult>> call, Response<JsonRespBase
						<SearchPromptResult>> response) {
					if (!mNeedWorkCallback || call.isCanceled()) {
						return;
					}
					String curKeyWord = mSearchLayout.getKeyword().trim();
					if (response != null && response.code() == 200) {
						if (response.body() != null && response.body().getCode() == NetStatusCode.SUCCESS) {
							SearchPromptResult data = response.body().getData();
							// 检验Key返回数据是否是当前需要的
							if (!curKeyWord.equals(data.keyword)) {
								// 丢弃这次搜索结果
								// 不更新
								return;
							}
							// display list here
							displayPromptUI(data.promptList);
							return;
						}
						if (AppDebugConfig.IS_DEBUG) {
							KLog.e(response.body());
						}
					}
					if (curKeyWord.equals(mLastInputKey)) {
						displayPromptUI(null);
					}
				}

				@Override
				public void onFailure(Call<JsonRespBase<SearchPromptResult>> call, Throwable t) {
					if (!mNeedWorkCallback || call.isCanceled()) {
						return;
					}
					if (AppDebugConfig.IS_FRAG_DEBUG) {
						KLog.e(t);
					}
					if (mSearchLayout.getKeyword().trim().equals(mLastInputKey)) {
						displayPromptUI(null);
					}
				}
			});

		}
	} // end internal class
}
