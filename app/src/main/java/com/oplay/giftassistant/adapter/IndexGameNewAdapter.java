package com.oplay.giftassistant.adapter;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;
import com.oplay.giftassistant.util.ToastUtil;

import java.util.ArrayList;

import cn.bingoogolapple.androidcommon.adapter.BGAAdapterViewAdapter;
import cn.bingoogolapple.androidcommon.adapter.BGAViewHolderHelper;

/**
 * Created by zsigui on 15-12-28.
 */
public class IndexGameNewAdapter extends BGAAdapterViewAdapter<IndexGameNew> {

	public IndexGameNewAdapter(Context context) {
		super(context, R.layout.item_index_game_new);
	}

	@Override
	protected void fillData(BGAViewHolderHelper bgaViewHolderHelper, int i, final IndexGameNew o) {
		bgaViewHolderHelper.setText(R.id.tv_name, o.name);
		if (o.playCount < 10000) {
			bgaViewHolderHelper.setText(R.id.tv_play,
					Html.fromHtml(String.format("<font color='#ffaa17'>%d人</font>在玩", o.playCount)));
		} else {
			bgaViewHolderHelper.setText(R.id.tv_play,
					Html.fromHtml(String.format("<font color='#ffaa17'>%.1f万人</font>在玩",
							(float) o.playCount / 10000)));
		}
		if (o.newCount > 0) {
			bgaViewHolderHelper.setVisibility(R.id.iv_gift, View.VISIBLE);
		}else {
			bgaViewHolderHelper.setVisibility(R.id.iv_gift, View.GONE);
		}
		bgaViewHolderHelper.setText(R.id.tv_size, o.size);
		if (o.totalCount > 0) {
			bgaViewHolderHelper.setText(R.id.tv_gift,
					Html.fromHtml(String.format("<font color='#ffaa17'>%s</font> 等共<font color='#ffaa17'>%d</font>款礼包",
							o.giftName, o.totalCount)));
		} else {
			bgaViewHolderHelper.setText(R.id.tv_gift, "暂时还木有礼包");
		}
		// n款礼包
		ImageLoader.getInstance().displayImage(o.img, bgaViewHolderHelper.<ImageView>getView(R.id.iv_icon),
				Global.IMAGE_OPTIONS);
		bgaViewHolderHelper.getView(R.id.rl_recommend).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastUtil.showShort("跳转游戏 " + o.name + " 详情页面");
			}
		});
		bgaViewHolderHelper.getView(R.id.tv_download).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastUtil.showShort("游戏 " + o.name + " 开始下载");
			}
		});
	}

	public void updateData(ArrayList<IndexGameNew> games) {
		this.mDatas = games;
		notifyDataSetChanged();
	}
}
