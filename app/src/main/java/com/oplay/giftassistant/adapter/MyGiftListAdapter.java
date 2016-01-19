package com.oplay.giftassistant.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.KeyConfig;
import com.oplay.giftassistant.listener.OnItemClickListener;
import com.oplay.giftassistant.model.data.resp.IndexGiftNew;
import com.oplay.giftassistant.util.DateUtil;
import com.oplay.giftassistant.util.DensityUtil;
import com.oplay.giftassistant.util.IntentUtil;
import com.oplay.giftassistant.util.ToastUtil;

import java.util.ArrayList;

import cn.bingoogolapple.androidcommon.adapter.BGARecyclerViewAdapter;
import cn.bingoogolapple.androidcommon.adapter.BGAViewHolderHelper;

/**
 * Created by zsigui on 16-1-7.
 */
public class MyGiftListAdapter extends BGARecyclerViewAdapter<IndexGiftNew> {

	private OnItemClickListener<IndexGiftNew> mItemClickListener;
	private int mType;

	public MyGiftListAdapter(RecyclerView recyclerView, int type) {
		super(recyclerView, R.layout.item_list_my_gift);
		this.mType = type;
	}

	public void setItemClickListener(OnItemClickListener<IndexGiftNew> itemClickListener) {
		mItemClickListener = itemClickListener;
	}

	@Override
	protected void fillData(BGAViewHolderHelper bgaViewHolderHelper, int i, final IndexGiftNew o) {
		bgaViewHolderHelper.setText(R.id.tv_name, o.name);
		if (o.isLimit) {
			bgaViewHolderHelper.setVisibility(R.id.iv_limit, View.VISIBLE);
			bgaViewHolderHelper.getView(R.id.tv_name).setPadding(DensityUtil.dip2px(mContext, 4), 0, 0, 0);
		} else {
			bgaViewHolderHelper.setVisibility(R.id.iv_limit, View.GONE);
			bgaViewHolderHelper.getView(R.id.tv_name).setPadding(DensityUtil.dip2px(mContext, 7), 0, 0, 0);
		}
		ImageLoader.getInstance().displayImage(o.img, (ImageView) bgaViewHolderHelper.getView(R.id.iv_icon));
		bgaViewHolderHelper.setText(R.id.tv_content, o.content);
		bgaViewHolderHelper.setText(R.id.tv_deadline, DateUtil.formatTime(o.useStartTime, "yyyy.MM.dd HH:mm") + " ~ "
				+ DateUtil.formatTime(o.useEndTime, "yyyy.MM.dd HH:mm"));
		bgaViewHolderHelper.setText(R.id.tv_gift_code,
				Html.fromHtml(String.format("礼包码: <font color='#ffaa17'>%s</font>", o.code)));
		if (mType == KeyConfig.TYPE_KEY_OVERTIME) {
			bgaViewHolderHelper.getView(R.id.btn_copy).setEnabled(false);
			bgaViewHolderHelper.setText(R.id.btn_copy, "已结束");
		} else {
			bgaViewHolderHelper.getView(R.id.btn_copy).setEnabled(true);
			bgaViewHolderHelper.setText(R.id.btn_copy, "复制");
			bgaViewHolderHelper.getView(R.id.btn_copy).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ClipboardManager cmb = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
					cmb.setPrimaryClip(ClipData.newPlainText("礼包码", o.code));
					ToastUtil.showShort("已复制");
				}
			});
		}
		bgaViewHolderHelper.getView(R.id.rl_recommend).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				IntentUtil.jumpGiftDetail(mContext, o.id);
			}
		});
	}

	public void updateData(ArrayList<IndexGiftNew> data) {
		if (data == null)
			return;
		mDatas = data;
		notifyDataSetChanged();
	}
}
