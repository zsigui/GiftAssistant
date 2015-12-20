package com.oplay.giftassistant.adapter;

import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.model.UserModel;

import cn.bingoogolapple.androidcommon.adapter.BGARecyclerViewAdapter;
import cn.bingoogolapple.androidcommon.adapter.BGAViewHolderHelper;

/**
 * Created by zsigui on 15-12-16.
 */
public class NormalRecyclerViewAdapter extends BGARecyclerViewAdapter<UserModel>{


	public NormalRecyclerViewAdapter(RecyclerView recyclerView) {
		super(recyclerView, R.layout.item_user);
	}

	@Override
	protected void fillData(BGAViewHolderHelper bgaViewHolderHelper, int i, UserModel userModel) {
		bgaViewHolderHelper.setText(R.id.tvName, userModel.name)
				.setText(R.id.tvPwd, userModel.pwd);
		Glide.with(mContext).load(userModel.img).into(bgaViewHolderHelper.<ImageView>getView(R.id.ivPic));

	}
}
