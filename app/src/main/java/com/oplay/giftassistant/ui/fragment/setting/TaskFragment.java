package com.oplay.giftassistant.ui.fragment.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
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
import com.oplay.giftassistant.model.data.resp.ScoreMission;
import com.oplay.giftassistant.model.data.resp.ScoreMissionList;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.model.json.base.JsonRespBase;
import com.oplay.giftassistant.ui.activity.GameDetailActivity;
import com.oplay.giftassistant.ui.activity.SearchActivity;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_WithName;
import com.oplay.giftassistant.ui.widget.ScoreText;
import com.oplay.giftassistant.util.DateUtil;
import com.oplay.giftassistant.util.ToastUtil;
import com.socks.library.KLog;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 16-1-6.
 */
public class TaskFragment extends BaseFragment_WithName implements OnItemClickListener<ScoreMission> {


	private LinearLayout llSetNick;
	private LinearLayout llSetAvatar;
	private LinearLayout llBind;
	private LinearLayout llFeedback;
	private LinearLayout llSearch;
	private LinearLayout llJudge;
	private LinearLayout llStar;
	private LinearLayout llLogin;
	private LinearLayout llDownload;
	private LinearLayout llShareNormal;
	private LinearLayout llShareLimit;
	private LinearLayout llGetWithBean;
	private LinearLayout llDownloadSpecified;
	private LinearLayout llContinuousLogin;

	private TextView tvSetNick;
	private TextView tvSetAvatar;
	private TextView tvBind;
	private TextView tvFeedback;
	private TextView tvSearch;
	private TextView tvJudge;
	private TextView tvStar;
	private TextView tvLogin;
	private TextView tvDownload;
	private TextView tvShareNormal;
	private TextView tvShareLimit;
	private TextView tvGetWithBean;
	private TextView tvDownloadSpecified;
	private TextView tvContinuousLogin;

	private ScoreText stSetNick;
	private ScoreText stSetAvatar;
	private ScoreText stBind;
	private ScoreText stFeedback;
	private ScoreText stSearch;
	private ScoreText stJudge;
	private ScoreText stStar;
	private ScoreText stLogin;
	private ScoreText stDownload;
	private ScoreText stShareNormal;
	private ScoreText stShareLimit;
	private ScoreText stGetWithBean;
	private ScoreText stDownloadSpecified;
	private ScoreText stContinuousLogin;

