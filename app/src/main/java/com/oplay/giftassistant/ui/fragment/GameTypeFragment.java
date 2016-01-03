package com.oplay.giftassistant.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.GameNoticeAdapter;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.model.data.req.ReqPageData;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;
import com.oplay.giftassistant.model.data.resp.OneTypeDataList;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;

import java.util.ArrayList;

import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 15-12-30.
 */
public class GameTypeFragment extends BaseFragment{

	private ArrayList<IndexGameNew> mData;
	private int mNextPage = 0;
	private JsonReqBase<ReqPageData> mPage;

	private BGARefreshLayout mRefreshLayout;
	private RecyclerView mDataView;
	private GameNoticeAdapter mAdapter;

	public static GameTypeFragment newInstance() {
		return new GameTypeFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_game_type);
		mRefreshLayout = getViewById(R.id.srl_layout);
		mDataView = getViewById(R.id.rv_container);
	}

	@Override
	protected void setListener() {

	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		ReqPageData data = new ReqPageData();
		data.page = mNextPage;
		mPage = new JsonReqBase<ReqPageData>(data);
	}

	@Override
	protected void lazyLoad() {
		mIsLoading = true;
		mPage.data.page = mNextPage;
		Global.getNetEngine().obtainIndexGameNotice(mPage)
				.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGameNew>>>() {
					@Override
					public void onResponse(Response<JsonRespBase<OneTypeDataList<IndexGameNew>>> response, Retrofit
							retrofit) {

					}

					@Override
					public void onFailure(Throwable t) {

					}
				});
	}
}
