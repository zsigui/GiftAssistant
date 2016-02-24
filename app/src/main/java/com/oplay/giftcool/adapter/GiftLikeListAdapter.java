package com.oplay.giftcool.adapter;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.GameTypeUtil;
import com.oplay.giftcool.model.data.resp.IndexGiftLike;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ViewUtil;
import com.socks.library.KLog;

import java.util.ArrayList;

import cn.bingoogolapple.androidcommon.adapter.BGAAdapterViewAdapter;
import cn.bingoogolapple.androidcommon.adapter.BGAViewHolderHelper;

/**
 * Created by zsigui on 15-12-30.
 */
public class GiftLikeListAdapter extends BGAAdapterViewAdapter<IndexGiftLike> {


	public GiftLikeListAdapter(Context context) {
		super(context.getApplicationContext(), R.layout.item_list_gift_like);
	}

	@Override
	protected void fillData(BGAViewHolderHelper bgaViewHolderHelper, int i, final IndexGiftLike o) {
		bgaViewHolderHelper.setText(R.id.tv_name, o.name);
		if (o.playCount > 10000) {
			bgaViewHolderHelper.setText(R.id.tv_content, Html.fromHtml(
					String.format("<font color='#ffaa17'>%.1f万人</font>在玩",
							(float) o.playCount / 10000)));
		} else {
			bgaViewHolderHelper.setText(R.id.tv_content, Html.fromHtml(
					String.format("<font color='#ffaa17'>%d人</font>在玩",
							o.playCount)));
		}
		bgaViewHolderHelper.setText(R.id.tv_size, o.size);
		bgaViewHolderHelper.setText(R.id.tv_count, Html.fromHtml(
				String.format("共<font color='#ffaa17'>%d</font>款礼包", o.totalCount)));
		bgaViewHolderHelper.setText(R.id.tv_remain, Html.fromHtml(
				String.format("今日新增<font color='#ffaa17'>%d</font>款", o.newCount)));
		ViewUtil.showImage((ImageView) bgaViewHolderHelper.getView(R.id.iv_icon), o.img);
		if (AssistantApp.getInstance().isAllowDownload()) {
			bgaViewHolderHelper.getView(R.id.rl_recommend).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					KLog.e("test-test", "jumpGameDetail");
					IntentUtil.jumpGameDetail(mContext, o.id, GameTypeUtil.JUMP_STATUS_GIFT);
				}
			});
		}
	}

	public void updateData(ArrayList<IndexGiftLike> data) {
		if (data == null) {
			return;
		}
		mDatas = data;
		notifyDataSetChanged();
	}
}
