package com.oplay.giftcool.ui.fragment.setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.MyAttentionListAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.GameTypeUtil;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.config.TypeStatusCode;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.manager.DialogManager;
import com.oplay.giftcool.model.data.req.ReqChangeFocus;
import com.oplay.giftcool.model.data.req.ReqPageData;
import com.oplay.giftcool.model.data.resp.MyAttention;
import com.oplay.giftcool.model.data.resp.OneTypeDataList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.activity.MainActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.ui.fragment.dialog.ConfirmDialog;
import com.oplay.giftcool.ui.fragment.game.GameFragment;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

import java.util.ArrayList;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * 我的关注页面
 * <p/>
 * Created by zsigui on 16-3-8.
 */
public class MyAttentionFragment extends BaseFragment_Refresh<MyAttention> implements OnItemClickListener<MyAttention> {

	private static final String PAGE_NAME = "我的关注页面";

	private final int PAGE_SIZE = 5;

	private String DIALOG_QUICK_TITLE;
	private String DIALOG_QUICK_CONTENT;

	// 空页面
	private TextView btnToGet;
	private ListView lvContent;

	private JsonReqBase<ReqPageData> mReqPageObj;
	private MyAttentionListAdapter mAdapter;

	public static MyAttentionFragment newInstance() {
		return new MyAttentionFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_attention_data);
		View emptyView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_attention_empty, null);
		mViewManager.setEmptyView(emptyView);
		btnToGet = getViewById(emptyView, R.id.btn_to_get);
		lvContent = getViewById(R.id.lv_content);
	}

	@Override
	protected void setListener() {
		btnToGet.setOnClickListener(this);
		mAdapter.setItemClickListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		DIALOG_QUICK_TITLE = getResources().getString(R.string.st_quick_focus_title);
		DIALOG_QUICK_CONTENT = getResources().getString(R.string.st_quick_focus_content);
		mAdapter = new MyAttentionListAdapter(getContext(), null);
		lvContent.setAdapter(mAdapter);

		ReqPageData reqPageData = new ReqPageData();
		reqPageData.page = PAGE_FIRST;
		reqPageData.pageSize = PAGE_SIZE;
		mReqPageObj = new JsonReqBase<ReqPageData>(reqPageData);
	}

	@Override
	protected void lazyLoad() {
		refreshInitConfig();

		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (NetworkUtil.isConnected(getContext())) {
					mReqPageObj.data.page = PAGE_FIRST;
					Global.getNetEngine().obtainAttentionMessage(mReqPageObj)
							.enqueue(new Callback<JsonRespBase<OneTypeDataList<MyAttention>>>() {
								@Override
								public void onResponse(Response<JsonRespBase<OneTypeDataList<MyAttention>>> response,
								                       Retrofit retrofit) {
									if (!mCanShowUI) {
										return;
									}
									if (response != null && response.isSuccess() && response.body() != null &&
											response.body().getCode() == NetStatusCode.SUCCESS) {
										refreshSuccessEnd();
										OneTypeDataList<MyAttention> backObj = response.body().getData();
										refreshLoadState(backObj.data, backObj.isEndPage);
										updateData(backObj.data);
										return;
									}
									refreshFailEnd();
								}

								@Override
								public void onFailure(Throwable t) {
									if (!mCanShowUI) {
										return;
									}
									if (AppDebugConfig.IS_DEBUG) {
										KLog.e(AppDebugConfig.TAG_FRAG, t);
									}
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
		if (mNoMoreLoad || mIsLoadMore) {
			return;
		}
		mIsLoadMore = true;
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (!NetworkUtil.isConnected(getContext())) {
					mViewManager.showErrorRetry();
					return;
				}
				mReqPageObj.data.page = mLastPage + 1;
				Global.getNetEngine().obtainAttentionMessage(mReqPageObj)
						.enqueue(new Callback<JsonRespBase<OneTypeDataList<MyAttention>>>() {
							@Override
							public void onResponse(Response<JsonRespBase<OneTypeDataList<MyAttention>>> response,
							                       Retrofit
									                       retrofit) {
								if (!mCanShowUI) {
									return;
								}
								if (response != null && response.isSuccess() && response.body() != null &&
										response.body().getCode() == NetStatusCode.SUCCESS) {
									moreLoadSuccessEnd();
									OneTypeDataList<MyAttention> backObj = response.body().getData();
									setLoadState(backObj.data, backObj.isEndPage);
									addMoreData(backObj.data);
									return;
								}
								moreLoadFailEnd();
							}

							@Override
							public void onFailure(Throwable t) {
								if (!mCanShowUI) {
									return;
								}
								moreLoadFailEnd();
							}
						});
			}
		});

	}


	/**
	 * 刷新当前数据
	 */
	public void updateData(ArrayList<MyAttention> data) {
		if (data == null || data.size() == 0) {
			mViewManager.showEmpty();
			return;
		}
		mViewManager.showContent();
		mHasData = true;
		mData = data;
		mAdapter.updateData(mData);
		mLastPage = PAGE_FIRST;
	}

	/**
	 * 添加更多数据
	 */
	private void addMoreData(ArrayList<MyAttention> moreData) {
		if (moreData == null) {
			return;
		}
		mData.addAll(moreData);
		mAdapter.updateData(mData);
		mLastPage += 1;
	}

	/**
	 * 移除特定数据
	 */
	private void removeData(int gameId) {
		for (int i = mData.size() - 1; i >= 0; i--) {
			if (mData.get(i).id == gameId) {
				mData.remove(i);
				break;
			}
		}
		updateData(mData);
		if (!mIsNotifyRefresh) {
			mIsNotifyRefresh = true;
			lazyLoad();
		}
	}

	@Override
	public void release() {
		super.release();
		if (mQuickFocusCall != null) {
			mQuickFocusCall.cancel();
			mQuickFocusCall = null;
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.btn_to_get:
				// 跳转游戏榜单界面
				if (MainActivity.sGlobalHolder == null) {
					IntentUtil.jumpGameNewList(getContext());
				} else {
					MainActivity.sGlobalHolder.jumpToIndexGame(GameFragment.INDEX_SUPER);
					getActivity().finish();
				}
				break;
		}
	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}

	private long mLastClickTime = 0;

	@Override
	public void onItemClick(MyAttention item, View view, int position) {
		switch (view.getId()) {
			case R.id.rl_recommend:
				IntentUtil.jumpGameDetail(getContext(), item.id, GameTypeUtil.JUMP_STATUS_GIFT);
				break;
			case R.id.btn_quick_focus:
				long curTime = System.currentTimeMillis();
				if (curTime - mLastClickTime < Global.CLICK_TIME_INTERVAL) {
					mLastClickTime = curTime;
					return;
				}
				handleQuickFocus(item.id);
				break;
		}
	}

	/*----------- 处理点击取消关注后的弹窗事件 ----------------*/

	private QuickDialogListener mDialogListener;

	private void handleQuickFocus(int id) {
		final ConfirmDialog dialog = ConfirmDialog.newInstance();
		dialog.setContent(DIALOG_QUICK_CONTENT);
		dialog.setTitle(DIALOG_QUICK_TITLE);
		if (mDialogListener == null) {
			mDialogListener = new QuickDialogListener();
		}
		mDialogListener.setId(id);
		mDialogListener.setDialog(dialog);
		dialog.setListener(mDialogListener);
		dialog.show(getChildFragmentManager(), "quick_focus");
	}

	private Call<JsonRespBase<Void>> mQuickFocusCall;

	class QuickDialogListener implements BaseFragment_Dialog.OnDialogClickListener {

		private ConfirmDialog mDialog;
		private JsonReqBase<ReqChangeFocus> mReqBase;
		private static final String TAG_PREFIX = "取消关注";

		public QuickDialogListener() {
			ReqChangeFocus focus = new ReqChangeFocus();
			focus.status = TypeStatusCode.FOCUS_OFF;
			mReqBase = new JsonReqBase<>(focus);
		}

		public void setDialog(ConfirmDialog dialog) {
			mDialog = dialog;
		}

		public void setId(int id) {
			mReqBase.data.gameId = id;
		}

		@Override
		public void onCancel() {
			mDialog.dismissAllowingStateLoss();
		}

		@Override
		public void onConfirm() {
			mDialog.dismissAllowingStateLoss();
			DialogManager.getInstance().showLoadingDialog(getChildFragmentManager());
			if (!NetworkUtil.isConnected(getContext())) {
				DialogManager.getInstance().hideLoadingDialog();
				return;
			}
			mQuickFocusCall = Global.getNetEngine().changeGameFocus(mReqBase);
			mQuickFocusCall.enqueue(new Callback<JsonRespBase<Void>>() {
				@Override
				public void onResponse(Response<JsonRespBase<Void>> response, Retrofit retrofit) {
					DialogManager.getInstance().hideLoadingDialog();
					if (response != null && response.isSuccess()) {
						if (response.body() != null && response.body().isSuccess()) {
							ToastUtil.showShort(TAG_PREFIX + "-成功");
							removeData(mReqBase.data.gameId);
							return;
						}
						ToastUtil.blurErrorMsg(TAG_PREFIX, response.body());
						return;
					}
					ToastUtil.blurErrorResp(TAG_PREFIX, response);

				}

				@Override
				public void onFailure(Throwable t) {
					DialogManager.getInstance().hideLoadingDialog();
					if (!mCanShowUI) {
						return;
					}
					ToastUtil.blurThrow(TAG_PREFIX);
				}
			});
		}
	}


}
