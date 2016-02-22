package com.oplay.giftcool.adapter;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.GiftTypeUtil;
import com.oplay.giftcool.listener.OnFinishListener;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.util.ViewUtil;
import com.socks.library.KLog;

import java.util.List;

/**
 * Created by zsigui on 15-12-24.
 */
public class NestedGiftListAdapter extends BaseAdapter implements View.OnClickListener, OnFinishListener {

	private static final int TAG_POS = 0xFF331234;

	private List<IndexGiftNew> mData;
	private Context mContext;
	private OnItemClickListener<IndexGiftNew> mListener;


	public NestedGiftListAdapter(Context context) {
		this(context, null);
	}

	public NestedGiftListAdapter(Context context, List<IndexGiftNew> data) {
		mContext = (context == null ? AssistantApp.getInstance().getApplicationContext() : context);
		this.mData = data;
	}

	public void updateData(List<IndexGiftNew> data) {
		this.mData = data;
		notifyDataSetChanged();
	}

	public OnItemClickListener<IndexGiftNew> getListener() {
		return mListener;
	}

	public void setListener(OnItemClickListener<IndexGiftNew> listener) {
		mListener = listener;
	}

	public List<IndexGiftNew> getData() {
		return mData;
	}

	public void setData(List<IndexGiftNew> data) {
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
		return getItem(position).hashCode();
	}

	@Override
	public int getViewTypeCount() {
		return GiftTypeUtil.TYPE_COUNT;
	}

	/**
	 * 获取ListItem类型<br/>
	 * 注意: 返回的 int 需要范围为 0 ~ getViewTypeCount() - 1, 否则会出现ArrayIndexOutOfBoundsException
	 */
	@Override
	public int getItemViewType(int position) {
		return GiftTypeUtil.getItemViewType(mData.get(position));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (getCount() == 0) {
			return null;
		}

		ViewHolder viewHolder;
		int type = getItemViewType(position);

		// inflate 页面，设置 holder
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = inflateView(parent, viewHolder, type);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}


		final IndexGiftNew gift = mData.get(position);

