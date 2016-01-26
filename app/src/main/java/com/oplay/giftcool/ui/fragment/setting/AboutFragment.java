package com.oplay.giftcool.ui.fragment.setting;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.StatusCode;
import com.oplay.giftcool.model.DecryptDataModel;
import com.oplay.giftcool.model.data.req.ReqInitApp;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.model.data.resp.UpdateInfo;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Dialog;
import com.oplay.giftcool.ui.fragment.dialog.ConfirmDialog;
import com.oplay.giftcool.util.AppInfoUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.socks.library.KLog;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by zsigui on 16-1-6.
 */
public class AboutFragment extends BaseFragment {

	private final static String PAGE_NAME = "关于";
	private RelativeLayout rlUpdate;
	private RelativeLayout rlQQ;
	private TextView tvUpdate;
	private TextView tvQQ;
	private TextView tvVersion;
	private Context mContext;

	private UpdateInfo mUpdateInfo;

	public static AboutFragment newInstance() {
		return new AboutFragment();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_about);
		rlUpdate = getViewById(R.id.rl_update);
		rlQQ = getViewById(R.id.rl_qq);
		tvUpdate = getViewById(R.id.tv_update);
		tvQQ = getViewById(R.id.tv_qq);
		tvVersion = getViewById(R.id.tv_version);
	}

	@Override
	protected void setListener() {
		rlUpdate.setOnClickListener(this);
		rlQQ.setOnClickListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		mContext = getContext();
		tvUpdate.setText(mContext.getResources().getString(R.string.st_about_checking_update));
		tvVersion.setText("礼包酷 " + DecryptDataModel.SDK_VER_NAME);
		tvQQ.setText(getQQInfo());
	}

	//根据初始化结果配置官方QQ群信息
	private String getQQInfo() {
		ArrayList<String> qqInfo = mApp.getQQInfo();
		String qqStrLocal = "515318514";
		String qqStrServer = "";
		if (qqInfo != null && qqInfo.size() > 0) {
			for (String qq : qqInfo) {
				qqStrServer = qq + ',';
			}
			if (qqStrServer.length() > 0) {
				qqStrServer = qqStrServer.substring(0, qqStrServer.length() - 1);
			}
		}
		if (TextUtils.isEmpty(qqStrServer)) {
			return qqStrLocal;
		} else {
			return qqStrServer;
		}
	}

	@Override
	protected void lazyLoad() {
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (NetworkUtil.isConnected(getContext())) {
					ReqInitApp data = new ReqInitApp();
					data.curVersionCode = AppInfoUtil.getAppVerCode(getContext());
					JsonReqBase<ReqInitApp> reqData = new JsonReqBase<>(data);
					Global.getNetEngine().checkUpdate(reqData)
							.enqueue(new Callback<JsonRespBase<UpdateInfo>>() {
								@Override
								public void onResponse(Response<JsonRespBase<UpdateInfo>> response,
								                       Retrofit retrofit) {
									if (!mCanShowUI) {
										return;
									}
									if (response != null && response.isSuccess() && response.body() != null &&
											response.body().getCode() == StatusCode.SUCCESS) {
										mUpdateInfo = response.body().getData();
										if (mUpdateInfo != null && mUpdateInfo.checkoutUpdateInfo(getContext())) {
											setUpdate(String.format(mContext.getResources().getString(R.string
													.st_about_wait_update_text), mUpdateInfo.versionName));
											return;
										}
									}
									setUpdate(mContext.getResources().getString(R.string.st_about_update_text));
								}

								@Override
								public void onFailure(Throwable t) {
									if (!mCanShowUI) {
										return;
									}
									if (AppDebugConfig.IS_DEBUG) {
										KLog.e(AppDebugConfig.TAG_FRAG, t);
									}
									setUpdate(mContext.getResources().getString(R.string.st_about_check_update_failed));
								}
							});
				} else {
					setUpdate(mContext.getResources().getString(R.string.st_about_check_update_failed));
				}
			}
		});
	}

	private void setUpdate(final String str) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (tvUpdate != null) {
					tvUpdate.setText(str);
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.rl_update:
				handleUpdate();
				break;
			case R.id.rl_qq:
				break;
		}
	}

	private void handleUpdate() {
		if (mUpdateInfo == null || !mUpdateInfo.checkoutUpdateInfo(getContext())) {
			return;
		}
		final IndexGameNew appInfo = new IndexGameNew();
		appInfo.id = Global.GIFTCOOL_GAME_ID;
		appInfo.name = getString(R.string.app_name);
		appInfo.apkFileSize = mUpdateInfo.apkFileSize;
		//没icon地址，随便填个
		appInfo.img = mUpdateInfo.downloadUrl;
		appInfo.downloadUrl = mUpdateInfo.downloadUrl;
		appInfo.destUrl = mUpdateInfo.downloadUrl;
		appInfo.packageName = mUpdateInfo.packageName;
		appInfo.versionName = mUpdateInfo.versionName;
		appInfo.size = appInfo.getApkFileSizeStr();
		appInfo.initAppInfoStatus(getContext());
		ConfirmDialog confirmDialog = getUpdateDialog(appInfo, mUpdateInfo.content);
		confirmDialog.show(getFragmentManager(), "update");
	}

	private ConfirmDialog getUpdateDialog(final IndexGameNew appInfo, final String content) {
		final ConfirmDialog confirmDialog = ConfirmDialog.newInstance();
		confirmDialog.setTitle("更新提示");
		confirmDialog.setContent(content);
		confirmDialog.setPositiveBtnText("马上更新");
		confirmDialog.setNegativeBtnText("暂不更新");
		confirmDialog.setListener(new BaseFragment_Dialog.OnDialogClickListener() {
			@Override
			public void onCancel() {
				confirmDialog.dismiss();
			}

			@Override
			public void onConfirm() {
				appInfo.startDownload();
				confirmDialog.dismiss();
			}
		});
		return confirmDialog;
	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}
}
