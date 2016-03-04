package com.oplay.giftcool.adapter.other;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.GiftTypeUtil;
import com.oplay.giftcool.listener.OnFinishListener;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.TimeDataList;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.ui.widget.stickylistheaders.StickyListHeadersAdapter;
import com.oplay.giftcool.util.ViewUtil;
import com.socks.library.KLog;

import net.ouwan.umipay.android.debug.Debug_Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by mink on 16-03-04.
 */
public class LimmitGiftListAdapter extends BaseAdapter implements View.OnClickListener, OnFinishListener, StickyListHeadersAdapter {

	private static final int TAG_POS = 0xFF331234;

	private List<String> mDate;
	private List<IndexGiftNew> mData;
	private Context mContext;
	private OnItemClickListener<IndexGiftNew> mListener;


	public LimmitGiftListAdapter(Context context) {
		this(context, null, null);
	}

	public LimmitGiftListAdapter(Context context, List<IndexGiftNew> data,List<String> date) {
		mContext = (context == null ? AssistantApp.getInstance().getApplicationContext()
				: context.getApplicationContext());
		this.mData = data;
		this.mDate = date;
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

	public void setData(List<IndexGiftNew> data,List<String> date) {
		mData = data;
		mDate = date;
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
			convertView = inflateView(parent, viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}


		final IndexGiftNew gift = mData.get(position);
		String date = "";
		try {
			date = mDate.get(position);
		}catch (Throwable e){
			Debug_Log.e(e);
		}

		setData(position, convertView, viewHolder, type, gift, date);

		return convertView;
	}

	/**
	 * 设置列表数据
	 */
	private void setData(int position, View convertView, ViewHolder viewHolder, int type, IndexGiftNew gift, String date) {
		setCommonField(viewHolder, gift);
		viewHolder.btnSend.setTag(TAG_POS, position);
		convertView.setTag(TAG_POS, position);
		viewHolder.btnSend.setState(type);
		viewHolder.btnSend.setOnClickListener(this);
		convertView.setOnClickListener(this);
		// 设置数据和按键状态
		if (type == GiftTypeUtil.TYPE_NORMAL_SEIZE
				|| type == GiftTypeUtil.TYPE_LIMIT_SEIZE
				|| type == GiftTypeUtil.TYPE_ZERO_SEIZE) {
			viewHolder.llMoney.setVisibility(View.VISIBLE);
			viewHolder.tvCount.setVisibility(View.GONE);
			viewHolder.tvScore.setText(String.valueOf(gift.score));
			int percent = gift.remainCount * 100 / gift.totalCount;
			viewHolder.tvPercent.setText(String.format("剩%d%%", percent));
			viewHolder.pbPercent.setProgress(percent);
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
		} else {
			switch (type) {
				case GiftTypeUtil.TYPE_LIMIT_WAIT_SEIZE:
					setDisabledField(viewHolder, View.VISIBLE,
							Html.fromHtml(String.format("开抢时间：<font color='#ffaa17'>%s</font>", gift
									.seizeTime)));
					break;
				case GiftTypeUtil.TYPE_LIMIT_FINISHED:
				case GiftTypeUtil.TYPE_NORMAL_FINISHED:
				case GiftTypeUtil.TYPE_LIMIT_EMPTY:
				case GiftTypeUtil.TYPE_LIMIT_SEIZED:
				case GiftTypeUtil.TYPE_NORMAL_SEIZED:
					setDisabledField(viewHolder, View.INVISIBLE, null);
					break;
				case GiftTypeUtil.TYPE_NORMAL_WAIT_SEIZE:
					setDisabledField(viewHolder, View.VISIBLE,
							Html.fromHtml(String.format("开抢时间：<font color='#ffaa17'>%s</font>", gift
									.seizeTime)));
					break;
				case GiftTypeUtil.TYPE_NORMAL_WAIT_SEARCH:
					setDisabledField(viewHolder, View.VISIBLE,
							Html.fromHtml(String.format("开淘时间：<font color='#ffaa17'>%s</font>", gift
									.searchTime)));
					break;
				case GiftTypeUtil.TYPE_NORMAL_SEARCH:
				case GiftTypeUtil.TYPE_NORMAL_SEARCHED:
					setDisabledField(viewHolder, View.VISIBLE,
							Html.fromHtml(String.format("已淘号：<font color='#ffaa17'>%d</font>次", gift
									.searchCount)));
					break;
			}
		}
	}

	/**
	 * 设置几个类型下的通用配置
	 */
	private void setCommonField(final ViewHolder itemHolder, final IndexGiftNew gift) {
		ViewUtil.showImage(itemHolder.ivIcon, gift.img);
		itemHolder.tvName.setText(String.format("[%s]%s", gift.gameName, gift.name));
		if (gift.giftType == GiftTypeUtil.GIFT_TYPE_LIMIT) {
			itemHolder.ivLimit.setVisibility(View.VISIBLE);
		} else {
			itemHolder.ivLimit.setVisibility(View.GONE);
		}
		if (gift.exclusive == 1) {
			itemHolder.tvName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_exclusive, 0, 0, 0);
		} else {
			itemHolder.tvName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
		itemHolder.tvContent.setText(String.format("%s", gift.content));
	}

	public void setDisabledField(ViewHolder itemHolder, int tvVisibility, Spanned tvText) {
		itemHolder.llMoney.setVisibility(View.GONE);
		itemHolder.pbPercent.setVisibility(View.GONE);
		itemHolder.tvPercent.setVisibility(View.GONE);
		itemHolder.tvCount.setVisibility(tvVisibility);
		itemHolder.tvCount.setText(tvText);
	}

	/**
	 * 根据XML填充convertView
	 */
	private View inflateView(ViewGroup parent, ViewHolder viewHolder) {
		View convertView;
		LayoutInflater inflater = LayoutInflater.from(mContext);
		convertView = inflater.inflate(R.layout.item_index_gift_limmit_list, parent, false);
		viewHolder.ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
		viewHolder.ivLimit = ViewUtil.getViewById(convertView, R.id.iv_limit);
		viewHolder.tvName = ViewUtil.getViewById(convertView, R.id.tv_name);
		viewHolder.tvContent = ViewUtil.getViewById(convertView, R.id.tv_content);
		viewHolder.btnSend = ViewUtil.getViewById(convertView, R.id.btn_send);
		viewHolder.tvScore = ViewUtil.getViewById(convertView, R.id.tv_score);
		viewHolder.tvOr = ViewUtil.getViewById(convertView, R.id.tv_or);
		viewHolder.tvBean = ViewUtil.getViewById(convertView, R.id.tv_bean);
		viewHolder.llMoney = ViewUtil.getViewById(convertView, R.id.ll_money);
		viewHolder.tvCount = ViewUtil.getViewById(convertView, R.id.tv_count);
		viewHolder.tvPercent = ViewUtil.getViewById(convertView, R.id.tv_percent);
		viewHolder.pbPercent = ViewUtil.getViewById(convertView, R.id.pb_percent);
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

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		if (getCount() == 0) {
			return null;
		}
		HeaderViewHolder headerViewHolder = null;

		// inflate 页面，设置 holder
		if (convertView == null) {
			headerViewHolder = new HeaderViewHolder();
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.item_header_gift_limmit, parent, false);
			headerViewHolder.tv_date = ViewUtil.getViewById(convertView, R.id.item_header_tv_gift_limmit_tv);
			convertView.setTag(headerViewHolder);
		} else {
			headerViewHolder = (HeaderViewHolder) convertView.getTag();
		}
		headerViewHolder.tv_date.setText(mDate.get(position));
		return convertView;
	}

