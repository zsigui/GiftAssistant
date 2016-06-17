package com.oplay.giftcool.ui.fragment.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.adapter.base.BaseRVHolder;
import com.oplay.giftcool.listener.OnItemClickListener;

/**
 * Created by zsigui on 16-6-17.
 */
public class ChoiceDialog extends DialogFragment {
    protected View mContentView;
    private SimpleListAdapter mAdapter;
    private OnItemClickListener<String> mListener;

    public void setAdapter(SimpleListAdapter adapter) {
        mAdapter = adapter;
    }

    public void setListener(OnItemClickListener<String> listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getContext(), R.style.DefaultCustomDialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        return dialog;
    }

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = getContentView(inflater, container, savedInstanceState);
        return mContentView;
    }

    @Override
    public final void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViewWithData(view, savedInstanceState);
    }

    private void bindViewWithData(View contentView, Bundle savedInstanceState) {
        final RecyclerView recyclerView = (RecyclerView) contentView.findViewById(R.id.rv_dialog_list);
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setListener(mListener);
    }

    private View getContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_rv_share_content, container, false);
    }


    static class SimpleListAdapter extends BaseRVAdapter<String> implements View.OnClickListener {

        public SimpleListAdapter(Context context) {
            super(context);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(mContext).inflate(R.layout.item_simple_list,  parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Holder h = (Holder) holder;
            h.tv.setText(getItem(position));
            h.tv.setOnClickListener(this);
            h.tv.setTag(TAG_POSITION, position);
        }

        @Override
        public void onClick(View v) {
            if (v.getTag(TAG_POSITION) == null) {
                return;
            }
            if (mListener != null) {
                int pos = (Integer) v.getTag(TAG_POSITION);
                mListener.onItemClick(getItem(pos), v, pos);
            }
        }

    }

    static class Holder extends BaseRVHolder {

        private TextView tv;

        public Holder(View itemView) {
            super(itemView);
            tv = getViewById(R.id.tv_content);
        }
    }
}
