package com.oplay.giftassistant.ui.fragment.setting;

import android.os.Bundle;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_FullScreen;
import com.oplay.giftassistant.ui.widget.ScoreText;

/**
 * Created by zsigui on 16-1-6.
 */
public class TaskFragment extends BaseFragment_FullScreen {

	private ScoreText tvSetNick;
	private ScoreText tvSetAvator;

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_score_task);
		tvSetAvator = getViewById(R.id.tv_set_avator);
		tvSetNick = getViewById(R.id.tv_set_nick);
		tvSetAvator.setStateEnable(true);
		tvSetNick.setStateEnable(false);
	}

	@Override
	protected void setListener() {

	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		setTitleBar(R.string.st_task_title);
	}

	@Override
	protected void lazyLoad() {

	}

	public static TaskFragment newInstance() {
		return new TaskFragment();
	}
}
