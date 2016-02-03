package com.oplay.giftcool.ui.fragment.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.BannerTypeUtil;
import com.oplay.giftcool.model.data.resp.IndexBanner;
import com.oplay.giftcool.util.ViewUtil;
import com.tendcloud.tenddata.TCAgent;

/**
 * Created by zsigui on 16-2-1.
 */
public class AllViewDialog extends DialogFragment implements View.OnClickListener {

	private View mContentView;
	private static final String KEY_BANNER = "key_data_banner";
	private IndexBanner mData;

	public static AllViewDialog newInstance(IndexBanner banner) {
		AllViewDialog dialog = new AllViewDialog();
		Bundle b = new Bundle();
		b.putSerializable(KEY_BANNER, banner);
		dialog.setArguments(b);
		return dialog;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mContentView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_banner, null);
		if (getArguments() != null && getArguments().getSerializable(KEY_BANNER) != null) {
			mData = (IndexBanner) getArguments().getSerializable(KEY_BANNER);
		}
		if (mData != null) {
			ViewUtil.showImage((ImageView) mContentView, mData.url);
			mContentView.setOnClickListener(this);
		}
		return new AlertDialog.Builder(getActivity(), R.style.DefaultCustomDialog)
				.setView(mContentView)
				.create();
	}

	@Override
	public void onClick(View v) {
		TCAgent.onEvent(getActivity(), "每日活动弹窗", "参与活动");
		BannerTypeUtil.handleBanner(getActivity(), mData);
		dismissAllowingStateLoss();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		TCAgent.onEvent(getActivity(), "每日活动弹窗", "取消");
		super.onCancel(dialog);
	}
}
