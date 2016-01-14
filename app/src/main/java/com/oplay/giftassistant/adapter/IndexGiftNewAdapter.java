package com.oplay.giftassistant.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.GiftTypeUtil;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.config.KeyConfig;
import com.oplay.giftassistant.manager.PayManager;
import com.oplay.giftassistant.model.data.resp.IndexGiftNew;
import com.oplay.giftassistant.ui.activity.GiftDetailActivity;
import com.oplay.giftassistant.ui.widget.button.GiftButton;
import com.oplay.giftassistant.util.DensityUtil;
import com.oplay.giftassistant.util.ViewUtil;

import java.util.List;

/**
 * Created by zsigui on 15-12-24.
 */
public class IndexGiftNewAdapter extends BaseAdapter {

	private List<IndexGiftNew> mData;
	private Context mContext;


	public IndexGiftNewAdapter(Context context) {
		this(context, null);
	}

	public IndexGiftNewAdapter(Context context, List<IndexGiftNew> data) {
		mContext = context;
		this.mData = data;
	}

	public void updateData(List<IndexGiftNew> data) {
		this.mData = data;
		notifyDataSetChanged();
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
		viewHolder.btnSend.setState(type);
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

		} else if (type == GiftTypeUtil.TYPE_LIMIT_SEIZE) {
			if (gift.priceType == GiftTypeUtil.PAY_TYPE_SCORE) {
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
					// 由于数量关系，限量礼包暂时没有等待抢号功能(由服务端状态排除)
					// 此部分逻辑一般不会执行
					setDisabledField(viewHolder, View.VISIBLE,
							Html.fromHtml(String.format("开抢时间：<font color='#ffaa17'>%s</font>", gift.seizeTime)));
					break;
				case GiftTypeUtil.TYPE_LIMIT_FINISHED:
				case GiftTypeUtil.TYPE_NORMAL_FINISHED:
				case GiftTypeUtil.TYPE_LIMIT_EMPTY:
				case GiftTypeUtil.TYPE_LIMIT_SEIZED:
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
				default:
					throw new IllegalStateException("type is not support! " + type);
			}
		}

		return convertView;
	}

	/**
	 * 设置几个类型下的通用配置
	 */
	private void setCommonField(final ViewHolder viewHolder, final IndexGiftNew gift) {
		ImageAware imageAware = new ImageViewAware(viewHolder.ivIcon, false);
		ImageLoader.getInstance().displayImage(gift.img, imageAware, Global.IMAGE_OPTIONS);
		final String name = String.format("[%s]%s", gift.gameName, gift.name);
		viewHolder.tvTitle.setText(name);
		if (gift.isLimit) {
			viewHolder.tvTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_limit_tag, 0, 0, 0);
			viewHolder.tvTitle.setCompoundDrawablePadding(DensityUtil.dip2px(mContext, 4));
		} else {
			viewHolder.tvTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
		viewHolder.tvContent.setText(String.format("%s", gift.content));
		viewHolder.btnSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (viewHolder.btnSend.getStatus()) {
					case GiftTypeUtil.TYPE_LIMIT_SEIZE:
					case GiftTypeUtil.TYPE_NORMAL_SEIZE:
						PayManager.getInstance().chargeGift(mContext, gift, viewHolder.btnSend);
						break;
					case GiftTypeUtil.TYPE_NORMAL_SEARCH:
						PayManager.getInstance().searchGift(mContext, gift, viewHolder.btnSend);
						break;
				}
			}
		});
		viewHolder.rlItem.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, GiftDetailActivity.class);
				intent.putExtra(KeyConfig.KEY_DATA, gift.id);
				intent.putExtra(KeyConfig.KEY_NAME, name);
				mContext.startActivity(intent);
			}
		});
	}

	public void setDisabledField(ViewHolder viewHolder, int tvVisibility, Spanned tvText) {
		viewHolder.tvCount.setVisibility(tvVisibility);
		viewHolder.tvCount.setText(tvText);
	}

	/**
	 * 根据XML填充convertView
	 */
	private View inflateView(ViewGroup parent, ViewHolder viewHolder, int type) {
		View convertView;
		LayoutInflater inflater = LayoutInflater.from(mContext);
		if (type == GiftTypeUtil.TYPE_NORMAL_SEIZE) {
			convertView = inflater.inflate(R.layout.item_index_gift_new_normal, parent, false);
			viewHolder.ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
			viewHolder.tvTitle = ViewUtil.getViewById(convertView, R.id.tv_name);
			viewHolder.tvContent = ViewUtil.getViewById(convertView, R.id.tv_play);
			viewHolder.btnSend = ViewUtil.getViewById(convertView, R.id.btn_send);
			viewHolder.tvScore = ViewUtil.getViewById(convertView, R.id.tv_score);
			viewHolder.tvPercent = ViewUtil.getViewById(convertView, R.id.tv_percent);
			viewHolder.pbPercent = ViewUtil.getViewById(convertView, R.id.pb_percent);
			viewHolder.rlItem = ViewUtil.getViewById(convertView, R.id.rl_recommend);
		} else if (type == GiftTypeUtil.TYPE_LIMIT_SEIZE) {
			convertView = inflater.inflate(R.layout.item_index_gift_new_limit, parent, false);
			viewHolder.ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
			viewHolder.tvTitle = ViewUtil.getViewById(convertView, R.id.tv_name);
			viewHolder.tvContent = ViewUtil.getViewById(convertView, R.id.tv_play);
			viewHolder.btnSend = ViewUtil.getViewById(convertView, R.id.btn_send);
			viewHolder.tvScore = ViewUtil.getViewById(convertView, R.id.tv_score);
			viewHolder.tvOr = ViewUtil.getViewById(convertView, R.id.tv_or);
			viewHolder.tvBean = ViewUtil.getViewById(convertView, R.id.tv_bean);
			viewHolder.tvRemain = ViewUtil.getViewById(convertView, R.id.tv_new_text);
			viewHolder.rlItem = ViewUtil.getViewById(convertView, R.id.rl_recommend);
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
					viewHolder.tvCount = ViewUtil.getViewById(convertView, R.id.tv_count);
					viewHolder.ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
					viewHolder.tvTitle = ViewUtil.getViewById(convertView, R.id.tv_name);
					viewHolder.tvContent = ViewUtil.getViewById(convertView, R.id.tv_play);
					viewHolder.btnSend = ViewUtil.getViewById(convertView, R.id.btn_send);
					viewHolder.rlItem = ViewUtil.getViewById(convertView, R.id.rl_recommend);
					break;
				default:
					throw new IllegalStateException("type is not support! " + type);
			}
		}
		convertView.setTag(viewHolder);
		return convertView;
	}

	/**
	 * ViewHolder缓存，由于大体结构相似，统一一个ViewHolder类型
	 */
	static class ViewHolder {
		RelativeLayout rlItem;
		ImageView ivIcon;
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