		setCommonField(viewHolder, gift);
		viewHolder.btnSend.setTag(TAG_POS, position);
		convertView.setTag(TAG_POS, position);
		viewHolder.btnSend.setState(type);
		viewHolder.btnSend.setOnClickListener(this);
		convertView.setOnClickListener(this);
		// 设置数据和按键状态
		if (type == GiftTypeUtil.TYPE_NORMAL_SEIZE) {

			viewHolder.tvScore.setVisibility(View.VISIBLE);
			viewHolder.tvScore.setText(String.valueOf(gift.score));
			int percent = gift.remainCount * 100 / gift.totalCount;
			if (percent > 0 && percent < 10) {
				viewHolder.tvPercent.setText("剩余  " + percent + "%");
			} else if (percent >= 10 && percent < 100) {
				viewHolder.tvPercent.setText("剩余 " + percent + "%");
			} else {
				viewHolder.tvPercent.setText("剩余" + percent + "%");
			}
			viewHolder.pbPercent.setProgress(percent);

		} else if (type == GiftTypeUtil.TYPE_LIMIT_SEIZE
				|| type == GiftTypeUtil.TYPE_ZERO_SEIZE) {
			if (gift.priceType == GiftTypeUtil.PAY_TYPE_SCORE
					&& gift.giftType != GiftTypeUtil.GIFT_TYPE_ZERO_SEIZE) {
				// 只用积分
				viewHolder.tvScore.setText(String.valueOf(gift.score));
				viewHolder.tvScore.setVisibility(View.VISIBLE);
				viewHolder.tvOr.setVisibility(View.GONE);
				viewHolder.tvBean.setVisibility(View.GONE);
			} else if (gift.priceType == GiftTypeUtil.PAY_TYPE_BEAN) {
				// 只用偶玩豆
				viewHolder.tvBean.setText(String.valueOf(gift.bean));
				viewHolder.tvBean.setVisibility(View.VISIBLE);
				viewHolder.tvOr.setVisibility(View.GONE);
				viewHolder.tvScore.setVisibility(View.GONE);
			} else {
				// 积分 或 偶玩豆
				viewHolder.tvScore.setText(String.valueOf(gift.score));
				viewHolder.tvBean.setText(String.valueOf(gift.bean));
				viewHolder.tvScore.setVisibility(View.VISIBLE);
				viewHolder.tvOr.setVisibility(View.VISIBLE);
				viewHolder.tvBean.setVisibility(View.VISIBLE);
			}
			viewHolder.tvRemain.setText(Html.fromHtml(String.format("剩余 <font color='#ffaa17'>%d个</font>",
					gift.remainCount)));
		} else {
			switch (type) {
				case GiftTypeUtil.TYPE_LIMIT_WAIT_SEIZE:
					setDisabledField(viewHolder, View.VISIBLE,
							Html.fromHtml(String.format("开抢时间：<font color='#ffaa17'>%s</font>", gift.seizeTime)));
					break;
				case GiftTypeUtil.TYPE_LIMIT_FINISHED:
				case GiftTypeUtil.TYPE_NORMAL_FINISHED:
				case GiftTypeUtil.TYPE_LIMIT_EMPTY:
				case GiftTypeUtil.TYPE_LIMIT_SEIZED:
				case GiftTypeUtil.TYPE_NORMAL_SEIZED:
					setDisabledField(viewHolder, View.GONE, null);
					break;
				case GiftTypeUtil.TYPE_NORMAL_WAIT_SEIZE:
					setDisabledField(viewHolder, View.VISIBLE,
							Html.fromHtml(String.format("开抢时间：<font color='#ffaa17'>%s</font>", gift.seizeTime)));
					break;
				case GiftTypeUtil.TYPE_NORMAL_WAIT_SEARCH:
					setDisabledField(viewHolder, View.VISIBLE,
							Html.fromHtml(String.format("开淘时间：<font color='#ffaa17'>%s</font>", gift.searchTime)));
					break;
				case GiftTypeUtil.TYPE_NORMAL_SEARCH:
				case GiftTypeUtil.TYPE_NORMAL_SEARCHED:
					setDisabledField(viewHolder, View.VISIBLE,
							Html.fromHtml(String.format("已淘号：<font color='#ffaa17'>%d</font>次", gift.searchCount)));
					break;
			}
		}

