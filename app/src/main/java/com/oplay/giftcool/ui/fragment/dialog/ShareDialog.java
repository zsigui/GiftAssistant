package com.oplay.giftcool.ui.fragment.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.base.BaseRVAdapter;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog_NoButton;

/**
 * Created by zsigui on 16-1-22.
 */
public class ShareDialog extends BaseFragment_Dialog_NoButton {

	private final static String ARGS_TITLE = "title";
	private String mTitle;
	private BaseRVAdapter mAdapter;

	public static ShareDialog newInstance(String title) {
		ShareDialog dialog = new ShareDialog();
		Bundle args = new Bundle();
		args.putString(ARGS_TITLE, title);
		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Bundle args = getArguments();
		if (args != null) {
			mTitle = args.getString(ARGS_TITLE);
		}
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void bindViewWithData(View contentView, @Nullable Bundle savedInstanceState) {
		final RecyclerView recyclerView = (RecyclerView) contentView.findViewById(R.id.rv_dialog_list);
		RecyclerView.LayoutManager gridLayoutManager = new GridLayoutManager(
				getContext(), 2, GridLayoutManager.VERTICAL, false);
		recyclerView.setLayoutManager(gridLayoutManager);
		recyclerView.setHasFixedSize(true);
		recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
		recyclerView.setAdapter(mAdapter);
	}

	@Override
	protected String getTitle() {
		return mTitle;
	}

	@Override
	protected View getContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return LayoutInflater.from(getContext()).inflate(R.layout.dialog_rv_share_content, container, false);
	}

	public void setAdapter(BaseRVAdapter adapter) {
		mAdapter = adapter;
	}
}
