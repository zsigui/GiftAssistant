package com.oplay.giftcool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.ui.widget.layout.flowlayout.FlowLayout;
import com.oplay.giftcool.ui.widget.layout.flowlayout.TagAdapter;

import java.util.List;

/**
 * 显示搜索历史记录
 *
 * Created by zsigui on 16-3-4.
 */
public class SearchHistoryAdapter extends TagAdapter<String> {

    private Context mContext;

    public SearchHistoryAdapter(Context context, List<String> data) {
        super(data);
        mContext = (context == null ?
                AssistantApp.getInstance().getApplicationContext() : context.getApplicationContext());
    }

    @Override
    public View getView(FlowLayout parent, int position, String s) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_search_history, parent, false);
        ((TextView)v.findViewById(R.id.tv_content)).setText(s);
        return v;
    }

    public void updateData(List<String> data) {
        setData(data);
        notifyDataChanged();
    }
}
