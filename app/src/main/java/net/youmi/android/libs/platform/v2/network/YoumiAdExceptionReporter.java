package net.youmi.android.libs.platform.v2.network;

import android.content.Context;

import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.common.debug.DLog;
import net.youmi.android.libs.common.v2.network.core.BaseHttpRequesterModel;
import net.youmi.android.libs.common.v2.network.core.BaseHttpResponseModel;
import net.youmi.android.libs.common.v2.network.exception.AbsExceptionReporter;
import net.youmi.android.libs.common.v2.network.exception.NetworkExceptionConfig;
import net.youmi.android.libs.platform.SDKBuild;
import net.youmi.android.libs.platform.global.Global_DeveloperConfig;
import net.youmi.android.libs.platform.global.Global_Runtime_ClientId;

import org.json.JSONObject;

/**
 * 有米异常上报实体类
 */
class YoumiAdExceptionReporter extends AbsExceptionReporter {

	/**
	 * @param context
	 * @param baseHttpRequesterModel http请求参数模型
	 * @param baseHttpResponseModel  http请求结果参数模型
	 * @param networkExceptionConfig 异常上报信息
	 */
	public YoumiAdExceptionReporter(Context context, BaseHttpRequesterModel baseHttpRequesterModel,
			BaseHttpResponseModel baseHttpResponseModel, NetworkExceptionConfig networkExceptionConfig) {
		super(context, baseHttpRequesterModel, baseHttpResponseModel, networkExceptionConfig);
	}

	@Override
	protected void addExtendParamsToThisJson(JSONObject jo) {
		try {
			Global_Runtime_ClientId cid = new Global_Runtime_ClientId(mContext);
			Basic_JSONUtil.putString(jo, "c".trim() + "id", cid.getCid());

			// 协议版本号这个有点纠结，因为目前不是所有请求采用同一个的，所以先随便弄个
			Basic_JSONUtil.putString(jo, "pc".trim() + "v", SDKBuild.PROTOCOL_CORE_VERSION);

			Basic_JSONUtil.putString(jo, "app".trim() + "id", Global_DeveloperConfig.getAppID(mContext));
		} catch (Exception e) {
			if (DLog.isNetLog) {
				DLog.te(DLog.mNetTag, this, e);
			}
		}
	}

}
