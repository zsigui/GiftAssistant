package com.oplay.giftassistant.adapter;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.model.data.resp.IndexNewGift;
import com.oplay.giftassistant.util.ToastUtil;
import com.oplay.giftassistant.util.ViewUtil;
import com.socks.library.KLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by zsigui on 15-12-24.
 */
public class IndexGiftNewAdapter extends BaseAdapter {

	// 限量礼包类型，可抢，limit
	private static final int TYPE_LIMIT_SEIZE = 0;
	// 限量礼包类型，等待抢，disabled - text
	private static final int TYPE_LIMIT_WAIT_SEIZE = 1;
	// 限量礼包类型，已结束, disabled
	private static final int TYPE_LIMIT_FINISHED = 2;
	// 正常礼包类型，可抢，normal
	private static final int TYPE_NORMAL_SEIZE = 10;
	// 正常礼包类型，可淘号，disabled - text
	private static final int TYPE_NORMAL_SEARCH = 11;
	// 正常礼包类型，等待抢号，disabled - text
	private static final int TYPE_NORMAL_WAIT_SEIZE = 12;
	// 正常礼包类型，等待淘号，disabled - text
	private static final int TYPE_NORMAL_WAIT_SEARCH = 13;

	private List<IndexNewGift> mDatas;
	private Context mContext;
	private DisplayImageOptions mImageOptions;

	public IndexGiftNewAdapter(Context context) {
		this(context, null);
	}

