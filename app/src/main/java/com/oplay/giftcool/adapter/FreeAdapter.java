package com.oplay.giftcool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseListAdapter;
import com.oplay.giftcool.config.util.GiftTypeUtil;
import com.oplay.giftcool.listener.OnFinishListener;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.TimeData;
import com.oplay.giftcool.ui.widget.stickylistheaders.StickyListHeadersAdapter;
import com.oplay.giftcool.util.ViewUtil;

import java.util.List;

/**
 * Created by zsigui on 16-5-23.
 */
public class FreeAdapter extends BaseListAdapter<TimeData<IndexGiftNew>> implements View.OnClickListener,
		OnFinishListener, StickyListHeadersAdapter {

	public FreeAdapter(Context context, List<TimeData<IndexGiftNew>> objects) {
		super(context, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return null;
	}

	@Override
	public void onClick(View v) {

	}

	@Override
	public int getViewTypeCount() {
		return GiftTypeUtil.TYPE_COUNT;
	}

	@Override
	public int getItemViewType(int position) {
		return GiftTypeUtil.getItemViewType(mData.get(position).data);
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
		headerViewHolder.tv_date.setText(formatDateTime(mData.get(position).date));
		return convertView;
	}

	private String formatDateTime(String date) {
		return null;
	}

	@Override
	public long getHeaderId(int position) {
		return 0;
	}

	private static class HeaderViewHolder {
		public TextView tv_date;

	}
}
