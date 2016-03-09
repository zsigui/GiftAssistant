package com.oplay.giftcool.adapter.other;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
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
import com.oplay.giftcool.model.data.resp.TimeData;
import com.oplay.giftcool.model.data.resp.TimeDataList;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.ui.widget.stickylistheaders.StickyListHeadersAdapter;
import com.oplay.giftcool.util.ViewUtil;
import com.socks.library.KLog;

import net.ouwan.umipay.android.debug.Debug_Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by mink on 16-03-04.
 */
public class LimitGiftListAdapter extends BaseAdapter implements View.OnClickListener, OnFinishListener, StickyListHeadersAdapter {

	private static final int TAG_POS = 0xFF331234;

	private List<TimeData<IndexGiftNew>> mTimeData;
	private Context mContext;
	private OnItemClickListener<IndexGiftNew> mListener;
	private HashMap<String,String> calendar;


	public LimitGiftListAdapter(Context context) {
		this(context, null);
	}

	public LimitGiftListAdapter(Context context, List<TimeData<IndexGiftNew>> data) {
		mContext = (context == null ? AssistantApp.getInstance().getApplicationContext()
				: context.getApplicationContext());
		this.mTimeData = data;
	}

	public void updateData(List<TimeData<IndexGiftNew>> data) {
		this.mTimeData = data;
		notifyDataSetChanged();
	}

	public OnItemClickListener<IndexGiftNew> getListener() {
		return mListener;
	}

	public void setListener(OnItemClickListener<IndexGiftNew> listener) {
		mListener = listener;
	}


	public List<TimeData<IndexGiftNew>> getData() {
		return mTimeData;
	}


	public void setData(List<TimeData<IndexGiftNew>> data) {
		this.mTimeData = data;
	}


	@Override
	public int getCount() {
		return mTimeData == null ? 0 : mTimeData.size();
	}

	@Override
	public Object getItem(int position) {
		return getCount() == 0 ? null : mTimeData.get(position);
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
		return GiftTypeUtil.getItemViewType(mTimeData.get(position).data);
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


		final IndexGiftNew gift = mTimeData.get(position).data;
		String date  = mTimeData.get(position).date;

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
		convertView = inflater.inflate(R.layout.item_index_gift_limit_list, parent, false);
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
				if (mTimeData != null && pos < mTimeData.size()) {
					IndexGiftNew gift = mTimeData.get(pos).data;
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
		mTimeData = null;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		if (getCount() == 0) {
			return null;
		}
		HeaderViewHolder headerViewHolder = null;

		if (convertView == null) {
			headerViewHolder = new HeaderViewHolder();
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.item_header_gift_limmit, parent, false);
			headerViewHolder.tv_date = ViewUtil.getViewById(convertView, R.id.item_header_tv_gift_limmit_tv);
			convertView.setTag(headerViewHolder);
		} else {
			headerViewHolder = (HeaderViewHolder) convertView.getTag();
		}
		headerViewHolder.tv_date.setText(formatDateTime(mTimeData.get(position).date));
		return convertView;
	}

	@Override
	public long getHeaderId(int position) {
		//用item数据的日期首次在列表中出现位置作为id
		String date = formatDateTime(mTimeData.get(position).date);
		for(int i= 0; i< mTimeData.size();i++){
			String d = formatDateTime(mTimeData.get(i).date);
			if(d.equals(date) ){
				return i;
			}
		}
		return -1;
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

	public String formatDateTime(String time){
		String date = "";
		try {
			if(calendar == null){
				calendar = new HashMap<String,String>();
			}
			date = calendar.get(time);
			if (date == null) {
				date = formatDateTimeHelper(time);
				calendar.put(time, date);
			}
		}catch (Throwable e){
			Debug_Log.e(e);
		}
		return date;
	}

	/**
	 * 格式化时间
	 * @param time
	 * @return
	 */
	private static String formatDateTimeHelper(String time) {
		SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd");
		if(TextUtils.isEmpty(time)){
			return "";
		}
		Date date = null;
		try {

			date = format.parse(time);
			Calendar current = Calendar.getInstance();
			Calendar today = Calendar.getInstance();	//今天

			today.set(Calendar.YEAR, current.get(Calendar.YEAR));
			today.set(Calendar.MONTH, current.get(Calendar.MONTH));
			today.set(Calendar.DAY_OF_MONTH,current.get(Calendar.DAY_OF_MONTH));
			//  Calendar.HOUR——12小时制的小时数 Calendar.HOUR_OF_DAY——24小时制的小时数
			today.set( Calendar.HOUR_OF_DAY, 0);
			today.set( Calendar.MINUTE, 0);
			today.set(Calendar.SECOND, 0);

			Calendar yesterday = Calendar.getInstance();	//昨天

			yesterday.set(Calendar.YEAR, current.get(Calendar.YEAR));
			yesterday.set(Calendar.MONTH, current.get(Calendar.MONTH));
			yesterday.set(Calendar.DAY_OF_MONTH,current.get(Calendar.DAY_OF_MONTH) - 1);
			yesterday.set( Calendar.HOUR_OF_DAY, 0);
			yesterday.set( Calendar.MINUTE, 0);
			yesterday.set(Calendar.SECOND, 0);

			current.setTime(date);
			current.set( Calendar.HOUR_OF_DAY, 0);
			current.set(Calendar.MINUTE, 0);
			current.set(Calendar.SECOND, 30);

			if(current.after(today)){
				return "今天";
			}else if(current.before(today) && current.after(yesterday)){
				return "昨天";
			}else{
				return "以前";
			}
		} catch (ParseException e) {
			Debug_Log.e(e);
		}
		return time;
	}

}
