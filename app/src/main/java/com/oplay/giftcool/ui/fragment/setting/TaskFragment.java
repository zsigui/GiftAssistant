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
import com.oplay.giftcool.config.GameTypeUtil;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.config.TaskTypeUtil;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.manager.OuwanSDKManager;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.model.data.resp.ScoreMission;
import com.oplay.giftcool.model.data.resp.ScoreMissionList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.sharesdk.ShareSDKManager;
import com.oplay.giftcool.ui.activity.MainActivity;
import com.oplay.giftcool.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.ui.fragment.game.GameFragment;
import com.oplay.giftcool.ui.fragment.gift.GiftFragment;
import com.oplay.giftcool.util.DateUtil;
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

        // 打开任务界面，取消红点
        if (MainActivity.sGlobalHolder != null) {
            MainActivity.sGlobalHolder.updateHintState(KeyConfig.TYPE_ID_SCORE_TASK, 0);
        }
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
    private Call<JsonRespBase<ScoreMissionList>> mCall;

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
        mCall.enqueue(new Callback<JsonRespBase<ScoreMissionList>>() {
            @Override
            public void onResponse(Call<JsonRespBase<ScoreMissionList>> call,
                                   Response<JsonRespBase<ScoreMissionList>> response) {
                if (!mCanShowUI || call.isCanceled()) {
                    return;
                }

                if (response != null && response.isSuccessful()) {
                    if (response.body() != null && response.body().getCode() == NetStatusCode.SUCCESS) {
                        mData = response.body().getData().missions;
                        setTaskIcon(mData);
                        mData = resort(mData);
                        mAdapter.updateData(mData);
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
            public void onFailure(Call<JsonRespBase<ScoreMissionList>> call, Throwable t) {
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
            String id = mission.id;
            if (id.equals(TaskTypeUtil.ID_SET_NICK)) {
                // 跳转到设置用户昵称界面
                mission.icon = R.drawable.ic_task_set_nick;
            } else if (id.equals(TaskTypeUtil.ID_UPLOAD_AVATOR)) {
                mission.icon = R.drawable.ic_task_upload_avator;
            } else if (id.equals(TaskTypeUtil.ID_BIND_PHONE) ||
                    id.equals(TaskTypeUtil.ID_BIND_OUWAN)) {
                mission.icon = R.drawable.ic_task_bind;
            } else if (id.equals(TaskTypeUtil.ID_FEEDBACK)) {
                mission.icon = R.drawable.ic_task_feedback;
            } else if (id.equals(TaskTypeUtil.ID_SEARCH)) {
                mission.icon = R.drawable.ic_task_search;
            } else if (id.equals(TaskTypeUtil.ID_JUDGE_GAME)) {
                //mission.icon = R.drawable.ic_task_judge_game;
            } else if (id.equals(TaskTypeUtil.ID_STAR_COMMENT)) {
                //mission.icon = R.drawable.ic_task_star_comment;
            } else if (id.equals(TaskTypeUtil.ID_LOGIN)) {
                mission.icon = R.drawable.ic_task_first_login;
            } else if (id.equals(TaskTypeUtil.ID_DOWNLOAD)) {
                mission.icon = R.drawable.ic_task_download;
            } else if (id.equals(TaskTypeUtil.ID_SHARE_NORMAL_GIFT)) {
                mission.icon = R.drawable.ic_task_share_normal_gift;
            } else if (id.equals(TaskTypeUtil.ID_SHARE_LIMIT_GIFT)) {
                mission.icon = R.drawable.ic_task_share_limit_gift;
            } else if (id.equals(TaskTypeUtil.ID_SHARE_GIFT_COOL)) {
                mission.icon = R.drawable.ic_task_share;
            } else if (id.equals(TaskTypeUtil.ID_GET_LIMIT_WITH_BEAN)) {
                mission.icon = R.drawable.ic_task_get_limit_with_bean;
            } else if (id.equals(TaskTypeUtil.ID_DOWNLOAD_SPECIFIED)) {
                //mission.icon = R.drawable.ic_task_download_specified;
            } else if (id.equals(TaskTypeUtil.ID_CONTINUOUS_LOGIN)) {
                mission.icon = R.drawable.ic_task_continuous_login;
            } else if (id.equals(TaskTypeUtil.ID_FIRST_LOGIN)) {
                mission.icon = R.drawable.ic_task_first_login;
            } else if (id.equals(TaskTypeUtil.ID_LOGIN_SPECIFIED)) {
                mission.icon = R.drawable.ic_task_login_specified;
            }
        }
    }

    /**
     * 对任务进行重新排序 <br />
     * 添加任务头，已完成任务置于后面，待完成任务置于前面
     */
    private ArrayList<ScoreMission> resort(ArrayList<ScoreMission> data) {
        if (data == null) {
            return null;
        }
        mCurFinishedTask = 0;
        ArrayList<ScoreMission> result = new ArrayList<>();
        addNewTaskType("期待任务", TaskTypeUtil.MISSION_TYPE_FUTURE, result, data);
        addNewTaskType("新手任务", TaskTypeUtil.MISSION_TYPE_TIRO, result, data);
        addNewTaskType("日常任务", TaskTypeUtil.MISSION_TYPE_DAILY, result, data);
        addNewTaskType("连续任务", TaskTypeUtil.MISSION_TYPE_CONTINUOUS, result, data);
        if (mCurFinishedTask == result.size()) {
            mCurFinishedTask = -1;
            AccountManager.getInstance().getUserInfo().isCompleteTodayMission = true;
            ObserverManager.getInstance().notifyUserUpdate(ObserverManager.STATUS.USER_UPDATE_ALL);
        }
        return result;
    }

    /**
     * 处理标记特定任务类型
     */
    private void addNewTaskType(String taskTypeName, int type, ArrayList<ScoreMission> result, ArrayList<ScoreMission> data) {
        int rawFinished = 0;
        int rawTotal;
        int rawFinishedIndex = -1;
        ArrayList<ScoreMission> rawTasks = new ArrayList<>();
        for (ScoreMission mission : data) {
            if ((TaskTypeUtil.ID_DOWNLOAD.equals(mission.id)
                    || TaskTypeUtil.ID_DOWNLOAD_SPECIFIED.equals(mission.id))
                    && !AssistantApp.getInstance().isAllowDownload()) {
                // 不允许下载，不添加
                mCurFinishedTask++;
                continue;
            }
            // 最后完成时间是今天，标志为已经完成
            if (mission.type == type) {
                if (!TextUtils.isEmpty(mission.lastCompleteTime)
                        && DateUtil.isToday(mission.lastCompleteTime)
                        && (mission.type == TaskTypeUtil.MISSION_TYPE_CONTINUOUS
                        || mission.dayCount == mission.dayCompleteCount)) {
                    rawFinished++;
                    mission.isFinished = true;
                    rawTasks.add(mission);
                    rawFinishedIndex = (rawFinishedIndex == -1 ? rawTasks.size() - 1 : rawFinishedIndex);
                    mCurFinishedTask++;
                } else {
                    mission.isFinished = false;
                    if (rawFinishedIndex == -1) {
                        rawTasks.add(mission);
                    } else {
                        rawTasks.add(rawFinishedIndex, mission);
                    }
                }
            }
        }
        rawTotal = rawTasks.size();
        if (rawTotal > 0) {
            ScoreMission rawTitle = new ScoreMission();
            rawTitle.name = String.format(taskTypeName + "(%d/%d)", rawFinished, rawTotal);
            result.add(rawTitle);
            result.addAll(rawTasks);
            mCurFinishedTask++;
        }
    }

    /**
     * 处理特定任务事件
     */
    private void handleMission(ScoreMission scoreMission) {
        if (scoreMission == null) return;
        if (getContext() == null || !(getContext() instanceof BaseAppCompatActivity)) {
            ToastUtil.showShort(ConstString.TEXT_ENTER_ERROR);
            return;
        }
        String id = scoreMission.id;
        ScoreManager.getInstance().setInWorking(true);
        if (id.equals(TaskTypeUtil.ID_SET_NICK)) {
            // 跳转到设置用户昵称信息界面
            ((BaseAppCompatActivity) getContext()).replaceFragWithTitle(R.id.fl_container,
                    SetNickFragment.newInstance(), getResources().getString(R.string.st_user_set_nick_title));
        } else if (id.equals(TaskTypeUtil.ID_UPLOAD_AVATOR)) {
            // 跳转到设置用户头像信息界面
            ((BaseAppCompatActivity) getContext()).replaceFragWithTitle(R.id.fl_container,
                    UploadAvatarFragment.newInstance(), getResources().getString(R.string.st_user_avator));
        } else if (id.equals(TaskTypeUtil.ID_BIND_PHONE)) {
            // 跳转到绑定手机账号界面
            OuwanSDKManager.getInstance().showBindPhoneView(getContext());
        } else if (id.equals(TaskTypeUtil.ID_BIND_OUWAN)) {
            // 跳转到绑定偶玩账号界面
            OuwanSDKManager.getInstance().showBindOuwanView(getContext());
        } else if (id.equals(TaskTypeUtil.ID_FEEDBACK)) {
            // 跳转反馈界面
            ((BaseAppCompatActivity) getContext()).replaceFragWithTitle(R.id.fl_container,
                    FeedBackFragment.newInstance(), getResources().getString(R.string.st_feedback_title));
        } else if (id.equals(TaskTypeUtil.ID_SEARCH)) {
            // 跳转搜索礼包/游戏界面
            IntentUtil.jumpSearch(getContext());
        } else if (id.equals(TaskTypeUtil.ID_JUDGE_GAME)) {
            // 评论
        } else if (id.equals(TaskTypeUtil.ID_STAR_COMMENT)) {
            // 为某条评论点赞，暂无
        } else if (id.equals(TaskTypeUtil.ID_LOGIN)) {
            // 跳转登录界面
            IntentUtil.jumpLogin(getContext());
        } else if (id.equals(TaskTypeUtil.ID_DOWNLOAD)) {
            // 跳转游戏榜单界面
            if (MainActivity.sGlobalHolder == null) {
                IntentUtil.jumpGameNewList(getContext());
            } else {
                MainActivity.sGlobalHolder.jumpToIndexGame(GameFragment.INDEX_NOTICE);
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        } else if (id.equals(TaskTypeUtil.ID_SHARE_NORMAL_GIFT)) {
            // 分享普通礼包
            if (MainActivity.sGlobalHolder == null) {
                IntentUtil.jumpGiftNewList(getContext());
            } else {
                MainActivity.sGlobalHolder.jumpToIndexGift(GiftFragment.POS_NEW);
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        } else if (id.equals(TaskTypeUtil.ID_SHARE_LIMIT_GIFT)) {
            // 分享限量礼包
            IntentUtil.jumpGiftLimitList(getContext(), false);
        } else if (id.equals(TaskTypeUtil.ID_GET_LIMIT_WITH_BEAN)) {
            // 使用偶玩豆购买限量礼包，跳转今日限量界面
            IntentUtil.jumpGiftLimitList(getContext(), false);
        } else if (id.equals(TaskTypeUtil.ID_DOWNLOAD_SPECIFIED)) {
            // 跳转指定游戏界面，暂无
            try {
                IntentUtil.jumpGameDetail(getContext(), Integer.parseInt(scoreMission.data),
                        GameTypeUtil.JUMP_STATUS_DETAIL);
            } catch (Exception e) {
                if (AppDebugConfig.IS_DEBUG) {
                    KLog.e(e);
                }
                ToastUtil.showShort("数据获取出错，跳转失败");
            }
        } else if (id.equals(TaskTypeUtil.ID_CONTINUOUS_LOGIN)) {
            // 跳转登录界面
            IntentUtil.jumpLogin(getContext());
        } else if (TaskTypeUtil.ID_LOGIN_SPECIFIED.equals(id)) {
            // ignored
        } else if (TaskTypeUtil.ID_SHARE_GIFT_COOL.equals(id)) {
            // 进行礼包酷分享
            ShareSDKManager.getInstance(getContext()).shareGCool(getContext(), getChildFragmentManager());
        } else {
            if (AppDebugConfig.IS_FRAG_DEBUG) {
                KLog.e("error id " + id);
            }
        }
    }

    @Override
    public void onUserUpdate(int action) {
        if (tvScore != null && AccountManager.getInstance().isLogin()) {
            tvScore.setText(String.valueOf(AccountManager.getInstance().getUserInfo().score));
        }
        if (mCurFinishedTask == -1) {
            // 为所有任务完成，通知主界面更新通知，忽略
            return;
        }
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
