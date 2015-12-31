package com.oplay.giftassistant.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;
import com.oplay.giftassistant.util.ToastUtil;

import java.util.ArrayList;

import cn.bingoogolapple.androidcommon.adapter.BGARecyclerViewAdapter;
import cn.bingoogolapple.androidcommon.adapter.BGAViewHolderHelper;

/**
 * Created by zsigui on 15-12-31.
 */
public class IndexGameHotAdapter extends BGARecyclerViewAdapter<IndexGameNew> {


	public IndexGameHotAdapter(RecyclerView recyclerView) {
		super(recyclerView, R.layout.item_grid_game_super);
	}

	public IndexGameHotAdapter(RecyclerView recyclerView, ArrayList<IndexGameNew> data) {
		this(recyclerView);
		this.mDatas = data;
	}

	@Override
	protected void fillData(BGAViewHolderHelper bgaViewHolderHelper, int i, final IndexGameNew o) {
		bgaViewHolderHelper.setText(R.id.tv_name, o.name);
		if (o.newCount > 0) {
			bgaViewHolderHelper.setVisibility(R.id.iv_gift, View.VISIBLE);
		} else {
			bgaViewHolderHelper.setVisibility(R.id.iv_gift, View.GONE);
		}
		ImageLoader.getInstance().displayImage(o.img, (ImageView) bgaViewHolderHelper.getView(R.id.iv_icon),
				Global.IMAGE_OPTIONS);
		bgaViewHolderHelper.getView(R.id.rl_recommend).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastUtil.showShort(o.name + " 游戏详情页跳转");
			}
		});
		bgaViewHolderHelper.getView(R.id.tv_download).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastUtil.showShort(o.name + " 开始下载");
			}
		});
	}

	public void updateData(ArrayList<IndexGameNew> data) {
		this.mDatas = data;
		notifyDataSetChanged();
	}
}
