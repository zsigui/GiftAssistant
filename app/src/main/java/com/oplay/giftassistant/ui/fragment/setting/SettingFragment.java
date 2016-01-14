package com.oplay.giftassistant.ui.fragment.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.utils.StorageUtils;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.manager.AccountManager;
import com.oplay.giftassistant.manager.ObserverManager;
import com.oplay.giftassistant.model.DecryptDataModel;
import com.oplay.giftassistant.model.json.base.JsonReqBase;
import com.oplay.giftassistant.ui.activity.base.BaseAppCompatActivity;
import com.oplay.giftassistant.ui.fragment.base.BaseFragment_WithName;
import com.oplay.giftassistant.ui.fragment.dialog.ConfirmDialog;
import com.oplay.giftassistant.ui.widget.ToggleButton;
import com.oplay.giftassistant.util.DataClearUtil;

import net.youmi.android.libs.common.util.Util_System_File;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 16-1-5.
 */
public class SettingFragment extends BaseFragment_WithName implements View.OnClickListener {

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
		ObserverManager.getInstance().addUserUpdateListener(this);
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
		//setTitleBar(R.string.st_setting_title);
		if (mApp.isShouldPushMsg())
			mBtnPush.toggleOn();
		else
			mBtnPush.toggleOff();
		if (mApp.isShouldAutoCheckUpdate())
			mBtnAutoCheckUpdate.toggleOn();
		else
			mBtnAutoCheckUpdate.toggleOff();
		if (mApp.isShouldAutoDeleteApk())
			mBtnAutoDelete.toggleOn();
		else
			mBtnAutoDelete.toggleOff();
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
		super.onClick(v);
		switch (v.getId()) {
			case R.id.tb_push:
				Global.THREAD_POOL.execute(new Runnable() {
					@Override
					public void run() {
						if (mApp.isShouldPushMsg()) {
							mApp.setShouldPushMsg(false);
						} else {
							mApp.setShouldPushMsg(true);
						}
					}
				});
				mBtnPush.toggle();
				break;
			case R.id.tb_auto_delete:
				Global.THREAD_POOL.execute(new Runnable() {
					@Override
					public void run() {
						if (mApp.isShouldAutoDeleteApk()) {
							mApp.setShouldAutoDeleteApk(false);
						} else {
							mApp.setShouldAutoDeleteApk(true);
						}
					}
				});
				mBtnAutoDelete.toggle();
				break;
			case R.id.tb_auto_check_update:
				Global.THREAD_POOL.execute(new Runnable() {
					@Override
					public void run() {
						if (mApp.isShouldAutoCheckUpdate()) {
							mApp.setShouldAutoCheckUpdate(false);
						} else {
							mApp.setShouldAutoCheckUpdate(true);
						}
					}
				});
				mBtnAutoCheckUpdate.toggle();
				break;
			case R.id.rl_clear:
				final ConfirmDialog dialog = ConfirmDialog.newInstance();
				dialog.setContent(getResources().getString(R.string.st_content_clear_cache));
				dialog.setListener(new ConfirmDialog.OnDialogClickListener() {
					@Override
					public void onCancel() {
						dialog.dismiss();
					}

					@Override
					public void onConfirm() {
						((BaseAppCompatActivity) getActivity()).showLoadingDialog();
						new Thread(new Runnable() {
							@Override
							public void run() {
								Util_System_File.delete(StorageUtils.getOwnCacheDirectory(getContext(),
										Global.EXTERNAL_CACHE));
								DataClearUtil.cleanExternalCache(getContext());
								DataClearUtil.cleanInternalCache(getContext());
								((BaseAppCompatActivity) getActivity()).hideLoadingDialog();
								((BaseAppCompatActivity) getActivity()).showSuccessHint("清除缓存成功");
							}
						}).start();
						dialog.dismiss();
					}
				});
				dialog.show(getChildFragmentManager(), ConfirmDialog.class.getSimpleName());
				break;
			case R.id.rl_feedback:
				((BaseAppCompatActivity) getActivity()).replaceFragWithTitle(R.id.fl_container, FeedBackFragment
						.newInstance(), getResources().getString(R.string.st_feedback_title));
				break;
			case R.id.rl_about:
				((BaseAppCompatActivity) getActivity()).replaceFragWithTitle(R.id.fl_container, AboutFragment
						.newInstance(), getResources().getString(R.string.st_about_title));
				break;
			case R.id.rl_logout:
				// 调用登出接口，但不关心结果
				Global.THREAD_POOL.execute(new Runnable() {
					@Override
					public void run() {
						Global.getNetEngine().logout(new JsonReqBase<Object>()).enqueue(new Callback<Void>() {
							@Override
							public void onResponse(Response<Void> response, Retrofit retrofit) {
							}

							@Override
							public void onFailure(Throwable t) {
							}
						});
					}
				});
				AccountManager.getInstance().setUser(null);
				break;
		}
	}

	@Override
	public void onUserUpdate() {
		updateData();
	}


}