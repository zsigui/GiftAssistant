package com.oplay.giftcool.util;

import android.content.Context;
import android.text.TextUtils;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.model.MobileInfoModel;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.socks.library.KLog;

import net.youmi.android.libs.common.global.Global_Runtime_SystemInfo;
import net.youmi.android.libs.platform.global.Global_Runtime_ClientId;

/**
 * Created by zsigui on 15-12-25.
 */
public class CommonUtil {


	/**
	 * 需要先调用initMobileInfoModel
	 *
	 * @param reqBase
	 */
	public static void addCommonParams(JsonReqBase reqBase, int cmd) {
		if (reqBase == null) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_UTIL, "parameter reqBase is not allowed to null");
			}
			return;
		}
		MobileInfoModel model = MobileInfoModel.getInstance();
		if (!model.isInit()) {
			initMobileInfoModel(AssistantApp.getInstance().getApplicationContext());
		}
		reqBase.imsi = model.getImsi();
		reqBase.imei = model.getImei();
		reqBase.cid = model.getCid();
		reqBase.mac = model.getMac();
		reqBase.chn = model.getChn();
		reqBase.apn = model.getApn();
		reqBase.cn = model.getCn();
		reqBase.dd = model.getDd();
		reqBase.dv = model.getDv();
		reqBase.os = model.getOs();
		reqBase.version = model.getVersion();
		reqBase.cmd = cmd;
	}

	public static void initMobileInfoModel(Context context) {
		MobileInfoModel model = MobileInfoModel.getInstance();
		if (!model.isInit()) {
			model.setImei(Global_Runtime_SystemInfo.getImei(context));
			model.setImsi(Global_Runtime_SystemInfo.getImsi(context));
			model.setCid(new Global_Runtime_ClientId(context).getCid());
			model.setMac(Global_Runtime_SystemInfo.getMac(context));
			model.setChn(AssistantApp.getInstance().getChannelId());
			model.setApn(AppInfoUtil.getAPN(context));
			model.setCn(AppInfoUtil.getSPN(context));
			model.setDd(Global_Runtime_SystemInfo.getDeviceModel());
			model.setDv(Global_Runtime_SystemInfo.getManufacturerInfo());
			model.setOs(Global_Runtime_SystemInfo.getDeviceOsRelease());
			model.setVersion(AppConfig.SDK_VER);
			if (TextUtils.isEmpty(model.getImei()) || TextUtils.isEmpty(model.getImsi())
					|| TextUtils.isEmpty(model.getCid()) || TextUtils.isEmpty(model.getMac())
					|| model.getChn() == -1 || TextUtils.isEmpty(model.getApn())
					|| TextUtils.isEmpty(model.getCn()) || TextUtils.isEmpty(model.getDd())
					|| TextUtils.isEmpty(model.getDv()) || TextUtils.isEmpty(model.getOs())) {
				model.setInit(false);
			} else {
				model.setInit(true);
			}
		}
	}
}
