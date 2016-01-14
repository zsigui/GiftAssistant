package net.youmi.android.libs.common.global.model;

import android.os.BatteryManager;

import net.youmi.android.libs.common.debug.Debug_SDK;

public class Model_Battery {

	/**
	 * 当前剩余电量百分比
	 */
	public int currentBatteryPercent;

	/**
	 * 是否正在充电
	 */
	public boolean isInCharge;

	/**
	 * 充电方式
	 * <ul>
	 * <li>0： 没有进行充电</li>
	 * <li>{@link android.os.BatteryManager#BATTERY_PLUGGED_AC} 充电器充电</li>
	 * <li>{@link android.os.BatteryManager#BATTERY_PLUGGED_USB} USB充电</li>
	 * <li>{@link android.os.BatteryManager#BATTERY_PLUGGED_WIRELESS} 无线充电</li>
	 * </ul>
	 * @see
	 */
	public int inChargeType;

	@Override
	public String toString() {
		if (Debug_SDK.isDebug) {
			StringBuilder builder = new StringBuilder();
			builder.append("Model_Battery [\n  currentBatteryPercent=");
			builder.append(currentBatteryPercent);
			builder.append(" \n  isInCharge=");
			builder.append(isInCharge);
			builder.append(" \n  inChargeType=");
			builder.append(inChargeType);
			switch (inChargeType) {
			case 0:
				builder.append(" --> 当前没有充电");
				break;
			case BatteryManager.BATTERY_PLUGGED_AC:
				builder.append(" --> 充电器充电");
				break;
			case BatteryManager.BATTERY_PLUGGED_USB:
				builder.append(" --> usb充电");
				break;
			case BatteryManager.BATTERY_PLUGGED_WIRELESS:
				builder.append(" --> 充电");
				break;
			default:
				builder.append(" --> 未知充电方式");
				break;
			}
			builder.append("\n]");
			return builder.toString();
		}
		return super.toString();
	}

}
