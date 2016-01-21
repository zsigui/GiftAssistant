package com.oplay.giftcool.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.GameTypeUtil;
import com.oplay.giftcool.model.data.resp.IndexGiftLike;
import com.oplay.giftcool.util.IntentUtil;

import java.util.ArrayList;

import cn.bingoogolapple.androidcommon.adapter.BGARecyclerViewAdapter;
import cn.bingoogolapple.androidcommon.adapter.BGAViewHolderHelper;

/**
 * Created by zsigui on 15-12-30.
 */
public class GiftLikeListAdapter extends BGARecyclerViewAdapter<IndexGiftLike> {


	public GiftLikeListAdapter(RecyclerView recyclerView) {
		super(recyclerView, R.layout.item_list_gift_like);
	}

	public GiftLikeListAdapter(RecyclerView recyclerView, ArrayList<IndexGiftLike> data) {
		this(recyclerView);
		this.mDatas = data;
	}

	@Override
	protected void fillData(BGAViewHolderHelper bgaViewHolderHelper, int i, final IndexGiftLike o) {
		bgaViewHolderHelper.setText(R.id.tv_name, o.name);
		if (o.playCount > 10000) {
			bgaViewHolderHelper.setText(R.id.tv_play, Html.fromHtml(
					String.format("<font color='#ffaa17'>%.1f万人</font>在玩",
							(float) o.playCount / 10000)));
		} else {
			bgaViewHolderHelper.setText(R.id.tv_play, Html.fromHtml(
					String.format("<font color='#ffaa17'>%d人</font>在玩",
							o.playCount)));
		}
		bgaViewHolderHelper.setText(R.id.tv_size, o.size);
		bgaViewHolderHelper.setText(R.id.tv_count, Html.fromHtml(
				String.format("共<font color='#ffaa17'>%d</font>款礼包", o.totalCount)));
		bgaViewHolderHelper.setText(R.id.tv_remain, Html.fromHtml(
				String.format("今日新增<font color='#ffaa17'>%d</font>款", o.newCount)));
		ImageLoader.getInstance().displayImage(o.img, (ImageView)bgaViewHolderHelper.getView(R.id.iv_icon));
		if (AssistantApp.getInstance().isAllowDownload()) {
			bgaViewHolderHelper.getView(R.id.rl_recommend).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					IntentUtil.jumpGameDetail(mContext, o.id, GameTypeUtil.JUMP_STATUS_GIFT);
				}
			});
		}
	}
}
