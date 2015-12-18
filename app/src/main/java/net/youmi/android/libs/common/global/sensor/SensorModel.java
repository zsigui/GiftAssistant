package net.youmi.android.libs.common.global.sensor;

import net.youmi.android.libs.common.debug.DLog;

import java.text.SimpleDateFormat;

public class SensorModel {

	long mGenerateTime;

	public float v0;

	public float v1;

	public float v2;

	public float v3;

	public float v4;

	public float v5;

	@Override
	public String toString() {
		if (DLog.isGlobalLog) {
			StringBuilder builder = new StringBuilder();
			builder.append("mGenerateTime=");
			builder.append(mGenerateTime);
			builder.append("  【");
			builder.append(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(mGenerateTime));
			builder.append("】");
			builder.append("  v0=");
			builder.append(v0);
			builder.append("  v1=");
			builder.append(v1);
			builder.append("  v2=");
			builder.append(v2);
			builder.append("  v3=");
			builder.append(v3);
			builder.append("  v4=");
			builder.append(v4);
			builder.append("  v5=");
			builder.append(v5);
			return builder.toString();
		}
		return super.toString();
	}
}
