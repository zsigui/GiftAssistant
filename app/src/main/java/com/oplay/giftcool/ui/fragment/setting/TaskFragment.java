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
import com.oplay.giftcool.manager.AlarmClockManager;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.model.data.resp.task.ScoreMission;
import com.oplay.giftcool.model.data.resp.task.ScoreMissionGroup;
import com.oplay.giftcool.model.data.resp.task.TaskInfoOne;
import com.oplay.giftcool.model.data.resp.task.TaskInfoThree;
import com.oplay.giftcool.model.data.resp.task.TaskInfoTwo;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.MixUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ToastUtil;

import java.util.ArrayList;
import java.util.Locale;

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

    public static TaskFragment newInstance() {
        return new TaskFragment();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        if (!AccountManager.getInstance().isLogin()) {
//            ToastUtil.showShort(mApp.getResources().getString(R.string.st_hint_un_login));
            ToastUtil.showShort(ConstString.TOAST_SESSION_UNAVAILABLE);
            IntentUtil.jumpLoginNoToast(getContext());
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
        AccountManager.getInstance().updatePartUserInfo();
        ScoreManager.getInstance().setTaskFinished(false);
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
                        ScoreManager.getInstance().setTaskFinished(false);
                        ArrayList<ScoreMissionGroup> missionGroups = response.body().getData();
                        mData = transferToMissionList(missionGroups);
                        setTaskIcon(mData);
                        mAdapter.updateData(mData);
                        refreshSuccessEnd();
                        return;
                    }
                    AccountManager.getInstance().judgeIsSessionFailed(response.body());
                }
                AppDebugConfig.warnResp(AppDebugConfig.TAG_FRAG, response);
                refreshFailEnd();
            }

            @Override
            public void onFailure(Call<JsonRespBase<ArrayList<ScoreMissionGroup>>> call, Throwable t) {
                if (!mCanShowUI || call.isCanceled()) {
                    return;
                }
                AppDebugConfig.w(AppDebugConfig.TAG_FRAG, t);
                refreshFailEnd();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ScoreManager.getInstance().isTaskFinished()) {
            notifyRefresh();
        }
    }

    /**
     * 获取包名列表
     */
