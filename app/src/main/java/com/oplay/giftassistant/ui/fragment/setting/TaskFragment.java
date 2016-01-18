package com.oplay.giftassistant.ui.fragment.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.adapter.ScoreTaskAdapter;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.config.KeyConfig;
import com.oplay.giftassistant.config.StatusCode;
import com.oplay.giftassistant.config.TaskTypeUtil;
import com.oplay.giftassistant.listener.OnItemClickListener;
import com.oplay.giftassistant.manager.AccountManager;
import com.oplay.giftassistant.manager.OuwanSDKManager;
import com.oplay.giftassistant.model.data.resp.ScoreMission;
import com.oplay.giftassistant.model.data.resp.ScoreMissionList;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.ui.activity.SearchActivity;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;
import com.oplay.giftassistant.util.DateUtil;
import com.oplay.giftassistant.util.IntentUtil;
import com.oplay.giftassistant.util.ToastUtil;
import com.socks.library.KLog;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 16-1-6.
 */
public class TaskFragment extends BaseFragment implements OnItemClickListener<ScoreMission> {



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
			ToastUtil.showShort("页面进入错误，请关闭页面登录后重试");
			return;
		}
		initViewManger(R.layout.fragment_score_task);
		tvScore = getViewById(R.id.tv_score);
		mDataView = getViewById(R.id.lv_container);
	}

	@Override
	protected void setListener() {
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		tvScore.setText(String.valueOf(AccountManager.getInstance().getUserInfo().score));
		mAdapter = new ScoreTaskAdapter(getContext(), this);
		mDataView.setAdapter(mAdapter);
	}

	@Override
	protected void lazyLoad() {
		refreshInitConfig();
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				Global.getNetEngine().obtainScoreTask(new JsonReqBase<String>())
						.enqueue(new Callback<JsonRespBase<ScoreMissionList>>() {
							@Override
							public void onResponse(Response<JsonRespBase<ScoreMissionList>> response,
							                       Retrofit retrofit) {
								if (!mCanShowUI) {
									return;
								}
								if (response != null && response.isSuccess()) {
									if (response.body() != null && response.body().getCode() == StatusCode.SUCCESS) {
										refreshSuccessEnd();
										mData = response.body().getData().missions;
										setTaskIcon(mData);
										mData = resort(mData);
										mAdapter.updateData(mData);
										return;
									}
									if (AppDebugConfig.IS_FRAG_DEBUG) {
										KLog.e(AppDebugConfig.TAG_FRAG,
												response.body() == null? "解析失败" : response.body().error());
									}
									ToastUtil.showShort("获取任务列表失败 - "
											+ (response.body() == null? "解析失败" : response.body().error()));
								}
								refreshFailEnd();
							}

							@Override
							public void onFailure(Throwable t) {
								if (!mCanShowUI) {
									return;
								}
								if (AppDebugConfig.IS_FRAG_DEBUG) {
									KLog.e(AppDebugConfig.TAG_FRAG, t);
								}
								refreshFailEnd();
							}
						});
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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (!AccountManager.getInstance().isLogin()) {
			// 跳转登录
			return;
		}
		if (resultCode == KeyConfig.SUCCESS) {
		/*	if (requestCode == KeyConfig.REQUEST_UPDATE_AVATAR) {
				updateOnceMission(TaskTypeUtil.ID_UPLOAD_AVATOR);
			} else if (requestCode == KeyConfig.REQUEST_SET_NICK) {
				updateOnceMission(TaskTypeUtil.ID_SET_NICK);
			}*/

		}
	}

	/**
	 * 更新一次性任务状态
	 */
	private void updateOnceMission(String id) {
		for (ScoreMission mission : mData) {
			if (mission.id.equals(id)) {
				mission.completeTime++;
				mission.lastCompleteTime = DateUtil.getDate("yyyy-MM-dd", 0);
				AccountManager.getInstance().getUserInfo().score += mission.rewardScore;
				return;
			}
		}
		mData = resort(mData);
		mAdapter.updateData(mData);
		onUserUpdate();
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
					id.equals(TaskTypeUtil.ID_BIND_PHONE)) {
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
				mission.icon = R.drawable.ic_task_login;
			} else if (id.equals(TaskTypeUtil.ID_DOWNLOAD)) {
				mission.icon = R.drawable.ic_task_download;
			} else if (id.equals(TaskTypeUtil.ID_SHARE_NORMAL_GIFT)) {
				mission.icon = R.drawable.ic_task_share_normal_gift;
			} else if (id.equals(TaskTypeUtil.ID_SHARE_LIMIT_GIFT)) {
				mission.icon = R.drawable.ic_task_share_limit_gift;
			} else if (id.equals(TaskTypeUtil.ID_GET_LIMIT_WITH_BEAN)) {
				mission.icon = R.drawable.ic_task_get_limit_with_bean;
			} else if (id.equals(TaskTypeUtil.ID_DOWNLOAD_SPECIFIED)) {
				//mission.icon = R.drawable.ic_task_download_specified;
			} else if (id.equals(TaskTypeUtil.ID_CONTINUOUS_LOGIN)) {
				mission.icon = R.drawable.ic_task_continuous_login;
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
		int rawFinished = 0;
		int dailyFinished = 0;
		int continuousFinished = 0;
		int rawTotal;
		int dailyTotal;
		int continuousTotal;
		int rawFinishedIndex = -1;
		int dailyFinishedIndex = -1;
		int continuousFinishedIndex = -1;
		ArrayList<ScoreMission> result = new ArrayList<>();
		ArrayList<ScoreMission> rawTasks = new ArrayList<>();
		ArrayList<ScoreMission> dailyTasks = new ArrayList<>();
		ArrayList<ScoreMission> continuousTasks = new ArrayList<>();
		for (ScoreMission mission : data) {
			// 最后完成时间是今天，标志为已经完成
			if (mission.type == TaskTypeUtil.MISSION_TYPE_TIRO) {
				if (DateUtil.isToday(mission.lastCompleteTime)) {
					rawFinished++;
					mission.isFinished = true;
					rawTasks.add(mission);
					rawFinishedIndex = (rawFinishedIndex == -1 ? rawTasks.size() - 1 : rawFinishedIndex);
				} else {
					mission.isFinished = false;
					if (rawFinishedIndex == -1) {
						rawTasks.add(mission);
					} else {
						rawTasks.add(rawFinishedIndex, mission);
					}
				}
			} else if (mission.type == TaskTypeUtil.MISSION_TYPE_DAILY) {
				if (DateUtil.isToday(mission.lastCompleteTime)) {
					dailyFinished++;
					mission.isFinished = true;
					dailyTasks.add(mission);
					dailyFinishedIndex = (dailyFinishedIndex == -1 ? dailyTasks.size() - 1 : dailyFinishedIndex);
				} else {
					mission.isFinished = false;
					if (dailyFinishedIndex == -1) {
						dailyTasks.add(mission);
					} else {
						dailyTasks.add(dailyFinishedIndex, mission);
					}
				}
			} else if (mission.type == TaskTypeUtil.MISSION_TYPE_CONTINUOUS) {
				// 对于连续任务，得判断最后完成时间
				if (DateUtil.isToday(mission.lastCompleteTime)) {
					continuousFinished++;
					mission.isFinished = true;
					continuousTasks.add(mission);
					continuousFinishedIndex = (continuousFinishedIndex == -1 ? continuousTasks.size() - 1 :
							continuousFinishedIndex);
				} else {
					mission.isFinished = false;
					if (continuousFinishedIndex == -1) {
						continuousTasks.add(mission);
					} else {
						continuousTasks.add(continuousFinishedIndex, mission);
					}
				}
			}
		}
		rawTotal = rawTasks.size();
		dailyTotal = dailyTasks.size();
		continuousTotal = continuousTasks.size();
		if (rawTotal > 0) {
			ScoreMission rawTitle = new ScoreMission();
			rawTitle.name = String.format("新手任务(%d/%d)", rawFinished, rawTotal);
			result.add(rawTitle);
			result.addAll(rawTasks);
		}
		if (dailyTotal > 0) {
			ScoreMission dailyTitle = new ScoreMission();
			dailyTitle.name = String.format("日常任务(%d/%d)", dailyFinished, dailyTotal);
			result.add(dailyTitle);
			result.addAll(dailyTasks);
		}
		if (continuousTotal > 0) {
			ScoreMission continuousTitle = new ScoreMission();
			continuousTitle.name = String.format("连续任务(%d/%d)", continuousFinished, continuousTotal);
			result.add(continuousTitle);
			result.addAll(continuousTasks);
		}
		return result;
	}

	private void handleMission(ScoreMission scoreMission) {
		if (scoreMission == null) return;
		String id = scoreMission.id;
		Intent intent;
		if (id.equals(TaskTypeUtil.ID_SET_NICK)) {
			// 跳转到设置用户昵称信息界面
			IntentUtil.jumpUserSetNick(getContext());
		} else if (id.equals(TaskTypeUtil.ID_UPLOAD_AVATOR)) {
			// 跳转到设置用户头像信息界面
			IntentUtil.jumpUserSetAvatar(getContext());
		} else if (id.equals(TaskTypeUtil.ID_BIND_PHONE)) {
			// 跳转到绑定手机账号界面
			OuwanSDKManager.getInstance().showBindPhoneView();
		} else if (id.equals(TaskTypeUtil.ID_BIND_OUWAN)){
			// 跳转到绑定偶玩账号界面
			OuwanSDKManager.getInstance().showBindOuwanView();
		} else if (id.equals(TaskTypeUtil.ID_FEEDBACK)) {
			// 跳转反馈界面
			IntentUtil.jumpFeedBack(getContext());
		} else if (id.equals(TaskTypeUtil.ID_SEARCH)) {
			// 跳转搜索礼包/游戏界面
			intent = new Intent(getContext(), SearchActivity.class);
			((BaseAppCompatActivity) getActivity()).openPageForResult(this, KeyConfig.REQUEST_UPDATE_AVATAR, intent);
		} else if (id.equals(TaskTypeUtil.ID_JUDGE_GAME)) {
			// 评论
		} else if (id.equals(TaskTypeUtil.ID_STAR_COMMENT)) {
			// 为某条评论点赞，暂无
		} else if (id.equals(TaskTypeUtil.ID_LOGIN)) {
			// 跳转登录界面
			IntentUtil.jumpLogin(getContext());
		} else if (id.equals(TaskTypeUtil.ID_DOWNLOAD)) {
			// 跳转新游推荐界面
			IntentUtil.jumpGameNewList(getContext());
		} else if (id.equals(TaskTypeUtil.ID_SHARE_NORMAL_GIFT)) {
			// 分享普通礼包
		} else if (id.equals(TaskTypeUtil.ID_SHARE_LIMIT_GIFT)) {
			// 分享限量礼包
		} else if (id.equals(TaskTypeUtil.ID_GET_LIMIT_WITH_BEAN)) {
			// 使用偶玩豆购买限量礼包，跳转今日限量界面
			IntentUtil.jumpGiftLimitList(getContext());
		} else if (id.equals(TaskTypeUtil.ID_DOWNLOAD_SPECIFIED)) {
			// 跳转指定游戏界面，暂无
			IntentUtil.jumpGameDetail(getContext(), Integer.parseInt(scoreMission.data), "");
		} else if (id.equals(TaskTypeUtil.ID_CONTINUOUS_LOGIN)) {
			// 跳转登录界面
			IntentUtil.jumpLogin(getContext());
		} else {
			if (AppDebugConfig.IS_FRAG_DEBUG) {
				KLog.e("error id " + id);
			}
		}
	}

	@Override
	public void onUserUpdate() {
		if (tvScore != null && AccountManager.getInstance().isLogin()) {
			tvScore.setText(AccountManager.getInstance().getUserInfo().score);
		}
	}

}
