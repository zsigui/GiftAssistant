package com.oplay.giftcool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.ui.widget.button.TagButton;
import com.oplay.giftcool.ui.widget.layout.flowlayout.FlowLayout;
import com.oplay.giftcool.ui.widget.layout.flowlayout.TagAdapter;

import java.util.List;

/**
 * Created by zsigui on 16-5-5.
 */
public class GameDetailTagAdapter extends TagAdapter<String> {

	private Context mContext;

	public GameDetailTagAdapter(Context context, List<String> datas) {
		super(datas);
		mContext = (context == null ?
				AssistantApp.getInstance().getApplicationContext() : context.getApplicationContext());
	}

	@Override
	public View getView(FlowLayout parent, int position, String s) {
		View v = LayoutInflater.from(mContext).inflate(R.layout.item_game_detail_info_tag, parent, false);
		TagButton tagButton = (TagButton) v.findViewById(R.id.tv_tag);
		int tagState = TagButton.STATE_BLUE;
		switch (position % 3) {
			case 0:
				tagState = TagButton.STATE_BLUE;
				break;
			case 1:
				tagState = TagButton.STATE_PURPLE;
				break;
			case 2:
				tagState = TagButton.STATE_ORANGE;
				break;
		}
		tagButton.setState(tagState);
		tagButton.setText(s);
		return v;
	}

	public void updateData(List<String> data) {
		setData(data);
		notifyDataChanged();
	}
}
