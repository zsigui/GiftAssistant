package com.oplay.giftcool.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.config.util.IndexTypeUtil;
import com.oplay.giftcool.model.data.resp.GameTypeMain;
import com.oplay.giftcool.ui.widget.button.TagButton;
import com.oplay.giftcool.util.IntentUtil;

import java.util.ArrayList;

/**
 * Created by zsigui on 16-1-10.
 */
public class GameTagAdapter extends BaseRVAdapter<GameTypeMain> implements View.OnClickListener {


    private static final int TAG_POSITION = 0xFFFF1444;

    private ArrayList<GameTypeMain> mHeaderData = new ArrayList<>(IndexTypeUtil.ITEM_GAME_TYPE_HEADER_COUNT);

    public GameTagAdapter(Context context) {
        super(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case IndexTypeUtil.ITEM_HEADER:
                final TypeHolder typeHolder = new TypeHolder(LayoutInflater.from(mContext).inflate(R.layout
                        .view_recycle_view, parent, false));
                typeHolder.mAdapter = new GameTypeMainAdapter(mContext);
                initHeaderData();
                GridLayoutManager glm = new GridLayoutManager(mContext, IndexTypeUtil
                        .ITEM_GAME_TYPE_GRID_COUNT);
                ViewGroup.LayoutParams lp = typeHolder.rvData.getLayoutParams();
                lp.height = mContext.getResources().getDimensionPixelSize(R.dimen.di_game_tag_header_height);
                typeHolder.rvData.setLayoutParams(lp);
                typeHolder.rvData.setLayoutManager(glm);
                typeHolder.rvData.setAdapter(typeHolder.mAdapter);
                return typeHolder;
            default:
                return new TagHolder(LayoutInflater.from(mContext).inflate(R.layout.item_grid_game_type_tag, parent,
                        false));
        }
    }

    @Override
    public void updateData(ArrayList<GameTypeMain> data) {
        mData = data;
        initHeaderData();
        notifyDataSetChanged();
    }

    private void initHeaderData() {
        final int count = (getItemCount() > IndexTypeUtil.ITEM_GAME_TYPE_HEADER_COUNT ?
                IndexTypeUtil.ITEM_GAME_TYPE_HEADER_COUNT : getItemCount());
        mHeaderData.clear();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                mHeaderData.add(mData.get(i));
            }
        }
    }

    /**
     * 获取头部视图的数量，此处为1
     */
    public int getHeaderCount() {
        return 1;
    }

    /**
     * 获取总的列表项
     */
    @Override
    public int getItemCount() {
        final int count = super.getItemCount();
        return count > IndexTypeUtil.ITEM_GAME_TYPE_HEADER_COUNT ?
                count - IndexTypeUtil.ITEM_GAME_TYPE_HEADER_COUNT + getHeaderCount()
                : (count == 0 ? 0 : getHeaderCount());
    }

    @Override
    public int getItemViewType(int position) {
        if (position < getHeaderCount()) {
            return IndexTypeUtil.ITEM_HEADER;
        }
        return IndexTypeUtil.ITEM_NORMAL;
    }

    @Override
    public GameTypeMain getItem(int position) {
        return position < getHeaderCount() ? null :
                mData.get(position + IndexTypeUtil.ITEM_GAME_TYPE_HEADER_COUNT - getHeaderCount());
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case IndexTypeUtil.ITEM_HEADER:
                final TypeHolder typeHolder = (TypeHolder) holder;
                typeHolder.mAdapter.updateData(mHeaderData);
                break;
            default:
                final TagHolder tagHolder = (TagHolder) holder;
                GameTypeMain o = getItem(position);
                if (o != null) {
                    tagHolder.tvTag.setText(o.name);
                    tagHolder.tvTag.setState(getState(position - getHeaderCount()));
                    tagHolder.itemView.setOnClickListener(this);
                    tagHolder.itemView.setTag(TAG_POSITION, position);
                }
        }

    }

    int colorInt = 0;
    int colorCount = 5;

    /**
     * 获取要显示的背景状态
     */
    private int getState(int pos) {
        int state = TagButton.STATE_NONE;
        int k = (pos >= 15 ? pos % 15 : pos);
        boolean isOdd = (pos / 15 % 2 == 1);
        switch (k) {
            case 1:
                state = isOdd ? TagButton.STATE_ORANGE : TagButton.STATE_RED;
                break;
            case 3:
                state = isOdd ? TagButton.STATE_BLUE : TagButton.STATE_ORANGE;
                break;
            case 8:
                state = isOdd ? TagButton.STATE_LIGHT_GREEN : TagButton.STATE_BLUE;
                break;
            case 9:
                state = isOdd ? TagButton.STATE_RED : TagButton.STATE_PURPLE;
                break;
            case 13:
                state = isOdd ? TagButton.STATE_PURPLE : TagButton.STATE_LIGHT_GREEN;
                break;
        }
        return state;
    }

    @Override
    public void onClick(View v) {
        if (mData == null || v.getTag(TAG_POSITION) == null) {
            return;
        }
        GameTypeMain o = getItem((Integer) v.getTag(TAG_POSITION));
        IntentUtil.jumpGameTagList(mContext, o.id, o.name);
    }

    static class TagHolder extends BaseRVHolder {
        TagButton tvTag;

        public TagHolder(View itemView) {
            super(itemView);
            tvTag = getViewById(R.id.tv_tag);
        }
    }

    static class TypeHolder extends BaseRVHolder {
        GameTypeMainAdapter mAdapter;
        RecyclerView rvData;

        public TypeHolder(View itemView) {
            super(itemView);
            rvData = getViewById(R.id.lv_content);
        }
    }
}