	@Override
	public long getHeaderId(int position) {
		return mDate.get(position).subSequence(0, 1).charAt(0);
	}

	/**
	 * ViewHolder缓存，由于大体结构相似，统一一个ViewHolder类型
	 */
	static class ViewHolder {
		ImageView ivIcon;
		ImageView ivLimit;
		TextView tvName;
		TextView tvContent;
		GiftButton btnSend;
		TextView tvScore;
		TextView tvOr;
		TextView tvBean;
		TextView tvCount;
		TextView tvPercent;
		LinearLayout llMoney;
		ProgressBar pbPercent;
	}

	static class HeaderViewHolder {
		public TextView tv_date;

	}
	private static ArrayList<IndexGiftNew> praseDateFromTimeDataList(ArrayList<TimeDataList<IndexGiftNew>> data){
		ArrayList list = new ArrayList<IndexGiftNew>();
		try { Collections.sort(data, new Comparator<TimeDataList<IndexGiftNew>>() {
			@Override
			public int compare(TimeDataList<IndexGiftNew> lhs, TimeDataList<IndexGiftNew> rhs) {

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
				//将字符串形式的时间转化为Date类型的时间
				try {
					Date a = sdf.parse(lhs.date);
					Date b = sdf.parse(rhs.date);

					//Date类的一个方法，如果a早于b返回true，否则返回false
					if (a.before(b))
						return 1;
					else
						return -1;
				} catch (Throwable e) {
					if (AppDebugConfig.IS_DEBUG) {
						KLog.e(AppDebugConfig.TAG_FRAG, e);
					}
				}
				return -1;
			}
		});
			for (TimeDataList<IndexGiftNew> d : data) {
				list.addAll(d.data);
			}
		}catch (Throwable e){
			Debug_Log.e(e);
		}
		return list;
	}


}
