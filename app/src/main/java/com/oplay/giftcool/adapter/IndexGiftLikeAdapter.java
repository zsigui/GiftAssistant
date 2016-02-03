package com.oplay.giftcool.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.GameTypeUtil;
import com.oplay.giftcool.model.data.resp.IndexGiftLike;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ViewUtil;

import java.util.List;

import cn.bingoogolapple.androidcommon.adapter.BGARecyclerViewAdapter;
import cn.bingoogolapple.androidcommon.adapter.BGAViewHolderHelper;

/**
 * Created by zsigui on 15-12-24.
 */
public class IndexGiftLikeAdapter extends BGARecyclerViewAdapter<IndexGiftLike>{

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
		if (TextUtils.isEmpty(o.giftName)) {
			bgaViewHolderHelper.setText(R.id.tv_gift, "暂无新礼包");
		} else {
			bgaViewHolderHelper.setText(R.id.tv_gift, o.giftName);
		}
		bgaViewHolderHelper.setText(R.id.tv_count, String.format("%d款礼包", o.totalCount));
		// n款礼包
		ViewUtil.showImage(bgaViewHolderHelper.<ImageView>getView(R.id.iv_icon), o.img);
        bgaViewHolderHelper.getView(R.id.rl_recommend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 去到游戏
                IntentUtil.jumpGameDetail(mContext, o.id, GameTypeUtil.JUMP_STATUS_GIFT);
            }
        });
	}

	public void updateData(List<IndexGiftLike> data) {
		this.mDatas = data;
		notifyDataSetChanged();
	}

}
