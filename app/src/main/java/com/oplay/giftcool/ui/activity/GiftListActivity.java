package com.oplay.giftcool.ui.activity;

import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.gift.GiftLikeListFragment;
import com.oplay.giftcool.ui.fragment.gift.GiftLimitListDataNewFragment;
import com.oplay.giftcool.util.IntentUtil;

/**
 * Created by zsigui on 15-12-29.
 */
public class GiftListActivity extends BaseAppCompatActivity {

//	private NetErrorFragment mNetErrorFragment;
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
				setBarTitle("珍贵限量礼包");
				break;
//			case KeyConfig.TYPE_ID_GIFT_NEW:
//				setBarTitle("新鲜出炉礼包");
//				break;
			case KeyConfig.TYPE_ID_GIFT_LIKE:
				setBarTitle("猜你喜欢");
				break;
		}

	}

	@Override
	protected void processLogic() {
		mNeedWorkCallback = true;
		loadData();
	}

	public void loadData() {
		if (mIsLoading) {
			return;
		}
		mIsLoading = true;
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (type == KeyConfig.TYPE_ID_GIFT_LIMIT) {
					displayGiftLimitUI();
				}
//				else if (type == KeyConfig.TYPE_ID_GIFT_NEW) {
				// 已废弃
//					loadNewGiftData();
//				}
				else if (type == KeyConfig.TYPE_ID_GIFT_LIKE) {
					mGameKey = getIntent().getStringExtra(KeyConfig.KEY_DATA);
					displayGiftLikeUI(mGameKey);
				}
			}
		});
	}


	/**
	 * 加载新鲜出炉礼包数据的网络请求声明
	 */
//	private Call<JsonRespGiftList> mCallLoadNew;
//
//	private void loadNewGiftData() {
//		if (!NetworkUtil.isConnected(this)) {
//			displayNetworkErrUI();
//			return;
//		}
//		if (mCallLoadNew != null) {
//			mCallLoadNew.cancel();
//			mCallLoadNew = mCallLoadNew.clone();
//		} else {
//			mCallLoadNew = mEngine.obtainGiftNew(new JsonReqBase<String>());
//		}
//		mCallLoadNew.enqueue(new Callback<JsonRespGiftList>() {
//			@Override
//			public void onResponse(Call<JsonRespGiftList> call, Response<JsonRespGiftList> response) {
//				if (!mNeedWorkCallback || call.isCanceled()) {
//					return;
//				}
//				mIsLoading = false;
//				if (response != null && response.isSuccessful()) {
//					if (response.body() != null && response.body().getCode() == NetStatusCode.SUCCESS) {
//						displayGiftNewUI(response.body().getData());
//						return;
//					}
//					if (AppDebugConfig.IS_DEBUG) {
//						KLog.d(AppDebugConfig.TAG_APP, (response.body() == null ? "解析失败" : response.body().error()));
//					}
//				}
//				// 加载错误页面也行
//				displayNetworkErrUI();
//			}


	private void displayGiftLikeUI(String gameKey) {
		replaceFrag(R.id.fl_container, GiftLikeListFragment.newInstance(gameKey),
				GiftLikeListFragment.class.getSimpleName(), false);
	}

	private void displayGiftLimitUI() {
		replaceFrag(R.id.fl_container, GiftLimitListDataNewFragment.newInstance(),
				GiftLimitListDataNewFragment.class.getSimpleName(), false);
	}

//	private void displayGiftNewUI(ArrayList<TimeDataList<IndexGiftNew>> data) {
//		replaceFrag(R.id.fl_container, GiftMutilDayFragment.newInstance(data, NetUrl.GIFT_GET_ALL_NEW_BY_PAGE),
//				GiftMutilDayFragment.class.getSimpleName(), false);
//	}

	/**
	 * 显示网络错误提示
	 */
//	private void displayNetworkErrUI() {
//		if (mNetErrorFragment == null) {
//			mNetErrorFragment = NetErrorFragment.newInstance();
//			mNetErrorFragment.setOnRetryListener(new NetErrorFragment.OnRetryListener() {
//				@Override
//				public void onRetry() {
//					loadData();
//				}
//			});
//		}
//		replaceFrag(R.id.fl_container, mNetErrorFragment, false);
//	}

	@Override
	protected void doBeforeFinish() {
		super.doBeforeFinish();
		if (MainActivity.sGlobalHolder == null) {
			IntentUtil.jumpHome(this, false);
		}
	}

	@Override
	public void release() {
		super.release();
//		if (mCallLoadNew != null) {
//			mCallLoadNew.cancel();
//			mCallLoadNew = null;
//		}
	}
}
