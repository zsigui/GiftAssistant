package com.oplay.giftcool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.TaskTypeUtil;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.model.data.resp.ScoreMission;
import com.oplay.giftcool.ui.widget.ScoreText;
import com.oplay.giftcool.util.ViewUtil;

import java.util.ArrayList;

/**
 * Created by zsigui on 16-1-7.
 */
public class ScoreTaskAdapter extends BaseAdapter {

	private ArrayList<ScoreMission> mData;
	private Context mContext;
	private OnItemClickListener<ScoreMission> mItemListener;

	public ScoreTaskAdapter(Context context, OnItemClickListener<ScoreMission> itemListener) {
		this(context, null, itemListener);
	}

	public ScoreTaskAdapter(Context context, ArrayList<ScoreMission> data,
	                        OnItemClickListener<ScoreMission> itemListener) {
		mContext = context;
		mData = data;
		mItemListener = itemListener;
	}

	public void setOnItemListener(OnItemClickListener<ScoreMission> itemListener) {
		mItemListener = itemListener;
	}

	public ArrayList<ScoreMission> getData() {
		return mData;
	}

	public void setData(ArrayList<ScoreMission> data) {
		mData = data;
	}

	@Override
	public int getCount() {
		return mData == null ? 0 : mData.size();
	}

	@Override
	public Object getItem(int position) {
		return getCount() == 0 ? null : mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		// 理论上0已经异常
		return getCount() == 0 ? TaskTypeUtil.TYPE_HEADER : TaskTypeUtil.getItemViewType(mData.get(position).type);
	}

	@Override
	public int getViewTypeCount() {
		return TaskTypeUtil.TYPE_COUNT;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (getCount() == 0)
			return null;

		int type = getItemViewType(position);
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			switch (type) {
				case TaskTypeUtil.TYPE_HEADER:
					convertView = LayoutInflater.from(mContext)
							.inflate(R.layout.item_score_task_header, parent, false);
					holder.tvName = ViewUtil.getViewById(convertView, R.id.tv_name);
					break;
				case TaskTypeUtil.TYPE_CONTENT:
					convertView = LayoutInflater.from(mContext)
							.inflate(R.layout.item_score_task_content, parent, false);
					holder.ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
					holder.tvName = ViewUtil.getViewById(convertView, R.id.tv_name);
					holder.stScore = ViewUtil.getViewById(convertView, R.id.st_score);
					holder.btnToDo = ViewUtil.getViewById(convertView, R.id.btn_todo);
					break;
				default:
					throw new IllegalStateException("error value of type : " + type);
			}
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		setValue(position, type, holder);

		return convertView;
	}

	private void setValue(final int position, int type, ViewHolder holder) {
		final ScoreMission mission = mData.get(position);
		switch (type) {
			case TaskTypeUtil.TYPE_CONTENT:
				ViewUtil.showImage(holder.ivIcon, mission.icon);
				if (mission.type == TaskTypeUtil.MISSION_TYPE_CONTINUOUS) {
					holder.tvName.setText(String.format("%s(%d/%d)", mission.name,
							mission.completeTime, mission.continuousDay));
				} else {
					if (mission.dayCount > 1) {
						holder.tvName.setText(String.format("%s(%d/%d)", mission.name,
								mission.dayCompleteCount, mission.dayCount));
					} else {
						holder.tvName.setText(mission.name);
					}
				}
				holder.stScore.setText("+" + mission.rewardScore);
				if (mission.isFinished) {
					if (mission.type == TaskTypeUtil.MISSION_TYPE_CONTINUOUS
							&& mission.completeTime != mission.continuousDay) {
						holder.stScore.setStateEnable(true);
					} else {
						holder.stScore.setStateEnable(false);
					}
					holder.btnToDo.setText("已完成");
					holder.btnToDo.setEnabled(false);
				} else if (mission.type == TaskTypeUtil.MISSION_TYPE_FUTURE) {
					holder.stScore.setStateEnable(true);
					holder.btnToDo.setText("待完成");
					holder.btnToDo.setEnabled(false);
				} else {
					holder.stScore.setStateEnable(true);
					holder.btnToDo.setText("去完成");
					holder.btnToDo.setEnabled(true);
				}
				holder.btnToDo.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mItemListener != null) {
							mItemListener.onItemClick(mission, null, position);
						}
					}
				});
				break;
			case TaskTypeUtil.TYPE_HEADER:
				holder.tvName.setText(mission.name);
				break;
			default:
				throw new IllegalStateException("error value of type : " + type);
		}
	}

	public void updateData(ArrayList<ScoreMission> data) {
		if (data == null)
			return;
		mData = data;
		notifyDataSetChanged();
	}

	static class ViewHolder {
		ImageView ivIcon;
		TextView tvName;
		ScoreText stScore;
		TextView btnToDo;
	}

}
