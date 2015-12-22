package com.oplay.giftassistant.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.widget.FrameLayout;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.SPConfig;
import com.oplay.giftassistant.config.StatusCode;
import com.oplay.giftassistant.engine.SearchEngine;
import com.oplay.giftassistant.model.data.resp.SearchData;
import com.oplay.giftassistant.model.json.JsonRespSearchData;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.widget.search.SearchLayout;
import com.oplay.giftassistant.util.NetworkUtil;
import com.oplay.giftassistant.util.SPUtil;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 15-12-22.
 */
public class SearchActivity extends BaseAppCompatActivity {

    private SearchLayout mSearchLayout;
    private FrameLayout flSearchContainer;
    private FragmentManager mFragmentManager;
    private SearchEngine mEngine;
    private List<String> mHistoryData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        processLogic();
    }

    private void processLogic() {
        obtainHistoryData();
        mEngine = mApp.getRetrofit().create(SearchEngine.class);
        mFragmentManager = getSupportFragmentManager();
        mSearchLayout.setSearchActionListener(new SearchLayout.OnSearchActionListener() {
            @Override
            public void onSearchPerform(String keyword) {
                if (NetworkUtil.isConnected(SearchActivity.this)) {
                    showLoadingDialog();
                    mEngine.getSearchData(keyword).enqueue(new Callback<JsonRespSearchData>() {
                        @Override
                        public void onResponse(Response<JsonRespSearchData> response, Retrofit retrofit) {
                            hideLoadingDialog();
                            if (response.code() == 200) {
                                if (response.body().getCode() == StatusCode.SUCCESS) {
                                    SearchData data = response.body().getData();
                                    // 检验Key返回数据是否是当前需要的
                                    if (!data.getKeyword().trim().equals(mSearchLayout.getKeyword())) {
                                        // 丢弃这次搜索结果
                                        // 不更新
                                        return;
                                    }
                                    if (data.getGameList() == null && data.getGiftList() == null) {
                                        displayEmptyUI();
                                        return;
                                    }
                                    // displayListHere
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
                            hideLoadingDialog();
                            if (AppDebugConfig.IS_DEBUG) {
                                KLog.e(t);
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
                displayHistoryUI();
            }

            @Override
            public void onSearchPromptPerform(String keyword) {
                if (NetworkUtil.isConnected(SearchActivity.this)) {
                    mEngine.getSearchPromt(keyword).enqueue(new Callback<JsonRespBase<List<String>>>() {
                        @Override
                        public void onResponse(Response<JsonRespBase<List<String>>> response, Retrofit retrofit) {

                        }

                        @Override
                        public void onFailure(Throwable t) {

                        }
                    });
                }
            }
        });
    }

    private void obtainHistoryData() {
        mHistoryData = new ArrayList<>(10);
        String s = SPUtil.getString(getApplicationContext(),
                SPConfig.SP_SEARCH_FILE,
                SPConfig.SP_SEARCH_INDEX_KEY,
                null);
        if (TextUtils.isEmpty(s)) {
            return;
        }
        String[] keys = s.split("\r");
        Collections.addAll(mHistoryData, keys);

    }

    private void displayHistoryUI() {
    }

    /**
     * 显示搜索结果界面
     *
     * @param data
     */
    private void displayDataUI(SearchData data) {

    }

    /**
     * 显示搜索结果为空界面
     */
    private void displayEmptyUI() {

    }

    /**
     * 显示网络错误提示
     */
    private void displayNetworkErrUI() {
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_search);
        mSearchLayout = getViewById(R.id.sl_search);

    }


}
