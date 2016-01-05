package com.oplay.giftassistant.ui.fragment.drawer;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.manager.AccountManager;
import com.oplay.giftassistant.manager.ObserverManager;
import com.oplay.giftassistant.model.DecryptDataModel;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment;
import com.oplay.giftassistant.ui.widget.ToggleButton;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 16-1-5.
 */
public class SettingFragment extends BaseFragment implements View.OnClickListener {

	private ToggleButton mBtnPush;
	private ToggleButton mBtnAutoDelete;
	private ToggleButton mBtnAutoCheckUpdate;
	private TextView mVer;
	private RelativeLayout mClearCache;
	private RelativeLayout mFeedback;
	private RelativeLayout mAbout;
	private RelativeLayout mLogout;


	public static SettingFragment newInstance() {
		return new SettingFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_setting);
		mBtnPush = getViewById(R.id.tb_push);
		mBtnAutoDelete = getViewById(R.id.tb_auto_delete);
		mBtnAutoCheckUpdate = getViewById(R.id.tb_auto_check_update);
		mVer = getViewById(R.id.tv_version);
		mClearCache = getViewById(R.id.rl_clear);
		mFeedback = getViewById(R.id.rl_feedback);
		mAbout = getViewById(R.id.rl_about);
		mLogout = getViewById(R.id.rl_logout);
	}

	@Override
	protected void setListener() {
		mBtnPush.setOnClickListener(this);
		mBtnAutoDelete.setOnClickListener(this);
		mBtnAutoCheckUpdate.setOnClickListener(this);
		mClearCache.setOnClickListener(this);
		mFeedback.setOnClickListener(this);
		mAbout.setOnClickListener(this);
		mLogout.setOnClickListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		ObserverManager.getInstance().addUserUpdateListener(this);
		if(mApp.isShouldPushMsg())
			mBtnPush.toggleOn();
		else
			mBtnPush.toggleOff();
		if (mApp.isShouldAutoCheckUpdate())
			mBtnAutoCheckUpdate.toggleOn();
		else
			mBtnAutoCheckUpdate.toggleOff();
		if (mApp.isShouldAutoCheckUpdate())
			mBtnAutoCheckUpdate.toggleOn();
		else
			mBtnAutoCheckUpdate.toggleOff();
		mVer.setText(DecryptDataModel.SDK_VER_NAME);
		updateData();
	}

	@Override
	protected void lazyLoad() {

	}



	private void updateData() {
		if (AccountManager.getInstance().isLogin()) {
			mLogout.setVisibility(View.VISIBLE);
		} else {
			mLogout.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.tb_push:
				if (!mApp.isShouldPushMsg()) {
					mApp.setShouldPushMsg(true);
				} else {
					mApp.setShouldPushMsg(false);
				}
				mBtnPush.toggle();
				break;
			case R.id.tb_auto_delete:
				if (!mApp.isShouldAutoDeleteApk()) {
					mApp.setShouldAutoDeleteApk(true);
				} else {
					mApp.setShouldAutoDeleteApk(false);
				}
				mBtnAutoDelete.toggle();
				break;
			case R.id.tb_auto_check_update:
				if (mApp.isShouldAutoCheckUpdate()) {
					mApp.setShouldAutoCheckUpdate(false);
				} else {
					mApp.setShouldAutoCheckUpdate(true);
				}
				break;
			case R.id.rl_clear:

				break;
			case R.id.rl_feedback:
				break;
			case R.id.rl_about:
				break;
			case R.id.rl_logout:
				// 调用登出接口，但不关心结果
				new Thread(new Runnable() {
					@Override
					public void run() {
						Global.getNetEngine().logout(new JsonReqBase<Object>()).enqueue(new Callback<Void>() {
							@Override
							public void onResponse(Response<Void> response, Retrofit retrofit) {}

							@Override
							public void onFailure(Throwable t) {}
						});
					}
				}).start();
				AccountManager.getInstance().setUser(null);
				break;
		}
	}

	@Override
	public void onUserUpdate() {
		updateData();
	}
}