//	public HashSet<String> getPackName() {
//		synchronized (this) {
//			if (mPackName == null || mPackName.isEmpty()) {
//				mPackName = SystemUtil.getInstalledPackName(getContext());
//			}
//		}
//		return mPackName;
//	}

    /**
     * 将各任务分组列表提取出来合并成一个列表
     */
    private ArrayList<ScoreMission> transferToMissionList(ArrayList<ScoreMissionGroup> missionGroups) {
        ArrayList<ScoreMission> missions = new ArrayList<>();
        final boolean allowDownload = AssistantApp.getInstance().isAllowDownload();
        int denominator = 0;
        int molecular = 0;
        for (ScoreMissionGroup missionGroup : missionGroups) {
            final ScoreMission groupHeader = new ScoreMission();
            groupHeader.isHeader = true;
            missions.add(groupHeader);
            for (ScoreMission m : missionGroup.missions) {
                denominator = Math.min(m.totalLimit, m.dailyLimit);
                molecular = Math.max(m.todayCompleteCount, m.totalCompleteCount);
                molecular = Math.min(denominator, molecular);
                if (denominator > 1) {
                    m.name = String.format(Locale.CHINA, "%s(%d/%d)", m.name, molecular, denominator);
                }
                if (m.actionType == TaskTypeUtil.MISSION_TYPE_DOWNLOAD) {
                    // 对于下载类型，需要预先判断
                    if (allowDownload) {
                        try {
                            TaskInfoThree info = AssistantApp.getInstance().getGson()
                                    .fromJson(m.actionInfo, TaskInfoThree.class);
                            if (m.isCompleted == 0) {
                                // 任务未完成，添加入队列中，已完成则跳过
                                ScoreManager.getInstance().addDownloadWork(getContext(), m.code, info);
                            }
                            AppDebugConfig.d(AppDebugConfig.TAG_MANAGER, "task : 添加下载: " + info.packName);
                            missions.add(m);
//							if (!getPackName().contains(info.packName)
//									|| ScoreManager.getInstance().containDownloadTask(getContext(), info.packName)) {
//								// 执行中的任务，或者还没有安装的游戏
//								// 设置试玩游戏信息
//								ScoreManager.getInstance().addDownloadWork(getContext(), m.code, info);
//								missions.add(m);
//							} else {
//								missionCountSub(missionGroup, m);
//							}
                        } catch (Throwable t) {
                            missionCountSub(missionGroup, m);
                            t.printStackTrace();
                        }
                    } else {
                        missionCountSub(missionGroup, m);
                    }
                } else if (m.code.equalsIgnoreCase(TaskTypeUtil.ID_FOCUS_GAME)) {
                    // 对于代号为关注游戏的
                    if (allowDownload) {
                        missions.add(m);
                    } else {
                        missionCountSub(missionGroup, m);
                    }
                } else {
                    missions.add(m);
                }
            }
            groupHeader.name = String.format(Locale.CHINA, "%s(%d/%d)",
                    missionGroup.name, missionGroup.completedCount, missionGroup.totalCount);
        }
        return missions;
    }

    /**
     * 去掉被屏蔽的下载任务的计数
     */
    private void missionCountSub(ScoreMissionGroup missionGroup, ScoreMission m) {
        if (m.isCompleted == 1) {
            missionGroup.completedCount--;
        }
        missionGroup.totalCount--;
    }


    @Override
    public void onItemClick(ScoreMission item, View view, int position) {
        if (mData == null || mData.size() == 0) {
            AppDebugConfig.d(AppDebugConfig.TAG_FRAG, "Empty or Null Data On Item Click! mData = " + mData);
            return;
        }
        handleMission(item);
    }

    private void setTaskIcon(ArrayList<ScoreMission> data) {
        if (data == null) {
            return;
        }
        for (ScoreMission mission : data) {
            if (!TextUtils.isEmpty(mission.icon)) {
                continue;
            }
            String id = mission.code;
            if (TaskTypeUtil.ID_SET_NICK.equalsIgnoreCase(id)) {
                mission.iconAlternate = R.drawable.ic_task_set_nick;
            } else if (TaskTypeUtil.ID_SET_AVATAR.equalsIgnoreCase(id)) {
                mission.iconAlternate = R.drawable.ic_task_upload_avator;
            } else if (TaskTypeUtil.ID_FIRST_LOGIN.equalsIgnoreCase(id)) {
                mission.iconAlternate = R.drawable.ic_task_first_login;
            } else if (TaskTypeUtil.ID_FOCUS_GAME.equalsIgnoreCase(id)) {
                mission.iconAlternate = R.drawable.ic_task_attention;
            } else if (TaskTypeUtil.ID_REQUEST_GIFT.equalsIgnoreCase(id)) {
                mission.iconAlternate = R.drawable.ic_task_hope_gift;
            } else if (TaskTypeUtil.ID_FEEDBACK.equalsIgnoreCase(id)) {
                mission.iconAlternate = R.drawable.ic_task_new_feedback;
            } else if (TaskTypeUtil.ID_UPGRADE.equalsIgnoreCase(id)) {
                mission.iconAlternate = R.drawable.ic_task_new_update;
            } else if (TaskTypeUtil.ID_SIGN_IN.equalsIgnoreCase(id)) {
                mission.iconAlternate = R.drawable.ic_task_sign_in;
            } else if (TaskTypeUtil.ID_GCOOL_SHARE.equalsIgnoreCase(id)) {
                mission.iconAlternate = R.drawable.ic_task_share_gcool;
            } else if (TaskTypeUtil.ID_GIFT_SHARE.equalsIgnoreCase(id)) {
                mission.iconAlternate = R.drawable.ic_task_share_gift;
            } else if (TaskTypeUtil.ID_PLAY_GAME.equalsIgnoreCase(id)) {
                mission.iconAlternate = R.drawable.ic_task_play_game;
            } else if (TaskTypeUtil.ID_BUG_GIFT_USE_OUWAN.equalsIgnoreCase(id)) {
                mission.iconAlternate = R.drawable.ic_task_get_limit_with_bean;
            } else {
                mission.iconAlternate = R.drawable.ic_task_default;
            }
        }
    }

    /**
     * 处理特定任务事件
     */
    private void handleMission(ScoreMission mission) {
        if (mission == null) return;
        if (getContext() == null || !(getContext() instanceof BaseAppCompatActivity)) {
            ToastUtil.showShort(ConstString.TOAST_EXECUTE_ERROR);
            return;
        }
        try {
            switch (mission.actionType) {
                case TaskTypeUtil.MISSION_TYPE_JUMP_PAGE:
                    final TaskInfoOne infoOne = AssistantApp.getInstance().getGson().fromJson(
                            mission.actionInfo, TaskInfoOne.class);
                    IntentUtil.handleJumpInfo(getContext(), infoOne);
                    break;
                case TaskTypeUtil.MISSION_TYPE_EXECUTE_LOGIC:
                    final TaskInfoTwo infoTwo = AssistantApp.getInstance().getGson().fromJson(
                            mission.actionInfo, TaskInfoTwo.class);
                    MixUtil.executeLogicCode(getContext(), getChildFragmentManager(), infoTwo);
                    break;
                case TaskTypeUtil.MISSION_TYPE_DOWNLOAD:
                    final TaskInfoThree infoThree = AssistantApp.getInstance().getGson().fromJson(
                            mission.actionInfo, TaskInfoThree.class);
                    handleInfoThree(infoThree);
                    break;
                default:
                    ToastUtil.showShort(ConstString.TOAST_VERSION_NOT_SUPPORT);
            }
        } catch (Throwable t) {
            AppDebugConfig.w(AppDebugConfig.TAG_FRAG, t);
        }
    }


    /**
     * 处理额外信息类型为三(下载打开应用)的数据
     */
    private void handleInfoThree(TaskInfoThree infoThree) {
        // 设置试玩游戏信息
//		ScoreManager.getInstance().addDownloadWork(getContext(), code, infoThree);
        AlarmClockManager.getInstance().setObserverGame(true);
        IntentUtil.jumpGameDetail(getContext(), infoThree.appId);
    }

    @Override
    public void onUserUpdate(int action) {
        switch (action) {
            case ObserverManager.STATUS.USER_UPDATE_PART:
                if (tvScore != null && AccountManager.getInstance().isLogin()) {
                    tvScore.setText(String.valueOf(AccountManager.getInstance().getUserInfo().score));
                }
//				break;
            case ObserverManager.STATUS.USER_UPDATE_ALL:
            case ObserverManager.STATUS.USER_UPDATE_TASK:
                notifyRefresh();
                break;
        }
    }

    /**
     * 通知对界面数据进行刷新
     */
    private void notifyRefresh() {
        if (mIsNotifyRefresh) {
            return;
        }
        mIsNotifyRefresh = true;
        lazyLoad();
    }

    @Override
    public String getPageName() {
        return PAGE_NAME;
    }

    @Override
    public void onUserActionFinish(int action, int code) {
        switch (action) {
            case ObserverManager.UserActionListener.ACTION_BIND_OUWAN:
//				ScoreManager.getInstance().reward(ScoreManager.RewardType.BIND_OUWAN, false);
                AccountManager.getInstance().updateUserInfo();
                break;
            case ObserverManager.UserActionListener.ACTION_BIND_PHONE:
//				ScoreManager.getInstance().reward(ScoreManager.RewardType.BIND_PHONE, false);
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