	public IndexGiftNewAdapter(Context context, List<IndexNewGift> datas) {
		mContext = context;
		this.mDatas = datas;
		mImageOptions = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.ic_img_empty)
				.showImageOnFail(R.drawable.ic_img_fail)
				.showImageOnLoading(R.drawable.ic_img_loading)
				.build();
	}

	public void updateData(List<IndexNewGift> data) {
		this.mDatas = data;
		notifyDataSetChanged();
	}

	public List<IndexNewGift> getDatas() {
		return mDatas;
	}

	public void setDatas(List<IndexNewGift> datas) {
		mDatas = datas;
	}

	@Override
	public int getCount() {
		return mDatas == null ? 0 : mDatas.size();
	}

	@Override
	public Object getItem(int position) {
		return getCount() == 0 ? null : mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).hashCode();
	}

	@Override
	public int getViewTypeCount() {
		return 7;
	}

	@Override
	public int getItemViewType(int position) {
		IndexNewGift gift = mDatas.get(position);
		long currentTime = System.currentTimeMillis();
		if (gift.isLimit == 1) {
			if (currentTime > gift.seizeTime) {
				// 已经开抢
				if (gift.remainCount == 0) {
					// 已经结束
					return TYPE_LIMIT_FINISHED;
				} else {
					// 抢号中
					return TYPE_LIMIT_SEIZE;
				}
			} else {
				// 等待抢号
				// time 代表抢号时间
				return TYPE_LIMIT_WAIT_SEIZE;
			}
		} // if finished
		else {
			if (currentTime > gift.seizeTime) {
				// 已经开抢
				if (gift.remainCount == 0) {
					// 已经结束
					// 淘号逻辑
					if (currentTime > gift.searchTime) {
						// 处于淘号状态
						return TYPE_NORMAL_SEARCH;
					} else {
						// 等待淘号
						return TYPE_NORMAL_WAIT_SEARCH;
					}
				} else {
					// 抢号中
					return TYPE_NORMAL_SEIZE;
				}
			} else {
				// 等待抢号
				// time 代表抢号时间
				return TYPE_NORMAL_WAIT_SEIZE;
			}
		} // else finished
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (getCount() == 0) {
			return null;
		}

		ViewHolder viewHolder;
		int type = getItemViewType(position);

		if (AppDebugConfig.IS_DEBUG) {
			KLog.d(AppDebugConfig.TAG_APP, "view_type = " + type);
		}

		// inflate 页面，设置 holder
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = inflateView(parent, viewHolder, type);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}


		final IndexNewGift gift = mDatas.get(position);

		setCommonField(viewHolder, gift);
		// 设置数据和按键状态
		if (type == TYPE_NORMAL_SEIZE) {

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

		} else if (type == TYPE_LIMIT_SEIZE) {
			if (gift.bean == -1) {
				// 只用积分
				viewHolder.tvScore.setText(String.valueOf(gift.score));
				viewHolder.tvScore.setVisibility(View.VISIBLE);
				viewHolder.tvOr.setVisibility(View.GONE);
				viewHolder.tvBean.setVisibility(View.GONE);
			} else if (gift.score == -1) {
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
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
			switch (type) {
				case TYPE_LIMIT_WAIT_SEIZE:
					// 由于数量关系，限量礼包暂时没有等待抢号功能(由服务端提供数据排除)
					// 此部分逻辑一般不会执行
					setDisabledField(viewHolder, R.string.st_gift_wait_seize, false, R.drawable.selector_btn_red,
							View.VISIBLE, Html.fromHtml(String.format("开抢时间：<font color='#ffaa17'>%s</font>",
									sdf.format(new Date(gift.seizeTime)))));
					break;
				case TYPE_LIMIT_FINISHED:
					setDisabledField(viewHolder, R.string.st_gift_finished, false, R.drawable.selector_btn_red,
							View.GONE, null);
					break;
				case TYPE_NORMAL_WAIT_SEIZE:
					setDisabledField(viewHolder, R.string.st_gift_wait_seize, false, R.drawable.selector_btn_orange,
							View.VISIBLE, Html.fromHtml(String.format("开抢时间：<font color='#ffaa17'>%s</font>",
									sdf.format(new Date(gift.seizeTime)))));
					break;
				case TYPE_NORMAL_WAIT_SEARCH:
					setDisabledField(viewHolder, R.string.st_gift_wait_search, false, R.drawable.selector_btn_orange,
							View.VISIBLE, Html.fromHtml(String.format("开淘时间：<font color='#ffaa17'>%s</font>",
									sdf.format(new Date(gift.searchTime)))));
					break;
				case TYPE_NORMAL_SEARCH:
					setDisabledField(viewHolder, R.string.st_gift_search, true, R.drawable.selector_btn_orange,
							View.VISIBLE, Html.fromHtml(String.format("已淘号：<font color='#ffaa17'>%d</font>次",
									gift.searchCount)));
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
	private void setCommonField(ViewHolder viewHolder, final IndexNewGift gift) {
		ImageLoader.getInstance().displayImage(gift.img, viewHolder.ivIcon, mImageOptions);
		viewHolder.tvTitle.setText(String.format("[%s]%s", gift.gameName, gift.name));
		viewHolder.tvContent.setText(String.format("%s", gift.content));
		// 思考:
		// 1.是否需要设置定时闹钟Alarm通知主页数据重新刷新(比如开抢？)
		// 2. 刷新方式是 重新请求网络数据 或者 直接刷新界面？
		// 如有必要定义通知，后期添加，主页也要添加通知接口或者修改已有的
		viewHolder.btnSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
					/*
					// 进入礼包详情,抢号界面
					Intent intent = new Intent(mContext, GiftDetailActivity.class);
					intent.putExtra("game_name", gift.gameName);
					mContext.startActivity(intent);
					 */
				ToastUtil.showShort(String.format("[%s]%s 珍贵礼包抢号事件触发", gift.gameName, gift.name));
			}
		});
	}

	public void setDisabledField(ViewHolder viewHolder, @StringRes int btnText, boolean btnEnabled,
	                             @DrawableRes int btnBg, int tvVisibility, Spanned tvText) {
		viewHolder.btnSend.setText(btnText);
		viewHolder.btnSend.setEnabled(btnEnabled);
		viewHolder.btnSend.setBackgroundResource(btnBg);
		viewHolder.tvCount.setVisibility(tvVisibility);
		viewHolder.tvCount.setText(tvText);
	}

	/**
	 * 根据XML填充convertView
	 */
	private View inflateView(ViewGroup parent, ViewHolder viewHolder, int type) {
		View convertView;
		LayoutInflater inflater = LayoutInflater.from(mContext);
		if (type == TYPE_NORMAL_SEIZE) {
			convertView = inflater.inflate(R.layout.item_index_gift_new_normal, parent, false);
			viewHolder.ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
			viewHolder.tvTitle = ViewUtil.getViewById(convertView, R.id.tv_title);
			viewHolder.tvContent = ViewUtil.getViewById(convertView, R.id.tv_content);
			viewHolder.btnSend = ViewUtil.getViewById(convertView, R.id.btn_send);
			viewHolder.tvScore = ViewUtil.getViewById(convertView, R.id.tv_score);
			viewHolder.tvPercent = ViewUtil.getViewById(convertView, R.id.tv_percent);
			viewHolder.pbPercent = ViewUtil.getViewById(convertView, R.id.pb_percent);
		} else if (type == TYPE_LIMIT_SEIZE) {
			convertView = inflater.inflate(R.layout.item_index_gift_new_limit, parent, false);
			viewHolder.ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
			viewHolder.tvTitle = ViewUtil.getViewById(convertView, R.id.tv_title);
			viewHolder.tvContent = ViewUtil.getViewById(convertView, R.id.tv_content);
			viewHolder.btnSend = ViewUtil.getViewById(convertView, R.id.btn_send);
			viewHolder.tvScore = ViewUtil.getViewById(convertView, R.id.tv_score);
			viewHolder.tvOr = ViewUtil.getViewById(convertView, R.id.tv_or);
			viewHolder.tvBean = ViewUtil.getViewById(convertView, R.id.tv_bean);
			viewHolder.tvRemain = ViewUtil.getViewById(convertView, R.id.tv_remain_text);
		} else {
			convertView = inflater.inflate(R.layout.item_index_gift_new_disabled, parent, false);
			switch (type) {
				case TYPE_LIMIT_WAIT_SEIZE:
				case TYPE_NORMAL_WAIT_SEIZE:
				case TYPE_NORMAL_WAIT_SEARCH:
				case TYPE_NORMAL_SEARCH:
					viewHolder.tvCount = ViewUtil.getViewById(convertView, R.id.tv_count);
				case TYPE_LIMIT_FINISHED:
					viewHolder.ivIcon = ViewUtil.getViewById(convertView, R.id.iv_icon);
					viewHolder.tvTitle = ViewUtil.getViewById(convertView, R.id.tv_title);
					viewHolder.tvContent = ViewUtil.getViewById(convertView, R.id.tv_content);
					viewHolder.btnSend = ViewUtil.getViewById(convertView, R.id.btn_send);
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
		ImageView ivIcon;
		TextView tvTitle;
		TextView tvContent;
		TextView btnSend;
		TextView tvScore;
		TextView tvOr;
		TextView tvBean;
		TextView tvRemain;
		TextView tvCount;
		TextView tvPercent;
		ProgressBar pbPercent;
	}
}
