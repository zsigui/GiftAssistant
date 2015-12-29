package net.youmi.android.libs.common.global.sensor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 获取新的非常见，但是唯一性很高的设备参数信息
 *
 * @author zhitaocai
 * @since 2015-1-11下午12:00:49
 */
public class Global_Runtime_SystemInfo_Sensor {

	/**
	 * 每次收集的传感器参数的容量
	 */
	private final static int CAPACITY = 30;

	/**
	 * 每次收集的传感器参数，最长持续CAPACITYs，不要过多地消耗电量
	 */
	private final static int LIFE_TIME = CAPACITY * 1000;

	private static Context mContext;

	@SuppressLint("NewApi")
	public final static String getSensorMessage(Context context) {
		try {

			SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
			List<Sensor> allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
			int size = allSensors.size();
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("<---当前获取到%d个传感器--->\n", size));
			if (Debug_SDK.isGlobalLog) {
				for (Sensor sensor : allSensors) {
					sb.append("\n  ");
					sb.append("\n  名称:").append(sensor.getName());
					sb.append("\n  设备版本：").append(sensor.getVersion());
					sb.append("\n  厂商：").append(sensor.getVendor());
					sb.append("\n  类型：").append(sensor.getType());
					sb.append("\n  传感器功率/mA毫安：").append(sensor.getPower());
					sb.append("\n  传感器精度：").append(sensor.getResolution());
					sb.append("\n  传感器的最大值：").append(sensor.getMaximumRange());
					sb.append("\n  最小延迟时间：").append(sensor.getMinDelay());
				}
				Debug_SDK
						.td(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Sensor.class, "<------当前获取到%d个传感器\n%s\n------>",
								size,
								sb.toString());
			}
			return sb.toString();
		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Sensor.class, e);
			}
		}
		return null;
	}

	/**
	 * 开始收集传感器参数，收集到的数据将会写入到数据库中，需要的话，从数据库中获取，也会在30s后自动关闭
	 *
	 * @param context
	 */
	public final static void startCollectSensorParams(Context context) {
		try {
			mContext = context.getApplicationContext();
		} catch (Exception e) {
			mContext = context;
		}
		startSelfCloseLifeTimer(mContext);
		startPollingTimer();
		registerAccelerometerSensor(mContext);
		registerGyroscopeSensor(mContext);
		registerLightSensor(mContext);
		// registerLinearAccelerationSensor(context);
	}

	/**
	 * 手动关闭传感器，同时取消收集传感器参数，节省用电
	 *
	 * @param context
	 */
	public final static void endCollectSensorParams(Context context) {
		try {
			mContext = context.getApplicationContext();
		} catch (Exception e) {
			mContext = context;
		}
		endPollingTimer();
		unRegisterSensor(mContext);
	}

	/**
	 * 获取加速度传感器的数据(最近30组)，并且获取成功之后，清楚数据库中的记录
	 *
	 * @param context
	 *
	 * @return
	 */
	public final static List<SensorModel> pollAccelerometerSensorParams(Context context) {
		return pollSensorParams(context, Sensor.TYPE_ACCELEROMETER);
	}

	/**
	 * 获取光线传感器的数据(最近30组)，并且获取成功之后，清楚数据库中的记录
	 *
	 * @param context
	 *
	 * @return
	 */
	public static List<SensorModel> pollLightSensorParams(Context context) {
		return pollSensorParams(context, Sensor.TYPE_LIGHT);
	}

	/**
	 * 获取陀螺仪传感器的数据(最近30组)，并且获取成功之后，清楚数据库中的记录
	 *
	 * @param context
	 *
	 * @return
	 */
	public static List<SensorModel> pollGyroscopeSensorParams(Context context) {
		return pollSensorParams(context, Sensor.TYPE_GYROSCOPE);
	}

	/**
	 * 获取光线传感器的数据(最近30组)，并且获取成功之后，清楚数据库中的记录
	 *
	 * @param context
	 *
	 * @return
	 */
	private static List<SensorModel> pollSensorParams(Context context, int sensorType) {
		try {
			mContext = context.getApplicationContext();
		} catch (Exception e) {
			mContext = context;
		}
		List<SensorModel> list = null;
		switch (sensorType) {
		case Sensor.TYPE_ACCELEROMETER:
			list = AccelerationSensorDBManager.getInstance(mContext).queryAll();
			break;
		case Sensor.TYPE_GYROSCOPE:
			list = GyroscopeSensorDBManager.getInstance(mContext).queryAll();
			break;
		case Sensor.TYPE_LIGHT:
			list = LightSensorDBManager.getInstance(mContext).queryAll();
			break;
		default:
			break;
		}
		if (list == null || list.size() <= 0) {
			return null;
		}
		String sensorTips = "";
		if (Debug_SDK.isGlobalLog) {
			switch (sensorType) {
			case Sensor.TYPE_ACCELEROMETER:
				sensorTips = "加速度";
				break;
			case Sensor.TYPE_GYROSCOPE:
				sensorTips = "陀螺仪";
				break;
			case Sensor.TYPE_LIGHT:
				sensorTips = "光线";
				break;
			default:
				break;
			}
		}
		// 只获取最近30组数据
		if (list.size() >= CAPACITY) {
			try {
				if (Debug_SDK.isGlobalLog) {
					Debug_SDK.ti(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Sensor.class, "*****%s传感器目前数据长度多于%d个，需要删减*****",
							sensorTips, CAPACITY);
				}
				List<SensorModel> temp = new ArrayList<SensorModel>();
				for (int i = CAPACITY; i < list.size(); ++i) {
					temp.add(list.get(i));
				}
				list.removeAll(temp);
			} catch (Exception e) {
			}
		}
		if (Debug_SDK.isGlobalLog) {
			Debug_SDK.ti(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Sensor.class, "*****%s传感器最近%d组数据*****", sensorTips,
					CAPACITY);
			for (int i = 0; i < list.size(); ++i) {
				SensorModel model = list.get(i);
				if (model != null) {
					Debug_SDK.ti(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Sensor.class, model.toString());
				}
			}
		}

		// ---------------
		// 获取到数据之后，清空数据库，不要让开发者的应用越来越大
		switch (sensorType) {
		case Sensor.TYPE_ACCELEROMETER:
			AccelerationSensorDBManager.getInstance(context).deleteAll();
			break;
		case Sensor.TYPE_GYROSCOPE:
			GyroscopeSensorDBManager.getInstance(context).deleteAll();
			break;
		case Sensor.TYPE_LIGHT:
			LightSensorDBManager.getInstance(context).deleteAll();
			break;
		default:
			break;
		}

		return list;
	}

	/**
	 * 获取进行过漂移补偿之后的的陀螺仪参数
	 *
	 * @param context
	 */
	@SuppressLint("InlinedApi")
	static void registerGyroscopeSensor(Context context) {
		registerSensor(context, Sensor.TYPE_GYROSCOPE);
	}

	/**
	 * 获取重力加速度
	 *
	 * @param context
	 */
	static void registerAccelerometerSensor(Context context) {
		registerSensor(context, Sensor.TYPE_ACCELEROMETER);
	}

	/**
	 * 获取线性加速计的数据（因为只是需要确定数据的变动情况，因此没有进行偏差抵消）
	 *
	 * @param context
	 */
	@SuppressLint("InlinedApi")
	static void registerLinearAccelerationSensor(Context context) {
		registerSensor(context, Sensor.TYPE_LINEAR_ACCELERATION);
	}

	/**
	 * 获取线性加速计的数据（因为只是需要确定数据的变动情况，因此没有进行偏差抵消）
	 *
	 * @param context
	 */
	static void registerLightSensor(Context context) {
		registerSensor(context, Sensor.TYPE_LIGHT);
	}

	private static void registerSensor(Context context, int sensorType) {
		SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		Sensor sensor = sensorManager.getDefaultSensor(sensorType);
		if (sensor != null) {

			// SENSOR_DELAY_NORMAL:取得倾斜度的时候使用（缺省）匹配屏幕方向的变化
			// SENSOR_DELAY_UI：匹配用户接口
			// SENSOR_DELAY_GAME：匹配游戏
			// SENSOR_DELAY_FASTEST.：匹配所能达到的最快
			sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_UI);
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.ti(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Sensor.class, "成功注册sensor : %d", sensorType);
			}
		} else {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.ti(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Sensor.class, "不存在sensor ： %d", sensorType);
			}
		}
	}

	/**
	 * 回收所有的sensor
	 *
	 * @param context
	 */
	private static void unRegisterSensor(Context context) {
		SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		if (sensorManager != null) {
			if (sensorEventListener != null) {
				sensorManager.unregisterListener(sensorEventListener);
				if (Debug_SDK.isGlobalLog) {
					Debug_SDK.ti(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Sensor.class, "成功移除所有的sensor");
				}
			}
		}
	}

	// ------------------------------------------------------------------------------------------------

	/**
	 * 是否可以收集传感器数据
	 */
	private static boolean flag = true;

	/**
	 * 光线传感器的数据是否已经在有效的时间内收集好
	 */
	private static boolean mLightSensorFlag = true;

	/**
	 * 加速度传感器的数据是否已经在有效的时间内收集好
	 */
	private static boolean mAccelerateSensorFlag = true;

	/**
	 * 陀螺仪的数据是否已经在有效的时间内收集好
	 */
	private static boolean mGyroscopeSensorFlag = true;

	private static int count = 0;

	// ------------------------------------------------------------------------------------------------
	// 每隔1秒钟更新一次数据

	private static Timer mPollingTimer;

	private static void startPollingTimer() {
		endPollingTimer();
		try {
			mPollingTimer = new Timer();
			mPollingTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					flag = !flag;
					if (count % 2 == 0) {
						mLightSensorFlag = false;
						mAccelerateSensorFlag = false;
						mGyroscopeSensorFlag = false;
					}

					if (Debug_SDK.isGlobalLog) {
						if (count % 2 == 0) {
							Debug_SDK.ti(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Sensor.class, "===== 第 %d 秒 =====",
									count / 2);
						}
					}
					count++;
				}
			}, 0, 500);
		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Sensor.class, e);
			}
		}
	}

	private static void endPollingTimer() {
		try {
			if (mPollingTimer != null) {
				mPollingTimer.cancel();
				mPollingTimer.purge();
				mPollingTimer = null;
			}
			count = 0;
			flag = true;
			mLightSensorFlag = true;
			mAccelerateSensorFlag = true;
			mGyroscopeSensorFlag = true;
		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Sensor.class, e);
			}
		}
	}

	private static Timer mLifeTimer;

	/**
	 * 启动一个延迟的任务，用于一定时间之后删除传感器监听器
	 *
	 * @param context
	 */
	private static void startSelfCloseLifeTimer(final Context context) {
		try {
			if (mLifeTimer != null) {
				if (Debug_SDK.isGlobalLog) {
					Debug_SDK.tv(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Sensor.class, "当前已经有自结束定时器");
				}
				return;
			}
			mLifeTimer = new Timer();
			mLifeTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					endCollectSensorParams(context);
					try {
						mLifeTimer.cancel();
						mLifeTimer.purge();
						mLifeTimer = null;
					} catch (Throwable e) {
						if (Debug_SDK.isGlobalLog) {
							Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Sensor.class, e);
						}
					}
				}
			}, LIFE_TIME);
		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Sensor.class, e);
			}
		}
	}

	// // ------------------------------------------------------------------------------------------------
	// // 下面这些事陀螺仪用到的参数
	// // 创建常量将纳秒转换为妙
	// private static final float NS2S = 1.0f / 1000000000.0f;
	//
	// private static float[] deltaRotationVector = new float[4];
	//
	// private static float timestamp;
	//
	// // ------------------------------------------------------------------------------------------------
	// // 下面这些是加速度传感器用到的参数
	//
	// private static float[] gravity = new float[3];

	private static SensorEventListener sensorEventListener = new SensorEventListener() {

		/**
		 * 当数据变化的时候被触发调用
		 * <ul>
		 * <li>Accuracy:精度</li>
		 * <li>Sensor:发生变化的感应器</li>
		 * <li>Timestamp:发生的时间，单位是纳秒</li>
		 * <li>Values:发生变化后的值,这个是一个长度为3数组</li>
		 * </ul>
		 */
		@SuppressLint("NewApi")
		@Override
		public void onSensorChanged(SensorEvent event) {
			// 这里主要是为了2s触发一次，不然就会一直刷数据 - -！
			if (flag) {
				return;
			}

			switch (event.sensor.getType()) {
			// 光线感应器:只需要values[0]的值，其他两个都为0.而values[0]就是我们开发光线感应器所需要的，单位是：lux照度单位
			case Sensor.TYPE_LIGHT:
				if (mLightSensorFlag) {
					return;
				}
				float lux = event.values[0];
				if (Debug_SDK.isGlobalLog) {
					Debug_SDK.td(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Sensor.class, "光线感应器: lux -> %f", lux);
				}
				try {
					SensorModel lightModel = new SensorModel();
					lightModel.mGenerateTime = System.currentTimeMillis();
					lightModel.v0 = new BigDecimal(event.values[0]).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
					LightSensorDBManager.getInstance(mContext).add(lightModel);
				} catch (Throwable e) {
					if (Debug_SDK.isGlobalLog) {
						Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Sensor.class, e);
					}
				}
				mLightSensorFlag = true;
				break;
			// // 线性加速感应器 : 线性加速度 = 加速度 - 重力加速度
			// case Sensor.TYPE_LINEAR_ACCELERATION:
			//
			// float la_X = event.values[0];
			// float la_Y = event.values[1];
			// float la_Z = event.values[2];
			// if (Debug_SDK.isGlobalLog) {
			// Debug_SDK.td(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Sensor.class,
			// "线性加速感应器原始数据(取小数点后两位)：X-> %.2f     Y-> %.2f     Z-> %.2f", la_X, la_Y, la_Z);
			// }
			// break;

			// 加速度感应器
			case Sensor.TYPE_ACCELEROMETER:
				if (mAccelerateSensorFlag) {
					return;
				}
				// 如果设备是平放在桌子上的（没有加速度），加速度计会读到g = 9.81 m/s2。
				// 同理，设备在自由落体或以 9.81 m/s2 的加速度坠向地面时，加速度计会读到 g = 0 m/s2。
				// 因此，要测出设备真实的加速度，必须排除加速计数据中的重力干扰。
				// 这可以通过高通滤波器来实现。
				// 反之，低通滤波器则可以用于分离出重力加速度值。
				float a_X = event.values[0];
				float a_Y = event.values[1];
				float a_Z = event.values[2];
				if (Debug_SDK.isGlobalLog) {
					Debug_SDK.td(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Sensor.class,
							"加速度感应器原始数据(取小数点后三位)：X-> %.3f     Y-> %.3f     Z-> %.3f", a_X, a_Y, a_Z);
				}
				try {
					SensorModel accelerateModel = new SensorModel();
					accelerateModel.mGenerateTime = System.currentTimeMillis();
					// 四舍五入后，保留3位小数位
					accelerateModel.v0 = new BigDecimal(event.values[0]).setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();
					accelerateModel.v1 = new BigDecimal(event.values[1]).setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();
					accelerateModel.v2 = new BigDecimal(event.values[2]).setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();
					AccelerationSensorDBManager.getInstance(mContext).add(accelerateModel);

				} catch (Throwable e) {
					if (Debug_SDK.isGlobalLog) {
						Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Sensor.class, e);
					}
				}
				// final float alpha = 0.8f;
				//
				// // 用低通滤波器分离出重力加速度
				// gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
				// gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
				// gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
				//
				// // 用高通滤波器剔除重力干扰
				// float linear_acceleration_X = event.values[0] - gravity[0];
				// float linear_acceleration_Y = event.values[1] - gravity[1];
				//
				//
				// if (Debug_SDK.isGlobalLog) {
				// Debug_SDK.td(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_20150111.class,
				// "处理后加速度感应器(取小数点后两位)：X-> %.2f     Y-> %.2f     Z-> %.2f", linear_acceleration_X,
				// linear_acceleration_Y, linear_acceleration_Z);
				// }
				// a_X -= SensorManager.GRAVITY_EARTH;
				// a_Y -= SensorManager.GRAVITY_EARTH;
				// if (Debug_SDK.isGlobalLog) {
				// Debug_SDK.td(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Sensor.class,
				// "处理后加速度感应器(取小数点后两位)：X-> %.2f     Y-> %.2f     Z-> %.2f", a_X, a_X, a_Z);
				// }

				mAccelerateSensorFlag = true;
				break;

			// 陀螺仪
			case Sensor.TYPE_GYROSCOPE:
				if (mGyroscopeSensorFlag) {
					return;
				}
				float axisX = event.values[0];
				float axisY = event.values[1];
				float axisZ = event.values[2];
				if (Debug_SDK.isGlobalLog) {
					Debug_SDK.td(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Sensor.class,
							"陀螺仪原始数据(取小数点后三位)：axisX-> %.3f     axisY-> %.3f     axisZ-> %.3f", axisX, axisY, axisZ);
				}
				try {
					SensorModel gyroscopeModel = new SensorModel();
					gyroscopeModel.mGenerateTime = System.currentTimeMillis();
					gyroscopeModel.v0 = new BigDecimal(event.values[0]).setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();
					gyroscopeModel.v1 = new BigDecimal(event.values[1]).setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();
					gyroscopeModel.v2 = new BigDecimal(event.values[2]).setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();
					GyroscopeSensorDBManager.getInstance(mContext).add(gyroscopeModel);

				} catch (Throwable e) {
					if (Debug_SDK.isGlobalLog) {
						Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Sensor.class, e);
					}
				}
				// // 根据陀螺仪采样数据计算出此次事件间隔的偏移量后，它将与当前旋转向量相乘
				// if (timestamp != 0) {
				// final float dT = (event.timestamp - timestamp) * NS2S;
				// // 未规格化的旋转向量坐标值
				// float axisX = event.values[0];
				// float axisY = event.values[1];
				// float axisZ = event.values[2];
				//
				// // 计算角速度
				// double omegaMagnitude = Math.sqrt((double) (axisX * axisX + axisY * axisY + axisZ * axisZ));
				//
				// // 如果旋转向量偏移值足够大，可以获得坐标值，则规格化旋转向量
				// // (也就是说，EPSILON为计算偏移量的起步值。小于该值的偏移视为误差，不予计算)
				// if (omegaMagnitude > 2000000000) {
				// axisX /= omegaMagnitude;
				// axisY /= omegaMagnitude;
				// axisZ /= omegaMagnitude;
				// }
				//
				// // 为了得到此次取样间隔的旋转偏移量，需要把围绕坐标轴旋转的角速度与时间间隔合并表示。
				// // 在转换为旋转矩阵之前，我们要把围绕坐标轴旋转的角度表示为四元组
				// double thetaOverTwo = omegaMagnitude * dT / 2.0f;
				// double sinThetaOverTwo = Math.sin(thetaOverTwo);
				// double cosThetaOverTwo = Math.cos(thetaOverTwo);
				// deltaRotationVector[0] = (float) sinThetaOverTwo * axisX;
				// deltaRotationVector[1] = (float) sinThetaOverTwo * axisY;
				// deltaRotationVector[2] = (float) sinThetaOverTwo * axisZ;
				// deltaRotationVector[3] = (float) cosThetaOverTwo;
				// }
				// timestamp = event.timestamp;
				// float[] deltaRotationMatrix = new float[9];
				// SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
				//
				// // 为了得到旋转后的向量，用户代码应该把我们计算出来的偏移量与当前向量叠加。
				// // rotationCurrent = rotationCurrent * deltaRotationMatrix;
				//
				// // User code should concatenate the delta rotation we computed with the current rotation
				// // in order to get the updated rotation.
				// // rotationCurrent = rotationCurrent * deltaRotationMatrix;
				//
				// if (Debug_SDK.isGlobalLog) {
				// Debug_SDK
				// .ti(Debug_SDK.mGlobalTag,
				// Global_Runtime_SystemInfo_20150111.class,
				// "deltaRotationVector[0]-> %.2f     deltaRotationVector[1]-> %.2f     deltaRotationVector[2]-> %.2f
				// deltaRotationVector[3]-> %.2f]",
				// deltaRotationVector[0], deltaRotationVector[1], deltaRotationVector[2],
				// deltaRotationVector[3]);
				// }
				mGyroscopeSensorFlag = true;
				break;
			default:
				break;
			}
		}

		/**
		 * 当获得数据的精度发生变化的时候被调用，比如突然无法获得数据时
		 */
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};
}