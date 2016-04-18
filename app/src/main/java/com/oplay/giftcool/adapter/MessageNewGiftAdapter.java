package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.TypeStatusCode;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.StatisticsManager;
import com.oplay.giftcool.model.data.req.ReqChangeMessageStatus;
import com.oplay.giftcool.model.data.resp.message.PushMessage;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ViewUtil;
import com.socks.library.KLog;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 推送消息的适配器
 * <p/>
 * Created by zsigui on 16-3-7.
 */
public class MessageNewGiftAdapter extends BaseRVAdapter<PushMessage> implements View.OnClickListener {

	private final String TITLE_MODULE;
	private final String CONTENT_MODULE;


	public MessageNewGiftAdapter(Context context) {
		super(context);
		TITLE_MODULE = context.getResources().getString(R.string.st_msg_push_list_title);
		CONTENT_MODULE = context.getResources().getString(R.string.st_msg_push_list_content);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ItemHolder(LayoutInflater.from(mContext).inflate(R.layout.item_list_push_message, parent, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		PushMessage data = getItem(position);
		ItemHolder itemHolder = (ItemHolder) holder;
		itemHolder.tvTime.setText(DateUtil.optDate(data.time));
		itemHolder.ivHint.setVisibility(data.readState == TypeStatusCode.PUSH_UNREAD ? View.VISIBLE : View.GONE);
		itemHolder.tvName.setText(String.format(TITLE_MODULE, data.gameName));
		itemHolder.tvContent.setText(String.format(CONTENT_MODULE, data.giftContent));
		ViewUtil.showImage(itemHolder.ivIcon, data.img);
		itemHolder.llToGet.setOnClickListener(this);
		itemHolder.llToGet.setTag(TAG_POSITION, position);
		itemHolder.rlMsg.setOnClickListener(this);
		itemHolder.rlMsg.setTag(TAG_POSITION, position);
	}

	@Override
	public void onClick(View v) {
		if (v.getTag(TAG_POSITION) == null) {
			return;
		}
		Integer pos = (Integer) v.getTag(TAG_POSITION);
		PushMessage data = getItem(pos);
		switch (v.getId()) {
			case R.id.rl_msg:
			case R.id.ll_to_get:
				IntentUtil.jumpGiftDetail(mContext, data.giftId);
                if (data.readState != TypeStatusCode.PUSH_READED) {
                    notifyHasRead(new int[]{pos}, new int[]{data.id});
                }
				StatisticsManager.getInstance().trace(mContext, StatisticsManager.ID.USER_MESSAGE_CENTER_CLICK,
						String.format("游戏名:%s, 礼包id:%d, 游戏id:%d", data.gameName, data.giftId, data.gameId));
				break;
		}
	}

	private JsonReqBase<ReqChangeMessageStatus> mReqChangeObj;
	private Call<JsonRespBase<Void>> mCall;

	/**
	 * 通知修改已读消息
	 *
	 * @param pos
	 * @param ids
	 */
	private void notifyHasRead(final int[] pos, int[] ids) {
		if (mReqChangeObj == null) {
			ReqChangeMessageStatus msg = new ReqChangeMessageStatus();
			msg.status = TypeStatusCode.PUSH_READED;
			mReqChangeObj = new JsonReqBase<>(msg);
		} else {
            mReqChangeObj.data.status = TypeStatusCode.PUSH_READED;
        }
		// 构造用','分隔的字符串
		StringBuilder sb = new StringBuilder();
		if (ids != null && ids.length > 0) {
			for (int id : ids) {
				sb.append(id).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
		}
		mReqChangeObj.data.msgIds = sb.toString();
		if (mCall != null) {
			mCall.cancel();
		}
		mCall = Global.getNetEngine().changePushMessageStatus(mReqChangeObj);
		mCall.enqueue(new Callback<JsonRespBase<Void>>() {
			@Override
			public void onResponse(Call<JsonRespBase<Void>> call, Response<JsonRespBase<Void>> response) {
				if (call.isCanceled()) {
					return;
				}
				if (response != null && response.isSuccessful()) {
					if (response.body() != null && response.body().isSuccess()) {
						// 修改消息为已读状态
						if (pos != null && mData != null) {
							for (int p : pos) {
								// 更新为已阅读
								if (p < mData.size()) {
									mData.get(p).readState = TypeStatusCode.PUSH_READED;
									notifyItemChanged(p);
								}
							}
						}
						AccountManager.getInstance().obtainUnreadPushMessageCount();
						return;
					}
					if (AppDebugConfig.IS_DEBUG) {
						KLog.d(AppDebugConfig.TAG_ADAPTER,
								(response.body() == null ? "解析出错":response.body().error()));
					}
					return;
				}
				if (AppDebugConfig.IS_DEBUG) {
					KLog.d(AppDebugConfig.TAG_ADAPTER, (response == null ? "返回出错":response.code() + "," + response.message()));
				}
			}

			@Override
			public void onFailure(Call<JsonRespBase<Void>> call, Throwable t) {
				if (AppDebugConfig.IS_DEBUG) {
					KLog.d(AppDebugConfig.TAG_ADAPTER, t);
				}
			}
		});
	}

	@Override
	public void release() {
		super.release();
		if (mCall != null) {
			mCall.cancel();
		}
	}

	static class ItemHolder extends BaseRVHolder {

		RelativeLayout rlMsg;
		ImageView ivIcon;
		TextView tvName;
		ImageView ivHint;
		TextView tvContent;
		LinearLayout llToGet;
		TextView tvTime;

		public ItemHolder(View itemView) {
			super(itemView);
			rlMsg = getViewById(R.id.rl_msg);
			ivIcon = getViewById(R.id.iv_icon);
			tvName = getViewById(R.id.tv_name);
			ivHint = getViewById(R.id.iv_hint);
			tvContent = getViewById(R.id.tv_content);
			llToGet = getViewById(R.id.ll_to_get);
			tvTime = getViewById(R.id.tv_time);
		}
	}
}
