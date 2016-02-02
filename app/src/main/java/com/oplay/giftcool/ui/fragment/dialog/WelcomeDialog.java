package com.oplay.giftcool.ui.fragment.dialog;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;

/**
 * Created by zsigui on 16-1-22.
 */
public class WelcomeDialog extends BaseFragment_Dialog {

	private TextView tvScore;
	private int mScore;

	public static WelcomeDialog newInstance(@LayoutRes int resId) {
		WelcomeDialog dialog = new WelcomeDialog();
		dialog.setContentView(resId);
		return dialog;
	}

	@Override
	protected void initView() {
		tvScore = getViewById(R.id.tv_score);
	}

	@Override
	protected void processLogic() {
		setScore(mScore);
	}

	@Override
	protected void setContentView(@LayoutRes int layoutId) {
		mContentView = LayoutInflater.from(AssistantApp.getInstance().getApplicationContext())
				.inflate(layoutId, null);
	}

	public void setScore(int score) {
		mScore = score;
		if (tvScore != null) {
			tvScore.setText(String.valueOf(score));
		}
	}

}