	private TextView btnSetNick;
	private TextView btnSetAvatar;
	private TextView btnBind;
	private TextView btnFeedBack;
	private TextView btnSearch;
	private TextView btnJudge;
	private TextView btnStar;
	private TextView btnLogin;
	private TextView btnDownload;
	private TextView btnShareNormal;
	private TextView btnShareLimit;
	private TextView btnGetWithBean;
	private TextView btnDownloadSpecified;
	private TextView btnContinuousLOgin;

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
		mViewManager.showLoading();
		mCanShowUI = true;
		mIsLoading = true;
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				Global.getNetEngine().obtainScoreTask(new JsonReqBase<Void>())
						.enqueue(new Callback<JsonRespBase<ScoreMissionList>>() {
							@Override
							public void onResponse(Response<JsonRespBase<ScoreMissionList>> response,
							                       Retrofit retrofit) {
								mIsLoading = false;
								if (response != null && response.isSuccess()) {
									if (response.body().getCode() == StatusCode.SUCCESS) {
										mHasData = true;
										setTaskIcon(mData);
										mData = resort(mData);
										mAdapter.updateData(mData);
										return;
									}
									if (AppDebugConfig.IS_FRAG_DEBUG) {
										KLog.e(AppDebugConfig.TAG_FRAG, response.body().getCode()
												+ ", " + response.body().getMsg());
									}
								}
								mViewManager.showErrorRetry();
							}

							@Override
							public void onFailure(Throwable t) {
								mIsLoading = false;
								if (AppDebugConfig.IS_FRAG_DEBUG) {
									KLog.e(AppDebugConfig.TAG_FRAG, t);
								}
								mViewManager.showErrorRetry();
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
		/*for (ScoreMission mission : data) {
			String id = mission.id;
			if (id.equals(TaskTypeUtil.ID_SET_NICK)) {
				// 跳转到设置用户昵称界面
				mission.icon = R.drawable.ic_task_set_nick;
			} else if (id.equals(TaskTypeUtil.ID_UPLOAD_AVATOR)) {
				mission.icon = R.drawable.ic_task_upload_avator;
			} else if (id.equals(TaskTypeUtil.ID_BIND)) {
				mission.icon = R.drawable.ic_task_bind;
			} else if (id.equals(TaskTypeUtil.ID_FEEDBACK)) {
				mission.icon = R.drawable.ic_task_feedback;
			} else if (id.equals(TaskTypeUtil.ID_SEARCH)) {
				mission.icon = R.drawable.ic_task_search;
			} else if (id.equals(TaskTypeUtil.ID_JUDGE_GAME)) {
				mission.icon = R.drawable.ic_task_judge_game;
			} else if (id.equals(TaskTypeUtil.ID_STAR_COMMENT)) {
				mission.icon = R.drawable.ic_task_star_comment;
			} else if (id.equals(TaskTypeUtil.ID_LOGIN)) {
				mission.icon = R.drawable.ic_task_login;
			} else if (id.equals(TaskTypeUtil.ID_DOWNLOAD)) {
				mission.icon = R.drawable.ic_task_download;
			} else if (id.equals(TaskTypeUtil.ID_SHARE_NORMAL_GIFT)) {
				mission.icon = R.drawable.ic_task_normal_gift;
			} else if (id.equals(TaskTypeUtil.ID_SHARE_LIMIT_GIFT)) {
				mission.icon = R.drawable.ic_task_share_limit_gift;
			} else if (id.equals(TaskTypeUtil.ID_GET_LIMIT_WITH_BEAN)) {
				mission.icon = R.drawable.ic_task_get_limit_with_bean;
			} else if (id.equals(TaskTypeUtil.ID_DONWLOAD_SPECIFIED)) {
				mission.icon = R.drawable.ic_task_download_specified;
			} else if (id.equals(TaskTypeUtil.ID_CONTINUOUS_LOGIN)) {
				mission.icon = R.drawable.ic_task_continuous_login;
			}
		}*/
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
					rawTasks.add(mission);
					rawFinishedIndex = (rawFinishedIndex == -1 ? rawTasks.size() - 1 : rawFinishedIndex);
				} else {
					if (rawFinishedIndex == -1) {
						rawTasks.add(mission);
					} else {
						rawTasks.add(rawFinishedIndex, mission);
					}
				}
			} else if (mission.type == TaskTypeUtil.MISSION_TYPE_DAILY) {
				if (DateUtil.isToday(mission.lastCompleteTime)) {
					dailyFinished++;
					dailyTasks.add(mission);
					dailyFinishedIndex = (dailyFinishedIndex == -1 ? dailyTasks.size() - 1 : dailyFinishedIndex);
				} else {
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
					continuousTasks.add(mission);
					continuousFinishedIndex = (continuousFinishedIndex == -1 ? continuousTasks.size() - 1 :
							continuousFinishedIndex);
				} else {
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
			// 跳转到设置用户昵称界面
		} else if (id.equals(TaskTypeUtil.ID_UPLOAD_AVATOR)) {
		} else if (id.equals(TaskTypeUtil.ID_BIND)) {
		} else if (id.equals(TaskTypeUtil.ID_FEEDBACK)) {
		} else if (id.equals(TaskTypeUtil.ID_SEARCH)) {
			intent = new Intent(getContext(), SearchActivity.class);
			((BaseAppCompatActivity) getActivity()).openPageForResult(this, KeyConfig.REQUEST_UPDATE_AVATAR, intent);
		} else if (id.equals(TaskTypeUtil.ID_JUDGE_GAME)) {
			intent = new Intent(getContext(), GameDetailActivity.class);
			// 设置评论Intent
			getActivity().startActivity(intent);
		} else if (id.equals(TaskTypeUtil.ID_STAR_COMMENT)) {

		} else if (id.equals(TaskTypeUtil.ID_LOGIN)) {
		} else if (id.equals(TaskTypeUtil.ID_DOWNLOAD)) {
		} else if (id.equals(TaskTypeUtil.ID_SHARE_NORMAL_GIFT)) {
		} else if (id.equals(TaskTypeUtil.ID_SHARE_LIMIT_GIFT)) {
		} else if (id.equals(TaskTypeUtil.ID_GET_LIMIT_WITH_BEAN)) {
		} else if (id.equals(TaskTypeUtil.ID_DONWLOAD_SPECIFIED)) {
		} else if (id.equals(TaskTypeUtil.ID_CONTINUOUS_LOGIN)) {
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