		return convertView;
	}

	/**
	 * 设置几个类型下的通用配置
	 */
	private void setCommonField(final ViewHolder viewHolder, final IndexGiftNew gift) {
		ViewUtil.showImage(viewHolder.ivIcon, gift.img);
		viewHolder.tvTitle.setText(String.format("[%s]%s", gift.gameName, gift.name));
		if (gift.giftType == GiftTypeUtil.GIFT_TYPE_LIMIT) {
			viewHolder.ivLimit.setVisibility(View.VISIBLE);
		} else {
			viewHolder.ivLimit.setVisibility(View.GONE);
		}
		viewHolder.tvContent.setText(String.format("%s", gift.content));
	}

	public void setDisabledField(ViewHolder viewHolder, int tvVisibility, Spanned tvText) {
		if (viewHolder.tvCount != null) {
			viewHolder.tvCount.setVisibility(tvVisibility);
			viewHolder.tvCount.setText(tvText);
		}
	}

	/**
	 * 根据XML填充convertView
	 */
	private View inflateView(ViewGroup parent, ViewHolder viewHolder, int type) {
		View convertView;
		LayoutInflater inflater = LayoutInflater.from(mContext);
		if (type == GiftTypeUtil.TYPE_NORMAL_SEIZE ) {
			convertView = inflater.inflate(R.layout.item_index_gift_new_normal, parent, false);
			viewHolder.ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
			viewHolder.ivLimit = ViewUtil.getViewById(convertView, R.id.iv_limit);
			viewHolder.tvTitle = ViewUtil.getViewById(convertView, R.id.tv_name);
			viewHolder.tvContent = ViewUtil.getViewById(convertView, R.id.tv_play);
			viewHolder.btnSend = ViewUtil.getViewById(convertView, R.id.btn_send);
			viewHolder.tvScore = ViewUtil.getViewById(convertView, R.id.tv_score);
			viewHolder.tvPercent = ViewUtil.getViewById(convertView, R.id.tv_percent);
			viewHolder.pbPercent = ViewUtil.getViewById(convertView, R.id.pb_percent);
		} else if (type == GiftTypeUtil.TYPE_LIMIT_SEIZE || type ==  GiftTypeUtil.TYPE_ZERO_SEIZE) {
			convertView = inflater.inflate(R.layout.item_index_gift_new_limit, parent, false);
			viewHolder.ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
			viewHolder.ivLimit = ViewUtil.getViewById(convertView, R.id.iv_limit);
			viewHolder.tvTitle = ViewUtil.getViewById(convertView, R.id.tv_name);
			viewHolder.tvContent = ViewUtil.getViewById(convertView, R.id.tv_play);
			viewHolder.btnSend = ViewUtil.getViewById(convertView, R.id.btn_send);
			viewHolder.tvScore = ViewUtil.getViewById(convertView, R.id.tv_score);
			viewHolder.tvOr = ViewUtil.getViewById(convertView, R.id.tv_or);
			viewHolder.tvBean = ViewUtil.getViewById(convertView, R.id.tv_bean);
			viewHolder.tvRemain = ViewUtil.getViewById(convertView, R.id.tv_new_text);
		} else {
			convertView = inflater.inflate(R.layout.item_index_gift_new_disabled, parent, false);
			switch (type) {
				case GiftTypeUtil.TYPE_LIMIT_WAIT_SEIZE:
				case GiftTypeUtil.TYPE_NORMAL_WAIT_SEIZE:
				case GiftTypeUtil.TYPE_NORMAL_WAIT_SEARCH:
				case GiftTypeUtil.TYPE_NORMAL_SEARCHED:
				case GiftTypeUtil.TYPE_LIMIT_SEIZED:
				case GiftTypeUtil.TYPE_LIMIT_EMPTY:
				case GiftTypeUtil.TYPE_NORMAL_SEARCH:
				case GiftTypeUtil.TYPE_LIMIT_FINISHED:
				case GiftTypeUtil.TYPE_NORMAL_FINISHED:
				case GiftTypeUtil.TYPE_NORMAL_SEIZED:
					viewHolder.ivLimit = ViewUtil.getViewById(convertView, R.id.iv_limit);
					viewHolder.tvCount = ViewUtil.getViewById(convertView, R.id.tv_count);
					viewHolder.ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
					viewHolder.tvTitle = ViewUtil.getViewById(convertView, R.id.tv_name);
					viewHolder.tvContent = ViewUtil.getViewById(convertView, R.id.tv_play);
					viewHolder.btnSend = ViewUtil.getViewById(convertView, R.id.btn_send);
					break;
			}
		}
		convertView.setTag(viewHolder);
		return convertView;
	}

	@Override
	public void onClick(View v) {
		try {
			if (v.getTag(TAG_POS) != null) {
				Integer pos = (Integer) v.getTag(TAG_POS);
				if (mData != null && pos < mData.size()) {
					IndexGiftNew gift = mData.get(pos);
					if (mListener != null) {
						mListener.onItemClick(gift, v, pos);
					}
				}
			}
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_ADAPTER, e);
			}
		}
	}

	@Override
	public void release() {
		mContext = null;
		mListener = null;
		mData = null;
	}

	/**
	 * ViewHolder缓存，由于大体结构相似，统一一个ViewHolder类型
	 */
	static class ViewHolder {
		ImageView ivIcon;
		ImageView ivLimit;
		TextView tvTitle;
		TextView tvContent;
		GiftButton btnSend;
		TextView tvScore;
		TextView tvOr;
		TextView tvBean;
		TextView tvRemain;
		TextView tvCount;
		TextView tvPercent;
		ProgressBar pbPercent;
	}
}
