package com.oplay.giftassistant.adapter;

import android.content.Context;
import android.text.Html;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;

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
	protected void fillData(BGAViewHolderHelper bgaViewHolderHelper, int i, IndexGameNew indexGameNew) {
		bgaViewHolderHelper.setText(R.id.tv_name, indexGameNew.name);
		if (indexGameNew.playCount < 10000) {
			bgaViewHolderHelper.setText(R.id.tv_play,
					Html.fromHtml(String.format("<font color='#ffaa17'>%d人</font>在玩",
							indexGameNew.playCount)));
		} else {
			bgaViewHolderHelper.setText(R.id.tv_play,
					Html.fromHtml(String.format("<font color='#ffaa17'>%.1f</font>在玩",
							(float) indexGameNew.playCount / 10000)));
		}
		bgaViewHolderHelper.setText(R.id.tv_size, indexGameNew.size + "M");
		bgaViewHolderHelper.setText(R.id.tv_gift,
				Html.fromHtml(String.format("<font color='#ffaa17'>%s</font> 等共<font color='#ffaa17'>%d</font>款礼包",
						indexGameNew.giftName, indexGameNew.hasGiftCount)));
		// n款礼包
		ImageLoader.getInstance().displayImage(indexGameNew.img, bgaViewHolderHelper.<ImageView>getView(R.id.iv_icon),
				Global.IMAGE_OPTIONS);
	}

	public void updateData(ArrayList<IndexGameNew> games) {
		this.mDatas = games;
		notifyDataSetChanged();
	}
}
