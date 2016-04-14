package com.oplay.giftcool.ui.fragment.setting;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.ScoreTaskAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.config.util.TaskTypeUtil;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.model.data.resp.ScoreMission;
import com.oplay.giftcool.model.data.resp.ScoreMissionGroup;
import com.oplay.giftcool.model.data.resp.TaskInfoOne;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-1-6.
 */
public class TaskFragment extends BaseFragment implements OnItemClickListener<ScoreMission>,
		ObserverManager.UserActionListener {


	private final static String PAGE_NAME = "金币任务";
	private TextView tvScore;
	private ListView mDataView;
	private ArrayList<ScoreMission> mData;
	private ScoreTaskAdapter mAdapter;
	private int mCurFinishedTask = 0;


	public static TaskFragment newInstance() {
		return new TaskFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		if (!AccountManager.getInstance().isLogin()) {
			ToastUtil.showShort(mApp.getResources().getString(R.string.st_hint_un_login));
			IntentUtil.jumpLogin(getContext());
			if (getActivity() != null) {
				getActivity().finish();
			}
			return;
		}
		initViewManger(R.layout.fragment_score_task);
		tvScore = getViewById(R.id.tv_score);
		mDataView = getViewById(R.id.lv_container);
	}

	@Override
	protected void setListener() {
		ObserverManager.getInstance().addUserUpdateListener(this);
		ObserverManager.getInstance().addUserActionListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		tvScore.setText(String.valueOf(AccountManager.getInstance().getUserInfo().score));
		mAdapter = new ScoreTaskAdapter(getContext(), this);
		mDataView.setAdapter(mAdapter);
		ScoreManager.getInstance().setInWorking(true);
		AccountManager.getInstance().updatePartUserInfo();

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ObserverManager.getInstance().removeUserUpdateListener(this);
		ObserverManager.getInstance().removeActionListener(this);
	}

	/**
	 * 获取任务列表信息的网络请求声明
	 */
	private Call<JsonRespBase<ArrayList<ScoreMissionGroup>>> mCall;

	@Override
	protected void lazyLoad() {
		if (mIsLoading) {
			return;
		}
		refreshInitConfig();

		if (!NetworkUtil.isConnected(getContext())) {
			refreshFailEnd();
			return;
		}
		if (mCall != null) {
			mCall.cancel();
			mCall = mCall.clone();
		} else {
			mCall = Global.getNetEngine().obtainScoreTask(new JsonReqBase<String>());
		}
		mCall.enqueue(new Callback<JsonRespBase<ArrayList<ScoreMissionGroup>>>() {
			@Override
			public void onResponse(Call<JsonRespBase<ArrayList<ScoreMissionGroup>>> call,
			                       Response<JsonRespBase<ArrayList<ScoreMissionGroup>>> response) {
				if (!mCanShowUI || call.isCanceled()) {
					return;
				}

				if (response != null && response.isSuccessful()) {
					if (response.body() != null && response.body().getCode() == NetStatusCode.SUCCESS) {
						ArrayList<ScoreMissionGroup> missionGroups = response.body().getData();
						setTaskIcon(mData);
						mAdapter.updateData(transferToMissionList(missionGroups));
						refreshSuccessEnd();
						return;
					}
					if (AppDebugConfig.IS_FRAG_DEBUG) {
						KLog.e(AppDebugConfig.TAG_FRAG,
								response.body() == null ? "解析失败" : response.body().error());
					}
					ToastUtil.showShort("获取任务列表失败 - "
							+ (response.body() == null ? "解析失败" : response.body().error()));
				}
				refreshFailEnd();
			}

			@Override
			public void onFailure(Call<JsonRespBase<ArrayList<ScoreMissionGroup>>> call, Throwable t) {
				if (!mCanShowUI || call.isCanceled()) {
					return;
				}
				if (AppDebugConfig.IS_FRAG_DEBUG) {
					KLog.e(AppDebugConfig.TAG_FRAG, t);
				}
				refreshFailEnd();
			}
		});
	}

	/**
	 * 将各任务分组列表提取出来合并成一个列表
	 */
	private ArrayList<ScoreMission> transferToMissionList(ArrayList<ScoreMissionGroup> missionGroups) {
		ArrayList<ScoreMission> missions = new ArrayList<>();
		for (ScoreMissionGroup missionGroup : missionGroups) {
			final ScoreMission groupHeader = new ScoreMission();
			groupHeader.name = String.format("%s(%d/%d)",
					missionGroup.name, missionGroup.completedCount, missionGroup.totalCount);
			missions.add(groupHeader);
			missions.addAll(missionGroup.missions);
		}
		return missions;
	}


	@Override
	public void onItemClick(ScoreMission item, View view, int position) {
		if (mData == null || mData.size() == 0) {
			if (AppDebugConfig.IS_FRAG_DEBUG) {
				KLog.e(AppDebugConfig.TAG_FRAG, "Empty or Null Data On Item Click! mData = " + mData);
			}
			return;
		}
		handleMission(item);
	}

	private void setTaskIcon(ArrayList<ScoreMission> data) {
		if (data == null) {
			if (AppDebugConfig.IS_FRAG_DEBUG) {
				KLog.e(AppDebugConfig.TAG_FRAG, "任务失败");
			}
			return;
		}
		for (ScoreMission mission : data) {
			if (!TextUtils.isEmpty(mission.icon)) {
				continue;
			}
			String id = mission.code;
			switch (id) {
				case TaskTypeUtil.ID_SET_NICK:
					mission.iconAlternate = R.drawable.ic_task_set_nick;
					break;
				case TaskTypeUtil.ID_SET_AVATAR:
					mission.iconAlternate = R.drawable.ic_task_upload_avator;
					break;
				case TaskTypeUtil.ID_FIRST_LOGIN:
					mission.iconAlternate = R.drawable.ic_task_first_login;
					break;
				case TaskTypeUtil.ID_FOCUS_GAME:
					mission.iconAlternate = R.drawable.ic_task_attention;
					break;
				case TaskTypeUtil.ID_REQUEST_GIFT:
					mission.iconAlternate = R.drawable.ic_task_default;
					break;
				case TaskTypeUtil.ID_FEEDBACK:
					mission.iconAlternate = R.drawable.ic_task_new_feedback;
					break;
				case TaskTypeUtil.ID_UPGRADE:
					mission.iconAlternate = R.drawable.ic_task_new_update;
					break;
				case TaskTypeUtil.ID_SIGN_IN:
					mission.iconAlternate = R.drawable.ic_task_sign_in;
					break;
				case TaskTypeUtil.ID_GCOOL_SHARE:
					mission.iconAlternate = R.drawable.ic_task_share_gcool;
					break;
				case TaskTypeUtil.ID_GIFT_SHARE:
					mission.iconAlternate = R.drawable.ic_task_share_gift;
					break;
				case TaskTypeUtil.ID_PLAY_GAME:
					mission.iconAlternate = R.drawable.ic_task_play_game;
					break;
				case TaskTypeUtil.ID_BUG_GIFT_USE_OUWAN:
					mission.iconAlternate = R.drawable.ic_task_get_limit_with_bean;
					break;
				default:
					mission.iconAlternate = R.drawable.ic_task_default;
					break;
			}
		}
	}

	/**
	 * 处理特定任务事件
	 */
	private void handleMission(ScoreMission mission) {
		if (mission == null) return;
		if (getContext() == null || !(getContext() instanceof BaseAppCompatActivity)) {
			ToastUtil.showShort(ConstString.TEXT_ENTER_ERROR);
			return;
		}
		try {
			switch (mission.actionType) {
				case TaskTypeUtil.MISSION_TYPE_JUMP_PAGE:
					final TaskInfoOne taskInfo = AssistantApp.getInstance().getGson().fromJson(
							mission.actionInfo, TaskInfoOne.class);
					IntentUtil.jumpByTaskInfoOne(getContext(), taskInfo);
					break;
				case TaskTypeUtil.MISSION_TYPE_EXECUTE_LOGIC:
					break;
				case TaskTypeUtil.MISSION_TYPE_DOWNLOAD:
					break;
			}
		} catch (Throwable t) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_FRAG, t);
			}
		}
