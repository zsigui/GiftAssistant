package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseListAdapter;
import com.oplay.giftcool.config.util.GiftTypeUtil;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.TimeData;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.ui.widget.stickylistheaders.StickyListHeadersAdapter;
import com.oplay.giftcool.util.ViewUtil;

import net.ouwan.umipay.android.debug.Debug_Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by zsigui on 16-5-23.
 */
public class FreeAdapter extends BaseListAdapter<TimeData<IndexGiftNew>> implements View.OnClickListener,
		StickyListHeadersAdapter {

	public FreeAdapter(Context context, List<TimeData<IndexGiftNew>> objects) {
		super(context, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (getCount() == 0) {
			return null;
		}

		int type = getItemViewType(position);
//		ViewHolder viewHolder;
//
//		// inflate 页面，设置 holder
//		if (convertView == null) {
//			viewHolder = new ViewHolder();
//			convertView = inflateView(parent, viewHolder);
//		} else {
//			viewHolder = (ViewHolder) convertView.getTag();
//		}

		switch (type) {
			case GiftTypeUtil.TYPE_CHARGE_UN_RESERVE:
			case GiftTypeUtil.TYPE_CHARGE_SEIZE:
			case GiftTypeUtil.TYPE_CHARGE_RESERVED:
			case GiftTypeUtil.TYPE_CHARGE_TAKE:
			case GiftTypeUtil.TYPE_CHARGE_SEIZED:
			case GiftTypeUtil.TYPE_CHARGE_EMPTY:
			case GiftTypeUtil.TYPE_CHARGE_RESERVE_EMPTY:
				ChargeHolder cHolder;
				if (convertView == null) {
					cHolder = new ChargeHolder();
					convertView = LayoutInflater.from(mContext).inflate(R.layout.item_index_free_gift_first_charge,
							parent, false);
					cHolder.ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
					cHolder.tvName = ViewUtil.getViewById(convertView, R.id.tv_name);
					cHolder.tvPlatform = ViewUtil.getViewById(convertView, R.id.tv_platform);
					cHolder.tvPrice = ViewUtil.getViewById(convertView, R.id.tv_content);
					cHolder.tvPercent = ViewUtil.getViewById(convertView, R.id.tv_percent);
					cHolder.pbPercent = ViewUtil.getViewById(convertView, R.id.pb_percent);
					cHolder.tvSeize = ViewUtil.getViewById(convertView, R.id.tv_seize);
					cHolder.tvSeizHint = ViewUtil.getViewById(convertView, R.id.tv_seize_hint);
					cHolder.tvReserveDeadline = ViewUtil.getViewById(convertView, R.id.tv_reserve_deadline);
					cHolder.btnSend = ViewUtil.getViewById(convertView, R.id.btn_send);
				} else {
					cHolder = (ChargeHolder) convertView.getTag();
				}

				IndexGiftNew o = getItem(position).data;
				ViewUtil.showImage(cHolder.ivIcon, o.img);
				cHolder.tvName.setText(o.name);
				cHolder.tvPlatform.setText(o.platform);
				ViewUtil.siteValueUI(cHolder.tvPrice, o.originPrice, true);
				switch (type) {
					case GiftTypeUtil.TYPE_CHARGE_UN_RESERVE:
						break;
				}
				break;
		}


		final IndexGiftNew gift = mData.get(position).data;
//		setData(position, convertView, viewHolder, type, gift);

		return convertView;
	}

//	/**
//	 * 设置几个类型下的通用配置
//	 */
//	private void setCommonField(final ViewHolder itemHolder, final IndexGiftNew gift) {
//		ViewUtil.showImage(itemHolder.ivIcon, gift.img);
//		itemHolder.tvName.setText(String.format("[%s]%s", gift.gameName, gift.name));
//		if (gift.exclusive == 1) {
//			itemHolder.tvName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_exclusive, 0, 0, 0);
//		} else {
//			itemHolder.tvName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
//		}
//	}
//
//	private void setData(int position, View convertView, ViewHolder viewHolder, int type, IndexGiftNew gift) {
//		setCommonField(viewHolder, gift);
//		viewHolder.btnSend.setTag(TAG_POSITION, position);
//		convertView.setTag(TAG_POSITION, position);
//		viewHolder.btnSend.setState(type);
//		viewHolder.btnSend.setOnClickListener(this);
//		convertView.setOnClickListener(this);
//		// 设置数据和按键状态
//		switch (type) {
//			case GiftTypeUtil.TYPE_NORMAL_SEIZE:
//			case GiftTypeUtil.TYPE_LIMIT_SEIZE:
//			case GiftTypeUtil.TYPE_ZERO_SEIZE:
//			case GiftTypeUtil.TYPE_LIMIT_FINISHED:
//			case GiftTypeUtil.TYPE_NORMAL_FINISHED:
//			case GiftTypeUtil.TYPE_LIMIT_EMPTY:
//			case GiftTypeUtil.TYPE_LIMIT_SEIZED:
//			case GiftTypeUtil.TYPE_NORMAL_SEIZED:
//				viewHolder.llMoney.setVisibility(View.VISIBLE);
//				viewHolder.tvCount.setVisibility(View.GONE);
//				viewHolder.tvScore.setText(String.valueOf(gift.score));
//				break;
//			default:
//				switch (type) {
//					case GiftTypeUtil.TYPE_LIMIT_WAIT_SEIZE:
//						setDisabledField(viewHolder, View.VISIBLE,
//								Html.fromHtml(String.format("开抢时间：<font color='#ffaa17'>%s</font>", gift
//										.seizeTime)));
//						break;
//					case GiftTypeUtil.TYPE_NORMAL_WAIT_SEIZE:
//						setDisabledField(viewHolder, View.VISIBLE,
//								Html.fromHtml(String.format("开抢时间：<font color='#ffaa17'>%s</font>", gift
//										.seizeTime)));
//						break;
//					case GiftTypeUtil.TYPE_NORMAL_WAIT_SEARCH:
//						setDisabledField(viewHolder, View.VISIBLE,
//								Html.fromHtml(String.format("开淘时间：<font color='#ffaa17'>%s</font>", gift
//										.searchTime)));
//						break;
//					case GiftTypeUtil.TYPE_NORMAL_SEARCH:
//					case GiftTypeUtil.TYPE_NORMAL_SEARCHED:
//						setDisabledField(viewHolder, View.VISIBLE,
//								Html.fromHtml(String.format("已淘号：<font color='#ffaa17'>%d</font>次", gift
//										.searchCount)));
//						break;
//				}
//
//		}
//	}
//
//	/**
//	 * 根据XML填充convertView
//	 */
//	private View inflateView(ViewGroup parent, ViewHolder viewHolder) {
//		View convertView;
//		LayoutInflater inflater = LayoutInflater.from(mContext);
//		convertView = inflater.inflate(R.layout.item_index_gift_limit_list, parent, false);
//		viewHolder.ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
////		viewHolder.ivLimit = ViewUtil.getViewById(convertView, R.id.iv_limit);
//		viewHolder.tvName = ViewUtil.getViewById(convertView, R.id.tv_name);
//		viewHolder.tvContent = ViewUtil.getViewById(convertView, R.id.tv_content);
//		viewHolder.btnSend = ViewUtil.getViewById(convertView, R.id.btn_send);
//		viewHolder.tvScore = ViewUtil.getViewById(convertView, R.id.tv_score);
//		viewHolder.tvOr = ViewUtil.getViewById(convertView, R.id.tv_or);
//		viewHolder.tvBean = ViewUtil.getViewById(convertView, R.id.tv_bean);
//		viewHolder.llMoney = ViewUtil.getViewById(convertView, R.id.ll_money);
//		viewHolder.tvCount = ViewUtil.getViewById(convertView, R.id.tv_count);
//		viewHolder.tvPercent = ViewUtil.getViewById(convertView, R.id.tv_percent);
//		viewHolder.pbPercent = ViewUtil.getViewById(convertView, R.id.pb_percent);
//		convertView.setTag(viewHolder);
//		return convertView;
//	}

	@Override
	public void onClick(View v) {

	}

	@Override
	public int getViewTypeCount() {
		return GiftTypeUtil.TYPE_COUNT;
	}

	@Override
	public int getItemViewType(int position) {
		return GiftTypeUtil.getItemViewTypeWithChargeCode(mData.get(position).data);
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		if (getCount() == 0) {
			return null;
		}
		HeaderViewHolder headerViewHolder;

		if (convertView == null) {
			headerViewHolder = new HeaderViewHolder();
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.item_header_gift_limmit, parent, false);
			headerViewHolder.tv_date = ViewUtil.getViewById(convertView, R.id.item_header_tv_gift_limmit_tv);
			convertView.setTag(headerViewHolder);
		} else {
			headerViewHolder = (HeaderViewHolder) convertView.getTag();
		}
		headerViewHolder.tv_date.setText(formatDateTime(mData.get(position).date));
		return convertView;
	}

	@Override
	public long getHeaderId(int position) {
		//用item数据的日期首次在列表中出现位置作为id
		String date = formatDateTime(mData.get(position).date);
		for (int i = 0; i < mData.size(); i++) {
			String d = formatDateTime(mData.get(i).date);
			if (d.equals(date)) {
				return i;
			}
		}
		return -1;
	}


	@Override
	public void release() {
		mContext = null;
		if (mData != null) {
			mData.clear();
			mData = null;
		}
		if (mCalendar != null) {
			mCalendar.clear();
			mCalendar = null;
		}
	}

	public ArrayMap<String, String> mCalendar;

	public String formatDateTime(String time) {
		String date = "";
		try {
			if (mCalendar == null) {
				mCalendar = new ArrayMap<>();
			}
			date = mCalendar.get(time);
			if (date == null) {
				date = formatDateTimeHelper(time);
				mCalendar.put(time, date);
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return date;
	}

	/**
	 * 格式化时间
	 *
	 * @param time
	 * @return
	 */
	private static String formatDateTimeHelper(String time) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
		if (TextUtils.isEmpty(time)) {
			return "";
		}
		Date date = null;
		try {

			date = format.parse(time);
			Calendar current = Calendar.getInstance();
			current.setTime(date);
			current.set(Calendar.HOUR_OF_DAY, 0);
			current.set(Calendar.MINUTE, 0);
			current.set(Calendar.SECOND, 30);

			Calendar today = Calendar.getInstance();    //今天
			today.set(Calendar.YEAR, current.get(Calendar.YEAR));
			today.set(Calendar.MONTH, current.get(Calendar.MONTH));
			today.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH));
			//  Calendar.HOUR——12小时制的小时数 Calendar.HOUR_OF_DAY——24小时制的小时数
			today.set(Calendar.HOUR_OF_DAY, 0);
			today.set(Calendar.MINUTE, 0);
			today.set(Calendar.SECOND, 0);

			if (current.before(today)) {
				return "昨天/以前";
			} else if (current.after(today)) {
				return "明天/以后";
			} else {
				return "今天";
			}
		} catch (ParseException e) {
			Debug_Log.e(e);
		}
		return time;
	}

	private static class HeaderViewHolder {
		public TextView tv_date;

	}

	private static class ViewHolder {
		ImageView ivIcon;
		//ImageView ivLimit;
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

	private static class ChargeHolder {
		ImageView ivIcon;
		TextView tvName;
		TextView tvPrice;
		TextView tvPlatform;
		GiftButton btnSend;
		TextView tvSeize;
		TextView tvPercent;
		ProgressBar pbPercent;
		TextView tvReserveDeadline;
		TextView tvSeizHint;
	}
}
