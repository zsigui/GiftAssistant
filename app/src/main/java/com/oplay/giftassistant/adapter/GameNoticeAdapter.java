package com.oplay.giftassistant.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
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
public class GameNoticeAdapter extends BGARecyclerViewAdapter<IndexGameNew> {

	public GameNoticeAdapter(RecyclerView recyclerView) {
		super(recyclerView, R.layout.item_index_game_notice);
	}

	public GameNoticeAdapter(RecyclerView recyclerView, ArrayList<IndexGameNew> data) {
		this(recyclerView);
		this.mDatas = data;
	}

	@Override
	protected void fillData(BGAViewHolderHelper bgaViewHolderHelper, int i, final IndexGameNew o) {
		bgaViewHolderHelper.setText(R.id.tv_name, o.name);
		String tagUrl = "drawable://";
		switch (i) {
			case 0:
				tagUrl += R.drawable.ic_tag_first;
				break;
			case 1:
				tagUrl += R.drawable.ic_tag_second;
				break;
			case 2:
				tagUrl += R.drawable.ic_tag_third;
				break;
			default:
				tagUrl += R.drawable.ic_tag_other;
		}
		ImageLoader.getInstance().displayImage("drawable://" + R.drawable.ic_tag_first,
				bgaViewHolderHelper.<ImageView>getView(R.id.iv_tag),
				Global.IMAGE_OPTIONS);
		if (o.playCount < 10000) {
			bgaViewHolderHelper.setText(R.id.tv_play,
					Html.fromHtml(String.format("<font color='#ffaa17'>%d人</font>在玩", o.playCount)));
		} else {
			bgaViewHolderHelper.setText(R.id.tv_play,
					Html.fromHtml(String.format("<font color='#ffaa17'>%.1f</font>在玩",
							(float) o.playCount / 10000)));
		}
		if (o.newCount > 0) {
			bgaViewHolderHelper.setVisibility(R.id.iv_gift, View.VISIBLE);
		} else {
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

    public void updateData(ArrayList<IndexGameNew> data) {
        this.mDatas = data;
        notifyDataSetChanged();
    }
}