//		String id = mission.id;
//		ScoreManager.getInstance().setInWorking(true);
//		if (id.equals(TaskTypeUtil.ID_SET_NICK)) {
//			// 跳转到设置用户昵称信息界面
////			((BaseAppCompatActivity) getContext()).replaceFragWithTitle(R.id.fl_container,
////					SetNickFragment.newInstance(), getResources().getString(R.string.st_user_set_nick_title));
//			TaskInfoOne infoOne = new TaskInfoOne();
//			infoOne.action = "Setting";
//			infoOne.type = KeyConfig.TYPE_ID_USER_SET_NICK;
//			IntentUtil.jumpByTaskInfoOne(getActivity(), infoOne);
//		} else if (id.equals(TaskTypeUtil.ID_UPLOAD_AVATOR)) {
//			// 跳转到设置用户头像信息界面
////			((BaseAppCompatActivity) getContext()).replaceFragWithTitle(R.id.fl_container,
////					UploadAvatarFragment.newInstance(), getResources().getString(R.string.st_user_avator));
//			TaskInfoOne infoOne = new TaskInfoOne();
//			infoOne.action = "Setting";
//			infoOne.type = KeyConfig.TYPE_ID_USER_SET_AVATAR;
//			IntentUtil.jumpByTaskInfoOne(getActivity(), infoOne);
//		} else if (id.equals(TaskTypeUtil.ID_BIND_PHONE)) {
//			// 跳转到绑定手机账号界面
//			OuwanSDKManager.getInstance().showBindPhoneView(getContext());
//		} else if (id.equals(TaskTypeUtil.ID_BIND_OUWAN)) {
//			// 跳转到绑定偶玩账号界面
//			OuwanSDKManager.getInstance().showBindOuwanView(getContext());
//		} else if (id.equals(TaskTypeUtil.ID_FEEDBACK)) {
//			// 跳转反馈界面
////			((BaseAppCompatActivity) getContext()).replaceFragWithTitle(R.id.fl_container,
////					FeedBackFragment.newInstance(), getResources().getString(R.string.st_feedback_title));
//			TaskInfoOne infoOne = new TaskInfoOne();
//			infoOne.action = "Setting";
//			infoOne.type = KeyConfig.TYPE_ID_FEEDBACK;
//			IntentUtil.jumpByTaskInfoOne(getActivity(), infoOne);
//		} else if (id.equals(TaskTypeUtil.ID_SEARCH)) {
//			// 跳转搜索礼包/游戏界面
//			IntentUtil.jumpSearch(getContext());
//		} else if (id.equals(TaskTypeUtil.ID_JUDGE_GAME)) {
//			// 评论
//		} else if (id.equals(TaskTypeUtil.ID_STAR_COMMENT)) {
//			// 为某条评论点赞，暂无
//		} else if (id.equals(TaskTypeUtil.ID_LOGIN)) {
//		} else if (id.equals(TaskTypeUtil.ID_DOWNLOAD)) {
//			// 跳转游戏榜单界面
////			if (MainActivity.sGlobalHolder == null) {
////				IntentUtil.jumpGameNewList(getContext());
////			} else {
////				MainActivity.sGlobalHolder.jumpToIndexGame(GameFragment.INDEX_NOTICE);
////				if (getActivity() != null) {
////					getActivity().finish();
////				}
////			}
//			TaskInfoOne infoOne = new TaskInfoOne();
//			infoOne.action = "Main";
//			infoOne.type = KeyConfig.TYPE_ID_INDEX_GAME;
//			infoOne.data = String.valueOf(GameFragment.INDEX_NOTICE);
//			IntentUtil.jumpByTaskInfoOne(getActivity(), infoOne);
//		} else if (id.equals(TaskTypeUtil.ID_SHARE_NORMAL_GIFT)) {
//			// 分享普通礼包
////			if (MainActivity.sGlobalHolder == null) {
////				IntentUtil.jumpGiftNewList(getContext());
////			} else {
////				MainActivity.sGlobalHolder.jumpToIndexGift(GiftFragment.POS_NEW);
////				if (getActivity() != null) {
////					getActivity().finish();
////				}
////			}
//			TaskInfoOne infoOne = new TaskInfoOne();
//			infoOne.action = "Main";
//			infoOne.type = KeyConfig.TYPE_ID_INDEX_GIFT;
//			infoOne.data = String.valueOf(GiftFragment.POS_NEW);
//			IntentUtil.jumpByTaskInfoOne(getActivity(), infoOne);
//		} else if (id.equals(TaskTypeUtil.ID_SHARE_LIMIT_GIFT)) {
//			// 分享限量礼包
////			IntentUtil.jumpGiftLimitList(getContext(), false);
//			TaskInfoOne infoOne = new TaskInfoOne();
//			infoOne.action = "GiftList";
//			infoOne.type = KeyConfig.TYPE_ID_GIFT_LIMIT;
//			IntentUtil.jumpByTaskInfoOne(getActivity(), infoOne);
//		} else if (id.equals(TaskTypeUtil.ID_GET_LIMIT_WITH_BEAN)) {
//			// 使用偶玩豆购买限量礼包，跳转今日限量界面
////			IntentUtil.jumpGiftLimitList(getContext(), false);
//			TaskInfoOne infoOne = new TaskInfoOne();
//			infoOne.action = "GiftList";
//			infoOne.type = KeyConfig.TYPE_ID_GIFT_LIMIT;
//			IntentUtil.jumpByTaskInfoOne(getActivity(), infoOne);
//		} else if (id.equals(TaskTypeUtil.ID_DOWNLOAD_SPECIFIED)) {
//			// 跳转指定游戏界面，暂无
//			TaskInfoOne infoOne = new TaskInfoOne();
//			infoOne.action = "GameDetail";
//			infoOne.type = 10998;
//			infoOne.data = "1";
//			IntentUtil.jumpByTaskInfoOne(getActivity(), infoOne);
//		} else if (id.equals(TaskTypeUtil.ID_CONTINUOUS_LOGIN)) {
//		} else if (TaskTypeUtil.ID_LOGIN_SPECIFIED.equals(id)) {
//			// ignored
//		} else if (TaskTypeUtil.ID_SHARE_GIFT_COOL.equals(id)) {
//			// 进行礼包酷分享
//			ShareSDKManager.getInstance(getContext()).shareGCool(getContext(), getChildFragmentManager());
//		} else {
//			if (AppDebugConfig.IS_FRAG_DEBUG) {
//				KLog.e("error id " + id);
//			}
//		}
	}

	@Override
	public void onUserUpdate(int action) {
		switch (action) {
			case ObserverManager.STATUS.USER_UPDATE_PART:
				if (tvScore != null && AccountManager.getInstance().isLogin()) {
					tvScore.setText(String.valueOf(AccountManager.getInstance().getUserInfo().score));
				}
				break;
			case ObserverManager.STATUS.USER_UPDATE_ALL:
			case ObserverManager.STATUS.USER_UPDATE_TASK:
				if (mIsNotifyRefresh) {
					return;
				}
				mIsNotifyRefresh = true;
				lazyLoad();
				break;
		}
	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}

	@Override
	public void onUserActionFinish(int action, int code) {
		switch (action) {
			case ObserverManager.UserActionListener.ACTION_BIND_OUWAN:
				ScoreManager.getInstance().reward(ScoreManager.RewardType.BIND_OUWAN, false);
				AccountManager.getInstance().updateUserInfo();
				break;
			case ObserverManager.UserActionListener.ACTION_BIND_PHONE:
				ScoreManager.getInstance().reward(ScoreManager.RewardType.BIND_PHONE, false);
				AccountManager.getInstance().updateUserInfo();
				break;
		}
	}

	@Override
	public void release() {
		super.release();
		if (mCall != null) {
			mCall.cancel();
			mCall = null;
		}
		if (mAdapter != null) {
			mAdapter.release();
			mAdapter = null;
		}
		if (mDataView != null) {
			mDataView.setAdapter(null);
			mDataView = null;
		}
		mData = null;
	}
}
