package net.youmi.android.libs.common.global.sensor;

import android.content.Context;

import net.youmi.android.libs.common.CommonConstant;

/**
 * 只记录墙上的广告的包名状态
 * 
 * @author zhitaocai
 * @author zhitaocai edit on 2014-7-8
 * 
 */
class AccelerationSensorDBManager extends BaseSensorDBManager {

	private static AccelerationSensorDBManager instance;

	AccelerationSensorDBManager(Context context, BaseSensorDBHelper helper) {
		super(context, helper);
	}

	synchronized static AccelerationSensorDBManager getInstance(Context context) throws NullPointerException {
		if (instance == null) {
			BaseSensorDBHelper dbHelper = new BaseSensorDBHelper(context, CommonConstant.get_DatabaseName_AccelerationSensor());
			instance = new AccelerationSensorDBManager(context.getApplicationContext(), dbHelper);
		}
		return instance;
	}
}