package com.oplay.giftassistant.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.TaskTypeUtil;
import com.oplay.giftassistant.listener.OnItemClickListener;
import com.oplay.giftassistant.model.data.resp.ScoreMission;
import com.oplay.giftassistant.ui.widget.ScoreText;
import com.oplay.giftassistant.util.ViewUtil;

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
				ImageLoader.getInstance().displayImage("drawable://" + mission.icon, holder.ivIcon);
				holder.tvName.setText(mission.name);
				holder.stScore.setText("+" + mission.rewardScore);
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
