package com.oplay.giftassistant.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.AssistantApp;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.GameTypeUtil;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.model.data.resp.IndexGiftLike;
import com.oplay.giftassistant.util.IntentUtil;

import java.util.List;

import cn.bingoogolapple.androidcommon.adapter.BGARecyclerViewAdapter;
import cn.bingoogolapple.androidcommon.adapter.BGAViewHolderHelper;

/**
 * Created by zsigui on 15-12-24.
 */
public class IndexGiftLikeAdapter extends BGARecyclerViewAdapter<IndexGiftLike> {

	public IndexGiftLikeAdapter(RecyclerView recyclerView) {
		super(recyclerView, R.layout.item_index_gift_like);
	}

	public IndexGiftLikeAdapter(RecyclerView recyclerView, List<IndexGiftLike> data) {
		super(recyclerView, R.layout.item_index_gift_like);
		this.mDatas = data;
	}


	@Override
	protected void fillData(BGAViewHolderHelper bgaViewHolderHelper, int i, final IndexGiftLike o) {
		bgaViewHolderHelper.setText(R.id.tv_game_name, o.name);
		bgaViewHolderHelper.setText(R.id.tv_remain, String.valueOf(o.newCount));
		bgaViewHolderHelper.setText(R.id.tv_count, String.format("%d款礼包", o.totalCount));
		// n款礼包
		ImageLoader.getInstance().displayImage(o.img, bgaViewHolderHelper.<ImageView>getView(R.id.iv_icon),
				Global.IMAGE_OPTIONS);
        if (AssistantApp.getInstance().isAllowDownload()) {
            bgaViewHolderHelper.getView(R.id.rl_recommend).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 去到游戏
	                IntentUtil.jumpGameDetail(mContext, o.id, GameTypeUtil.JUMP_STATUS_GIFT);
                }
            });
        }
	}

	public void updateData(List<IndexGiftLike> data) {
		this.mDatas = data;
		notifyDataSetChanged();
	}

}
