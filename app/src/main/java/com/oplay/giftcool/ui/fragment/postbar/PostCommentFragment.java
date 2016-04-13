package com.oplay.giftcool.ui.fragment.postbar;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.listener.ShowBottomBarListener;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_WebView;

/**
 * Created by zsigui on 16-4-13.
 */
public class PostCommentFragment extends BaseFragment_WebView implements ShowBottomBarListener, View.OnTouchListener {

	private LinearLayout llBottom;
	private EditText etContent;
	private TextView tvSend;

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_post_reply_comment);
		llBottom = getViewById(R.id.ll_bottom);
	}

	@Override
	protected void setListener() {
		etContent.setOnTouchListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {

	}

	@Override
	protected void lazyLoad() {

	}

	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public void showBar(boolean isShow, Object param) {

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return false;
	}
}
