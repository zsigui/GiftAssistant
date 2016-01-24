package com.oplay.giftcool.ui.activity;

import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.config.StatusCode;
import com.oplay.giftcool.engine.NetEngine;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.TimeDataList;
import com.oplay.giftcool.model.json.JsonRespGiftList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.NetErrorFragment;
import com.oplay.giftcool.ui.fragment.gift.GiftLikeListFragment;
import com.oplay.giftcool.ui.fragment.gift.GiftMutilDayFragment;
import com.oplay.giftcool.util.NetworkUtil;
import com.socks.library.KLog;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 15-12-29.
 */
public class GiftListActivity extends BaseAppCompatActivity {

	private NetEngine mEngine;
	private NetErrorFragment mNetErrorFragment;
	private int type = 0;
	private String mGameKey;


	@Override
	@SuppressWarnings("unchecked")
	protected void initView() {
		setContentView(R.layout.activity_common_with_back_white_bg);

		if (getIntent() != null) {
			type = getIntent().getIntExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_DEFAULT);
		}
	}

	@Override
	protected void initMenu(@NonNull Toolbar toolbar) {
		super.initMenu(toolbar);
		switch (type) {
			case KeyConfig.TYPE_ID_GIFT_LIMIT:
				setBarTitle("今日限量礼包");
				break;
			case KeyConfig.TYPE_ID_GIFT_NEW:
				setBarTitle("新鲜出炉礼包");
				break;
			case KeyConfig.TYPE_ID_GIFT_LIKE:
				setBarTitle("猜你喜欢");
				break;
		}

	}

	@Override
	protected void processLogic() {
		mEngine = Global.getNetEngine();
		mNeedWorkCallback = true;
		loadData();
	}

	public void loadData() {
		if (mIsLoading) {
			return;
		}
		mIsLoading = true;
		displayLoadingUI(R.id.fl_container);
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (type == KeyConfig.TYPE_ID_GIFT_LIMIT) {
					loadLimitGiftData();
				} else if (type == KeyConfig.TYPE_ID_GIFT_NEW) {
					loadNewGiftData();
				} else if (type == KeyConfig.TYPE_ID_GIFT_LIKE) {
					mGameKey = getIntent().getStringExtra(KeyConfig.KEY_DATA);
					displayGiftLikeUI(mGameKey);
				}
			}
		}).start();
	}

	private void loadNewGiftData() {
		if (!NetworkUtil.isConnected(this)) {
			displayNetworkErrUI();
			return;
		}
		mEngine.obtainGiftNew(new JsonReqBase<String>()).enqueue(new Callback<JsonRespGiftList>() {
			@Override
			public void onResponse(Response<JsonRespGiftList> response, Retrofit retrofit) {
				if (!mNeedWorkCallback) {
					return;
				}
				mIsLoading = false;
				if (response != null && response.isSuccess()) {
					if (response.body() != null && response.body().getCode() == StatusCode.SUCCESS) {
						displayGiftNewUI(response.body().getData());
						return;
					}
					if (AppDebugConfig.IS_DEBUG) {
						KLog.d(AppDebugConfig.TAG_APP, (response.body() == null ? "解析失败" : response.body().error()));
					}
				}
				// 加载错误页面也行
				displayNetworkErrUI();
			}

			@Override
			public void onFailure(Throwable t) {
				if (!mNeedWorkCallback) {
					return;
				}
				mIsLoading = false;
				if (AppDebugConfig.IS_DEBUG) {
					KLog.e(t);
				}
				displayNetworkErrUI();
			}
		});
	}

	private void loadLimitGiftData() {
		if (!NetworkUtil.isConnected(this)) {
			displayNetworkErrUI();
			return;
		}
		mEngine.obtainGiftLimit(new JsonReqBase<String>()).enqueue(new Callback<JsonRespGiftList>() {
			@Override
			public void onResponse(Response<JsonRespGiftList> response, Retrofit retrofit) {
				if (!mNeedWorkCallback) {
					return;
				}
				mIsLoading = false;
				if (response != null && response.isSuccess()) {
					if (response.body() != null && response.body().getCode() == StatusCode.SUCCESS) {
						displayGiftLimitUI(response.body().getData());
						return;
					}
					if (AppDebugConfig.IS_DEBUG) {
						KLog.d(AppDebugConfig.TAG_APP, (response.body() == null ? "解析失败" : response.body().error()));
					}
				}
				// 加载错误页面也行
				displayNetworkErrUI();
			}

			@Override
			public void onFailure(Throwable t) {
				if (!mNeedWorkCallback) {
					return;
				}
				mIsLoading = false;
				if (AppDebugConfig.IS_DEBUG) {
					KLog.e(t);
				}
				displayNetworkErrUI();
			}
		});
	}

	private void displayGiftLikeUI(String gameKey) {
		replaceFrag(R.id.fl_container, GiftLikeListFragment.newInstance(gameKey),
				GiftLikeListFragment.class.getSimpleName(), false);
	}

	private void displayGiftLimitUI(ArrayList<TimeDataList<IndexGiftNew>> data) {
		replaceFrag(R.id.fl_container, GiftMutilDayFragment.newInstance(data, NetUrl.GIFT_GET_ALL_LIMIT_BY_PAGE),
				GiftMutilDayFragment.class.getSimpleName(), false);
	}

	private void displayGiftNewUI(ArrayList<TimeDataList<IndexGiftNew>> data) {
		replaceFrag(R.id.fl_container, GiftMutilDayFragment.newInstance(data, NetUrl.GIFT_GET_ALL_NEW_BY_PAGE),
				GiftMutilDayFragment.class.getSimpleName(), false);
	}

	/**
	 * 显示网络错误提示
	 */
	private void displayNetworkErrUI() {
		if (mNetErrorFragment == null) {
			mNetErrorFragment = NetErrorFragment.newInstance();
			mNetErrorFragment.setOnRetryListener(new NetErrorFragment.OnRetryListener() {
				@Override
				public void onRetry() {
					loadData();
				}
			});
		}
		reattachFrag(R.id.fl_container, mNetErrorFragment, mNetErrorFragment.getClass().getSimpleName());
	}

}